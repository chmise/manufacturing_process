const API_BASE_URL = 'http://localhost:8080/api';

class DashboardService {
  constructor() {
    this.subscribers = [];
    this.pollingInterval = null;
    this.isPolling = false;
  }

  subscribe(callback) {
    this.subscribers.push(callback);
    return () => {
      this.subscribers = this.subscribers.filter(sub => sub !== callback);
    };
  }

  notify(type, data) {
    this.subscribers.forEach(callback => callback(type, data));
  }

  startPolling(interval = 3000) {
    if (this.isPolling) return;
    
    this.isPolling = true;
    this.fetchData();
    
    this.pollingInterval = setInterval(() => {
      this.fetchData();
    }, interval);
  }

  stopPolling() {
    if (this.pollingInterval) {
      clearInterval(this.pollingInterval);
      this.pollingInterval = null;
      this.isPolling = false;
    }
  }

  async fetchData() {
    try {
      const dashboardData = await this.getFactorySummary();
      this.notify('dashboard', dashboardData);
      
      // stations 배열은 백엔드에서 제공하지 않음
      const stationsData = [];
      this.notify('stations', stationsData);
      
    } catch (error) {
      console.error('Backend connection failed:', error);
      this.notify('error', error);
    }
  }

  async getLatestKPIs() {
    const response = await fetch(`${API_BASE_URL}/kpi/realtime`);
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    return await response.json();
  }
  
  
  async getFactorySummary() {
    const response = await fetch(`${API_BASE_URL}/dashboard`);
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    return await response.json();
  }

}

const dashboardService = new DashboardService();
export default dashboardService;