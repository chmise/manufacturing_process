package com.u1mobis.dashboard_backend.interfaces.dto.company;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompanyCreateRequest {
    private String name;
    private String code;
}