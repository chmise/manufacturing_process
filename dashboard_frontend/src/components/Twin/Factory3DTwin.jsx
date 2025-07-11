// src/components/Twin/Factory3DTwin.jsx
import React, { useRef, useEffect, useState } from 'react';
import unityManager from '../../utils/UnityManager';

const Factory3DTwin = () => {
  const unityContainerRef = useRef(null);
  const [isUnityLoaded, setIsUnityLoaded] = useState(false);
  const [loadingProgress, setLoadingProgress] = useState(0);
  const [errorMessage, setErrorMessage] = useState('');
  const [unityMessages, setUnityMessages] = useState([]);

  useEffect(() => {
    let mounted = true;

    // Unity 매니저 상태 구독
    const unsubscribe = unityManager.subscribe((state) => {
      if (!mounted) return;

      switch (state.type) {
        case 'progress':
          setLoadingProgress(state.progress);
          break;
        case 'loaded':
          setIsUnityLoaded(true);
          setLoadingProgress(100);
          setErrorMessage('');
          break;
        case 'error':
          setErrorMessage(state.error.message || state.error.toString());
          setLoadingProgress(0);
          break;
        case 'message':
          handleUnityData(state.data);
          break;
      }
    });

    // 초기 상태 확인
    const currentState = unityManager.getState();
    if (currentState.isLoaded) {
      console.log('Unity 이미 로드됨 - 캔버스 재연결 시도');
      setIsUnityLoaded(true);
      setLoadingProgress(100);
      
      // 캔버스에 Unity 재연결
      setTimeout(() => {
        const connected = unityManager.attachToCanvas('unity-canvas');
        if (connected) {
          console.log('Unity 캔버스 재연결 성공');
        } else {
          console.log('Unity 캔버스 재연결 실패 - 재로드 시도');
          setIsUnityLoaded(false);
          setLoadingProgress(0);
          unityManager.loadUnity('unity-canvas').catch(error => {
            if (mounted) {
              console.error('Unity 재로드 실패:', error);
              setErrorMessage(error.message);
            }
          });
        }
      }, 100);
    } else if (!currentState.isLoading) {
      // Unity 로드 시작
      console.log('Unity 로드 요청');
      setLoadingProgress(0);
      unityManager.loadUnity('unity-canvas').catch(error => {
        if (mounted) {
          console.error('Unity 로드 실패:', error);
          setErrorMessage(error.message);
        }
      });
    }

    return () => {
      mounted = false;
      unsubscribe();
    };
  }, []);

  // Unity 데이터 처리
  const handleUnityData = (data) => {
    setUnityMessages(prev => [...prev.slice(-9), data]); // 최근 10개만 유지
    
    switch (data.type) {
      case 'robotClicked':
        console.log('로봇 클릭됨:', data.payload);
        // TODO: 로봇 정보 모달 표시
        break;
      case 'processClicked':
        console.log('공정 클릭됨:', data.payload);
        // TODO: 공정 정보 표시
        break;
      case 'statusUpdate':
        console.log('상태 업데이트:', data.payload);
        // TODO: 실시간 상태 업데이트
        break;
      default:
        console.log('기타 Unity 데이터:', data);
    }
  };

  const handleRetry = () => {
    console.log('Unity 재시도 시작');
    
    // 상태 리셋
    setErrorMessage('');
    setLoadingProgress(0);
    setIsUnityLoaded(false);
    
    // 먼저 강제 재연결 시도
    const reconnected = unityManager.forceReconnectCanvas('unity-canvas');
    if (reconnected) {
      console.log('Unity 강제 재연결 성공');
      setIsUnityLoaded(true);
      setLoadingProgress(100);
    } else {
      // 재연결 실패 시 완전 재로드
      console.log('Unity 강제 재연결 실패 - 완전 재로드');
      unityManager.loadUnity('unity-canvas').catch(error => {
        console.error('Unity 재시도 실패:', error);
        setErrorMessage(error.message);
      });
    }
  };

  const handleSkipUnity = () => {
    console.log('Unity 건너뛰기');
    setErrorMessage('');
    setIsUnityLoaded(true);
    setLoadingProgress(100);
  };

  // Unity에 메시지 전송 함수들
  const sendToUnity = {
    updateRobotStatus: (robotId, status) => {
      if (window.SendToUnity) {
        window.SendToUnity('GameManager', 'UpdateRobotStatus', JSON.stringify({
          robotId,
          status
        }));
      }
    },
    
    highlightProcess: (processId) => {
      if (window.SendToUnity) {
        window.SendToUnity('GameManager', 'HighlightProcess', processId);
      }
    },
    
    updateProductionData: (data) => {
      if (window.SendToUnity) {
        window.SendToUnity('GameManager', 'UpdateProductionData', JSON.stringify(data));
      }
    }
  };

  return (
    <div style={{ width: '100%', height: '100%', position: 'relative' }}>
      {/* Unity 캔버스 */}
      <canvas
        id="unity-canvas"
        ref={unityContainerRef}
        style={{
          width: '100%',
          height: '100%',
          display: isUnityLoaded ? 'block' : 'none',
          backgroundColor: '#2c3e50'
        }}
      />
      
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
            3D 팩토리 로딩 중...
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
                backgroundColor: loadingProgress > 90 ? '#ffc107' : '#007bff',
                transition: 'width 0.3s ease'
              }}
            />
          </div>
          
          <div style={{ marginBottom: '15px', color: '#6c757d' }}>
            {Math.round(loadingProgress)}%
          </div>
          
          <div style={{ fontSize: '12px', color: '#6c757d', textAlign: 'center' }}>
            💡 페이지를 다시 방문해도 다시 로딩하지 않습니다
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
            3. Unity 빌드 설정에서 압축 해제<br/>
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
              건너뛰기
            </button>
          </div>
        </div>
      )}

      {/* Unity 로드 성공 후 대체 컨텐츠 */}
      {isUnityLoaded && !unityManager.getState().canvasConnected && (
        <div
          style={{
            position: 'absolute',
            top: '50%',
            left: '50%',
            transform: 'translate(-50%, -50%)',
            textAlign: 'center',
            color: 'white',
            fontSize: '16px',
            backgroundColor: 'rgba(0,0,0,0.8)',
            padding: '20px',
            borderRadius: '8px'
          }}
        >
          Unity 캔버스 연결 중...
          <br />
          <button
            onClick={handleRetry}
            style={{
              marginTop: '10px',
              padding: '5px 15px',
              backgroundColor: '#007bff',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer'
            }}
          >
            다시 연결
          </button>
        </div>
      )}

      {/* Unity 로드 성공하고 캔버스 연결됨 - 테스트 UI */}
      {isUnityLoaded && unityManager.getState().canvasConnected && (
        <div
          style={{
            position: 'absolute',
            bottom: '20px',
            right: '20px',
            backgroundColor: 'rgba(0,0,0,0.7)',
            color: 'white',
            padding: '10px',
            borderRadius: '5px',
            fontSize: '12px'
          }}
        >
          Unity 3D 뷰어 활성화됨
          
          {/* 테스트 버튼들 */}
          <div style={{ marginTop: '10px', display: 'flex', gap: '5px' }}>
            <button
              onClick={() => sendToUnity.updateRobotStatus('Robot_01', 'running')}
              style={{
                padding: '3px 8px',
                backgroundColor: '#28a745',
                color: 'white',
                border: 'none',
                borderRadius: '3px',
                cursor: 'pointer',
                fontSize: '10px'
              }}
            >
              로봇 상태
            </button>
            <button
              onClick={() => sendToUnity.highlightProcess('Process_A')}
              style={{
                padding: '3px 8px',
                backgroundColor: '#ffc107',
                color: 'black',
                border: 'none',
                borderRadius: '3px',
                cursor: 'pointer',
                fontSize: '10px'
              }}
            >
              공정 강조
            </button>
          </div>
        </div>
      )}

      {/* Unity 메시지 로그 (개발용) */}
      {unityMessages.length > 0 && process.env.NODE_ENV === 'development' && (
        <div
          style={{
            position: 'absolute',
            bottom: '10px',
            right: '10px',
            backgroundColor: 'rgba(0,0,0,0.8)',
            color: 'white',
            padding: '10px',
            borderRadius: '5px',
            fontSize: '11px',
            maxWidth: '300px',
            maxHeight: '150px',
            overflow: 'auto'
          }}
        >
          <div style={{ fontWeight: 'bold', marginBottom: '5px' }}>Unity 메시지:</div>
          {unityMessages.map((msg, index) => (
            <div key={index} style={{ marginBottom: '2px' }}>
              {JSON.stringify(msg, null, 1)}
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default Factory3DTwin;