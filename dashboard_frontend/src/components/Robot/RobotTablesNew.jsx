import React from 'react';
import PropTypes from 'prop-types';
import TablerTable from '../ui/TablerTable';
import TablerButton from '../ui/TablerButton';
import useApi from '../../hooks/useApi';
import { useCustomizationContext } from '../../hooks/useCustomization';

const RobotTablesNew = ({ companyId }) => {
  const { getIndustryTheme } = useCustomizationContext();
  const industry = getIndustryTheme();

  const { data: robotData, loading, error, refetch } = useApi(
    `/api/robots${companyId ? `?companyId=${companyId}` : ''}`,
    {
      immediate: true,
      refreshInterval: 5000, // 5초마다 새로고침
      transform: (data) => {
        if (!Array.isArray(data)) return [];
        
        return data.map(robot => ({
          ...robot,
          statusBadge: robot.statusText,
          temperatureFormatted: `${robot.temperature}°C`,
          powerFormatted: `${robot.powerConsumption.toFixed(1)}kW`,
          qualityFormatted: `${robot.quality.toFixed(1)}%`,
          lastUpdated: new Date(robot.lastUpdated || Date.now()).toLocaleTimeString()
        }));
      }
    }
  );

  const getStatusBadge = (status) => {
    const statusMap = {
      'RUNNING': { class: 'badge bg-success', text: '운영중' },
      'IDLE': { class: 'badge bg-warning', text: '대기중' },
      'ERROR': { class: 'badge bg-danger', text: '오류' },
      'MAINTENANCE': { class: 'badge bg-info', text: '점검중' },
      'READY': { class: 'badge bg-primary', text: '준비' }
    };
    
    const statusInfo = statusMap[status] || { class: 'badge bg-secondary', text: status };
    return <span className={statusInfo.class}>{statusInfo.text}</span>;
  };

  const getQualityColor = (quality) => {
    if (quality >= 98) return 'text-success';
    if (quality >= 95) return 'text-warning';
    return 'text-danger';
  };

  const columns = [
    {
      key: 'robotName',
      title: '로봇명',
      width: '200px'
    },
    {
      key: 'robotId',
      title: '로봇 ID',
      width: '150px',
      render: (value) => <code className="text-muted">{value}</code>
    },
    {
      key: 'statusText',
      title: '상태',
      width: '120px',
      render: (value) => getStatusBadge(value),
      sortable: true
    },
    {
      key: 'cycleTime',
      title: '사이클 타임',
      width: '120px',
      render: (value) => `${value}초`,
      sortable: true
    },
    {
      key: 'productionCount',
      title: '생산량',
      width: '100px',
      render: (value) => value.toLocaleString(),
      sortable: true
    },
    {
      key: 'quality',
      title: '품질률',
      width: '100px',
      render: (value) => (
        <span className={getQualityColor(value)}>
          {value.toFixed(1)}%
        </span>
      ),
      sortable: true
    },
    {
      key: 'temperature',
      title: '온도',
      width: '100px',
      render: (value) => `${value}°C`,
      sortable: true
    },
    {
      key: 'powerConsumption',
      title: '전력소비',
      width: '120px',
      render: (value) => `${value.toFixed(1)}kW`,
      sortable: true
    },
    {
      key: 'lastUpdated',
      title: '마지막 업데이트',
      width: '150px',
      render: (value, row) => (
        <small className="text-muted">{row.lastUpdated}</small>
      )
    },
    {
      key: 'actions',
      title: '액션',
      width: '120px',
      render: (value, row) => (
        <div className="btn-group">
          <TablerButton
            size="sm"
            variant="outline-primary"
            onClick={() => handleRobotControl(row.robotId, 'start')}
          >
            시작
          </TablerButton>
          <TablerButton
            size="sm"
            variant="outline-secondary"
            onClick={() => handleRobotControl(row.robotId, 'stop')}
          >
            정지
          </TablerButton>
        </div>
      ),
      sortable: false
    }
  ];

  const handleRobotControl = (robotId, action) => {
    console.log(`Robot ${robotId} ${action} command`);
    // 실제 로봇 제어 로직 구현
  };

  const handleRowClick = (robot) => {
    console.log('Robot details:', robot);
    // 로봇 상세 정보 모달 또는 페이지로 이동
  };

  const tableActions = (
    <div className="btn-group">
      <TablerButton
        variant="primary"
        size="sm"
        onClick={refetch}
        loading={loading}
      >
        새로고침
      </TablerButton>
      <TablerButton
        variant="outline-primary"
        size="sm"
        onClick={() => console.log('Export robots')}
      >
        내보내기
      </TablerButton>
    </div>
  );

  return (
    <TablerTable
      title="로봇 현황"
      data={robotData || []}
      columns={columns}
      loading={loading}
      error={error}
      pagination={true}
      pageSize={10}
      searchable={true}
      sortable={true}
      actions={tableActions}
      onRowClick={handleRowClick}
      className={industry ? `industry-${industry}` : ''}
    />
  );
};

RobotTablesNew.propTypes = {
  companyId: PropTypes.oneOfType([PropTypes.string, PropTypes.number])
};

export default RobotTablesNew;