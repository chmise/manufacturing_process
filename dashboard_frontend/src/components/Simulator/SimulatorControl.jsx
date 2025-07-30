import React, { useState, useEffect } from 'react';
import { apiService } from '../../service/apiService';
import mqttService from '../../service/mqttService';
import './SimulatorControl.css';

const SimulatorControl = () => {
  const [simulatorStatus, setSimulatorStatus] = useState(null);
  const [simulatorStats, setSimulatorStats] = useState(null);
  const [unityData, setUnityData] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);
  const [autoRefresh, setAutoRefresh] = useState(false);
  const [mqttConnected, setMqttConnected] = useState(false);
  const [selectedLineId, setSelectedLineId] = useState(1);

  // 시뮬레이터 상태 조회
  const fetchSimulatorStatus = async () => {
    try {
      setError(null);
      const response = await apiService.simulator.getStatus();
      // 백엔드 응답 구조에 맞게 수정
      if (response.success) {
        setSimulatorStatus(response.simulationStatus);
      } else {
        setError(response.message || '시뮬레이터 상태 조회 실패');
      }
    } catch (err) {
      setError('시뮬레이터 상태 조회 실패: ' + err.message);
      console.error('시뮬레이터 상태 조회 실패:', err);
    }
  };

  // 시뮬레이터 통계 조회
  const fetchSimulatorStats = async () => {
    try {
      const response = await apiService.simulator.getStatistics();
      if (response.success) {
        setSimulatorStats(response.statistics);
      }
    } catch (err) {
      console.error('시뮬레이터 통계 조회 실패:', err);
    }
  };


  // Unity 실시간 데이터 조회 (companyName 인자 필수)
  const fetchUnityData = async (companyName) => {
    try {
      const data = await apiService.unity.getRealtimeData(companyName);
      setUnityData(data);
    } catch (err) {
      console.error('Unity 데이터 조회 실패:', err);
    }
  };

  // MQTT 연결 초기화
  const initializeMQTT = async () => {
    try {
      await mqttService.connect();
      setMqttConnected(true);
      console.log('MQTT 서비스 연결 완료');
    } catch (err) {
      console.error('MQTT 연결 실패:', err);
      setMqttConnected(false);
      setError('MQTT 브로커 연결 실패: ' + err.message);
    }
  };

  // 시뮬레이션 시작 (MQTT 직접 전송)
  const handleStartSimulation = async () => {
    setIsLoading(true);
    try {
      setError(null);
      
      if (!mqttConnected) {
        await initializeMQTT();
      }
      
      // MQTT로 시뮬레이션 시작 신호 전송
      await mqttService.startSimulation(selectedLineId);
      
      // 환경 데이터도 함께 전송
      await mqttService.sendEnvironmentData(selectedLineId);
      
      // 컨베이어 시작 신호 전송
      await mqttService.updateConveyorStatus(selectedLineId, { isRunning: true });
      
      alert(`라인 ${selectedLineId} 시뮬레이션이 MQTT로 시작 신호를 전송했습니다.`);
      
      // 상태 새로고침
      await fetchSimulatorStatus();
      
    } catch (err) {
      setError('MQTT 시뮬레이션 시작 실패: ' + err.message);
      console.error('MQTT 시뮬레이션 시작 실패:', err);
    }
    setIsLoading(false);
  };

  // 시뮬레이션 중지 (MQTT 직접 전송)
  const handleStopSimulation = async () => {
    setIsLoading(true);
    try {
      setError(null);
      
      if (!mqttConnected) {
        await initializeMQTT();
      }
      
      // MQTT로 시뮬레이션 중지 신호 전송
      await mqttService.stopSimulation(selectedLineId);
      
      // 컨베이어 중지 신호 전송
      await mqttService.updateConveyorStatus(selectedLineId, { isRunning: false });
      
      alert(`라인 ${selectedLineId} 시뮬레이션이 MQTT로 중지 신호를 전송했습니다.`);
      
      // 상태 새로고침
      await fetchSimulatorStatus();
      
    } catch (err) {
      setError('MQTT 시뮬레이션 중지 실패: ' + err.message);
      console.error('MQTT 시뮬레이션 중지 실패:', err);
    }
    setIsLoading(false);
  };

  // 시뮬레이션 리셋 (MQTT 직접 전송)
  const handleResetSimulation = async () => {
    if (!confirm('시뮬레이션을 리셋하시겠습니까? 모든 데이터가 초기화됩니다.')) {
      return;
    }
    
    setIsLoading(true);
    try {
      setError(null);
      
      if (!mqttConnected) {
        await initializeMQTT();
      }
      
      // MQTT로 시뮬레이션 리셋 신호 전송
      await mqttService.resetSimulation(selectedLineId);
      
      alert(`라인 ${selectedLineId} 시뮬레이션이 MQTT로 리셋 신호를 전송했습니다.`);
      
      // 상태 새로고침
      await fetchSimulatorStatus();
      
    } catch (err) {
      setError('MQTT 시뮬레이션 리셋 실패: ' + err.message);
      console.error('MQTT 시뮬레이션 리셋 실패:', err);
    }
    setIsLoading(false);
  };

  // 제품 생성 (MQTT 직접 전송)
  const handleCreateProduct = async () => {
    setIsLoading(true);
    try {
      setError(null);
      
      if (!mqttConnected) {
        await initializeMQTT();
      }
      
      // MQTT로 제품 생성 신호 전송
      await mqttService.createProduct(selectedLineId, {
        productColor: 'BLUE',
        doorColor: 'WHITE'
      });
      
      alert(`라인 ${selectedLineId}에 새 제품 생성 신호를 MQTT로 전송했습니다.`);
      
    } catch (err) {
      setError('MQTT 제품 생성 실패: ' + err.message);
      console.error('MQTT 제품 생성 실패:', err);
    }
    setIsLoading(false);
  };


  // companyName을 localStorage의 userData에서 가져오는 함수
  const getCurrentCompanyName = () => {
    try {
      const userData = JSON.parse(localStorage.getItem('userData') || '{}');
      return userData.companyName || 'u1mobis'; // 기본값
    } catch (error) {
      console.error('사용자 데이터 파싱 오류:', error);
      return 'u1mobis'; // 기본값
    }
  };

  // 데이터 새로고침
  const refreshAllData = async () => {
    setIsLoading(true);
    const companyName = getCurrentCompanyName();
    try {
      await Promise.all([
        fetchSimulatorStatus(),
        fetchSimulatorStats(),
        fetchUnityData(companyName)
      ]);
    } catch (err) {
      console.error('데이터 새로고침 실패:', err);
    }
    setIsLoading(false);
  };

  // 자동 새로고침 토글
  const toggleAutoRefresh = () => {
    setAutoRefresh(!autoRefresh);
  };

  // 자동 새로고침 효과
  useEffect(() => {
    let interval;
    if (autoRefresh) {
      interval = setInterval(refreshAllData, 5000); // 5초마다 새로고침
    }
    return () => {
      if (interval) clearInterval(interval);
    };
  }, [autoRefresh]);

  // 컴포넌트 마운트 시 초기 데이터 로드 및 MQTT 연결
  useEffect(() => {
    const initializeComponent = async () => {
      // MQTT 연결 초기화
      await initializeMQTT();
      
      // 데이터 로드
      refreshAllData();
    };
    
    initializeComponent();
    
    // 컴포넌트 언마운트 시 MQTT 연결 해제
    return () => {
      if (mqttService.isConnectedToBroker()) {
        mqttService.disconnect();
      }
    };
  }, []);

  // 실행 중인 라인 개수 계산
  const getRunningLinesCount = () => {
    if (!simulatorStatus) return 0;
    return Object.values(simulatorStatus).filter(line => line.isRunning).length;
  };

  // Unity 데이터에서 제품 개수 계산
  const getProductCount = () => {
    if (!unityData || !unityData.products) return 0;
    return Object.keys(unityData.products).length;
  };

  // Unity 데이터에서 로봇 개수 계산
  const getRobotCount = () => {
    if (!unityData || !unityData.robots) return 0;
    return Object.keys(unityData.robots).length;
  };

  return (
    <div className="simulator-control">
      <div className="simulator-header">
        <h1>🏭 제조 디지털 트윈 시뮬레이터 제어 (MQTT 직접 전송)</h1>
        
        {/* MQTT 연결 상태 및 라인 선택 */}
        <div className="mqtt-status-section">
          <div className="mqtt-status">
            <span className={`status-indicator ${mqttConnected ? 'connected' : 'disconnected'}`}>
              {mqttConnected ? '🟢 MQTT 연결됨' : '🔴 MQTT 연결 안됨'}
            </span>
          </div>
          <div className="line-selector">
            <label htmlFor="lineSelect">생산 라인: </label>
            <select 
              id="lineSelect" 
              value={selectedLineId} 
              onChange={(e) => setSelectedLineId(parseInt(e.target.value))}
              className="form-select"
            >
              <option value={1}>라인 1</option>
              <option value={2}>라인 2</option>
            </select>
          </div>
        </div>
        
        <div className="control-buttons">
          <button 
            className="btn btn-success" 
            onClick={handleStartSimulation}
            disabled={isLoading}
          >
            ▶️ 시뮬레이션 시작
          </button>
          <button 
            className="btn btn-danger" 
            onClick={handleStopSimulation}
            disabled={isLoading}
          >
            ⏹️ 시뮬레이션 중지
          </button>
          <button 
            className="btn btn-warning" 
            onClick={handleResetSimulation}
            disabled={isLoading}
          >
            🔄 시뮬레이션 리셋
          </button>
          <button 
            className="btn btn-info" 
            onClick={refreshAllData}
            disabled={isLoading}
          >
            🔄 데이터 새로고침
          </button>
          <button 
            className={`btn ${autoRefresh ? 'btn-primary' : 'btn-secondary'}`}
            onClick={toggleAutoRefresh}
          >
            {autoRefresh ? '⏸️ 자동새로고침 중지' : '▶️ 자동새로고침 시작'}
          </button>
          <button 
            className="btn btn-success" 
            onClick={handleCreateProduct}
            disabled={isLoading || !mqttConnected}
          >
            🚗 제품 생성 (MQTT)
          </button>
        </div>
      </div>

      {error && (
        <div className="alert alert-danger">
          <strong>오류:</strong> {error}
        </div>
      )}

      {isLoading && (
        <div className="loading-indicator">
          <div className="spinner"></div>
          <span>처리 중...</span>
        </div>
      )}

      <div className="simulator-dashboard">
        {/* 시뮬레이터 상태 카드 */}
        <div className="status-card">
          <h3>📊 시뮬레이터 상태</h3>
          {simulatorStatus ? (
            <div className="status-grid">
              <div className="status-item">
                <span className="label">실행 중인 라인:</span>
                <span className="value">{getRunningLinesCount()}개</span>
              </div>
              {Object.entries(simulatorStatus).map(([key, line]) => (
                <div key={key} className="line-status">
                  <div className="line-header">
                    <span className="line-name">{line.lineName || `라인 ${line.lineId}`}</span>
                    <span className={`status-badge ${line.isRunning ? 'running' : 'stopped'}`}>
                      {line.isRunning ? '🟢 실행중' : '🔴 중지됨'}
                    </span>
                  </div>
                  {line.isRunning && (
                    <div className="line-details">
                      <div>시작 시간: {new Date(line.startTime).toLocaleString()}</div>
                      <div>생산 개수: {line.productionCount}개</div>
                    </div>
                  )}
                </div>
              ))}
            </div>
          ) : (
            <div className="no-data">상태 정보를 불러오는 중...</div>
          )}
        </div>

        {/* Unity 실시간 데이터 카드 */}
        <div className="unity-data-card">
          <h3>🎮 Unity 실시간 데이터</h3>
          {unityData ? (
            <div className="unity-grid">
              <div className="unity-item">
                <span className="label">현재 제품:</span>
                <span className="value">{getProductCount()}개</span>
              </div>
              <div className="unity-item">
                <span className="label">활성 로봇:</span>
                <span className="value">{getRobotCount()}개</span>
              </div>
              <div className="unity-item">
                <span className="label">스테이션:</span>
                <span className="value">{unityData.stations ? Object.keys(unityData.stations).length : 0}개</span>
              </div>
              <div className="unity-item">
                <span className="label">마지막 업데이트:</span>
                <span className="value">{new Date(unityData.timestamp).toLocaleTimeString()}</span>
              </div>
            </div>
          ) : (
            <div className="no-data">Unity 데이터를 불러오는 중...</div>
          )}
        </div>

        {/* 시뮬레이터 통계 카드 */}
        <div className="stats-card">
          <h3>📈 시뮬레이션 통계</h3>
          {simulatorStats ? (
            <div className="stats-grid">
              <div className="stat-item">
                <span className="label">총 라인 수:</span>
                <span className="value">{simulatorStats.totalLines || 0}개</span>
              </div>
              <div className="stat-item">
                <span className="label">실행 중인 라인:</span>
                <span className="value">{simulatorStats.runningLines || 0}개</span>
              </div>
              <div className="stat-item">
                <span className="label">총 생산 개수:</span>
                <span className="value">{simulatorStats.totalProductionCount || 0}개</span>
              </div>
              <div className="stat-item">
                <span className="label">라인별 평균 생산:</span>
                <span className="value">{(simulatorStats.averageProductionPerLine || 0).toFixed(1)}개</span>
              </div>
            </div>
          ) : (
            <div className="no-data">통계 데이터를 불러오는 중...</div>
          )}
        </div>
      </div>

      {/* Unity 제품 상세 정보 */}
      {unityData && unityData.products && Object.keys(unityData.products).length > 0 && (
        <div className="products-section">
          <h3>🚗 현재 생산 중인 제품</h3>
          <div className="products-grid">
            {Object.entries(unityData.products).map(([carId, product]) => (
              <div key={carId} className="product-card">
                <div className="product-header">
                  <span className="product-id">{carId}</span>
                  <span className={`product-status ${product.status?.toLowerCase()}`}>
                    {product.status}
                  </span>
                </div>
                <div className="product-details">
                  <div>📍 현재 스테이션: {product.currentStation}</div>
                  <div>🎨 제품 색상: {product.productColor}</div>
                  <div>🚪 문 색상: {product.doorColor}</div>
                  <div>📊 작업 진행률: {product.workProgress}%</div>
                  <div>🔄 재작업 횟수: {product.reworkCount}</div>
                  {product.position && (
                    <div>📍 위치: ({product.position.x?.toFixed(1)}, {product.position.y?.toFixed(1)})</div>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Unity 로봇 상태 정보 */}
      {unityData && unityData.robots && Object.keys(unityData.robots).length > 0 && (
        <div className="robots-section">
          <h3>🤖 로봇 상태</h3>
          <div className="robots-grid">
            {Object.entries(unityData.robots).map(([robotId, robot]) => (
              <div key={robotId} className="robot-card">
                <div className="robot-header">
                  <span className="robot-id">{robotId}</span>
                  <span className={`robot-status ${robot.statusText?.toLowerCase()}`}>
                    {robot.statusText}
                  </span>
                </div>
                <div className="robot-details">
                  <div>🔋 배터리: {robot.batteryLevel}%</div>
                  <div>⚡ 동작: {robot.currentAction}</div>
                  <div>🏭 라인: {robot.lineId}</div>
                  <div>🌡️ 온도: {robot.temperature}°C</div>
                  <div>⚡ 전력: {robot.powerConsumption}W</div>
                  {robot.position && (
                    <div>📍 위치: ({robot.position.x?.toFixed(1)}, {robot.position.y?.toFixed(1)})</div>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};

export default SimulatorControl;