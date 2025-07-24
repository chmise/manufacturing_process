import React, { useState, useEffect } from 'react';
import { apiService } from '../../service/apiService';

const DigitalTwinOverlay = ({ isOpen, onClose, clickType, clickData, position }) => {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (isOpen && clickData) {
      fetchData();
    }
  }, [isOpen, clickData, clickType]);

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

  // 팝오버 위치 계산
  const popoverStyle = {
    position: 'fixed',
    left: position.x,
    top: position.y - 20,
    transform: 'translateX(-50%)',
    backgroundColor: 'white',
    borderRadius: '12px',
    padding: '16px',
    minWidth: '320px',
    maxWidth: '450px',
    maxHeight: '400px',
    overflow: 'auto',
    boxShadow: '0 8px 32px rgba(0, 0, 0, 0.2), 0 0 0 1px rgba(0, 0, 0, 0.1)',
    zIndex: 1000,
    fontSize: '14px',
    border: '1px solid #e0e0e0'
  };

  return (
    <div style={popoverStyle}>
      {/* 팝오버 화살표 */}
      <div style={{
        position: 'absolute',
        bottom: '-9px',
        left: '50%',
        transform: 'translateX(-50%)',
        width: 0,
        height: 0,
        borderLeft: '9px solid transparent',
        borderRight: '9px solid transparent',
        borderTop: '9px solid white'
      }} />
      
      {/* 헤더 */}
      <div style={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: '16px',
        paddingBottom: '12px',
        borderBottom: '2px solid #f0f0f0'
      }}>
        <h3 style={{
          margin: 0,
          color: '#333',
          fontSize: '18px',
          fontWeight: 'bold'
        }}>
          {getTitle(clickType)}
        </h3>
        <button
          onClick={onClose}
          style={{
            background: 'none',
            border: 'none',
            fontSize: '20px',
            cursor: 'pointer',
            color: '#666',
            padding: '4px',
            width: '24px',
            height: '24px',
            borderRadius: '50%',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center'
          }}
          onMouseOver={(e) => e.target.style.backgroundColor = '#f0f0f0'}
          onMouseOut={(e) => e.target.style.backgroundColor = 'transparent'}
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
            color: '#666'
          }}>
            <div style={{
              width: '20px',
              height: '20px',
              border: '2px solid #f3f3f3',
              borderTop: '2px solid #3498db',
              borderRadius: '50%',
              animation: 'spin 1s linear infinite',
              margin: '0 auto 10px'
            }} />
            데이터를 불러오는 중...
          </div>
        )}

        {error && (
          <div style={{
            padding: '16px',
            backgroundColor: '#fee',
            border: '1px solid #f88',
            borderRadius: '8px',
            color: '#c33',
            textAlign: 'center'
          }}>
            <strong>오류:</strong> {error}
            <div style={{ marginTop: '8px' }}>
              <button
                onClick={fetchData}
                style={{
                  padding: '6px 12px',
                  backgroundColor: '#dc3545',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: 'pointer',
                  fontSize: '12px'
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

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
      {/* 로봇 기본 정보 */}
      <div style={{
        padding: '12px',
        backgroundColor: '#f8f9fa',
        borderRadius: '8px',
        border: '1px solid #e9ecef'
      }}>
        <div style={{ 
          fontSize: '16px', 
          fontWeight: 'bold', 
          color: '#333',
          marginBottom: '8px'
        }}>
          {data.robotName}
        </div>
        <div style={{ fontSize: '12px', color: '#666', marginBottom: '4px' }}>
          로봇 ID: {data.robotId}
        </div>
        <div style={{ fontSize: '12px', color: '#666', marginBottom: '4px' }}>
          타입: {data.robotType}
        </div>
        <div style={{ fontSize: '12px', color: '#666' }}>
          공정: {data.stationCode}
        </div>
      </div>

      {/* 상태 정보 */}
      <div style={{
        padding: '12px',
        backgroundColor: '#fff',
        borderRadius: '8px',
        border: '1px solid #e9ecef'
      }}>
        <div style={{
          display: 'flex',
          justifyContent: 'between',
          alignItems: 'center',
          marginBottom: '8px'
        }}>
          <span style={{ fontWeight: 'bold', color: '#333' }}>현재 상태:</span>
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
            <span style={{ color: '#666' }}>모터:</span>
            <span style={{ 
              marginLeft: '4px',
              color: data.motorStatus === 1 ? '#4CAF50' : '#f44336'
            }}>
              {data.motorStatus === 1 ? 'ON' : 'OFF'}
            </span>
          </div>
          <div>
            <span style={{ color: '#666' }}>LED:</span>
            <span style={{ 
              marginLeft: '4px',
              color: data.ledStatus === 1 ? '#4CAF50' : '#f44336'
            }}>
              {data.ledStatus === 1 ? 'ON' : 'OFF'}
            </span>
          </div>
        </div>
      </div>

      {/* 마지막 업데이트 */}
      {data.lastUpdate && (
        <div style={{
          fontSize: '11px',
          color: '#999',
          textAlign: 'center',
          paddingTop: '8px',
          borderTop: '1px solid #f0f0f0'
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
        backgroundColor: '#f8f9fa',
        borderRadius: '8px',
        border: '1px solid #e9ecef'
      }}>
        <div style={{ 
          fontSize: '16px', 
          fontWeight: 'bold', 
          color: '#333',
          marginBottom: '4px'
        }}>
          공정 코드: {data.stationCode}
        </div>
      </div>

      {/* 로봇 목록 */}
      {data.robots && data.robots.length > 0 && (
        <div>
          <div style={{ fontWeight: 'bold', marginBottom: '8px', color: '#333' }}>
            로봇 상태 ({data.robots.length}개)
          </div>
          {data.robots.map((robot, index) => (
            <div key={index} style={{
              padding: '8px',
              backgroundColor: '#fff',
              borderRadius: '6px',
              border: '1px solid #e9ecef',
              marginBottom: '6px'
            }}>
              <div style={{ fontSize: '14px', fontWeight: 'bold' }}>
                로봇 #{robot.robotId}
              </div>
              <div style={{ fontSize: '12px', color: '#666' }}>
                모터: {robot.motorStatus === 1 ? 'ON' : 'OFF'} | 
                LED: {robot.ledStatus === 1 ? 'ON' : 'OFF'}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* 제품 목록 */}
      {data.products && data.products.length > 0 && (
        <div>
          <div style={{ fontWeight: 'bold', marginBottom: '8px', color: '#333' }}>
            제품 현황 ({data.products.length}개)
          </div>
          {data.products.map((product, index) => (
            <div key={index} style={{
              padding: '8px',
              backgroundColor: '#fff',
              borderRadius: '6px',
              border: '1px solid #e9ecef',
              marginBottom: '6px'
            }}>
              <div style={{ fontSize: '14px', fontWeight: 'bold' }}>
                제품 ID: {product.productId}
              </div>
              <div style={{ fontSize: '12px', color: '#666' }}>
                상태: {product.status}
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

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
      {/* 제품 기본 정보 */}
      <div style={{
        padding: '12px',
        backgroundColor: '#f8f9fa',
        borderRadius: '8px',
        border: '1px solid #e9ecef'
      }}>
        <div style={{ 
          fontSize: '16px', 
          fontWeight: 'bold', 
          color: '#333',
          marginBottom: '8px'
        }}>
          제품 ID: {data.productId}
        </div>
        <div style={{ fontSize: '12px', color: '#666', marginBottom: '4px' }}>
          현재 위치: {data.stationName} ({data.stationCode})
        </div>
      </div>

      {/* 상태 정보 */}
      <div style={{
        padding: '12px',
        backgroundColor: '#fff',
        borderRadius: '8px',
        border: '1px solid #e9ecef'
      }}>
        <div style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          marginBottom: '8px'
        }}>
          <span style={{ fontWeight: 'bold', color: '#333' }}>제품 상태:</span>
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

      {/* 위치 정보 */}
      {(data.xPosition !== null || data.yPosition !== null || data.zPosition !== null) && (
        <div style={{
          padding: '12px',
          backgroundColor: '#fff',
          borderRadius: '8px',
          border: '1px solid #e9ecef'
        }}>
          <div style={{ fontWeight: 'bold', marginBottom: '8px', color: '#333' }}>
            3D 위치 좌표
          </div>
          <div style={{ display: 'flex', gap: '12px', fontSize: '12px' }}>
            <div>X: <span style={{ fontWeight: 'bold' }}>{data.xPosition?.toFixed(2) || 'N/A'}</span></div>
            <div>Y: <span style={{ fontWeight: 'bold' }}>{data.yPosition?.toFixed(2) || 'N/A'}</span></div>
            <div>Z: <span style={{ fontWeight: 'bold' }}>{data.zPosition?.toFixed(2) || 'N/A'}</span></div>
          </div>
        </div>
      )}

      {/* 마지막 업데이트 */}
      {data.lastUpdate && (
        <div style={{
          fontSize: '11px',
          color: '#999',
          textAlign: 'center',
          paddingTop: '8px',
          borderTop: '1px solid #f0f0f0'
        }}>
          마지막 업데이트: {new Date(data.lastUpdate).toLocaleString('ko-KR')}
        </div>
      )}
    </div>
  );
};

export default DigitalTwinOverlay;