import { useEffect, useRef } from 'react'
import ApexCharts from 'apexcharts'
import { getKPIColor } from '../../utils/kpiColors'

const ProductionStatus = ({ oee = 61.2, oeeComponents = null }) => {
  const chartRef = useRef(null)

  useEffect(() => {
    const timer = setTimeout(() => {
      const chartElement = document.getElementById('chart-production-status')
      
      if (!chartElement) {
        console.error("Chart element not found")
        return
      }

      // 안전한 값 검증 및 변환
      const oeeValue = isNaN(parseFloat(oee)) || parseFloat(oee) < 0 ? 0 : parseFloat(oee);
      const remaining = 100 - oeeValue;

      // OEE 성과 구간별 색상 적용
      const gradeInfo = getKPIColor(oeeValue, 'oee');
      console.log('OEE Value:', oeeValue, 'Grade Info:', gradeInfo, 'Original oee:', oee);

      const options = {
        chart: {
          type: "donut",
          fontFamily: 'inherit',
          height: 200,
          animations: {
            enabled: true,
            easing: 'easeinout',
            speed: 800
          },
        },
        series: [oeeValue, remaining],
        labels: ["OEE", "미달성"],
        colors: [gradeInfo.color, '#e9ecef'],
        legend: {
          show: false
        },
        plotOptions: {
          pie: {
            donut: {
              size: '65%',
              labels: {
                show: true,
                total: {
                  show: true,
                  color: gradeInfo.color,
                  fontSize: '18px',
                  fontWeight: 600,
                  label: 'OEE',
                  formatter: function () {
                    return (isNaN(oeeValue) ? 0 : oeeValue).toFixed(1) + '%';
                  }
                }
              }
            }
          }
        },
        tooltip: {
          enabled: true,
          y: {
            formatter: function (val, opts) {
              if (opts.seriesIndex === 0) {
                // 실제 API에서 받은 OEE 구성요소 사용
                const availability = oeeComponents?.availability || 0;
                const performance = oeeComponents?.performance || 0;
                const quality = oeeComponents?.quality || 0;
                
                return `가동률: ${availability.toFixed(1)}%<br/>성능률: ${performance.toFixed(1)}%<br/>품질률: ${quality.toFixed(1)}%<br/>OEE: ${val.toFixed(1)}%<br/>등급: ${gradeInfo.grade}`;
              }
              return `미달성: ${val.toFixed(1)}%`;
            }
          }
        },
        dataLabels: {
          enabled: false
        },
        responsive: [{
          breakpoint: 480,
          options: {
            chart: {
              height: 180
            },
            legend: {
              fontSize: '10px'
            }
          }
        }]
      };

      try {
        if (chartRef.current) {
          chartRef.current.destroy();
        }
        
        chartRef.current = new ApexCharts(chartElement, options);
        chartRef.current.render();
      } catch (error) {
        console.error("Chart rendering error:", error);
      }
    }, 100);

    return () => {
      clearTimeout(timer);
      if (chartRef.current) {
        chartRef.current.destroy();
        chartRef.current = null;
      }
    };
  }, [oee, oeeComponents]); // oee 값과 구성요소가 변경될 때마다 차트 업데이트

  // OEE 성과 구간별 정보 (통일된 색상 유틸리티 사용)
  const textGradeInfo = getKPIColor(parseFloat(oee), 'oee');
  
  // 텍스트 색상을 CSS 클래스로 변환
  const getTextColorClass = (color) => {
    const colorMap = {
      '#28a745': 'text-success',  // 연초록
      '#007bff': 'text-primary',  // 파랑
      '#ffc107': 'text-warning',  // 노랑
      '#fd7e14': 'text-warning',  // 주황 (warning으로 대체)
      '#dc3545': 'text-danger',   // 빨강
      '#6c757d': 'text-secondary' // 회색
    };
    return colorMap[color] || 'text-muted';
  };

  return (
    <div>
      <div id="chart-production-status" className="position-relative"></div>
      
      {/* OEE 상세 정보 */}
      <div className="mt-3">
        <div className="row text-center">
          <div className="col-6">
            <div className="text-muted small">등급</div>
            <div className={`fw-bold ${getTextColorClass(textGradeInfo.color)}`}>
              {textGradeInfo.grade}
            </div>
          </div>
          <div className="col-6">
            <div className="text-muted small">목표</div>
            <div className="fw-bold text-primary">85%</div>
          </div>
        </div>
        
        {/* 진행률 바 */}
        <div className="mt-2">
          <div className="progress" style={{ height: '6px' }}>
            <div 
              className="progress-bar"
              style={{ 
                width: `${Math.min(parseFloat(oee), 100)}%`,
                backgroundColor: textGradeInfo.color
              }}
            ></div>
          </div>
          <div className="d-flex justify-content-between mt-1">
            <small className="text-muted">0%</small>
            <small className={getTextColorClass(textGradeInfo.color)}>{parseFloat(oee).toFixed(1)}%</small>
            <small className="text-muted">100%</small>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ProductionStatus;