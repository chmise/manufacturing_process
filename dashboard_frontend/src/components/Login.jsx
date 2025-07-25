import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import apiService from "../service/apiService";
import "../styles/Login.css";

const Login = ({ onLogin }) => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");

    try {
      const loginResponse = await apiService.user.login({ username, password });
      
      if (loginResponse.success) {
        // JWT 토큰 저장
        localStorage.setItem('accessToken', loginResponse.accessToken);
        localStorage.setItem('refreshToken', loginResponse.refreshToken);
        
        // 사용자 정보를 localStorage에 저장
        const userData = {
          userName: loginResponse.userName,
          companyName: loginResponse.companyName,
          userId: loginResponse.userId,
          companyId: loginResponse.companyId
        };
        localStorage.setItem('userData', JSON.stringify(userData));
        localStorage.setItem('isLoggedIn', 'true');
        
        // Unity에 회사명 전달
        if (window.SetCompanyName && loginResponse.companyName) {
          setTimeout(() => {
            window.SetCompanyName(loginResponse.companyName);
          }, 1000); // Unity 로딩을 기다림
        }
        
        onLogin(userData);
        navigate("/dashboard");
      } else {
        setError("잘못된 사용자명 또는 비밀번호입니다.");
      }
    } catch (err) {
      console.error('로그인 오류:', err);
      setError("서버 연결에 실패했습니다. 잠시 후 다시 시도해주세요.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <div className="login-box">
        <h1 className="login-title">Login</h1>
        <p className="login-subtitle">안녕하세요. U1 MOBIS 입니다.</p>

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <div className="input-wrapper">
              <span className="input-icon">👤</span>
              <input
                type="text"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="아이디 또는 이메일을 입력하세요."
                required
              />
            </div>
          </div>

          <div className="form-group">
            <div className="input-wrapper">
              <span className="input-icon">🔒</span>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="비밀번호를 입력하세요."
                required
              />
            </div>
          </div>

          {error && <div className="error-message">{error}</div>}

          <button type="submit" className="login-button" disabled={loading}>
            {loading ? "로그인 중..." : "로그인"}
          </button>
        </form>

        <div className="signup-link">
          <span>계정이 없으신가요? </span>
          <a
            href="#"
            onClick={(e) => {
              e.preventDefault();
              navigate("/register"); 
            }}
          >
            회원가입
          </a>
        </div>

        <div className="login-info">
          <p>
            <strong>테스트용 계정:</strong>
          </p>
          <p>사용자명: admin 비밀번호: password</p>
        </div>
      </div>
    </div>
  );
};

export default Login;   
