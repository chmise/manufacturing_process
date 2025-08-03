import React, { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import SampleModeGuide from './SampleModeGuide';
import SampleModeIndicator from './SampleModeIndicator';
import OnboardingTour from './OnboardingTour';
import apiService from '../service/apiService';

/**
 * 샘플 모드 통합 관리 컴포넌트
 * "5분 MES" - 샘플 모드의 모든 기능을 통합 관리
 */
const SampleModeManager = ({ companyName, industryCode, children, onModeChange }) => {
  const [sampleModeStatus, setSampleModeStatus] = useState({
    isActive: false,
    loading: true
  });
  const [showGuide, setShowGuide] = useState(false);
  const [showTour, setShowTour] = useState(false);
  const [isFirstVisit, setIsFirstVisit] = useState(false);

  useEffect(() => {
    checkSampleModeStatus();
    checkFirstVisit();
  }, [companyName]);

  const checkSampleModeStatus = async () => {
    try {
      const response = await apiService.get(`/api/${companyName}/sample-data/status`);
      setSampleModeStatus({
        isActive: response.data.sampleMode,
        loading: false
      });

      // 샘플 모드가 활성화되어 있고 첫 방문이면 가이드 표시
      if (response.data.sampleMode && isFirstVisit) {
        setShowGuide(true);
      }
    } catch (error) {
      console.error('샘플 모드 상태 확인 실패:', error);
      setSampleModeStatus({
        isActive: false,
        loading: false
      });
    }
  };

  const checkFirstVisit = () => {
    const visitKey = `mes_first_visit_${companyName}`;
    const hasVisited = localStorage.getItem(visitKey);
    
    if (!hasVisited) {
      setIsFirstVisit(true);
      localStorage.setItem(visitKey, 'true');
    }
  };

  const handleSwitchToReal = async () => {
    try {
      await apiService.post(`/api/${companyName}/sample-data/switch-to-real`);
      setSampleModeStatus({
        isActive: false,
        loading: false
      });
      onModeChange?.('real');
      
      // 성공 메시지 표시
      showNotification('실제 데이터 모드로 전환되었습니다. 이제 실제 장비와 연결하세요.', 'success');
    } catch (error) {
      console.error('실제 데이터 전환 실패:', error);
      showNotification('전환 중 오류가 발생했습니다. 다시 시도해주세요.', 'error');
    }
  };

  const handleGuideComplete = () => {
    setShowGuide(false);
    // 가이드 완료 후 투어 시작 여부 확인
    const tourKey = `mes_tour_completed_${companyName}`;
    const tourCompleted = localStorage.getItem(tourKey);
    
    if (!tourCompleted) {
      setTimeout(() => setShowTour(true), 500); // 약간의 딜레이 후 투어 시작
    }
  };

  const handleTourComplete = () => {
    setShowTour(false);
    const tourKey = `mes_tour_completed_${companyName}`;
    localStorage.setItem(tourKey, 'true');
    
    showNotification('MES 둘러보기가 완료되었습니다! 이제 자유롭게 체험해보세요.', 'info');
  };

  const handleTourSkip = () => {
    setShowTour(false);
    const tourKey = `mes_tour_skipped_${companyName}`;
    localStorage.setItem(tourKey, 'true');
  };

  const showNotification = (message, type = 'info') => {
    // 간단한 토스트 알림 (실제로는 toast 라이브러리 사용 권장)
    const alertClass = {
      success: 'alert-success',
      error: 'alert-danger',
      info: 'alert-info',
      warning: 'alert-warning'
    };

    const notification = document.createElement('div');
    notification.className = `alert ${alertClass[type]} position-fixed top-0 end-0 m-3`;
    notification.style.zIndex = '9999';
    notification.innerHTML = `
      <div class="d-flex justify-content-between align-items-center">
        <span>${message}</span>
        <button type="button" class="btn-close" onclick="this.parentElement.parentElement.remove()"></button>
      </div>
    `;
    
    document.body.appendChild(notification);
    
    // 5초 후 자동 제거
    setTimeout(() => {
      if (notification.parentElement) {
        notification.remove();
      }
    }, 5000);
  };

  const restartTour = () => {
    const tourKey = `mes_tour_completed_${companyName}`;
    localStorage.removeItem(tourKey);
    setShowTour(true);
  };

  const openGuide = () => {
    setShowGuide(true);
  };

  if (sampleModeStatus.loading) {
    return (
      <div className="d-flex justify-content-center align-items-center" style={{ minHeight: '200px' }}>
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">로딩중...</span>
        </div>
      </div>
    );
  }

  if (!sampleModeStatus.isActive) {
    // 실제 데이터 모드일 때는 children만 렌더링
    return <>{children}</>;
  }

  return (
    <div className="sample-mode-container">
      {/* 샘플 모드 표시기 */}
      <div data-tour="sample-indicator">
        <SampleModeIndicator
          companyName={companyName}
          industryCode={industryCode}
          onSwitchToReal={handleSwitchToReal}
        />
      </div>

      {/* 메인 컨텐츠 */}
      <div className="position-relative">
        {children}

        {/* 샘플 모드 도움말 플로팅 버튼 */}
        <div className="position-fixed bottom-0 end-0 m-3" style={{ zIndex: 1020 }}>
          <div className="dropdown dropup">
            <button 
              className="btn btn-info rounded-circle shadow"
              type="button"
              data-bs-toggle="dropdown"
              style={{ width: '56px', height: '56px' }}
            >
              <i className="fas fa-question-circle fs-4"></i>
            </button>
            
            <ul className="dropdown-menu dropdown-menu-end">
              <li>
                <button className="dropdown-item" onClick={openGuide}>
                  <i className="fas fa-book me-2"></i>
                  사용 가이드 보기
                </button>
              </li>
              <li>
                <button className="dropdown-item" onClick={restartTour}>
                  <i className="fas fa-route me-2"></i>
                  둘러보기 다시하기
                </button>
              </li>
              <li><hr className="dropdown-divider" /></li>
              <li>
                <button className="dropdown-item text-primary" onClick={handleSwitchToReal}>
                  <i className="fas fa-plug me-2"></i>
                  실제 데이터 연결
                </button>
              </li>
            </ul>
          </div>
        </div>
      </div>

      {/* 가이드 모달 */}
      {showGuide && (
        <SampleModeGuide
          companyName={companyName}
          industryCode={industryCode}
          onSwitchToReal={handleSwitchToReal}
          onClose={handleGuideComplete}
        />
      )}

      {/* 온보딩 투어 */}
      {showTour && (
        <OnboardingTour
          industryCode={industryCode}
          onComplete={handleTourComplete}
          onSkip={handleTourSkip}
        />
      )}
    </div>
  );
};

SampleModeManager.propTypes = {
  companyName: PropTypes.string.isRequired,
  industryCode: PropTypes.string.isRequired,
  children: PropTypes.node.isRequired,
  onModeChange: PropTypes.func
};

export default SampleModeManager;