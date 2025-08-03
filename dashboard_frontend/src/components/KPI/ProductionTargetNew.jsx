import React from 'react';
import PropTypes from 'prop-types';
import StatsCard from '../ui/StatsCard';
import useApi from '../../hooks/useApi';
import { useCustomizationContext } from '../../hooks/useCustomization';

const ProductionTargetNew = ({ companyId }) => {
  const { getIndustryTheme } = useCustomizationContext();
  const industry = getIndustryTheme();

  const { data: targetData, loading, error } = useApi(
    `/api/dashboard/production-target${companyId ? `?companyId=${companyId}` : ''}`,
    {
      immediate: true,
      refreshInterval: 300000, // 5분마다 새로고침
      transform: (data) => {
        if (!data) return { current: 0, target: 0, achievement: 0 };
        
        const achievement = data.target > 0 ? (data.current / data.target) * 100 : 0;
        return {
          current: data.current || 0,
          target: data.target || 0,
          achievement: achievement
        };
      }
    }
  );

  const getAchievementGrade = () => {
    if (!targetData) return { grade: 'N/A', color: 'secondary', trend: 0 };
    
    const achievement = targetData.achievement;
    if (achievement >= 100) return { grade: 'A', color: 'success', trend: 1 };
    if (achievement >= 90) return { grade: 'B', color: 'info', trend: 0 };
    if (achievement >= 80) return { grade: 'C', color: 'warning', trend: -1 };
    return { grade: 'D', color: 'danger', trend: -1 };
  };

  const achievementGrade = getAchievementGrade();

  const targetIcon = (
    <>
      <path stroke="none" d="M0 0h24v24H0z" fill="none"/>
      <circle cx="12" cy="12" r="9"/>
      <circle cx="12" cy="12" r="6"/>
      <circle cx="12" cy="12" r="3"/>
    </>
  );

  if (loading) {
    return (
      <StatsCard
        title="생산 목표 달성률"
        value="로딩중..."
        icon={targetIcon}
        loading={true}
        industry={industry}
      />
    );
  }

  if (error) {
    return (
      <StatsCard
        title="생산 목표 달성률"
        value="오류"
        icon={targetIcon}
        color="danger"
        industry={industry}
      />
    );
  }

  return (
    <div className="row">
      <div className="col-md-4">
        <StatsCard
          title="현재 생산량"
          value={targetData?.current?.toLocaleString() || '0'}
          icon={targetIcon}
          color="primary"
          industry={industry}
        />
      </div>
      <div className="col-md-4">
        <StatsCard
          title="목표 생산량"
          value={targetData?.target?.toLocaleString() || '0'}
          icon={targetIcon}
          color="info"
          industry={industry}
        />
      </div>
      <div className="col-md-4">
        <StatsCard
          title="목표 달성률"
          value={`${targetData?.achievement?.toFixed(1) || '0'}%`}
          trend={achievementGrade.trend}
          trendValue={3.2}
          icon={targetIcon}
          color={achievementGrade.color}
          industry={industry}
        />
      </div>
    </div>
  );
};

ProductionTargetNew.propTypes = {
  companyId: PropTypes.oneOfType([PropTypes.string, PropTypes.number])
};

export default ProductionTargetNew;