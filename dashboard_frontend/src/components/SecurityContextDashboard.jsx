import React, { useState, useEffect } from 'react';
import apiService from '../service/apiService';

const SecurityContextDashboard = () => {
  const [securityContext, setSecurityContext] = useState(null);
  const [deviceTrust, setDeviceTrust] = useState(null);
  const [userRole, setUserRole] = useState(null);
  const [companyStats, setCompanyStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [refreshInterval, setRefreshInterval] = useState(null);

  // 사용자 정보 (실제로는 Context나 Redux에서 가져올 것)
  const [currentUser] = useState(() => {
    try {
      const userData = JSON.parse(localStorage.getItem('userData') || '{}');
      return {
        userId: userData.userId || 1,
        companyId: userData.companyId || 1,
        username: userData.username || 'admin'
      };
    } catch {
      return { userId: 1, companyId: 1, username: 'admin' };
    }
  });

  // 보안 컨텍스트 데이터 로드
  const loadSecurityData = async () => {
    try {
      setLoading(true);
      
      // 병렬로 모든 보안 데이터 로드
      const [contextResult, trustResult, roleResult, statsResult] = await Promise.allSettled([
        apiService.security.assessContext(currentUser.userId, currentUser.companyId),
        apiService.security.evaluateDeviceTrust(currentUser.userId),
        apiService.security.getUserRoleLevel(currentUser.userId, currentUser.companyId),
        apiService.security.getCompanyRoleStatistics(currentUser.companyId)
      ]);

      // 결과 처리
      if (contextResult.status === 'fulfilled') {
        setSecurityContext(contextResult.value);
      }
      
      if (trustResult.status === 'fulfilled') {
        setDeviceTrust(trustResult.value);
      }
      
      if (roleResult.status === 'fulfilled') {
        setUserRole(roleResult.value);
      }
      
      if (statsResult.status === 'fulfilled') {
        setCompanyStats(statsResult.value);
      }

      setError('');
    } catch (err) {
      console.error('보안 데이터 로드 실패:', err);
      setError('보안 데이터를 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  // 컴포넌트 마운트시 데이터 로드 및 자동 새로고침 설정
  useEffect(() => {
    loadSecurityData();
    
    // 30초마다 자동 새로고침
    const interval = setInterval(loadSecurityData, 30000);
    setRefreshInterval(interval);
    
    return () => {
      if (interval) clearInterval(interval);
    };
  }, []);

  // 위험도 레벨에 따른 색상 반환
  const getRiskColor = (riskScore) => {
    if (riskScore <= 10) return 'text-success';
    if (riskScore <= 25) return 'text-info';
    if (riskScore <= 50) return 'text-warning';
    return 'text-danger';
  };

  // 위험도 레벨에 따른 배경색 반환
  const getRiskBgColor = (riskScore) => {
    if (riskScore <= 10) return 'bg-success-subtle';
    if (riskScore <= 25) return 'bg-info-subtle';
    if (riskScore <= 50) return 'bg-warning-subtle';
    return 'bg-danger-subtle';
  };

  // 신뢰도 레벨 아이콘
  const getTrustIcon = (trustLevel) => {
    switch (trustLevel) {
      case 'TRUSTED': return 'ti ti-shield-check text-success';
      case 'FAMILIAR': return 'ti ti-shield text-info';
      case 'RECOGNIZED': return 'ti ti-shield-half text-warning';
      case 'UNKNOWN': return 'ti ti-shield-x text-danger';
      default: return 'ti ti-shield text-muted';
    }
  };

  // 권한 레벨 아이콘
  const getRoleIcon = (roleLevel) => {
    if (roleLevel?.includes('ADMIN')) return 'ti ti-crown text-warning';
    if (roleLevel?.includes('OWNER')) return 'ti ti-building text-primary';
    if (roleLevel?.includes('HEAD')) return 'ti ti-user-star text-info';
    return 'ti ti-user text-secondary';
  };

  // 수동 새로고침
  const handleRefresh = () => {
    loadSecurityData();
  };

  if (loading && !securityContext) {
    return (
      <div className="d-flex justify-content-center align-items-center" style={{ minHeight: '400px' }}>
        <div className="text-center">
          <div className="spinner-border text-primary mb-3" role="status">
            <span className="visually-hidden">Loading...</span>
          </div>
          <p className="text-muted">보안 컨텍스트 분석 중...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="container-fluid py-4">
      {/* 헤더 */}
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2 className="mb-1">
            <i className="ti ti-shield-check me-2 text-primary"></i>
            보안 컨텍스트 대시보드
          </h2>
          <p className="text-muted mb-0">실시간 보안 상태 모니터링 및 위험도 분석</p>
        </div>
        <button 
          className="btn btn-outline-primary"
          onClick={handleRefresh}
          disabled={loading}
        >
          {loading ? (
            <span className="spinner-border spinner-border-sm me-1"></span>
          ) : (
            <i className="ti ti-refresh me-1"></i>
          )}
          새로고침
        </button>
      </div>

      {error && (
        <div className="alert alert-danger mb-4">
          <i className="ti ti-alert-circle me-2"></i>
          {error}
        </div>
      )}

      <div className="row">
        {/* 종합 위험도 점수 */}
        {securityContext && (
          <div className="col-lg-4 mb-4">
            <div className="card h-100 shadow-sm">
              <div className="card-body text-center">
                <div className="mb-3">
                  <div 
                    className={`d-inline-flex align-items-center justify-content-center rounded-circle ${getRiskBgColor(securityContext.riskScore)}`}
                    style={{ width: '80px', height: '80px' }}
                  >
                    <span className={`fs-3 fw-bold ${getRiskColor(securityContext.riskScore)}`}>
                      {securityContext.riskScore}
                    </span>
                  </div>
                </div>
                <h5 className="card-title">종합 위험도 점수</h5>
                <p className="text-muted mb-3">
                  보안 레벨: <span className={`fw-bold ${getRiskColor(securityContext.riskScore)}`}>
                    {securityContext.securityLevel}
                  </span>
                </p>
                
                {/* 위험도 세부 항목 */}
                {securityContext.riskDetails && (
                  <div className="mt-3">
                    <div className="row g-2 small">
                      <div className="col-6">
                        <div className="text-muted">기본 위험도</div>
                        <div className="fw-semibold">{securityContext.riskDetails.baseRisk || 0}</div>
                      </div>
                      <div className="col-6">
                        <div className="text-muted">시간 위험도</div>
                        <div className="fw-semibold">{securityContext.riskDetails.timeRisk || 0}</div>
                      </div>
                      <div className="col-6">
                        <div className="text-muted">디바이스 위험도</div>
                        <div className="fw-semibold">{securityContext.riskDetails.deviceRisk || 0}</div>
                      </div>
                      <div className="col-6">
                        <div className="text-muted">위치 위험도</div>
                        <div className="fw-semibold">{securityContext.riskDetails.locationRisk || 0}</div>
                      </div>
                    </div>
                  </div>
                )}
              </div>
            </div>
          </div>
        )}

        {/* 디바이스 신뢰도 */}
        {deviceTrust && (
          <div className="col-lg-4 mb-4">
            <div className="card h-100 shadow-sm">
              <div className="card-body">
                <div className="d-flex align-items-center mb-3">
                  <i className={getTrustIcon(deviceTrust.trustLevel) + ' fs-2 me-3'}></i>
                  <div>
                    <h5 className="card-title mb-1">디바이스 신뢰도</h5>
                    <span className="badge bg-primary">
                      {deviceTrust.trustLevel}
                    </span>
                  </div>
                </div>
                
                <div className="mb-3">
                  <div className="d-flex justify-content-between mb-1">
                    <span className="small text-muted">신뢰도 점수</span>
                    <span className="small fw-semibold">{deviceTrust.trustScore}/100</span>
                  </div>
                  <div className="progress" style={{ height: '6px' }}>
                    <div 
                      className="progress-bar bg-info" 
                      style={{ width: `${deviceTrust.trustScore}%` }}
                    ></div>
                  </div>
                </div>

                <div className="small text-muted">
                  <div className="mb-1">
                    <i className="ti ti-device-laptop me-1"></i>
                    브라우저: {deviceTrust.userAgent ? 
                      deviceTrust.userAgent.substring(0, 30) + '...' : 'Unknown'}
                  </div>
                  <div>
                    <i className="ti ti-world me-1"></i>
                    IP: {deviceTrust.clientIp || 'Unknown'}
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* 사용자 권한 정보 */}
        {userRole && (
          <div className="col-lg-4 mb-4">
            <div className="card h-100 shadow-sm">
              <div className="card-body">
                <div className="d-flex align-items-center mb-3">
                  <i className={getRoleIcon(userRole.roleLevel) + ' fs-2 me-3'}></i>
                  <div>
                    <h5 className="card-title mb-1">권한 정보</h5>
                    <span className="text-muted">{currentUser.username}</span>
                  </div>
                </div>
                
                <div className="mb-3">
                  <div className="d-flex justify-content-between mb-2">
                    <span className="text-muted">권한 레벨</span>
                    <span className="fw-semibold">{userRole.description}</span>
                  </div>
                  <div className="d-flex justify-content-between">
                    <span className="text-muted">권한 점수</span>
                    <span className="fw-semibold">{userRole.level}/100</span>
                  </div>
                </div>

                <div className="progress mb-3" style={{ height: '6px' }}>
                  <div 
                    className="progress-bar bg-warning" 
                    style={{ width: `${userRole.level}%` }}
                  ></div>
                </div>

                <div className="small">
                  <span className="badge bg-secondary me-1">
                    {userRole.roleLevel}
                  </span>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>

      <div className="row">
        {/* 보안 권장사항 */}
        {securityContext?.recommendations && securityContext.recommendations.length > 0 && (
          <div className="col-lg-6 mb-4">
            <div className="card shadow-sm">
              <div className="card-header bg-warning-subtle">
                <h6 className="card-title mb-0">
                  <i className="ti ti-lightbulb me-2 text-warning"></i>
                  보안 권장사항
                </h6>
              </div>
              <div className="card-body">
                <ul className="list-unstyled mb-0">
                  {securityContext.recommendations.map((recommendation, index) => (
                    <li key={index} className="mb-2">
                      <i className="ti ti-arrow-right me-2 text-muted"></i>
                      {recommendation}
                    </li>
                  ))}
                </ul>
              </div>
            </div>
          </div>
        )}

        {/* 회사 권한 통계 */}
        {companyStats?.statistics && (
          <div className="col-lg-6 mb-4">
            <div className="card shadow-sm">
              <div className="card-header bg-info-subtle">
                <h6 className="card-title mb-0">
                  <i className="ti ti-users me-2 text-info"></i>
                  회사 권한 통계
                </h6>
              </div>
              <div className="card-body">
                <div className="small text-muted mb-3">
                  총 사용자: <span className="fw-bold text-dark">{companyStats.totalUsers}</span>명
                </div>
                
                <div className="row g-2">
                  {Object.entries(companyStats.statistics).map(([role, count]) => (
                    <div key={role} className="col-6">
                      <div className="d-flex justify-content-between align-items-center p-2 bg-light rounded">
                        <span className="small">{role}</span>
                        <span className="badge bg-primary">{count}</span>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* 실시간 업데이트 정보 */}
      <div className="row">
        <div className="col-12">
          <div className="card shadow-sm">
            <div className="card-body py-3">
              <div className="d-flex justify-content-between align-items-center">
                <div className="d-flex align-items-center text-muted small">
                  <div className="spinner-grow spinner-grow-sm text-success me-2" style={{ width: '0.75rem', height: '0.75rem' }}></div>
                  실시간 모니터링 중 - 30초마다 자동 업데이트
                </div>
                <div className="text-muted small">
                  마지막 업데이트: {new Date().toLocaleTimeString()}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SecurityContextDashboard;