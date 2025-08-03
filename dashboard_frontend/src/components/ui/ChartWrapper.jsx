import React, { useEffect, useRef, useState } from 'react';
import PropTypes from 'prop-types';
import ApexCharts from 'apexcharts';
import TablerCard from './TablerCard';

const ChartWrapper = ({
  title,
  subtitle,
  actions,
  chartType = 'line',
  series = [],
  options = {},
  height = 300,
  loading = false,
  error = null,
  className = '',
  industry,
  onChartReady,
  ...props
}) => {
  const chartRef = useRef(null);
  const chartInstance = useRef(null);
  const [isReady, setIsReady] = useState(false);

  const getIndustryColors = () => {
    const industryColorMap = {
      automotive: ['#1f77b4', '#aec7e8', '#1f77b4aa'],
      electronics: ['#ff7f0e', '#ffbb78', '#ff7f0eaa'],
      food: ['#2ca02c', '#98df8a', '#2ca02caa'],
      chemical: ['#d62728', '#ff9896', '#d62728aa'],
      textile: ['#9467bd', '#c5b0d5', '#9467bdaa'],
      metal: ['#8c564b', '#c49c94', '#8c564baa']
    };
    
    return industryColorMap[industry] || ['#1f77b4', '#aec7e8', '#1f77b4aa'];
  };

  const getDefaultOptions = () => {
    const colors = industry ? getIndustryColors() : ['#1f77b4'];
    
    const defaultOptions = {
      chart: {
        type: chartType,
        fontFamily: 'inherit',
        height: height,
        parentHeightOffset: 0,
        toolbar: {
          show: false
        },
        animations: {
          enabled: true,
          easing: 'easeinout',
          speed: 800
        }
      },
      colors: colors,
      dataLabels: {
        enabled: false
      },
      stroke: {
        width: chartType === 'line' ? 2 : 0,
        curve: 'smooth'
      },
      grid: {
        strokeDashArray: 4,
        padding: {
          top: 0,
          right: 0,
          bottom: 0,
          left: 0
        }
      },
      tooltip: {
        theme: 'light'
      },
      legend: {
        show: series.length > 1,
        position: 'bottom',
        horizontalAlign: 'center',
        fontSize: '12px',
        markers: {
          width: 8,
          height: 8,
          radius: 2
        }
      }
    };

    // Chart type specific options
    if (chartType === 'donut' || chartType === 'pie') {
      defaultOptions.plotOptions = {
        pie: {
          donut: {
            labels: {
              show: true,
              total: {
                show: true,
                showAlways: false,
                fontSize: '14px',
                fontWeight: 600,
                color: '#6c757d'
              }
            }
          }
        }
      };
      defaultOptions.legend.position = 'right';
    }

    if (chartType === 'bar') {
      defaultOptions.plotOptions = {
        bar: {
          horizontal: false,
          columnWidth: '55%',
          borderRadius: 2
        }
      };
    }

    return defaultOptions;
  };

  const initializeChart = () => {
    if (!chartRef.current || !series.length) return;

    const mergedOptions = {
      ...getDefaultOptions(),
      ...options,
      series: series
    };

    if (chartInstance.current) {
      chartInstance.current.destroy();
    }

    chartInstance.current = new ApexCharts(chartRef.current, mergedOptions);
    chartInstance.current.render().then(() => {
      setIsReady(true);
      onChartReady?.(chartInstance.current);
    });
  };

  useEffect(() => {
    if (!loading && !error && series.length > 0) {
      initializeChart();
    }

    return () => {
      if (chartInstance.current) {
        chartInstance.current.destroy();
        chartInstance.current = null;
      }
    };
  }, [series, options, loading, error]);

  useEffect(() => {
    if (chartInstance.current && isReady && !loading && !error) {
      chartInstance.current.updateSeries(series);
    }
  }, [series, isReady, loading, error]);

  const renderContent = () => {
    if (loading) {
      return (
        <div className="chart-loading" style={{ height: `${height}px` }}>
          <div className="spinner-border text-primary" role="status">
            <span className="visually-hidden">Loading...</span>
          </div>
        </div>
      );
    }

    if (error) {
      return (
        <div className="chart-loading" style={{ height: `${height}px` }}>
          <div className="text-danger">
            <div className="mb-2">⚠️ 차트 로딩 오류</div>
            <small>{error}</small>
          </div>
        </div>
      );
    }

    if (!series.length) {
      return (
        <div className="chart-loading" style={{ height: `${height}px` }}>
          <div className="text-muted">
            📊 데이터가 없습니다
          </div>
        </div>
      );
    }

    return (
      <div className="chart-container">
        <div ref={chartRef} />
      </div>
    );
  };

  return (
    <TablerCard
      title={title}
      subtitle={subtitle}
      actions={actions}
      className={`chart-card ${industry ? `industry-${industry}` : ''} ${className}`}
      {...props}
    >
      {renderContent()}
    </TablerCard>
  );
};

ChartWrapper.propTypes = {
  title: PropTypes.string,
  subtitle: PropTypes.string,
  actions: PropTypes.node,
  chartType: PropTypes.oneOf(['line', 'area', 'bar', 'column', 'pie', 'donut', 'radialBar']),
  series: PropTypes.array.isRequired,
  options: PropTypes.object,
  height: PropTypes.number,
  loading: PropTypes.bool,
  error: PropTypes.string,
  className: PropTypes.string,
  industry: PropTypes.oneOf(['automotive', 'electronics', 'food', 'chemical', 'textile', 'metal']),
  onChartReady: PropTypes.func
};

export default ChartWrapper;