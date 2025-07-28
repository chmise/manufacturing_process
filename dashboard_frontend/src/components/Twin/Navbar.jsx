import React from 'react';
import ProductionTarget from '../KPI/ProductionTarget';
import HourlyProduction from '../KPI/HourlyProduction';
import CycleTime from '../KPI/CycleTime';
import ExternalEnvironment from '../Environment/ExternalEnvironment';
import InternalEnvironment from '../Environment/InternalEnvironment';

const Navbar = ({ selectedProduct }) => {  
  return (
    <div className="d-flex flex-column h-100">
      {/* 환경 카드들 - 더 작은 높이 */}
      <div style={{ flex: '0.8 1 0', marginBottom: '0.8rem' }}>
        <ExternalEnvironment />
      </div>
      
      <div style={{ flex: '0.8 1 0', marginBottom: '0.8rem' }}>
        <InternalEnvironment />
      </div>
      
      {/* 생산 목표 - 일반 높이 */}
      <div style={{ flex: '0.8 1 0'}}>
        <ProductionTarget />
      </div>
      
      {/* 하단 시간당 생산수 + 사이클 타임 - 디지털 트윈 하단에 맞춤 */}
      <div style={{ flex: '0.5 1 0' }}>
        <div className="d-flex h-100" style={{ gap: '0.5rem' }}>
          <div className="w-50">
            <HourlyProduction />
          </div>
          <div className="w-50">
            <CycleTime />
          </div>
        </div>
      </div>
    </div>
  );
}

export default Navbar;