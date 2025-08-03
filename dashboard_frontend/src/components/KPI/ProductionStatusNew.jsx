import React from 'react';
import PropTypes from 'prop-types';
import StatsCard from '../ui/StatsCard';
import ChartWrapper from '../ui/ChartWrapper';
import { useCustomizationContext } from '../../hooks/useCustomization';

const ProductionStatusNew = ({ oee = 61.2, oeeComponents = null }) => {
  const { getIndustryTheme } = useCustomizationContext();
  const industry = getIndustryTheme();

  // OEE 도넛 차트 데이터
  const chartSeries = [oee, 100 - oee];
  const chartOptions = {
    labels: ['OEE', '나머지'],
    colors: industry === 'automotive' ? ['#1f77b4', '#e9ecef'] : undefined,
    plotOptions: {
      pie: {
        donut: {
          size: '70%',
          labels: {
            show: true,
            name: {
              show: false
            },
            value: {
              show: true,
              fontSize: '16px',
              fontWeight: 600,
              formatter: (val) => `${parseFloat(val).toFixed(1)}%`
            },
            total: {
              show: true,
              showAlways: true,
              label: 'OEE',
              fontSize: '14px',
              fontWeight: 500,
              color: '#6c757d',
              formatter: () => `${oee.toFixed(1)}%`
            }
          }
        }
      }
    },
    tooltip: {
      enabled: false
    },
    legend: {
      show: false
    }
  };

  const getOEEGrade = () => {
    if (oee >= 85) return { grade: 'A', color: 'success', trend: 1 };
    if (oee >= 75) return { grade: 'B', color: 'info', trend: 0 };
    if (oee >= 65) return { grade: 'C', color: 'warning', trend: -1 };
    return { grade: 'D', color: 'danger', trend: -1 };
  };

  const oeeGrade = getOEEGrade();

  const productionIcon = (
    <>
      <path stroke="none" d="M0 0h24v24H0z" fill="none"/>
      <circle cx="12" cy="12" r="2"/>
      <path d="M12 1v6l4 0v4l5 0v-2a9 9 0 0 0 -9 -9"/>
      <path d="M1 12v2a9 9 0 0 0 9 9v-6l-4 0v-4l-5 0"/>
    </>
  );

  return (
    <div className="row">
      <div className="col-md-6">
        <StatsCard
          title="종합장비효율 (OEE)"
          value={`${oee.toFixed(1)}%`}
          trend={oeeGrade.trend}
          trendValue={2.5}
          icon={productionIcon}
          color={oeeGrade.color}
          industry={industry}
        />
      </div>
      <div className="col-md-6">
        <ChartWrapper
          title="OEE 상세"
          chartType="donut"
          series={chartSeries}
          options={chartOptions}
          height={200}
          industry={industry}
        />
      </div>
    </div>
  );
};

ProductionStatusNew.propTypes = {
  oee: PropTypes.number,
  oeeComponents: PropTypes.object
};

export default ProductionStatusNew;