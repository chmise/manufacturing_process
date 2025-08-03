import React from 'react';
import PropTypes from 'prop-types';

/**
 * 샘플 데이터 배지 컴포넌트
 * 차트, 테이블, 카드 등에 표시하여 샘플 데이터임을 명확히 표시
 */
const SampleBadge = ({ 
  size = 'sm', 
  position = 'top-right', 
  variant = 'sample',
  showIcon = true,
  tooltip = true,
  className = '',
  children 
}) => {
  const variants = {
    sample: 'bg-info text-white',
    demo: 'bg-success text-white',
    test: 'bg-warning text-dark',
    live: 'bg-danger text-white'
  };

  const icons = {
    sample: 'fa-flask',
    demo: 'fa-play',
    test: 'fa-vial',
    live: 'fa-broadcast-tower'
  };

  const texts = {
    sample: '샘플',
    demo: '데모',
    test: '테스트',
    live: '실시간'
  };

  const tooltips = {
    sample: '체험용 샘플 데이터입니다',
    demo: '데모 데이터입니다',
    test: '테스트 데이터입니다',
    live: '실시간 데이터입니다'
  };

  const positions = {
    'top-left': 'position-absolute top-0 start-0 translate-middle',
    'top-right': 'position-absolute top-0 end-0 translate-middle',
    'bottom-left': 'position-absolute bottom-0 start-0 translate-middle',
    'bottom-right': 'position-absolute bottom-0 end-0 translate-middle',
    'inline': ''
  };

  const sizes = {
    xs: 'fs-7',
    sm: 'fs-6',
    md: 'fs-5',
    lg: 'fs-4'
  };

  const badgeClass = `
    badge 
    ${variants[variant]} 
    ${positions[position]}
    ${sizes[size]}
    ${className}
  `.trim();

  const badge = (
    <span 
      className={badgeClass}
      style={{ 
        zIndex: 10,
        ...(position.includes('top') ? { marginTop: '8px' } : {}),
        ...(position.includes('bottom') ? { marginBottom: '8px' } : {}),
        ...(position.includes('start') ? { marginLeft: '8px' } : {}),
        ...(position.includes('end') ? { marginRight: '8px' } : {})
      }}
      {...(tooltip ? {
        'data-bs-toggle': 'tooltip',
        'data-bs-placement': 'top',
        'title': tooltips[variant]
      } : {})}
    >
      {showIcon && (
        <i className={`fas ${icons[variant]} me-1`}></i>
      )}
      {texts[variant]}
    </span>
  );

  // position이 inline인 경우 배지만 반환
  if (position === 'inline') {
    return badge;
  }

  // 상대 위치 배지인 경우 컨테이너와 함께 반환
  return (
    <div className="position-relative">
      {children}
      {badge}
    </div>
  );
};

SampleBadge.propTypes = {
  size: PropTypes.oneOf(['xs', 'sm', 'md', 'lg']),
  position: PropTypes.oneOf(['top-left', 'top-right', 'bottom-left', 'bottom-right', 'inline']),
  variant: PropTypes.oneOf(['sample', 'demo', 'test', 'live']),
  showIcon: PropTypes.bool,
  tooltip: PropTypes.bool,
  className: PropTypes.string,
  children: PropTypes.node
};

export default SampleBadge;

/**
 * 샘플 데이터 카드 래퍼 컴포넌트
 * 카드 전체를 샘플 데이터로 표시하고 스타일링
 */
export const SampleCard = ({ children, title, className = '', ...props }) => {
  return (
    <div className={`card border-info ${className}`} {...props}>
      {title && (
        <div className="card-header bg-info text-white d-flex justify-content-between align-items-center">
          <h6 className="mb-0">{title}</h6>
          <SampleBadge variant="sample" position="inline" size="xs" />
        </div>
      )}
      <div className="card-body position-relative">
        {children}
        {!title && (
          <SampleBadge variant="sample" position="top-right" />
        )}
      </div>
    </div>
  );
};

SampleCard.propTypes = {
  children: PropTypes.node.isRequired,
  title: PropTypes.string,
  className: PropTypes.string
};

/**
 * 샘플 차트 래퍼 컴포넌트
 * 차트에 샘플 배지와 워터마크 효과 적용
 */
export const SampleChart = ({ children, showWatermark = true, className = '', ...props }) => {
  return (
    <div className={`position-relative ${className}`} {...props}>
      {children}
      <SampleBadge variant="sample" position="top-right" />
      
      {showWatermark && (
        <div 
          className="position-absolute top-50 start-50 translate-middle"
          style={{
            fontSize: '4rem',
            opacity: 0.05,
            pointerEvents: 'none',
            zIndex: 1,
            transform: 'translate(-50%, -50%) rotate(-15deg)',
            fontWeight: 'bold',
            color: '#17a2b8'
          }}
        >
          SAMPLE DATA
        </div>
      )}
    </div>
  );
};

SampleChart.propTypes = {
  children: PropTypes.node.isRequired,
  showWatermark: PropTypes.bool,
  className: PropTypes.string
};