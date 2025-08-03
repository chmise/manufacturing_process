package com.u1mobis.dashboard_backend.domain.company.model;

public enum ProductionType {
    MASS_PRODUCTION("대량생산", "연속적으로 동일한 제품을 대량으로 생산"),
    VARIETY_SMALL_LOT("다품종 소량생산", "다양한 제품을 소량씩 생산"),
    CUSTOM_ORDER("주문생산", "고객 주문에 따른 맞춤형 생산"),
    BATCH_PRODUCTION("배치생산", "일정 단위로 나누어 생산");

    private final String displayName;
    private final String description;

    ProductionType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isSuitableForAutomation() {
        return this == MASS_PRODUCTION || this == BATCH_PRODUCTION;
    }

    public boolean requiresFlexibleScheduling() {
        return this == VARIETY_SMALL_LOT || this == CUSTOM_ORDER;
    }
}