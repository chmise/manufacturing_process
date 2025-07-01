// Factory2DTwin.jsx - 기존 코드에 추가할 부분들

import React, { useRef, useEffect, useState } from 'react';
import ClickRobot from './ClickRobot';

const Factory2DTwin = () => {
  // 기존 상태들...
  const canvasRef = useRef(null);
  const containerRef = useRef(null);
  const [containerSize, setContainerSize] = useState({ width: 800, height: 600 });
  const [popoverState, setPopoverState] = useState({
    isOpen: false,
    selectedProcess: '',
    selectedRobot: null,
    position: { x: 0, y: 0 }
  });
  const scaleInfoRef = useRef({ scale: 1, offsetX: 0, offsetY: 0 });

  // 🆕 제품 움직임을 위한 새로운 상태들
  const [stationData, setStationData] = useState({});
  const [products, setProducts] = useState([]);
  const [isConnected, setIsConnected] = useState(false);
  const [selectedProduct, setSelectedProduct] = useState(null);

  // 🆕 실시간 데이터 연결
  useEffect(() => {
    const interval = setInterval(async () => {
      try {
        const response = await fetch('http://localhost:8080/api/station/status/all');
        if (response.ok) {
          const stations = await response.json();
          updateStationData(stations);
          setIsConnected(true);
        }
      } catch (error) {
        console.log('백엔드 연결 실패, 시뮬레이션 데이터 사용');
        generateSimulationData();
        setIsConnected(false);
      }
    }, 3000);

    // 초기 데이터 로드
    generateSimulationData();

    return () => clearInterval(interval);
  }, []);

  // 🆕 스테이션 데이터 업데이트
  const updateStationData = (stations) => {
    const newStationData = {};
    stations.forEach(station => {
      newStationData[station.stationId] = {
        progress: station.progress || 0,
        operation: station.currentOperation || '대기',
        status: station.status || 'IDLE',
        efficiency: station.efficiency || 0,
        lastUpdate: station.timestamp
      };
    });
    setStationData(newStationData);
    updateProductPositions(newStationData);
  };

  // 🆕 시뮬레이션 데이터 생성 (백엔드 연결 실패시)
  const generateSimulationData = () => {
    const stations = [
      'A01_DOOR', 'A02_WIRE', 'A03_HEAD', 'A04_CRASH',
      'B01_FUEL', 'B02_CHASSIS', 'B03_MUFFLER',
      'C01_FEM', 'C02_GLASS', 'C03_SEAT', 'C04_BUMPER', 'C05_TIRE',
      'D01_WHEEL', 'D02_LAMP', 'D03_WATER'
    ];
    
    const newData = {};
    stations.forEach(stationId => {
      newData[stationId] = {
        progress: Math.random() * 100,
        operation: ['작업중', '검사중', '대기', '이동중'][Math.floor(Math.random() * 4)],
        status: ['RUNNING', 'IDLE', 'RUNNING'][Math.floor(Math.random() * 3)],
        efficiency: 80 + Math.random() * 20,
        lastUpdate: new Date().toISOString()
      };
    });
    
    setStationData(newData);
    updateProductPositions(newData);
  };

  // 🆕 제품 위치 업데이트 (기존 lines 데이터 활용)
  const updateProductPositions = (stationData) => {
    const stationMapping = {
      // Line A
      'A01_DOOR': { line: 'A', processIndex: 0 },
      'A02_WIRE': { line: 'A', processIndex: 1 },
      'A03_HEAD': { line: 'A', processIndex: 2 },
      'A04_CRASH': { line: 'A', processIndex: 3 },
      // Line B
      'B01_FUEL': { line: 'B', processIndex: 0 },
      'B02_CHASSIS': { line: 'B', processIndex: 1 },
      'B03_MUFFLER': { line: 'B', processIndex: 2 },
      // Line C
      'C01_FEM': { line: 'C', processIndex: 0 },
      'C02_GLASS': { line: 'C', processIndex: 1 },
      'C03_SEAT': { line: 'C', processIndex: 2 },
      'C04_BUMPER': { line: 'C', processIndex: 3 },
      'C05_TIRE': { line: 'C', processIndex: 4 },
      // Line D
      'D01_WHEEL': { line: 'D', processIndex: 2 },
      'D02_LAMP': { line: 'D', processIndex: 1 },
      'D03_WATER': { line: 'D', processIndex: 0 }
    };

    const newProducts = [];
    Object.entries(stationData).forEach(([stationId, data]) => {
      const mapping = stationMapping[stationId];
      if (!mapping) return;

      const line = lines.find(l => l.name === mapping.line);
      if (!line || !line.processes[mapping.processIndex]) return;

      const process = line.processes[mapping.processIndex];
      
      // 진행률에 따른 제품 위치 계산
      const progressRatio = data.progress / 100;
      let productX;
      
      if (line.dir === 1) { // 우향
        productX = process.x - (process.width / 2) + (process.width * progressRatio);
      } else { // 좌향
        productX = process.x + (process.width / 2) - (process.width * progressRatio);
      }
      
      // 제품 상태에 따른 색상
      let color = '#4CAF50'; // 기본 초록
      if (data.status === 'ERROR') color = '#f44336'; // 빨강
      else if (data.status === 'IDLE') color = '#9E9E9E'; // 회색
      else if (data.progress < 30) color = '#FF9800'; // 주황 (시작)
      else if (data.progress < 70) color = '#2196F3'; // 파랑 (진행)

      newProducts.push({
        id: `product_${stationId}`,
        stationId: stationId,
        x: productX,
        y: line.y,
        line: line.name,
        progress: data.progress,
        status: data.status,
        operation: data.operation,
        color: color,
        size: 12
      });
    });

    setProducts(newProducts);
  };

  // 기존 컨테이너 크기 조정 코드...
  useEffect(() => {
    const updateSize = () => {
      if (containerRef.current) {
        const rect = containerRef.current.getBoundingClientRect();
        const newWidth = Math.floor(rect.width);
        const newHeight = Math.floor(rect.height);
        
        setContainerSize(prevSize => {
          if (Math.abs(prevSize.width - newWidth) > 5 || Math.abs(prevSize.height - newHeight) > 5) {
            return { width: newWidth, height: newHeight };
          }
          return prevSize;
        });
      }
    };

    const timer = setTimeout(updateSize, 100);
    const resizeObserver = new ResizeObserver(entries => {
      clearTimeout(resizeObserver.timer);
      resizeObserver.timer = setTimeout(updateSize, 150);
    });

    if (containerRef.current) {
      resizeObserver.observe(containerRef.current);
    }

    return () => {
      clearTimeout(timer);
      clearTimeout(resizeObserver.timer);
      resizeObserver.disconnect();
    };
  }, []);

  // 기존 lines 데이터 (그대로 유지)
  const beltHeight = 60;
  const boxHeight = 120;
  
  const lines = [
    { 
      name: 'A',
      y: 150,
      dir: 1,
      processes: [
        { name: '도어탈거', x: 150, width: 120 },
        { name: '와이어링', x: 300, width: 120 },
        { name: '헤드라이너', x: 450, width: 120 },
        { name: '크래쉬패드', x: 750, width: 350 }
      ]
    },
    { 
      name: 'B',
      y: 350,
      dir: -1,
      processes: [
        { name: '연료탱크', x: 850, width: 100 },
        { name: '샤시메리지', x: 500, width: 500 },
        { name: '머플러', x: 150, width: 100 }
      ]
    },
    { 
      name: 'C',
      y: 550,
      dir: 1,
      processes: [
        { name: 'FEM', x: 150, width: 120 },
        { name: '글라스', x: 300, width: 120 },
        { name: '시트', x: 450, width: 120 },
        { name: '범퍼', x: 600, width: 120 },
        { name: '타이어', x: 750, width: 120 }
      ]
    },
    { 
      name: 'D',
      y: 750,
      dir: -1,
      processes: [
        { name: '수밀검사', x: 320, width: 450 },
        { name: '헤드램프', x: 650, width: 120 },
        { name: '휠 얼라이언트', x: 800, width: 120 }
      ]
    }
  ];

  // 🆕 제품 클릭 핸들러 추가
  const handleCanvasClick = (event) => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const rect = canvas.getBoundingClientRect();
    const clientX = event.clientX - rect.left;
    const clientY = event.clientY - rect.top;

    const { scale, offsetX, offsetY } = scaleInfoRef.current;
    const canvasX = (clientX - offsetX) / scale;
    const canvasY = (clientY - offsetY) / scale;

    // 🆕 제품 클릭 확인 (우선순위)
    const clickedProduct = products.find(product => {
      const distance = Math.sqrt(
        Math.pow(canvasX - product.x, 2) + Math.pow(canvasY - product.y, 2)
      );
      return distance <= product.size + 5;
    });

    if (clickedProduct) {
      setSelectedProduct(clickedProduct);
      return;
    }

    // 기존 공정 박스 클릭 로직...
    for (const line of lines) {
      const boxY = line.y - boxHeight/2;
      
      for (const process of line.processes) {
        const boxLeft = process.x - process.width/2;
        const boxRight = process.x + process.width/2;
        const textAreaTop = boxY;
        const textAreaBottom = boxY + 30;
        
        if (canvasX >= boxLeft && canvasX <= boxRight && 
            canvasY >= textAreaTop && canvasY <= textAreaBottom) {
          
          const boxCenterX = process.x;
          const boxTopY = textAreaTop;
          const popoverX = rect.left + offsetX + (boxCenterX * scale);
          const popoverY = rect.top + offsetY + (boxTopY * scale);
          
          setPopoverState({
            isOpen: true,
            selectedProcess: process.name,
            selectedRobot: null,
            position: { x: popoverX, y: popoverY }
          });
          return;
        }
      }
    }
    
    setPopoverState(prev => ({ ...prev, isOpen: false }));
    setSelectedProduct(null);
  };

  // 기존 마우스 이동 핸들러 (그대로 유지)
  const handleMouseMove = (event) => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const rect = canvas.getBoundingClientRect();
    const clientX = event.clientX - rect.left;
    const clientY = event.clientY - rect.top;

    const { scale, offsetX, offsetY } = scaleInfoRef.current;
    const canvasX = (clientX - offsetX) / scale;
    const canvasY = (clientY - offsetY) / scale;

    let isOverClickableArea = false;

    // 제품 위에 마우스가 있는지 확인
    const overProduct = products.some(product => {
      const distance = Math.sqrt(
        Math.pow(canvasX - product.x, 2) + Math.pow(canvasY - product.y, 2)
      );
      return distance <= product.size + 5;
    });

    if (overProduct) {
      isOverClickableArea = true;
    } else {
      // 기존 공정 박스 체크
      for (const line of lines) {
        const boxY = line.y - boxHeight/2;
        
        for (const process of line.processes) {
          const boxLeft = process.x - process.width/2;
          const boxRight = process.x + process.width/2;
          const textAreaTop = boxY;
          const textAreaBottom = boxY + 30;
          
          if (canvasX >= boxLeft && canvasX <= boxRight && 
              canvasY >= textAreaTop && canvasY <= textAreaBottom) {
            isOverClickableArea = true;
            break;
          }
        }
        if (isOverClickableArea) break;
      }
    }

    canvas.style.cursor = isOverClickableArea ? 'pointer' : 'default';
  };

  // 기존 팝오버 핸들러들 (그대로 유지)
  const handleRobotSelect = (robot) => {
    setPopoverState(prev => ({
      ...prev,
      selectedRobot: robot
    }));
    
    console.log(`선택된 로봇:`, {
      process: popoverState.selectedProcess,
      robot: robot
    });
  };

  const handleClosePopover = () => {
    setPopoverState({
      isOpen: false,
      selectedProcess: '',
      selectedRobot: null,
      position: { x: 0, y: 0 }
    });
  };

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (popoverState.isOpen && !canvasRef.current?.contains(event.target)) {
        handleClosePopover();
      }
    };

    document.addEventListener('click', handleClickOutside);
    return () => document.removeEventListener('click', handleClickOutside);
  }, [popoverState.isOpen]);

  // 🆕 제품들 그리기 함수
  const drawProducts = (ctx) => {
    products.forEach(product => {
      // 제품 표시 (원형)
      ctx.fillStyle = product.color;
      ctx.beginPath();
      ctx.arc(product.x, product.y, product.size, 0, 2 * Math.PI);
      ctx.fill();

      // 테두리
      ctx.strokeStyle = '#333';
      ctx.lineWidth = 1;
      ctx.stroke();

      // 제품 ID 표시
      ctx.fillStyle = 'white';
      ctx.font = 'bold 8px Arial';
      ctx.textAlign = 'center';
      ctx.fillText(product.stationId.substr(-2), product.x, product.y + 3);

      // 선택된 제품 강조
      if (selectedProduct && selectedProduct.id === product.id) {
        ctx.strokeStyle = '#ff0000';
        ctx.lineWidth = 3;
        ctx.beginPath();
        ctx.arc(product.x, product.y, product.size + 3, 0, 2 * Math.PI);
        ctx.stroke();
      }
    });
  };

  // 🆕 캔버스 그리기 로직 (기존 로직에 제품 그리기 추가)
  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    const dpr = window.devicePixelRatio || 1;

    canvas.width = containerSize.width * dpr;
    canvas.height = containerSize.height * dpr;
    canvas.style.width = containerSize.width + 'px';
    canvas.style.height = containerSize.height + 'px';
    ctx.scale(dpr, dpr);

    const contentW = 1000, contentH = 900;
    const scale = Math.min(containerSize.width / contentW, containerSize.height / contentH) * 0.95;
    const offsetX = (containerSize.width - contentW * scale) / 2;
    const offsetY = (containerSize.height - contentH * scale) / 2;

    scaleInfoRef.current = { scale, offsetX, offsetY };

    ctx.save();
    ctx.translate(offsetX, offsetY);
    ctx.scale(scale, scale);

    // 기존 그리기 로직들...
    // 컨베이어 시스템 그리기
    ctx.fillStyle = '#444';
    const conveyorPath = new Path2D();
    conveyorPath.rect(0, 120, 1000, beltHeight);
    conveyorPath.rect(940, 180, 60, 140);
    conveyorPath.rect(0, 320, 1000, beltHeight);
    conveyorPath.rect(0, 380, 60, 140);
    conveyorPath.rect(0, 520, 1000, beltHeight);
    conveyorPath.rect(940, 580, 60, 140);
    conveyorPath.rect(0, 720, 1000, beltHeight);
    ctx.fill(conveyorPath);

    // 공정 박스 그리기
    lines.forEach(line => {
      const boxY = line.y - boxHeight/2;
      
      line.processes.forEach(process => {
        ctx.strokeStyle = '#1976d2';
        ctx.lineWidth = 2;
        ctx.strokeRect(process.x - process.width/2, boxY, process.width, boxHeight);
        
        ctx.fillStyle = '#333';
        ctx.font = 'bold 16px Arial';
        ctx.textAlign = 'center';
        ctx.fillText(process.name, process.x, boxY + 25);
      });
    });

    // 방향 화살표 그리기
    const drawArrow = (x, y, angle, strokeColor = '#ffffff', lineWidth = 3) => {
      ctx.save();
      ctx.translate(x, y);
      ctx.rotate(angle);
      ctx.strokeStyle = strokeColor;
      ctx.lineWidth = lineWidth;
      ctx.beginPath();
      ctx.moveTo(0, 0);
      ctx.lineTo(-25, -15);
      ctx.lineTo(-25, 15);
      ctx.closePath();
      ctx.stroke();
      ctx.restore();
    };

    drawArrow(30, 150, 0);
    drawArrow(970, 350, Math.PI);
    drawArrow(30, 550, 0);
    drawArrow(970, 750, Math.PI);

    // 라인 알파벳 표시
    ctx.fillStyle = '#ffffff';
    ctx.font = 'bold 20px Arial';
    ctx.textAlign = 'center';
    ctx.fillText('A', 80, 150 + 7);
    ctx.fillText('B', 80, 350 + 7);
    ctx.fillText('C', 80, 550 + 7);
    ctx.fillText('D', 80, 750 + 7);

    // 🆕 제품들 그리기
    drawProducts(ctx);

    ctx.restore();
  }, [containerSize, products]); // products 의존성 추가

  return (
    <>
      {/* 🆕 연결 상태 표시 */}
      <div style={{
        position: 'absolute',
        top: '10px',
        left: '10px',
        zIndex: 1000,
        background: isConnected ? '#4CAF50' : '#ff9800',
        color: 'white',
        padding: '6px 12px',
        borderRadius: '4px',
        fontSize: '12px'
      }}>
        {isConnected ? '🟢 실시간' : '🟡 시뮬'}
      </div>

      {/* 🆕 제품 정보 패널 */}
      {selectedProduct && (
        <div style={{
          position: 'absolute',
          top: '10px',
          right: '10px',
          zIndex: 1000,
          background: 'white',
          border: '2px solid #ccc',
          borderRadius: '8px',
          padding: '12px',
          fontSize: '12px',
          minWidth: '180px',
          boxShadow: '0 2px 8px rgba(0,0,0,0.1)'
        }}>
          <h4 style={{ margin: '0 0 8px 0', fontSize: '14px' }}>🚗 제품 정보</h4>
          <div><strong>위치:</strong> {selectedProduct.stationId}</div>
          <div><strong>진행률:</strong> {selectedProduct.progress.toFixed(1)}%</div>
          <div><strong>상태:</strong> {selectedProduct.status}</div>
          <div><strong>작업:</strong> {selectedProduct.operation}</div>
          <button 
            onClick={() => setSelectedProduct(null)}
            style={{
              marginTop: '8px',
              padding: '4px 8px',
              border: 'none',
              background: '#f44336',
              color: 'white',
              borderRadius: '4px',
              cursor: 'pointer',
              fontSize: '10px'
            }}
          >
            닫기
          </button>
        </div>
      )}

      {/* 기존 캔버스 및 팝오버 */}
      <div 
        ref={containerRef}
        style={{ 
          width: '100%',
          height: '100%',
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          padding: '10px'
        }}
      >
        <canvas
          ref={canvasRef}
          onClick={handleCanvasClick}
          onMouseMove={handleMouseMove}
          style={{
            display: 'block',
            maxWidth: '100%',
            maxHeight: '100%',
            imageRendering: '-webkit-optimize-contrast',
            WebkitImageRendering: '-webkit-optimize-contrast',
            msInterpolationMode: 'nearest-neighbor'
          }}
        />
      </div>
      
      <ClickRobot
        isOpen={popoverState.isOpen}
        processName={popoverState.selectedProcess}
        position={popoverState.position}
        onClose={handleClosePopover}
        onSelectRobot={handleRobotSelect}
      />
    </>
  );
};

export default Factory2DTwin;