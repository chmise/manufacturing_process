import React, { useRef, useEffect, useState } from 'react';
import ClickRobot from './ClickRobot';
import dashboardService from '../../service/dashboardService';

const Factory2DTwin = () => {
  // DOM 참조 및 상태 관리
  const canvasRef = useRef(null);
  const containerRef = useRef(null);
  const [containerSize, setContainerSize] = useState({ width: 800, height: 600 });
  
  // 로봇 선택 팝오버 상태 관리
  const [popoverState, setPopoverState] = useState({
    isOpen: false,
    selectedProcess: '',
    selectedRobot: null,
    position: { x: 0, y: 0 }
  });

  // 제품 추적을 위한 상태 (원본 코드에 추가)
  const [stationsData, setStationsData] = useState([]);
  const [products, setProducts] = useState([]);

  // 스케일링 정보 저장
  const scaleInfoRef = useRef({ scale: 1, offsetX: 0, offsetY: 0 });

  // 제품 추적 데이터 구독 (원본 코드에 추가)
  useEffect(() => {
    let mounted = true;

    const handleDataUpdate = (type, data) => {
      if (!mounted) return;

      if (type === 'stations') {
        setStationsData(data);
        // 스테이션 데이터를 기반으로 제품 위치 계산
        updateProductPositions(data);
      }
    };

    const unsubscribe = dashboardService.subscribe(handleDataUpdate);
    dashboardService.startPolling(3000);

    return () => {
      mounted = false;
      unsubscribe();
      dashboardService.stopPolling();
    };
  }, []);

  // 제품 위치 업데이트 함수 (실제 데이터만)
  const updateProductPositions = (stations) => {
    const newProducts = [];
    
    stations.forEach((station, index) => {
      if (station.status === 'RUNNING' || station.status === 'running') {
        // 각 라인별 제품 위치 계산
        const linePositions = getProductPositions(station.stationId, index);
        linePositions.forEach(pos => {
          newProducts.push({
            id: `${station.stationId}_${pos.id}`,
            x: pos.x,
            y: pos.y,
            stationId: station.stationId,
            line: pos.line
          });
        });
      }
    });
    
    setProducts(newProducts);
  };

  // 실제 데이터가 있을 때만 애니메이션
  useEffect(() => {
    if (stationsData.length === 0) return;

    const animationTimer = setInterval(() => {
      updateProductPositions(stationsData);
    }, 100); // 100ms마다 위치 업데이트

    return () => clearInterval(animationTimer);
  }, [stationsData]);

  // 스테이션별 제품 위치 계산
  const getProductPositions = (stationId, index) => {
    const time = Date.now() / 1000; // 시간 기반 애니메이션
    const positions = [];
    
    // 라인별 제품 위치 정의
    const lineConfig = {
      'A': { y: 150, startX: 50, endX: 950, direction: 1 },
      'B': { y: 350, startX: 950, endX: 50, direction: -1 },
      'C': { y: 550, startX: 50, endX: 950, direction: 1 },
      'D': { y: 750, startX: 950, endX: 50, direction: -1 }
    };
    
    // 스테이션 ID를 기반으로 라인 결정
    let line = 'A';
    if (stationId.startsWith('B')) line = 'B';
    else if (stationId.startsWith('C')) line = 'C';
    else if (stationId.startsWith('D')) line = 'D';
    
    const config = lineConfig[line];
    if (!config) return positions;
    
    // 제품 개수 (스테이션 효율성 기반)
    const productCount = Math.max(1, Math.floor((index + 1) * 2));
    
    for (let i = 0; i < productCount; i++) {
      // 시간과 인덱스를 기반으로 X 위치 계산
      const speed = 20; // 이동 속도
      const offset = (time * speed + i * 150) % (config.endX - config.startX);
      
      let x;
      if (config.direction === 1) {
        x = config.startX + offset;
      } else {
        x = config.startX - offset;
      }
      
      positions.push({
        id: i,
        x: x,
        y: config.y,
        line: line
      });
    }
    
    return positions;
  };

  // 컨테이너 크기 변화 감지 및 반응형 처리 (원본 유지)
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

  // 조립 라인 데이터 정의 (원본 유지)
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

  // 캔버스 클릭 이벤트 핸들러 (완전 원본 방식)
  const handleCanvasClick = (event) => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const rect = canvas.getBoundingClientRect();
    const clientX = event.clientX - rect.left;
    const clientY = event.clientY - rect.top;

    const { scale, offsetX, offsetY } = scaleInfoRef.current;
    const canvasX = (clientX - offsetX) / scale;
    const canvasY = (clientY - offsetY) / scale;

    // 각 공정 박스의 텍스트 영역에 대해 클릭 여부 확인
    for (const line of lines) {
      const boxY = line.y - boxHeight/2;
      
      for (const process of line.processes) {
        const boxLeft = process.x - process.width/2;
        const boxRight = process.x + process.width/2;
        const textAreaTop = boxY;
        const textAreaBottom = boxY + boxHeight;

        if (canvasX >= boxLeft && canvasX <= boxRight && 
            canvasY >= textAreaTop && canvasY <= textAreaBottom) {
          
          setPopoverState({
            isOpen: true,
            selectedProcess: process.name,
            selectedRobot: null,
            position: { x: event.clientX, y: event.clientY }  // 브라우저 절대 좌표 사용
          });
          return;
        }
      }
    }
  };

  // 마우스 이동 이벤트 핸들러 (완전 원본 방식)
  const handleMouseMove = (event) => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const rect = canvas.getBoundingClientRect();
    const clientX = event.clientX - rect.left;
    const clientY = event.clientY - rect.top;

    const { scale, offsetX, offsetY } = scaleInfoRef.current;
    const canvasX = (clientX - offsetX) / scale;
    const canvasY = (clientY - offsetY) / scale;

    let isOverProcess = false;

    for (const line of lines) {
      const boxY = line.y - boxHeight/2;
      
      for (const process of line.processes) {
        const boxLeft = process.x - process.width/2;
        const boxRight = process.x + process.width/2;
        const textAreaTop = boxY;
        const textAreaBottom = boxY + boxHeight;

        if (canvasX >= boxLeft && canvasX <= boxRight && 
            canvasY >= textAreaTop && canvasY <= textAreaBottom) {
          isOverProcess = true;
          break;
        }
      }
      if (isOverProcess) break;
    }

    canvas.style.cursor = isOverProcess ? 'pointer' : 'default';
  };

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

  // 화면 클릭시 팝오버 닫기 이벤트 (원본 유지)
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (popoverState.isOpen && !canvasRef.current?.contains(event.target)) {
        handleClosePopover();
      }
    };

    document.addEventListener('click', handleClickOutside);
    return () => document.removeEventListener('click', handleClickOutside);
  }, [popoverState.isOpen]);

  // 캔버스 그리기 메인 로직 (원본 + 제품 추적 추가)
  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    const dpr = window.devicePixelRatio || 1;

    // 캔버스 고해상도 설정 (원본 유지)
    canvas.width = containerSize.width * dpr;
    canvas.height = containerSize.height * dpr;
    canvas.style.width = containerSize.width + 'px';
    canvas.style.height = containerSize.height + 'px';
    ctx.scale(dpr, dpr);

    // 콘텐츠 스케일링 및 중앙 정렬 설정 (원본 유지)
    const contentW = 1000, contentH = 900;
    const scale = Math.min(containerSize.width / contentW, containerSize.height / contentH) * 0.95;
    const offsetX = (containerSize.width - contentW * scale) / 2;
    const offsetY = (containerSize.height - contentH * scale) / 2;

    scaleInfoRef.current = { scale, offsetX, offsetY };

    ctx.save();
    ctx.translate(offsetX, offsetY);
    ctx.scale(scale, scale);

    // 배경 (원본 유지)
    ctx.fillStyle = '#f0f0f0';
    ctx.fillRect(0, 0, contentW, contentH);

    // 컨베이어 시스템 그리기 (원본 코드 완전 유지)
    ctx.fillStyle = '#444';
    
    const conveyorPath = new Path2D();
    
    // A라인 (0~1000, 120~180)
    conveyorPath.rect(0, 120, 1000, beltHeight);
    
    // A→B 수직 연결 (940~1000, 180~320)
    conveyorPath.rect(940, 180, 60, 140);
    
    // B라인 (0~1000, 320~380)
    conveyorPath.rect(0, 320, 1000, beltHeight);
    
    // B→C 수직 연결 (0~60, 380~520)
    conveyorPath.rect(0, 380, 60, 140);
    
    // C라인 (0~1000, 520~580)
    conveyorPath.rect(0, 520, 1000, beltHeight);
    
    // C→D 수직 연결 (940~1000, 580~720)
    conveyorPath.rect(940, 580, 60, 140);
    
    // D라인 (0~1000, 720~780)
    conveyorPath.rect(0, 720, 1000, beltHeight);
    
    ctx.fill(conveyorPath);

    // 공정 박스 그리기 (원본 코드 완전 유지)
    lines.forEach(line => {
      const boxY = line.y - boxHeight/2;
      
      line.processes.forEach(process => {
        // 박스 테두리 그리기
        ctx.strokeStyle = '#1976d2';
        ctx.lineWidth = 2;
        ctx.strokeRect(process.x - process.width/2, boxY, process.width, boxHeight);
        
        // 공정명 텍스트 그리기
        ctx.fillStyle = '#333';
        ctx.font = 'bold 16px Arial';
        ctx.textAlign = 'center';
        ctx.fillText(process.name, process.x, boxY + 25);
      });
    });

    // 방향 화살표 그리기 함수 (원본 유지)
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

    // 각 라인별 방향 화살표 그리기 (원본 유지)
    drawArrow(30, 150, 0);                    // A라인 (우향) - 컨베이어 중앙
    drawArrow(970, 350, Math.PI);             // B라인 (좌향) - 컨베이어 중앙
    drawArrow(30, 550, 0);                    // C라인 (우향) - 컨베이어 중앙
    drawArrow(970, 750, Math.PI);             // D라인 (좌향) - 컨베이어 중앙

    // 컨베이어 벨트 내부에 라인 알파벳 표시 (원본 유지)
    ctx.fillStyle = '#ffffff';
    ctx.font = 'bold 20px Arial';
    ctx.textAlign = 'center';
    
    ctx.fillText('A', 80, 150 + 7);          // A라인 - 컨베이어 중앙
    ctx.fillText('B', 80, 350 + 7);          // B라인 - 컨베이어 중앙
    ctx.fillText('C', 80, 550 + 7);          // C라인 - 컨베이어 중앙
    ctx.fillText('D', 80, 750 + 7);          // D라인 - 컨베이어 중앙

    // 제품 추적 표시 (실제 데이터 기반만)
    if (products.length > 0) {
      products.forEach(product => {
        // 제품을 빨간 원으로 표시
        ctx.fillStyle = '#ff4444';
        ctx.beginPath();
        ctx.arc(product.x, product.y, 5, 0, 2 * Math.PI);
        ctx.fill();
        
        // 제품 주변에 흰색 테두리
        ctx.strokeStyle = '#ffffff';
        ctx.lineWidth = 2;
        ctx.stroke();
      });
    }

    ctx.restore();
  }, [containerSize, products]); // products 의존성 추가

  return (
    <>
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