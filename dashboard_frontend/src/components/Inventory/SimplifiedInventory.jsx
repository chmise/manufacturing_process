import React, { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import { SampleCard, SampleBadge } from '../ui/SampleBadge';
import KeyMaterialModal from './KeyMaterialModal';
import useApi from '../../hooks/useApi';
import { useCustomizationContext } from '../../hooks/useCustomization';

/**
 * 중소기업용 간소화된 재고 관리 컴포넌트
 * 복잡한 BOM 대신 핵심 원자재 3-5개만 간단하게 관리
 */
const SimplifiedInventory = ({ companyId, companyName, isSampleMode = false }) => {
  const { getIndustryTheme } = useCustomizationContext();
  const industry = getIndustryTheme();
  
  const [selectedMaterial, setSelectedMaterial] = useState(null);
  const [showAddModal, setShowAddModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [reorderAlerts, setReorderAlerts] = useState([]);

  // 핵심 원자재 데이터 조회 (최대 5개만)
  const { data: materials, loading, error, refetch } = useApi(
    `/api/${companyName}/inventory/key-materials`,
    {
      immediate: true,
      refreshInterval: 60000, // 1분마다 새로고침
      transform: (data) => {
        if (!Array.isArray(data)) return [];
        
        // 최대 5개까지만 관리
        const keyMaterials = data.slice(0, 5).map(material => ({
          ...material,
          urgencyLevel: getUrgencyLevel(material.currentStock, material.safetyStock),
          daysRemaining: calculateDaysRemaining(material.currentStock, material.dailyUsage || 0),
          reorderNeeded: material.currentStock <= material.safetyStock
        }));

        // 발주 알림 업데이트
        const alerts = keyMaterials
          .filter(m => m.reorderNeeded)
          .map(m => ({
            materialName: m.materialName,
            currentStock: m.currentStock,
            safetyStock: m.safetyStock,
            urgency: m.urgencyLevel
          }));
        
        setReorderAlerts(alerts);
        return keyMaterials;
      }
    }
  );

  const getUrgencyLevel = (current, safety) => {
    if (current <= 0) return 'critical';
    if (current <= safety * 0.5) return 'urgent';
    if (current <= safety) return 'warning';
    return 'normal';
  };

  const calculateDaysRemaining = (currentStock, dailyUsage) => {
    if (!dailyUsage || dailyUsage <= 0) return 999;
    return Math.floor(currentStock / dailyUsage);
  };

  const getUrgencyColor = (level) => {
    const colors = {
      critical: 'danger',
      urgent: 'warning',
      warning: 'info',
      normal: 'success'
    };
    return colors[level] || 'secondary';
  };

  const getUrgencyText = (level) => {
    const texts = {
      critical: '긴급',
      urgent: '부족',
      warning: '주의',
      normal: '충분'
    };
    return texts[level] || '알 수 없음';
  };

  const handleReorder = (material) => {
    // 간단한 발주 요청 처리
    const message = `${material.materialName}을(를) 발주하시겠습니까?\n\n현재 재고: ${material.currentStock}개\n안전 재고: ${material.safetyStock}개\n권장 발주량: ${material.safetyStock * 2}개`;
    
    if (window.confirm(message)) {
      // 실제로는 발주 API 호출
      console.log(`발주 요청: ${material.materialName}`);
      alert(`${material.materialName} 발주 요청이 접수되었습니다.`);
    }
  };

  const handleEditMaterial = (material) => {
    setSelectedMaterial(material);
    setShowEditModal(true);
  };

  const handleModalSave = () => {
    refetch(); // 데이터 새로고침
    setShowAddModal(false);
    setShowEditModal(false);
    setSelectedMaterial(null);
  };

  if (loading) {
    return (
      <div className="card">
        <div className="card-body text-center py-5">
          <div className="spinner-border text-primary" role="status">
            <span className="visually-hidden">로딩중...</span>
          </div>
          <p className="mt-3 text-muted">핵심 원자재 정보를 불러오는 중...</p>
        </div>
      </div>
    );
  }

  const cardContent = (
    <div className="card">
      <div className="card-header d-flex justify-content-between align-items-center">
        <div>
          <h5 className="card-title mb-0">
            <i className="fas fa-boxes me-2 text-primary"></i>
            핵심 원자재 관리
          </h5>
          <small className="text-muted">주요 원자재 3-5개만 간단하게 관리하세요</small>
        </div>
        <div>
          {reorderAlerts.length > 0 && (
            <span className="badge bg-warning me-2">
              <i className="fas fa-exclamation-triangle me-1"></i>
              발주 필요 {reorderAlerts.length}개
            </span>
          )}
          <button 
            className="btn btn-sm btn-outline-primary"
            onClick={() => setShowAddModal(true)}
          >
            <i className="fas fa-plus me-1"></i>
            원자재 추가
          </button>
        </div>
      </div>

      <div className="card-body">
        {/* 발주 알림 */}
        {reorderAlerts.length > 0 && (
          <div className="alert alert-warning mb-4">
            <div className="d-flex align-items-center mb-2">
              <i className="fas fa-bell me-2"></i>
              <strong>발주 알림</strong>
            </div>
            <ul className="mb-0">
              {reorderAlerts.map((alert, index) => (
                <li key={index}>
                  <strong>{alert.materialName}</strong> - 
                  현재 {alert.currentStock}개 (안전재고: {alert.safetyStock}개)
                </li>
              ))}
            </ul>
          </div>
        )}

        {/* 원자재 카드 그리드 */}
        <div className="row">
          {materials && materials.length > 0 ? (
            materials.map((material, index) => (
              <div key={material.id || index} className="col-lg-4 col-md-6 mb-3">
                <div className={`card border-${getUrgencyColor(material.urgencyLevel)} h-100`}>
                  <div className="card-body">
                    <div className="d-flex justify-content-between align-items-start mb-3">
                      <h6 className="card-title mb-0">{material.materialName}</h6>
                      <span className={`badge bg-${getUrgencyColor(material.urgencyLevel)}`}>
                        {getUrgencyText(material.urgencyLevel)}
                      </span>
                    </div>

                    <div className="row text-center mb-3">
                      <div className="col-6">
                        <div className="h4 mb-0">{material.currentStock}</div>
                        <small className="text-muted">현재 재고</small>
                      </div>
                      <div className="col-6">
                        <div className="h4 mb-0">{material.safetyStock}</div>
                        <small className="text-muted">안전 재고</small>
                      </div>
                    </div>

                    {/* 진행률 바 */}
                    <div className="mb-3">
                      <div className="progress" style={{ height: '8px' }}>
                        <div 
                          className={`progress-bar bg-${getUrgencyColor(material.urgencyLevel)}`}
                          style={{ 
                            width: `${Math.max(5, Math.min(100, (material.currentStock / (material.safetyStock * 2)) * 100))}%` 
                          }}
                        />
                      </div>
                      <small className="text-muted">
                        {material.daysRemaining < 999 ? `약 ${material.daysRemaining}일 사용 가능` : '사용량 정보 없음'}
                      </small>
                    </div>

                    <div className="d-flex gap-2">
                      <button 
                        className="btn btn-sm btn-outline-primary flex-fill"
                        onClick={() => handleEditMaterial(material)}
                      >
                        <i className="fas fa-edit me-1"></i>
                        수정
                      </button>
                      {material.reorderNeeded && (
                        <button 
                          className="btn btn-sm btn-warning flex-fill"
                          onClick={() => handleReorder(material)}
                        >
                          <i className="fas fa-shopping-cart me-1"></i>
                          발주
                        </button>
                      )}
                    </div>

                    {/* 추가 정보 */}
                    <div className="mt-2">
                      <small className="text-muted">
                        <i className="fas fa-map-marker-alt me-1"></i>
                        {material.location || '위치 미정'}
                      </small>
                      {material.supplier && (
                        <div>
                          <small className="text-muted">
                            <i className="fas fa-truck me-1"></i>
                            {material.supplier}
                          </small>
                        </div>
                      )}
                    </div>
                  </div>
                </div>
              </div>
            ))
          ) : (
            <div className="col-12">
              <div className="text-center py-5">
                <i className="fas fa-boxes fs-1 text-muted mb-3 d-block"></i>
                <h5 className="text-muted">아직 등록된 원자재가 없습니다</h5>
                <p className="text-muted">
                  제조에 꼭 필요한 핵심 원자재 3-5개를 등록해보세요
                </p>
                <button 
                  className="btn btn-primary"
                  onClick={() => setShowAddModal(true)}
                >
                  <i className="fas fa-plus me-2"></i>
                  첫 번째 원자재 등록하기
                </button>
              </div>
            </div>
          )}
        </div>

        {/* 도움말 */}
        <div className="alert alert-light mt-4">
          <div className="d-flex align-items-center">
            <i className="fas fa-lightbulb text-warning me-2"></i>
            <div>
              <strong>💡 간단한 재고 관리 팁</strong>
              <div className="small text-muted mt-1">
                • 핵심 원자재 3-5개만 선별해서 관리하면 효율적입니다<br/>
                • 안전재고는 보통 일주일치 사용량으로 설정하세요<br/>
                • 발주 알림이 뜨면 즉시 발주를 검토해보세요
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );

  // 샘플 모드인 경우 SampleCard로 래핑
  if (isSampleMode) {
    return (
      <SampleCard title="핵심 원자재 관리 (샘플 데이터)">
        {cardContent.props.children}
      </SampleCard>
    );
  }

  return (
    <>
      {cardContent}
      
      {/* 원자재 추가 모달 */}
      <KeyMaterialModal
        isOpen={showAddModal}
        onClose={() => setShowAddModal(false)}
        onSave={handleModalSave}
        companyName={companyName}
        mode="add"
      />
      
      {/* 원자재 수정 모달 */}
      <KeyMaterialModal
        isOpen={showEditModal}
        onClose={() => {
          setShowEditModal(false);
          setSelectedMaterial(null);
        }}
        onSave={handleModalSave}
        companyName={companyName}
        material={selectedMaterial}
        mode="edit"
      />
    </>
  );
};

SimplifiedInventory.propTypes = {
  companyId: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
  companyName: PropTypes.string.isRequired,
  isSampleMode: PropTypes.bool
};

export default SimplifiedInventory;