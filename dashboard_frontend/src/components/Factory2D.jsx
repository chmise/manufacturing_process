import React, { useState } from 'react';
import Factory2DTwin from "./Twin/Factory2DTwin";
import Navbar from "./Twin/Navabr";

const Factory2D = () => {
  // 선택된 제품 상태를 부모 컴포넌트에서 관리
  const [selectedProduct, setSelectedProduct] = useState(null);

  // 제품 선택 핸들러
  const handleProductSelect = (product) => {
    setSelectedProduct(product);
  };

  return (
    <div className="row g-3">
      <div className="col-sm-6 col-lg-4">
        <div className="card">
          <div className="card-body" style={{ height: "50rem" }}>
            {/* Navbar에 선택된 제품 정보 전달 */}
            <Navbar selectedProduct={selectedProduct} />
          </div>
        </div>
      </div>

      <div className="col-sm-6 col-lg-8">
        <div className="card">
          <div className="card-body" style={{ height: "50rem" }}>
            {/* Factory2DTwin에 제품 선택 핸들러 전달 */}
            <Factory2DTwin onProductSelect={handleProductSelect} />
          </div>
        </div>
      </div>
    </div>
  );
};

export default Factory2D;