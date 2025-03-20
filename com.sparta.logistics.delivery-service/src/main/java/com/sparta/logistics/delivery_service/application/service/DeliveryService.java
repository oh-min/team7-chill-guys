package com.sparta.logistics.delivery_service.application.service;

import com.sparta.logistics.delivery_service.application.dto.request.DeliveryCreateRequestDto;
import com.sparta.logistics.delivery_service.application.dto.request.DeliveryUpdateRequestDto;
import com.sparta.logistics.delivery_service.application.dto.DeliveryManagerInfoDto;
import com.sparta.logistics.delivery_service.application.dto.response.DeliveryResponseDto;
import com.sparta.logistics.delivery_service.application.mapper.DeliveryInfoMapper;
import com.sparta.logistics.delivery_service.application.mapper.DeliveryMapper;
import com.sparta.logistics.delivery_service.application.service.mock.MockCompanyService;
import com.sparta.logistics.delivery_service.domain.model.Delivery;
import com.sparta.logistics.delivery_service.domain.model.DeliveryStatus;
import com.sparta.logistics.delivery_service.domain.repository.DeliveryRepository;
import com.sparta.logistics.delivery_service.infrastructure.client.CompanyClient;
import com.sparta.logistics.delivery_service.infrastructure.client.DeliveryManagerClient;
import com.sparta.logistics.delivery_service.infrastructure.client.ProductClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryRouteService deliveryRouteService;

    private final MockCompanyService mockCompanyService;

    private final DeliveryManagerClient deliveryManagerClient;
    private final CompanyClient companyClient;
    private final ProductClient productClient;

    private final ProducerService producerService;

    @Transactional
    public void createDelivery(DeliveryCreateRequestDto deliveryCreateRequestDto) {
        // 1. 상품을 보고 출발 허브 결정
        UUID productId = deliveryCreateRequestDto.getProductId();
        UUID departureHubId = productClient.getHubIdByProductId(productId);

        // 2. 수령 업체 보고 도착 허브 결정
        UUID destinationHubId = companyClient.getHubIdByCompanyId(deliveryCreateRequestDto.getCompanyId());

        // 3. 배송 저장
        Delivery delivery = DeliveryMapper.toEntity(deliveryCreateRequestDto, departureHubId, destinationHubId);
        deliveryRepository.save(delivery);

        deliveryRouteService.createDeliveryRoutes(delivery);

    }

    @Transactional(readOnly = true)
    public List<DeliveryResponseDto> getDeliveryList() {
        List<Delivery> deliveryList = deliveryRepository.findByAndDeletedAtIsNull();

        List<DeliveryResponseDto> deliveryResponseDtoList = new ArrayList<>();
        for(Delivery delivery : deliveryList) {
            deliveryResponseDtoList.add(DeliveryMapper.toDto(delivery));
        }
        return deliveryResponseDtoList;
    }

    @Transactional(readOnly = true)
    public DeliveryResponseDto getDelivery(UUID deliveryId) {
        Delivery delivery = deliveryRepository.findByIdAndDeletedAtIsNull(deliveryId)
                .orElseThrow(() -> new RuntimeException());
        return DeliveryMapper.toDto(delivery);
    }

    @Transactional
    public void updateDelivery(UUID deliveryId, DeliveryUpdateRequestDto deliveryUpdateRequestDto) {
        Delivery delivery = deliveryRepository.findByIdAndDeletedAtIsNull(deliveryId)
                .orElseThrow(() -> new RuntimeException("배송 찾을 수 없음"));

        if(delivery.isInfoChangeable()) {
            delivery.updateOf(deliveryUpdateRequestDto);
        } else throw new RuntimeException("배송중이라 변경 못함");

        deliveryRepository.save(delivery);
    }

    @Transactional
    public void deleteDelivery(UUID deliveryId) {
        Delivery delivery = deliveryRepository.findByIdAndDeletedAtIsNull(deliveryId)
                .orElseThrow(() -> new RuntimeException("배송 찾을 수 없음"));

        delivery.deletedOf();
    }

    @Transactional
    public void assignPendingDeliveries() {
        List<Delivery> pendingDeliveryies = deliveryRepository.findByDeliveryStatusAndDeletedAtIsNull(DeliveryStatus.PENDING);

        if(!pendingDeliveryies.isEmpty()) {

            for(Delivery delivery : pendingDeliveryies) {
                UUID departureHubId = delivery.getDepartureHubId();
                UUID destinationHubId = delivery.getDestinationHubId();
                String type = "COMPANY";
                DeliveryManagerInfoDto dto = deliveryManagerClient.assignDeliveryManager(departureHubId, destinationHubId, type);
                delivery.assignDeliveryManager(dto.getId());

                deliveryRepository.save(delivery);

                log.info("DeliveryManager Assigned");

                // 배송정보를 kafka로 전송
                producerService.sendInfo(type, DeliveryInfoMapper.toDto(delivery, dto.getSlackId()));
            }
        }
    }

    @Transactional
    public void changeDeliveryStatus(UUID deliveryId, DeliveryStatus deliveryStatus) {
        Delivery delivery = deliveryRepository.findByIdAndDeletedAtIsNull(deliveryId)
                .orElseThrow(() -> new RuntimeException("배송 찾을 수 없음"));

        delivery.changeDeliveryStatus(deliveryStatus);
        deliveryRepository.save(delivery);
    }
}
