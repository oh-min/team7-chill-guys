//package com.sparta.logistics.order_service.client;
//
//import org.springframework.cloud.openfeign.FeignClient;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//
//import java.util.UUID;
//
//@FeignClient(name = "product-service")
//public interface ProductClient {
//
//    @GetMapping("/api/products/{productId}")
//    ProductResponse getProduct(@PathVariable("productId") UUID productId);
//
//    record ProductResponse(UUID productId, Long stock) {}
//}
