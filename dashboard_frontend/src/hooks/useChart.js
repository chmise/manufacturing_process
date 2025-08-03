import { useState, useRef, useCallback, useEffect } from 'react';
import ApexCharts from 'apexcharts';

const useChart = (initialOptions = {}, initialSeries = []) => {
  const [options, setOptions] = useState(initialOptions);
  const [series, setSeries] = useState(initialSeries);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  
  const chartRef = useRef(null);
  const chartInstance = useRef(null);
  const containerRef = useRef(null);

  // 차트 초기화
  const initChart = useCallback((container) => {
    if (!container || !series.length) return null;

    try {
      if (chartInstance.current) {
        chartInstance.current.destroy();
      }

      const chartConfig = {
        ...options,
        series: series
      };

      chartInstance.current = new ApexCharts(container, chartConfig);
      return chartInstance.current;
    } catch (err) {
      console.error('Chart initialization error:', err);
      setError(err.message);
      return null;
    }
  }, [options, series]);

  // 차트 렌더링
  const renderChart = useCallback(async () => {
    if (!containerRef.current || !chartInstance.current) return;

    try {
      setLoading(true);
      setError(null);
      await chartInstance.current.render();
      setLoading(false);
    } catch (err) {
      console.error('Chart render error:', err);
      setError(err.message);
      setLoading(false);
    }
  }, []);

  // 시리즈 업데이트
  const updateSeries = useCallback((newSeries, animate = true) => {
    if (!chartInstance.current) return;

    try {
      setSeries(newSeries);
      chartInstance.current.updateSeries(newSeries, animate);
    } catch (err) {
      console.error('Chart update series error:', err);
      setError(err.message);
    }
  }, []);

  // 옵션 업데이트
  const updateOptions = useCallback((newOptions, redrawPaths = true, animate = true) => {
    if (!chartInstance.current) return;

    try {
      setOptions(prev => ({ ...prev, ...newOptions }));
      chartInstance.current.updateOptions(newOptions, redrawPaths, animate);
    } catch (err) {
      console.error('Chart update options error:', err);
      setError(err.message);
    }
  }, []);

  // 차트 크기 조정
  const resize = useCallback(() => {
    if (!chartInstance.current) return;

    try {
      chartInstance.current.resize();
    } catch (err) {
      console.error('Chart resize error:', err);
      setError(err.message);
    }
  }, []);

  // 차트 데이터 추가 (실시간 업데이트용)
  const appendData = useCallback((newData) => {
    if (!chartInstance.current) return;

    try {
      chartInstance.current.appendData(newData);
    } catch (err) {
      console.error('Chart append data error:', err);
      setError(err.message);
    }
  }, []);

  // 차트 상태 초기화
  const resetChart = useCallback(() => {
    if (!chartInstance.current) return;

    try {
      chartInstance.current.resetSeries();
      setError(null);
    } catch (err) {
      console.error('Chart reset error:', err);
      setError(err.message);
    }
  }, []);

  // 차트 이미지 내보내기
  const exportChart = useCallback((type = 'png', options = {}) => {
    if (!chartInstance.current) return null;

    try {
      return chartInstance.current.dataURI({ 
        type, 
        width: options.width,
        height: options.height
      });
    } catch (err) {
      console.error('Chart export error:', err);
      setError(err.message);
      return null;
    }
  }, []);

  // 차트 컨테이너 참조 설정
  const setContainer = useCallback((element) => {
    containerRef.current = element;
    if (element) {
      const chart = initChart(element);
      if (chart) {
        renderChart();
      }
    }
  }, [initChart, renderChart]);

  // 정리
  const destroy = useCallback(() => {
    if (chartInstance.current) {
      try {
        chartInstance.current.destroy();
        chartInstance.current = null;
      } catch (err) {
        console.error('Chart destroy error:', err);
      }
    }
  }, []);

  // 컴포넌트 언마운트 시 정리
  useEffect(() => {
    return () => {
      destroy();
    };
  }, [destroy]);

  // 창 크기 변경 시 차트 크기 조정
  useEffect(() => {
    const handleResize = () => {
      setTimeout(resize, 100); // 약간의 지연으로 레이아웃 완료 후 실행
    };

    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, [resize]);

  return {
    // 상태
    options,
    series,
    loading,
    error,
    chartInstance: chartInstance.current,
    
    // 메서드
    setContainer,
    updateSeries,
    updateOptions,
    resize,
    appendData,
    resetChart,
    exportChart,
    destroy,
    
    // 차트 참조 (legacy support)
    chartRef: setContainer
  };
};

// 특정 차트 타입용 편의 훅들
export const useLineChart = (initialOptions = {}, initialSeries = []) => {
  const defaultOptions = {
    chart: {
      type: 'line',
      fontFamily: 'inherit',
      height: 300,
      toolbar: { show: false },
      animations: { enabled: true }
    },
    stroke: {
      width: 2,
      curve: 'smooth'
    },
    grid: {
      strokeDashArray: 4
    },
    ...initialOptions
  };

  return useChart(defaultOptions, initialSeries);
};

export const useDonutChart = (initialOptions = {}, initialSeries = []) => {
  const defaultOptions = {
    chart: {
      type: 'donut',
      fontFamily: 'inherit',
      height: 300,
      toolbar: { show: false }
    },
    plotOptions: {
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
    },
    legend: {
      position: 'bottom'
    },
    ...initialOptions
  };

  return useChart(defaultOptions, initialSeries);
};

export const useBarChart = (initialOptions = {}, initialSeries = []) => {
  const defaultOptions = {
    chart: {
      type: 'bar',
      fontFamily: 'inherit',
      height: 300,
      toolbar: { show: false }
    },
    plotOptions: {
      bar: {
        horizontal: false,
        columnWidth: '55%',
        borderRadius: 2
      }
    },
    grid: {
      strokeDashArray: 4
    },
    ...initialOptions
  };

  return useChart(defaultOptions, initialSeries);
};

export default useChart;