package com.u1mobis.dashboard_backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.u1mobis.dashboard_backend.service.KPICalculationService.KpiResult;

class KPICalculationServiceTest {

    private static final double EPS = 1e-9;

    @Test
    @DisplayName("OEE = 가용성 × 성능 × 품질")
    void oeeIsAvailabilityTimesPerformanceTimesQuality() {
        // 계획 480분, 비가동 30분 → 가동 450분 / 400대 생산, 양품 380대, 목표 사이클타임 60초
        KpiResult result = KPICalculationService.calculate(480, 30, 60.0, 380, 400, 370, 390);

        double availability = 450.0 / 480;
        double actualCycleTime = 450 * 60.0 / 400; // 67.5초
        double performance = 60.0 / actualCycleTime;
        double quality = 380.0 / 400;

        assertThat(result.availability()).isCloseTo(availability, within(EPS));
        assertThat(result.performance()).isCloseTo(performance, within(EPS));
        assertThat(result.quality()).isCloseTo(quality, within(EPS));
        assertThat(result.oee()).isCloseTo(availability * performance * quality, within(EPS));
        assertThat(result.fty()).isCloseTo(370.0 / 400, within(EPS));
        assertThat(result.otd()).isCloseTo(390.0 / 400, within(EPS));
    }

    @Test
    @DisplayName("성능률은 100%를 넘지 않는다")
    void performanceIsCappedAtOne() {
        // 목표보다 빨리 만든 경우
        KpiResult result = KPICalculationService.calculate(60, 0, 120.0, 100, 100, 100, 100);

        assertThat(result.performance()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("생산수량이 0이어도 NaN 없이 0을 돌려준다")
    void zeroProductionGivesZeroNotNaN() {
        KpiResult result = KPICalculationService.calculate(480, 0, 60.0, 0, 0, 0, 0);

        assertThat(result.oee()).isZero();
        assertThat(result.quality()).isZero();
        assertThat(result.fty()).isZero();
        assertThat(result.otd()).isZero();
    }

    @Test
    @DisplayName("계획시간이 0이거나 비가동시간이 더 길어도 음수/NaN이 나오지 않는다")
    void zeroOrNegativeOperatingTimeGivesZero() {
        KpiResult noPlan = KPICalculationService.calculate(0, 0, 60.0, 10, 10, 10, 10);
        KpiResult tooMuchDowntime = KPICalculationService.calculate(100, 150, 60.0, 10, 10, 10, 10);

        assertThat(noPlan.availability()).isZero();
        assertThat(noPlan.oee()).isZero();
        assertThat(tooMuchDowntime.availability()).isZero();
        assertThat(tooMuchDowntime.oee()).isZero();
    }
}
