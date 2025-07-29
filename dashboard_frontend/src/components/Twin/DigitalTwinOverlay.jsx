import React, { useState, useEffect } from 'react';
import { apiService } from '../../service/apiService';

const DigitalTwinOverlay = ({ isOpen, onClose, clickType, clickData, position }) => {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [isDragging, setIsDragging] = useState(false);
  const [dragOffset, setDragOffset] = useState({ x: 0, y: 0 });
  const [currentPosition, setCurrentPosition] = useState(position);

  useEffect(() => {
    if (isOpen && clickData) {
      fetchData();
    }
  }, [isOpen, clickData, clickType]);

  useEffect(() => {
    setCurrentPosition(position);
  }, [position]);

  // 마우스 드래그 이벤트 핸들러
  const handleMouseDown = (e) => {
    if (e.target.closest('.drag-handle')) {
      setIsDragging(true);
      const rect = e.currentTarget.getBoundingClientRect();
      setDragOffset({
        x: e.clientX - rect.left,
        y: e.clientY - rect.top
      });
    }
  };

  const handleMouseMove = (e) => {
    if (isDragging) {
      setCurrentPosition({
        x: e.clientX - dragOffset.x + (e.currentTarget.offsetWidth / 2),
        y: e.clientY - dragOffset.y + 20
      });
    }
  };

  const handleMouseUp = () => {
    setIsDragging(false);
  };

  // 전역 마우스 이벤트 리스너
  useEffect(() => {
    if (isDragging) {
      document.addEventListener('mousemove', handleMouseMove);
      document.addEventListener('mouseup', handleMouseUp);
      document.body.style.cursor = 'grabbing';
      document.body.style.userSelect = 'none';
    }

    return () => {
      document.removeEventListener('mousemove', handleMouseMove);
      document.removeEventListener('mouseup', handleMouseUp);
      document.body.style.cursor = '';
      document.body.style.userSelect = '';
    };
  }, [isDragging, dragOffset]);

  const fetchData = async () => {
    setLoading(true);
    setError(null);
    
    try {
      let result = null;
      
      switch (clickType) {
        case 'robot':
          if (clickData.robotId) {
            result = await apiService.get(`/click/robot/${clickData.robotId}`);
          }
          break;
          
        case 'station':
          if (clickData.stationCode) {
            result = await apiService.get(`/click/station/${clickData.stationCode}`);
          }
          break;
          
        case 'product':
          if (clickData.productId) {
            result = await apiService.get(`/click/product/${clickData.productId}`);
          }
          break;
          
        default:
          break;
      }
      
      setData(result);
    } catch (err) {
      console.error('Digital Twin data fetch error:', err);
      setError(err.message || '데이터를 불러올 수 없습니다.');
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  // 팝오버 위치 계산 - 반투명 글래스모피즘 스타일 적용
  const popoverStyle = {
    position: 'fixed',
    left: currentPosition.x,
    top: currentPosition.y - 20,
    transform: 'translateX(-50%)',
    background: 'linear-gradient(135deg, rgba(0, 150, 255, 0.15), rgba(0, 100, 200, 0.25))',
    backdropFilter: 'blur(10px)',
    WebkitBackdropFilter: 'blur(10px)',
    border: '1px solid rgba(0, 150, 255, 0.3)',
    borderRadius: '12px',
    padding: '16px',
    minWidth: '320px',
    maxWidth: '500px',
    maxHeight: '500px',
    overflow: 'auto',
    boxShadow: `
      0 8px 32px rgba(0, 150, 255, 0.2),
      0 2px 8px rgba(0, 0, 0, 0.3),
      inset 0 1px 0 rgba(255, 255, 255, 0.1)
    `,
    zIndex: 1000,
    fontSize: '14px',
    color: '#ffffff',
    textShadow: '0 1px 2px rgba(0, 0, 0, 0.5)',
    animation: isDragging ? 'none' : 'overlaySlideIn 0.4s ease-out',
    cursor: isDragging ? 'grabbing' : 'default'
  };

  return (
    <div 
      style={popoverStyle}
      onMouseDown={handleMouseDown}
    >
      {/* 네온 글로우 효과 */}
      <div
        style={{
          position: 'absolute',
          inset: '-1px',
          background: 'linear-gradient(135deg, rgba(0, 150, 255, 0.4), rgba(0, 100, 200, 0.2))',
          borderRadius: '12px',
          zIndex: -1,
          filter: 'blur(4px)',
          opacity: '0.6'
        }}
      />
      
      {/* 팝오버 화살표 */}
      <div style={{
        position: 'absolute',
        bottom: '-8px',
        left: '50%',
        transform: 'translateX(-50%)',
        width: 0,
        height: 0,
        borderLeft: '8px solid transparent',
        borderRight: '8px solid transparent',
        borderTop: '8px solid rgba(0, 150, 255, 0.25)',
        filter: 'drop-shadow(0 2px 4px rgba(0, 0, 0, 0.3))'
      }} />
      
      {/* 화살표 내부 */}
      <div style={{
        position: 'absolute',
        bottom: '-6px',
        left: '50%',
        transform: 'translateX(-50%)',
        width: 0,
        height: 0,
        borderLeft: '6px solid transparent',
        borderRight: '6px solid transparent',
        borderTop: '6px solid rgba(0, 150, 255, 0.15)'
      }} />
      
      {/* 헤더 */}
      <div 
        className="drag-handle"
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          marginBottom: '16px',
          paddingBottom: '12px',
          borderBottom: '1px solid rgba(0, 150, 255, 0.3)',
          cursor: isDragging ? 'grabbing' : 'grab',
          userSelect: 'none'
        }}
      >
        <h3 style={{
          margin: 0,
          color: '#ffffff',
          fontSize: '18px',
          fontWeight: 'bold',
          textShadow: '0 1px 2px rgba(0, 0, 0, 0.5)',
          background: 'linear-gradient(45deg, #ffffff, #a8d8ff)',
          WebkitBackgroundClip: 'text',
          WebkitTextFillColor: 'transparent',
          backgroundClip: 'text',
          cursor: isDragging ? 'grabbing' : 'grab',
          display: 'flex',
          alignItems: 'center',
          gap: '8px'
        }}>
          <span style={{ cursor: isDragging ? 'grabbing' : 'grab' }}>⋮⋮</span>
          {getTitle(clickType)}
        </h3>
        <button
          onClick={onClose}
          style={{
            background: 'rgba(0, 150, 255, 0.2)',
            border: '1px solid rgba(0, 150, 255, 0.4)',
            color: '#ffffff',
            fontSize: '20px',
            cursor: 'pointer',
            padding: '4px',
            width: '24px',
            height: '24px',
            borderRadius: '50%',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            backdropFilter: 'blur(10px)',
            WebkitBackdropFilter: 'blur(10px)',
            transition: 'all 0.3s ease',
            flexShrink: 0
          }}
          onMouseDown={(e) => e.stopPropagation()} // 닫기 버튼 클릭 시 드래그 방지
          onMouseOver={(e) => {
            e.target.style.background = 'rgba(0, 150, 255, 0.4)';
            e.target.style.borderColor = 'rgba(0, 200, 255, 0.6)';
            e.target.style.boxShadow = '0 4px 12px rgba(0, 150, 255, 0.3)';
            e.target.style.transform = 'scale(1.05)';
          }}
          onMouseOut={(e) => {
            e.target.style.background = 'rgba(0, 150, 255, 0.2)';
            e.target.style.borderColor = 'rgba(0, 150, 255, 0.4)';
            e.target.style.boxShadow = 'none';
            e.target.style.transform = 'scale(1)';
          }}
        >
          ×
        </button>
      </div>

      {/* 컨텐츠 */}
      <div>
        {loading && (
          <div style={{
            textAlign: 'center',
            padding: '20px',
            color: '#ffffff',
            textShadow: '0 1px 2px rgba(0, 0, 0, 0.5)'
          }}>
            <div style={{
              width: '20px',
              height: '20px',
              border: '2px solid rgba(0, 150, 255, 0.2)',
              borderTop: '2px solid #00ccff',
              borderRadius: '50%',
              animation: 'spin 1s linear infinite',
              margin: '0 auto 10px',
              boxShadow: '0 0 20px rgba(0, 150, 255, 0.3)'
            }} />
            데이터를 불러오는 중...
          </div>
        )}

        {error && (
          <div style={{
            padding: '16px',
            background: 'linear-gradient(135deg, rgba(255, 80, 80, 0.2), rgba(255, 60, 60, 0.3))',
            border: '1px solid rgba(255, 80, 80, 0.5)',
            borderRadius: '8px',
            color: '#ffffff',
            textAlign: 'center',
            backdropFilter: 'blur(10px)',
            WebkitBackdropFilter: 'blur(10px)',
            textShadow: '0 1px 2px rgba(0, 0, 0, 0.5)'
          }}>
            <strong>오류:</strong> {error}
            <div style={{ marginTop: '8px' }}>
              <button
                onClick={fetchData}
                style={{
                  padding: '6px 12px',
                  background: 'rgba(255, 80, 80, 0.3)',
                  color: '#ffffff',
                  border: '1px solid rgba(255, 80, 80, 0.5)',
                  borderRadius: '4px',
                  cursor: 'pointer',
                  fontSize: '12px',
                  backdropFilter: 'blur(10px)',
                  WebkitBackdropFilter: 'blur(10px)',
                  transition: 'all 0.3s ease'
                }}
                onMouseOver={(e) => {
                  e.target.style.background = 'rgba(255, 80, 80, 0.5)';
                  e.target.style.transform = 'scale(1.05)';
                }}
                onMouseOut={(e) => {
                  e.target.style.background = 'rgba(255, 80, 80, 0.3)';
                  e.target.style.transform = 'scale(1)';
                }}
              >
                다시 시도
              </button>
            </div>
          </div>
        )}

        {data && !loading && !error && (
          <div>
            {clickType === 'robot' && <RobotStatusDisplay data={data} />}
            {clickType === 'station' && <StationStatusDisplay data={data} />}
            {clickType === 'product' && <ProductStatusDisplay data={data} />}
          </div>
        )}
      </div>
      
      {/* CSS 애니메이션 추가 */}
      <style jsx>{`
        @keyframes spin {
          0% { transform: rotate(0deg); }
          100% { transform: rotate(360deg); }
        }
        
        @keyframes overlaySlideIn {
          from {
            opacity: 0;
            transform: translateX(-50%) translateY(-10px) scale(0.95);
          }
          to {
            opacity: 1;
            transform: translateX(-50%) translateY(-20px) scale(1);
          }
        }
      `}</style>
    </div>
  );
};

