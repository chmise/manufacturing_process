import React from 'react';
import PropTypes from 'prop-types';

const TablerButton = ({ 
  children, 
  variant = 'primary', 
  size = 'md',
  icon,
  iconPosition = 'left',
  loading = false,
  disabled = false,
  outline = false,
  pill = false,
  square = false,
  className = '',
  onClick,
  type = 'button',
  ...props 
}) => {
  const getButtonClasses = () => {
    let classes = ['btn'];
    
    // Variant classes
    if (outline) {
      classes.push(`btn-outline-${variant}`);
    } else {
      classes.push(`btn-${variant}`);
    }
    
    // Size classes
    if (size !== 'md') {
      classes.push(`btn-${size}`);
    }
    
    // Shape classes
    if (pill) classes.push('btn-pill');
    if (square) classes.push('btn-square');
    
    // State classes
    if (loading) classes.push('btn-loading');
    if (disabled) classes.push('disabled');
    
    // Custom classes
    if (className) classes.push(className);
    
    return classes.join(' ');
  };

  const renderIcon = (position) => {
    if (!icon || iconPosition !== position) return null;
    
    return (
      <svg className="icon icon-tabler" width="24" height="24" viewBox="0 0 24 24" strokeWidth="2" stroke="currentColor" fill="none" strokeLinecap="round" strokeLinejoin="round">
        {icon}
      </svg>
    );
  };

  return (
    <button
      type={type}
      className={getButtonClasses()}
      onClick={onClick}
      disabled={disabled || loading}
      {...props}
    >
      {renderIcon('left')}
      {children && <span>{children}</span>}
      {renderIcon('right')}
    </button>
  );
};

TablerButton.propTypes = {
  children: PropTypes.node,
  variant: PropTypes.oneOf(['primary', 'secondary', 'success', 'warning', 'danger', 'info', 'light', 'dark']),
  size: PropTypes.oneOf(['xs', 'sm', 'md', 'lg', 'xl']),
  icon: PropTypes.node,
  iconPosition: PropTypes.oneOf(['left', 'right']),
  loading: PropTypes.bool,
  disabled: PropTypes.bool,
  outline: PropTypes.bool,
  pill: PropTypes.bool,
  square: PropTypes.bool,
  className: PropTypes.string,
  onClick: PropTypes.func,
  type: PropTypes.oneOf(['button', 'submit', 'reset'])
};

export default TablerButton;