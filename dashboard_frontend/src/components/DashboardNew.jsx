import React, { useState, useEffect } from 'react';
import { CustomizationProvider, useCustomizationContext } from '../hooks/useCustomization';
import useApi from '../hooks/useApi';

// New KPI Components
import ProductionStatusNew from './KPI/ProductionStatusNew';
import FTYStatusNew from './KPI/FTYStatusNew';
import OTDStatusNew from './KPI/OTDStatusNew';
import ProductionTargetNew from './KPI/ProductionTargetNew';
import HourlyProductionNew from './KPI/HourlyProductionNew';
import CycleTimeNew from './KPI/CycleTimeNew';
import PowerEfficiencyNew from './KPI/PowerEfficiencyNew';

// New Table Components
import RobotTablesNew from './Robot/RobotTablesNew';
import InventoryTableNew from './Inventory/InventoryTableNew';

// UI Components
import TablerCard from './ui/TablerCard';
import TablerButton from './ui/TablerButton';
import StatsCard from './ui/StatsCard';

const DashboardContent = () => {
  const {
    setupCompleted,
    loading,
    getSelectedKPIs,
    getIndustryTheme,
    getCompanyInfo,
    getDashboardSettings
  } = useCustomizationContext();

  const [userInfo, setUserInfo] = useState(null);
  const [companyInfo, setCompanyInfo] = useState(null);
  const [lastUpdated, setLastUpdated] = useState(new Date());

  const industry = getIndustryTheme();
  const selectedKPIs = getSelectedKPIs();
  const dashboardSettings = getDashboardSettings();
  const companyData = getCompanyInfo();

  // URL에서 회사명 추출
  const getCompanyNameFromUrl = () => {
    const path = window.location.pathname;
    const segments = path.split('/').filter(Boolean);
    return segments.length > 0 && segments[0] !== 'login' && segments[0] !== 'register' ? segments[0] : null;
  };

  const currentCompany = getCompanyNameFromUrl();

  // 대시보드 데이터 로딩
  const { data: dashboardData, loading: dashboardLoading } = useApi(
    `/api/dashboard/summary${companyInfo?.companyId ? `?companyId=${companyInfo.companyId}` : ''}`,
    {
      immediate: true,
      refreshInterval: 30000, // 30초마다 새로고침
      onSuccess: () => setLastUpdated(new Date())
    }
  );

  useEffect(() => {
    const userData = localStorage.getItem('userData');
    if (userData) {
      const parsedUserData = JSON.parse(userData);
      setUserInfo(parsedUserData);
      setCompanyInfo(parsedUserData.company);
    }
  }, []);

  // 커스터마이징이 완료되지 않은 경우
  if (!setupCompleted && !loading) {
    return (
      <div className="container-xl">
        <div className="page-header d-print-none">
          <div className="row g-2 align-items-center">
            <div className="col">
              <h2 className="page-title">대시보드 설정 필요</h2>
            </div>
          </div>
        </div>
        
        <div className="row justify-content-center">
          <div className="col-md-8">
            <TablerCard
              title="초기 설정이 필요합니다"
              className="text-center"
            >
              <div className="mb-4">
                <div className="mb-3">
                  <svg className="icon icon-tabler icon-xl text-muted" width="48" height="48" viewBox="0 0 24 24" strokeWidth="1" stroke="currentColor" fill="none" strokeLinecap="round" strokeLinejoin="round">
                    <path stroke="none" d="M0 0h24v24H0z" fill="none"/>
                    <path d="M10.325 4.317c.426 -1.756 2.924 -1.756 3.35 0a1.724 1.724 0 0 0 2.573 1.066c1.543 -.94 3.31 .826 2.37 2.37a1.724 1.724 0 0 0 1.065 2.572c1.756 .426 1.756 2.924 0 3.35a1.724 1.724 0 0 0 -1.066 2.573c.94 1.543 -.826 3.31 -2.37 2.37a1.724 1.724 0 0 0 -2.572 1.065c-.426 1.756 -2.924 1.756 -3.35 0a1.724 1.724 0 0 0 -2.573 -1.066c-1.543 .94 -3.31 -.826 -2.37 -2.37a1.724 1.724 0 0 0 -1.065 -2.572c-1.756 -.426 -1.756 -2.924 0 -3.35a1.724 1.724 0 0 0 1.066 -2.573c-.94 -1.543 .826 -3.31 2.37 -2.37c1 .608 2.296 .07 2.572 -1.065z"/>
                    <circle cx="12" cy="12" r="3"/>
                  </svg>
                </div>
                <h3>제조업 맞춤 설정</h3>
                <p className="text-muted">
                  귀하의 제조업 특성에 맞는 대시보드를 구성하기 위해 초기 설정이 필요합니다.
                  업종, 생산 방식, 중요 지표 등을 설정하여 맞춤형 모니터링 환경을 만들어보세요.
                </p>
              </div>
              
              <TablerButton
                variant="primary"
                onClick={() => window.location.href = `/${currentCompany}/setup`}
              >
                초기 설정 시작하기
              </TablerButton>
            </TablerCard>
          </div>
        </div>
      </div>
    );
  }

  // 로딩 중
  if (loading || dashboardLoading) {
    return (
      <div className="container-xl">
        <div className="d-flex justify-content-center align-items-center" style={{ height: '400px' }}>
          <div className="text-center">
            <div className="spinner-border text-primary mb-3" role="status">
              <span className="visually-hidden">Loading...</span>
            </div>
            <div>대시보드를 불러오는 중...</div>
          </div>
        </div>
      </div>
    );
  }

  // KPI 컴포넌트 렌더링 함수
  const renderKPIComponent = (kpiName) => {
    const props = {
      companyId: companyInfo?.companyId,
      data: dashboardData
    };

    switch (kpiName) {
      case '생산량':
        return <ProductionTargetNew key="production-target" {...props} />;
      case '품질률':
        return <FTYStatusNew key="fty-status" fty={dashboardData?.fty || 98.5} />;
      case '효율성':
        return <ProductionStatusNew key="production-status" oee={dashboardData?.oee || 61.2} />;
      case '안전성':
        return <OTDStatusNew key="otd-status" otd={dashboardData?.otd || 95.2} />;
      case '비용':
        return <PowerEfficiencyNew key="power-efficiency" {...props} />;
      case '장비가동률':
        return (
          <StatsCard
            key="equipment-utilization"
            title="장비 가동률"
            value={`${(dashboardData?.equipmentUtilization || 87.3).toFixed(1)}%`}
            trend={1}
            trendValue={2.1}
            color="info"
            industry={industry}
            icon={
              <>
                <path stroke="none" d="M0 0h24v24H0z" fill="none"/>
                <circle cx="12" cy="12" r="2"/>
                <path d="M12 1v6l4 0v4l5 0v-2a9 9 0 0 0 -9 -9"/>
                <path d="M1 12v2a9 9 0 0 0 9 9v-6l-4 0v-4l-5 0"/>
              </>
            }
          />
        );
      default:
        return null;
    }
  };

  const shouldShowChart = (chartName) => {
    return dashboardSettings[chartName] !== 'false';
  };

  return (
    <div className={`container-xl dashboard-${industry}`}>
      {/* 페이지 헤더 */}
      <div className="page-header d-print-none">
        <div className="row g-2 align-items-center">
          <div className="col">
            <h2 className="page-title">
              {companyData?.industryType || '제조업'} 대시보드
            </h2>
            <div className="text-muted mt-1">
              마지막 업데이트: {lastUpdated.toLocaleTimeString()}
            </div>
          </div>
          <div className="col-auto">
            <div className="btn-list">
              <TablerButton
                variant="outline-primary"
                size="sm"
                onClick={() => window.location.reload()}
              >
                새로고침
              </TablerButton>
              <TablerButton
                variant="primary"
                size="sm"
                onClick={() => window.location.href = `/${currentCompany}/setup`}
              >
                설정 변경
              </TablerButton>
            </div>
          </div>
        </div>
      </div>

      {/* KPI 카드들 */}
      <div className="row row-deck row-cards mb-4">
        {selectedKPIs.map((kpi, index) => (
          <div key={`kpi-${index}`} className="col-sm-6 col-lg-3">
            {renderKPIComponent(kpi)}
          </div>
        ))}
      </div>

      {/* 차트 섹션 */}
      <div className="row row-deck row-cards mb-4">
        {shouldShowChart('hourlyProduction') && (
          <div className="col-lg-8">
            <HourlyProductionNew companyId={companyInfo?.companyId} />
          </div>
        )}
        {shouldShowChart('cycleTime') && (
          <div className="col-lg-4">
            <CycleTimeNew companyId={companyInfo?.companyId} />
          </div>
        )}
      </div>

      {/* 테이블 섹션 */}
      <div className="row row-deck row-cards">
        <div className="col-12 mb-4">
          <RobotTablesNew companyId={companyInfo?.companyId} />
        </div>
        <div className="col-12">
          <InventoryTableNew companyId={companyInfo?.companyId} />
        </div>
      </div>
    </div>
  );
};

const DashboardNew = () => {
  const [companyId, setCompanyId] = useState(1); // 기본값

  useEffect(() => {
    const userData = localStorage.getItem('userData');
    if (userData) {
      const parsedUserData = JSON.parse(userData);
      setCompanyId(parsedUserData.company?.companyId || 1);
    }
  }, []);

  return (
    <CustomizationProvider companyId={companyId}>
      <DashboardContent />
    </CustomizationProvider>
  );
};

export default DashboardNew;