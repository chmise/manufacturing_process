import { useEffect, useRef, useState } from 'react';
import { apiService, tokenManager } from '../service/apiService';
import { getWebSocketUrl } from '../config';

// 웹소켓 라이브러리를 동적으로 로드하여 에러 방지
let SockJS, Stomp;

const loadWebSocketLibraries = async () => {
  try {
    if (!SockJS) {
      const sockjsModule = await import('sockjs-client');
      SockJS = sockjsModule.default;
    }
    if (!Stomp) {
      const stompModule = await import('@stomp/stompjs');
      Stomp = stompModule.Client;
    }
    return { SockJS, Stomp };
  } catch (error) {
    console.error('웹소켓 라이브러리 로드 오류:', error);
    return null;
  }
};

/**
 * 회사별 알림 웹소켓 (STOMP over SockJS)
 *
 * 연결할 때 로그인 토큰을 보내고, 자기 회사 알림(/topic/alerts/{회사명})만 구독한다.
 * 대시보드 숫자는 웹소켓이 아니라 폴링으로 갱신한다.
 */
const useWebSocket = (companyName, userId) => {
  const stompClient = useRef(null);
  const [isConnected, setIsConnected] = useState(false);
  const [alerts, setAlerts] = useState([]);
  const [alertHistory, setAlertHistory] = useState(() => {
    // 로컬 스토리지에서 알림 이력 로드
    try {
      const saved = localStorage.getItem(`alertHistory_${companyName}`);
      return saved ? JSON.parse(saved) : [];
    } catch {
      return [];
    }
  });

  useEffect(() => {
    if (!companyName || !userId || !tokenManager.getAccessToken()) return;

    let cancelled = false;

    const initWebSocket = async () => {
      const libs = await loadWebSocketLibraries();
      if (!libs || cancelled) {
        if (!libs) console.error('웹소켓 라이브러리 로드 실패');
        return;
      }

      try {
        const client = new libs.Stomp();
        stompClient.current = client;
        client.webSocketFactory = () => new libs.SockJS(getWebSocketUrl());

        // 연결(재연결 포함) 직전마다 최신 토큰을 헤더에 넣는다
        client.beforeConnect = () => {
          client.connectHeaders = {
            Authorization: `Bearer ${tokenManager.getAccessToken()}`
          };
        };

        // 연결 콜백 설정
        client.onConnect = () => {
          setIsConnected(true);

          // 회사별 알림 구독
          const alertTopic = `/topic/alerts/${companyName}`;
          client.subscribe(alertTopic, (message) => {
            const alert = JSON.parse(message.body);

            const alertWithId = {
              ...alert,
              id: Date.now() + Math.random(),
              timestamp: new Date(alert.timestamp)
            };

            setAlerts(prevAlerts => [
              alertWithId,
              ...prevAlerts.slice(0, 4) // 최대 5개만 유지
            ]);

            // 알림 이력에 추가 (최대 100개 유지)
            setAlertHistory(prevHistory => {
              const newHistory = [alertWithId, ...prevHistory.slice(0, 99)];
              // 로컬 스토리지에 저장
              try {
                localStorage.setItem(`alertHistory_${companyName}`, JSON.stringify(newHistory));
              } catch (error) {
                console.error('알림 이력 저장 실패:', error);
              }
              return newHistory;
            });

            // 전역 알림 이벤트 발생 (Dashboard에서 사용)
            window.dispatchEvent(new CustomEvent('globalAlert', {
              detail: alertWithId
            }));

            // 5초 후 알림 자동 제거
            setTimeout(() => {
              setAlerts(prevAlerts =>
                prevAlerts.filter(a => a.id !== alertWithId.id)
              );
            }, 5000);
          });
        };

        // 에러 콜백 설정
        client.onStompError = (frame) => {
          console.error('STOMP 에러:', frame.headers?.message || frame);
          setIsConnected(false);
        };

        client.onWebSocketError = (error) => {
          console.error('WebSocket 연결 실패:', error);
          setIsConnected(false);
        };

        client.onWebSocketClose = () => {
          setIsConnected(false);
        };

        // 연결 시작
        client.activate();
      } catch (error) {
        console.error('웹소켓 초기화 오류:', error);
        setIsConnected(false);
      }
    };

    initWebSocket();

    // 정리 함수
    return () => {
      cancelled = true;
      if (stompClient.current) {
        stompClient.current.deactivate();
        stompClient.current = null;
        setIsConnected(false);
      }
    };
  }, [companyName, userId]);

  const removeAlert = (alertId) => {
    setAlerts(prevAlerts => prevAlerts.filter(alert => alert.id !== alertId));
  };

  const clearAlertHistory = async () => {
    try {
      // 백엔드에서 모든 알림 삭제
      await apiService.alerts.deleteAllAlerts(companyName);
    } catch (error) {
      console.error('전체 알림 삭제 실패:', error);
    }
    // 백엔드 삭제가 실패해도 화면과 localStorage는 비운다
    setAlertHistory([]);
    localStorage.removeItem(`alertHistory_${companyName}`);
  };

  const removeIndividualAlert = async (alertId) => {
    try {
      // 백엔드에서 개별 알림 삭제
      await apiService.alerts.deleteAlert(alertId, companyName);
    } catch (error) {
      console.error('개별 알림 삭제 실패:', error);
    }
    // 백엔드 삭제가 실패해도 화면에서는 지운다 (UX)
    setAlertHistory(prevHistory => {
      const updatedHistory = prevHistory.filter(alert => (alert.id || alert.alertId) !== alertId);
      localStorage.setItem(`alertHistory_${companyName}`, JSON.stringify(updatedHistory));
      return updatedHistory;
    });
  };

  return {
    isConnected,
    alerts,
    alertHistory,
    removeAlert,
    clearAlertHistory,
    removeIndividualAlert
  };
};

export default useWebSocket;
