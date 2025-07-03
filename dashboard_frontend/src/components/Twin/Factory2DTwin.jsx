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

  const [stationData, setStationData] = useState({});
  const [products, setProducts] = useState([]);
  const [isConnected, setIsConnected] = useState(false);
  const [selectedProduct, setSelectedProduct] = useState(null);

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

    generateSimulationData();
    return () => clearInterval(interval);
  }, []);

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

  const beltHeight = 60;
  const boxHeight = 120;

  const lines = [
    { name: 'A', y: 150, dir: 1, processes: [
        { name: '도어탈거', x: 150, width: 120 },
        { name: '와이어링', x: 300, width: 120 },
        { name: '헤드라이너', x: 450, width: 120 },
        { name: '크래쉬패드', x: 750, width: 350 }
      ] },
    { name: 'B', y: 350, dir: -1, processes: [
        { name: '연료탱크', x: 850, width: 100 },
        { name: '샤시메리지', x: 500, width: 500 },
        { name: '머플러', x: 150, width: 100 }
      ] },
    { name: 'C', y: 550, dir: 1, processes: [
        { name: 'FEM', x: 150, width: 120 },
        { name: '글라스', x: 300, width: 120 },
        { name: '시트', x: 450, width: 120 },
        { name: '범퍼', x: 600, width: 120 },
        { name: '타이어', x: 750, width: 120 }
      ] },
    { name: 'D', y: 750, dir: -1, processes: [
        { name: '수밀검사', x: 320, width: 450 },
        { name: '헤드램프', x: 650, width: 120 },
        { name: '휠 얼라이언트', x: 800, width: 120 }
      ] }
  ];

  const updateProductPositions = (stationData) => {
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

    const newProducts = [];
    Object.entries(stationData).forEach(([stationId, data]) => {
      const mapping = stationMapping[stationId];
      if (!mapping) return;

      const line = lines.find(l => l.name === mapping.line);
      if (!line || !line.processes[mapping.processIndex]) return;

      const process = line.processes[mapping.processIndex];
      const progressRatio = data.progress / 100;
      let productX;
      if (line.dir === 1) {
        productX = process.x - (process.width / 2) + (process.width * progressRatio);
      } else {
        productX = process.x + (process.width / 2) - (process.width * progressRatio);
      }

      let color = '#4CAF50';
      if (data.status === 'ERROR') color = '#f44336';
      else if (data.status === 'IDLE') color = '#9E9E9E';
      else if (data.progress < 30) color = '#FF9800';
      else if (data.progress < 70) color = '#2196F3';

      newProducts.push({
        id: `product_${stationId}`,
        stationId,
        x: productX,
        y: line.y,
        line: line.name,
        progress: data.progress,
        status: data.status,
        operation: data.operation,
        color,
        size: 12
      });
    });

    setProducts(newProducts);
  };

  return (
    <div>
      {/* 컴포넌트 내용 생략: 제품 렌더링, 캔버스, 팝오버 등 */}
    </div>
  );
};

export default Factory2DTwin;