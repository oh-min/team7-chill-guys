package com.sparta.logistics.delivery_service.infrastructure.client;

import com.sparta.logistics.delivery_service.application.dto.response.HubRouteListResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@FeignClient(name="hubroute-service")
public interface HubRouteClient {

    @GetMapping("/api/hub-routes")
    List<HubRouteListResponseDto> getHubRouteList(@RequestParam(required = false) UUID startHubId,
                                                  @RequestParam(required = false) UUID endHubId);
}