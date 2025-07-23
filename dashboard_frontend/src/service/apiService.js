const API_BASE_URL = 'http://localhost:8080/api';

export const apiService = {
  // 대시보드 메인 데이터
  getDashboardData: async () => {
    const response = await fetch(`${API_BASE_URL}/dashboard`);
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    return response.json();
  },

  // 실시간 KPI 조회
  getRealTimeKPI: async () => {
    const response = await fetch(`${API_BASE_URL}/kpi/realtime`);
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    return response.json();
  },

  // KPI 데이터 처리 (테스트용)
  processKPIData: async (kpiData) => {
    const response = await fetch(`${API_BASE_URL}/kpi/process`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(kpiData)
    });
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    return response.json();
  },

  // 현재 환경 데이터 조회
  getCurrentEnvironment: async () => {
    const response = await fetch(`${API_BASE_URL}/environment/current`);
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    return response.json();
  },

  // 환경 데이터 입력 (테스트용)
  inputEnvironmentData: async (envData) => {
    const response = await fetch(`${API_BASE_URL}/environment/input`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(envData)
    });
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    return response.json();
  },

  // 컨베이어 상태 조회
  getConveyorStatus: async () => {
    const response = await fetch(`${API_BASE_URL}/conveyor/status`);
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    return response.json();
  },

  // 생산 현황 조회
  getProductionStatus: async () => {
    const response = await fetch(`${API_BASE_URL}/production/status`);
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    return response.json();
  },

  // 범용 GET 요청
  get: async (endpoint) => {
    const response = await fetch(`${API_BASE_URL}${endpoint}`);
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    return response.json();
  },

  // 범용 POST 요청
  post: async (endpoint, data) => {
    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(data)
    });
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    return response.json();
  }
};

export default apiService;