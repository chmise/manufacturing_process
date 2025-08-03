import React from 'react';
import PropTypes from 'prop-types';
import ChartWrapper from '../ui/ChartWrapper';
import useApi from '../../hooks/useApi';
import { useCustomizationContext } from '../../hooks/useCustomization';

const CycleTimeNew = ({ companyId }) => {
  const { getIndustryTheme } = useCustomizationContext();
  const industry = getIndustryTheme();

  const { data: cycleTimeData, loading, error } = useApi(
    `/api/dashboard/cycle-time${companyId ? `?companyId=${companyId}` : ''}`,
    {
      immediate: true,
      refreshInterval: 30000, // 30초마다 새로고침
      transform: (data) => {
        if (!data || !Array.isArray(data)) return [];
        
        return data.map(item => ({
          time: new Date(item.timestamp).toLocaleTimeString(),
          cycleTime: item.cycleTime || 0,
          target: item.targetCycleTime || 45
        }));
      }
    }
  );

  const chartSeries = cycleTimeData ? [
    {
      name: '실제 사이클 타임',
      data: cycleTimeData.map(item => item.cycleTime)
    },
    {
      name: '목표 사이클 타임',
      data: cycleTimeData.map(item => item.target)
    }
  ] : [];

  const chartOptions = {
    chart: {
      type: 'line',
      zoom: {
        enabled: false
      }
    },
    xaxis: {
      categories: cycleTimeData ? cycleTimeData.map(item => item.time) : [],
      title: {
        text: '시간'
      }
    },
    yaxis: {
      title: {
        text: '사이클 타임 (초)'
      },
      min: 0
    },
    stroke: {
      width: [3, 2],
      dashArray: [0, 5]
    },
    markers: {
      size: [4, 0]
    },
    tooltip: {
      shared: true,
      intersect: false,
      y: {
        formatter: (val) => `${val}초`
      }
    },
    legend: {
      position: 'top',
      horizontalAlign: 'right'
    }
  };

  return (
    <ChartWrapper
      title="사이클 타임 모니터링"
      subtitle="실시간 사이클 타임 vs 목표"
      chartType="line"
      series={chartSeries}
      options={chartOptions}
      height={300}
      loading={loading}
      error={error}
      industry={industry}
    />
  );
};

CycleTimeNew.propTypes = {
  companyId: PropTypes.oneOfType([PropTypes.string, PropTypes.number])
};

export default CycleTimeNew;