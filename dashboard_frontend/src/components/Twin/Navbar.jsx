import React from 'react';
import ProductionTarget from '../KPI/ProductionTarget';
import HourlyProduction from '../KPI/HourlyProduction';
import CycleTime from '../KPI/CycleTime';
import ExternalEnvironment from '../Environment/ExternalEnvironment';
import InternalEnvironment from '../Environment/InternalEnvironment';

const Navbar = ({ selectedProduct }) => {  
  return (
    <div className="d-flex flex-column h-100" style={{ gap: '0.8rem' }}>
      <div style={{ height: '215px' }}>
        <ExternalEnvironment />
      </div>
      
      <div style={{ height: '215px' }}>
        <InternalEnvironment />
      </div>
      
      <div style={{ height: '215px' }}>
        <ProductionTarget />
      </div>
      
      <div style={{ height: '215px' }}>
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