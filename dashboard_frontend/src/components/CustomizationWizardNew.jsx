import React, { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import TablerCard from './ui/TablerCard';
import TablerButton from './ui/TablerButton';
import useCustomization from '../hooks/useCustomization';

const CustomizationWizardNew = ({ companyId, onComplete }) => {
  const [currentStep, setCurrentStep] = useState(1);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const {
    industries,
    submitStep1,
    submitStep2,
    submitStep3,
    completeSetup,
    setupCompleted
  } = useCustomization(companyId);

  const [formData, setFormData] = useState({
    step1: {
      industryTypeId: null,
      companySize: '',
      productionType: ''
    },
    step2: {
      workingHours: '',
      employeeCount: '',
      dailyProductionCapacity: '',
      automationLevel: 50,
      qualityMethods: [],
      specialRequirements: ''
    },
    step3: {
      selectedKpis: [],
      dashboardSettings: {},
      alertSettings: {}
    }
  });

  useEffect(() => {
    if (setupCompleted) {
      onComplete?.();
    }
  }, [setupCompleted, onComplete]);

  const updateFormData = (step, field, value) => {
    setFormData(prev => ({
      ...prev,
      [step]: {
        ...prev[step],
        [field]: value
      }
    }));
  };

  // 업종별 맞춤 질문 시스템
  const getIndustrySpecificQuestions = (industryCode) => {
    const industryQuestions = {
      'AUTOMOTIVE': {
        concerns: [
          { 
            key: '품질정밀도', 
            title: '품질이 정밀하게 나왔는지?', 
            desc: '치수 정확도와 품질 편차 모니터링', 
            icon: '🎯',
            examples: '• 치수 정밀도 • 토크 값 관리 • 조립 품질'
          },
          { 
            key: '사이클타임', 
            title: '생산 속도가 목표에 맞는지?', 
            desc: '사이클 타임과 생산 효율 추적', 
            icon: '⏱️',
            examples: '• 사이클 타임 • 라인 속도 • 병목 구간'
          },
          { 
            key: '불량추적', 
            title: '불량이 어디서 발생하는지?', 
            desc: '불량 원인과 발생 지점 분석', 
            icon: '🔍',
            examples: '• 불량 원인 분석 • 공정별 불량률 • 재작업률'
          }
        ],
        qualityMethods: [
          { value: 'dimensional', title: '🎯 치수 검사', desc: '정밀 치수 측정' },
          { value: 'torque', title: '🔧 토크 검사', desc: '볼트 토크 값 확인' },
          { value: 'function', title: '⚙️ 기능 검사', desc: '부품 작동 테스트' },
          { value: 'visual', title: '👁️ 외관 검사', desc: '표면 결함 확인' }
        ]
      },
      'ELECTRONICS': {
        concerns: [
          { 
            key: '수율관리', 
            title: '수율이 목표치에 맞는지?', 
            desc: '생산 수율과 품질 수준 모니터링', 
            icon: '📈',
            examples: '• 웨이퍼 수율 • 테스트 통과율 • 수율 트렌드'
          },
          { 
            key: '불량분석', 
            title: '어떤 불량이 많이 나오는지?', 
            desc: '불량 유형별 발생 현황 분석', 
            icon: '🔬',
            examples: '• 불량 유형 분류 • 공정별 수율 • 불량 패턴'
          },
          { 
            key: '설비상태', 
            title: '장비가 정상 작동하는지?', 
            desc: '정밀 장비의 상태와 성능 추적', 
            icon: '🎛️',
            examples: '• 장비 온도 • 진동 수준 • 교정 상태'
          }
        ],
        qualityMethods: [
          { value: 'electrical', title: '⚡ 전기 검사', desc: '전기적 특성 테스트' },
          { value: 'functional', title: '🔧 기능 검사', desc: '동작 기능 확인' },
          { value: 'automated', title: '🤖 자동 검사', desc: '자동화 테스트 장비' },
          { value: 'burn_in', title: '🔥 번인 검사', desc: '내구성 테스트' }
        ]
      },
      'FOOD_BEVERAGE': {
        concerns: [
          { 
            key: '위생관리', 
            title: '위생 상태가 안전한지?', 
            desc: '식품 안전과 위생 기준 모니터링', 
            icon: '🧼',
            examples: '• 세균 수 검사 • 온도 관리 • 청결도 점검'
          },
          { 
            key: '유통기한', 
            title: '제품이 신선한지?', 
            desc: '제품 신선도와 유통기한 관리', 
            icon: '📅',
            examples: '• 유통기한 추적 • 보관 온도 • 품질 변화'
          },
          { 
            key: '온도모니터링', 
            title: '온도가 적정한지?', 
            desc: '냉장/냉동/가열 온도 실시간 추적', 
            icon: '🌡️',
            examples: '• 냉장고 온도 • 조리 온도 • 온도 이력'
          }
        ],
        qualityMethods: [
          { value: 'haccp', title: '🛡️ HACCP 기준', desc: 'HACCP 안전 관리' },
          { value: 'temperature', title: '🌡️ 온도 관리', desc: '온도 모니터링' },
          { value: 'microbial', title: '🔬 미생물 검사', desc: '세균 검사' },
          { value: 'sensory', title: '👃 관능 검사', desc: '맛, 냄새, 색상' }
        ]
      },
      'CHEMICAL_PHARMA': {
        concerns: [
          { 
            key: '안전관리', 
            title: '작업장이 안전한지?', 
            desc: '화학물질 안전과 작업자 보호', 
            icon: '⚠️',
            examples: '• 가스 농도 • 안전 장비 • 누출 감지'
          },
          { 
            key: '배치관리', 
            title: '배치별 품질이 일정한지?', 
            desc: '배치 단위 품질 관리와 추적', 
            icon: '🧪',
            examples: '• 배치 품질 • 성분 분석 • 공정 변수'
          },
          { 
            key: '환경모니터링', 
            title: '온도/압력이 적정한지?', 
            desc: '공정 환경 조건 실시간 모니터링', 
            icon: '📊',
            examples: '• 반응 온도 • 압력 수준 • pH 관리'
          }
        ],
        qualityMethods: [
          { value: 'analytical', title: '🧪 성분 분석', desc: '화학 성분 검사' },
          { value: 'safety', title: '⚠️ 안전 검사', desc: '안전성 평가' },
          { value: 'environmental', title: '🌡️ 환경 모니터링', desc: '온도/압력/pH' },
          { value: 'gmp', title: '🏭 GMP 기준', desc: 'GMP 품질 관리' }
        ]
      },
      'TEXTILE_APPAREL': {
        concerns: [
          { 
            key: '색상관리', 
            title: '색상이 균일하게 나오는지?', 
            desc: '염색과 색상 품질 모니터링', 
            icon: '🎨',
            examples: '• 색상 편차 • 염색 균일성 • 색상 매칭'
          },
          { 
            key: '텍스처품질', 
            title: '원단 품질이 좋은지?', 
            desc: '원단의 질감과 물성 관리', 
            icon: '🧵',
            examples: '• 원단 강도 • 신축성 • 표면 품질'
          },
          { 
            key: '계절대응', 
            title: '계절 주문에 맞춰 생산되는지?', 
            desc: '계절별 수요와 생산 계획 관리', 
            icon: '🗓️',
            examples: '• 계절 트렌드 • 주문 현황 • 재고 수준'
          }
        ],
        qualityMethods: [
          { value: 'color', title: '🎨 색상 검사', desc: '색상 정확도 측정' },
          { value: 'fabric', title: '🧵 원단 검사', desc: '원단 품질 테스트' },
          { value: 'dimensional', title: '📏 치수 검사', desc: '의류 치수 확인' },
          { value: 'durability', title: '💪 내구성 검사', desc: '세탁/마모 테스트' }
        ]
      },
      'METAL_MACHINERY': {
        concerns: [
          { 
            key: '정밀가공', 
            title: '가공 정밀도가 맞는지?', 
            desc: '가공 치수와 정밀도 모니터링', 
            icon: '📐',
            examples: '• 치수 정확도 • 표면 거칠기 • 공차 관리'
          },
          { 
            key: '재료강도', 
            title: '재료 강도가 충분한지?', 
            desc: '금속 재료의 강도와 내구성 확인', 
            icon: '💪',
            examples: '• 인장 강도 • 경도 측정 • 피로 수명'
          },
          { 
            key: '장비수명', 
            title: '가공 장비가 잘 관리되는지?', 
            desc: '절삭 도구와 장비 상태 관리', 
            icon: '🔧',
            examples: '• 도구 마모 • 장비 진동 • 정비 주기'
          }
        ],
        qualityMethods: [
          { value: 'dimensional', title: '📏 치수 검사', desc: '정밀 치수 측정' },
          { value: 'hardness', title: '💎 경도 검사', desc: '재료 경도 측정' },
          { value: 'surface', title: '🔍 표면 검사', desc: '표면 거칠기 검사' },
          { value: 'nondestructive', title: '🎯 비파괴 검사', desc: '초음파/자기탐상' }
        ]
      }
    };

    return industryQuestions[industryCode] || industryQuestions['AUTOMOTIVE']; // 기본값
  };

  // 선택된 업종 코드 가져오기
  const getSelectedIndustryCode = () => {
    const selectedIndustry = industries?.find(
      industry => industry.industryTypeId.toString() === formData.step1.industryTypeId
    );
    return selectedIndustry?.industryCode || null;
  };

  const handleStep1Submit = async () => {
    if (!formData.step1.industryTypeId || !formData.step1.companySize || !formData.step1.productionType) {
      setError('모든 필드를 입력해주세요.');
      return;
    }

    setLoading(true);
    setError('');

    try {
      await submitStep1(formData.step1);
      setCurrentStep(2);
    } catch (error) {
      setError(error.response?.data?.error || '설정 저장에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleStep2Submit = async () => {
    setLoading(true);
    setError('');

    try {
      await submitStep2(formData.step2);
      setCurrentStep(3);
    } catch (error) {
      setError(error.response?.data?.error || '설정 저장에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleStep3Submit = async () => {
    setLoading(true);
    setError('');

    try {
      await submitStep3(formData.step3);
      setCurrentStep(4);
    } catch (error) {
      setError(error.response?.data?.error || '설정 저장에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleComplete = async () => {
    setLoading(true);
    setError('');

    try {
      await completeSetup();
      onComplete?.();
    } catch (error) {
      setError(error.response?.data?.error || '설정 완료에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const renderStepIndicator = () => (
    <div className="steps steps-counter steps-lime mb-4">
      <a href="#" className={`step-item ${currentStep >= 1 ? 'active' : ''}`}>
        <span className="step-counter">1</span>
        <span className="step-name">기본 정보</span>
      </a>
      <a href="#" className={`step-item ${currentStep >= 2 ? 'active' : ''}`}>
        <span className="step-counter">2</span>
        <span className="step-name">생산 환경</span>
      </a>
      <a href="#" className={`step-item ${currentStep >= 3 ? 'active' : ''}`}>
        <span className="step-counter">3</span>
        <span className="step-name">KPI 설정</span>
      </a>
      <a href="#" className={`step-item ${currentStep >= 4 ? 'active' : ''}`}>
        <span className="step-counter">4</span>
        <span className="step-name">완료</span>
      </a>
    </div>
  );

  const renderStep1 = () => (
    <TablerCard
      title="1단계: 기본 정보 입력"
      subtitle="귀하의 제조업 분야와 회사 규모를 선택해주세요"
    >
      <div className="row">
        <div className="col-md-6 mb-3">
          <label className="form-label required">제조업 분야</label>
          <select 
            className="form-select"
            value={formData.step1.industryTypeId || ''}
            onChange={(e) => updateFormData('step1', 'industryTypeId', e.target.value)}
          >
            <option value="">선택해주세요</option>
            {industries?.map(industry => (
              <option key={industry.industryTypeId} value={industry.industryTypeId}>
                {industry.iconName} {industry.industryName}
              </option>
            ))}
          </select>
          <div className="form-hint">
            업종에 따라 맞춤형 KPI와 모니터링 항목이 제공됩니다.
          </div>
        </div>

        <div className="col-md-6 mb-3">
          <label className="form-label required">회사 규모</label>
          <select 
            className="form-select"
            value={formData.step1.companySize}
            onChange={(e) => updateFormData('step1', 'companySize', e.target.value)}
          >
            <option value="">선택해주세요</option>
            <option value="STARTUP">스타트업 (직원 10명 미만)</option>
            <option value="SMALL">중소기업 (직원 10-50명)</option>
            <option value="MEDIUM">중견기업 (직원 50-300명)</option>
            <option value="LARGE">대기업 (직원 300명 이상)</option>
          </select>
        </div>

        <div className="col-12 mb-3">
          <label className="form-label required">생산 방식</label>
          <div className="form-selectgroup form-selectgroup-boxes d-flex flex-column">
            {[
              { value: 'MASS_PRODUCTION', title: '대량생산', desc: '동일 제품을 대량으로 연속 생산' },
              { value: 'VARIETY_SMALL_LOT', title: '다품종 소량생산', desc: '다양한 제품을 소량씩 생산' },
              { value: 'CUSTOM_ORDER', title: '주문생산', desc: '고객 주문에 따른 맞춤 생산' },
              { value: 'BATCH_PRODUCTION', title: '배치생산', desc: '일정한 배치 단위로 생산' }
            ].map(type => (
              <label key={type.value} className="form-selectgroup-item flex-fill">
                <input
                  type="radio"
                  name="productionType"
                  value={type.value}
                  className="form-selectgroup-input"
                  checked={formData.step1.productionType === type.value}
                  onChange={(e) => updateFormData('step1', 'productionType', e.target.value)}
                />
                <div className="form-selectgroup-label d-flex align-items-center p-3">
                  <div className="me-3">
                    <span className="form-selectgroup-check"></span>
                  </div>
                  <div>
                    <strong>{type.title}</strong>
                    <div className="text-muted">{type.desc}</div>
                  </div>
                </div>
              </label>
            ))}
          </div>
        </div>
      </div>

      <div className="card-footer">
        <div className="d-flex">
          <TablerButton
            variant="primary"
            onClick={handleStep1Submit}
            loading={loading}
            className="ms-auto"
          >
            다음 단계
          </TablerButton>
        </div>
      </div>
    </TablerCard>
  );

  const renderStep2 = () => (
    <TablerCard
      title="2단계: 우리 공장은 어떻게 운영되나요?"
      subtitle="간단한 질문 몇 개로 맞춤 설정을 해드릴게요"
    >
      <div className="row">
        <div className="col-md-6 mb-4">
          <label className="form-label required">🕐 하루에 몇 시간 정도 가동하세요?</label>
          <div className="form-selectgroup form-selectgroup-boxes d-flex flex-column">
            {[
              { value: '8', title: '8시간', desc: '일반 근무 시간 (1교대)', capacity: 'low' },
              { value: '12', title: '12시간', desc: '연장 근무 포함', capacity: 'medium' },
              { value: '16', title: '16시간', desc: '2교대 운영', capacity: 'high' },
              { value: '24', title: '24시간', desc: '3교대/연속 운영', capacity: 'very_high' }
            ].map(option => (
              <label key={option.value} className="form-selectgroup-item">
                <input
                  type="radio"
                  name="workingHours"
                  value={option.value}
                  className="form-selectgroup-input"
                  checked={formData.step2.workingHours === option.value}
                  onChange={(e) => {
                    updateFormData('step2', 'workingHours', e.target.value);
                    updateFormData('step2', 'dailyProductionCapacity', option.capacity);
                  }}
                />
                <div className="form-selectgroup-label d-flex align-items-center p-2">
                  <div className="me-3">
                    <span className="form-selectgroup-check"></span>
                  </div>
                  <div>
                    <strong>{option.title}</strong>
                    <div className="text-muted small">{option.desc}</div>
                  </div>
                </div>
              </label>
            ))}
          </div>
        </div>

        <div className="col-md-6 mb-4">
          <label className="form-label required">👥 직원이 몇 명 정도 근무하나요?</label>
          <div className="form-selectgroup form-selectgroup-boxes d-flex flex-column">
            {[
              { value: 'small', title: '5명 미만', desc: '소규모 가족 기업' },
              { value: 'medium', title: '5-20명', desc: '중소기업 규모' },
              { value: 'large', title: '20-50명', desc: '중견기업 규모' },
              { value: 'very_large', title: '50명 이상', desc: '대기업 규모' }
            ].map(option => (
              <label key={option.value} className="form-selectgroup-item">
                <input
                  type="radio"
                  name="employeeCount"
                  value={option.value}
                  className="form-selectgroup-input"
                  checked={formData.step2.employeeCount === option.value}
                  onChange={(e) => updateFormData('step2', 'employeeCount', e.target.value)}
                />
                <div className="form-selectgroup-label d-flex align-items-center p-2">
                  <div className="me-3">
                    <span className="form-selectgroup-check"></span>
                  </div>
                  <div>
                    <strong>{option.title}</strong>
                    <div className="text-muted small">{option.desc}</div>
                  </div>
                </div>
              </label>
            ))}
          </div>
        </div>

        <div className="col-12 mb-4">
          <label className="form-label required">🤖 생산 과정에서 사람이 직접 하는 일이 얼마나 되나요?</label>
          <div className="form-selectgroup form-selectgroup-boxes d-flex flex-column">
            {[
              { value: '20', title: '대부분 수작업', desc: '사람이 직접 만드는 일이 많아요', level: '수작업 중심' },
              { value: '50', title: '반반 정도', desc: '기계와 사람이 함께 작업해요', level: '반자동화' },
              { value: '80', title: '대부분 자동화', desc: '기계가 대부분 하고 사람은 관리만', level: '자동화 중심' },
              { value: '95', title: '완전 자동화', desc: '기계가 거의 모든 것을 처리해요', level: '완전 자동' }
            ].map(option => (
              <label key={option.value} className="form-selectgroup-item">
                <input
                  type="radio"
                  name="automationLevel"
                  value={option.value}
                  className="form-selectgroup-input"
                  checked={formData.step2.automationLevel.toString() === option.value}
                  onChange={(e) => updateFormData('step2', 'automationLevel', parseInt(e.target.value))}
                />
                <div className="form-selectgroup-label d-flex align-items-center p-3">
                  <div className="me-3">
                    <span className="form-selectgroup-check"></span>
                  </div>
                  <div>
                    <strong>{option.title}</strong>
                    <div className="text-muted">{option.desc}</div>
                    <div className="badge bg-blue-lt mt-1">{option.level}</div>
                  </div>
                </div>
              </label>
            ))}
          </div>
        </div>

        <div className="col-12 mb-3">
          <label className="form-label">🏆 품질 관리는 어떻게 하고 계세요?</label>
          <div className="form-selectgroup form-selectgroup-pills">
            {(() => {
              const industryCode = getSelectedIndustryCode();
              const industryQuestions = getIndustrySpecificQuestions(industryCode);
              return industryQuestions.qualityMethods;
            })().map(option => (
              <label key={option.value} className="form-selectgroup-item">
                <input
                  type="checkbox"
                  name="qualityMethods"
                  value={option.value}
                  className="form-selectgroup-input"
                  checked={formData.step2.qualityMethods?.includes(option.value) || false}
                  onChange={(e) => {
                    const current = formData.step2.qualityMethods || [];
                    if (e.target.checked) {
                      updateFormData('step2', 'qualityMethods', [...current, option.value]);
                    } else {
                      updateFormData('step2', 'qualityMethods', current.filter(m => m !== option.value));
                    }
                  }}
                />
                <div className="form-selectgroup-label">
                  <div>
                    <strong>{option.title}</strong>
                    <div className="text-muted small">{option.desc}</div>
                  </div>
                </div>
              </label>
            ))}
          </div>
        </div>
      </div>

      <div className="card-footer">
        <div className="d-flex">
          <TablerButton
            variant="secondary"
            onClick={() => setCurrentStep(1)}
          >
            이전
          </TablerButton>
          <TablerButton
            variant="primary"
            onClick={handleStep2Submit}
            loading={loading}
            className="ms-auto"
          >
            다음 단계
          </TablerButton>
        </div>
      </div>
    </TablerCard>
  );

  const renderStep3 = () => (
    <TablerCard
      title="3단계: 가장 궁금한 것이 무엇인가요?"
      subtitle="매일 체크하고 싶은 것들을 선택해주세요 (여러 개 선택 가능)"
    >
      <div className="row">
        <div className="col-12 mb-4">
          {(() => {
            const industryCode = getSelectedIndustryCode();
            const selectedIndustry = industries?.find(
              industry => industry.industryTypeId.toString() === formData.step1.industryTypeId
            );
            
            if (industryCode && selectedIndustry) {
              return (
                <div className="alert alert-info mb-3">
                  <div className="d-flex align-items-center">
                    <span className="h3 me-3">{selectedIndustry.iconName}</span>
                    <div>
                      <strong>{selectedIndustry.industryName}</strong>에 특화된 모니터링 항목들입니다.
                      <div className="text-muted small">업종 특성에 맞는 맞춤형 대시보드를 제공합니다.</div>
                    </div>
                  </div>
                </div>
              );
            }
            return null;
          })()}
          
          <div className="row">
            {(() => {
              const industryCode = getSelectedIndustryCode();
              const industryQuestions = getIndustrySpecificQuestions(industryCode);
              
              // 업종별 특화 질문에 기본 질문들 추가
              const combinedConcerns = [
                ...industryQuestions.concerns,
                { 
                  key: '생산량', 
                  title: '오늘 얼마나 만들었지?', 
                  desc: '실시간 생산 수량과 목표 달성률을 확인', 
                  icon: '📊',
                  examples: '• 시간별 생산량 • 목표 대비 달성률 • 예상 완료 시간'
                },
                { 
                  key: '비용', 
                  title: '전기료는 얼마나 나올까?', 
                  desc: '전력 소비와 운영 비용 추적', 
                  icon: '💰',
                  examples: '• 전력 사용량 • 시간당 비용 • 절약 현황'
                },
                { 
                  key: '계획', 
                  title: '내일 일정은 어떻게 되지?', 
                  desc: '생산 계획과 일정 관리', 
                  icon: '📅',
                  examples: '• 주간 계획 • 긴급 주문 • 납기 현황'
                }
              ];
              
              return combinedConcerns;
            })().map(concern => (
              <div key={concern.key} className="col-lg-6 mb-4">
                <label className="form-check card-check">
                  <input 
                    className="form-check-input"
                    type="checkbox"
                    checked={formData.step3.selectedKpis.includes(concern.key)}
                    onChange={(e) => {
                      const currentKpis = formData.step3.selectedKpis;
                      if (e.target.checked) {
                        updateFormData('step3', 'selectedKpis', [...currentKpis, concern.key]);
                      } else {
                        updateFormData('step3', 'selectedKpis', currentKpis.filter(k => k !== concern.key));
                      }
                    }}
                  />
                  <div className="form-check-label">
                    <div className="card card-sm">
                      <div className="card-body">
                        <div className="d-flex align-items-center">
                          <span className="h1 me-3">{concern.icon}</span>
                          <div className="flex-fill">
                            <div className="font-weight-medium">{concern.title}</div>
                            <div className="text-muted small mb-2">{concern.desc}</div>
                            <div className="text-muted" style={{fontSize: '0.75rem'}}>
                              {concern.examples}
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </label>
              </div>
            ))}
          </div>
          <div className="alert alert-info mt-3">
            <div className="d-flex">
              <div>
                <svg className="icon alert-icon" width="24" height="24" viewBox="0 0 24 24" strokeWidth="2" stroke="currentColor" fill="none" strokeLinecap="round" strokeLinejoin="round">
                  <path stroke="none" d="M0 0h24v24H0z" fill="none"/>
                  <circle cx="12" cy="12" r="9"/>
                  <line x1="12" y1="8" x2="12" y2="12"/>
                  <line x1="12" y1="16" x2="12.01" y2="16"/>
                </svg>
              </div>
              <div>
                <strong>💡 추천:</strong> 처음에는 2-3개만 선택하시고, 나중에 언제든 추가하실 수 있습니다!
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="card-footer">
        <div className="d-flex">
          <TablerButton
            variant="secondary"
            onClick={() => setCurrentStep(2)}
          >
            이전
          </TablerButton>
          <TablerButton
            variant="primary"
            onClick={handleStep3Submit}
            loading={loading}
            className="ms-auto"
          >
            다음 단계
          </TablerButton>
        </div>
      </div>
    </TablerCard>
  );

  const renderStep4 = () => (
    <TablerCard className="text-center">
      <div className="mb-4">
        <div className="mb-3">
          <svg className="icon icon-tabler icon-xl text-success" width="48" height="48" viewBox="0 0 24 24" strokeWidth="1" stroke="currentColor" fill="none" strokeLinecap="round" strokeLinejoin="round">
            <path stroke="none" d="M0 0h24v24H0z" fill="none"/>
            <circle cx="12" cy="12" r="9"/>
            <path d="M9 12l2 2 4 -4"/>
          </svg>
        </div>
        <h2>설정 완료!</h2>
        <p className="text-muted">
          모든 설정이 완료되었습니다. 이제 귀하의 제조업 특성에 맞게 
          커스터마이징된 대시보드를 사용할 수 있습니다.
        </p>
      </div>

      <div className="row justify-content-center mb-4">
        <div className="col-md-8">
          <div className="card">
            <div className="card-body">
              <h4>설정 요약</h4>
              <div className="row text-start">
                <div className="col-6">
                  <strong>업종:</strong><br />
                  <span className="text-muted">
                    {industries?.find(i => i.industryTypeId.toString() === formData.step1.industryTypeId)?.industryName}
                  </span>
                </div>
                <div className="col-6">
                  <strong>생산 방식:</strong><br />
                  <span className="text-muted">
                    {formData.step1.productionType === 'MASS_PRODUCTION' && '대량생산'}
                    {formData.step1.productionType === 'VARIETY_SMALL_LOT' && '다품종 소량생산'}
                    {formData.step1.productionType === 'CUSTOM_ORDER' && '주문생산'}
                    {formData.step1.productionType === 'BATCH_PRODUCTION' && '배치생산'}
                  </span>
                </div>
                <div className="col-12 mt-2">
                  <strong>선택된 KPI:</strong><br />
                  <span className="text-muted">
                    {formData.step3.selectedKpis.join(', ')}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="d-flex justify-content-center">
        <TablerButton
          variant="primary"
          size="lg"
          onClick={handleComplete}
          loading={loading}
        >
          대시보드 시작하기
        </TablerButton>
      </div>
    </TablerCard>
  );

  return (
    <div className="container-xl">
      <div className="page-header d-print-none">
        <div className="row g-2 align-items-center">
          <div className="col">
            <h2 className="page-title">제조업 맞춤 설정</h2>
            <div className="text-muted mt-1">
              귀하의 제조업 특성에 맞는 대시보드를 구성합니다
            </div>
          </div>
        </div>
      </div>

      <div className="row justify-content-center">
        <div className="col-12 col-lg-10">
          {renderStepIndicator()}
          
          {error && (
            <div className="alert alert-danger" role="alert">
              <div className="d-flex">
                <div>
                  <svg className="icon alert-icon" width="24" height="24" viewBox="0 0 24 24" strokeWidth="2" stroke="currentColor" fill="none" strokeLinecap="round" strokeLinejoin="round">
                    <path stroke="none" d="M0 0h24v24H0z" fill="none"/>
                    <circle cx="12" cy="12" r="9"/>
                    <line x1="12" y1="8" x2="12" y2="12"/>
                    <line x1="12" y1="16" x2="12.01" y2="16"/>
                  </svg>
                </div>
                <div>{error}</div>
              </div>
            </div>
          )}

          {currentStep === 1 && renderStep1()}
          {currentStep === 2 && renderStep2()}
          {currentStep === 3 && renderStep3()}
          {currentStep === 4 && renderStep4()}
        </div>
      </div>
    </div>
  );
};

CustomizationWizardNew.propTypes = {
  companyId: PropTypes.oneOfType([PropTypes.string, PropTypes.number]).isRequired,
  onComplete: PropTypes.func
};

export default CustomizationWizardNew;