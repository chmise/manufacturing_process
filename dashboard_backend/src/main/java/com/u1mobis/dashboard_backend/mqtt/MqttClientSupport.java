package com.u1mobis.dashboard_backend.mqtt;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Paho MQTT 클라이언트 공통 처리.
 */
@Slf4j
public final class MqttClientSupport {

    private static final long RETRY_SECONDS = 5;

    private MqttClientSupport() {
    }

    public static MqttConnectOptions defaultOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setAutomaticReconnect(true); // 한 번 연결된 뒤 끊기면 Paho가 다시 연결
        options.setConnectionTimeout(10);
        options.setKeepAliveInterval(30);
        return options;
    }

    /**
     * 브로커가 아직 뜨지 않았어도 서버 시작이 막히지 않도록, 처음 연결에 성공할 때까지
     * 백그라운드에서 5초마다 다시 시도한다. 연결되면 onConnected를 한 번 실행한다.
     */
    public static ScheduledExecutorService connectInBackground(MqttClient client, MqttConnectOptions options,
                                                               String name, Runnable onConnected) {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, name + "-connect");
            thread.setDaemon(true);
            return thread;
        });

        executor.scheduleWithFixedDelay(() -> {
            if (client.isConnected()) {
                executor.shutdown();
                return;
            }
            try {
                client.connect(options);
                log.info("[{}] MQTT 브로커 연결 성공: {}", name, client.getServerURI());
            } catch (MqttException e) {
                log.warn("[{}] MQTT 브로커 연결 실패, {}초 후 다시 시도합니다: {}", name, RETRY_SECONDS, e.getMessage());
                return;
            }
            try {
                onConnected.run();
            } catch (RuntimeException e) {
                log.error("[{}] MQTT 연결 후 처리 실패: {}", name, e.getMessage(), e);
            }
            executor.shutdown();
        }, 0, RETRY_SECONDS, TimeUnit.SECONDS);

        return executor;
    }

    public static void closeQuietly(MqttClient client, ScheduledExecutorService connector) {
        if (connector != null) {
            connector.shutdownNow();
        }
        if (client == null) {
            return;
        }
        try {
            if (client.isConnected()) {
                client.disconnect();
            }
            client.close();
        } catch (MqttException e) {
            log.debug("MQTT 클라이언트 종료 중 오류: {}", e.getMessage());
        }
    }
}
