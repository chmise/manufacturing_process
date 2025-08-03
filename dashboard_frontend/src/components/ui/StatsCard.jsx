import React from 'react';
import PropTypes from 'prop-types';
import TablerCard from './TablerCard';

const StatsCard = ({
  title,
  value,
  trend,
  trendValue,
  icon,
  color = 'primary',
  industry,
  loading = false,
  className = '',
  ...props
}) => {
  const getColorClass = () => {
    if (industry) {
      return `industry-${industry}`;
    }
    return `text-${color}`;
  };

  const getTrendClass = () => {
    if (!trend) return 'neutral';
    if (trend > 0) return 'positive';
    if (trend < 0) return 'negative';
    return 'neutral';
  };

  const formatTrendValue = () => {
    if (!trendValue) return '';
    const sign = trendValue > 0 ? '+' : '';
    return `${sign}${trendValue}%`;
  };

  const renderIcon = () => {
    if (!icon) return null;
    
    return (
      <div className={`stats-icon ${getColorClass()}`}>
        <svg className="icon icon-tabler" width="24" height="24" viewBox="0 0 24 24" strokeWidth="2" stroke="currentColor" fill="none" strokeLinecap="round" strokeLinejoin="round">
          {icon}
        </svg>
      </div>
    );
  };

  const renderContent = () => {
    if (loading) {
      return (
        <div className="d-flex justify-content-center align-items-center" style={{ height: '80px' }}>
          <div className="spinner-border spinner-border-sm text-muted" role="status">
            <span className="visually-hidden">Loading...</span>
          </div>
        </div>
      );
    }

    return (
      <>
        <div className="d-flex align-items-center mb-2">
          {renderIcon()}
          <div className="ms-3 flex-grow-1">
            <h2 className={`kpi-value ${getColorClass()} mb-0`}>
              {typeof value === 'number' ? value.toLocaleString() : value}
            </h2>
          </div>
        </div>
        
        <div className="d-flex justify-content-between align-items-center">
          <p className="kpi-label mb-0">{title}</p>
          {(trend !== undefined || trendValue) && (
            <small className={`kpi-trend ${getTrendClass()}`}>
              {formatTrendValue()}
              {trend > 0 && ' ↗'}
              {trend < 0 && ' ↘'}
              {trend === 0 && ' →'}
            </small>
          )}
        </div>
      </>
    );
  };

  return (
    <TablerCard 
      className={`stats-card ${industry ? `industry-${industry}` : ''} ${className}`}
      {...props}
    >
      {renderContent()}
    </TablerCard>
  );
};

StatsCard.propTypes = {
  title: PropTypes.string.isRequired,
  value: PropTypes.oneOfType([PropTypes.number, PropTypes.string]).isRequired,
  trend: PropTypes.number,
  trendValue: PropTypes.number,
  icon: PropTypes.node,
  color: PropTypes.oneOf(['primary', 'secondary', 'success', 'warning', 'danger', 'info']),
  industry: PropTypes.oneOf(['automotive', 'electronics', 'food', 'chemical', 'textile', 'metal']),
  loading: PropTypes.bool,
  className: PropTypes.string
};

export default StatsCard;