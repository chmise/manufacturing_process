// src/utils/UnityManager.js
class UnityManager {
  constructor() {
    this.unityInstance = null;
    this.isLoading = false;
    this.isLoaded = false;
    this.loadPromise = null;
    this.canvas = null;
    this.subscribers = new Set();
  }

  // 상태 변경 구독
  subscribe(callback) {
    this.subscribers.add(callback);
    return () => this.subscribers.delete(callback);
  }

  // 구독자들에게 상태 알림
  notify(state) {
    this.subscribers.forEach(callback => callback(state));
  }

  // Unity 인스턴스 로드 (한 번만 실행)
  async loadUnity(canvasId = 'unity-canvas') {
    // 이미 로드된 경우
    if (this.isLoaded && this.unityInstance) {
      console.log('Unity 이미 로드됨 - 재사용');
      this.attachToCanvas(canvasId);
      this.notify({ type: 'loaded', instance: this.unityInstance });
      return this.unityInstance;
    }

    // 이미 로딩 중인 경우
    if (this.isLoading && this.loadPromise) {
      console.log('Unity 로딩 중 - 기존 Promise 반환');
      return this.loadPromise;
    }

    // 새로 로드 시작
    console.log('Unity 새로 로드 시작');
    this.isLoading = true;
    
    this.loadPromise = this.doLoadUnity(canvasId);
    
    try {
      const instance = await this.loadPromise;
      this.unityInstance = instance;
      this.isLoaded = true;
      this.isLoading = false;
      
      console.log('Unity 로드 완료');
      this.notify({ type: 'loaded', instance });
      return instance;
    } catch (error) {
      this.isLoading = false;
      this.loadPromise = null;
      console.error('Unity 로드 실패:', error);
      this.notify({ type: 'error', error });
      throw error;
    }
  }

  // 실제 Unity 로드 로직
  async doLoadUnity(canvasId) {
    return new Promise((resolve, reject) => {
      // Unity 설정
      const buildUrl = '/unity3d';
      const config = {
        dataUrl: `${buildUrl}/factoryTwin.data`,
        frameworkUrl: `${buildUrl}/factoryTwin.framework.js`,
        codeUrl: `${buildUrl}/factoryTwin.wasm`
      };

      const loadUnityInstance = () => {
        const canvas = document.getElementById(canvasId);
        if (!canvas) {
          reject(new Error(`Unity 캔버스를 찾을 수 없습니다: ${canvasId}`));
          return;
        }

        this.canvas = canvas;

        if (typeof window.createUnityInstance !== 'undefined') {
          console.log('createUnityInstance 사용');
          
          window.createUnityInstance(canvas, config, (progress) => {
            const progressPercent = progress * 100;
            this.notify({ type: 'progress', progress: progressPercent });
          }).then((unityInstance) => {
            console.log('Unity 인스턴스 생성 완료');
            this.setupUnityReactCommunication(unityInstance);
            resolve(unityInstance);
          }).catch((error) => {
            console.error('Unity 인스턴스 생성 실패:', error);
            reject(error);
          });
        } else {
          setTimeout(loadUnityInstance, 1000);
        }
      };

      // Framework 스크립트 로드
      const loadFramework = () => {
        if (window.createUnityInstance) {
          loadUnityInstance();
          return;
        }

        const script = document.createElement('script');
        script.src = `${buildUrl}/factoryTwin.framework.js`;
        script.async = true;
        
        script.onload = () => {
          console.log('Unity Framework 로드 완료');
          setTimeout(loadUnityInstance, 500);
        };
        
        script.onerror = (error) => {
          console.error('Framework 로드 실패:', error);
          reject(new Error('Unity Framework 로드 실패'));
        };

        document.head.appendChild(script);
      };

      // Loader 스크립트 로드 시도
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
    });
  }

  // 캔버스에 Unity 인스턴스 연결
  attachToCanvas(canvasId) {
    const newCanvas = document.getElementById(canvasId);
    if (!newCanvas || !this.unityInstance) return;

    // 캔버스가 변경된 경우에만 재연결
    if (this.canvas !== newCanvas) {
      console.log('새 캔버스에 Unity 연결');
      this.canvas = newCanvas;
      
      // Unity 캔버스 교체 로직 (Unity API에 따라 다를 수 있음)
      // 대부분의 경우 Unity는 동일한 캔버스 ID를 사용해야 함
    }
  }

  // Unity-React 통신 설정
  setupUnityReactCommunication(unityInstance) {
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

    // Unity에서 React로 데이터 수신
    window.ReceiveFromUnity = (data) => {
      try {
        const parsedData = typeof data === 'string' ? JSON.parse(data) : data;
        console.log('Unity에서 받은 데이터:', parsedData);
        this.notify({ type: 'message', data: parsedData });
      } catch (error) {
        console.error('Unity 데이터 파싱 오류:', error);
      }
    };
  }

  // Unity 인스턴스 정리 (앱 종료 시에만)
  cleanup() {
    if (this.unityInstance) {
      console.log('Unity 매니저 정리');
      try {
        if (typeof this.unityInstance.Quit === 'function') {
          this.unityInstance.Quit();
        }
      } catch (e) {
        console.log('Unity 정리 중 오류 (무시됨):', e.message);
      }
      
      this.unityInstance = null;
      this.isLoaded = false;
      this.isLoading = false;
      this.loadPromise = null;
      this.canvas = null;
    }

    // 스크립트 제거
    try {
      const scripts = document.querySelectorAll('script[src*="factoryTwin"]');
      scripts.forEach(script => {
        if (script.parentNode) {
          script.parentNode.removeChild(script);
        }
      });
    } catch (e) {
      console.log('스크립트 정리 중 오류 (무시됨):', e.message);
    }
  }

  // Unity 상태 확인
  getState() {
    return {
      isLoaded: this.isLoaded,
      isLoading: this.isLoading,
      hasInstance: !!this.unityInstance
    };
  }
}

// 싱글톤 인스턴스 생성
const unityManager = new UnityManager();

export default unityManager;