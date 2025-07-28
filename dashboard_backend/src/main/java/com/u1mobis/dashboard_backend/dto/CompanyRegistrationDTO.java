package com.u1mobis.dashboard_backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyRegistrationDTO {
    private String companyName;
    private String companyCode;
}