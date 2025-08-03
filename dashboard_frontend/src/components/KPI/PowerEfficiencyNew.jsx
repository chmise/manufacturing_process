import React from 'react';
import PropTypes from 'prop-types';
import StatsCard from '../ui/StatsCard';
import ChartWrapper from '../ui/ChartWrapper';
import useApi from '../../hooks/useApi';
import { useCustomizationContext } from '../../hooks/useCustomization';

const PowerEfficiencyNew = ({ companyId }) => {
  const { getIndustryTheme } = useCustomizationContext();
  const industry = getIndustryTheme();

  const { data: powerData, loading, error } = useApi(
    `/api/dashboard/power-efficiency${companyId ? `?companyId=${companyId}` : ''}`,
    {
      immediate: true,
      refreshInterval: 60000, // 1분마다 새로고침
      transform: (data) => {
        if (!data) return { efficiency: 0, consumption: 0, trend: [] };
        
        return {
          efficiency: data.efficiency || 0,
          consumption: data.totalConsumption || 0,
          trend: data.hourlyTrend || []
        };
      }
    }
  );

  const getEfficiencyGrade = () => {
    if (!powerData) return { grade: 'N/A', color: 'secondary', trend: 0 };
    
    const efficiency = powerData.efficiency;
    if (efficiency >= 90) return { grade: 'A', color: 'success', trend: 1 };
    if (efficiency >= 80) return { grade: 'B', color: 'info', trend: 0 };
    if (efficiency >= 70) return { grade: 'C', color: 'warning', trend: -1 };
    return { grade: 'D', color: 'danger', trend: -1 };
  };

  const efficiencyGrade = getEfficiencyGrade();

  const powerIcon = (
    <>
      <path stroke="none" d="M0 0h24v24H0z" fill="none"/>
      <path d="M13 3l0 7l6 0l-8 11l0 -7l-6 0l8 -11"/>
    </>
  );

  const chartSeries = powerData?.trend ? [
    {
      name: '전력 소비량',
      data: powerData.trend.map(item => item.consumption || 0)
    },
    {
      name: '효율성 지수',
      data: powerData.trend.map(item => item.efficiency || 0)
    }
  ] : [];

  const chartOptions = {
    chart: {
      type: 'area',
      stacked: false
    },
    xaxis: {
      categories: powerData?.trend ? powerData.trend.map(item => `${item.hour}시`) : [],
      title: {
        text: '시간'
      }
    },
    yaxis: [
      {
        title: {
          text: '전력 소비량 (kWh)'
        },
        seriesName: '전력 소비량'
      },
      {
        opposite: true,
        title: {
          text: '효율성 지수 (%)'
        },
        seriesName: '효율성 지수'
      }
    ],
    stroke: {
      width: [2, 2],
      curve: 'smooth'
    },
    fill: {
      type: 'gradient',
      gradient: {
        shadeIntensity: 1,
        opacityFrom: 0.7,
        opacityTo: 0.3
      }
    },
    tooltip: {
      shared: true,
      intersect: false
    }
  };

  return (
    <div className="row">
      <div className="col-md-6">
        <div className="row">
          <div className="col-12 mb-3">
            <StatsCard
              title="전력 효율성"
              value={`${powerData?.efficiency?.toFixed(1) || '0'}%`}
              trend={efficiencyGrade.trend}
              trendValue={2.1}
              icon={powerIcon}
              color={efficiencyGrade.color}
              industry={industry}
              loading={loading}
            />
          </div>
          <div className="col-12">
            <StatsCard
              title="총 전력 소비량"
              value={`${powerData?.consumption?.toLocaleString() || '0'} kWh`}
              icon={powerIcon}
              color="warning"
              industry={industry}
              loading={loading}
            />
          </div>
        </div>
      </div>
      <div className="col-md-6">
        <ChartWrapper
          title="시간별 전력 효율성"
          subtitle="전력 소비량 vs 효율성 지수"
          chartType="area"
          series={chartSeries}
          options={chartOptions}
          height={280}
          loading={loading}
          error={error}
          industry={industry}
        />
      </div>
    </div>
  );
};

PowerEfficiencyNew.propTypes = {
  companyId: PropTypes.oneOfType([PropTypes.string, PropTypes.number])
};

export default PowerEfficiencyNew;