// 제목 생성 함수
const getTitle = (clickType) => {
  switch (clickType) {
    case 'robot': return '🤖 로봇 상태';
    case 'station': return '🏭 공정 상태';
    case 'product': return '📦 제품 상태';
    default: return '정보';
  }
};

// 메트릭 카드 컴포넌트
const MetricCard = ({ icon, label, value, unit, color = '#00ccff' }) => (
  <div style={{
    padding: '12px',
    background: 'linear-gradient(135deg, rgba(0, 150, 255, 0.08), rgba(0, 100, 200, 0.12))',
    borderRadius: '8px',
    border: '1px solid rgba(0, 150, 255, 0.2)',
    backdropFilter: 'blur(10px)',
    WebkitBackdropFilter: 'blur(10px)',
    textAlign: 'center',
    minWidth: '80px'
  }}>
    <div style={{ fontSize: '18px', marginBottom: '4px' }}>{icon}</div>
    <div style={{ 
      fontSize: '18px', 
      fontWeight: 'bold', 
      color: color,
      textShadow: '0 1px 2px rgba(0, 0, 0, 0.5)',
      marginBottom: '2px'
    }}>
      {value}{unit}
    </div>
    <div style={{ 
      fontSize: '11px', 
      color: '#e0f2ff',
      textShadow: '0 1px 2px rgba(0, 0, 0, 0.3)'
    }}>
      {label}
    </div>
  </div>
);

