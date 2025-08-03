import React from 'react';

export const Progress = ({ value = 0, className = '', ...props }) => {
  return (
    <div className={`w-full bg-gray-200 rounded-full h-2 ${className}`} {...props}>
      <div 
        className="bg-blue-600 h-2 rounded-full transition-all duration-300 ease-in-out"
        style={{ width: `${Math.min(100, Math.max(0, value))}%` }}
      />
    </div>
  );
};