import React, { useRef, useEffect, useState } from 'react';
import ClickRobot from './ClickRobot';

const Factory2DTwin = () => {
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

  // 실제 MQTT 데이터를 위한 상태들
  const [stationData, setStationData] = useState({});
  const [products, setProducts] = useState([]);
  const [isConnected, setIsConnected] = useState(false);
  const [selectedProduct, setSelectedProduct] = useState(null);
  
  // 애니메이션을 위한 상태들
  const animationFrameRef = useRef(null);
  const lastUpdateTimeRef = useRef(Date.now());
  
  // 라인 데이터
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

  // 자동차 모델명 리스트
  const vehicleModels = ['SEDAN_A', 'SUV_B', 'TRUCK_C'];

  // 자동차 제품명 첫 글자 추출 함수
  const getVehicleInitial = (stationId) => {
    const stationIndex = parseInt(stationId.match(/\d+/)?.[0] || '0');
    const modelIndex = stationIndex % vehicleModels.length;
    const model = vehicleModels[modelIndex];
    return model.charAt(0).toUpperCase();
  };

  // 자동차 모델명 전체 표시 함수
  const getFullVehicleName = (stationId) => {
    const stationIndex = parseInt(stationId.match(/\d+/)?.[0] || '0');
    const modelIndex = stationIndex % vehicleModels.length;
    return vehicleModels[modelIndex];
  };

  // 실제 MQTT 데이터 연결
  useEffect(() => {
    const fetchData = async () => {
      try {
        console.log('🔍 API 요청 시도: http://localhost:8080/api/station/status/all');
        const response = await fetch('http://localhost:8080/api/station/status/all');
        
        if (response.ok) {
          const stations = await response.json();
          console.log('📊 받은 스테이션 데이터:', stations);
          updateStationData(stations);
          setIsConnected(true);
        } else {
          console.log('⚠️ Spring Boot API 응답 오류:', response.status);
          // 연결 실패시 더미 데이터 생성
          generateTestData();
          setIsConnected(false);
        }
      } catch (error) {
        console.log('❌ 백엔드 연결 실패:', error.message);
        // 연결 실패시 더미 데이터 생성
        generateTestData();
        setIsConnected(false);
      }
    };

    // 즉시 실행
    fetchData();
    
    // 3초마다 반복
    const interval = setInterval(fetchData, 3000);

    return () => clearInterval(interval);
  }, []);

  // 테스트용 가짜 데이터 생성 (API 연결 실패시)
  const generateTestData = () => {
    console.log('🧪 테스트 데이터 생성 중...');
    const testStations = [
      {
        stationId: 'A01_DOOR',
        progress: Math.random() * 100,
        currentOperation: '도어탈거_작업중',
        status: 'RUNNING',
        efficiency: 85 + Math.random() * 15,
        cycleTime: 180 + Math.random() * 30,
        productionCount: Math.floor(Math.random() * 50),
        timestamp: new Date().toISOString()
      },
      {
        stationId: 'A02_WIRE',
        progress: Math.random() * 100,
        currentOperation: '와이어링_진행중',
        status: 'RUNNING',
        efficiency: 80 + Math.random() * 20,
        cycleTime: 200 + Math.random() * 40,
        productionCount: Math.floor(Math.random() * 45),
        timestamp: new Date().toISOString()
      },
      {
        stationId: 'B01_FUEL',
        progress: Math.random() * 100,
        currentOperation: '연료탱크_조립중',
        status: 'RUNNING',
        efficiency: 90 + Math.random() * 10,
        cycleTime: 150 + Math.random() * 25,
        productionCount: Math.floor(Math.random() * 55),
        timestamp: new Date().toISOString()
      },
      {
        stationId: 'C01_FEM',
        progress: Math.random() * 100,
        currentOperation: 'FEM_설치중',
        status: 'RUNNING',
        efficiency: 85 + Math.random() * 15,
        cycleTime: 170 + Math.random() * 30,
        productionCount: Math.floor(Math.random() * 48),
        timestamp: new Date().toISOString()
      }
    ];
    
    console.log('🧪 생성된 테스트 데이터:', testStations);
    updateStationData(testStations);
  };

  // 실제 스테이션 데이터 업데이트
  const updateStationData = (stations) => {
    const timestamp = new Date().toISOString();
    console.log(`🔄 [${timestamp}] 새로운 API 응답 수신`);
    
    if (!stations || !Array.isArray(stations)) {
      console.error('❌ 잘못된 데이터 형식:', stations);
      return;
    }

    const newStationData = {};
    
    stations.forEach((station, index) => {
      const stationId = station.stationId || station.station_id || station.id || `STATION_${index}`;
      
      newStationData[stationId] = {
        progress: station.progress || station.progressRate || Math.random() * 100,
        operation: station.currentOperation || station.operation || '작업중',
        status: station.status || 'RUNNING',
        efficiency: station.efficiency || (80 + Math.random() * 20),
        cycleTime: station.cycleTime || station.cycle_time || (150 + Math.random() * 50),
        productionCount: station.productionCount || station.production_count || Math.floor(Math.random() * 50),
        lastUpdate: station.timestamp || station.lastUpdate || timestamp,
        updateTime: Date.now()
      };
    });
    
    setStationData(newStationData);
    updateProductPositions(newStationData);
  };

  // 제품 위치 업데이트 (부드러운 애니메이션을 위한 targetX 설정)
  const updateProductPositions = (stationData) => {
    console.log('🚗 제품 위치 업데이트 시작');
    
    const stationMapping = {
      'A01_DOOR': { line: 'A', processIndex: 0 },
      'A02_WIRE': { line: 'A', processIndex: 1 },
      'A03_HEAD': { line: 'A', processIndex: 2 },
      'A04_CRASH': { line: 'A', processIndex: 3 },
      'B01_FUEL': { line: 'B', processIndex: 0 },
      'B02_CHASSIS': { line: 'B', processIndex: 1 },
      'B03_MUFFLER': { line: 'B', processIndex: 2 },
      'C01_FEM': { line: 'C', processIndex: 0 },
      'C02_GLASS': { line: 'C', processIndex: 1 },
      'C03_SEAT': { line: 'C', processIndex: 2 },
      'C04_BUMPER': { line: 'C', processIndex: 3 },
      'C05_TIRE': { line: 'C', processIndex: 4 },
      'D01_WHEEL': { line: 'D', processIndex: 2 },
      'D02_LAMP': { line: 'D', processIndex: 1 },
      'D03_WATER': { line: 'D', processIndex: 0 }
    };

    setProducts(prevProducts => {
      const newProducts = [];

      Object.entries(stationData).forEach(([stationId, data]) => {
        const mapping = stationMapping[stationId];
        if (!mapping) return;

        const line = lines.find(l => l.name === mapping.line);
        if (!line || !line.processes[mapping.processIndex]) return;

        const process = line.processes[mapping.processIndex];
        
        // 목표 위치 계산
        const progressRatio = Math.max(0, Math.min(100, data.progress || 0)) / 100;
        let targetX;
        
        if (line.dir === 1) { // 우향
          targetX = process.x - (process.width / 2) + (process.width * progressRatio);
        } else { // 좌향
          targetX = process.x + (process.width / 2) - (process.width * progressRatio);
        }
        
        // 기존 제품 찾기 (부드러운 애니메이션을 위해)
        const existingProduct = prevProducts.find(p => p.stationId === stationId);
        
        // 상태에 따른 색상
        let color = '#4CAF50';
        if (data.status === 'ERROR' || data.status === 'FAULT') {
          color = '#f44336';
        } else if (data.status === 'IDLE' || data.status === 'STOPPED') {
          color = '#9E9E9E';
        } else if (data.status === 'WARNING') {
          color = '#FF9800';
        } else if (data.status === 'RUNNING') {
          if (data.progress < 30) color = '#FF9800';
          else if (data.progress < 70) color = '#2196F3';
          else color = '#4CAF50';
        }

        const product = {
          id: `product_${stationId}`,
          stationId: stationId,
          x: existingProduct ? existingProduct.x : targetX, // 기존 위치에서 시작
          targetX: targetX, // 목표 위치
          y: line.y,
          line: line.name,
          progress: data.progress || 0,
          status: data.status,
          operation: data.operation,
          cycleTime: data.cycleTime,
          productionCount: data.productionCount,
          vehicleInitial: getVehicleInitial(stationId),
          color: color,
          size: 14,
          lastUpdate: data.lastUpdate,
          speed: 0.05 // 애니메이션 속도
        };

        newProducts.push(product);
      });

      return newProducts;
    });
  };

  // 🆕 부드러운 애니메이션 루프
  useEffect(() => {
    const animate = () => {
      const now = Date.now();
      const deltaTime = now - lastUpdateTimeRef.current;
      lastUpdateTimeRef.current = now;

      // 제품들의 위치를 부드럽게 업데이트
      setProducts(prevProducts => {
        return prevProducts.map(product => {
          if (Math.abs(product.x - product.targetX) > 1) {
            // 목표 지점으로 부드럽게 이동
            const direction = product.targetX > product.x ? 1 : -1;
            const distance = Math.abs(product.targetX - product.x);
            const moveDistance = Math.min(distance, product.speed * deltaTime);
            
            return {
              ...product,
              x: product.x + (direction * moveDistance)
            };
          }
          return product;
        });
      });

      animationFrameRef.current = requestAnimationFrame(animate);
    };

    animationFrameRef.current = requestAnimationFrame(animate);

    return () => {
      if (animationFrameRef.current) {
        cancelAnimationFrame(animationFrameRef.current);
      }
    };
  }, []);

  // 컨테이너 크기 조정
  useEffect(() => {
    let resizeTimer = null;

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
    
    const resizeObserver = new ResizeObserver(() => {
      if (resizeTimer) {
        clearTimeout(resizeTimer);
      }
      resizeTimer = setTimeout(updateSize, 150);
    });

    if (containerRef.current) {
      resizeObserver.observe(containerRef.current);
    }

    return () => {
      clearTimeout(timer);
      if (resizeTimer) {
        clearTimeout(resizeTimer);
      }
      resizeObserver.disconnect();
    };
  }, []);

  // 클릭 이벤트 핸들러
  const handleCanvasClick = (event) => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const rect = canvas.getBoundingClientRect();
    const clientX = event.clientX - rect.left;
    const clientY = event.clientY - rect.top;

    const { scale, offsetX, offsetY } = scaleInfoRef.current;
    const canvasX = (clientX - offsetX) / scale;
    const canvasY = (clientY - offsetY) / scale;

    // 제품 클릭 확인 (우선순위)
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

    // 공정 박스 클릭 로직
    for (const line of lines) {
      const boxY = line.y - boxHeight/2;
      
      for (const process of line.processes) {
        const boxLeft = process.x - process.width/2;
        const boxRight = process.x + process.width/2;
        const textAreaTop = boxY;
        const textAreaBottom = boxY + 30;
        
        if (canvasX >= boxLeft && canvasX <= boxRight && 
            canvasY >= textAreaTop && canvasY <= textAreaBottom) {
          
          setPopoverState({
            isOpen: true,
            selectedProcess: process.name,
            selectedRobot: null,
            position: { x: event.clientX, y: event.clientY }
          });
          return;
        }
      }
    }
    
    setPopoverState(prev => ({ ...prev, isOpen: false }));
    setSelectedProduct(null);
  };

  // 마우스 이동 핸들러
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
      // 공정 박스 체크
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

  // 팝오버 핸들러들
  const handleRobotSelect = (robot) => {
    setPopoverState(prev => ({
      ...prev,
      selectedRobot: robot
    }));
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

  // 시간 포맷팅 함수
  const formatLastUpdate = (timestamp) => {
    if (!timestamp) return '데이터 없음';
    
    try {
      const updateTime = new Date(timestamp);
      if (!isNaN(updateTime.getTime())) {
        const now = new Date();
        const diffSeconds = Math.floor((now - updateTime) / 1000);
        
        if (diffSeconds < 60) return `${diffSeconds}초 전`;
        if (diffSeconds < 3600) return `${Math.floor(diffSeconds / 60)}분 전`;
        return updateTime.toLocaleTimeString();
      }
      return new Date().toLocaleTimeString();
    } catch (error) {
      return '시간 오류';
    }
  };

  // 캔버스 그리기 로직 (실시간 렌더링)
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

    // 배경 클리어
    ctx.clearRect(0, 0, contentW, contentH);

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

    // 라인 알파벳 표시
    ctx.fillStyle = '#ffffff';
    ctx.font = 'bold 20px Arial';
    ctx.textAlign = 'center';
    ctx.fillText('A', 80, 150 + 7);
    ctx.fillText('B', 80, 350 + 7);
    ctx.fillText('C', 80, 550 + 7);
    ctx.fillText('D', 80, 750 + 7);

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

    // 🆕 움직이는 제품들 그리기
    products.forEach((product) => {
      // 배경 원 (글로우 효과)
      ctx.fillStyle = product.color + '40';
      ctx.beginPath();
      ctx.arc(product.x, product.y, product.size + 3, 0, 2 * Math.PI);
      ctx.fill();

      // 메인 제품 원
      ctx.fillStyle = product.color;
      ctx.beginPath();
      ctx.arc(product.x, product.y, product.size, 0, 2 * Math.PI);
      ctx.fill();

      // 테두리
      ctx.strokeStyle = '#000000';
      ctx.lineWidth = 2;
      ctx.beginPath();
      ctx.arc(product.x, product.y, product.size, 0, 2 * Math.PI);
      ctx.stroke();

      // 자동차 모델 첫 글자
      ctx.fillStyle = 'white';
      ctx.font = 'bold 12px Arial';
      ctx.textAlign = 'center';
      ctx.textBaseline = 'middle';
      ctx.strokeStyle = '#000000';
      ctx.lineWidth = 1;
      ctx.strokeText(product.vehicleInitial, product.x, product.y);
      ctx.fillText(product.vehicleInitial, product.x, product.y);

      // 진행률 표시
      ctx.fillStyle = '#000000';
      ctx.font = 'bold 8px Arial';
      ctx.fillText(`${product.progress.toFixed(0)}%`, product.x, product.y - product.size - 8);

      // 선택된 제품 강조
      if (selectedProduct && selectedProduct.id === product.id) {
        ctx.strokeStyle = '#ff0000';
        ctx.lineWidth = 3;
        ctx.beginPath();
        ctx.arc(product.x, product.y, product.size + 5, 0, 2 * Math.PI);
        ctx.stroke();

        // 이동 방향 화살표
        if (Math.abs(product.targetX - product.x) > 1) {
          const direction = product.targetX > product.x ? 1 : -1;
          ctx.strokeStyle = '#ff0000';
          ctx.lineWidth = 2;
          ctx.beginPath();
          ctx.moveTo(product.x + (direction * 20), product.y - 20);
          ctx.lineTo(product.x + (direction * 30), product.y - 20);
          ctx.lineTo(product.x + (direction * 25), product.y - 25);
          ctx.moveTo(product.x + (direction * 30), product.y - 20);
          ctx.lineTo(product.x + (direction * 25), product.y - 15);
          ctx.stroke();
        }
      }
    });

    // 공정 박스 그리기 (투명하게)
    lines.forEach(line => {
      const boxY = line.y - boxHeight/2;
      
      line.processes.forEach(process => {
        // 박스 배경 (투명)
        ctx.fillStyle = 'rgba(255, 255, 255, 0.1)';
        ctx.fillRect(process.x - process.width/2, boxY, process.width, boxHeight);
        
        // 테두리
        ctx.strokeStyle = '#1976d2';
        ctx.lineWidth = 2;
        ctx.strokeRect(process.x - process.width/2, boxY, process.width, boxHeight);
        
        // 텍스트
        ctx.fillStyle = '#333';
        ctx.font = 'bold 16px Arial';
        ctx.textAlign = 'center';
        ctx.fillText(process.name, process.x, boxY + 25);
      });
    });

    ctx.restore();
  }, [containerSize, products, selectedProduct]);

  return (
    <>
      {/* 연결 상태 표시 */}
      <div style={{
        position: 'absolute',
        top: '10px',
        left: '10px',
        zIndex: 1000,
        background: isConnected ? '#4CAF50' : '#f44336',
        color: 'white',
        padding: '6px 12px',
        borderRadius: '4px',
        fontSize: '12px'
      }}>
        {isConnected ? '🟢 실제 데이터 연결됨' : '🟡 시뮬레이션 모드'}
      </div>

      {/* 실시간 제품 정보 패널 */}
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
          minWidth: '220px',
          boxShadow: '0 2px 8px rgba(0,0,0,0.1)'
        }}>
          <h4 style={{ margin: '0 0 8px 0', fontSize: '14px' }}>
            🚗 실시간 제품 정보
          </h4>
          <div><strong>스테이션:</strong> {selectedProduct.stationId}</div>
          <div><strong>차량모델:</strong> {getFullVehicleName(selectedProduct.stationId)}</div>
          <div><strong>진행률:</strong> {selectedProduct.progress.toFixed(1)}%</div>
          <div><strong>상태:</strong> {selectedProduct.status}</div>
          <div><strong>작업:</strong> {selectedProduct.operation}</div>
          {selectedProduct.cycleTime > 0 && (
            <div><strong>사이클타임:</strong> {selectedProduct.cycleTime.toFixed(1)}s</div>
          )}
          {selectedProduct.productionCount > 0 && (
            <div><strong>생산수량:</strong> {selectedProduct.productionCount}대</div>
          )}
          <div><strong>업데이트:</strong> {formatLastUpdate(selectedProduct.lastUpdate)}</div>
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

      {/* 범례 (자동차 모델별 색상) */}
      <div style={{
        position: 'absolute',
        bottom: '10px',
        left: '10px',
        zIndex: 1000,
        background: 'rgba(255,255,255,0.9)',
        border: '1px solid #ccc',
        borderRadius: '6px',
        padding: '8px',
        fontSize: '11px'
      }}>
        <div style={{ fontWeight: 'bold', marginBottom: '4px' }}>차량 모델:</div>
        {vehicleModels.map((model, index) => (
          <div key={model} style={{ display: 'flex', alignItems: 'center', marginBottom: '2px' }}>
            <div style={{
              width: '12px',
              height: '12px',
              borderRadius: '50%',
              background: '#4CAF50',
              marginRight: '6px',
              fontSize: '8px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: 'white',
              fontWeight: 'bold'
            }}>
              {model.charAt(0)}
            </div>
            <span>{model}</span>
          </div>
        ))}
      </div>

      {/* 캔버스 */}
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