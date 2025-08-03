import React, { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import apiService from '../service/apiService';

/**
 * 샘플 모드 가이드 컴포넌트
 * "5분 MES" - 설정 완료 후 바로 체험할 수 있는 안내 시스템
 */
const SampleModeGuide = ({ companyName, industryCode, onSwitchToReal, onClose }) => {
  const [guideData, setGuideData] = useState(null);
  const [benchmarkData, setBenchmarkData] = useState(null);
  const [currentStep, setCurrentStep] = useState(0);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadGuideData();
    loadBenchmarkData();
  }, [industryCode]);

  const loadGuideData = async () => {
    try {
      const response = await apiService.get(`/api/${companyName}/sample-data/guide`, {
        params: { industryCode }
      });
      setGuideData(response.data.guide);
    } catch (error) {
      console.error('가이드 데이터 로딩 실패:', error);
    }
  };

  const loadBenchmarkData = async () => {
    try {
      const response = await apiService.get(`/api/${companyName}/sample-data/benchmark`, {
        params: { industryCode }
      });
      setBenchmarkData(response.data.benchmark);
    } catch (error) {
      console.error('벤치마킹 데이터 로딩 실패:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSwitchToReal = async () => {
    if (window.confirm('샘플 모드를 종료하고 실제 데이터 모드로 전환하시겠습니까?')) {
      try {
        await apiService.post(`/api/${companyName}/sample-data/switch-to-real`);
        onSwitchToReal?.();
      } catch (error) {
        console.error('실제 데이터 전환 실패:', error);
        alert('전환 중 오류가 발생했습니다.');
      }
    }
  };

  const guideSteps = [
    {
      title: "🎉 설정 완료!",
      content: "축하합니다! 5분만에 MES 설정이 완료되었습니다.",
      details: [
        "업종별 맞춤 대시보드가 준비되었습니다",
        "샘플 데이터로 바로 체험해보세요",
        "실제 장비 연결 전에 미리 경험할 수 있습니다"
      ]
    },
    {
      title: "📊 업종별 특화 기능",
      content: guideData?.title || "업종별 맞춤 기능을 확인해보세요",
      details: guideData?.keyPoints || []
    },
    {
      title: "💡 활용 팁",
      content: "효과적인 MES 활용 방법을 알려드립니다",
      details: guideData?.tips || []
    },
    {
      title: "📈 업계 평균 비교",
      content: "우리 업종 평균과 비교해보세요",
      details: benchmarkData ? [
        `업계 평균 대비 현재 상태를 확인하세요`,
        `지속적인 개선을 통해 경쟁력을 높이세요`,
        `${benchmarkData.description}`
      ] : []
    }
  ];

  if (loading) {
    return (
      <div className="d-flex justify-content-center p-5">
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">로딩중...</span>
        </div>
      </div>
    );
  }

  return (
    <div className="modal fade show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
      <div className="modal-dialog modal-lg modal-dialog-centered">
        <div className="modal-content">
          {/* 헤더 */}
          <div className="modal-header bg-primary text-white">
            <h4 className="modal-title">
              🚀 MES 체험 가이드
              <span className="badge bg-success ms-2">샘플 모드</span>
            </h4>
            <button type="button" className="btn-close btn-close-white" onClick={onClose}></button>
          </div>

          {/* 진행 표시기 */}
          <div className="modal-body p-0">
            <div className="progress" style={{ height: '4px' }}>
              <div 
                className="progress-bar bg-primary" 
                style={{ width: `${((currentStep + 1) / guideSteps.length) * 100}%` }}
              ></div>
            </div>

            <div className="p-4">
              {/* 현재 스텝 내용 */}
              <div className="text-center mb-4">
                <h2>{guideSteps[currentStep]?.title}</h2>
                <p className="text-muted lead">{guideSteps[currentStep]?.content}</p>
              </div>

              {/* 상세 내용 */}
              <div className="row">
                <div className="col-12">
                  <div className="card border-0 bg-light">
                    <div className="card-body">
                      <ul className="list-unstyled mb-0">
                        {guideSteps[currentStep]?.details.map((detail, index) => (
                          <li key={index} className="mb-2">
                            <i className="fas fa-check-circle text-success me-2"></i>
                            {detail}
                          </li>
                        ))}
                      </ul>
                    </div>
                  </div>
                </div>
              </div>

              {/* 벤치마킹 데이터 표시 */}
              {currentStep === 3 && benchmarkData && (
                <div className="row mt-3">
                  <div className="col-12">
                    <div className="card border-primary">
                      <div className="card-header bg-primary text-white">
                        <h5 className="mb-0">📊 업계 평균 지표</h5>
                      </div>
                      <div className="card-body">
                        <div className="row text-center">
                          {Object.entries(benchmarkData).map(([key, value]) => {
                            if (key === 'description') return null;
                            return (
                              <div key={key} className="col-md-4 mb-2">
                                <div className="h4 text-primary">{value}%</div>
                                <small className="text-muted">{key.replace('avg', '평균 ')}</small>
                              </div>
                            );
                          })}
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              )}

              {/* 샘플 모드 안내 */}
              <div className="alert alert-info mt-3">
                <div className="d-flex align-items-center">
                  <i className="fas fa-info-circle me-2"></i>
                  <div>
                    <strong>샘플 모드입니다!</strong> 
                    <span className="ms-2">
                      현재 표시되는 모든 데이터는 체험용 샘플입니다. 
                      실제 장비와 연결하면 실시간 데이터를 확인할 수 있습니다.
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* 푸터 */}
          <div className="modal-footer">
            <div className="d-flex justify-content-between w-100">
              <div>
                {currentStep > 0 && (
                  <button 
                    className="btn btn-outline-secondary"
                    onClick={() => setCurrentStep(currentStep - 1)}
                  >
                    <i className="fas fa-arrow-left me-1"></i>
                    이전
                  </button>
                )}
              </div>

              <div className="d-flex gap-2">
                {currentStep < guideSteps.length - 1 ? (
                  <button 
                    className="btn btn-primary"
                    onClick={() => setCurrentStep(currentStep + 1)}
                  >
                    다음
                    <i className="fas fa-arrow-right ms-1"></i>
                  </button>
                ) : (
                  <>
                    <button 
                      className="btn btn-success"
                      onClick={onClose}
                    >
                      <i className="fas fa-play me-1"></i>
                      체험 시작
                    </button>
                    <button 
                      className="btn btn-outline-primary"
                      onClick={handleSwitchToReal}
                    >
                      <i className="fas fa-plug me-1"></i>
                      실제 데이터 연결
                    </button>
                  </>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

SampleModeGuide.propTypes = {
  companyName: PropTypes.string.isRequired,
  industryCode: PropTypes.string.isRequired,
  onSwitchToReal: PropTypes.func,
  onClose: PropTypes.func.isRequired
};

export default SampleModeGuide;