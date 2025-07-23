import React, { useEffect, useRef, useState } from 'react';
import ApexCharts from 'apexcharts';
import axios from 'axios';

const SummaryCard = () => {
  const chartRef = useRef(null);
  const apexChartInstance = useRef(null);
  const [data, setData] = useState([]);
  const [hasData, setHasData] = useState(false);

  useEffect(() => {
    axios.get('http://localhost:8080/api/stocks/summary')
      .then(response => {
        if (response.data && Array.isArray(response.data)) {
          setData(response.data);
          setHasData(true);
        } else {
          console.warn('백엔드에서 받은 데이터가 비어있거나 형식이 맞지 않습니다.');
        }
      })
      .catch(error => {
        console.error('백엔드 요청 실패:', error);
        setHasData(false);
      });
  }, []);

  useEffect(() => {
    if (!chartRef.current || !hasData || data.length === 0) return;

    // 기존 차트가 있으면 제거
    if (apexChartInstance.current) {
      apexChartInstance.current.destroy();
    }

    const chart = new ApexCharts(chartRef.current, {
      chart: {
        type: 'donut',
        fontFamily: 'inherit',
        height: 260,
        sparkline: { enabled: true },
        animations: { enabled: false }
      },
      series: data.map(item => item.count),
      labels: data.map(item => item.carModel),
      tooltip: {
        theme: 'dark',
        fillSeriesColor: false
      },
      grid: { strokeDashArray: 4 },
      colors: [
        '#FF0000', // 강렬한 빨강
        '#0000FF', // 선명한 파랑
        '#00FF00', // 진한 초록
        '#FFA500', // 선명한 오렌지
        '#800080'  // 보라색
      ],
      legend: {
        show: true,
        position: 'bottom',
        offsetY: 12,
        markers: { width: 10, height: 10, radius: 100 },
        itemMargin: { horizontal: 8, vertical: 8 }
      },
      plotOptions: {
        pie: {
          donut: {
            labels: { show: false }
          }
        }
      }
    });

    chart.render();
    apexChartInstance.current = chart;

    return () => chart.destroy();
  }, [data, hasData]);

  return (
    <div style={{ position: 'relative', paddingTop: '4rem' }}>
      {/* 좌측 상단 텍스트 */}
      <div style={{
        position: 'absolute',
        top: 0,
        left: 0,
        fontSize: '1rem',
        fontWeight: 'bold',
        color: '#1f2937'
      }}>
        차 이름
      </div>

      {/* 도넛 차트 */}
      {hasData ? (
        <div id="chart-demo-pie" ref={chartRef} />
      ) : (
        <div style={{
          height: '260px',
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          color: '#999'
        }}>
          데이터를 불러오는 중입니다. 잠시만 기다려 주세요.
        </div>
      )}
    </div>
  );
};

export default SummaryCard;