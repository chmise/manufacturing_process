const API_BASE_URL = 'http://localhost:8080/api';

export const kpiService = {
  // 실시간 KPI 데이터 조회
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
  }
};