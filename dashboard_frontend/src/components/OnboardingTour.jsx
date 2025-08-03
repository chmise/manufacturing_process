import React, { useState, useEffect } from 'react';
import PropTypes from 'prop-types';

/**
 * 온보딩 투어 컴포넌트
 * 첫 사용자를 위한 대시보드 주요 기능 안내 투어
 */
const OnboardingTour = ({ industryCode, onComplete, onSkip }) => {
  const [currentStep, setCurrentStep] = useState(0);
  const [isVisible, setIsVisible] = useState(true);

  // 업종별 맞춤 투어 스텝
  const getTourSteps = (industryCode) => {
    const commonSteps = [
      {
        target: '[data-tour="sample-indicator"]',
        title: '🎯 체험 모드 안내',
        content: '현재 샘플 데이터로 MES를 체험 중입니다. 언제든지 실제 데이터로 전환할 수 있어요.',
        position: 'bottom'
      },
      {
        target: '[data-tour="main-kpis"]',
        title: '📊 핵심 지표 (KPI)',
        content: '생산 효율성, 품질, 일정 준수 등 핵심 지표를 한눈에 확인하세요.',
        position: 'bottom'
      },
      {
        target: '[data-tour="production-chart"]',
        title: '📈 실시간 생산 현황',
        content: '시간별 생산량과 목표 달성률을 실시간으로 모니터링할 수 있습니다.',
        position: 'top'
      },
      {
        target: '[data-tour="quality-monitor"]',
        title: '⭐ 품질 관리',
        content: '품질 지표와 불량률을 추적하여 품질 개선에 활용하세요.',
        position: 'left'
      }
    ];

    const industrySpecificSteps = {
      'AUTOMOTIVE': [
        {
          target: '[data-tour="cycle-time"]',
          title: '⏱️ 사이클 타임 모니터링',
          content: '자동차 제조의 핵심인 사이클 타임을 실시간으로 추적합니다. 목표 시간 대비 현재 성과를 확인하세요.',
          position: 'right'
        },
        {
          target: '[data-tour="torque-management"]',
          title: '🔧 토크 관리',
          content: '볼트 체결 토크 값을 모니터링하여 조립 품질을 보장합니다.',
          position: 'top'
        }
      ],
      'ELECTRONICS': [
        {
          target: '[data-tour="yield-monitor"]',
          title: '📈 수율 관리',
          content: '웨이퍼 수율과 테스트 통과율을 모니터링하여 생산 효율을 최적화하세요.',
          position: 'right'
        },
        {
          target: '[data-tour="equipment-status"]',
          title: '🎛️ 정밀 장비 상태',
          content: '반도체 장비의 온도, 진동 등 정밀 상태를 실시간으로 추적합니다.',
          position: 'top'
        }
      ],
      'FOOD_BEVERAGE': [
        {
          target: '[data-tour="haccp-monitor"]',
          title: '🛡️ HACCP 관리',
          content: '식품 안전 기준을 실시간으로 모니터링하여 안전한 제품을 생산하세요.',
          position: 'right'
        },
        {
          target: '[data-tour="temperature-control"]',
          title: '🌡️ 온도 관리',
          content: '냉장, 냉동, 가열 온도를 실시간으로 추적하여 품질을 보장합니다.',
          position: 'top'
        }
      ]
    };

    return [
      ...commonSteps,
      ...(industrySpecificSteps[industryCode] || []),
      {
        target: '[data-tour="digital-twin"]',
        title: '🏭 3D 디지털 트윈',
        content: '3D로 제조 현장을 시각화하여 직관적으로 상황을 파악할 수 있습니다.',
        position: 'left'
      },
      {
        target: '[data-tour="alerts"]',
        title: '🚨 실시간 알림',
        content: '중요한 이슈가 발생하면 즉시 알림을 받아 빠르게 대응할 수 있습니다.',
        position: 'bottom'
      }
    ];
  };

  const tourSteps = getTourSteps(industryCode);

  useEffect(() => {
    // 현재 스텝에 해당하는 요소 하이라이트
    const currentTarget = document.querySelector(tourSteps[currentStep]?.target);
    if (currentTarget) {
      currentTarget.style.boxShadow = '0 0 0 4px rgba(0, 123, 255, 0.3)';
      currentTarget.style.zIndex = '1030';
      currentTarget.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }

    return () => {
      // 하이라이트 제거
      document.querySelectorAll('[data-tour]').forEach(el => {
        el.style.boxShadow = '';
        el.style.zIndex = '';
      });
    };
  }, [currentStep, tourSteps]);

  const handleNext = () => {
    if (currentStep < tourSteps.length - 1) {
      setCurrentStep(currentStep + 1);
    } else {
      handleComplete();
    }
  };

  const handlePrevious = () => {
    if (currentStep > 0) {
      setCurrentStep(currentStep - 1);
    }
  };

  const handleComplete = () => {
    setIsVisible(false);
    // 모든 하이라이트 제거
    document.querySelectorAll('[data-tour]').forEach(el => {
      el.style.boxShadow = '';
      el.style.zIndex = '';
    });
    onComplete?.();
  };

  const handleSkip = () => {
    setIsVisible(false);
    // 모든 하이라이트 제거
    document.querySelectorAll('[data-tour]').forEach(el => {
      el.style.boxShadow = '';
      el.style.zIndex = '';
    });
    onSkip?.();
  };

  if (!isVisible || !tourSteps[currentStep]) {
    return null;
  }

  const currentTourStep = tourSteps[currentStep];

  return (
    <>
      {/* 오버레이 */}
      <div 
        className="position-fixed top-0 start-0 w-100 h-100"
        style={{ 
          backgroundColor: 'rgba(0, 0, 0, 0.4)', 
          zIndex: 1025 
        }}
        onClick={handleSkip}
      />

      {/* 투어 팝업 */}
      <div 
        className="position-fixed"
        style={{ 
          zIndex: 1030,
          top: '50%',
          left: '50%',
          transform: 'translate(-50%, -50%)',
          maxWidth: '400px',
          width: '90%'
        }}
      >
        <div className="card shadow-lg border-0">
          <div className="card-header bg-primary text-white d-flex justify-content-between align-items-center">
            <div>
              <h5 className="mb-0">🎓 MES 둘러보기</h5>
              <small className="opacity-75">
                {currentStep + 1} / {tourSteps.length}
              </small>
            </div>
            <button 
              className="btn btn-sm btn-outline-light"
              onClick={handleSkip}
            >
              건너뛰기
            </button>
          </div>

          <div className="card-body">
            <div className="mb-3">
              <h6 className="fw-bold">{currentTourStep.title}</h6>
              <p className="text-muted mb-0">{currentTourStep.content}</p>
            </div>

            {/* 진행 표시기 */}
            <div className="progress mb-3" style={{ height: '4px' }}>
              <div 
                className="progress-bar bg-primary"
                style={{ width: `${((currentStep + 1) / tourSteps.length) * 100}%` }}
              />
            </div>

            {/* 단계 표시 */}
            <div className="d-flex justify-content-center mb-3">
              {tourSteps.map((_, index) => (
                <span
                  key={index}
                  className={`rounded-circle me-1 ${
                    index === currentStep ? 'bg-primary' : 'bg-light'
                  }`}
                  style={{ 
                    width: '8px', 
                    height: '8px', 
                    display: 'inline-block' 
                  }}
                />
              ))}
            </div>
          </div>

          <div className="card-footer bg-light">
            <div className="d-flex justify-content-between">
              <button 
                className="btn btn-outline-secondary"
                onClick={handlePrevious}
                disabled={currentStep === 0}
              >
                <i className="fas fa-arrow-left me-1"></i>
                이전
              </button>

              <button 
                className="btn btn-primary"
                onClick={handleNext}
              >
                {currentStep === tourSteps.length - 1 ? (
                  <>
                    <i className="fas fa-check me-1"></i>
                    완료
                  </>
                ) : (
                  <>
                    다음
                    <i className="fas fa-arrow-right ms-1"></i>
                  </>
                )}
              </button>
            </div>
          </div>
        </div>
      </div>
    </>
  );
};

OnboardingTour.propTypes = {
  industryCode: PropTypes.string.isRequired,
  onComplete: PropTypes.func,
  onSkip: PropTypes.func
};

export default OnboardingTour;