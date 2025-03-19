package com.sparta.logistics.delivery_service.presentation;

import com.sparta.logistics.delivery_service.application.dto.request.DeliveryRouteUpdateRequestDto;
import com.sparta.logistics.delivery_service.application.dto.response.DeliveryRouteResponseDto;
import com.sparta.logistics.delivery_service.application.service.DeliveryRouteService;
import com.sparta.logistics.delivery_service.domain.model.DeliveryRouteStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/deliveries/{delivery_id}/routes")
@RequiredArgsConstructor
public class DeliveryRouteController {

    private final DeliveryRouteService deliveryRouteService;

    /**
     * 배송 경로 기록 조회
     */
    @GetMapping
    public ResponseEntity<List<DeliveryRouteResponseDto>> getDeliveryRoutesList (@PathVariable("delivery_id")UUID deliveryId) {
        return ResponseEntity.ok().body(deliveryRouteService.getDeliveryRoutesList(deliveryId));
    }

    /**
     * 배송 경로 단건 조회
     */
    @GetMapping("/{routes_id}")
    public ResponseEntity<DeliveryRouteResponseDto> getDeliveryRoute(@PathVariable("delivery_id")UUID deliveryId,
                                                                     @PathVariable("routes_id") UUID routesId) {
        return ResponseEntity.ok().body(deliveryRouteService.getDeliveryRoute(deliveryId, routesId));
    }

    /**
     * 배송 경로 기록 수정
     */
    @PutMapping("/{routes_id}")
    public ResponseEntity<Void> updateDeliveryRoute(@PathVariable("delivery_id")UUID deliveryId,
                                                    @PathVariable("routes_id") UUID routesId,
                                                    @RequestBody DeliveryRouteUpdateRequestDto updateDeliveryRouteUpdateRequestDto) {
        deliveryRouteService.updateDeliveryRoute(deliveryId, routesId, updateDeliveryRouteUpdateRequestDto);
        return ResponseEntity.ok().build();
    }

    /**
     * 허브 배송 담당자 배정
     */
    @PutMapping("/{routes_id}/assign")
    public ResponseEntity<Void> assignHubDeliveryManager(@PathVariable("delivery_id") UUID deliveryId,
                                                         @PathVariable("routes_id") UUID routesId) {
        deliveryRouteService.assignHudDeliveryManager(deliveryId, routesId);
        return ResponseEntity.ok().build();
    }

    /**
     * 배송 상태 변경
     */
    @PutMapping("/{routes_id}/status")
    public ResponseEntity<Void> changeDeliveryStatus(@PathVariable("delivery_id") UUID deliveryId,
                                                     @PathVariable("routes_id") UUID routesId,
                                                     @RequestParam("status")DeliveryRouteStatus status) {
        deliveryRouteService.changeDeliveryStatus(deliveryId, routesId, status);
        return ResponseEntity.ok().build();
    }


    /**
     * 배송 경로 기록 삭제
     */
    @DeleteMapping("/{routes_id}")
    public ResponseEntity<Void> deleteDeliveryRoute(@PathVariable("delivery_id")UUID deliveryId,
                                                    @PathVariable("routes_id") UUID routesId) {
        deliveryRouteService.deleteDeliveryRoute(deliveryId, routesId);
        return ResponseEntity.ok().build();
    }
}
