import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import "../styles/Login.css";

const Login = ({ onLogin }) => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const handleSubmit = (e) => {
    e.preventDefault();

    // 간단한 로그인 검증 (실제로는 서버에서 처리)
    if (username === "admin" && password === "password") {
      setError("");
      onLogin();
      navigate("/dashboard");
    } else {
      setError("잘못된 사용자명 또는 비밀번호입니다.");
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

          <button type="submit" className="login-button">
            로그인
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
