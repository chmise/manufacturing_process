import React, { useRef, useEffect, useState } from 'react';
import ClickRobot from './ClickRobot';

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

  // 차량 관리 상태
  const [vehicles, setVehicles] = useState([]);
  const [isProducing, setIsProducing] = useState(false);
  const vehicleIdCounter = useRef(0);
  const animationFrameRef = useRef(null);

  // 스케일링 정보 저장
  const scaleInfoRef = useRef({ scale: 1, offsetX: 0, offsetY: 0 });

  // 현대차 모델 정의
  const hyundaiModels = [
    { name: '아반떼', color: '#1976d2', code: 'AVANTE' },
    { name: '투싼', color: '#388e3c', code: 'TUCSON' },
    { name: '팰리세이드', color: '#7b1fa2', code: 'PALISADE' },
    { name: '코나', color: '#f57c00', code: 'KONA' },
    { name: '그랜저', color: '#d32f2f', code: 'GRANDEUR' }
  ];

  // 컨테이너 크기 변화 감지 및 반응형 처리
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

  // 조립 라인 데이터 정의 (A라인 기준으로 통일, 넓은 간격)
  const beltHeight = 60;     // 모든 라인 컨베이어 높이 통일 (A라인 기준)
  const boxHeight = 120;     // 모든 공정박스 높이 통일 (A라인 기준)

  // 스테이션 경로 정의 (실제 생산 순서)
  const stationPath = [
    { id: 'ENTRY', name: '입구', x: 0, y: 150 },
    { id: 'A01_DOOR', name: '도어탈거', x: 150, y: 150 },
    { id: 'A02_WIRING', name: '와이어링', x: 300, y: 150 },
    { id: 'A03_HEADLINER', name: '헤드라이너', x: 450, y: 150 },
    { id: 'A04_CRASH_PAD', name: '크래쉬패드', x: 750, y: 150 },
    { id: 'TRANSIT_AB', name: 'A→B 이동', x: 970, y: 250 },
    { id: 'B01_FUEL_TANK', name: '연료탱크', x: 850, y: 350 },
    { id: 'B02_CHASSIS_MERGE', name: '샤시메리지', x: 500, y: 350 },
    { id: 'B03_MUFFLER', name: '머플러', x: 150, y: 350 },
    { id: 'TRANSIT_BC', name: 'B→C 이동', x: 30, y: 450 },
    { id: 'C01_FEM', name: 'FEM', x: 150, y: 550 },
    { id: 'C02_GLASS', name: '글라스', x: 300, y: 550 },
    { id: 'C03_SEAT', name: '시트', x: 450, y: 550 },
    { id: 'C04_BUMPER', name: '범퍼', x: 600, y: 550 },
    { id: 'C05_TIRE', name: '타이어', x: 750, y: 550 },
    { id: 'TRANSIT_CD', name: 'C→D 이동', x: 970, y: 650 },
    { id: 'D01_WHEEL_ALIGNMENT', name: '휠 얼라이언트', x: 800, y: 750 },
    { id: 'D02_HEADLAMP', name: '헤드램프', x: 650, y: 750 },
    { id: 'D03_WATER_LEAK_TEST', name: '수밀검사', x: 320, y: 750 },
    { id: 'EXIT', name: '출구', x: 0, y: 750 }
  ];

  // 차량 생성 함수
  const createNewVehicle = () => {
    if (isProducing) return;
    
    setIsProducing(true);
    const randomModel = hyundaiModels[Math.floor(Math.random() * hyundaiModels.length)];
    vehicleIdCounter.current += 1;
    
    const newVehicle = {
      id: `VH_${Date.now()}_${vehicleIdCounter.current}`,
      model: randomModel,
      currentStationIndex: 0,
      x: stationPath[0].x,
      y: stationPath[0].y,
      targetX: stationPath[0].x,
      targetY: stationPath[0].y,
      progress: 0,
      workProgress: 0,
      speed: 1.0,
      createdTime: new Date(),
      status: 'moving'
    };
    
    setVehicles(prev => [...prev, newVehicle]);
    
    console.log(`🚗 새 차량 생성: ${newVehicle.model.name} (${newVehicle.id})`);
    
    // 1초 후 다음 차량 생성 가능
    setTimeout(() => setIsProducing(false), 1000);
  };

  // 스테이션별 작업 시간 정의 (실제 현장 데이터 기반)
  const stationWorkTime = {
    'A01_DOOR': 45,       // 도어탈거: 45초
    'A02_WIRING': 120,    // 와이어링: 2분
    'A03_HEADLINER': 90,  // 헤드라이너: 1.5분
    'A04_CRASH_PAD': 180, // 크래쉬패드: 3분
    'B01_FUEL_TANK': 60,  // 연료탱크: 1분
    'B02_CHASSIS_MERGE': 300, // 샤시메리지: 5분 (가장 오래 걸림)
    'B03_MUFFLER': 75,    // 머플러: 1.25분
    'C01_FEM': 90,        // FEM: 1.5분
    'C02_GLASS': 150,     // 글라스: 2.5분
    'C03_SEAT': 120,      // 시트: 2분
    'C04_BUMPER': 80,     // 범퍼: 1.33분
    'C05_TIRE': 100,      // 타이어: 1.67분
    'D01_WHEEL_ALIGNMENT': 180, // 휠 얼라이언트: 3분
    'D02_HEADLAMP': 60,   // 헤드램프: 1분
    'D03_WATER_LEAK_TEST': 240, // 수밀검사: 4분 (검사 시간)
  };

  // 차량 이동 함수 (현장 데이터 시뮬레이션)
  const moveVehicle = (vehicle) => {
    const currentStation = stationPath[vehicle.currentStationIndex];
    const nextStationIndex = vehicle.currentStationIndex + 1;
    
    if (nextStationIndex >= stationPath.length) {
      // 생산 완료 - 실제 현장에서는 완성차 출하
      console.log(`🎉 ${vehicle.model.name} 생산 완료! 총 소요시간: ${Math.round((Date.now() - vehicle.createdTime) / 1000)}초`);
      return { ...vehicle, status: 'completed' };
    }
    
    const nextStation = stationPath[nextStationIndex];
    
    // 현재 스테이션에서 작업 중
    if (vehicle.status === 'working') {
      const stationId = currentStation.id;
      const expectedWorkTime = stationWorkTime[stationId] || 60;
      
      // 현장 데이터 시뮬레이션: 작업 시간 변동성 (±20%)
      const workVariance = 0.8 + Math.random() * 0.4;
      const actualWorkSpeed = (100 / expectedWorkTime) * workVariance;
      
      vehicle.workProgress += actualWorkSpeed;
      
      // 현장에서 발생할 수 있는 지연 시뮬레이션 (5% 확률)
      if (Math.random() < 0.05 && vehicle.workProgress < 50) {
        vehicle.workProgress -= actualWorkSpeed * 0.3; // 약간의 지연
        if (!vehicle.hasDelayLogged) {
          console.log(`⚠️ ${vehicle.model.name}: ${currentStation.name}에서 작업 지연 발생`);
          vehicle.hasDelayLogged = true;
        }
      }
      
      if (vehicle.workProgress >= 100) {
        // 작업 완료, 다음 스테이션으로 이동 시작
        vehicle.status = 'moving';
        vehicle.workProgress = 0;
        vehicle.currentStationIndex = nextStationIndex;
        vehicle.targetX = nextStation.x;
        vehicle.targetY = nextStation.y;
        vehicle.progress = 0;
        vehicle.hasDelayLogged = false;
        
        console.log(`✅ ${vehicle.model.name}: ${currentStation.name} 작업 완료 → ${nextStation.name} 이동 시작`);
      }
      return vehicle;
    }
    
    // 스테이션 간 이동 중
    if (vehicle.status === 'moving') {
      const dx = vehicle.targetX - vehicle.x;
      const dy = vehicle.targetY - vehicle.y;
      const distance = Math.sqrt(dx * dx + dy * dy);
      
      if (distance < 2) {
        // 목표 스테이션 도착
        vehicle.x = vehicle.targetX;
        vehicle.y = vehicle.targetY;
        vehicle.status = 'working';
        vehicle.progress = 0;
        
        console.log(`🎯 ${vehicle.model.name}: ${nextStation.name} 도착, 작업 시작`);
      } else {
        // 이동 중 (현장 컨베이어 속도 시뮬레이션)
        // 실제 자동차 공장 컨베이어 속도: 0.2-0.5 m/s
        const baseSpeed = 1.5; // 캔버스 좌표계 기준
        const speedVariance = 0.9 + Math.random() * 0.2; // 컨베이어 속도 변동 (±10%)
        const moveSpeed = baseSpeed * speedVariance;
        
        vehicle.x += (dx / distance) * moveSpeed;
        vehicle.y += (dy / distance) * moveSpeed;
        vehicle.progress = ((distance - Math.sqrt((vehicle.targetX - vehicle.x) ** 2 + (vehicle.targetY - vehicle.y) ** 2)) / distance) * 100;
      }
    }
    
    return vehicle;
  };

  // 차량 애니메이션 루프
  useEffect(() => {
    const animate = () => {
      setVehicles(prevVehicles => {
        return prevVehicles
          .map(moveVehicle)
          .filter(vehicle => vehicle.status !== 'completed');
      });
      
      animationFrameRef.current = requestAnimationFrame(animate);
    };
    
    if (vehicles.length > 0) {
      animationFrameRef.current = requestAnimationFrame(animate);
    }
    
    return () => {
      if (animationFrameRef.current) {
        cancelAnimationFrame(animationFrameRef.current);
      }
    };
  }, [vehicles.length > 0]);
  
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

  // 캔버스 클릭 이벤트 핸들러
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
  };

  // 마우스 이동 이벤트 핸들러 (커서 변경용)
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

    canvas.style.cursor = isOverClickableArea ? 'pointer' : 'default';
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

  // 화면 클릭시 팝오버 닫기 이벤트
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (popoverState.isOpen && !canvasRef.current?.contains(event.target)) {
        handleClosePopover();
      }
    };

    document.addEventListener('click', handleClickOutside);
    return () => document.removeEventListener('click', handleClickOutside);
  }, [popoverState.isOpen]);

  // 캔버스 그리기 메인 로직
  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    const dpr = window.devicePixelRatio || 1;

    // 캔버스 고해상도 설정
    canvas.width = containerSize.width * dpr;
    canvas.height = containerSize.height * dpr;
    canvas.style.width = containerSize.width + 'px';
    canvas.style.height = containerSize.height + 'px';
    ctx.scale(dpr, dpr);

    // 콘텐츠 스케일링 및 중앙 정렬 설정
    const contentW = 1000, contentH = 900;
    const scale = Math.min(containerSize.width / contentW, containerSize.height / contentH) * 0.95;
    const offsetX = (containerSize.width - contentW * scale) / 2;
    const offsetY = (containerSize.height - contentH * scale) / 2;

    scaleInfoRef.current = { scale, offsetX, offsetY };

    ctx.save();
    ctx.translate(offsetX, offsetY);
    ctx.scale(scale, scale);

    // 컨베이어 시스템 그리기 (모든 라인 동일한 높이, 넓은 간격)
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

    // 공정 박스 그리기 (모든 박스 동일한 높이)
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

    // 방향 화살표 그리기 함수
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

    // 각 라인별 방향 화살표 그리기 (컨베이어 중앙에 맞춰 조정)
    drawArrow(30, 150, 0);                    // A라인 (우향) - 컨베이어 중앙
    drawArrow(970, 350, Math.PI);             // B라인 (좌향) - 컨베이어 중앙
    drawArrow(30, 550, 0);                    // C라인 (우향) - 컨베이어 중앙
    drawArrow(970, 750, Math.PI);             // D라인 (좌향) - 컨베이어 중앙

    // 컨베이어 벨트 내부에 라인 알파벳 표시 (컨베이어 중앙에 맞춰 조정)
    ctx.fillStyle = '#ffffff';
    ctx.font = 'bold 20px Arial';
    ctx.textAlign = 'center';
    
    ctx.fillText('A', 80, 150 + 7);          // A라인 - 컨베이어 중앙
    ctx.fillText('B', 80, 350 + 7);          // B라인 - 컨베이어 중앙
    ctx.fillText('C', 80, 550 + 7);          // C라인 - 컨베이어 중앙
    ctx.fillText('D', 80, 750 + 7);          // D라인 - 컨베이어 중앙

    // 차량 그리기
    vehicles.forEach(vehicle => {
      drawVehicle(ctx, vehicle);
    });

    ctx.restore();
  }, [containerSize, vehicles]);

  // 차량 그리기 함수
  const drawVehicle = (ctx, vehicle) => {
    const vehicleSize = 20;
    const { x, y, model, status, workProgress } = vehicle;
    
    // 차량 본체 그리기
    ctx.save();
    ctx.fillStyle = model.color;
    ctx.strokeStyle = '#000';
    ctx.lineWidth = 2;
    
    // 차량 모양 (둥근 사각형)
    ctx.beginPath();
    ctx.roundRect(x - vehicleSize/2, y - vehicleSize/2, vehicleSize, vehicleSize, 4);
    ctx.fill();
    ctx.stroke();
    
    // 차량 모델명 표시
    ctx.fillStyle = '#fff';
    ctx.font = 'bold 8px Arial';
    ctx.textAlign = 'center';
    ctx.fillText(model.code.substring(0, 3), x, y + 2);
    
    // 상태 표시
    if (status === 'working') {
      // 작업 진행률 표시 (원형 프로그레스)
      const radius = vehicleSize / 2 + 5;
      ctx.strokeStyle = '#4caf50';
      ctx.lineWidth = 3;
      ctx.beginPath();
      ctx.arc(x, y, radius, -Math.PI/2, -Math.PI/2 + (workProgress / 100) * 2 * Math.PI);
      ctx.stroke();
    }
    
    // 차량 ID 표시 (작은 글씨)
    ctx.fillStyle = '#666';
    ctx.font = '6px Arial';
    ctx.fillText(vehicle.id.split('_')[2], x, y - vehicleSize/2 - 8);
    
    ctx.restore();
  };

  return (
    <>
      {/* 차량 발주 컨트롤 패널 */}
      <div style={{
        position: 'absolute',
        top: '10px',
        left: '10px',
        background: 'rgba(255, 255, 255, 0.95)',
        padding: '15px',
        borderRadius: '8px',
        boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)',
        zIndex: 1000,
        minWidth: '250px'
      }}>
        <h4 style={{ margin: '0 0 10px 0', color: '#1976d2' }}>🏭 디지털 트윈 제어</h4>
        
        <button
          onClick={createNewVehicle}
          disabled={isProducing}
          style={{
            background: isProducing ? '#ccc' : '#1976d2',
            color: 'white',
            border: 'none',
            padding: '10px 20px',
            borderRadius: '5px',
            cursor: isProducing ? 'not-allowed' : 'pointer',
            fontSize: '14px',
            fontWeight: 'bold',
            width: '100%',
            marginBottom: '10px'
          }}
        >
          {isProducing ? '생산 중...' : '🚗 차량 발주'}
        </button>
        
        <div style={{ fontSize: '12px', color: '#666' }}>
          <div>🚗 생산 중인 차량: <strong>{vehicles.length}대</strong></div>
          <div>📊 현대차 5종 모델 랜덤 생성</div>
          {vehicles.length > 0 && (
            <div style={{ marginTop: '8px', fontSize: '11px' }}>
              최근 생성: <strong style={{ color: vehicles[vehicles.length - 1]?.model.color }}>
                {vehicles[vehicles.length - 1]?.model.name}
              </strong>
            </div>
          )}
        </div>
      </div>

      {/* 차량 상태 정보 패널 */}
      {vehicles.length > 0 && (
        <div style={{
          position: 'absolute',
          top: '10px',
          right: '10px',
          background: 'rgba(255, 255, 255, 0.95)',
          padding: '15px',
          borderRadius: '8px',
          boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)',
          zIndex: 1000,
          maxWidth: '300px',
          maxHeight: '400px',
          overflow: 'auto'
        }}>
          <h4 style={{ margin: '0 0 10px 0', color: '#1976d2' }}>📋 생산 현황</h4>
          
          {vehicles.slice(-5).reverse().map(vehicle => {
            const currentStation = stationPath[vehicle.currentStationIndex];
            return (
              <div key={vehicle.id} style={{
                padding: '8px',
                marginBottom: '5px',
                background: vehicle.model.color + '20',
                borderRadius: '4px',
                borderLeft: `4px solid ${vehicle.model.color}`
              }}>
                <div style={{ fontSize: '12px', fontWeight: 'bold' }}>
                  {vehicle.model.name} ({vehicle.id.split('_')[2]})
                </div>
                <div style={{ fontSize: '11px', color: '#666' }}>
                  위치: {currentStation?.name || '이동 중'}
                </div>
                <div style={{ fontSize: '11px', color: '#666' }}>
                  상태: {vehicle.status === 'working' ? `작업 중 (${Math.round(vehicle.workProgress)}%)` : '이동 중'}
                </div>
              </div>
            );
          })}
        </div>
      )}

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