// 로봇 상태 표시 컴포넌트
const RobotStatusDisplay = ({ data }) => {
  const getStatusColor = (status) => {
    switch (status) {
      case '운영중': return '#4CAF50';
      case '정지': return '#f44336';
      case '점검중': return '#ff9800';
      default: return '#9e9e9e';
    }
  };

  const getQualityColor = (quality) => {
    if (quality >= 95) return '#4CAF50';
    if (quality >= 85) return '#ff9800';
    return '#f44336';
  };

  const getTemperatureColor = (temp) => {
    if (temp <= 30) return '#2196F3';
    if (temp <= 50) return '#4CAF50';
    if (temp <= 70) return '#ff9800';
    return '#f44336';
  };

  const getPowerColor = (power) => {
    if (power <= 50) return '#4CAF50';
    if (power <= 75) return '#ff9800';
    return '#f44336';
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
      {/* 로봇 기본 정보 */}
      <div style={{
        padding: '12px',
        background: 'linear-gradient(135deg, rgba(0, 150, 255, 0.08), rgba(0, 100, 200, 0.12))',
        borderRadius: '8px',
        border: '1px solid rgba(0, 150, 255, 0.2)',
        backdropFilter: 'blur(10px)',
        WebkitBackdropFilter: 'blur(10px)'
      }}>
        <div style={{ 
          fontSize: '16px', 
          fontWeight: 'bold', 
          color: '#ffffff',
          marginBottom: '8px',
          textShadow: '0 1px 2px rgba(0, 0, 0, 0.5)'
        }}>
          {data.robotName}
        </div>
        <div style={{ fontSize: '12px', color: '#e0f2ff', marginBottom: '4px', textShadow: '0 1px 2px rgba(0, 0, 0, 0.3)' }}>
          로봇 ID: {data.robotId}
        </div>
        <div style={{ fontSize: '12px', color: '#e0f2ff', marginBottom: '4px', textShadow: '0 1px 2px rgba(0, 0, 0, 0.3)' }}>
          타입: {data.robotType}
        </div>
        <div style={{ fontSize: '12px', color: '#e0f2ff', textShadow: '0 1px 2px rgba(0, 0, 0, 0.3)' }}>
          공정: {data.stationCode}
        </div>
      </div>

      {/* 상태 정보 */}
      <div style={{
        padding: '12px',
        background: 'linear-gradient(135deg, rgba(0, 150, 255, 0.08), rgba(0, 100, 200, 0.12))',
        borderRadius: '8px',
        border: '1px solid rgba(0, 150, 255, 0.2)',
        backdropFilter: 'blur(10px)',
        WebkitBackdropFilter: 'blur(10px)'
      }}>
        <div style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          marginBottom: '8px'
        }}>
          <span style={{ fontWeight: 'bold', color: '#ffffff', textShadow: '0 1px 2px rgba(0, 0, 0, 0.5)' }}>현재 상태:</span>
          <span style={{
            padding: '4px 8px',
            borderRadius: '12px',
            fontSize: '12px',
            fontWeight: 'bold',
            color: 'white',
            backgroundColor: getStatusColor(data.statusText)
          }}>
            {data.statusText}
          </span>
        </div>
        
        <div style={{ display: 'flex', gap: '12px', fontSize: '12px' }}>
          <div>
            <span style={{ color: '#e0f2ff', textShadow: '0 1px 2px rgba(0, 0, 0, 0.3)' }}>모터:</span>
            <span style={{ 
              marginLeft: '4px',
              color: data.motorStatus === 1 ? '#4CAF50' : '#f44336',
              fontWeight: 'bold'
            }}>
              {data.motorStatus === 1 ? 'ON' : 'OFF'}
            </span>
          </div>
          <div>
            <span style={{ color: '#e0f2ff', textShadow: '0 1px 2px rgba(0, 0, 0, 0.3)' }}>LED:</span>
            <span style={{ 
              marginLeft: '4px',
              color: data.ledStatus === 1 ? '#4CAF50' : '#f44336',
              fontWeight: 'bold'
            }}>
              {data.ledStatus === 1 ? 'ON' : 'OFF'}
            </span>
          </div>
        </div>
      </div>

      {/* 생산 메트릭 */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(2, 1fr)',
        gap: '8px'
      }}>
        <MetricCard
          icon="⏱️"
          label="사이클타임"
          value={data.cycleTime || 0}
          unit="초"
          color="#00ccff"
        />
        <MetricCard
          icon="📊"
          label="제작갯수"
          value={data.productionCount || 0}
          unit="개"
          color="#4CAF50"
        />
        <MetricCard
          icon="✅"
          label="품질"
          value={data.quality || 0}
          unit="%"
          color={getQualityColor(data.quality || 0)}
        />
        <MetricCard
          icon="🌡️"
          label="온도"
          value={data.temperature || 0}
          unit="°C"
          color={getTemperatureColor(data.temperature || 0)}
        />
      </div>

      {/* 전력량 정보 */}
      <div style={{
        padding: '12px',
        background: 'linear-gradient(135deg, rgba(0, 150, 255, 0.08), rgba(0, 100, 200, 0.12))',
        borderRadius: '8px',
        border: '1px solid rgba(0, 150, 255, 0.2)',
        backdropFilter: 'blur(10px)',
        WebkitBackdropFilter: 'blur(10px)'
      }}>
        <div style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          marginBottom: '8px'
        }}>
          <span style={{ 
            fontWeight: 'bold', 
            color: '#ffffff', 
            textShadow: '0 1px 2px rgba(0, 0, 0, 0.5)',
            display: 'flex',
            alignItems: 'center',
            gap: '6px'
          }}>
            ⚡ 전력 소비량
          </span>
          <span style={{
            fontSize: '18px',
            fontWeight: 'bold',
            color: getPowerColor(data.powerConsumption || 0),
            textShadow: '0 1px 2px rgba(0, 0, 0, 0.5)'
          }}>
            {data.powerConsumption || 0} kW
          </span>
        </div>
        
        {/* 전력 사용량 바 */}
        <div style={{
          width: '100%',
          height: '6px',
          backgroundColor: 'rgba(255, 255, 255, 0.2)',
          borderRadius: '3px',
          overflow: 'hidden'
        }}>
          <div style={{
            height: '100%',
            width: `${Math.min((data.powerConsumption || 0) / 100 * 100, 100)}%`,
            backgroundColor: getPowerColor(data.powerConsumption || 0),
            borderRadius: '3px',
            transition: 'width 0.3s ease'
          }} />
        </div>
      </div>

      {/* 마지막 업데이트 */}
      {data.lastUpdate && (
        <div style={{
          fontSize: '11px',
          color: '#b0d4ff',
          textAlign: 'center',
          paddingTop: '8px',
          borderTop: '1px solid rgba(0, 150, 255, 0.3)',
          textShadow: '0 1px 2px rgba(0, 0, 0, 0.3)'
        }}>
          마지막 업데이트: {new Date(data.lastUpdate).toLocaleString('ko-KR')}
        </div>
      )}
    </div>
  );
};

