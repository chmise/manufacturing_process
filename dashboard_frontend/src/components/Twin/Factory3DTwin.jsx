import React, { useRef, useEffect, useState } from 'react';
import DigitalTwinOverlay from './DigitalTwinOverlay';

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

  useEffect(() => {
    console.log('Factory3DTwin 마운트');
    
    // Unity 없이도 테스트 함수들을 사용할 수 있도록 설정
    setupTestFunctions();

    // 전역 Unity 인스턴스가 이미 있는지 확인
    if (window.unityGlobalState.instance) {
      console.log('✅ 기존 Unity 인스턴스 재사용');
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
                
                console.log('✅ Unity 캔버스 재연결 및 크기 조정 완료 (전체화면 없음)');
              }
            }
          }
        }, 100);
      }
      return;
    }

    const loadUnity = async () => {
      // 이미 로딩 중이거나 로드된 경우 중복 방지
      if (isLoadingRef.current || window.unityGlobalState.isLoaded) {
        console.log('Unity 이미 로드됨 또는 로딩 중 - 스킵');
        return;
      }

      try {
        console.log('Unity 로드 시작...');
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

        console.log('Unity 설정:', config);

        // Unity 로더 방식 확인
        const loadUnityInstance = () => {
          // 컴포넌트가 언마운트되었는지 확인
          if (!isLoadingRef.current) {
            console.log('컴포넌트 언마운트됨 - Unity 로딩 중단');
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
            console.log('createUnityInstance 사용');
            
            window.createUnityInstance(canvas, config, (progress) => {
              const progressPercent = progress * 100;
              setLoadingProgress(progressPercent);
            }).then((unityInstance) => {
              // 로딩 중에 컴포넌트가 언마운트되었는지 확인
              if (!isLoadingRef.current) {
                console.log('컴포넌트 언마운트됨 - Unity 인스턴스 정리');
                if (unityInstance && typeof unityInstance.Quit === 'function') {
                  try {
                    unityInstance.Quit();
                  } catch (e) {
                    console.log('Unity 정리 중 오류 (무시됨):', e.message);
                  }
                }
                return;
              }

              console.log('✅ Unity 로드 성공! 백그라운드 실행 설정');
              
              // 백그라운드 실행을 위한 설정 (전체화면 방지)
              if (unityInstance.Module) {
                unityInstance.Module.pauseMainLoop = false;
                unityInstance.Module.noExitRuntime = true;
                
                // 전체화면 방지 설정
                unityInstance.Module.requestFullscreen = false;
                if (unityInstance.Module.canvas) {
                  unityInstance.Module.canvas.requestFullscreen = null;
                }
                
                console.log('✅ Unity 백그라운드 실행 설정 완료 (전체화면 방지)');
              }
              
              // 전역 상태에 저장 (백그라운드 실행을 위해)
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
            console.log('Unity 로더 대기 중...');
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
            console.log('Unity Framework 로드 완료');
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
          console.log('Unity Loader 로드 완료');
          loadFramework();
        };
        
        loaderScript.onerror = () => {
          console.log('Loader 없음 - Framework 직접 로드');
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
      console.log('Factory3DTwin 컴포넌트 언마운트 - Unity 인스턴스는 백그라운드에서 유지');
      clearTimeout(timer);
      isLoadingRef.current = false;

      // 진행률 타이머만 정리 (Unity 인스턴스는 유지)
      if (progressIntervalRef.current) {
        clearInterval(progressIntervalRef.current);
        progressIntervalRef.current = null;
      }

      // Unity 인스턴스는 전역 상태에서 계속 유지
      // window.unityGlobalState에 저장되어 있어서 다른 페이지 갔다와도 계속 실행됨
    };
  }, []);

  // 컨테이너 변경 감지 및 Unity 재연결
  useEffect(() => {
    if (unityContainerRef.current && window.unityGlobalState.instance && isUnityLoaded) {
      console.log('🔄 Unity 컨테이너 재연결 확인');
      
      const container = unityContainerRef.current;
      const canvas = window.unityGlobalState.canvas;
      
      // 캔버스가 현재 컨테이너에 없으면 재연결
      if (canvas && !container.contains(canvas)) {
        console.log('🔧 Unity 캔버스 재연결 시작');
        
        // 기존 위치에서 제거
        if (canvas.parentNode) {
          canvas.parentNode.removeChild(canvas);
        }
        
        // 새 컨테이너에 추가
        container.appendChild(canvas);
        
        // Unity 강제 리프레시
        setTimeout(() => {
          if (window.unityGlobalState.instance) {
            try {
              // Unity 해상도 및 렌더링 강제 업데이트
              const unityInstance = window.unityGlobalState.instance;
              
              // 캔버스 크기 강제 설정
              canvas.style.width = '100%';
              canvas.style.height = '100%';
              
              // Unity Module 직접 조작
              if (unityInstance.Module) {
                // WebGL 컨텍스트 강제 복원
                if (unityInstance.Module.canvas) {
                  const gl = unityInstance.Module.canvas.getContext('webgl') || 
                            unityInstance.Module.canvas.getContext('experimental-webgl');
                  if (gl && gl.isContextLost && gl.isContextLost()) {
                    console.log('🔄 WebGL 컨텍스트 복원 시도');
                  }
                }
                
                // Unity 렌더링 강제 재시작
                unityInstance.Module.pauseMainLoop = false;
                
                // 화면 크기 변경 이벤트 강제 발생
                if (typeof unityInstance.SendMessage === 'function') {
                  unityInstance.SendMessage('*', 'OnApplicationFocus', 'true');
                }
              }
              
              console.log('✅ Unity 재연결 완료');
              
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
          console.log('👁️ 페이지 다시 보임 - Unity 활성화');
          
          // 페이지가 다시 보일 때 Unity 강제 리프레시
          setTimeout(() => {
            try {
              const unityInstance = window.unityGlobalState.instance;
              const canvas = window.unityGlobalState.canvas;
              
              if (unityInstance && canvas) {
                // Unity에 포커스 복원 알림 (전체화면 제거)
                if (typeof unityInstance.SendMessage === 'function') {
                  unityInstance.SendMessage('*', 'OnApplicationFocus', 'true');
                  unityInstance.SendMessage('*', 'OnApplicationPause', 'false');
                }
                
                // 렌더링 강제 재시작 (전체화면 없이)
                if (unityInstance.Module) {
                  unityInstance.Module.pauseMainLoop = false;
                  
                  // WebGL 컨텍스트 확인 및 복원
                  const gl = canvas.getContext('webgl') || canvas.getContext('experimental-webgl');
                  if (gl) {
                    gl.viewport(0, 0, canvas.width, canvas.height);
                  }
                  
                  // 캔버스 크기만 조정 (전체화면 하지 않음)
                  canvas.style.width = '100%';
                  canvas.style.height = '100%';
                }
                
                console.log('✅ Unity 페이지 복원 완료 (전체화면 없음)');
              }
            } catch (error) {
              console.error('⚠️ Unity 페이지 복원 중 오류 (무시됨):', error);
            }
          }, 100);
        } else {
          console.log('🔒 페이지 숨김 - Unity 백그라운드 실행 유지');
        }
      }
    };

    document.addEventListener('visibilitychange', handleVisibilityChange);
    window.addEventListener('focus', handleVisibilityChange);
    window.addEventListener('blur', () => {
      if (window.unityGlobalState.instance && window.unityGlobalState.instance.Module) {
        // 브라우저 포커스 잃어도 Unity 계속 실행
        window.unityGlobalState.instance.Module.pauseMainLoop = false;
        console.log('🌙 브라우저 포커스 잃음 - Unity 백그라운드 실행 유지');
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
    console.log('Unity-React 통신 설정');
    
    // React에서 Unity로 데이터 전송
    window.SendToUnity = (gameObjectName, methodName, parameter) => {
      try {
        if (unityInstance && unityInstance.SendMessage) {
          unityInstance.SendMessage(gameObjectName, methodName, parameter);
          console.log('Unity로 메시지 전송:', { gameObjectName, methodName, parameter });
        }
      } catch (error) {
        console.error('Unity 메시지 전송 실패:', error);
      }
    };

    // Unity에서 React로 데이터 수신 (글로벌 함수)
    window.ReceiveFromUnity = (data) => {
      try {
        const parsedData = typeof data === 'string' ? JSON.parse(data) : data;
        console.log('Unity에서 받은 데이터:', parsedData);
        handleUnityData(parsedData);
      } catch (error) {
        console.error('Unity 데이터 파싱 오류:', error);
      }
    };

    // Unity에서 직접 호출할 수 있는 함수들
    window.OnRobotClicked = (robotData) => {
      console.log('로봇 클릭 이벤트:', robotData);
      try {
        const data = typeof robotData === 'string' ? JSON.parse(robotData) : robotData;
        handleDigitalTwinClick('robot', data, data.position);
      } catch (error) {
        console.error('로봇 클릭 데이터 파싱 오류:', error);
      }
    };

    window.OnStationClicked = (stationData) => {
      console.log('공정 클릭 이벤트:', stationData);
      try {
        const data = typeof stationData === 'string' ? JSON.parse(stationData) : stationData;
        handleDigitalTwinClick('station', data, data.position);
      } catch (error) {
        console.error('공정 클릭 데이터 파싱 오류:', error);
      }
    };

    window.OnProductClicked = (productData) => {
      console.log('제품 클릭 이벤트:', productData);
      try {
        const data = typeof productData === 'string' ? JSON.parse(productData) : productData;
        handleDigitalTwinClick('product', data, data.position);
      } catch (error) {
        console.error('제품 클릭 데이터 파싱 오류:', error);
      }
    };

    // 테스트용 함수들 (개발 단계에서 사용)
    window.TestRobotClick = () => {
      const testData = {
        robotId: 1,
        position: { x: window.innerWidth / 2, y: window.innerHeight / 2 }
      };
      handleDigitalTwinClick('robot', testData, testData.position);
    };

    window.TestStationClick = () => {
      const testData = {
        stationCode: 'A01',
        position: { x: window.innerWidth / 2, y: window.innerHeight / 2 }
      };
      handleDigitalTwinClick('station', testData, testData.position);
    };

    window.TestProductClick = () => {
      const testData = {
        productId: 'A01_PROD_001',
        position: { x: window.innerWidth / 2, y: window.innerHeight / 2 }
      };
      handleDigitalTwinClick('product', testData, testData.position);
    };

    console.log('✅ Unity-React 통신 함수들이 window 객체에 등록되었습니다:');
    console.log('- window.OnRobotClicked(robotData)');
    console.log('- window.OnStationClicked(stationData)');  
    console.log('- window.OnProductClicked(productData)');
    console.log('- window.TestRobotClick() (테스트용)');
    console.log('- window.TestStationClick() (테스트용)');
    console.log('- window.TestProductClick() (테스트용)');
  };

  // Unity 데이터 처리 (ClickableObject.cs와 호환)
  const handleUnityData = (data) => {
    console.log('Unity에서 받은 데이터:', data);
    
    switch (data.type) {
      case 'objectClicked':
        console.log('오브젝트 클릭됨:', data.payload);
        handleObjectClick(data.payload);
        break;
      case 'robotClicked':
        console.log('로봇 클릭됨:', data.payload);
        handleDigitalTwinClick('robot', data.payload, data.position);
        break;
      case 'stationClicked':
      case 'processClicked':
        console.log('공정 클릭됨:', data.payload);
        handleDigitalTwinClick('station', data.payload, data.position);
        break;
      case 'productClicked':
        console.log('제품 클릭됨:', data.payload);
        handleDigitalTwinClick('product', data.payload, data.position);
        break;
      case 'statusUpdate':
        console.log('상태 업데이트:', data.payload);
        break;
      default:
        console.log('기타 Unity 데이터:', data);
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
        console.log('알 수 없는 오브젝트 타입:', objectType);
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
    console.log('디지털 트윈 클릭:', { type, payload, position });
    
    // 화면 좌표로 변환 (Unity에서 받은 좌표가 있다면 사용, 없으면 기본값)
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
  };

  // 테스트 함수들 설정 (Unity 없이도 사용 가능)
  const setupTestFunctions = () => {
    // 테스트용 함수들 (Unity ClickableObject 시뮬레이션)
    window.TestRobotClick = () => {
      // Unity ClickableObject 형태로 테스트
      const testData = {
        type: 'objectClicked',
        payload: {
          objectId: 'ROBOT_001',
          objectType: 'robot'
        }
      };
      handleUnityData(testData);
    };

    window.TestStationClick = () => {
      // Unity ClickableObject 형태로 테스트
      const testData = {
        type: 'objectClicked',
        payload: {
          objectId: 'STATION_A01',
          objectType: 'station'
        }
      };
      handleUnityData(testData);
    };

    window.TestProductClick = () => {
      // Unity CarSpawner에서 생성하는 차량 ID 형태로 테스트
      const testData = {
        type: 'objectClicked',
        payload: {
          objectId: 'CAR_001',
          objectType: 'product'
        }
      };
      handleUnityData(testData);
    };

    // Unity에서 직접 차량 클릭 테스트
    window.TestCarClick = (carId = 'CAR_001') => {
      const testData = {
        type: 'objectClicked',
        payload: {
          objectId: carId,
          objectType: 'product'
        }
      };
      handleUnityData(testData);
    };

    console.log('✅ 테스트 함수들이 등록되었습니다 (Unity 불필요)');
  };

  const handleRetry = () => {
    console.log('Unity 재시도 시작');
    
    // 전역 상태 리셋
    if (window.unityGlobalState.instance) {
      try {
        if (typeof window.unityGlobalState.instance.Quit === 'function') {
          window.unityGlobalState.instance.Quit();
        }
      } catch (e) {
        console.log('재시도 중 Unity 정리 오류 (무시됨):', e.message);
      }
    }
    
    window.unityGlobalState.instance = null;
    window.unityGlobalState.isLoaded = false;
    window.unityGlobalState.canvas = null;
    
    // 기존 상태 리셋
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
    
    // 기존 스크립트 제거
    try {
      const scripts = document.querySelectorAll('script[src*="factoryTwin"]');
      scripts.forEach(script => {
        if (script.parentNode) {
          script.parentNode.removeChild(script);
        }
      });
    } catch (e) {
      console.log('스크립트 제거 중 오류 (무시됨):', e.message);
    }
    
    // 페이지 새로고침
    setTimeout(() => {
      window.location.reload();
    }, 500);
  };

  const handleSkipUnity = () => {
    console.log('Unity 건너뛰기');
    setErrorMessage('');
    setIsUnityLoaded(true);
    setLoadingProgress(100);
    isLoadingRef.current = false;
    
    // 진행률 타이머 정리
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
            Unity 3D 뷰어 (백그라운드 실행)
            <br />
            <small style={{ marginTop: '10px', display: 'block', opacity: 0.7 }}>
              Unity 씬이 여기에 표시됩니다
            </small>
          </div>
          
          {/* 테스트 버튼들 (Unity가 없을 때만 표시) */}
          <div style={{ 
            display: 'flex', 
            gap: '10px', 
            flexWrap: 'wrap',
            justifyContent: 'center'
          }}>
            <button
              onClick={() => window.TestRobotClick && window.TestRobotClick()}
              style={{
                padding: '10px 15px',
                backgroundColor: '#4CAF50',
                color: 'white',
                border: 'none',
                borderRadius: '5px',
                cursor: 'pointer',
                fontSize: '14px'
              }}
            >
              🤖 로봇 테스트
            </button>
            <button
              onClick={() => window.TestStationClick && window.TestStationClick()}
              style={{
                padding: '10px 15px',
                backgroundColor: '#2196F3',
                color: 'white',
                border: 'none',
                borderRadius: '5px',
                cursor: 'pointer',
                fontSize: '14px'
              }}
            >
              🏭 공정 테스트
            </button>
            <button
              onClick={() => window.TestProductClick && window.TestProductClick()}
              style={{
                padding: '10px 15px',
                backgroundColor: '#FF9800',
                color: 'white',
                border: 'none',
                borderRadius: '5px',
                cursor: 'pointer',
                fontSize: '14px'
              }}
            >
              📦 제품 테스트
            </button>
            <button
              onClick={() => window.TestCarClick && window.TestCarClick('CAR_005')}
              style={{
                padding: '10px 15px',
                backgroundColor: '#9C27B0',
                color: 'white',
                border: 'none',
                borderRadius: '5px',
                cursor: 'pointer',
                fontSize: '14px'
              }}
            >
              🚗 차량 테스트
            </button>
          </div>
          
          <div style={{ 
            marginTop: '20px', 
            fontSize: '12px', 
            opacity: 0.7,
            textAlign: 'center'
          }}>
            위 버튼들을 클릭하여 디지털 트윈 오버레이를 테스트할 수 있습니다
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
      
    </div>
  );
};

export default Factory3DTwin;