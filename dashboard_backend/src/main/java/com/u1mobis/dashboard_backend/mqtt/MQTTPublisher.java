package com.u1mobis.dashboard_backend.mqtt;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MQTTPublisher {
    
    private MqttClient mqttClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @PostConstruct
    public void initialize() {
        try {
            mqttClient = new MqttClient("tcp://localhost:1883", "dashboard-publisher-" + System.currentTimeMillis());
            
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setKeepAliveInterval(30);
            
            mqttClient.connect(options);
            
            log.info("MQTT Publisher 초기화 완료 - 브로커: tcp://localhost:1883");
            
        } catch (Exception e) {
            log.error("MQTT Publisher 초기화 실패: {}", e.getMessage());
        }
    }
    
    @PreDestroy
    public void cleanup() {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
                mqttClient.close();
            }
        } catch (Exception e) {
            log.error("MQTT Publisher 정리 실패: {}", e.getMessage());
        }
    }
    
    /**
     * 환경 데이터 발송
     * 토픽: factory/{companyCode}/environment
     */
    public void publishEnvironmentData(String companyCode, double temperature, double humidity, int airQuality) {
        try {
            String topic = String.format("factory/%s/environment", companyCode);
            
            // 매개변수를 로컬 변수로 복사하여 익명 클래스에서 사용
            final double temp = temperature;
            final double humid = humidity;
            final int airQual = airQuality;
            
            Object data = new Object() {
                public final double temperature = temp;
                public final double humidity = humid;
                public final int air_quality = airQual;
                public final String timestamp = java.time.LocalDateTime.now().toString();
            };
            
            String payload = objectMapper.writeValueAsString(data);
            publishMessage(topic, payload);
            
        } catch (Exception e) {
            log.error("환경 데이터 발송 실패 - 회사: {}", companyCode, e);
        }
    }
    
    /**
     * 생산 시작 데이터 발송
     * 토픽: factory/{companyCode}/{lineId}/production/started
     */
    public void publishProductionStarted(String companyCode, Long lineId, String productId, 
                                       int targetQuantity, String dueDate) {
        try {
            String topic = String.format("factory/%s/%d/production/started", companyCode, lineId);
            
            final String prodId = productId;
            final int targetQty = targetQuantity;
            final String due = dueDate;
            
            Object data = new Object() {
                public final String product_id = prodId;
                public final int target_quantity = targetQty;
                public final String due_date = due;
                public final String timestamp = java.time.LocalDateTime.now().toString();
            };
            
            String payload = objectMapper.writeValueAsString(data);
            publishMessage(topic, payload);
            
        } catch (Exception e) {
            log.error("생산 시작 데이터 발송 실패 - 회사: {}, 라인: {}", companyCode, lineId, e);
        }
    }
    
    /**
     * 생산 완료 데이터 발송
     * 토픽: factory/{companyCode}/{lineId}/production/completed
     */
    public void publishProductionCompleted(String companyCode, Long lineId, String productId, 
                                         double cycleTime, String quality, String dueDate) {
        try {
            String topic = String.format("factory/%s/%d/production/completed", companyCode, lineId);
            
            final String prodId = productId;
            final double cycle = cycleTime;
            final String qual = quality;
            final String due = dueDate;
            
            Object data = new Object() {
                public final String product_id = prodId;
                public final double cycle_time = cycle;
                public final String quality = qual;
                public final String due_date = due;
                public final String timestamp = java.time.LocalDateTime.now().toString();
            };
            
            String payload = objectMapper.writeValueAsString(data);
            publishMessage(topic, payload);
            
        } catch (Exception e) {
            log.error("생산 완료 데이터 발송 실패 - 회사: {}, 라인: {}", companyCode, lineId, e);
        }
    }
    
    /**
     * 운영 데이터 발송 (KPI)
     * 토픽: factory/{companyCode}/{lineId}/operations
     */
    public void publishOperationsData(String companyCode, Long lineId, int plannedTime, int downtime,
                                    double targetCycleTime, int goodCount, int totalCount,
                                    int firstTimePassCount, int onTimeDeliveryCount) {
        try {
            String topic = String.format("factory/%s/%d/operations", companyCode, lineId);
            
            final int planTime = plannedTime;
            final int down = downtime;
            final double targetCycle = targetCycleTime;
            final int goodCnt = goodCount;
            final int totalCnt = totalCount;
            final int firstPassCnt = firstTimePassCount;
            final int onTimeCnt = onTimeDeliveryCount;
            
            Object data = new Object() {
                public final int planned_time = planTime;
                public final int downtime = down;
                public final double target_cycle_time = targetCycle;
                public final int good_count = goodCnt;
                public final int total_count = totalCnt;
                public final int first_time_pass_count = firstPassCnt;
                public final int on_time_delivery_count = onTimeCnt;
                public final String timestamp = java.time.LocalDateTime.now().toString();
            };
            
            String payload = objectMapper.writeValueAsString(data);
            publishMessage(topic, payload);
            
        } catch (Exception e) {
            log.error("운영 데이터 발송 실패 - 회사: {}, 라인: {}", companyCode, lineId, e);
        }
    }
    
    /**
     * 컨베이어 제어 데이터 발송
     * 토픽: factory/{companyCode}/{lineId}/conveyor
     */
    public void publishConveyorData(String companyCode, Long lineId, String command, String reason) {
        try {
            String topic = String.format("factory/%s/%d/conveyor", companyCode, lineId);
            
            final String cmd = command;
            final String rsn = reason;
            
            Object data = new Object() {
                public final String command = cmd;
                public final String reason = rsn;
                public final String timestamp = java.time.LocalDateTime.now().toString();
            };
            
            String payload = objectMapper.writeValueAsString(data);
            publishMessage(topic, payload);
            
        } catch (Exception e) {
            log.error("컨베이어 데이터 발송 실패 - 회사: {}, 라인: {}", companyCode, lineId, e);
        }
    }
    
    /**
     * 로봇 상태 데이터 발송
     * 토픽: factory/{companyCode}/robot
     */
    public void publishRobotData(String companyCode, String robotId, String status, String currentTask, int batteryLevel) {
        try {
            String topic = String.format("factory/%s/robot", companyCode);
            
            final String robId = robotId;
            final String stat = status;
            final String task = currentTask;
            final int battery = batteryLevel;
            
            Object data = new Object() {
                public final String robot_id = robId;
                public final String status = stat;
                public final String current_task = task;
                public final int battery_level = battery;
                public final String timestamp = java.time.LocalDateTime.now().toString();
            };
            
            String payload = objectMapper.writeValueAsString(data);
            publishMessage(topic, payload);
            
        } catch (Exception e) {
            log.error("로봇 데이터 발송 실패 - 회사: {}, 로봇: {}", companyCode, robotId, e);
        }
    }
    
    /**
     * 스테이션 상태 데이터 발송 (추가)
     * 토픽: factory/{companyCode}/{lineId}/station/status
     */
    public void publishStationStatus(String companyCode, Long lineId, String stationId, String status,
                                   double temperature, double efficiency, String currentProduct) {
        try {
            String topic = String.format("factory/%s/%d/station/status", companyCode, lineId);
            
            final String stId = stationId;
            final String stat = status;
            final double temp = temperature;
            final double eff = efficiency;
            final String currProd = currentProduct;
            
            Object data = new Object() {
                public final String station_id = stId;
                public final String status = stat;
                public final double temperature = temp;
                public final double efficiency = eff;
                public final String current_product = currProd;
                public final String timestamp = java.time.LocalDateTime.now().toString();
            };
            
            String payload = objectMapper.writeValueAsString(data);
            publishMessage(topic, payload);
            
        } catch (Exception e) {
            log.error("스테이션 상태 발송 실패 - 회사: {}, 라인: {}", companyCode, lineId, e);
        }
    }
    
    /**
     * 제품 상세 정보 발송 (추가)
     * 토픽: factory/{companyCode}/{lineId}/product/details
     */
    public void publishProductDetails(String companyCode, Long lineId, String productId, String productColor,
                                    String doorColor, int workProgress, double positionX, double positionY) {
        try {
            String topic = String.format("factory/%s/%d/product/details", companyCode, lineId);
            
            final String prodId = productId;
            final String prodColor = productColor;
            final String dColor = doorColor;
            final int progress = workProgress;
            final double posX = positionX;
            final double posY = positionY;
            
            Object data = new Object() {
                public final String product_id = prodId;
                public final String product_color = prodColor;
                public final String door_color = dColor;
                public final int work_progress = progress;
                public final double position_x = posX;
                public final double position_y = posY;
                public final String timestamp = java.time.LocalDateTime.now().toString();
            };
            
            String payload = objectMapper.writeValueAsString(data);
            publishMessage(topic, payload);
            
        } catch (Exception e) {
            log.error("제품 상세 정보 발송 실패 - 회사: {}, 라인: {}", companyCode, lineId, e);
        }
    }
    
    /**
     * 제품 이동 데이터 발송
     * 토픽: factory/{companyCode}/{lineId}/product/moved
     */
    public void publishProductMoved(String companyCode, Long lineId, String productId, 
                                  String fromStation, String toStation, double positionX, double positionY, int moveDuration) {
        try {
            String topic = String.format("factory/%s/%d/product/moved", companyCode, lineId);
            
            final String prodId = productId;
            final String from = fromStation;
            final String to = toStation;
            final double posX = positionX;
            final double posY = positionY;
            final int duration = moveDuration;
            
            Object data = new Object() {
                public final String product_id = prodId;
                public final Long line_id = lineId;
                public final String from_station = from;
                public final String to_station = to;
                public final double position_x = posX;
                public final double position_y = posY;
                public final int move_duration = duration;
                public final String timestamp = java.time.LocalDateTime.now().toString();
            };
            
            String payload = objectMapper.writeValueAsString(data);
            publishMessage(topic, payload);
            
        } catch (Exception e) {
            log.error("제품 이동 데이터 발송 실패 - 회사: {}, 라인: {}", companyCode, lineId, e);
        }
    }
    
    /**
     * 제품 구역 도착 데이터 발송
     * 토픽: factory/{companyCode}/{lineId}/product/arrived/{areaType}
     */
    public void publishProductArrived(String companyCode, Long lineId, String productId, 
                                    String areaType, double positionX, double positionY) {
        try {
            String topic = String.format("factory/%s/%d/product/arrived/%s", companyCode, lineId, areaType);
            
            final String prodId = productId;
            final String area = areaType;
            final double posX = positionX;
            final double posY = positionY;
            
            Object data = new Object() {
                public final String product_id = prodId;
                public final Long line_id = lineId;
                public final String area_type = area;
                public final double position_x = posX;
                public final double position_y = posY;
                public final String timestamp = java.time.LocalDateTime.now().toString();
            };
            
            String payload = objectMapper.writeValueAsString(data);
            publishMessage(topic, payload);
            
        } catch (Exception e) {
            log.error("제품 도착 데이터 발송 실패 - 회사: {}, 라인: {}", companyCode, lineId, e);
        }
    }
    
    /**
     * 로봇 작업 시작 데이터 발송
     * 토픽: factory/{companyCode}/{lineId}/{robotId}/work/started
     */
    public void publishRobotWorkStarted(String companyCode, Long lineId, String robotId, 
                                      String productId, String doorType, int workDuration) {
        try {
            String topic = String.format("factory/%s/%d/%s/work/started", companyCode, lineId, robotId);
            
            final String robId = robotId;
            final String prodId = productId;
            final String door = doorType;
            final int duration = workDuration;
            
            Object data = new Object() {
                public final String robot_id = robId;
                public final Long line_id = lineId;
                public final String product_id = prodId;
                public final String door_type = door;
                public final int work_duration = duration;
                public final String timestamp = java.time.LocalDateTime.now().toString();
            };
            
            String payload = objectMapper.writeValueAsString(data);
            publishMessage(topic, payload);
            
        } catch (Exception e) {
            log.error("로봇 작업 시작 데이터 발송 실패 - 회사: {}, 라인: {}, 로봇: {}", companyCode, lineId, robotId, e);
        }
    }
    
    /**
     * 로봇 작업 완료 데이터 발송
     * 토픽: factory/{companyCode}/{lineId}/{robotId}/work/completed
     */
    public void publishRobotWorkCompleted(String companyCode, Long lineId, String robotId, 
                                        String productId, String doorType, int actualWorkTime) {
        try {
            String topic = String.format("factory/%s/%d/%s/work/completed", companyCode, lineId, robotId);
            
            final String robId = robotId;
            final String prodId = productId;
            final String door = doorType;
            final int actualTime = actualWorkTime;
            
            Object data = new Object() {
                public final String robot_id = robId;
                public final Long line_id = lineId;
                public final String product_id = prodId;
                public final String door_type = door;
                public final int actual_work_time = actualTime;
                public final String timestamp = java.time.LocalDateTime.now().toString();
            };
            
            String payload = objectMapper.writeValueAsString(data);
            publishMessage(topic, payload);
            
        } catch (Exception e) {
            log.error("로봇 작업 완료 데이터 발송 실패 - 회사: {}, 라인: {}, 로봇: {}", companyCode, lineId, robotId, e);
        }
    }
    
    /**
     * 전체 로봇 작업 완료 데이터 발송
     * 토픽: factory/{companyCode}/{lineId}/robots/all/completed
     */
    public void publishAllRobotsCompleted(String companyCode, Long lineId, String productId, 
                                        String[] completedRobots, int totalWorkTime) {
        try {
            String topic = String.format("factory/%s/%d/robots/all/completed", companyCode, lineId);
            
            final String prodId = productId;
            final String[] robots = completedRobots;
            final int totalTime = totalWorkTime;
            
            Object data = new Object() {
                public final String product_id = prodId;
                public final Long line_id = lineId;
                public final String[] robots_completed = robots;
                public final int total_work_time = totalTime;
                public final boolean all_doors_attached = true;
                public final String timestamp = java.time.LocalDateTime.now().toString();
            };
            
            String payload = objectMapper.writeValueAsString(data);
            publishMessage(topic, payload);
            
        } catch (Exception e) {
            log.error("전체 로봇 완료 데이터 발송 실패 - 회사: {}, 라인: {}", companyCode, lineId, e);
        }
    }
    
    /**
     * 수밀검사 시작 데이터 발송
     * 토픽: factory/{companyCode}/{lineId}/inspection/started
     */
    public void publishInspectionStarted(String companyCode, Long lineId, String productId, 
                                       String inspectionType, int testDuration, double pressureApplied) {
        try {
            String topic = String.format("factory/%s/%d/inspection/started", companyCode, lineId);
            
            final String prodId = productId;
            final String inspection = inspectionType;
            final int duration = testDuration;
            final double pressure = pressureApplied;
            
            Object data = new Object() {
                public final String product_id = prodId;
                public final Long line_id = lineId;
                public final String inspection_type = inspection;
                public final int test_duration = duration;
                public final double pressure_applied = pressure;
                public final String timestamp = java.time.LocalDateTime.now().toString();
            };
            
            String payload = objectMapper.writeValueAsString(data);
            publishMessage(topic, payload);
            
        } catch (Exception e) {
            log.error("수밀검사 시작 데이터 발송 실패 - 회사: {}, 라인: {}", companyCode, lineId, e);
        }
    }
    
    /**
     * 수밀검사 완료 데이터 발송
     * 토픽: factory/{companyCode}/{lineId}/inspection/completed
     */
    public void publishInspectionCompleted(String companyCode, Long lineId, String productId, 
                                         String inspectionType, int actualDuration, String result, boolean leakDetected) {
        try {
            String topic = String.format("factory/%s/%d/inspection/completed", companyCode, lineId);
            
            final String prodId = productId;
            final String inspection = inspectionType;
            final int duration = actualDuration;
            final String testResult = result;
            final boolean leak = leakDetected;
            
            Object data = new Object() {
                public final String product_id = prodId;
                public final Long line_id = lineId;
                public final String inspection_type = inspection;
                public final int test_duration = duration;
                public final String result = testResult;
                public final boolean leak_detected = leak;
                public final String timestamp = java.time.LocalDateTime.now().toString();
            };
            
            String payload = objectMapper.writeValueAsString(data);
            publishMessage(topic, payload);
            
        } catch (Exception e) {
            log.error("수밀검사 완료 데이터 발송 실패 - 회사: {}, 라인: {}", companyCode, lineId, e);
        }
    }

    /**
     * MQTT 메시지 발송
     */
    private void publishMessage(String topic, String payload) {
        try {
            if (mqttClient == null || !mqttClient.isConnected()) {
                log.warn("MQTT 클라이언트가 연결되지 않음");
                return;
            }
            
            MqttMessage message = new MqttMessage();
            message.setPayload(payload.getBytes());
            message.setQos(1);  // At least once delivery
            message.setRetained(false);
            
            mqttClient.publish(topic, message);
            log.debug("MQTT 메시지 발송 완료 - 토픽: {}", topic);
            
        } catch (Exception e) {
            log.error("MQTT 메시지 발송 실패 - 토픽: {}, 내용: {}", topic, payload, e);
        }
    }
}