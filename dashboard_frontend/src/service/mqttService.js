class MQTTService {
  constructor() {
    this.isConnected = false;
    this.brokerUrl = 'http://localhost:8080/api'; // 백엔드를 통한 MQTT 전송
  }

  // 모의 연결 (실제로는 백엔드 상태 확인)
  connect() {
    return new Promise((resolve) => {
      console.log('MQTT 서비스 준비 완료 (백엔드를 통한 전송)');
      this.isConnected = true;
      resolve();
    });
  }

  // 연결 해제
  disconnect() {
    this.isConnected = false;
    console.log('MQTT 서비스 연결 해제');
  }

  // HTTP를 통해 MQTT 메시지 전송
  async publish(topic, message) {
    const messageStr = typeof message === 'string' ? message : JSON.stringify(message);
    
    try {
      const response = await fetch(`${this.brokerUrl}/mqtt/publish`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          topic: topic,
          message: messageStr,
          qos: 1
        })
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      console.log(`MQTT 메시지 전송 성공 [${topic}]:`, messageStr);
      return await response.json();
    } catch (error) {
      console.error(`MQTT 메시지 전송 실패 [${topic}]:`, error);
      throw error;
    }
  }

  // 시뮬레이션 시작 신호 전송
  async startSimulation(lineId = 1) {
    const topic = `factory/line${lineId}/simulator/control`;
    const message = {
      command: 'START',
      lineId: lineId,
      timestamp: Date.now(),
      source: 'dashboard_simulator'
    };
    return this.publish(topic, message);
  }

  // 시뮬레이션 중지 신호 전송
  async stopSimulation(lineId = 1) {
    const topic = `factory/line${lineId}/simulator/control`;
    const message = {
      command: 'STOP',
      lineId: lineId,
      timestamp: Date.now(),
      source: 'dashboard_simulator'
    };
    return this.publish(topic, message);
  }

  // 시뮬레이션 리셋 신호 전송
  async resetSimulation(lineId = 1) {
    const topic = `factory/line${lineId}/simulator/control`;
    const message = {
      command: 'RESET',
      lineId: lineId,
      timestamp: Date.now(),
      source: 'dashboard_simulator'
    };
    return this.publish(topic, message);
  }

  // 제품 생성 신호 전송
  async createProduct(lineId = 1, productData = {}) {
    const topic = `factory/line${lineId}/production/start`;
    const message = {
      productId: `PRD_${Date.now()}`,
      lineId: lineId,
      productColor: productData.productColor || 'WHITE',
      doorColor: productData.doorColor || 'WHITE',
      timestamp: Date.now(),
      source: 'dashboard_simulator',
      ...productData
    };
    return this.publish(topic, message);
  }

  // 스테이션 작업 완료 신호 전송
  async completeStationWork(lineId = 1, stationId, productId) {
    const topic = `factory/line${lineId}/${stationId}/production/completed`;
    const message = {
      productId: productId,
      stationId: stationId,
      lineId: lineId,
      completedAt: Date.now(),
      status: 'COMPLETED',
      source: 'dashboard_simulator'
    };
    return this.publish(topic, message);
  }

  // 환경 데이터 전송
  async sendEnvironmentData(lineId = 1, environmentData = {}) {
    const topic = `factory/line${lineId}/environment`;
    const message = {
      lineId: lineId,
      temperature: environmentData.temperature || (20 + Math.random() * 10).toFixed(1),
      humidity: environmentData.humidity || (40 + Math.random() * 20).toFixed(1),
      airPressure: environmentData.airPressure || (1013 + Math.random() * 10).toFixed(1),
      timestamp: Date.now(),
      source: 'dashboard_simulator',
      ...environmentData
    };
    return this.publish(topic, message);
  }

  // 로봇 상태 업데이트 전송
  async updateRobotStatus(robotId, robotData = {}) {
    const topic = `factory/robot/${robotId}/status`;
    const message = {
      robotId: robotId,
      status: robotData.status || 'ACTIVE',
      batteryLevel: robotData.batteryLevel || (70 + Math.random() * 30),
      currentTask: robotData.currentTask || 'IDLE',
      position: robotData.position || { x: 0, y: 0, z: 0 },
      timestamp: Date.now(),
      source: 'dashboard_simulator',
      ...robotData
    };
    return this.publish(topic, message);
  }

  // 컨베이어 상태 업데이트 전송
  async updateConveyorStatus(lineId = 1, conveyorData = {}) {
    const topic = `factory/line${lineId}/conveyor/status`;
    const message = {
      lineId: lineId,
      isRunning: conveyorData.isRunning !== undefined ? conveyorData.isRunning : true,
      speed: conveyorData.speed || (1.0 + Math.random() * 0.5).toFixed(2),
      direction: conveyorData.direction || 'FORWARD',
      timestamp: Date.now(),
      source: 'dashboard_simulator',
      ...conveyorData
    };
    return this.publish(topic, message);
  }

  // 연결 상태 확인
  isConnectedToBroker() {
    return this.isConnected && this.client && this.client.connected;
  }
}

// 싱글톤 인스턴스 생성
const mqttService = new MQTTService();

export default mqttService;