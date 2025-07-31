import React, { useState, useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import apiService from "../service/apiService";

const EnterpriseCompanyRegister = () => {
  // 기본 상태
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [isRegistered, setIsRegistered] = useState(false);
  const [currentStep, setCurrentStep] = useState(1);
  
  // 폼 데이터
  const [formData, setFormData] = useState({
    companyName: "",
    businessNumber: "",
    officialEmail: "",
    officialPhone: "",
    website: "",
    address: ""
  });
  
  // 등록 결과
  const [registrationResult, setRegistrationResult] = useState(null);
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  // 초대 토큰이 있으면 자동으로 처리
  useEffect(() => {
    const invitationToken = searchParams.get('invitation');
    if (invitationToken) {
      handleInvitationValidation(invitationToken);
    }
  }, [searchParams]);

  // 초대 링크 검증
  const handleInvitationValidation = async (token) => {
    try {
      setLoading(true);
      const result = await apiService.auth.validateInvitation(token);
      
      if (result.valid) {
        // 초대가 유효하면 해당 회사 정보로 자동 설정
        setFormData(prev => ({
          ...prev,
          companyName: result.companyName || ""
        }));
        setSuccess(`${result.companyName}에서 초대되었습니다!`);
        setCurrentStep(2); // 직원 등록 단계로 이동
      } else {
        setError("유효하지 않거나 만료된 초대 링크입니다.");
      }
    } catch (err) {
      setError("초대 링크 검증에 실패했습니다.");
    } finally {
      setLoading(false);
    }
  };

  // 입력 필드 변경 핸들러
  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    
    // 에러 초기화
    if (error) setError("");
  };

  // 회사명 중복 체크
  const checkCompanyName = async (name) => {
    if (!name.trim()) return true;
    
    try {
      const exists = await apiService.company.checkCompanyName(name.trim());
      if (exists) {
        setError("이미 등록된 회사명입니다.");
        return false;
      }
      return true;
    } catch (err) {
      console.warn('회사명 중복 체크 실패:', err);
      return true; // 오류 시 진행 허용
    }
  };

  // 1단계: 회사 등록
  const handleCompanyRegister = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");
    setSuccess("");

    // 필수 필드 검증
    if (!formData.companyName.trim()) {
      setError("회사명을 입력해주세요.");
      setLoading(false);
      return;
    }

    // 회사명 중복 체크
    const isNameValid = await checkCompanyName(formData.companyName);
    if (!isNameValid) {
      setLoading(false);
      return;
    }

    try {
      // 스마트 회사 등록 (엔터프라이즈 검증 포함)
      const response = await apiService.company.register(formData);

      if (response.success) {
        setSuccess("회사가 성공적으로 등록되었습니다!");
        setRegistrationResult(response);
        setIsRegistered(true);
        setCurrentStep(3); // 결과 표시 단계
      } else {
        setError(response.message || "회사 등록에 실패했습니다.");
      }
    } catch (err) {
      console.error('회사 등록 오류:', err);
      setError("서버 연결에 실패했습니다. 잠시 후 다시 시도해주세요.");
    } finally {
      setLoading(false);
    }
  };

  // 진행률 계산
  const getProgressPercentage = () => {
    switch (currentStep) {
      case 1: return 33;
      case 2: return 66;
      case 3: return 100;
      default: return 0;
    }
  };

  // QR 코드 다운로드
  const downloadQRCode = () => {
    if (registrationResult?.qrCode) {
      const link = document.createElement('a');
      link.href = registrationResult.qrCode;
      link.download = `${registrationResult.companyName}_QR.png`;
      link.click();
    }
  };

  // QR 코드 공유
  const shareQRCode = async () => {
    if (navigator.share && registrationResult?.invitationLink) {
      try {
        await navigator.share({
          title: `${registrationResult.companyName} 직원 초대`,
          text: '우리 회사에 합류하세요!',
          url: registrationResult.invitationLink
        });
      } catch (err) {
        // 공유 실패시 클립보드에 복사
        copyInvitationLink();
      }
    } else {
      copyInvitationLink();
    }
  };

  // 초대 링크 복사
  const copyInvitationLink = () => {
    if (registrationResult?.invitationLink) {
      navigator.clipboard.writeText(registrationResult.invitationLink);
      alert('초대 링크가 클립보드에 복사되었습니다!');
    }
  };

  return (
    <div className="vh-100 d-flex justify-content-center align-items-center" 
         style={{ 
           background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
           position: 'relative',
           overflow: 'hidden'
         }}>
      
      {/* 배경 애니메이션 */}
      <div className="floating-shapes">
        <div className="shape shape-1"></div>
        <div className="shape shape-2"></div>
        <div className="shape shape-3"></div>
      </div>

      <div className="container">
        <div className="row justify-content-center">
          <div className="col-lg-8 col-xl-7">
            <div className="card border-0 shadow-lg" 
                 style={{ 
                   background: 'rgba(255, 255, 255, 0.95)',
                   backdropFilter: 'blur(20px)',
                   borderRadius: '24px'
                 }}>
              
              {/* 진행률 바 */}
              <div className="card-header bg-transparent border-0 p-4">
                <div className="d-flex justify-content-between align-items-center mb-3">
                  <span className="badge bg-primary px-3 py-2">
                    <i className="ti ti-shield-check me-1"></i>
                    엔터프라이즈 등급
                  </span>
                  <span className="text-muted small">
                    단계 {currentStep}/3
                  </span>
                </div>
                <div className="progress" style={{ height: '6px', borderRadius: '3px' }}>
                  <div 
                    className="progress-bar bg-gradient" 
                    style={{ 
                      width: `${getProgressPercentage()}%`,
                      background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
                    }}
                  ></div>
                </div>
              </div>

              <div className="card-body p-5">
                
                {/* 1단계: 회사 정보 입력 */}
                {currentStep === 1 && (
                  <>
                    <div className="text-center mb-4">
                      <div className="mb-3">
                        <i className="ti ti-building-factory fs-1 text-primary"></i>
                      </div>
                      <h2 className="card-title fw-bold fs-2 mb-2">스마트 회사 등록</h2>
                      <p className="text-muted">엔터프라이즈급 보안으로 보호되는 스마트 제조 시스템</p>
                    </div>

                    <form onSubmit={handleCompanyRegister}>
                      <div className="row">
                        <div className="col-md-6 mb-3">
                          <label className="form-label fw-semibold">
                            <i className="ti ti-building me-2 text-primary"></i>
                            회사명 <span className="text-danger">*</span>
                          </label>
                          <input
                            type="text"
                            name="companyName"
                            className="form-control form-control-lg"
                            value={formData.companyName}
                            onChange={handleInputChange}
                            placeholder="회사명을 입력하세요"
                            required
                            style={{ borderColor: error && error.includes('회사명') ? '#dc3545' : '#e5e7eb' }}
                          />
                        </div>
                        
                        <div className="col-md-6 mb-3">
                          <label className="form-label fw-semibold">
                            <i className="ti ti-id me-2 text-primary"></i>
                            사업자등록번호
                          </label>
                          <input
                            type="text"
                            name="businessNumber"
                            className="form-control form-control-lg"
                            value={formData.businessNumber}
                            onChange={handleInputChange}
                            placeholder="000-00-00000"
                            maxLength="12"
                          />
                        </div>
                      </div>

                      <div className="row">
                        <div className="col-md-6 mb-3">
                          <label className="form-label fw-semibold">
                            <i className="ti ti-mail me-2 text-primary"></i>
                            공식 이메일
                          </label>
                          <input
                            type="email"
                            name="officialEmail"
                            className="form-control form-control-lg"
                            value={formData.officialEmail}
                            onChange={handleInputChange}
                            placeholder="contact@company.com"
                          />
                        </div>
                        
                        <div className="col-md-6 mb-3">
                          <label className="form-label fw-semibold">
                            <i className="ti ti-phone me-2 text-primary"></i>
                            대표 전화번호
                          </label>
                          <input
                            type="tel"
                            name="officialPhone"
                            className="form-control form-control-lg"
                            value={formData.officialPhone}
                            onChange={handleInputChange}
                            placeholder="02-0000-0000"
                          />
                        </div>
                      </div>

                      <div className="mb-3">
                        <label className="form-label fw-semibold">
                          <i className="ti ti-world me-2 text-primary"></i>
                          웹사이트
                        </label>
                        <input
                          type="url"
                          name="website"
                          className="form-control form-control-lg"
                          value={formData.website}
                          onChange={handleInputChange}
                          placeholder="https://www.company.com"
                        />
                      </div>

                      <div className="mb-4">
                        <label className="form-label fw-semibold">
                          <i className="ti ti-map-pin me-2 text-primary"></i>
                          회사 주소
                        </label>
                        <textarea
                          name="address"
                          className="form-control"
                          rows="2"
                          value={formData.address}
                          onChange={handleInputChange}
                          placeholder="회사 주소를 입력하세요"
                        />
                      </div>

                      {error && (
                        <div className="alert alert-danger d-flex align-items-center mb-4">
                          <i className="ti ti-alert-circle me-2"></i>
                          {error}
                        </div>
                      )}

                      <button 
                        type="submit" 
                        className="btn btn-lg w-100 text-white fw-semibold mb-4"
                        style={{ 
                          background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                          border: 'none'
                        }}
                        disabled={loading || !formData.companyName.trim()}
                      >
                        {loading ? (
                          <>
                            <span className="spinner-border spinner-border-sm me-2"></span>
                            검증 및 등록 중...
                          </>
                        ) : (
                          <>
                            <i className="ti ti-shield-check me-2"></i>
                            엔터프라이즈 등록
                          </>
                        )}
                      </button>
                    </form>
                  </>
                )}

                {/* 3단계: 등록 성공 */}
                {currentStep === 3 && registrationResult && (
                  <>
                    <div className="text-center mb-4">
                      <div className="mb-3">
                        <i className="ti ti-circle-check fs-1 text-success"></i>
                      </div>
                      <h2 className="card-title fw-bold text-success mb-2">등록 완료!</h2>
                      <p className="text-muted">
                        <strong>{registrationResult.companyName}</strong>이 성공적으로 등록되었습니다
                      </p>
                    </div>

                    {/* 검증 점수 표시 */}
                    {registrationResult.verificationScore && (
                      <div className="alert alert-info mb-4">
                        <div className="d-flex justify-content-between align-items-center">
                          <span>
                            <i className="ti ti-shield-check me-2"></i>
                            기업 검증 점수
                          </span>
                          <span className="fw-bold">
                            {registrationResult.verificationScore.toFixed(1)}%
                          </span>
                        </div>
                      </div>
                    )}

                    {/* 생성된 회사 코드 */}
                    <div className="mb-4">
                      <label className="form-label fw-semibold mb-3">
                        <i className="ti ti-key me-2"></i>
                        스마트 회사 코드
                      </label>
                      <div className="input-group">
                        <input
                          type="text"
                          className="form-control form-control-lg text-center fw-bold"
                          value={registrationResult.companyCode}
                          style={{ 
                            borderColor: '#28a745', 
                            backgroundColor: '#f8fff9', 
                            fontSize: '1.2rem',
                            letterSpacing: '2px'
                          }}
                          readOnly
                        />
                        <button
                          type="button"
                          className="btn btn-success"
                          onClick={() => {
                            navigator.clipboard.writeText(registrationResult.companyCode);
                            alert('회사코드가 클립보드에 복사되었습니다!');
                          }}
                        >
                          <i className="ti ti-copy"></i>
                        </button>
                      </div>
                    </div>

                    {/* QR 코드 */}
                    {registrationResult.qrCode && (
                      <div className="mb-4 text-center">
                        <label className="form-label fw-semibold mb-3">
                          <i className="ti ti-qrcode me-2"></i>
                          QR 코드
                        </label>
                        <div className="d-inline-block p-3 bg-white rounded shadow-sm">
                          <img 
                            src={registrationResult.qrCode} 
                            alt="Company QR Code"
                            style={{ width: '150px', height: '150px' }}
                          />
                        </div>
                        <div className="mt-2">
                          <button 
                            className="btn btn-outline-primary btn-sm me-2"
                            onClick={downloadQRCode}
                          >
                            <i className="ti ti-download me-1"></i>
                            다운로드
                          </button>
                          <button 
                            className="btn btn-outline-success btn-sm"
                            onClick={shareQRCode}
                          >
                            <i className="ti ti-share me-1"></i>
                            공유
                          </button>
                        </div>
                      </div>
                    )}

                    {/* 초대 링크 */}
                    {registrationResult.invitationLink && (
                      <div className="mb-4">
                        <label className="form-label fw-semibold mb-2">
                          <i className="ti ti-link me-2"></i>
                          직원 초대 링크
                        </label>
                        <div className="input-group">
                          <input
                            type="text"
                            className="form-control"
                            value={registrationResult.invitationLink}
                            style={{ fontSize: '0.9rem' }}
                            readOnly
                          />
                          <button
                            type="button"
                            className="btn btn-primary"
                            onClick={copyInvitationLink}
                          >
                            <i className="ti ti-copy me-1"></i>
                            복사
                          </button>
                        </div>
                        <div className="form-text text-success">
                          <i className="ti ti-info-circle me-1"></i>
                          이 링크를 직원들과 공유하여 쉽게 회원가입할 수 있습니다.
                        </div>
                      </div>
                    )}

                    {/* 엔터프라이즈 토큰 */}
                    {registrationResult.enterpriseToken && (
                      <div className="alert alert-warning mb-4">
                        <h6 className="alert-heading">
                          <i className="ti ti-shield-lock me-2"></i>
                          엔터프라이즈 토큰
                        </h6>
                        <p className="mb-2 small">
                          만료 시간: {new Date(registrationResult.tokenExpiresAt).toLocaleString()}
                        </p>
                        <p className="mb-0 small">
                          이 토큰은 고급 보안 기능에 사용됩니다.
                        </p>
                      </div>
                    )}

                    <div className="d-grid gap-2">
                      <button 
                        className="btn btn-success btn-lg"
                        onClick={() => navigate("/register")}
                      >
                        <i className="ti ti-user-plus me-2"></i>
                        직원 회원가입하기
                      </button>
                      <button 
                        className="btn btn-outline-primary"
                        onClick={() => navigate("/login")}
                      >
                        <i className="ti ti-login me-2"></i>
                        로그인하기
                      </button>
                    </div>
                  </>
                )}

                {/* 하단 링크 */}
                {currentStep === 1 && (
                  <div className="text-center mt-4">
                    <span className="text-muted">이미 회사가 등록되어 있나요? </span>
                    <button
                      className="btn btn-link text-decoration-none fw-semibold p-0"
                      style={{ color: '#667eea' }}
                      onClick={() => navigate("/login")}
                    >
                      로그인하기
                    </button>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>

      <style jsx="true">{`
        @keyframes float {
          0%, 100% { 
            transform: translateY(0px) rotate(0deg); 
            opacity: 0.7; 
          }
          50% { 
            transform: translateY(-20px) rotate(180deg); 
            opacity: 0.3; 
          }
        }
        
        .floating-shapes .shape {
          position: absolute;
          border-radius: 50%;
          background: rgba(255, 255, 255, 0.1);
          animation: float 6s ease-in-out infinite;
        }
        
        .shape-1 {
          width: 300px;
          height: 300px;
          top: -150px;
          right: -150px;
        }
        
        .shape-2 {
          width: 200px;
          height: 200px;
          bottom: -100px;
          left: -100px;
          animation-delay: 2s;
        }
        
        .shape-3 {
          width: 150px;
          height: 150px;
          top: 50%;
          right: 10%;
          animation-delay: 4s;
        }
        
        .form-control:focus {
          border-color: #667eea !important;
          box-shadow: 0 0 0 0.2rem rgba(102, 126, 234, 0.25) !important;
        }
        
        .btn:hover {
          transform: translateY(-2px);
          box-shadow: 0 10px 30px rgba(102, 126, 234, 0.4);
        }
        
        .card {
          transition: all 0.3s ease;
        }
        
        .progress-bar {
          transition: width 0.6s ease;
        }
      `}</style>
    </div>
  );
};

export default EnterpriseCompanyRegister;