// 공정 상태 표시 컴포넌트
const StationStatusDisplay = ({ data }) => {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
      {/* 공정 정보 */}
      <div style={{
        padding: '12px',
        background: 'linear-gradient(135deg, rgba(0, 150, 255, 0.08), rgba(0, 100, 200, 0.12))',
        borderRadius: '8px',
        border: '1px solid rgba(0, 150, 255, 0.2)',
        backdropFilter: 'blur(10px)',
        WebkitBackdropFilter: 'blur(10px)'
      }}>
        <div style={{ 
          fontSize: '16px', 
          fontWeight: 'bold', 
          color: '#ffffff',
          marginBottom: '4px',
          textShadow: '0 1px 2px rgba(0, 0, 0, 0.5)'
        }}>
          공정 코드: {data.stationCode}
        </div>
        <div style={{ 
          fontSize: '12px', 
          color: '#e0f2ff',
          textShadow: '0 1px 2px rgba(0, 0, 0, 0.3)'
        }}>
          공정명: {data.stationName || '알 수 없음'}
        </div>
      </div>

      {/* 공정 통계 */}
      {(data.totalCycleTime || data.totalProduction || data.avgQuality || data.avgTemperature || data.totalPower) && (
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(2, 1fr)',
          gap: '8px'
        }}>
          <MetricCard
            icon="⏱️"
            label="평균 사이클"
            value={data.avgCycleTime || 0}
            unit="초"
            color="#00ccff"
          />
          <MetricCard
            icon="📊"
            label="총 생산량"
            value={data.totalProduction || 0}
            unit="개"
            color="#4CAF50"
          />
          <MetricCard
            icon="✅"
            label="평균 품질"
            value={data.avgQuality || 0}
            unit="%"
            color="#ff9800"
          />
          <MetricCard
            icon="⚡"
            label="총 전력량"
            value={data.totalPower || 0}
            unit="kW"
            color="#f44336"
          />
        </div>
      )}

      {/* 로봇 목록 */}
      {data.robots && data.robots.length > 0 && (
        <div>
          <div style={{ 
            fontWeight: 'bold', 
            marginBottom: '8px', 
            color: '#ffffff',
            textShadow: '0 1px 2px rgba(0, 0, 0, 0.5)'
          }}>
            로봇 상태 ({data.robots.length}개)
          </div>
          {data.robots.map((robot, index) => (
            <div key={index} style={{
              padding: '8px',
              background: 'linear-gradient(135deg, rgba(0, 150, 255, 0.05), rgba(0, 100, 200, 0.08))',
              borderRadius: '6px',
              border: '1px solid rgba(0, 150, 255, 0.2)',
              marginBottom: '6px',
              backdropFilter: 'blur(10px)',
              WebkitBackdropFilter: 'blur(10px)'
            }}>
              <div style={{ 
                fontSize: '14px', 
                fontWeight: 'bold',
                color: '#ffffff',
                textShadow: '0 1px 2px rgba(0, 0, 0, 0.5)'
              }}>
                로봇 #{robot.robotId}
              </div>
              <div style={{ 
                fontSize: '12px', 
                color: '#e0f2ff',
                textShadow: '0 1px 2px rgba(0, 0, 0, 0.3)'
              }}>
                모터: {robot.motorStatus === 1 ? 'ON' : 'OFF'} | 
                LED: {robot.ledStatus === 1 ? 'ON' : 'OFF'} |
                제작: {robot.productionCount || 0}개 |
                품질: {robot.quality || 0}%
              </div>
            </div>
          ))}
        </div>
      )}

      {/* 제품 목록 */}
      {data.products && data.products.length > 0 && (
        <div>
          <div style={{ 
            fontWeight: 'bold', 
            marginBottom: '8px', 
            color: '#ffffff',
            textShadow: '0 1px 2px rgba(0, 0, 0, 0.5)'
          }}>
            제품 현황 ({data.products.length}개)
          </div>
          {data.products.map((product, index) => (
            <div key={index} style={{
              padding: '8px',
              background: 'linear-gradient(135deg, rgba(0, 150, 255, 0.05), rgba(0, 100, 200, 0.08))',
              borderRadius: '6px',
              border: '1px solid rgba(0, 150, 255, 0.2)',
              marginBottom: '6px',
              backdropFilter: 'blur(10px)',
              WebkitBackdropFilter: 'blur(10px)'
            }}>
              <div style={{ 
                fontSize: '14px', 
                fontWeight: 'bold',
                color: '#ffffff',
                textShadow: '0 1px 2px rgba(0, 0, 0, 0.5)'
              }}>
                제품 ID: {product.productId}
              </div>
              <div style={{ 
                fontSize: '12px', 
                color: '#e0f2ff',
                textShadow: '0 1px 2px rgba(0, 0, 0, 0.3)'
              }}>
                상태: {product.status} | 품질: {product.quality || 0}%
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

// 제품 상태 표시 컴포넌트
const ProductStatusDisplay = ({ data }) => {
  const getStatusColor = (status) => {
    switch (status?.toLowerCase()) {
      case 'processing': case '가공중': return '#2196F3';
      case 'waiting': case '대기중': return '#ff9800';
      case 'completed': case '완료': return '#4CAF50';
      case 'error': case '오류': return '#f44336';
      default: return '#9e9e9e';
    }
  };

  const getQualityColor = (quality) => {
    if (quality >= 95) return '#4CAF50';
    if (quality >= 85) return '#ff9800';
    return '#f44336';
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
      {/* 제품 기본 정보 */}
      <div style={{
        padding: '12px',
        background: 'linear-gradient(135deg, rgba(0, 150, 255, 0.08), rgba(0, 100, 200, 0.12))',
        borderRadius: '8px',
        border: '1px solid rgba(0, 150, 255, 0.2)',
        backdropFilter: 'blur(10px)',
        WebkitBackdropFilter: 'blur(10px)'
      }}>
        <div style={{ 
          fontSize: '16px', 
          fontWeight: 'bold', 
          color: '#ffffff',
          marginBottom: '8px',
          textShadow: '0 1px 2px rgba(0, 0, 0, 0.5)'
        }}>
          제품 ID: {data.productId}
        </div>
        <div style={{ fontSize: '12px', color: '#e0f2ff', marginBottom: '4px', textShadow: '0 1px 2px rgba(0, 0, 0, 0.3)' }}>
          현재 위치: {data.stationName} ({data.stationCode})
        </div>
      </div>

      {/* 상태 정보 */}
      <div style={{
        padding: '12px',
        background: 'linear-gradient(135deg, rgba(0, 150, 255, 0.08), rgba(0, 100, 200, 0.12))',
        borderRadius: '8px',
        border: '1px solid rgba(0, 150, 255, 0.2)',
        backdropFilter: 'blur(10px)',
        WebkitBackdropFilter: 'blur(10px)'
      }}>
        <div style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          marginBottom: '8px'
        }}>
          <span style={{ fontWeight: 'bold', color: '#ffffff', textShadow: '0 1px 2px rgba(0, 0, 0, 0.5)' }}>제품 상태:</span>
          <span style={{
            padding: '4px 8px',
            borderRadius: '12px',
            fontSize: '12px',
            fontWeight: 'bold',
            color: 'white',
            backgroundColor: getStatusColor(data.statusText)
          }}>
            {data.statusText}
          </span>
        </div>
      </div>

      {/* 제품 품질 및 생산 정보 */}
      {(data.quality || data.processingTime || data.cycleTime) && (
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(2, 1fr)',
          gap: '8px'
        }}>
          {data.quality && (
            <MetricCard
              icon="✅"
              label="품질 점수"
              value={data.quality}
              unit="%"
              color={getQualityColor(data.quality)}
            />
          )}
          {data.processingTime && (
            <MetricCard
              icon="⏱️"
              label="가공 시간"
              value={data.processingTime}
              unit="분"
              color="#00ccff"
            />
          )}
          {data.cycleTime && (
            <MetricCard
              icon="🔄"
              label="사이클타임"
              value={data.cycleTime}
              unit="초"
              color="#4CAF50"
            />
          )}
          {data.defectCount !== undefined && (
            <MetricCard
              icon="⚠️"
              label="불량 수"
              value={data.defectCount}
              unit="개"
              color={data.defectCount > 0 ? "#f44336" : "#4CAF50"}
            />
          )}
        </div>
      )}

      {/* 위치 정보 */}
      {(data.xPosition !== null || data.yPosition !== null || data.zPosition !== null) && (
        <div style={{
          padding: '12px',
          background: 'linear-gradient(135deg, rgba(0, 150, 255, 0.08), rgba(0, 100, 200, 0.12))',
          borderRadius: '8px',
          border: '1px solid rgba(0, 150, 255, 0.2)',
          backdropFilter: 'blur(10px)',
          WebkitBackdropFilter: 'blur(10px)'
        }}>
          <div style={{ 
            fontWeight: 'bold', 
            marginBottom: '8px', 
            color: '#ffffff',
            textShadow: '0 1px 2px rgba(0, 0, 0, 0.5)'
          }}>
            📍 3D 위치 좌표
          </div>
          <div style={{ display: 'flex', gap: '12px', fontSize: '12px' }}>
            <div style={{ color: '#e0f2ff', textShadow: '0 1px 2px rgba(0, 0, 0, 0.3)' }}>
              X: <span style={{ fontWeight: 'bold', color: '#00ccff' }}>{data.xPosition?.toFixed(2) || 'N/A'}</span>
            </div>
            <div style={{ color: '#e0f2ff', textShadow: '0 1px 2px rgba(0, 0, 0, 0.3)' }}>
              Y: <span style={{ fontWeight: 'bold', color: '#00ccff' }}>{data.yPosition?.toFixed(2) || 'N/A'}</span>
            </div>
            <div style={{ color: '#e0f2ff', textShadow: '0 1px 2px rgba(0, 0, 0, 0.3)' }}>
              Z: <span style={{ fontWeight: 'bold', color: '#00ccff' }}>{data.zPosition?.toFixed(2) || 'N/A'}</span>
            </div>
          </div>
        </div>
      )}

      {/* 생산 이력 */}
      {data.productionHistory && data.productionHistory.length > 0 && (
        <div style={{
          padding: '12px',
          background: 'linear-gradient(135deg, rgba(0, 150, 255, 0.08), rgba(0, 100, 200, 0.12))',
          borderRadius: '8px',
          border: '1px solid rgba(0, 150, 255, 0.2)',
          backdropFilter: 'blur(10px)',
          WebkitBackdropFilter: 'blur(10px)'
        }}>
          <div style={{ 
            fontWeight: 'bold', 
            marginBottom: '8px', 
            color: '#ffffff',
            textShadow: '0 1px 2px rgba(0, 0, 0, 0.5)'
          }}>
            📋 생산 이력
          </div>
          {data.productionHistory.slice(0, 3).map((history, index) => (
            <div key={index} style={{
              fontSize: '11px',
              color: '#e0f2ff',
              textShadow: '0 1px 2px rgba(0, 0, 0, 0.3)',
              marginBottom: '4px',
              padding: '4px 8px',
              background: 'rgba(0, 150, 255, 0.1)',
              borderRadius: '4px'
            }}>
              {history.station} → {history.status} ({new Date(history.timestamp).toLocaleTimeString('ko-KR')})
            </div>
          ))}
        </div>
      )}

      {/* 마지막 업데이트 */}
      {data.lastUpdate && (
        <div style={{
          fontSize: '11px',
          color: '#b0d4ff',
          textAlign: 'center',
          paddingTop: '8px',
          borderTop: '1px solid rgba(0, 150, 255, 0.3)',
          textShadow: '0 1px 2px rgba(0, 0, 0, 0.3)'
        }}>
          마지막 업데이트: {new Date(data.lastUpdate).toLocaleString('ko-KR')}
        </div>
      )}
    </div>
  );
};

export default DigitalTwinOverlay;