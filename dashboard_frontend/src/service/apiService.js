const API_BASE_URL = 'http://localhost:8080/api';

// 토큰 관리 유틸리티
const tokenManager = {
  getAccessToken: () => localStorage.getItem('accessToken'),
  getRefreshToken: () => localStorage.getItem('refreshToken'),
  setTokens: (accessToken, refreshToken) => {
    localStorage.setItem('accessToken', accessToken);
    localStorage.setItem('refreshToken', refreshToken);
  },
  clearTokens: () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('userData');
    localStorage.removeItem('isLoggedIn');
  },
  isTokenExpired: (token) => {
    if (!token) return true;
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.exp * 1000 < Date.now();
    } catch {
      return true;
    }
  }
};

// 인증이 필요한 HTTP 클라이언트
class AuthenticatedHttpClient {
  constructor() {
    this.isRefreshing = false;
    this.refreshSubscribers = [];
  }

  // 대기 중인 요청들을 새 토큰으로 재시도
  onRefreshed(token) {
    this.refreshSubscribers.map(callback => callback(token));
    this.refreshSubscribers = [];
  }

  // 토큰 새로고침 대기열에 추가
  addRefreshSubscriber(callback) {
    this.refreshSubscribers.push(callback);
  }

  // 토큰 새로고침
  async refreshToken() {
    const refreshToken = tokenManager.getRefreshToken();
    if (!refreshToken) {
      throw new Error('No refresh token available');
    }

    try {
      const response = await fetch(`${API_BASE_URL}/user/refresh-token`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ refreshToken })
      });

      if (!response.ok) {
        throw new Error('Token refresh failed');
      }

      const data = await response.json();
      if (data.success) {
        tokenManager.setTokens(data.accessToken, data.refreshToken);
        return data.accessToken;
      } else {
        throw new Error(data.message || 'Token refresh failed');
      }
    } catch (error) {
      tokenManager.clearTokens();
      // 로그인 페이지로 리다이렉트
      window.location.href = '/login';
      throw error;
    }
  }

  // 인증 헤더 생성
  async getAuthHeaders() {
    let token = tokenManager.getAccessToken();
    
    // 토큰이 만료되었다면 새로고침
    if (tokenManager.isTokenExpired(token)) {
      if (this.isRefreshing) {
        // 이미 새로고침 중이면 대기
        return new Promise((resolve) => {
          this.addRefreshSubscriber((newToken) => {
            resolve({
              'Content-Type': 'application/json',
              'Authorization': `Bearer ${newToken}`
            });
          });
        });
      }

      this.isRefreshing = true;
      try {
        token = await this.refreshToken();
        this.onRefreshed(token);
      } finally {
        this.isRefreshing = false;
      }
    }

    return {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    };
  }

  // 인증된 GET 요청
  async get(endpoint) {
    const headers = await this.getAuthHeaders();
    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
      method: 'GET',
      headers
    });

    if (!response.ok) {
      if (response.status === 401) {
        // 토큰이 여전히 유효하지 않으면 로그아웃
        tokenManager.clearTokens();
        window.location.href = '/login';
      }
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    return response.json();
  }

  // 인증된 POST 요청
  async post(endpoint, data) {
    const headers = await this.getAuthHeaders();
    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
      method: 'POST',
      headers,
      body: JSON.stringify(data)
    });

    if (!response.ok) {
      if (response.status === 401) {
        tokenManager.clearTokens();
        window.location.href = '/login';
      }
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    return response.json();
  }

  // 인증된 PUT 요청
  async put(endpoint, data) {
    const headers = await this.getAuthHeaders();
    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
      method: 'PUT',
      headers,
      body: JSON.stringify(data)
    });

    if (!response.ok) {
      if (response.status === 401) {
        tokenManager.clearTokens();
        window.location.href = '/login';
      }
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    return response.json();
  }

  // 인증된 DELETE 요청
  async delete(endpoint) {
    const headers = await this.getAuthHeaders();
    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
      method: 'DELETE',
      headers
    });

    if (!response.ok) {
      if (response.status === 401) {
        tokenManager.clearTokens();
        window.location.href = '/login';
      }
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    return response.json();
  }
}

// HTTP 클라이언트 인스턴스
const httpClient = new AuthenticatedHttpClient();

// 공개 API (인증 불필요)
const publicAPI = {
  // 로그인 (토큰 없이)
  login: async (credentials) => {
    const response = await fetch(`${API_BASE_URL}/user/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        username: credentials.username,
        password: credentials.password
      })
    });
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    return response.json();
  },

  // 회원가입 (토큰 없이)
  register: async (userData) => {
    const response = await fetch(`${API_BASE_URL}/user/register`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(userData)
    });
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    return response.json();
  }
};

// 메인 API 서비스
export const apiService = {
  // 토큰 관리
  auth: {
    ...publicAPI,
    getCurrentUser: () => httpClient.get('/user/me'),
    logout: () => {
      tokenManager.clearTokens();
      window.location.href = '/login';
    },
    refreshToken: () => httpClient.refreshToken()
  },

  // 대시보드 API
  dashboard: {
    getData: () => httpClient.get('/dashboard'),
    getRealTimeKPI: () => httpClient.get('/kpi/realtime'),
    processKPIData: (kpiData) => httpClient.post('/kpi/process', kpiData)
  },

  // 환경 데이터 API
  environment: {
    getCurrent: () => httpClient.get('/environment/current'),
    inputData: (envData) => httpClient.post('/environment/input', envData)
  },

  // 생산 관련 API
  production: {
    getStatus: () => httpClient.get('/production/status'),
    getConveyorStatus: () => httpClient.get('/conveyor/status')
  },

  // 재고 관리 API
  inventory: {
    getStock: () => httpClient.get('/stock'),
    updateStock: (stockData) => httpClient.put('/stock', stockData)
  },

  // 클릭 이벤트 API
  clickEvent: {
    handleObjectClick: (objectType, objectId) => 
      httpClient.post('/click/object', { objectType, objectId }),
    getRobotStatus: (robotId) => httpClient.get(`/click/robot/${robotId}`),
    getStationInfo: (stationId) => httpClient.get(`/click/station/${stationId}`)
  },

  // 범용 HTTP 메서드 (인증 포함)
  get: (endpoint) => httpClient.get(endpoint),
  post: (endpoint, data) => httpClient.post(endpoint, data),
  put: (endpoint, data) => httpClient.put(endpoint, data),
  delete: (endpoint) => httpClient.delete(endpoint),

  // 사용자 인증 관련 (하위 호환성)
  user: {
    login: publicAPI.login,
    register: publicAPI.register,
    getCurrentUser: () => httpClient.get('/user/me'),
    refreshToken: (refreshTokenRequest) => 
      httpClient.post('/user/refresh-token', refreshTokenRequest)
  }
};

export default apiService;