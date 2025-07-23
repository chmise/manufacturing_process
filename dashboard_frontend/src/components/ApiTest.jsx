
import React, { useState } from 'react';
import apiService from '../service/apiService';

const ApiTest = () => {
  const [results, setResults] = useState({});
  const [loading, setLoading] = useState({});

  const testApi = async (endpoint, testName) => {
    setLoading(prev => ({ ...prev, [testName]: true }));
    try {
      let result;
      switch (endpoint) {
        case 'dashboard':
          result = await apiService.getDashboardData();
          break;
        case 'kpi':
          result = await apiService.getRealTimeKPI();
          break;
        case 'environment':
          result = await apiService.getCurrentEnvironment();
          break;
        case 'conveyor':
          result = await apiService.getConveyorStatus();
          break;
        case 'production':
          result = await apiService.getProductionStatus();
          break;
        default:
          throw new Error('Unknown endpoint');
      }
      
      setResults(prev => ({
        ...prev,
        [testName]: { success: true, data: result, timestamp: new Date() }
      }));
    } catch (error) {
      setResults(prev => ({
        ...prev,
        [testName]: { success: false, error: error.message, timestamp: new Date() }
      }));
    } finally {
      setLoading(prev => ({ ...prev, [testName]: false }));
    }
  };

  const testKpiPost = async () => {
    setLoading(prev => ({ ...prev, 'kpi-post': true }));
    try {
      const testData = {
        planned_time: 480,
        downtime: 30,
        target_cycle_time: 2.5,
        good_count: 180,
        total_count: 200,
        first_time_pass_count: 175,
        on_time_delivery_count: 190
      };
      
      const result = await apiService.processKPIData(testData);
      setResults(prev => ({
        ...prev,
        'kpi-post': { success: true, data: result, timestamp: new Date() }
      }));
    } catch (error) {
      setResults(prev => ({
        ...prev,
        'kpi-post': { success: false, error: error.message, timestamp: new Date() }
      }));
    } finally {
      setLoading(prev => ({ ...prev, 'kpi-post': false }));
    }
  };

  const testEnvironmentPost = async () => {
    setLoading(prev => ({ ...prev, 'env-post': true }));
    try {
      const testData = {
        temperature: 23.5,
        humidity: 45.2,
        air_quality: 85
      };
      
      const result = await apiService.inputEnvironmentData(testData);
      setResults(prev => ({
        ...prev,
        'env-post': { success: true, data: result, timestamp: new Date() }
      }));
    } catch (error) {
      setResults(prev => ({
        ...prev,
        'env-post': { success: false, error: error.message, timestamp: new Date() }
      }));
    } finally {
      setLoading(prev => ({ ...prev, 'env-post': false }));
    }
  };

  const renderResult = (testName) => {
    const result = results[testName];
    if (!result) return null;

    return (
      <div className={`mt-2 p-2 border rounded ${result.success ? 'border-success bg-light' : 'border-danger bg-light'}`}>
        <div className="d-flex justify-content-between">
          <strong>{result.success ? '✅ 성공' : '❌ 실패'}</strong>
          <small>{result.timestamp.toLocaleTimeString()}</small>
        </div>
        {result.success ? (
          <pre className="mt-1 mb-0 small">{JSON.stringify(result.data, null, 2)}</pre>
        ) : (
          <div className="text-danger mt-1">{result.error}</div>
        )}
      </div>
    );
  };

  return (
    <div className="container mt-4">
      <div className="card">
        <div className="card-header">
          <h3>API 연결 테스트</h3>
          <small className="text-muted">백엔드 API 엔드포인트 연결 상태를 확인합니다.</small>
        </div>
        <div className="card-body">
          <div className="row">
            {/* GET 요청 테스트 */}
            <div className="col-md-6">
              <h5>GET 요청 테스트</h5>
              
              <div className="mb-3">
                <button 
                  className="btn btn-primary"
                  onClick={() => testApi('dashboard', 'dashboard')}
                  disabled={loading.dashboard}
                >
                  {loading.dashboard && <span className="spinner-border spinner-border-sm me-2"></span>}
                  GET /api/dashboard
                </button>
                {renderResult('dashboard')}
              </div>

              <div className="mb-3">
                <button 
                  className="btn btn-primary"
                  onClick={() => testApi('kpi', 'kpi')}
                  disabled={loading.kpi}
                >
                  {loading.kpi && <span className="spinner-border spinner-border-sm me-2"></span>}
                  GET /api/kpi/realtime
                </button>
                {renderResult('kpi')}
              </div>

              <div className="mb-3">
                <button 
                  className="btn btn-primary"
                  onClick={() => testApi('environment', 'environment')}
                  disabled={loading.environment}
                >
                  {loading.environment && <span className="spinner-border spinner-border-sm me-2"></span>}
                  GET /api/environment/current
                </button>
                {renderResult('environment')}
              </div>

              <div className="mb-3">
                <button 
                  className="btn btn-primary"
                  onClick={() => testApi('conveyor', 'conveyor')}
                  disabled={loading.conveyor}
                >
                  {loading.conveyor && <span className="spinner-border spinner-border-sm me-2"></span>}
                  GET /api/conveyor/status
                </button>
                {renderResult('conveyor')}
              </div>

              <div className="mb-3">
                <button 
                  className="btn btn-primary"
                  onClick={() => testApi('production', 'production')}
                  disabled={loading.production}
                >
                  {loading.production && <span className="spinner-border spinner-border-sm me-2"></span>}
                  GET /api/production/status
                </button>
                {renderResult('production')}
              </div>
            </div>

            {/* POST 요청 테스트 */}
            <div className="col-md-6">
              <h5>POST 요청 테스트</h5>
              
              <div className="mb-3">
                <button 
                  className="btn btn-success"
                  onClick={testKpiPost}
                  disabled={loading['kpi-post']}
                >
                  {loading['kpi-post'] && <span className="spinner-border spinner-border-sm me-2"></span>}
                  POST /api/kpi/process
                </button>
                {renderResult('kpi-post')}
              </div>

              <div className="mb-3">
                <button 
                  className="btn btn-success"
                  onClick={testEnvironmentPost}
                  disabled={loading['env-post']}
                >
                  {loading['env-post'] && <span className="spinner-border spinner-border-sm me-2"></span>}
                  POST /api/environment/input
                </button>
                {renderResult('env-post')}
              </div>
            </div>
          </div>

          <div className="mt-4">
            <button 
              className="btn btn-warning"
              onClick={() => setResults({})}
            >
              결과 초기화
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};


export default ApiTest;
