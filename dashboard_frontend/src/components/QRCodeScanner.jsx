import React, { useState, useRef, useEffect } from 'react';
import QrScanner from 'qr-scanner';
import { useNavigate } from 'react-router-dom';
import apiService from '../service/apiService';

const QRCodeScanner = () => {
  const videoRef = useRef(null);
  const [qrScanner, setQrScanner] = useState(null);
  const [scanning, setScanning] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');
  const [cameraDevices, setCameraDevices] = useState([]);
  const [selectedCamera, setSelectedCamera] = useState('');
  const [processing, setProcessing] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    // 컴포넌트 마운트시 카메라 권한 요청 및 장치 목록 조회
    initializeCamera();
    
    return () => {
      // 컴포넌트 언마운트시 스캐너 정리
      if (qrScanner) {
        qrScanner.destroy();
      }
    };
  }, []);

  // 카메라 초기화
  const initializeCamera = async () => {
    try {
      // QR 스캐너 라이브러리가 지원되는지 확인
      const hasCamera = await QrScanner.hasCamera();
      if (!hasCamera) {
        setError('카메라를 사용할 수 없습니다.');
        return;
      }

      // 카메라 장치 목록 조회
      const cameras = await QrScanner.listCameras(true);
      setCameraDevices(cameras);
      
      if (cameras.length > 0) {
        // 후면 카메라 우선 선택 (없으면 첫 번째 카메라)
        const backCamera = cameras.find(camera => 
          camera.label.toLowerCase().includes('back') || 
          camera.label.toLowerCase().includes('rear')
        );
        const preferredCamera = backCamera || cameras[0];
        setSelectedCamera(preferredCamera.id);
      }
    } catch (err) {
      console.error('카메라 초기화 오류:', err);
      setError('카메라 초기화에 실패했습니다: ' + err.message);
    }
  };

  // QR 스캔 시작
  const startScanning = async () => {
    if (!videoRef.current) return;

    try {
      setScanning(true);
      setError('');
      setResult(null);

      const scanner = new QrScanner(
        videoRef.current,
        handleScanResult,
        {
          preferredCamera: selectedCamera || 'environment',
          highlightScanRegion: true,
          highlightCodeOutline: true,
          maxScansPerSecond: 5,
        }
      );

      setQrScanner(scanner);
      await scanner.start();
    } catch (err) {
      console.error('QR 스캔 시작 오류:', err);
      setError('QR 스캔을 시작할 수 없습니다: ' + err.message);
      setScanning(false);
    }
  };

  // QR 스캔 중지
  const stopScanning = () => {
    if (qrScanner) {
      qrScanner.stop();
      qrScanner.destroy();
      setQrScanner(null);
    }
    setScanning(false);
  };

  // QR 코드 스캔 결과 처리
  const handleScanResult = async (result) => {
    if (processing) return; // 중복 처리 방지
    
    setProcessing(true);
    setResult(result);
    
    try {
      // QR 코드 내용 분석
      const qrData = result.data;
      
      // URL인지 확인
      if (qrData.startsWith('http://') || qrData.startsWith('https://')) {
        const url = new URL(qrData);
        
        // 초대 링크인지 확인
        if (url.searchParams.has('invitation')) {
          const invitationToken = url.searchParams.get('invitation');
          await handleInvitationQR(invitationToken);
        } else if (url.pathname.includes('/register') && url.searchParams.has('invitation')) {
          // 다른 형태의 초대 링크
          const invitationToken = url.searchParams.get('invitation');
          await handleInvitationQR(invitationToken);
        } else {
          // 일반 URL - 새 탭에서 열기
          window.open(qrData, '_blank');
          setResult({
            ...result,
            type: 'url',
            message: 'URL이 새 탭에서 열렸습니다.'
          });
        }
      } else {
        // 단순 텍스트 또는 다른 형태의 데이터
        try {
          const jsonData = JSON.parse(qrData);
          if (jsonData.type === 'smart-invitation' && jsonData.token) {
            // NFC 형태의 초대 데이터
            await handleInvitationQR(jsonData.token);
          } else {
            setResult({
              ...result,
              type: 'data',
              message: 'QR 코드 데이터를 읽었습니다.'
            });
          }
        } catch {
          // JSON이 아닌 일반 텍스트
          setResult({
            ...result,
            type: 'text',
            message: 'QR 코드 내용을 읽었습니다.'
          });
        }
      }
    } catch (err) {
      console.error('QR 결과 처리 오류:', err);
      setError('QR 코드 처리 중 오류가 발생했습니다: ' + err.message);
    } finally {
      setProcessing(false);
      stopScanning();
    }
  };

  // 초대 QR 코드 처리
  const handleInvitationQR = async (invitationToken) => {
    try {
      const validation = await apiService.auth.validateInvitation(invitationToken);
      
      if (validation.valid) {
        setResult({
          data: invitationToken,
          type: 'invitation',
          message: `${validation.companyName}에서 초대되었습니다!`,
          companyName: validation.companyName,
          invitationType: validation.type
        });
        
        // 3초 후 회원가입 페이지로 이동
        setTimeout(() => {
          navigate(`/register?invitation=${invitationToken}`);
        }, 3000);
      } else {
        setError('유효하지 않거나 만료된 초대입니다.');
      }
    } catch (err) {
      setError('초대 검증에 실패했습니다: ' + err.message);
    }
  };

  // 카메라 변경
  const handleCameraChange = (deviceId) => {
    setSelectedCamera(deviceId);
    if (scanning && qrScanner) {
      qrScanner.setCamera(deviceId);
    }
  };

  // 파일에서 QR 코드 읽기
  const handleFileUpload = async (event) => {
    const file = event.target.files[0];
    if (!file) return;

    try {
      setProcessing(true);
      const result = await QrScanner.scanImage(file);
      await handleScanResult({ data: result });
    } catch (err) {
      setError('이미지에서 QR 코드를 찾을 수 없습니다.');
    } finally {
      setProcessing(false);
    }
  };

  return (
    <div className="container py-4">
      <div className="row justify-content-center">
        <div className="col-lg-8">
          <div className="card shadow-lg border-0">
            <div className="card-header bg-primary text-white text-center py-4">
              <h3 className="mb-2">
                <i className="ti ti-qrcode me-2"></i>
                QR 코드 스캐너
              </h3>
              <p className="mb-0 opacity-75">
                초대 QR 코드를 스캔하거나 업로드하여 간편하게 가입하세요
              </p>
            </div>

            <div className="card-body p-4">
              {/* 카메라 선택 */}
              {cameraDevices.length > 1 && (
                <div className="mb-4">
                  <label className="form-label fw-semibold">
                    <i className="ti ti-camera me-2"></i>
                    카메라 선택
                  </label>
                  <select 
                    className="form-select"
                    value={selectedCamera}
                    onChange={(e) => handleCameraChange(e.target.value)}
                    disabled={scanning}
                  >
                    {cameraDevices.map(camera => (
                      <option key={camera.id} value={camera.id}>
                        {camera.label || `Camera ${camera.id}`}
                      </option>
                    ))}
                  </select>
                </div>
              )}

              {/* 비디오 영역 */}
              <div className="text-center mb-4">
                <div 
                  className="position-relative d-inline-block"
                  style={{ 
                    borderRadius: '12px',
                    overflow: 'hidden',
                    border: scanning ? '3px solid #007bff' : '3px dashed #dee2e6'
                  }}
                >
                  <video 
                    ref={videoRef}
                    style={{
                      width: '100%',
                      maxWidth: '400px',
                      height: '300px',
                      objectFit: 'cover',
                      display: scanning ? 'block' : 'none'
                    }}
                  />
                  
                  {!scanning && (
                    <div 
                      className="d-flex align-items-center justify-content-center text-muted"
                      style={{ width: '400px', height: '300px', maxWidth: '100%' }}
                    >
                      <div className="text-center">
                        <i className="ti ti-camera-off fs-1 mb-3"></i>
                        <p className="mb-0">카메라 미리보기</p>
                      </div>
                    </div>
                  )}

                  {processing && (
                    <div 
                      className="position-absolute top-0 start-0 w-100 h-100 d-flex align-items-center justify-content-center"
                      style={{ backgroundColor: 'rgba(0,0,0,0.7)' }}
                    >
                      <div className="text-center text-white">
                        <div className="spinner-border mb-2" role="status"></div>
                        <p className="mb-0">처리 중...</p>
                      </div>
                    </div>
                  )}
                </div>
              </div>

              {/* 컨트롤 버튼 */}
              <div className="d-flex justify-content-center gap-3 mb-4">
                {!scanning ? (
                  <button 
                    className="btn btn-primary btn-lg"
                    onClick={startScanning}
                    disabled={processing}
                  >
                    <i className="ti ti-scan me-2"></i>
                    스캔 시작
                  </button>
                ) : (
                  <button 
                    className="btn btn-danger btn-lg"
                    onClick={stopScanning}
                  >
                    <i className="ti ti-square me-2"></i>
                    스캔 중지
                  </button>
                )}

                <label className="btn btn-outline-primary btn-lg">
                  <i className="ti ti-upload me-2"></i>
                  이미지 업로드
                  <input 
                    type="file" 
                    accept="image/*" 
                    onChange={handleFileUpload}
                    className="d-none"
                    disabled={processing}
                  />
                </label>
              </div>

              {/* 에러 메시지 */}
              {error && (
                <div className="alert alert-danger">
                  <i className="ti ti-alert-circle me-2"></i>
                  {error}
                  <button 
                    type="button" 
                    className="btn-close float-end" 
                    onClick={() => setError('')}
                  ></button>
                </div>
              )}

              {/* 스캔 결과 */}
              {result && (
                <div className={`alert ${result.type === 'invitation' ? 'alert-success' : 'alert-info'}`}>
                  <div className="d-flex align-items-start">
                    <i className={`ti ${result.type === 'invitation' ? 'ti-check-circle' : 'ti-info-circle'} me-2 fs-5`}></i>
                    <div className="flex-grow-1">
                      <h6 className="alert-heading mb-2">
                        {result.type === 'invitation' ? '초대 코드 인식!' : 'QR 코드 스캔 완료'}
                      </h6>
                      <p className="mb-2">{result.message}</p>
                      
                      {result.type === 'invitation' && (
                        <div className="mb-3">
                          <div className="small">
                            <strong>회사:</strong> {result.companyName}<br/>
                            <strong>초대 유형:</strong> {result.invitationType}
                          </div>
                          <div className="mt-2">
                            <div className="spinner-border spinner-border-sm me-2"></div>
                            <span className="small text-muted">3초 후 회원가입 페이지로 이동합니다...</span>
                          </div>
                        </div>
                      )}
                      
                      <details className="small">
                        <summary className="text-muted">스캔 데이터 보기</summary>
                        <code className="d-block mt-2 p-2 bg-light rounded small">
                          {result.data}
                        </code>
                      </details>
                    </div>
                  </div>
                </div>
              )}

              {/* 사용 안내 */}
              <div className="card bg-light border-0">
                <div className="card-body py-3">
                  <h6 className="card-title mb-2">
                    <i className="ti ti-info-circle me-2 text-info"></i>
                    사용 안내
                  </h6>
                  <ul className="list-unstyled mb-0 small text-muted">
                    <li className="mb-1">• 회사에서 제공한 초대 QR 코드를 스캔하세요</li>
                    <li className="mb-1">• 카메라를 QR 코드에 맞춰 자동으로 인식됩니다</li>
                    <li className="mb-1">• 이미지 파일로 된 QR 코드도 업로드할 수 있습니다</li>
                    <li>• 충분한 조명과 안정된 손으로 스캔하세요</li>
                  </ul>
                </div>
              </div>
            </div>

            {/* 하단 네비게이션 */}
            <div className="card-footer bg-transparent text-center py-3">
              <div className="d-flex justify-content-center gap-3">
                <button 
                  className="btn btn-outline-secondary"
                  onClick={() => navigate('/company-register')}
                >
                  <i className="ti ti-building me-1"></i>
                  회사 등록
                </button>
                <button 
                  className="btn btn-outline-primary"
                  onClick={() => navigate('/register')}
                >
                  <i className="ti ti-user-plus me-1"></i>
                  일반 회원가입
                </button>
                <button 
                  className="btn btn-outline-success"
                  onClick={() => navigate('/login')}
                >
                  <i className="ti ti-login me-1"></i>
                  로그인
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default QRCodeScanner;