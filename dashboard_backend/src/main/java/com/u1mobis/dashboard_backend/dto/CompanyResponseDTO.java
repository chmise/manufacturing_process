package com.u1mobis.dashboard_backend.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyResponseDTO {
    private boolean success;
    private String message;
    private Long companyId;
    private String companyName;
    private String companyCode;
    private LocalDateTime createdAt;
}