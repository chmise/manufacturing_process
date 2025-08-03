import React from 'react';
import PropTypes from 'prop-types';
import ChartWrapper from '../ui/ChartWrapper';
import useApi from '../../hooks/useApi';
import { useCustomizationContext } from '../../hooks/useCustomization';

const HourlyProductionNew = ({ companyId }) => {
  const { getIndustryTheme } = useCustomizationContext();
  const industry = getIndustryTheme();

  const { data: productionData, loading, error } = useApi(
    `/api/dashboard/hourly-production${companyId ? `?companyId=${companyId}` : ''}`,
    {
      immediate: true,
      refreshInterval: 60000, // 1분마다 새로고침
      transform: (data) => {
        if (!data || !Array.isArray(data)) return [];
        
        return data.map(item => ({
          hour: item.hour,
          production: item.productionCount || 0,
          target: item.targetCount || 100
        }));
      }
    }
  );

  const chartSeries = productionData ? [
    {
      name: '생산량',
      data: productionData.map(item => item.production)
    },
    {
      name: '목표',
      data: productionData.map(item => item.target)
    }
  ] : [];

  const chartOptions = {
    chart: {
      type: 'bar',
      stacked: false
    },
    xaxis: {
      categories: productionData ? productionData.map(item => `${item.hour}시`) : [],
      title: {
        text: '시간'
      }
    },
    yaxis: {
      title: {
        text: '생산량 (개)'
      }
    },
    tooltip: {
      shared: true,
      intersect: false
    },
    legend: {
      position: 'top',
      horizontalAlign: 'right'
    }
  };

  return (
    <ChartWrapper
      title="시간별 생산 현황"
      subtitle="실시간 생산량 vs 목표"
      chartType="bar"
      series={chartSeries}
      options={chartOptions}
      height={350}
      loading={loading}
      error={error}
      industry={industry}
    />
  );
};

HourlyProductionNew.propTypes = {
  companyId: PropTypes.oneOfType([PropTypes.string, PropTypes.number])
};

export default HourlyProductionNew;