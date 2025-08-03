import React from 'react';
import PropTypes from 'prop-types';

const TablerCard = ({ 
  children,
  title,
  subtitle,
  actions,
  status,
  statusPosition = 'top',
  className = '',
  headerClassName = '',
  bodyClassName = '',
  size = 'md',
  ...props 
}) => {
  const getCardClasses = () => {
    let classes = ['card'];
    
    if (size !== 'md') {
      classes.push(`card-${size}`);
    }
    
    if (status) {
      classes.push(`card-status-${statusPosition}`);
    }
    
    if (className) {
      classes.push(className);
    }
    
    return classes.join(' ');
  };

  const renderHeader = () => {
    if (!title && !subtitle && !actions) return null;
    
    return (
      <div className={`card-header ${headerClassName}`}>
        <div>
          {title && <h3 className="card-title">{title}</h3>}
          {subtitle && <p className="card-subtitle">{subtitle}</p>}
        </div>
        {actions && (
          <div className="card-actions">
            {actions}
          </div>
        )}
      </div>
    );
  };

  return (
    <div 
      className={getCardClasses()} 
      style={status ? { '--tblr-card-status-color': status } : {}}
      {...props}
    >
      {renderHeader()}
      <div className={`card-body ${bodyClassName}`}>
        {children}
      </div>
    </div>
  );
};

TablerCard.propTypes = {
  children: PropTypes.node.isRequired,
  title: PropTypes.node,
  subtitle: PropTypes.node,
  actions: PropTypes.node,
  status: PropTypes.string,
  statusPosition: PropTypes.oneOf(['top', 'start', 'end', 'bottom']),
  className: PropTypes.string,
  headerClassName: PropTypes.string,
  bodyClassName: PropTypes.string,
  size: PropTypes.oneOf(['sm', 'md', 'lg'])
};

export default TablerCard;