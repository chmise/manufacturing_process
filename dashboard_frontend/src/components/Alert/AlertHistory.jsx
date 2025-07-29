import React, { useState } from 'react';
import './AlertHistory.css';

const AlertHistory = ({ alertHistory, clearHistory, removeIndividualAlert }) => {
  const [isOpen, setIsOpen] = useState(false);

  const getAlertIcon = (alertType) => {
    switch (alertType) {
      case 'TEMPERATURE':
        return (
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M14 4v10.54a4 4 0 1 1-4 0V4a2 2 0 0 1 4 0Z"/>
          </svg>
        );
      case 'HUMIDITY':
        return (
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M7 16.3c2.2 0 4-1.83 4-4.05 0-1.16-.57-2.26-1.71-3.19S7.29 6.75 7 5.3c-.29 1.45-1.14 2.84-2.29 3.76S3 11.1 3 12.25c0 2.22 1.8 4.05 4 4.05z"/>
          </svg>
        );
      case 'AIR_QUALITY':
        return (
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M8 19a4 4 0 0 1-4-4V9a4 4 0 0 1 4-4h1l2-3h2l2 3h1a4 4 0 0 1 4 4v6a4 4 0 0 1-4 4"/>
            <circle cx="12" cy="13" r="3"/>
          </svg>
        );
      case 'DELAY':
        return (
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <circle cx="12" cy="12" r="10"/>
            <polyline points="12,6 12,12 16,14"/>
          </svg>
        );
      case 'OEE_LOW':
      case 'FTY_LOW':
      case 'OTD_LOW':
        return (
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M3 3v18h18"/>
            <path d="m19 9-5 5-4-4-3 3"/>
            <circle cx="12" cy="12" r="1"/>
          </svg>
        );
      case 'OEE_EXCELLENT':
        return (
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M3 3v18h18"/>
            <path d="m4 15 4-6 4 2 5-4"/>
            <circle cx="18" cy="9" r="1"/>
          </svg>
        );
      default:
        return (
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M3 12a9 9 0 1 0 18 0a9 9 0 0 0 -18 0"/>
            <path d="M12 8v4"/>
            <path d="M12 16h.01"/>
          </svg>
        );
    }
  };

  const getAlertTypeColor = (alertType) => {
    switch (alertType) {
      case 'TEMPERATURE': return 'text-danger';
      case 'HUMIDITY': return 'text-info';
      case 'AIR_QUALITY': return 'text-warning';
      case 'DELAY': return 'text-secondary';
      case 'OEE_LOW':
      case 'FTY_LOW':
      case 'OTD_LOW': return 'text-danger';
      case 'OEE_EXCELLENT': return 'text-success';
      default: return 'text-muted';
    }
  };

  const formatTime = (timestamp) => {
    if (!timestamp) return '';
    const date = new Date(timestamp);
    return date.toLocaleString('ko-KR', {
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    });
  };

  return (
    <div className="nav-item dropdown me-3">
      <a 
        href="#" 
        className="nav-link position-relative" 
        data-bs-toggle="dropdown" 
        aria-label="알림 이력"
        onClick={(e) => {
          e.preventDefault();
          setIsOpen(!isOpen);
        }}
      >
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="icon">
          <path d="M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9"/>
          <path d="M10.3 21a1.94 1.94 0 0 0 3.4 0"/>
        </svg>
        {alertHistory.length > 0 && (
          <span className="badge bg-red badge-notification badge-pill">
            {alertHistory.length > 99 ? '99+' : alertHistory.length}
          </span>
        )}
      </a>
      
      <div className={`dropdown-menu dropdown-menu-end dropdown-menu-arrow alert-history-dropdown ${isOpen ? 'show' : ''}`}>
        <div className="dropdown-header d-flex justify-content-between align-items-center">
          <h6 className="mb-0">알림 이력</h6>
          {alertHistory.length > 0 && (
            <button 
              className="btn btn-sm btn-ghost-secondary"
              onClick={clearHistory}
              title="모든 알림 삭제"
            >
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M3 6h18"/>
                <path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6"/>
                <path d="M8 6V4c0-1 1-2 2-2h4c0-1 1-2 2-2v2"/>
              </svg>
            </button>
          )}
        </div>
        
        <div className="alert-history-content">
          {alertHistory.length === 0 ? (
            <div className="dropdown-item-text text-center text-muted py-4">
              <div className="mb-2">
                <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1" strokeLinecap="round" strokeLinejoin="round" className="opacity-50">
                  <path d="M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9"/>
                  <path d="M10.3 21a1.94 1.94 0 0 0 3.4 0"/>
                </svg>
              </div>
              알림 이력이 없습니다
            </div>
          ) : (
            alertHistory.map((alert, index) => (
              <div key={alert.id || index} className="dropdown-item alert-history-item">
                <div className="row align-items-center">
                  <div className="col-auto">
                    <span className={`alert-history-icon ${getAlertTypeColor(alert.alertType)}`}>
                      {getAlertIcon(alert.alertType)}
                    </span>
                  </div>
                  <div className="col">
                    <div className="alert-history-title">
                      <strong>{alert.alertType?.replace('_', ' ')}</strong>
                    </div>
                    <div className="alert-history-message text-muted small">
                      {alert.message}
                    </div>
                    <div className="alert-history-meta d-flex justify-content-between align-items-center mt-1">
                      <small className="text-muted">
                        {alert.companyName}
                      </small>
                      <small className="text-muted">
                        {formatTime(alert.timestamp)}
                      </small>
                    </div>
                  </div>
                  <div className="col-auto">
                    <button 
                      className="btn btn-sm btn-ghost-secondary"
                      onClick={(e) => {
                        e.preventDefault();
                        e.stopPropagation();
                        if (removeIndividualAlert && (alert.id || alert.alertId)) {
                          removeIndividualAlert(alert.id || alert.alertId);
                        }
                      }}
                      title="이 알림 삭제"
                    >
                      <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <path d="M18 6 6 18"/>
                        <path d="m6 6 12 12"/>
                      </svg>
                    </button>
                  </div>
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
};

export default AlertHistory;