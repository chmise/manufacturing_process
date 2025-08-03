import React, { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import apiService from '../../service/apiService';

/**
 * 핵심 원자재 추가/수정 모달
 * 중소기업을 위한 간단한 폼
 */
const KeyMaterialModal = ({ 
  isOpen, 
  onClose, 
  onSave, 
  companyName, 
  material = null, 
  mode = 'add' // 'add' or 'edit'
}) => {
  const [formData, setFormData] = useState({
    materialName: '',
    currentStock: 0,
    safetyStock: 0,
    dailyUsage: 0,
    unit: '개',
    location: '',
    supplier: '',
    supplierContact: '',
    unitPrice: 0,
    description: ''
  });
  
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState({});

  // 수정 모드일 때 기존 데이터 로드
  useEffect(() => {
    if (material && mode === 'edit') {
      setFormData({
        materialName: material.materialName || '',
        currentStock: material.currentStock || 0,
        safetyStock: material.safetyStock || 0,
        dailyUsage: material.dailyUsage || 0,
        unit: material.unit || '개',
        location: material.location || '',
        supplier: material.supplier || '',
        supplierContact: material.supplierContact || '',
        unitPrice: material.unitPrice || 0,
        description: material.description || ''
      });
    } else {
      // 추가 모드일 때 초기화
      setFormData({
        materialName: '',
        currentStock: 0,
        safetyStock: 0,
        dailyUsage: 0,
        unit: '개',
        location: '',
        supplier: '',
        supplierContact: '',
        unitPrice: 0,
        description: ''
      });
    }
    setErrors({});
  }, [material, mode, isOpen]);

  const handleInputChange = (e) => {
    const { name, value, type } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'number' ? (value === '' ? 0 : Number(value)) : value
    }));
    
    // 에러 제거
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: null }));
    }
  };

  const validateForm = () => {
    const newErrors = {};
    
    if (!formData.materialName.trim()) {
      newErrors.materialName = '원자재명을 입력해주세요';
    }
    
    if (formData.currentStock < 0) {
      newErrors.currentStock = '현재 재고는 0 이상이어야 합니다';
    }
    
    if (formData.safetyStock < 0) {
      newErrors.safetyStock = '안전 재고는 0 이상이어야 합니다';
    }
    
    if (formData.dailyUsage < 0) {
      newErrors.dailyUsage = '일일 사용량은 0 이상이어야 합니다';
    }
    
    if (formData.unitPrice < 0) {
      newErrors.unitPrice = '단가는 0 이상이어야 합니다';
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }
    
    setLoading(true);
    try {
      let result;
      if (mode === 'edit' && material) {
        result = await apiService.put(
          `/api/${companyName}/inventory/key-materials/${material.id}`,
          formData
        );
      } else {
        result = await apiService.post(
          `/api/${companyName}/inventory/key-materials`,
          formData
        );
      }
      
      onSave?.(result.data);
      onClose();
    } catch (error) {
      console.error('원자재 저장 실패:', error);
      
      if (error.response?.status === 400) {
        alert('핵심 원자재는 최대 5개까지만 등록할 수 있습니다.');
      } else {
        alert('저장 중 오류가 발생했습니다. 다시 시도해주세요.');
      }
    } finally {
      setLoading(false);
    }
  };

  const commonUnits = ['개', 'kg', 'L', 'm', 'm²', 'box', 'pcs', 't'];

  if (!isOpen) return null;

  return (
    <div className="modal fade show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
      <div className="modal-dialog modal-lg">
        <div className="modal-content">
          <div className="modal-header">
            <h5 className="modal-title">
              <i className="fas fa-boxes me-2 text-primary"></i>
              {mode === 'edit' ? '원자재 수정' : '원자재 추가'}
            </h5>
            <button 
              type="button" 
              className="btn-close" 
              onClick={onClose}
              disabled={loading}
            ></button>
          </div>

          <form onSubmit={handleSubmit}>
            <div className="modal-body">
              <div className="row">
                {/* 기본 정보 */}
                <div className="col-12 mb-4">
                  <h6 className="border-bottom pb-2 mb-3">
                    <i className="fas fa-info-circle me-1"></i>
                    기본 정보
                  </h6>
                  
                  <div className="row">
                    <div className="col-md-8 mb-3">
                      <label className="form-label">
                        원자재명 <span className="text-danger">*</span>
                      </label>
                      <input
                        type="text"
                        className={`form-control ${errors.materialName ? 'is-invalid' : ''}`}
                        name="materialName"
                        value={formData.materialName}
                        onChange={handleInputChange}
                        placeholder="예: 철판, 플라스틱 원료, 포장재 등"
                        required
                      />
                      {errors.materialName && (
                        <div className="invalid-feedback">{errors.materialName}</div>
                      )}
                    </div>
                    
                    <div className="col-md-4 mb-3">
                      <label className="form-label">단위</label>
                      <select
                        className="form-select"
                        name="unit"
                        value={formData.unit}
                        onChange={handleInputChange}
                      >
                        {commonUnits.map(unit => (
                          <option key={unit} value={unit}>{unit}</option>
                        ))}
                      </select>
                    </div>
                  </div>
                </div>

                {/* 재고 정보 */}
                <div className="col-12 mb-4">
                  <h6 className="border-bottom pb-2 mb-3">
                    <i className="fas fa-warehouse me-1"></i>
                    재고 정보
                  </h6>
                  
                  <div className="row">
                    <div className="col-md-4 mb-3">
                      <label className="form-label">
                        현재 재고 <span className="text-danger">*</span>
                      </label>
                      <input
                        type="number"
                        className={`form-control ${errors.currentStock ? 'is-invalid' : ''}`}
                        name="currentStock"
                        value={formData.currentStock}
                        onChange={handleInputChange}
                        min="0"
                        required
                      />
                      {errors.currentStock && (
                        <div className="invalid-feedback">{errors.currentStock}</div>
                      )}
                    </div>
                    
                    <div className="col-md-4 mb-3">
                      <label className="form-label">
                        안전 재고 <span className="text-danger">*</span>
                      </label>
                      <input
                        type="number"
                        className={`form-control ${errors.safetyStock ? 'is-invalid' : ''}`}
                        name="safetyStock"
                        value={formData.safetyStock}
                        onChange={handleInputChange}
                        min="0"
                        required
                      />
                      {errors.safetyStock && (
                        <div className="invalid-feedback">{errors.safetyStock}</div>
                      )}
                      <small className="text-muted">최소 보유해야 할 수량</small>
                    </div>
                    
                    <div className="col-md-4 mb-3">
                      <label className="form-label">일일 사용량</label>
                      <input
                        type="number"
                        className={`form-control ${errors.dailyUsage ? 'is-invalid' : ''}`}
                        name="dailyUsage"
                        value={formData.dailyUsage}
                        onChange={handleInputChange}
                        min="0"
                        step="0.1"
                      />
                      {errors.dailyUsage && (
                        <div className="invalid-feedback">{errors.dailyUsage}</div>
                      )}
                      <small className="text-muted">예상 소진일 계산용</small>
                    </div>
                  </div>
                </div>

                {/* 공급업체 정보 */}
                <div className="col-12 mb-4">
                  <h6 className="border-bottom pb-2 mb-3">
                    <i className="fas fa-truck me-1"></i>
                    공급업체 정보
                  </h6>
                  
                  <div className="row">
                    <div className="col-md-4 mb-3">
                      <label className="form-label">공급업체명</label>
                      <input
                        type="text"
                        className="form-control"
                        name="supplier"
                        value={formData.supplier}
                        onChange={handleInputChange}
                        placeholder="예: (주)재료상사"
                      />
                    </div>
                    
                    <div className="col-md-4 mb-3">
                      <label className="form-label">연락처</label>
                      <input
                        type="text"
                        className="form-control"
                        name="supplierContact"
                        value={formData.supplierContact}
                        onChange={handleInputChange}
                        placeholder="예: 02-1234-5678"
                      />
                    </div>
                    
                    <div className="col-md-4 mb-3">
                      <label className="form-label">단가 (원)</label>
                      <input
                        type="number"
                        className={`form-control ${errors.unitPrice ? 'is-invalid' : ''}`}
                        name="unitPrice"
                        value={formData.unitPrice}
                        onChange={handleInputChange}
                        min="0"
                        placeholder="0"
                      />
                      {errors.unitPrice && (
                        <div className="invalid-feedback">{errors.unitPrice}</div>
                      )}
                    </div>
                  </div>
                </div>

                {/* 기타 정보 */}
                <div className="col-12 mb-3">
                  <h6 className="border-bottom pb-2 mb-3">
                    <i className="fas fa-map-marker-alt me-1"></i>
                    기타 정보
                  </h6>
                  
                  <div className="row">
                    <div className="col-md-6 mb-3">
                      <label className="form-label">보관 위치</label>
                      <input
                        type="text"
                        className="form-control"
                        name="location"
                        value={formData.location}
                        onChange={handleInputChange}
                        placeholder="예: A창고 1번 랙"
                      />
                    </div>
                    
                    <div className="col-md-6 mb-3">
                      <label className="form-label">메모</label>
                      <textarea
                        className="form-control"
                        name="description"
                        value={formData.description}
                        onChange={handleInputChange}
                        rows="2"
                        placeholder="원자재에 대한 추가 정보를 입력하세요"
                      />
                    </div>
                  </div>
                </div>

                {/* 도움말 */}
                <div className="col-12">
                  <div className="alert alert-light">
                    <div className="d-flex align-items-center">
                      <i className="fas fa-lightbulb text-warning me-2"></i>
                      <div>
                        <strong>💡 등록 팁</strong>
                        <div className="small text-muted mt-1">
                          • 안전 재고는 보통 일주일치 사용량으로 설정하세요<br/>
                          • 일일 사용량을 입력하면 예상 소진일을 자동 계산해드립니다<br/>
                          • 핵심 원자재는 최대 5개까지만 등록 가능합니다
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div className="modal-footer">
              <button 
                type="button" 
                className="btn btn-secondary" 
                onClick={onClose}
                disabled={loading}
              >
                취소
              </button>
              <button 
                type="submit" 
                className="btn btn-primary"
                disabled={loading}
              >
                {loading ? (
                  <>
                    <span className="spinner-border spinner-border-sm me-2" role="status"></span>
                    저장 중...
                  </>
                ) : (
                  <>
                    <i className="fas fa-save me-2"></i>
                    저장
                  </>
                )}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

KeyMaterialModal.propTypes = {
  isOpen: PropTypes.bool.isRequired,
  onClose: PropTypes.func.isRequired,
  onSave: PropTypes.func,
  companyName: PropTypes.string.isRequired,
  material: PropTypes.object,
  mode: PropTypes.oneOf(['add', 'edit'])
};

export default KeyMaterialModal;