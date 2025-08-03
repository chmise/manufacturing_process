import React, { useState } from 'react';
import PropTypes from 'prop-types';
import apiService from '../service/apiService';

/**
 * 샘플 모드 표시 컴포넌트
 * 대시보드 상단에 고정되어 현재 샘플 모드임을 명확히 표시
 */
const SampleModeIndicator = ({ companyName, industryCode, onSwitchToReal }) => {
  const [isExpanded, setIsExpanded] = useState(false);
  const [loading, setLoading] = useState(false);

  const handleSwitchToReal = async () => {
    const confirmed = window.confirm(
      '샘플 모드를 종료하고 실제 데이터 모드로 전환하시겠습니까?\n\n' +
      '• 현재 샘플 데이터가 모두 삭제됩니다\n' +
      '• 실제 장비와 연결해야 데이터를 볼 수 있습니다\n' +
      '• 이 작업은 되돌릴 수 없습니다'
    );

    if (!confirmed) return;

    setLoading(true);
    try {
      await apiService.post(`/api/${companyName}/sample-data/switch-to-real`);
      onSwitchToReal?.();
    } catch (error) {
      console.error('실제 데이터 전환 실패:', error);
      alert('전환 중 오류가 발생했습니다. 다시 시도해주세요.');
    } finally {
      setLoading(false);
    }
  };

  const industryNames = {
    'AUTOMOTIVE': '자동차 제조업',
    'ELECTRONICS': '전자/반도체 제조업',
    'FOOD_BEVERAGE': '식품/음료 제조업',
    'CHEMICAL_PHARMA': '화학/제약 제조업',
    'TEXTILE_APPAREL': '섬유/의류 제조업',
    'METAL_MACHINERY': '금속/기계 제조업'
  };

  const industryIcons = {
    'AUTOMOTIVE': '🚗',
    'ELECTRONICS': '💻',
    'FOOD_BEVERAGE': '🍽️',
    'CHEMICAL_PHARMA': '⚗️',
    'TEXTILE_APPAREL': '👕',
    'METAL_MACHINERY': '⚙️'
  };

  return (
    <div className="alert alert-info border-0 mb-3 position-sticky" 
         style={{ top: '0', zIndex: 1020, borderRadius: '0' }}>
      <div className="container-fluid">
        <div className="d-flex justify-content-between align-items-center">
          {/* 메인 표시 */}
          <div className="d-flex align-items-center">
            <div className="me-3">
              <span className="badge bg-success fs-6 px-3 py-2">
                <i className="fas fa-flask me-2"></i>
                체험 모드
              </span>
            </div>
            
            <div>
              <div className="d-flex align-items-center">
                <span className="h2 me-2">{industryIcons[industryCode]}</span>
                <div>
                  <strong>{industryNames[industryCode]} 맞춤 대시보드</strong>
                  <div className="small text-muted">
                    샘플 데이터로 MES를 미리 체험해보세요
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* 컨트롤 버튼들 */}
          <div className="d-flex align-items-center gap-2">
            <button 
              className="btn btn-outline-info btn-sm"
              onClick={() => setIsExpanded(!isExpanded)}
            >
              <i className={`fas fa-chevron-${isExpanded ? 'up' : 'down'} me-1`}></i>
              {isExpanded ? '접기' : '더보기'}
            </button>
            
            <button 
              className="btn btn-primary btn-sm"
              onClick={handleSwitchToReal}
              disabled={loading}
            >
              {loading ? (
                <span className="spinner-border spinner-border-sm me-1"></span>
              ) : (
                <i className="fas fa-plug me-1"></i>
              )}
              실제 데이터 연결
            </button>
          </div>
        </div>

        {/* 확장된 정보 */}
        {isExpanded && (
          <div className="mt-3 pt-3 border-top">
            <div className="row">
              <div className="col-md-8">
                <h6>🎯 현재 체험 중인 기능들:</h6>
                <div className="row">
                  <div className="col-sm-6">
                    <ul className="list-unstyled small">
                      <li><i className="fas fa-check text-success me-1"></i> 실시간 생산 모니터링</li>
                      <li><i className="fas fa-check text-success me-1"></i> 품질 관리 대시보드</li>
                      <li><i className="fas fa-check text-success me-1"></i> 재고 현황 추적</li>
                    </ul>
                  </div>
                  <div className="col-sm-6">
                    <ul className="list-unstyled small">
                      <li><i className="fas fa-check text-success me-1"></i> KPI 분석 차트</li>
                      <li><i className="fas fa-check text-success me-1"></i> 업종별 특화 지표</li>
                      <li><i className="fas fa-check text-success me-1"></i> 3D 디지털 트윈</li>
                    </ul>
                  </div>
                </div>
              </div>
              
              <div className="col-md-4">
                <div className="card border-success">
                  <div className="card-body p-3">
                    <h6 className="card-title mb-2">
                      <i className="fas fa-lightbulb text-warning me-1"></i>
                      체험 팁
                    </h6>
                    <ul className="list-unstyled small mb-0">
                      <li>• 차트를 클릭해서 상세 정보 확인</li>
                      <li>• 3D 화면에서 장비 상태 확인</li>
                      <li>• 알림을 클릭해서 대응 방법 학습</li>
                    </ul>
                  </div>
                </div>
              </div>
            </div>

            <div className="row mt-3">
              <div className="col-12">
                <div className="d-flex align-items-center justify-content-between p-2 bg-light rounded">
                  <div className="small">
                    <i className="fas fa-info-circle text-info me-1"></i>
                    <strong>알림:</strong> 모든 데이터는 샘플입니다. 실제 장비 연결 시 실시간 데이터로 전환됩니다.
                  </div>
                  <div className="d-flex gap-2">
                    <span className="badge bg-secondary">
                      <i className="fas fa-database me-1"></i>
                      샘플 데이터 활성
                    </span>
                    <span className="badge bg-warning text-dark">
                      <i className="fas fa-unlink me-1"></i>
                      장비 미연결
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

SampleModeIndicator.propTypes = {
  companyName: PropTypes.string.isRequired,
  industryCode: PropTypes.string.isRequired,
  onSwitchToReal: PropTypes.func
};

export default SampleModeIndicator;