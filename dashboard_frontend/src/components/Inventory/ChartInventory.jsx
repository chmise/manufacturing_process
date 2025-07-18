import React, { useEffect, useRef, useState } from 'react';
import ApexCharts from 'apexcharts';
import axios from 'axios';

const ChartInventory = () => {
  const chartRef = useRef(null);
  const [series, setSeries] = useState([]);
  const [hasData, setHasData] = useState(false);

  useEffect(() => {
    axios.get('http://localhost:8080/api/stocks/chart')
      .then(response => {
        if (response.data && response.data.length > 0) {
          const chartData = response.data.map(item => ({
            name: item.carModel,
            data: item.monthlyStock
          }));
          setSeries(chartData);
          setHasData(true);
        } else {
          console.warn("데이터가 비어 있습니다.");
        }
      })
      .catch(error => {
        console.error("백엔드에서 차트 데이터를 불러오지 못했습니다:", error);
        setHasData(false);
      });
  }, []);

  useEffect(() => {
    if (!chartRef.current || !hasData || series.length === 0) return;

    const chart = new ApexCharts(chartRef.current, {
      chart: {
        type: "line",
        fontFamily: 'inherit',
        height: 400,
        parentHeightOffset: 0,
        toolbar: { show: false },
        animations: { enabled: false }
      },
      stroke: {
        width: 2,
        lineCap: "round",
        curve: "straight"
      },
      series: series,
      tooltip: {
        theme: 'dark'
      },
      grid: {
        padding: { top: -20, right: 0, left: -4, bottom: -4 },
        strokeDashArray: 4
      },
      xaxis: {
        categories: [
          '1월', '2월', '3월', '4월', '5월', '6월',
          '7월', '8월', '9월', '10월', '11월', '12월'
        ],
        labels: { padding: 0 },
        tooltip: { enabled: false }
      },
      yaxis: {
        tickAmount: 8,
        min: 0,
        max: 100000,
        labels: {
          formatter: (val) => val.toLocaleString(),
          padding: 4
        }
      },
      colors: ['#facc15', '#10b981', '#3b82f6', '#000000', '#ef4444'],
      legend: {
        show: true,
        position: 'bottom',
        offsetY: 12,
        markers: { width: 10, height: 10, radius: 100 },
        itemMargin: { horizontal: 8, vertical: 8 }
      }
    });

    chart.render();

    return () => chart.destroy();
  }, [series, hasData]);

  return (
    <div className="card">
      <div className="card-body" style={{ height: '460px', position: 'relative', paddingTop: '50px' }}>
        {/* 좌측 상단 텍스트 */}
        <div style={{ position: 'absolute', top: '10px', left: '16px', fontWeight: 'bold' }}>부품 수주</div>

        {hasData ? (
          <div
            ref={chartRef}
            className="position-relative"
            id="chart-demo-line"
            style={{ height: '100%' }}
          ></div>
        ) : (
          <div style={{ textAlign: 'center', paddingTop: '150px', color: '#999' }}>
            데이터를 불러오는 중입니다. 잠시만 기다려 주세요.
          </div>
        )}
      </div>
    </div>
  );
};

export default ChartInventory;
