package com.u1mobis.dashboard_backend.interfaces.dto.company;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class IndustryTypeResponse {
    private String id;
    private String name;
    private String code;
    private String description;
    private String iconName;
    private String primaryColor;
    private boolean active;
}