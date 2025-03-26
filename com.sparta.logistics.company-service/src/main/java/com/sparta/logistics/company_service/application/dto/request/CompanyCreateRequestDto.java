package com.sparta.logistics.company_service.application.dto.request;

import com.sparta.logistics.company_service.domain.CompanyType;
import lombok.*;

import java.util.UUID;

@Getter
@Builder
public class CompanyCreateRequestDto {

    private UUID hubId;
    private String name;
    private CompanyType type;
    private String address;
    private String phone;
    private Long createdBy;
}
