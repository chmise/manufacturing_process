package com.u1mobis.dashboard_backend.mqtt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.u1mobis.dashboard_backend.dto.MqttRobotDataDto;
import com.u1mobis.dashboard_backend.entity.Company;
import com.u1mobis.dashboard_backend.repository.CompanyRepository;
import com.u1mobis.dashboard_backend.service.ConveyorService;
import com.u1mobis.dashboard_backend.service.EnvironmentService;
import com.u1mobis.dashboard_backend.service.KPICalculationService;
import com.u1mobis.dashboard_backend.service.ProductionService;
import com.u1mobis.dashboard_backend.service.RobotService;

@ExtendWith(MockitoExtension.class)
class MQTTMessageProcessorTest {

    @Mock
    private ProductionService productionService;
    @Mock
    private KPICalculationService kpiCalculationService;
    @Mock
    private EnvironmentService environmentService;
    @Mock
    private ConveyorService conveyorService;
    @Mock
    private RobotService robotService;
    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private MQTTMessageProcessor processor;

    private static Company company(long id, String name, String code) {
        return Company.builder().companyId(id).companyName(name).companyCode(code).build();
    }

    @Test
    @DisplayName("토픽의 회사코드로 DB에서 회사를 찾아 환경 데이터를 저장한다 (대소문자 무관)")
    void resolvesCompanyByCode() {
        when(companyRepository.findByCompanyCode("AB12CD34"))
                .thenReturn(Optional.of(company(7L, "에이컴퍼니", "AB12CD34")));

        processor.processMQTTMessage("factory/ab12cd34/environment",
                "{\"temperature\":23.5,\"humidity\":40.0,\"air_quality\":50}");

        verify(environmentService).saveEnvironmentData("에이컴퍼니", 23.5, 40.0, 50);
    }

    @Test
    @DisplayName("등록되지 않은 회사의 메시지는 어떤 서비스에도 전달하지 않는다")
    void ignoresUnknownCompany() {
        when(companyRepository.findByCompanyCode("UNKNOWN1")).thenReturn(Optional.empty());
        when(companyRepository.findByCompanyName("unknown1")).thenReturn(Optional.empty());

        processor.processMQTTMessage("factory/unknown1/environment",
                "{\"temperature\":1.0,\"humidity\":1.0,\"air_quality\":1}");

        verifyNoInteractions(environmentService, productionService, kpiCalculationService,
                conveyorService, robotService);
    }

    @Test
    @DisplayName("로봇 상태는 토픽 회사의 ID로 갱신을 요청한다")
    void robotTelemetryUsesCompanyFromTopic() {
        when(companyRepository.findByCompanyCode("AB12CD34"))
                .thenReturn(Optional.of(company(7L, "에이컴퍼니", "AB12CD34")));

        processor.processMQTTMessage("factory/AB12CD34/robot",
                "{\"robot_id\":\"7_ROBOT_01\",\"status_text\":\"작동중\",\"temperature\":40.1,"
                        + "\"cycle_time\":30,\"power_consumption\":1.5,\"timestamp\":1700000000000,"
                        + "\"unknown_field\":\"무시됨\"}");

        ArgumentCaptor<MqttRobotDataDto> captor = ArgumentCaptor.forClass(MqttRobotDataDto.class);
        verify(robotService).updateRobotFromMqtt(eq(7L), captor.capture());
        assertThat(captor.getValue().getRobotId()).isEqualTo("7_ROBOT_01");
        assertThat(captor.getValue().getCycleTime()).isEqualTo(30);
    }

    @Test
    @DisplayName("factory/{회사}/robot 형식만 로봇 상태 토픽으로 본다")
    void robotTopicDetection() {
        assertThat(MQTTMessageProcessor.isRobotTelemetryTopic("factory/AB12CD34/robot")).isTrue();
        assertThat(MQTTMessageProcessor.isRobotTelemetryTopic("factory/AB12CD34/1/robots/all/completed")).isFalse();
        assertThat(MQTTMessageProcessor.isRobotTelemetryTopic("factory/AB12CD34/1/ROBOT_01/work/started")).isFalse();
    }
}
