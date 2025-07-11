// dashboard_frontend/src/App.jsx
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { useEffect } from 'react'
import Layout from './components/Layout'
import Dashboard from './components/Dashboard'
import Factory3D from './components/Factory3D'
import Inventory from './components/Inventory'
import unityManager from './utils/UnityManager'

function App() {
  useEffect(() => {
    console.log('App 컴포넌트 마운트 - Unity 매니저 초기화');

    // 페이지 언로드 시 Unity 정리 (브라우저 탭 닫기/새로고침)
    const handleBeforeUnload = () => {
      console.log('페이지 언로드 - Unity 정리');
      unityManager.cleanup();
    };

    // 브라우저 뒤로가기/앞으로가기 감지
    const handlePopState = () => {
      console.log('브라우저 네비게이션 감지');
      // Unity는 정리하지 않고 유지 - 페이지 이동 간 재사용
    };

    // 페이지 visibility 변경 감지 (탭 전환)
    const handleVisibilityChange = () => {
      if (document.hidden) {
        console.log('페이지가 숨겨짐 (탭 전환)');
        // Unity 일시정지 등의 최적화 가능
      } else {
        console.log('페이지가 다시 보임');
        // Unity 재개 등의 처리 가능
      }
    };

    // 이벤트 리스너 등록
    window.addEventListener('beforeunload', handleBeforeUnload);
    window.addEventListener('popstate', handlePopState);
    document.addEventListener('visibilitychange', handleVisibilityChange);

    // Unity 매니저 상태 확인
    const unityState = unityManager.getState();
    console.log('Unity 초기 상태:', unityState);

    return () => {
      console.log('App 컴포넌트 언마운트');
      
      // 이벤트 리스너 제거
      window.removeEventListener('beforeunload', handleBeforeUnload);
      window.removeEventListener('popstate', handlePopState);
      document.removeEventListener('visibilitychange', handleVisibilityChange);
      
      // 개발 환경에서는 핫 리로드를 위해 정리
      if (process.env.NODE_ENV === 'development') {
        console.log('개발 환경 - Unity 정리');
        unityManager.cleanup();
      }
      // 프로덕션에서는 Unity 유지 (페이지 이동 간 재사용)
    };
  }, []);

  // Unity 관련 전역 에러 핸들링
  useEffect(() => {
    const handleUnhandledRejection = (event) => {
      if (event.reason && event.reason.toString().includes('Unity')) {
        console.error('Unity 관련 에러:', event.reason);
        // Unity 에러 시 선택적 정리 및 재시도 로직
      }
    };

    const handleError = (event) => {
      if (event.error && event.error.toString().includes('Unity')) {
        console.error('Unity JavaScript 에러:', event.error);
      }
    };

    window.addEventListener('unhandledrejection', handleUnhandledRejection);
    window.addEventListener('error', handleError);

    return () => {
      window.removeEventListener('unhandledrejection', handleUnhandledRejection);
      window.removeEventListener('error', handleError);
    };
  }, []);

  return (
    <BrowserRouter>
      <Layout>
        <Routes>
          <Route path="/" element={<Dashboard />} />
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/factory3d" element={<Factory3D />} />
          <Route path="/inventory" element={<Inventory />} />
        </Routes>
      </Layout>
    </BrowserRouter>
  )
}

export default App