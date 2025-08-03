import React, { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from './ui/card';
import { Button } from './ui/button';
import { Progress } from './ui/progress';
import { Alert, AlertDescription } from './ui/alert';
import apiService from '../service/apiService';

const CustomizationWizard = ({ companyId, onComplete }) => {
  const [currentStep, setCurrentStep] = useState(1);
  const [industries, setIndustries] = useState([]);
  const [formData, setFormData] = useState({
    step1: {
      industryTypeId: null,
      companySize: '',
      productionType: ''
    },
    step2: {
      dailyProductionCapacity: '',
      automationLevel: 50,
      qualityStandards: '',
      specialRequirements: ''
    },
    step3: {
      selectedKpis: [],
      dashboardSettings: {},
      alertSettings: {}
    }
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    loadIndustries();
    checkSetupStatus();
  }, []);

  const loadIndustries = async () => {
    try {
      const response = await apiService.get('/api/customization/industries');
      setIndustries(response.data);
    } catch (error) {
      console.error('업종 로딩 오류:', error);
      setError('업종 정보를 불러오는데 실패했습니다.');
    }
  };

  const checkSetupStatus = async () => {
    try {
      const response = await apiService.get(`/api/customization/setup-status/${companyId}`);
      if (response.data.setupCompleted) {
        onComplete?.();
      }
    } catch (error) {
      console.error('설정 상태 확인 오류:', error);
    }
  };

  const handleStep1Submit = async () => {
    if (!formData.step1.industryTypeId || !formData.step1.companySize || !formData.step1.productionType) {
      setError('모든 필드를 입력해주세요.');
      return;
    }

    setLoading(true);
    setError('');

    try {
      await apiService.post(`/api/customization/setup/step1/${companyId}`, formData.step1);
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
      await apiService.post(`/api/customization/setup/step2/${companyId}`, formData.step2);
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
      await apiService.post(`/api/customization/setup/step3/${companyId}`, formData.step3);
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
      await apiService.post(`/api/customization/setup/complete/${companyId}`, { confirmSetup: true });
      onComplete?.();
    } catch (error) {
      setError(error.response?.data?.error || '설정 완료에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const updateFormData = (step, field, value) => {
    setFormData(prev => ({
      ...prev,
      [step]: {
        ...prev[step],
        [field]: value
      }
    }));
  };

  const renderStep1 = () => (
    <div className="space-y-6">
      <div>
        <h3 className="text-lg font-semibold mb-4">1단계: 기본 정보</h3>
        
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium mb-2">제조업 분야</label>
            <select 
              className="w-full p-2 border rounded-md"
              value={formData.step1.industryTypeId || ''}
              onChange={(e) => updateFormData('step1', 'industryTypeId', e.target.value)}
            >
              <option value="">선택해주세요</option>
              {industries.map(industry => (
                <option key={industry.industryTypeId} value={industry.industryTypeId}>
                  {industry.industryName}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium mb-2">회사 규모</label>
            <select 
              className="w-full p-2 border rounded-md"
              value={formData.step1.companySize}
              onChange={(e) => updateFormData('step1', 'companySize', e.target.value)}
            >
              <option value="">선택해주세요</option>
              <option value="STARTUP">스타트업</option>
              <option value="SMALL">중소기업</option>
              <option value="MEDIUM">중견기업</option>
              <option value="LARGE">대기업</option>
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium mb-2">생산 방식</label>
            <select 
              className="w-full p-2 border rounded-md"
              value={formData.step1.productionType}
              onChange={(e) => updateFormData('step1', 'productionType', e.target.value)}
            >
              <option value="">선택해주세요</option>
              <option value="MASS_PRODUCTION">대량생산</option>
              <option value="VARIETY_SMALL_LOT">다품종 소량생산</option>
              <option value="CUSTOM_ORDER">주문생산</option>
              <option value="BATCH_PRODUCTION">배치생산</option>
            </select>
          </div>
        </div>
      </div>

      <Button onClick={handleStep1Submit} disabled={loading} className="w-full">
        다음 단계
      </Button>
    </div>
  );

  const renderStep2 = () => (
    <div className="space-y-6">
      <div>
        <h3 className="text-lg font-semibold mb-4">2단계: 생산 환경</h3>
        
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium mb-2">일일 생산 능력</label>
            <input 
              type="number"
              className="w-full p-2 border rounded-md"
              placeholder="단위 개수를 입력해주세요"
              value={formData.step2.dailyProductionCapacity}
              onChange={(e) => updateFormData('step2', 'dailyProductionCapacity', e.target.value)}
            />
          </div>

          <div>
            <label className="block text-sm font-medium mb-2">
              자동화 수준: {formData.step2.automationLevel}%
            </label>
            <input 
              type="range"
              min="0"
              max="100"
              className="w-full"
              value={formData.step2.automationLevel}
              onChange={(e) => updateFormData('step2', 'automationLevel', e.target.value)}
            />
          </div>

          <div>
            <label className="block text-sm font-medium mb-2">품질 표준</label>
            <textarea 
              className="w-full p-2 border rounded-md"
              rows="3"
              placeholder="ISO, HACCP 등 적용 중인 품질 표준을 입력해주세요"
              value={formData.step2.qualityStandards}
              onChange={(e) => updateFormData('step2', 'qualityStandards', e.target.value)}
            />
          </div>

          <div>
            <label className="block text-sm font-medium mb-2">특수 요구사항</label>
            <textarea 
              className="w-full p-2 border rounded-md"
              rows="3"
              placeholder="클린룸, 안전 규정 등 특수 요구사항을 입력해주세요"
              value={formData.step2.specialRequirements}
              onChange={(e) => updateFormData('step2', 'specialRequirements', e.target.value)}
            />
          </div>
        </div>
      </div>

      <div className="flex space-x-2">
        <Button variant="outline" onClick={() => setCurrentStep(1)} className="flex-1">
          이전
        </Button>
        <Button onClick={handleStep2Submit} disabled={loading} className="flex-1">
          다음 단계
        </Button>
      </div>
    </div>
  );

  const renderStep3 = () => (
    <div className="space-y-6">
      <div>
        <h3 className="text-lg font-semibold mb-4">3단계: KPI 및 모니터링</h3>
        
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium mb-2">중요 지표 선택</label>
            <div className="grid grid-cols-2 gap-2">
              {['생산량', '품질률', '효율성', '안전성', '비용', '장비가동률'].map(kpi => (
                <label key={kpi} className="flex items-center space-x-2">
                  <input 
                    type="checkbox"
                    checked={formData.step3.selectedKpis.includes(kpi)}
                    onChange={(e) => {
                      const currentKpis = formData.step3.selectedKpis;
                      if (e.target.checked) {
                        updateFormData('step3', 'selectedKpis', [...currentKpis, kpi]);
                      } else {
                        updateFormData('step3', 'selectedKpis', currentKpis.filter(k => k !== kpi));
                      }
                    }}
                  />
                  <span>{kpi}</span>
                </label>
              ))}
            </div>
          </div>
        </div>
      </div>

      <div className="flex space-x-2">
        <Button variant="outline" onClick={() => setCurrentStep(2)} className="flex-1">
          이전
        </Button>
        <Button onClick={handleStep3Submit} disabled={loading} className="flex-1">
          다음 단계
        </Button>
      </div>
    </div>
  );

  const renderStep4 = () => (
    <div className="space-y-6">
      <div className="text-center">
        <h3 className="text-lg font-semibold mb-4">설정 완료</h3>
        <p className="text-gray-600 mb-6">
          모든 설정이 완료되었습니다. 확인을 누르시면 커스터마이징된 대시보드를 사용할 수 있습니다.
        </p>
      </div>

      <div className="flex space-x-2">
        <Button variant="outline" onClick={() => setCurrentStep(3)} className="flex-1">
          이전
        </Button>
        <Button onClick={handleComplete} disabled={loading} className="flex-1">
          설정 완료
        </Button>
      </div>
    </div>
  );

  return (
    <div className="max-w-2xl mx-auto p-6">
      <Card>
        <CardHeader>
          <CardTitle>제조업 맞춤 설정</CardTitle>
          <Progress value={(currentStep / 4) * 100} className="w-full" />
        </CardHeader>
        <CardContent>
          {error && (
            <Alert className="mb-4">
              <AlertDescription>{error}</AlertDescription>
            </Alert>
          )}

          {currentStep === 1 && renderStep1()}
          {currentStep === 2 && renderStep2()}
          {currentStep === 3 && renderStep3()}
          {currentStep === 4 && renderStep4()}
        </CardContent>
      </Card>
    </div>
  );
};

export default CustomizationWizard;