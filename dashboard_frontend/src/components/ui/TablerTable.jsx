import React, { useState, useMemo } from 'react';
import PropTypes from 'prop-types';
import TablerCard from './TablerCard';
import TablerButton from './TablerButton';

const TablerTable = ({
  title,
  data = [],
  columns = [],
  loading = false,
  error = null,
  pagination = false,
  pageSize = 10,
  searchable = false,
  sortable = false,
  actions,
  className = '',
  onRowClick,
  onRowSelect,
  selectable = false,
  ...props
}) => {
  const [currentPage, setCurrentPage] = useState(1);
  const [searchTerm, setSearchTerm] = useState('');
  const [sortConfig, setSortConfig] = useState({ key: null, direction: 'asc' });
  const [selectedRows, setSelectedRows] = useState(new Set());

  // Filtering
  const filteredData = useMemo(() => {
    if (!searchTerm) return data;
    
    return data.filter(row =>
      columns.some(col => {
        const value = row[col.key];
        return value && value.toString().toLowerCase().includes(searchTerm.toLowerCase());
      })
    );
  }, [data, searchTerm, columns]);

  // Sorting
  const sortedData = useMemo(() => {
    if (!sortConfig.key) return filteredData;

    return [...filteredData].sort((a, b) => {
      const aValue = a[sortConfig.key];
      const bValue = b[sortConfig.key];

      if (aValue < bValue) {
        return sortConfig.direction === 'asc' ? -1 : 1;
      }
      if (aValue > bValue) {
        return sortConfig.direction === 'asc' ? 1 : -1;
      }
      return 0;
    });
  }, [filteredData, sortConfig]);

  // Pagination
  const paginatedData = useMemo(() => {
    if (!pagination) return sortedData;
    
    const startIndex = (currentPage - 1) * pageSize;
    return sortedData.slice(startIndex, startIndex + pageSize);
  }, [sortedData, currentPage, pageSize, pagination]);

  const totalPages = Math.ceil(sortedData.length / pageSize);

  const handleSort = (key) => {
    if (!sortable) return;
    
    setSortConfig(prev => ({
      key,
      direction: prev.key === key && prev.direction === 'asc' ? 'desc' : 'asc'
    }));
  };

  const handleRowSelect = (rowIndex, checked) => {
    const newSelected = new Set(selectedRows);
    if (checked) {
      newSelected.add(rowIndex);
    } else {
      newSelected.delete(rowIndex);
    }
    setSelectedRows(newSelected);
    onRowSelect?.(Array.from(newSelected), newSelected.has(rowIndex) ? 'select' : 'deselect');
  };

  const handleSelectAll = (checked) => {
    if (checked) {
      const allIndexes = new Set(data.map((_, index) => index));
      setSelectedRows(allIndexes);
      onRowSelect?.(Array.from(allIndexes), 'select-all');
    } else {
      setSelectedRows(new Set());
      onRowSelect?.([], 'deselect-all');
    }
  };

  const renderTableHeader = () => (
    <thead>
      <tr>
        {selectable && (
          <th style={{ width: '40px' }}>
            <input
              type="checkbox"
              className="form-check-input"
              checked={selectedRows.size === data.length && data.length > 0}
              onChange={(e) => handleSelectAll(e.target.checked)}
            />
          </th>
        )}
        {columns.map((column) => (
          <th
            key={column.key}
            className={sortable && column.sortable !== false ? 'cursor-pointer' : ''}
            onClick={() => column.sortable !== false && handleSort(column.key)}
            style={column.width ? { width: column.width } : {}}
          >
            <div className="d-flex align-items-center">
              {column.title}
              {sortable && column.sortable !== false && sortConfig.key === column.key && (
                <span className="ms-1">
                  {sortConfig.direction === 'asc' ? '↑' : '↓'}
                </span>
              )}
            </div>
          </th>
        ))}
      </tr>
    </thead>
  );

  const renderTableBody = () => {
    if (loading) {
      return (
        <tbody>
          <tr>
            <td colSpan={columns.length + (selectable ? 1 : 0)} className="text-center py-4">
              <div className="spinner-border spinner-border-sm text-primary" role="status">
                <span className="visually-hidden">Loading...</span>
              </div>
              <div className="mt-2 text-muted">데이터를 불러오는 중...</div>
            </td>
          </tr>
        </tbody>
      );
    }

    if (error) {
      return (
        <tbody>
          <tr>
            <td colSpan={columns.length + (selectable ? 1 : 0)} className="text-center py-4">
              <div className="text-danger">오류: {error}</div>
            </td>
          </tr>
        </tbody>
      );
    }

    if (paginatedData.length === 0) {
      return (
        <tbody>
          <tr>
            <td colSpan={columns.length + (selectable ? 1 : 0)} className="text-center py-4">
              <div className="text-muted">데이터가 없습니다</div>
            </td>
          </tr>
        </tbody>
      );
    }

    return (
      <tbody>
        {paginatedData.map((row, index) => {
          const originalIndex = data.indexOf(row);
          return (
            <tr
              key={originalIndex}
              className={onRowClick ? 'cursor-pointer' : ''}
              onClick={() => onRowClick?.(row, originalIndex)}
            >
              {selectable && (
                <td>
                  <input
                    type="checkbox"
                    className="form-check-input"
                    checked={selectedRows.has(originalIndex)}
                    onChange={(e) => handleRowSelect(originalIndex, e.target.checked)}
                    onClick={(e) => e.stopPropagation()}
                  />
                </td>
              )}
              {columns.map((column) => (
                <td key={column.key}>
                  {column.render 
                    ? column.render(row[column.key], row, originalIndex)
                    : row[column.key]
                  }
                </td>
              ))}
            </tr>
          );
        })}
      </tbody>
    );
  };

  const renderPagination = () => {
    if (!pagination || totalPages <= 1) return null;

    return (
      <div className="d-flex justify-content-between align-items-center mt-3">
        <div className="text-muted small">
          총 {sortedData.length}개 중 {((currentPage - 1) * pageSize) + 1}-{Math.min(currentPage * pageSize, sortedData.length)}개 표시
        </div>
        <div className="btn-group">
          <TablerButton
            variant="outline-primary"
            size="sm"
            disabled={currentPage === 1}
            onClick={() => setCurrentPage(prev => Math.max(1, prev - 1))}
          >
            이전
          </TablerButton>
          <span className="btn btn-sm btn-outline-primary disabled">
            {currentPage} / {totalPages}
          </span>
          <TablerButton
            variant="outline-primary"
            size="sm"
            disabled={currentPage === totalPages}
            onClick={() => setCurrentPage(prev => Math.min(totalPages, prev + 1))}
          >
            다음
          </TablerButton>
        </div>
      </div>
    );
  };

  const renderSearchBar = () => {
    if (!searchable) return null;

    return (
      <div className="mb-3">
        <input
          type="text"
          className="form-control"
          placeholder="검색..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
        />
      </div>
    );
  };

  return (
    <TablerCard
      title={title}
      actions={actions}
      className={`table-card ${className}`}
      {...props}
    >
      {renderSearchBar()}
      <div className="table-responsive">
        <table className="table table-vcenter">
          {renderTableHeader()}
          {renderTableBody()}
        </table>
      </div>
      {renderPagination()}
    </TablerCard>
  );
};

TablerTable.propTypes = {
  title: PropTypes.string,
  data: PropTypes.array.isRequired,
  columns: PropTypes.arrayOf(PropTypes.shape({
    key: PropTypes.string.isRequired,
    title: PropTypes.string.isRequired,
    render: PropTypes.func,
    sortable: PropTypes.bool,
    width: PropTypes.string
  })).isRequired,
  loading: PropTypes.bool,
  error: PropTypes.string,
  pagination: PropTypes.bool,
  pageSize: PropTypes.number,
  searchable: PropTypes.bool,
  sortable: PropTypes.bool,
  actions: PropTypes.node,
  className: PropTypes.string,
  onRowClick: PropTypes.func,
  onRowSelect: PropTypes.func,
  selectable: PropTypes.bool
};

export default TablerTable;