import React, { useRef, useEffect, useState } from 'react';
import DigitalTwinOverlay from './DigitalTwinOverlay';
import HoverTooltip from './HoverTooltip';

// 전역 Unity 상태 관리 (간단 버전)
window.unityGlobalState = window.unityGlobalState || {
  instance: null,
  isLoaded: false,
  canvas: null
};

const Factory3DTwin = () => {
  const unityContainerRef = useRef(null);
  const [isUnityLoaded, setIsUnityLoaded] = useState(false);
  const [loadingProgress, setLoadingProgress] = useState(0);
  const [errorMessage, setErrorMessage] = useState('');
  const unityInstanceRef = useRef(null);
  const isLoadingRef = useRef(false);
  const progressIntervalRef = useRef(null);
  
  // 디지털 트윈 오버레이 상태
  const [overlayOpen, setOverlayOpen] = useState(false);
  const [overlayData, setOverlayData] = useState(null);
  const [overlayType, setOverlayType] = useState(null);
  const [overlayPosition, setOverlayPosition] = useState({ x: 0, y: 0 });
  
  // 호버 툴팁 상태 (Unity에서 직접 제어)
  const [hoverVisible, setHoverVisible] = useState(false);
  const [hoverData, setHoverData] = useState(null);
  const [hoverType, setHoverType] = useState(null);
  const [hoverPosition, setHoverPosition] = useState({ x: 0, y: 0 });

  // 실시간 데이터 상태
  const [realtimeData, setRealtimeData] = useState(null);
  const realtimeIntervalRef = useRef(null);

  useEffect(() => {
    // Unity 통신 설정
    setupTestFunctions();
    
    // 로그인된 사용자의 회사명 자동 설정
    const setCompanyNameFromUserData = () => {
      const userData = localStorage.getItem('userData');
      if (userData) {
        try {
          const user = JSON.parse(userData);
          if (user.companyName && window.SetCompanyName) {
            setTimeout(() => {
              window.SetCompanyName(user.companyName);
            }, 2000); // Unity 로딩 완료 후 설정
          }
        } catch (error) {
          console.error('사용자 데이터 파싱 오류:', error);
        }
      }
    };

    // 전역 Unity 인스턴스가 이미 있는지 확인
    if (window.unityGlobalState.instance) {
      unityInstanceRef.current = window.unityGlobalState.instance;
      setIsUnityLoaded(true);
      setLoadingProgress(100);
      
      // 기존 캔버스를 현재 컨테이너로 이동
      if (window.unityGlobalState.canvas && unityContainerRef.current) {
        // 기존 캔버스의 부모에서 제거
        if (window.unityGlobalState.canvas.parentNode) {
          window.unityGlobalState.canvas.parentNode.removeChild(window.unityGlobalState.canvas);
        }
        
        // 새 컨테이너에 추가
        unityContainerRef.current.appendChild(window.unityGlobalState.canvas);
        
        // Unity 캔버스 크기 재조정
        setTimeout(() => {
          if (window.unityGlobalState.instance && window.unityGlobalState.instance.Module) {
            // Unity 화면 크기 강제 업데이트
            const canvas = window.unityGlobalState.canvas;
            const container = unityContainerRef.current;
            
            if (canvas && container) {
              canvas.style.width = '100%';
              canvas.style.height = '100%';
              
              // Unity 내부 해상도 업데이트 (전체화면 제거)
              // 캔버스 크기만 조정하고 전체화면은 하지 않음
              if (canvas && container) {
                canvas.style.width = '100%';
                canvas.style.height = '100%';
                
                // Unity Module 캔버스 크기 조정
                if (window.unityGlobalState.instance.Module.canvas) {
                  window.unityGlobalState.instance.Module.canvas.style.width = '100%';
                  window.unityGlobalState.instance.Module.canvas.style.height = '100%';
                }
                
              }
            }
          }
        }, 100);
      }
      
      // 기존 Unity 인스턴스에서도 회사명 설정
      setCompanyNameFromUserData();
      
      // 실시간 데이터 폴링 시작
      startRealtimeDataPolling();
      return;
    }

    const loadUnity = async () => {
      // 이미 로딩 중이거나 로드된 경우 중복 방지
      if (isLoadingRef.current || window.unityGlobalState.isLoaded) {
        return;
      }

      try {
        isLoadingRef.current = true;
        setErrorMessage('');

        // 1. 로딩 진행률 시뮬레이션
        progressIntervalRef.current = setInterval(() => {
          setLoadingProgress(prev => {
            if (prev >= 98) return prev;
            return prev + Math.random() * 2;
          });
        }, 200);

        // 2. Unity 설정
        const buildUrl = '/unity3d';
        const config = {
          dataUrl: `${buildUrl}/factoryTwin.data`,
          frameworkUrl: `${buildUrl}/factoryTwin.framework.js`,
          codeUrl: `${buildUrl}/factoryTwin.wasm`
        };

        // Unity 인스턴스 로드
        const loadUnityInstance = () => {
          // 컴포넌트가 언마운트되었는지 확인
          if (!isLoadingRef.current) {
            return;
          }

          const canvas = document.getElementById('unity-canvas') || unityContainerRef.current?.querySelector('canvas');
          if (!canvas) {
            console.error('Unity 캔버스를 찾을 수 없습니다');
            setErrorMessage('Unity 캔버스 요소를 찾을 수 없습니다');
            if (progressIntervalRef.current) {
              clearInterval(progressIntervalRef.current);
            }
            isLoadingRef.current = false;
            return;
          }

          // Unity 인스턴스 생성
          if (typeof window.createUnityInstance !== 'undefined') {
            window.createUnityInstance(canvas, config, (progress) => {
              const progressPercent = progress * 100;
              setLoadingProgress(progressPercent);
            }).then((unityInstance) => {
              // 로딩 중에 컴포넌트가 언마운트되었는지 확인
              if (!isLoadingRef.current) {
                if (unityInstance && typeof unityInstance.Quit === 'function') {
                  try {
                    unityInstance.Quit();
                  } catch (e) {
                    // Unity 정리 중 오류 무시
                  }
                }
                return;
              }
              
              // Unity 백그라운드 실행 설정
              if (unityInstance.Module) {
                unityInstance.Module.pauseMainLoop = false;
                unityInstance.Module.noExitRuntime = true;
                unityInstance.Module.requestFullscreen = false;
                if (unityInstance.Module.canvas) {
                  unityInstance.Module.canvas.requestFullscreen = null;
                }
              }
              
              // 전역 상태 저장
              window.unityGlobalState.instance = unityInstance;
              window.unityGlobalState.isLoaded = true;
              window.unityGlobalState.canvas = canvas;
              
              unityInstanceRef.current = unityInstance;
              setIsUnityLoaded(true);
              setLoadingProgress(100);
              if (progressIntervalRef.current) {
                clearInterval(progressIntervalRef.current);
              }
              isLoadingRef.current = false;
              
              setupUnityReactCommunication(unityInstance);
              
              // Unity 로딩 완료 후 회사명 자동 설정
              setCompanyNameFromUserData();
              
              // 실시간 데이터 폴링 시작
              startRealtimeDataPolling();
            }).catch((error) => {
              console.error('❌ Unity 로드 실패:', error);
              setErrorMessage(`Unity 로드 실패: ${error.message || error.toString()}`);
              if (progressIntervalRef.current) {
                clearInterval(progressIntervalRef.current);
              }
              isLoadingRef.current = false;
            });
          } else {
            // Unity 로더 대기
            setTimeout(loadUnityInstance, 1000);
          }
        };

        // Framework 스크립트 로드
        const loadFramework = () => {
          // 이미 로드되어 있는지 확인
          if (window.createUnityInstance) {
            loadUnityInstance();
            return;
          }

          // Framework 스크립트 동적 로드
          const script = document.createElement('script');
          script.src = `${buildUrl}/factoryTwin.framework.js`;
          script.async = true;
          
          script.onload = () => {
            setTimeout(loadUnityInstance, 500);
          };
          
          script.onerror = (error) => {
            console.error('Framework 로드 실패:', error);
            setErrorMessage('Unity Framework 로드 실패 - factoryTwin.framework.js를 확인하세요');
            if (progressIntervalRef.current) {
              clearInterval(progressIntervalRef.current);
            }
            isLoadingRef.current = false;
          };

          document.head.appendChild(script);
        };

        // Loader 스크립트 로드 (있다면)
        const loaderScript = document.createElement('script');
        loaderScript.src = `${buildUrl}/factoryTwin.loader.js`;
        loaderScript.async = true;
        
        loaderScript.onload = () => {
          loadFramework();
        };
        
        loaderScript.onerror = () => {
          loadFramework();
        };

        document.head.appendChild(loaderScript);

      } catch (error) {
        console.error('Unity 초기화 오류:', error);
        setErrorMessage(`Unity 초기화 실패: ${error.message}`);
        if (progressIntervalRef.current) {
          clearInterval(progressIntervalRef.current);
        }
        isLoadingRef.current = false;
      }
    };

    // Unity 로드 시작
    const timer = setTimeout(loadUnity, 100);

    return () => {
      clearTimeout(timer);
      isLoadingRef.current = false;

      // 진행률 타이머만 정리 (Unity 인스턴스는 유지)
      if (progressIntervalRef.current) {
        clearInterval(progressIntervalRef.current);
        progressIntervalRef.current = null;
      }

      // 실시간 데이터 폴링 정리
      if (realtimeIntervalRef.current) {
        clearInterval(realtimeIntervalRef.current);
        realtimeIntervalRef.current = null;
      }

      // Unity 인스턴스는 전역 상태에서 계속 유지
      // window.unityGlobalState에 저장되어 있어서 다른 페이지 갔다와도 계속 실행됨
    };
  }, []);

  // 컨테이너 변경 감지 및 Unity 재연결
  useEffect(() => {
    if (unityContainerRef.current && window.unityGlobalState.instance && isUnityLoaded) {
      
      const container = unityContainerRef.current;
      const canvas = window.unityGlobalState.canvas;
      
      // 캔버스가 현재 컨테이너에 없으면 재연결
      if (canvas && !container.contains(canvas)) {
        
        // 기존 위치에서 제거
        if (canvas.parentNode) {
          canvas.parentNode.removeChild(canvas);
        }
        
        // 새 컨테이너에 추가
        container.appendChild(canvas);
        
        // Unity 캔버스 재설정
        setTimeout(() => {
          if (window.unityGlobalState.instance) {
            try {
              const unityInstance = window.unityGlobalState.instance;
              
              // 캔버스 크기 설정
              canvas.style.width = '100%';
              canvas.style.height = '100%';
              
              if (unityInstance.Module) {
                // WebGL 컨텍스트 복원 시도
                if (unityInstance.Module.canvas) {
                  const gl = unityInstance.Module.canvas.getContext('webgl') || 
                            unityInstance.Module.canvas.getContext('experimental-webgl');
                  if (gl && gl.isContextLost && gl.isContextLost()) {
                    // WebGL 컨텍스트 복원 시도
                  }
                }
                
                // Unity 렌더링 재시작
                unityInstance.Module.pauseMainLoop = false;
                
                // 애플리케이션 포커스 복원
                if (typeof unityInstance.SendMessage === 'function') {
                  unityInstance.SendMessage('*', 'OnApplicationFocus', 'true');
                }
              }
              
            } catch (error) {
              console.error('⚠️ Unity 재연결 중 오류 (무시됨):', error);
            }
          }
        }, 200);
      }
    }
  }, [isUnityLoaded]);
  // 페이지 가시성 변화 감지 (백그라운드 실행 보장)
  useEffect(() => {
    const handleVisibilityChange = () => {
      if (window.unityGlobalState.instance && window.unityGlobalState.instance.Module) {
        const isVisible = !document.hidden;
        
        // 페이지가 숨겨져도 Unity 계속 실행
        window.unityGlobalState.instance.Module.pauseMainLoop = false;
        
        if (isVisible) {
          // 페이지가 다시 보일 때 Unity 복원
          setTimeout(() => {
            try {
              const unityInstance = window.unityGlobalState.instance;
              const canvas = window.unityGlobalState.canvas;
              
              if (unityInstance && canvas) {
                // Unity 포커스 복원
                if (typeof unityInstance.SendMessage === 'function') {
                  unityInstance.SendMessage('*', 'OnApplicationFocus', 'true');
                  unityInstance.SendMessage('*', 'OnApplicationPause', 'false');
                }
                
                // 렌더링 재시작
                if (unityInstance.Module) {
                  unityInstance.Module.pauseMainLoop = false;
                  
                  // WebGL 컨텍스트 복원
                  const gl = canvas.getContext('webgl') || canvas.getContext('experimental-webgl');
                  if (gl) {
                    gl.viewport(0, 0, canvas.width, canvas.height);
                  }
                  
                  // 캔버스 크기 조정
                  canvas.style.width = '100%';
                  canvas.style.height = '100%';
                }
              }
            } catch (error) {
              console.error('⚠️ Unity 페이지 복원 중 오류 (무시됨):', error);
            }
          }, 100);
        }
      }
    };

    document.addEventListener('visibilitychange', handleVisibilityChange);
    window.addEventListener('focus', handleVisibilityChange);
    window.addEventListener('blur', () => {
      if (window.unityGlobalState.instance?.Module) {
        window.unityGlobalState.instance.Module.pauseMainLoop = false;
      }
    });

    return () => {
      document.removeEventListener('visibilitychange', handleVisibilityChange);
      window.removeEventListener('focus', handleVisibilityChange);
      window.removeEventListener('blur', handleVisibilityChange);
    };
  }, []);

  // Unity와 React 간 통신 설정
  const setupUnityReactCommunication = (unityInstance) => {
    // React에서 Unity로 데이터 전송
    window.SendToUnity = (gameObjectName, methodName, parameter) => {
      try {
        if (unityInstance && unityInstance.SendMessage) {
          unityInstance.SendMessage(gameObjectName, methodName, parameter);
        }
      } catch (error) {
        console.error('Unity 메시지 전송 실패:', error);
      }
    };

    // Unity에서 호버 데이터를 직접 받으므로 별도 이벤트 리스너 불필요

    // Unity에서 React로 데이터 수신 (글로벌 함수)
    window.ReceiveFromUnity = (data) => {
      try {
        const parsedData = typeof data === 'string' ? JSON.parse(data) : data;
        handleUnityData(parsedData);
      } catch (error) {
        console.error('Unity 데이터 파싱 오류:', error);
      }
    };

    // Unity에서 호버 데이터 수신 (로그 없음)
    window.ReceiveHoverFromUnity = (hoverData) => {
      try {
        const data = typeof hoverData === 'string' ? JSON.parse(hoverData) : hoverData;
        
        if (data.type === 'objectHovered') {
          handleUnityHover(data.payload);
        } else if (data.type === 'hoverExit') {
          handleUnityHoverExit();
        }
      } catch (error) {
        // 호버 데이터 파싱 오류 무시
      }
    };

    // Unity에서 직접 호출할 수 있는 함수들
    window.OnRobotClicked = (robotData) => {
      try {
        const data = typeof robotData === 'string' ? JSON.parse(robotData) : robotData;
        handleDigitalTwinClick('robot', data, data.position);
      } catch (error) {
        console.error('로봇 클릭 데이터 파싱 오류:', error);
      }
    };

    window.OnStationClicked = (stationData) => {
      try {
        const data = typeof stationData === 'string' ? JSON.parse(stationData) : stationData;
        handleDigitalTwinClick('station', data, data.position);
      } catch (error) {
        console.error('공정 클릭 데이터 파싱 오류:', error);
      }
    };

    window.OnProductClicked = (productData) => {
      try {
        const data = typeof productData === 'string' ? JSON.parse(productData) : productData;
        handleDigitalTwinClick('product', data, data.position);
      } catch (error) {
        console.error('제품 클릭 데이터 파싱 오류:', error);
      }
    };

    // 회사명 설정 함수 추가
    window.SetCompanyName = (companyName) => {
      try {
        if (unityInstance && unityInstance.SendMessage) {
          unityInstance.SendMessage('CompanyManager', 'SetCompanyName', companyName);
        }
      } catch (error) {
        console.error('회사명 설정 실패:', error);
      }
    };

    // Unity-React 통신 함수 등록 완료
  };

  // Unity 데이터 처리
  const handleUnityData = (data) => {
    switch (data.type) {
      case 'objectClicked':
        handleObjectClick(data.payload);
        break;
      case 'robotClicked':
        handleDigitalTwinClick('robot', data.payload, data.position);
        break;
      case 'stationClicked':
      case 'processClicked':
        handleDigitalTwinClick('station', data.payload, data.position);
        break;
      case 'productClicked':
        handleDigitalTwinClick('product', data.payload, data.position);
        break;
      case 'statusUpdate':
        // 상태 업데이트 처리
        break;
      default:
        // 기타 Unity 데이터 처리
    }
  };

  // Unity ClickableObject에서 오는 일반 클릭 처리
  const handleObjectClick = (payload) => {
    const { objectId, objectType } = payload;
    
    // 화면 중앙 좌표 사용 (Unity에서 좌표를 보내지 않음)
    const position = { x: window.innerWidth / 2, y: window.innerHeight / 2 };
    
    switch (objectType) {
      case 'robot':
        // 로봇 클릭 - objectId에서 robotId 추출
        const robotId = extractRobotId(objectId);
        handleDigitalTwinClick('robot', { robotId }, position);
        break;
        
      case 'station':
        // 공정 클릭 - objectId에서 stationCode 추출  
        const stationCode = extractStationCode(objectId);
        handleDigitalTwinClick('station', { stationCode }, position);
        break;
        
      case 'product':
        // 제품 클릭 - Unity의 CAR_XXX 형태를 데이터베이스 productId로 변환
        const productId = convertCarIdToProductId(objectId);
        handleDigitalTwinClick('product', { productId }, position);
        break;
        
      default:
        break;
    }
  };

  // 유틸리티 함수들
  const extractRobotId = (objectId) => {
    // 예: "ROBOT_001" -> 1, "ARM_ROBOT_A01_001" -> 1
    const match = objectId.match(/(\d+)$/);
    return match ? parseInt(match[1]) : 1;
  };

  const extractStationCode = (objectId) => {
    // 예: "STATION_A01" -> "A01", "ASSEMBLY_A01" -> "A01"
    const match = objectId.match(/([A-D]\d{2})/);
    return match ? match[1] : 'A01';
  };

  const convertCarIdToProductId = (objectId) => {
    // Unity의 CAR_XXX 형태를 데이터베이스의 productId 형태로 변환
    // 예: "CAR_001" -> "A01_PROD_001" (첫 번째 공정 제품으로 가정)
    if (objectId.startsWith('CAR_')) {
      const carNumber = objectId.replace('CAR_', '');
      return `A01_PROD_${carNumber.padStart(3, '0')}`;
    }
    // 이미 올바른 형태라면 그대로 반환
    return objectId;
  };

  // 디지털 트윈 클릭 이벤트 처리
  const handleDigitalTwinClick = (type, payload, position) => {
    const screenX = position?.x || window.innerWidth / 2;
    const screenY = position?.y || window.innerHeight / 2;
    
    setOverlayType(type);
    setOverlayData(payload);
    setOverlayPosition({ x: screenX, y: screenY });
    setOverlayOpen(true);
  };

  // 오버레이 닫기
  const handleOverlayClose = () => {
    setOverlayOpen(false);
    setOverlayData(null);
    setOverlayType(null);
    // 오버레이가 닫히면 호버 툴팁도 숨김
    setHoverVisible(false);
  };

  // Unity에서 직접 호버 데이터를 전송하므로 React 측 감지 로직 불필요

  // Unity에서 온 호버 데이터 처리 (정확한 좌표 사용)
  const handleUnityHover = (payload) => {
    if (overlayOpen) {
      return;
    }
    
    const { objectId, objectType, position } = payload;
    
    // Unity에서 받은 정확한 좌표 사용
    if (!position || typeof position.x !== 'number' || typeof position.y !== 'number') {
      return;
    }
    
    // objectId를 기반으로 호버 데이터 생성
    let hoverData = {};
    
    switch (objectType) {
      case 'robot':
        const robotId = extractRobotId(objectId);
        hoverData = { robotId, status: '운영 중' };
        break;
      case 'station':
        const stationCode = extractStationCode(objectId);
        hoverData = { stationCode, status: '가동 중' };
        break;
      case 'product':
        const productId = convertCarIdToProductId(objectId);
        hoverData = { productId, currentStation: 'A01' };
        break;
      default:
        hoverData = { objectId, objectType };
    }
    
    // 간단한 좌표 보정: Unity 캔버스 영역 찾기
    const unityContainer = document.querySelector('[data-unity-canvas], canvas, #unity-canvas');
    let adjustedPosition = { x: position.x, y: position.y };
    
    if (unityContainer) {
      const rect = unityContainer.getBoundingClientRect();
      
      // 정규화된 Unity 좌표(0~1)를 실제 픽셀 좌표로 변환
      adjustedPosition = {
        x: rect.left + (position.x * rect.width),
        y: rect.top + (position.y * rect.height)
      };
    } else {
      // 대시보드가 왼쪽에 있다고 가정하고 오프셋 추가
      adjustedPosition = {
        x: position.x + 450, // 대시보드 너비만큼 오프셋
        y: position.y + 50   // 상단 여백
      };
    }
    
    setHoverType(objectType);
    setHoverData(hoverData);
    setHoverPosition(adjustedPosition);
    setHoverVisible(true);
  };

  // Unity 호버 종료 처리  
  const handleUnityHoverExit = () => {
    setHoverVisible(false);
    setHoverData(null);
    setHoverType(null);
    setHoverPosition({ x: 0, y: 0 });
  };

  // 실시간 데이터 폴링 시작
  const startRealtimeDataPolling = () => {
    // 기존 폴링이 있다면 정리
    if (realtimeIntervalRef.current) {
      clearInterval(realtimeIntervalRef.current);
    }

    // 3초마다 실시간 데이터 조회 및 Unity 전송
    realtimeIntervalRef.current = setInterval(async () => {
      try {
        // 현재 로그인한 사용자의 회사명 가져오기
        const userData = localStorage.getItem('userData');
        let companyName = 'u1mobis'; // 기본값
        
        if (userData) {
          try {
            const user = JSON.parse(userData);
            if (user.companyName) {
              companyName = user.companyName;
            }
          } catch (e) {
            console.warn('사용자 데이터 파싱 실패, 기본 회사명 사용');
          }
        }

        // API 서비스를 통한 인증된 호출
        const { apiService } = await import('../../service/apiService');
        const data = await apiService.unity.getRealtimeData(companyName);
        
        setRealtimeData(data);
        sendRealtimeDataToUnity(data);
      } catch (error) {
        console.error('실시간 데이터 조회 실패:', error);
      }
    }, 3000);
  };

  // Unity로 실시간 데이터 전송
  const sendRealtimeDataToUnity = (data) => {
    if (!window.unityGlobalState?.instance) {
      console.warn('⚠️ Unity 인스턴스가 없어서 데이터 전송 불가');
      return;
    }

    console.log('🔄 Unity로 데이터 전송 시도:', data);
    
    // 테스트 데이터가 없으면 더미 데이터 생성
    if (!data || Object.keys(data).length === 0) {
      console.log('📝 테스트 데이터 생성 중...');
      data = {
        products: {
          'CAR_Line1_001': {
            position: { x: 10, y: 0, z: 5 },
            status: 'moving',
            currentStation: 'DoorStation'
          },
          'CAR_Line2_001': {
            position: { x: -10, y: 0, z: 15 },
            status: 'processing',
            currentStation: 'WaterTestStation'
          }
        },
        stations: {
          'DoorStation_Line1': {
            status: 'operating',
            currentProduct: 'CAR_Line1_001',
            efficiency: 85.5
          },
          'WaterLeakTestStation_Line2': {
            status: 'idle',
            currentProduct: null,
            efficiency: 92.0
          }
        },
        robots: {
          'Robot_FrontRight_Line2': {
            status: 'active',
            currentTask: 'working',
            batteryLevel: 75
          }
        }
      };
      console.log('🧪 테스트 데이터 생성됨:', data);
    }
    
    try {
      const unityInstance = window.unityGlobalState.instance;

      // 제품 위치 업데이트
      if (data.products) {
        Object.entries(data.products).forEach(([carId, productData]) => {
          const position = productData.position;
          const updateData = {
            carId: carId,
            position: position,
            status: productData.status,
            currentStation: productData.currentStation
          };
          
          console.log('📤 차량 데이터 전송:', updateData);
          unityInstance.SendMessage('DigitalTwinManager', 'UpdateProductPosition', JSON.stringify(updateData));
        });
      }

      // 공정 상태 업데이트
      if (data.stations) {
        Object.entries(data.stations).forEach(([stationId, stationData]) => {
          const updateData = {
            stationId: stationId,
            status: stationData.status,
            currentProduct: stationData.currentProduct,
            efficiency: stationData.efficiency
          };
          
          console.log('📤 스테이션 데이터 전송:', updateData);
          unityInstance.SendMessage('DigitalTwinManager', 'UpdateStationStatus', JSON.stringify(updateData));
        });
      }

      // 로봇 상태 업데이트
      if (data.robots) {
        Object.entries(data.robots).forEach(([robotId, robotData]) => {
          const updateData = {
            robotId: robotId,
            status: robotData.status,
            currentTask: robotData.currentTask,
            batteryLevel: robotData.batteryLevel
          };
          
          console.log('📤 로봇 데이터 전송:', updateData);
          unityInstance.SendMessage('DigitalTwinManager', 'UpdateRobotStatus', JSON.stringify(updateData));
        });
      }

      console.log('✅ Unity 데이터 전송 완료');
    } catch (error) {
      console.error('❌ Unity 데이터 전송 실패:', error);
    }
  };

  // Unity 통신 설정 초기화
  const setupTestFunctions = () => {
    // Unity-React 통신 함수 등록 완료
  };

  const handleRetry = () => {
    // 전역 상태 리셋
    if (window.unityGlobalState.instance) {
      try {
        if (typeof window.unityGlobalState.instance.Quit === 'function') {
          window.unityGlobalState.instance.Quit();
        }
      } catch (e) {
        // Unity 정리 오류 무시
      }
    }
    
    // 상태 초기화
    window.unityGlobalState.instance = null;
    window.unityGlobalState.isLoaded = false;
    window.unityGlobalState.canvas = null;
    
    setErrorMessage('');
    setLoadingProgress(0);
    setIsUnityLoaded(false);
    isLoadingRef.current = false;
    unityInstanceRef.current = null;
    
    // 진행률 타이머 정리
    if (progressIntervalRef.current) {
      clearInterval(progressIntervalRef.current);
      progressIntervalRef.current = null;
    }
    
    // Unity 스크립트 제거
    try {
      const scripts = document.querySelectorAll('script[src*="factoryTwin"]');
      scripts.forEach(script => {
        script.parentNode?.removeChild(script);
      });
    } catch (e) {
      // 스크립트 제거 오류 무시
    }
    
    // 페이지 새로고침
    setTimeout(() => window.location.reload(), 500);
  };

  const handleSkipUnity = () => {
    setErrorMessage('');
    setIsUnityLoaded(true);
    setLoadingProgress(100);
    isLoadingRef.current = false;
    
    if (progressIntervalRef.current) {
      clearInterval(progressIntervalRef.current);
      progressIntervalRef.current = null;
    }
  };

  return (
    <div style={{ width: '100%', height: '100%', position: 'relative' }}>
      {/* Unity 캔버스 컨테이너 */}
      <div
        ref={unityContainerRef}
        style={{
          width: '100%',
          height: '100%',
          display: isUnityLoaded ? 'block' : 'none',
          backgroundColor: '#2c3e50'
        }}
      >
        {/* 기존 Unity 캔버스가 없을 때만 새 캔버스 생성 */}
        {!window.unityGlobalState.canvas && (
          <canvas
            id="unity-canvas"
            style={{
              width: '100%',
              height: '100%'
            }}
          />
        )}
      </div>
      
      {/* 로딩 화면 */}
      {!isUnityLoaded && !errorMessage && (
        <div
          style={{
            position: 'absolute',
            top: 0,
            left: 0,
            width: '100%',
            height: '100%',
            display: 'flex',
            flexDirection: 'column',
            justifyContent: 'center',
            alignItems: 'center',
            backgroundColor: '#f8f9fa'
          }}
        >
          <div style={{ marginBottom: '20px', fontSize: '18px', color: '#6c757d' }}>
            3D 팩토리 로딩 중... (백그라운드 실행)
          </div>
          
          <div
            style={{
              width: '300px',
              height: '20px',
              backgroundColor: '#e9ecef',
              borderRadius: '10px',
              overflow: 'hidden',
              marginBottom: '10px'
            }}
          >
            <div
              style={{
                width: `${loadingProgress}%`,
                height: '100%',
                backgroundColor: loadingProgress > 90 ? '#28a745' : '#007bff',
                transition: 'width 0.3s ease'
              }}
            />
          </div>
          
          <div style={{ marginBottom: '15px', color: '#6c757d' }}>
            {Math.round(loadingProgress)}%
          </div>
          
          <div style={{ fontSize: '12px', color: '#6c757d', textAlign: 'center' }}>
            💡 페이지를 이동해도 Unity는 백그라운드에서 계속 실행됩니다
          </div>
        </div>
      )}

      {/* 에러 화면 */}
      {errorMessage && (
        <div
          style={{
            position: 'absolute',
            top: '50%',
            left: '50%',
            transform: 'translate(-50%, -50%)',
            textAlign: 'center',
            padding: '20px',
            backgroundColor: 'white',
            borderRadius: '8px',
            boxShadow: '0 2px 10px rgba(0,0,0,0.1)',
            maxWidth: '500px'
          }}
        >
          <div style={{ marginBottom: '10px', fontSize: '16px', fontWeight: 'bold', color: '#dc3545' }}>
            Unity 3D 로드 실패
          </div>
          <div style={{ marginBottom: '15px', fontSize: '14px', color: '#6c757d' }}>
            {errorMessage}
          </div>
          <div style={{ marginBottom: '15px', fontSize: '12px', color: '#6c757d', textAlign: 'left' }}>
            <strong>확인사항:</strong><br/>
            1. 파일 경로: /public/unity3d/factoryTwin.*<br/>
            2. 브라우저 개발자 도구 → Network 탭에서 404 에러 확인<br/>
            3. Unity 빌드 설정에서 압축 해제 + Run in Background 체크<br/>
            4. 파일 권한 확인
          </div>
          <div style={{ display: 'flex', gap: '10px', justifyContent: 'center' }}>
            <button
              onClick={handleRetry}
              style={{
                padding: '8px 16px',
                backgroundColor: '#007bff',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer'
              }}
            >
              다시 시도
            </button>
            <button
              onClick={handleSkipUnity}
              style={{
                padding: '8px 16px',
                backgroundColor: '#6c757d',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer'
              }}
            >
              Unity 건너뛰기
            </button>
          </div>
        </div>
      )}

      {/* Unity 로드 성공 후 대체 컨텐츠 */}
      {isUnityLoaded && !window.unityGlobalState?.instance && (
        <div
          style={{
            width: '100%',
            height: '100%',
            display: 'flex',
            flexDirection: 'column',
            justifyContent: 'center',
            alignItems: 'center',
            backgroundColor: '#2c3e50',
            color: 'white',
            fontSize: '18px',
            position: 'relative'
          }}
        >
          <div style={{ marginBottom: '20px' }}>
            Unity 3D 디지털 트윈
            <br />
            <small style={{ marginTop: '10px', display: 'block', opacity: 0.7 }}>
              Unity 3D 팩토리 씬이 여기에 표시됩니다
            </small>
          </div>
          
          <div style={{ 
            marginTop: '20px', 
            fontSize: '14px', 
            opacity: 0.8,
            textAlign: 'center',
            maxWidth: '400px'
          }}>
            클릭 가능한 오브젝트:
            <br />
            • 로봇 (Robot, Arm)
            <br />
            • 공정 설비 (Station, Assembly, Process)
          </div>
        </div>
      )}

      {/* 디지털 트윈 오버레이 */}
      <DigitalTwinOverlay
        isOpen={overlayOpen}
        onClose={handleOverlayClose}
        clickType={overlayType}
        clickData={overlayData}
        position={overlayPosition}
      />

      {/* 호버 툴팁 */}
      <HoverTooltip
        isVisible={hoverVisible}
        position={hoverPosition}
        data={hoverData}
        type={hoverType}
      />
      
    </div>
  );
};

export default Factory3DTwin;