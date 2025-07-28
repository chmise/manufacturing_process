import React, { useState, useEffect } from 'react';
import './SimulatorControl.css';

const SimulatorControl = () => {
  const [simulatorStatus, setSimulatorStatus] = useState({
    isRunning: false,
    status: 'STOPPED',
    currentCarCount: 0,
    carsInProduction: [],
    message: '시뮬레이션 중지됨'
  });
  
  const [loading, setLoading] = useState(false);
  const [lastUpdate, setLastUpdate] = useState(null);

  // 컴포넌트 마운트 시 초기 상태 조회
  useEffect(() => {
    fetchSimulatorStatus();
    
    // 5초마다 상태 업데이트
    const interval = setInterval(fetchSimulatorStatus, 5000);
    
    return () => clearInterval(interval);
  }, []);

  const fetchSimulatorStatus = async () => {
    try {
      const response = await fetch('http://localhost:8080/api/simulator/status');
      if (response.ok) {
        const data = await response.json();
        setSimulatorStatus(data);
        setLastUpdate(new Date().toLocaleTimeString());
      }
    } catch (error) {
      console.error('시뮬레이터 상태 조회 실패:', error);
    }
  };

  const startSimulation = async () => {
    setLoading(true);
    try {
      const response = await fetch('http://localhost:8080/api/simulator/start', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        }
      });
      
      if (response.ok) {
        const data = await response.json();
        setSimulatorStatus(prev => ({ ...prev, ...data }));
        // 시작 후 즉시 상태 업데이트
        setTimeout(fetchSimulatorStatus, 1000);
      } else {
        alert('시뮬레이션 시작 실패');
      }
    } catch (error) {
      console.error('시뮬레이션 시작 오류:', error);
      alert('시뮬레이션 시작 중 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const stopSimulation = async () => {
    setLoading(true);
    try {
      const response = await fetch('http://localhost:8080/api/simulator/stop', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        }
      });
      
      if (response.ok) {
        const data = await response.json();
        setSimulatorStatus(prev => ({ ...prev, ...data }));
        // 중지 후 즉시 상태 업데이트
        setTimeout(fetchSimulatorStatus, 1000);
      } else {
        alert('시뮬레이션 중지 실패');
      }
    } catch (error) {
      console.error('시뮬레이션 중지 오류:', error);
      alert('시뮬레이션 중지 중 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'RUNNING':
        return '#28a745'; // 녹색
      case 'STOPPED':
        return '#6c757d'; // 회색
      case 'ERROR':
        return '#dc3545'; // 빨간색
      default:
        return '#6c757d';
    }
  };

  const getStatusIcon = (status) => {
    switch (status) {
      case 'RUNNING':
        return '▶️';
      case 'STOPPED':
        return '⏹️';
      case 'ERROR':
        return '❌';
      default:
        return '❓';
    }
  };

  return (
    <div className="simulator-control">
      <div className="simulator-header">
        <h2>🏭 제조공정 시뮬레이터</h2>
        <div className="status-indicator">
          <span className="status-icon">{getStatusIcon(simulatorStatus.status)}</span>
          <span 
            className="status-text"
            style={{ color: getStatusColor(simulatorStatus.status) }}
          >
            {simulatorStatus.status}
          </span>
        </div>
      </div>

      <div className="simulator-info">
        <div className="info-card">
          <h4>📊 현재 상태</h4>
          <p><strong>상태:</strong> {simulatorStatus.message}</p>
          <p><strong>생산 중인 차량:</strong> {simulatorStatus.currentCarCount}대</p>
          {lastUpdate && (
            <p><strong>마지막 업데이트:</strong> {lastUpdate}</p>
          )}
        </div>

        {simulatorStatus.carsInProduction.length > 0 && (
          <div className="info-card">
            <h4>🚗 생산 중인 차량 목록</h4>
            <div className="car-list">
              {simulatorStatus.carsInProduction.map((carId, index) => (
                <span key={index} className="car-item">
                  {carId}
                </span>
              ))}
            </div>
          </div>
        )}
      </div>

      <div className="simulator-controls">
        <button
          className="control-btn start-btn"
          onClick={startSimulation}
          disabled={loading || simulatorStatus.isRunning}
        >
          {loading ? '처리 중...' : '🚀 시뮬레이션 시작'}
        </button>

        <button
          className="control-btn stop-btn"
          onClick={stopSimulation}
          disabled={loading || !simulatorStatus.isRunning}
        >
          {loading ? '처리 중...' : '⏹️ 시뮬레이션 중지'}
        </button>
      </div>

      <div className="simulator-features">
        <h4>🔧 시뮬레이터 기능</h4>
        <div className="features-grid">
          <div className="feature-item">
            <span className="feature-icon">🌡️</span>
            <div>
              <strong>환경 센서</strong>
              <small>온도, 습도, 공기질 (10초마다)</small>
            </div>
          </div>
          
          <div className="feature-item">
            <span className="feature-icon">🚗</span>
            <div>
              <strong>차량 생산</strong>
              <small>새 차량 30초마다 생산</small>
            </div>
          </div>
          
          <div className="feature-item">
            <span className="feature-icon">🔄</span>
            <div>
              <strong>공정 진행</strong>
              <small>도어조립 → 누수테스트</small>
            </div>
          </div>
          
          <div className="feature-item">
            <span className="feature-icon">🤖</span>
            <div>
              <strong>로봇 제어</strong>
              <small>5대 로봇 실시간 상태</small>
            </div>
          </div>
          
          <div className="feature-item">
            <span className="feature-icon">📡</span>
            <div>
              <strong>MQTT 통신</strong>
              <small>실시간 IoT 데이터 전송</small>
            </div>
          </div>
          
          <div className="feature-item">
            <span className="feature-icon">🎮</span>
            <div>
              <strong>Unity 연동</strong>
              <small>3D 디지털 트윈 실시간 업데이트</small>
            </div>
          </div>
        </div>
      </div>

      <div className="simulator-description">
        <h4>ℹ️ 시뮬레이터 정보</h4>
        <p>
          현대자동차 의장공정 디지털 트윈 시뮬레이터입니다. 
          DoorStation → WaterLeakTestStation 2개 공정을 시뮬레이션하며, 
          실시간 MQTT 데이터를 생성하여 Unity 3D 환경과 연동됩니다.
        </p>
        <p>
          <strong>📈 KPI 지표:</strong> OEE, FTY, OTD 등 실시간 생산성 지표 계산<br/>
          <strong>🎯 품질 관리:</strong> 95% 합격률, 90-150분 사이클타임<br/>
          <strong>🔄 실시간 업데이트:</strong> Unity로 3초마다 데이터 전송
        </p>
      </div>
    </div>
  );
};

export default SimulatorControl;