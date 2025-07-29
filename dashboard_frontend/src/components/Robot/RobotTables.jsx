import React, { useState, useEffect } from 'react';
import apiService from '../../service/apiService';

const RobotTable = ({ lastUpdated }) => {
  const [robots, setRobots] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // 현재 회사명 가져오기
  const getCurrentCompanyName = () => {
    const path = window.location.pathname;
    const segments = path.split('/').filter(Boolean);
    return segments.length > 0 && segments[0] !== 'login' && segments[0] !== 'register' ? segments[0] : null;
  };

  // 로봇 데이터 로드
  const loadRobotData = async () => {
    try {
      setLoading(true);
      setError(null);
      
      const companyName = getCurrentCompanyName();
      if (!companyName) {
        throw new Error('회사 정보를 찾을 수 없습니다.');
      }

      const response = await apiService.robots.getAllRobots(companyName);
      
      if (response && Array.isArray(response)) {
        // 백엔드에서 계산된 데이터를 그대로 사용
        const transformedRobots = response.map(robot => ({
          id: robot.robotId,
          name: robot.robotName || '알 수 없는 로봇',
          location: robot.stationCode || 'N/A',
          status: getStatusDisplay(robot.statusText),
          utilization: `${(robot.utilization || 0).toFixed(1)}%`,
          cycleTime: robot.cycleTime ? `${robot.cycleTime}초` : 'N/A',
          alarm: robot.alarmStatus || '정상',
          health: `${Math.round(robot.health || 0)}점`,
          workCount: `${robot.productionCount || 0}건`,
          connection: robot.connectionStatus || '오프라인',
          temperature: robot.temperature || 0,
          lastUpdate: robot.lastUpdate ? new Date(robot.lastUpdate).toLocaleTimeString() : 'N/A'
        }));
        
        setRobots(transformedRobots);
      } else {
        setRobots([]);
      }
    } catch (err) {
      console.error('로봇 데이터 로드 실패:', err);
      setError(err.message || '로봇 데이터를 불러오는데 실패했습니다.');
      setRobots([]);
    } finally {
      setLoading(false);
    }
  };

  // 상태 표시용 변환 함수
  const getStatusDisplay = (statusText) => {
    const statusMap = {
      'RUNNING': '가동중',
      '작동중': '가동중',
      'IDLE': '대기중',
      '대기중': '대기중',
      'MAINTENANCE': '정지',
      '정지': '정지',
      'ERROR': '오류',
      '오류': '오류'
    };
    return statusMap[statusText] || '알 수 없음';
  };

  // 컴포넌트 마운트 시 데이터 로드
  useEffect(() => {
    loadRobotData();
  }, []);

  // lastUpdated가 변경될 때마다 데이터 새로고침
  useEffect(() => {
    if (lastUpdated) {
      loadRobotData();
    }
  }, [lastUpdated]);

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

  return (
    <div>

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
            {loading ? (
              <tr>
                <td colSpan="12" className="text-center py-5">
                  <div className="text-muted">
                    <div className="spinner-border mb-3" role="status">
                      <span className="visually-hidden">로딩 중...</span>
                    </div>
                    <h5>로봇 데이터를 불러오는 중...</h5>
                  </div>
                </td>
              </tr>
            ) : error ? (
              <tr>
                <td colSpan="12" className="text-center py-5">
                  <div className="text-danger">
                    <i className="ti ti-alert-circle fs-1 mb-3 d-block"></i>
                    <h5>데이터 로딩 실패</h5>
                    <p>{error}</p>
                    <button className="btn btn-primary" onClick={loadRobotData}>
                      다시 시도
                    </button>
                  </div>
                </td>
              </tr>
            ) : robots.length > 0 ? (
              robots.map((robot) => (
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
                    <span className={`badge ${getStatusColor(robot.status)}`}>
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
              {robots.filter(r => r.status === '가동중').length}대
            </div>
          </div>
          <div className="col-md-2">
            <div className="text-muted small">대기 중</div>
            <div className="h5 text-warning">
              {robots.filter(r => r.status === '대기중').length}대
            </div>
          </div>
          <div className="col-md-2">
            <div className="text-muted small">평균 가동률</div>
            <div className="h5 text-primary">
              {robots.length > 0 ? (
                (robots.reduce((sum, r) => sum + parseFloat(r.utilization.replace('%', '')), 0) / robots.length).toFixed(1)
              ) : 0}%
            </div>
          </div>
          <div className="col-md-2">
            <div className="text-muted small">알람 발생</div>
            <div className="h5 text-danger">
              {robots.filter(r => r.alarm !== '정상').length}건
            </div>
          </div>
          <div className="col-md-2">
            <div className="text-muted small">온라인</div>
            <div className="h5 text-success">
              {robots.filter(r => r.connection === '온라인').length}대
            </div>
          </div>
          <div className="col-md-2">
            <div className="text-muted small">총 작업량</div>
            <div className="h5 text-info">
              {robots.reduce((sum, r) => sum + parseInt(r.workCount.replace('건', '')), 0).toLocaleString()}건
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default RobotTable;