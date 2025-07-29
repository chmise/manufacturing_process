import React, { useState, useEffect } from 'react';
import { apiService } from '../../service/apiService';

const RobotTable = ({ stationsData = [], lastUpdated }) => {
  const [robots, setRobots] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // 데이터베이스에서 로봇 데이터 가져오기
  useEffect(() => {
    const fetchRobots = async () => {
      try {
        setLoading(true);
        const response = await apiService.robot.getAllRobots();
        
        if (response && Array.isArray(response)) {
          setRobots(response);
        } else {
          console.error('잘못된 로봇 데이터 형식:', response);
          setRobots([]);
        }
      } catch (error) {
        console.error('로봇 데이터 조회 실패:', error);
        setError(error.message);
        setRobots([]);
      } finally {
        setLoading(false);
      }
    };

    fetchRobots();
  }, []);

  // 데이터베이스 로봇 데이터를 테이블 형식으로 변환
  const transformDatabaseRobot = (robot) => {
    const getStatusText = (statusText) => {
      if (!statusText) return '알 수 없음';
      return statusText;
    };

    const getStatusColor = (statusText) => {
      switch (statusText) {
        case '작동중': 
        case '가동중': return 'bg-primary text-white';
        case '대기중': return 'bg-warning text-dark';
        case '정지': 
        case '중지': return 'bg-danger text-white';
        default: return 'bg-secondary text-white';
      }
    };

    const getUtilization = (statusText, quality) => {
      if (statusText === '작동중' && quality) {
        return `${(quality * 100).toFixed(1)}%`;
      }
      return '0.0%';
    };

    const getAlarmStatus = (statusText) => {
      if (statusText === '작동중') return '정상';
      if (statusText === '대기중') return '경고';
      return '심각';
    };

    const getConnectionStatus = (statusText) => {
      return statusText !== '정지' ? '온라인' : '오프라인';
    };

    return {
      id: robot.robotId || 'Unknown',
      name: robot.robotName || '이름 없음',
      robotType: robot.robotType || '일반',
      location: robot.stationCode || `Line-${robot.lineId || 'A'}`,
      status: getStatusText(robot.statusText),
      statusColor: getStatusColor(robot.statusText),
      utilization: getUtilization(robot.statusText, robot.quality),
      cycleTime: robot.cycleTime ? `${robot.cycleTime}초` : '-',
      alarm: getAlarmStatus(robot.statusText),
      health: robot.quality ? `${Math.round(robot.quality * 100)}점` : '-',
      workCount: robot.productionCount ? `${robot.productionCount}건` : '0건',
      connection: getConnectionStatus(robot.statusText),
      temperature: robot.temperature || 0,
      powerConsumption: robot.powerConsumption || 0,
      lastUpdate: robot.lastUpdate ? new Date(robot.lastUpdate).toLocaleString() : '-',
      motorStatus: robot.motorStatus,
      ledStatus: robot.ledStatus
    };
  };

  // IoT 데이터를 기존 로봇 테이블 형식으로 변환 (백업용)
  const transformStationToRobot = (station, index) => {
    const robotMap = {
      'ROBOT_ARM_01': '로봇팔#1',
      'CONVEYOR_01': '컨베이어#1', 
      'QUALITY_CHECK_01': '품질검사로봇#1',
      'INVENTORY_01': '재고로봇#1'
    };

    const locationMap = {
      'ROBOT_ARM_01': 'Line-A St-1',
      'CONVEYOR_01': 'Line-A St-2',
      'QUALITY_CHECK_01': 'Line-A St-3', 
      'INVENTORY_01': 'Line-A St-4'
    };

    // 상태 변환 (영어 -> 한국어)
    const statusMap = {
      'RUNNING': '가동중',
      'IDLE': '대기중',
      'MAINTENANCE': '정지',
      'ERROR': '정지'
    };

    // 알람 상태 계산
    const getAlarmStatus = (alertCount) => {
      if (alertCount === 0) return '정상';
      if (alertCount <= 2) return '경고';
      return '심각';
    };

    // 건강도 계산 (효율성 기반)
    const calculateHealth = (efficiency, alerts) => {
      const baseHealth = Math.round(parseFloat(efficiency || 0) * 100);
      const penalty = (alerts || 0) * 5; // 알림 1개당 5점 감점
      return Math.max(0, Math.min(100, baseHealth - penalty));
    };

    // 가동률 계산 (효율성과 상태 기반)
    const calculateUtilization = (efficiency, status) => {
      if (status === 'RUNNING') {
        return (parseFloat(efficiency || 0) * 100).toFixed(1) + '%';
      }
      return '0.0%';
    };

    // 사이클 타임 계산
    const calculateCycleTime = (stationId, metrics) => {
      if (metrics?.cycle_time) return `${metrics.cycle_time}초`;
      
      // 스테이션별 기본 사이클 타임
      const defaultCycles = {
        'ROBOT_ARM_01': Math.floor(Math.random() * 10) + 15, // 15-25초
        'CONVEYOR_01': Math.floor(Math.random() * 5) + 8,    // 8-13초
        'QUALITY_CHECK_01': Math.floor(Math.random() * 8) + 12, // 12-20초
        'INVENTORY_01': Math.floor(Math.random() * 15) + 20   // 20-35초
      };
      
      return `${defaultCycles[stationId] || 18}초`;
    };

    // 작업량 계산
    const calculateWorkCount = (metrics, stationId) => {
      let count = 0;
      switch (stationId) {
        case 'ROBOT_ARM_01':
          count = metrics?.assemblies || Math.floor(Math.random() * 200) + 600;
          break;
        case 'CONVEYOR_01':
          count = metrics?.parts_transported || Math.floor(Math.random() * 300) + 800;
          break;
        case 'QUALITY_CHECK_01':
          count = metrics?.inspections || Math.floor(Math.random() * 150) + 400;
          break;
        case 'INVENTORY_01':
          count = metrics?.retrievals || Math.floor(Math.random() * 100) + 200;
          break;
        default:
          count = Math.floor(Math.random() * 200) + 500;
      }
      return `${count}건`;
    };

    const health = calculateHealth(station.efficiency, station.alertCount);
    
    return {
      id: station.stationId || `ROB_${String(index + 1).padStart(3, '0')}`,
      name: robotMap[station.stationId] || `로봇#${index + 1}`,
      location: locationMap[station.stationId] || `Line-A St-${index + 1}`,
      status: statusMap[station.status] || '알 수 없음',
      utilization: calculateUtilization(station.efficiency, station.status),
      cycleTime: calculateCycleTime(station.stationId, station.metrics),
      alarm: getAlarmStatus(station.alertCount || 0),
      health: `${health}점`,
      workCount: calculateWorkCount(station.metrics || {}, station.stationId),
      connection: station.status !== 'ERROR' ? '온라인' : '오프라인',
      temperature: station.temperature || 0,
      lastUpdate: station.lastUpdate || new Date().toLocaleTimeString()
    };
  };

  // 데이터베이스 로봇 데이터 우선 사용, 없으면 IoT 스테이션 데이터 사용
  const displayRobots = robots.length > 0 
    ? robots.map(transformDatabaseRobot)
    : stationsData.length > 0 
      ? stationsData.map(transformStationToRobot)
      : [];

  const getStatusColor = (status) => {
    switch (status) {
      case '가동중': return 'bg-primary text-white';
      case '대기중': return 'bg-warning text-dark';
      case '정지': return 'bg-danger text-white';
      default: return 'bg-secondary text-white';
    }
  };

  const getAlarmColor = (alarm) => {
    switch (alarm) {
      case '정상': return 'bg-success text-white';
      case '경고': return 'bg-warning text-dark';
      case '심각': return 'bg-danger text-white';
      default: return 'bg-secondary text-white';
    }
  };

  const getConnectionColor = (connection) => {
    switch (connection) {
      case '온라인': return 'bg-success text-white';
      case '오프라인': return 'bg-danger text-white';
      default: return 'bg-secondary text-white';
    }
  };

  const getHealthColor = (health) => {
    const score = parseInt(health);
    if (score >= 90) return 'text-success';
    if (score >= 70) return 'text-warning';
    return 'text-danger';
  };

  // 로딩 상태 표시
  if (loading) {
    return (
      <div className="text-center py-5">
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">로딩 중...</span>
        </div>
        <div className="mt-2">로봇 데이터를 불러오는 중...</div>
      </div>
    );
  }

  // 에러 상태 표시
  if (error) {
    return (
      <div className="alert alert-danger">
        <h4>데이터 로딩 오류</h4>
        <p>로봇 데이터를 불러오는 중 오류가 발생했습니다: {error}</p>
        <p>IoT 스테이션 데이터로 대체합니다.</p>
      </div>
    );
  }

  return (
    <div>
      {robots.length > 0 && (
        <div className="alert alert-success mb-3">
          <i className="ti ti-database me-2"></i>
          데이터베이스에서 {robots.length}개의 로봇 데이터를 불러왔습니다.
        </div>
      )}

      <div className="table-responsive">
        <table className="table table-vcenter table-hover">
          <thead>
            <tr>
              <th>로봇ID</th>
              <th className="text-nowrap">로봇명</th>
              <th className="text-nowrap">위치</th>
              <th className="text-nowrap">상태</th>
              <th className="text-nowrap">가동률</th>
              <th className="text-nowrap">사이클타임</th>
              <th className="text-nowrap">알람</th>
              <th className="text-nowrap">건강도</th>
              <th className="text-nowrap">작업량</th>
              <th className="text-nowrap">통신상태</th>
              <th className="text-nowrap">온도</th>
              <th className="text-nowrap">최근 업데이트</th>
            </tr>
          </thead>
          <tbody>
            {displayRobots.length > 0 ? (
              displayRobots.map((robot) => (
                <tr key={robot.id}>
                  <th className="text-primary">{robot.id}</th>
                  <td>
                    <div className="d-flex align-items-center">
                      <span className="me-2">🤖</span>
                      <strong>{robot.name}</strong>
                    </div>
                  </td>
                  <td>
                    <span className="badge bg-light text-dark">{robot.location}</span>
                  </td>
                  <td>
                    <span className={`badge ${robot.statusColor || getStatusColor(robot.status)}`}>
                      {robot.status}
                    </span>
                  </td>
                  <td>
                    <div className="fw-bold">{robot.utilization}</div>
                    <div className="progress mt-1" style={{ height: '4px' }}>
                      <div 
                        className="progress-bar bg-primary"
                        style={{ width: robot.utilization }}
                      ></div>
                    </div>
                  </td>
                  <td>
                    <span className="fw-bold">{robot.cycleTime}</span>
                  </td>
                  <td>
                    <span className={`badge ${getAlarmColor(robot.alarm)}`}>
                      {robot.alarm}
                    </span>
                  </td>
                  <td>
                    <span className={`fw-bold ${getHealthColor(robot.health)}`}>
                      {robot.health}
                    </span>
                  </td>
                  <td>
                    <span className="fw-bold text-info">{robot.workCount}</span>
                  </td>
                  <td>
                    <span className={`badge ${getConnectionColor(robot.connection)}`}>
                      {robot.connection}
                    </span>
                  </td>
                  <td>
                    <span className={robot.temperature > 40 ? 'text-danger' : 'text-success'}>
                      {robot.temperature}°C
                    </span>
                  </td>
                  <td>
                    <span className="status status-blue">
                      {robot.lastUpdate}
                    </span>
                  </td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan="12" className="text-center py-5">
                  <div className="text-muted">
                    <i className="ti ti-robot fs-1 mb-3 d-block"></i>
                    <h5>등록되어 있는 로봇이 없습니다</h5>
                    <p>현재 시스템에 등록된 로봇이 없거나 데이터베이스 연결에 문제가 있습니다.</p>
                  </div>
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {/* 하단 통계 정보 */}
      <div className="card-footer bg-light">
        <div className="row text-center">
          <div className="col-md-2">
            <div className="text-muted small">가동 중</div>
            <div className="h5 text-success">
              {displayRobots.filter(r => r.status === '가동중' || r.status === '작동중').length}대
            </div>
          </div>
          <div className="col-md-2">
            <div className="text-muted small">대기 중</div>
            <div className="h5 text-warning">
              {displayRobots.filter(r => r.status === '대기중').length}대
            </div>
          </div>
          <div className="col-md-2">
            <div className="text-muted small">평균 가동률</div>
            <div className="h5 text-primary">
              {displayRobots.length > 0 ? (
                (displayRobots.reduce((sum, r) => sum + parseFloat(r.utilization), 0) / displayRobots.length).toFixed(1)
              ) : 0}%
            </div>
          </div>
          <div className="col-md-2">
            <div className="text-muted small">알람 발생</div>
            <div className="h5 text-danger">
              {displayRobots.filter(r => r.alarm !== '정상').length}건
            </div>
          </div>
          <div className="col-md-2">
            <div className="text-muted small">온라인</div>
            <div className="h5 text-success">
              {displayRobots.filter(r => r.connection === '온라인').length}대
            </div>
          </div>
          <div className="col-md-2">
            <div className="text-muted small">총 작업량</div>
            <div className="h5 text-info">
              {displayRobots.reduce((sum, r) => sum + parseInt(r.workCount), 0).toLocaleString()}건
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default RobotTable;