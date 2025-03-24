package com.sparta.logistics.hub_service.hubroute.application.service;

import com.sparta.logistics.hub_service.global.utils.PaginationUtils;
import com.sparta.logistics.hub_service.hub.domain.entity.Hub;
import com.sparta.logistics.hub_service.hub.domain.repository.HubRepository;
import com.sparta.logistics.hub_service.hubroute.application.dto.request.HubRouteUpdateRequestDto;
import com.sparta.logistics.hub_service.hubroute.application.dto.response.HubRouteDetailResponseDto;
import com.sparta.logistics.hub_service.hubroute.application.dto.response.HubRouteListResponseDto;
import com.sparta.logistics.hub_service.hubroute.application.dto.response.HubRouteUpdateResponseDto;
import com.sparta.logistics.hub_service.hubroute.application.dto.response.KakaoResponseDto;
import com.sparta.logistics.hub_service.hubroute.domain.entity.HubRoute;
import com.sparta.logistics.hub_service.hubroute.domain.repository.HubRouteRepository;
import com.sparta.logistics.hub_service.hubroute.infrastructure.client.KakaoClient;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class HubRouteServiceImpl implements HubRouteService {

  private final HubRouteRepository hubRouteRepository;
  private final HubRepository hubRepository;
  private final KakaoClient kakaoClient;

  // 허브 루트 단일 조회
  @Override
  public HubRouteDetailResponseDto getHubRouteDetail(UUID hubRoutesId) {
    HubRoute hubRoute = hubRouteRepository.findById(hubRoutesId)
        .orElseThrow(() -> new IllegalArgumentException("해당하는 허브 이동 경로가 없습니다."));
    return HubRouteDetailResponseDto.toResponse(hubRoute);
  }

  // 허브 루트 전체 조회(목록) 및 검색
  @Override
  @Cacheable(
      value = "hubRoutes",
      key = "((#startHubId != null ? #startHubId.toString() : '')) + '-' + ((#endHubId != null ? #endHubId.toString() : ''))"
  )
  public Page<HubRouteListResponseDto> getHubRouteList(UUID startHubId, UUID endHubId,
      Pageable pageable) {

    log.debug("DB 조회 시작: startHubId = {}, endHubId = {}", startHubId, endHubId);

    List<HubRoute> hubRoutes;

    if (startHubId != null && endHubId != null) {
      hubRoutes = hubRouteRepository.findByStartHubIdAndEndHubId(startHubId,
          endHubId);
    } else if (startHubId != null || endHubId != null) {
      hubRoutes = hubRouteRepository.findByStartHubIdOrEndHubId(startHubId,
          endHubId);
    } else {
      hubRoutes = hubRouteRepository.findAll();
    }
    List<HubRouteListResponseDto> dtoList = hubRoutes.stream()
        .map(HubRouteListResponseDto::toResponse)
        .collect(Collectors.toList());

    return PaginationUtils.paginateList(dtoList, pageable);

  }

  // 허브 루트 수정
  @Override
  @Transactional
  public HubRouteUpdateResponseDto updateHubRoute(UUID hubId, HubRouteUpdateRequestDto requestDto,
      String userIdHeader) {
    HubRoute hubRoute = hubRouteRepository.findById(hubId)
        .orElseThrow(() -> new IllegalArgumentException("해당하는 허브 이동 경로가 없습니다."));

    Long currentId = Long.valueOf(userIdHeader);

    hubRoute.updateUpdateBy(currentId);
    hubRoute.updateDeliveryTime(requestDto.getDeliveryTime());
    hubRoute.updateDeliveryDistance(requestDto.getDeliveryDistance());

    HubRoute updateHubRoute = hubRouteRepository.save(hubRoute);
    return new HubRouteUpdateResponseDto(updateHubRoute);
  }

  public void updateRoutesForHub(Hub updateHub) {
    List<Hub> allOtherHubs = hubRepository.findAllExcept(updateHub.getId());

    log.info("업데이트된 허브 : {}", updateHub);

    for (Hub otherHub : allOtherHubs) {
      updateRouteBetween(updateHub, otherHub);
      log.info("정 방향 : {} 에서 {} 로 이동 경로", updateHub, otherHub);

      updateRouteBetween(otherHub, updateHub);
      log.info("반대 방향 : {} 에서 {} 로 이동 경로", otherHub, updateHub);
    }
  }

  private void updateRouteBetween(Hub from, Hub to) {
    KakaoClient.RouteInfo routeInfo = kakaoClient.getRouteInfo(
        from.getLongitude(), from.getLatitude(),
        to.getLongitude(), to.getLatitude()
    );

    log.info("허브 간 거리 갱신 - From: {}, To: {}, Distance: {}m, Duration: {}min",
        from.getHubName(), to.getHubName(),
        routeInfo.getDistance(), routeInfo.getTime());

    HubRoute route = hubRouteRepository.findOptionalByStartHubIdAndEndHubId(from.getId(),
            to.getId())
        .orElse(HubRoute.builder()
            .startHubId(from.getId())
            .endHubId(to.getId())
            .startHubName(from.getHubName())
            .endHubName(to.getHubName())
            .deliveryDistance(routeInfo.getDistance())
            .deliveryTime(routeInfo.getTime())
            .build()
        );

    route.updateStartHubName(from.getHubName());
    route.updateEndHubName(to.getHubName());
    route.updateDeliveryDistance(routeInfo.getDistance());
    route.updateDeliveryTime(routeInfo.getTime());

    hubRouteRepository.save(route);
  }


  @Override
  @Transactional
  public void deleteHubRoute(Long userId, UUID hubRoutesId, String userIdHeader) {

    HubRoute hubRoute = hubRouteRepository.findById(hubRoutesId)
        .orElseThrow(() -> new IllegalArgumentException("해당하는 허브 이동 경로 정보가 없습니다."));

    Long currentId = Long.valueOf(userIdHeader);

    hubRoute.setDeletedBy(currentId);
    hubRoute.setDeletedAt(LocalDateTime.now());
    hubRouteRepository.save(hubRoute);
  }

  @Transactional
  public void autoDeleteHubRoute(UUID hubId, String userIdHeader) {
    Long currentId = Long.valueOf(userIdHeader);

    List<HubRoute> relatedRoutes = hubRouteRepository
        .findAllByStartHubIdOrEndHubId(hubId, hubId);

    for (HubRoute route : relatedRoutes) {
      route.setDeletedBy(currentId);
      route.setDeletedAt(LocalDateTime.now());
    }

    hubRouteRepository.saveAll(relatedRoutes);
  }

}
