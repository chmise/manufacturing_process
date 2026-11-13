package com.u1mobis.dashboard_backend.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MQTTControllerTest {

    @Test
    @DisplayName("자기 회사코드 토픽만 발행할 수 있다")
    void onlyOwnCompanyTopicIsAllowed() {
        assertThat(MQTTController.isOwnCompanyTopic("factory/AB12CD34/environment", "AB12CD34")).isTrue();
        assertThat(MQTTController.isOwnCompanyTopic("factory/ab12cd34/1/operations", "AB12CD34")).isTrue();

        assertThat(MQTTController.isOwnCompanyTopic("factory/ZZ99ZZ99/environment", "AB12CD34")).isFalse();
        assertThat(MQTTController.isOwnCompanyTopic("factory/AB12CD34", "AB12CD34")).isFalse();
        assertThat(MQTTController.isOwnCompanyTopic("other/AB12CD34/environment", "AB12CD34")).isFalse();
        assertThat(MQTTController.isOwnCompanyTopic("factory/AB12CD34/#", "AB12CD34")).isFalse();
        assertThat(MQTTController.isOwnCompanyTopic("factory/+/environment", "AB12CD34")).isFalse();
        assertThat(MQTTController.isOwnCompanyTopic("factory/AB12CD34/environment", null)).isFalse();
    }
}
