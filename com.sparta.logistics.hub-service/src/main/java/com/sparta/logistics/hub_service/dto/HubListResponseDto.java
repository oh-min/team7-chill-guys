package com.sparta.logistics.hub_service.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HubListResponseDto {

  private long userId;
  private String hubName;
  private String address;
}
