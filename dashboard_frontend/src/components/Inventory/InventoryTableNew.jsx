import React, { useState } from 'react';
import PropTypes from 'prop-types';
import TablerTable from '../ui/TablerTable';
import TablerButton from '../ui/TablerButton';
import TablerModal, { ModalFooter } from '../ui/TablerModal';
import useApi from '../../hooks/useApi';
import { useCustomizationContext } from '../../hooks/useCustomization';

const InventoryTableNew = ({ companyId }) => {
  const { getIndustryTheme } = useCustomizationContext();
  const industry = getIndustryTheme();
  
  const [selectedStock, setSelectedStock] = useState(null);
  const [modalOpen, setModalOpen] = useState(false);

  const { data: stockData, loading, error, refetch } = useApi(
    `/api/stocks${companyId ? `?companyId=${companyId}` : ''}`,
    {
      immediate: true,
      refreshInterval: 30000, // 30초마다 새로고침
      transform: (data) => {
        if (!Array.isArray(data)) return [];
        
        return data.map(stock => ({
          ...stock,
          stockStatus: getStockStatus(stock.currentStock, stock.safetyStock),
          lastUpdated: new Date(stock.inboundDate || Date.now()).toLocaleDateString()
        }));
      }
    }
  );

  const getStockStatus = (current, safety) => {
    if (current <= 0) return { status: 'OUT_OF_STOCK', class: 'danger', text: '재고 없음' };
    if (current <= safety) return { status: 'LOW_STOCK', class: 'warning', text: '부족' };
    if (current <= safety * 1.5) return { status: 'NORMAL', class: 'info', text: '보통' };
    return { status: 'SUFFICIENT', class: 'success', text: '충분' };
  };

  const getStockBadge = (current, safety) => {
    const status = getStockStatus(current, safety);
    return <span className={`badge bg-${status.class}`}>{status.text}</span>;
  };

  const columns = [
    {
      key: 'stockCode',
      title: '재고 코드',
      width: '120px',
      render: (value) => <code className="text-primary">{value}</code>
    },
    {
      key: 'stockName',
      title: '품목명',
      width: '200px',
      sortable: true
    },
    {
      key: 'currentStock',
      title: '현재 재고',
      width: '120px',
      render: (value) => (
        <strong className={value <= 0 ? 'text-danger' : 'text-primary'}>
          {value.toLocaleString()}
        </strong>
      ),
      sortable: true
    },
    {
      key: 'safetyStock',
      title: '안전 재고',
      width: '120px',
      render: (value) => (
        <span className="text-muted">{value.toLocaleString()}</span>
      ),
      sortable: true
    },
    {
      key: 'stockStatus',
      title: '상태',
      width: '100px',
      render: (value, row) => getStockBadge(row.currentStock, row.safetyStock),
      sortable: false
    },
    {
      key: 'stockLocation',
      title: '위치',
      width: '120px'
    },
    {
      key: 'partnerCompany',
      title: '공급업체',
      width: '150px'
    },
    {
      key: 'carModel',
      title: '차종',
      width: '100px',
      render: (value) => (
        <span className="badge bg-light text-dark">{value}</span>
      )
    },
    {
      key: 'stockState',
      title: '품질 상태',
      width: '100px',
      render: (value) => {
        const stateMap = {
          '양호': { class: 'success', text: '양호' },
          '미사용': { class: 'info', text: '미사용' },
          '불량': { class: 'danger', text: '불량' }
        };
        const state = stateMap[value] || { class: 'secondary', text: value };
        return <span className={`badge bg-${state.class}`}>{state.text}</span>;
      }
    },
    {
      key: 'lastUpdated',
      title: '마지막 업데이트',
      width: '150px',
      render: (value, row) => (
        <small className="text-muted">{row.lastUpdated}</small>
      )
    }
  ];

  const handleRowClick = (stock) => {
    setSelectedStock(stock);
    setModalOpen(true);
  };

  const handleStockAction = (action, stock) => {
    console.log(`Stock ${action}:`, stock);
    // 실제 재고 관리 로직 구현
    setModalOpen(false);
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
        variant="outline-success"
        size="sm"
        onClick={() => console.log('Add new stock')}
      >
        재고 추가
      </TablerButton>
      <TablerButton
        variant="outline-primary"
        size="sm"
        onClick={() => console.log('Export stocks')}
      >
        내보내기
      </TablerButton>
    </div>
  );

  return (
    <>
      <TablerTable
        title="재고 현황"
        data={stockData || []}
        columns={columns}
        loading={loading}
        error={error}
        pagination={true}
        pageSize={15}
        searchable={true}
        sortable={true}
        actions={tableActions}
        onRowClick={handleRowClick}
        className={industry ? `industry-${industry}` : ''}
      />

      <TablerModal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        title="재고 상세 정보"
        size="lg"
        footer={
          <ModalFooter>
            <TablerButton
              variant="secondary"
              onClick={() => setModalOpen(false)}
            >
              닫기
            </TablerButton>
            <TablerButton
              variant="warning"
              onClick={() => handleStockAction('adjust', selectedStock)}
            >
              재고 조정
            </TablerButton>
            <TablerButton
              variant="success"
              onClick={() => handleStockAction('reorder', selectedStock)}
            >
              발주 요청
            </TablerButton>
          </ModalFooter>
        }
      >
        {selectedStock && (
          <div className="row">
            <div className="col-md-6">
              <table className="table table-borderless">
                <tbody>
                  <tr>
                    <td><strong>재고 코드:</strong></td>
                    <td><code>{selectedStock.stockCode}</code></td>
                  </tr>
                  <tr>
                    <td><strong>품목명:</strong></td>
                    <td>{selectedStock.stockName}</td>
                  </tr>
                  <tr>
                    <td><strong>현재 재고:</strong></td>
                    <td className={selectedStock.currentStock <= 0 ? 'text-danger' : 'text-primary'}>
                      <strong>{selectedStock.currentStock.toLocaleString()}</strong>
                    </td>
                  </tr>
                  <tr>
                    <td><strong>안전 재고:</strong></td>
                    <td>{selectedStock.safetyStock.toLocaleString()}</td>
                  </tr>
                </tbody>
              </table>
            </div>
            <div className="col-md-6">
              <table className="table table-borderless">
                <tbody>
                  <tr>
                    <td><strong>위치:</strong></td>
                    <td>{selectedStock.stockLocation}</td>
                  </tr>
                  <tr>
                    <td><strong>공급업체:</strong></td>
                    <td>{selectedStock.partnerCompany}</td>
                  </tr>
                  <tr>
                    <td><strong>차종:</strong></td>
                    <td><span className="badge bg-light text-dark">{selectedStock.carModel}</span></td>
                  </tr>
                  <tr>
                    <td><strong>품질 상태:</strong></td>
                    <td>{getStockBadge(selectedStock.currentStock, selectedStock.safetyStock)}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        )}
      </TablerModal>
    </>
  );
};

InventoryTableNew.propTypes = {
  companyId: PropTypes.oneOfType([PropTypes.string, PropTypes.number])
};

export default InventoryTableNew;