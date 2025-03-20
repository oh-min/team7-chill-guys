package com.sparta.logistics.hub_service.hubroute.application.dto.request;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HubRouteCreateRequestDto {

  private UUID startHubId;
  private UUID endHubId;
}
