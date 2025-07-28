import React from 'react';

const HoverTooltip = ({ isVisible, position, data, type }) => {
  if (!isVisible || !data) return null;

  const getTooltipContent = () => {
    switch (type) {
      case 'robot':
        return (
          <div>
            <div style={{ fontWeight: 'bold', marginBottom: '4px' }}>
              🤖 로봇 {data.robotId || 'N/A'}
            </div>
            <div style={{ fontSize: '12px', color: '#666' }}>
              상태: {data.status || '운영 중'}
            </div>
          </div>
        );
      
      case 'station':
        return (
          <div>
            <div style={{ fontWeight: 'bold', marginBottom: '4px' }}>
              🏭 공정 {data.stationCode || 'N/A'}
            </div>
            <div style={{ fontSize: '12px', color: '#666' }}>
              상태: {data.status || '가동 중'}
            </div>
          </div>
        );
      
      case 'product':
        return (
          <div>
            <div style={{ fontWeight: 'bold', marginBottom: '4px' }}>
              🚗 차량 {data.productId || 'N/A'}
            </div>
            <div style={{ fontSize: '12px', color: '#666' }}>
              위치: {data.currentStation || '이동 중'}
            </div>
          </div>
        );
      
      default:
        return (
          <div>
            <div style={{ fontWeight: 'bold' }}>오브젝트 정보</div>
            <div style={{ fontSize: '12px', color: '#666' }}>클릭하여 자세히 보기</div>
          </div>
        );
    }
  };

  return (
    <div
      style={{
        position: 'fixed',
        left: position.x + 10,
        top: position.y - 10,
        zIndex: 9999,
        background: 'linear-gradient(135deg, rgba(0, 150, 255, 0.15), rgba(0, 100, 200, 0.25))',
        backdropFilter: 'blur(10px)',
        WebkitBackdropFilter: 'blur(10px)',
        border: '1px solid rgba(0, 150, 255, 0.3)',
        borderRadius: '12px',
        padding: '10px 14px',
        fontSize: '12px',
        fontWeight: '500',
        color: '#ffffff',
        textShadow: '0 1px 2px rgba(0, 0, 0, 0.5)',
        boxShadow: `
          0 8px 32px rgba(0, 150, 255, 0.2),
          0 2px 8px rgba(0, 0, 0, 0.3),
          inset 0 1px 0 rgba(255, 255, 255, 0.1)
        `,
        pointerEvents: 'none',
        maxWidth: '220px',
        transform: 'translate(-50%, -100%)', // 중앙 정렬 + 위쪽 배치
        animation: 'hoverTooltipFadeIn 0.2s ease-out',
        minWidth: '120px'
      }}
    >
      {getTooltipContent()}
      
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
      
      {/* 화살표 */}
      <div
        style={{
          position: 'absolute',
          bottom: '-8px',
          left: '50%',
          transform: 'translateX(-50%)',
          width: '0',
          height: '0',
          borderLeft: '8px solid transparent',
          borderRight: '8px solid transparent',
          borderTop: '8px solid rgba(0, 150, 255, 0.25)',
          filter: 'drop-shadow(0 2px 4px rgba(0, 0, 0, 0.3))'
        }}
      />
      
      {/* 화살표 내부 */}
      <div
        style={{
          position: 'absolute',
          bottom: '-6px',
          left: '50%',
          transform: 'translateX(-50%)',
          width: '0',
          height: '0',
          borderLeft: '6px solid transparent',
          borderRight: '6px solid transparent',
          borderTop: '6px solid rgba(0, 150, 255, 0.15)'
        }}
      />
      
      <style jsx>{`
        @keyframes hoverTooltipFadeIn {
          from {
            opacity: 0;
            transform: translate(-50%, -90%) scale(0.95);
          }
          to {
            opacity: 1;
            transform: translate(-50%, -100%) scale(1);
          }
        }
      `}</style>
    </div>
  );
};

export default HoverTooltip;