import React, { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import { SampleCard } from '../ui/SampleBadge';
import useApi from '../../hooks/useApi';
import { useCustomizationContext } from '../../hooks/useCustomization';

/**
 * 중소기업용 간단한 주간 일정 관리 컴포넌트
 * 복잡한 ERP 대신 직관적인 주간 스케줄링
 */
const SimpleWeeklySchedule = ({ companyName, isSampleMode = false }) => {
  const { getIndustryTheme } = useCustomizationContext();
  const industry = getIndustryTheme();
  
  const [selectedDate, setSelectedDate] = useState(new Date());
  const [showAddModal, setShowAddModal] = useState(false);
  const [selectedSchedule, setSelectedSchedule] = useState(null);
  const [weekOffset, setWeekOffset] = useState(0);

  // 현재 주 계산
  const getCurrentWeek = () => {
    const now = new Date();
    const week = new Date(now);
    week.setDate(now.getDate() - now.getDay() + (weekOffset * 7)); // 주 시작을 일요일로
    return week;
  };

  const currentWeekStart = getCurrentWeek();
  
  // 주간 일정 데이터 조회
  const { data: schedules, loading, error, refetch } = useApi(
    `/api/${companyName}/scheduling/weekly`,
    {
      immediate: true,
      refreshInterval: 60000, // 1분마다 새로고침
      params: {
        startDate: currentWeekStart.toISOString().split('T')[0],
        endDate: new Date(currentWeekStart.getTime() + 6 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
      },
      transform: (data) => {
        if (!Array.isArray(data)) return [];
        
        return data.map(schedule => ({
          ...schedule,
          startTime: new Date(schedule.startDateTime),
          endTime: new Date(schedule.endDateTime),
          duration: Math.round((new Date(schedule.endDateTime) - new Date(schedule.startDateTime)) / (1000 * 60 * 60)), // 시간 단위
          statusColor: getStatusColor(schedule.status)
        }));
      }
    }
  );

  const getStatusColor = (status) => {
    const colors = {
      'SCHEDULED': 'primary',
      'IN_PROGRESS': 'warning',
      'COMPLETED': 'success',
      'DELAYED': 'danger',
      'CANCELLED': 'secondary'
    };
    return colors[status] || 'secondary';
  };

  const getStatusText = (status) => {
    const texts = {
      'SCHEDULED': '예정',
      'IN_PROGRESS': '진행중',
      'COMPLETED': '완료',
      'DELAYED': '지연',
      'CANCELLED': '취소'
    };
    return texts[status] || status;
  };

  // 요일별 날짜 생성
  const getWeekDays = () => {
    const days = [];
    const weekStart = getCurrentWeek();
    
    for (let i = 0; i < 7; i++) {
      const day = new Date(weekStart);
      day.setDate(weekStart.getDate() + i);
      days.push(day);
    }
    return days;
  };

  const weekDays = getWeekDays();
  const dayNames = ['일', '월', '화', '수', '목', '금', '토'];

  // 날짜별 일정 그룹화
  const getSchedulesForDay = (date) => {
    if (!schedules) return [];
    
    return schedules.filter(schedule => {
      const scheduleDate = schedule.startTime.toDateString();
      const targetDate = date.toDateString();
      return scheduleDate === targetDate;
    }).sort((a, b) => a.startTime - b.startTime);
  };

  const handlePrevWeek = () => {
    setWeekOffset(weekOffset - 1);
  };

  const handleNextWeek = () => {
    setWeekOffset(weekOffset + 1);
  };

  const handleThisWeek = () => {
    setWeekOffset(0);
  };

  const handleScheduleClick = (schedule) => {
    setSelectedSchedule(schedule);
    // 여기서 상세 정보 모달이나 편집 화면을 열 수 있음
  };

  if (loading) {
    return (
      <div className="card">
        <div className="card-body text-center py-5">
          <div className="spinner-border text-primary" role="status">
            <span className="visually-hidden">로딩중...</span>
          </div>
          <p className="mt-3 text-muted">주간 일정을 불러오는 중...</p>
        </div>
      </div>
    );
  }

  const cardContent = (
    <div className="card">
      <div className="card-header d-flex justify-content-between align-items-center">
        <div>
          <h5 className="card-title mb-0">
            <i className="fas fa-calendar-week me-2 text-primary"></i>
            주간 생산 일정
          </h5>
          <small className="text-muted">
            {currentWeekStart.toLocaleDateString()} ~ {new Date(currentWeekStart.getTime() + 6 * 24 * 60 * 60 * 1000).toLocaleDateString()}
          </small>
        </div>
        <div className="d-flex gap-2">
          <button 
            className="btn btn-sm btn-outline-secondary"
            onClick={handlePrevWeek}
          >
            <i className="fas fa-chevron-left"></i>
          </button>
          <button 
            className="btn btn-sm btn-outline-primary"
            onClick={handleThisWeek}
          >
            이번 주
          </button>
          <button 
            className="btn btn-sm btn-outline-secondary"
            onClick={handleNextWeek}
          >
            <i className="fas fa-chevron-right"></i>
          </button>
          <button 
            className="btn btn-sm btn-primary"
            onClick={() => setShowAddModal(true)}
          >
            <i className="fas fa-plus me-1"></i>
            일정 추가
          </button>
        </div>
      </div>

      <div className="card-body p-0">
        {/* 주간 캘린더 그리드 */}
        <div className="table-responsive">
          <table className="table table-bordered mb-0">
            <thead className="bg-light">
              <tr>
                {weekDays.map((day, index) => (
                  <th key={index} className="text-center" style={{ width: '14.28%' }}>
                    <div className="d-flex flex-column align-items-center">
                      <span className="fw-bold">{dayNames[index]}</span>
                      <span className={`small ${day.toDateString() === new Date().toDateString() ? 'text-primary fw-bold' : 'text-muted'}`}>
                        {day.getDate()}
                      </span>
                    </div>
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              <tr style={{ height: '400px' }}>
                {weekDays.map((day, index) => {
                  const daySchedules = getSchedulesForDay(day);
                  const isToday = day.toDateString() === new Date().toDateString();
                  
                  return (
                    <td key={index} className={`align-top p-2 ${isToday ? 'bg-light-primary' : ''}`}>
                      <div className="d-flex flex-column gap-1" style={{ minHeight: '380px' }}>
                        {daySchedules.length > 0 ? (
                          daySchedules.map((schedule, scheduleIndex) => (
                            <div
                              key={schedule.id || scheduleIndex}
                              className={`card border-${schedule.statusColor} cursor-pointer`}
                              style={{ fontSize: '12px' }}
                              onClick={() => handleScheduleClick(schedule)}
                            >
                              <div className="card-body p-2">
                                <div className={`badge bg-${schedule.statusColor} mb-1`}>
                                  {getStatusText(schedule.status)}
                                </div>
                                <div className="fw-bold text-truncate" title={schedule.productName}>
                                  {schedule.productName}
                                </div>
                                <div className="text-muted small">
                                  {schedule.startTime.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' })} - 
                                  {schedule.endTime.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' })}
                                </div>
                                <div className="text-muted small">
                                  <i className="fas fa-box me-1"></i>
                                  목표: {schedule.targetQuantity}개
                                </div>
                                {schedule.assignedWorker && (
                                  <div className="text-muted small">
                                    <i className="fas fa-user me-1"></i>
                                    {schedule.assignedWorker}
                                  </div>
                                )}
                              </div>
                            </div>
                          ))
                        ) : (
                          <div className="text-center text-muted py-4">
                            <i className="fas fa-calendar-plus fa-2x mb-2 d-block opacity-25"></i>
                            <small>일정 없음</small>
                          </div>
                        )}
                      </div>
                    </td>
                  );
                })}
              </tr>
            </tbody>
          </table>
        </div>

        {/* 요약 통계 */}
        <div className="border-top p-3">
          <div className="row text-center">
            <div className="col-md-3">
              <div className="h4 text-primary mb-0">
                {schedules ? schedules.filter(s => s.status === 'SCHEDULED').length : 0}
              </div>
              <small className="text-muted">예정된 작업</small>
            </div>
            <div className="col-md-3">
              <div className="h4 text-warning mb-0">
                {schedules ? schedules.filter(s => s.status === 'IN_PROGRESS').length : 0}
              </div>
              <small className="text-muted">진행 중</small>
            </div>
            <div className="col-md-3">
              <div className="h4 text-success mb-0">
                {schedules ? schedules.filter(s => s.status === 'COMPLETED').length : 0}
              </div>
              <small className="text-muted">완료</small>
            </div>
            <div className="col-md-3">
              <div className="h4 text-danger mb-0">
                {schedules ? schedules.filter(s => s.status === 'DELAYED').length : 0}
              </div>
              <small className="text-muted">지연</small>
            </div>
          </div>
        </div>

        {/* 도움말 */}
        <div className="alert alert-light mx-3 mb-3">
          <div className="d-flex align-items-center">
            <i className="fas fa-lightbulb text-warning me-2"></i>
            <div>
              <strong>💡 일정 관리 팁</strong>
              <div className="small text-muted mt-1">
                • 일정을 클릭하면 상세 정보를 확인할 수 있습니다<br/>
                • 작업자와 시간을 미리 배정하여 효율성을 높이세요<br/>
                • 지연되는 작업은 빨간색으로 표시됩니다
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );

  // 샘플 모드인 경우 SampleCard로 래핑
  if (isSampleMode) {
    return (
      <SampleCard title="주간 생산 일정 (샘플 데이터)">
        {cardContent.props.children}
      </SampleCard>
    );
  }

  return cardContent;
};

SimpleWeeklySchedule.propTypes = {
  companyName: PropTypes.string.isRequired,
  isSampleMode: PropTypes.bool
};

export default SimpleWeeklySchedule;