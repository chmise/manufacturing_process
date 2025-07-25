import React, { useState } from 'react';
import '../styles/Register.css';

const Register = () => {
  const [form, setForm] = useState({
    employeeCode: '',
    name: '',
    username: '',
    password: '',
    confirmPassword: '',
    email: '',
    birth: '',
    companyName: '',
  });

  const [error, setError] = useState('');

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (form.password !== form.confirmPassword) {
      setError('비밀번호가 일치하지 않습니다.');
      return;
    }
    setError('');

    try {
      const response = await fetch('http://localhost:8080/api/user/register', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          username: form.username,
          password: form.password,
          companyName: form.companyName,
          email: form.email,
          birth: form.birth,
        }),
      });

      const data = await response.json();

      if (data.success) {
        // 토큰 저장 (자동 로그인)
        localStorage.setItem('accessToken', data.accessToken);
        localStorage.setItem('refreshToken', data.refreshToken);
        
        alert('회원가입이 완료되었습니다!');
        // 대시보드로 이동
        window.location.href = '/dashboard';
      } else {
        setError(data.message || '회원가입에 실패했습니다.');
      }
    } catch (error) {
      console.error('Registration error:', error);
      setError('서버와의 통신에 실패했습니다.');
    }
  };

  return (
    <div className="register-container">
      <div className="register-box">
        <h1 className="register-title">회원가입</h1>
        <p className="register-subtitle">환영합니다! 아래 정보를 입력해주세요.</p>

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>사원코드</label>
            <div className="input-wrapper">
              <span className="input-icon">🆔</span>
              <input
                name="employeeCode"
                value={form.employeeCode}
                onChange={handleChange}
                placeholder="사원코드를 입력하세요"
                required
              />
            </div>
          </div>

          <div className="form-group">
            <label>이름</label>
            <div className="input-wrapper">
              <span className="input-icon">🙍‍♂️</span>
              <input
                name="name"
                value={form.name}
                onChange={handleChange}
                placeholder="이름을 입력하세요"
                required
              />
            </div>
          </div>

          <div className="form-group">
            <label>사용할 ID</label>
            <div className="input-wrapper">
              <span className="input-icon">👤</span>
              <input
                name="username"
                value={form.username}
                onChange={handleChange}
                placeholder="ID를 입력하세요"
                required
              />
            </div>
          </div>

          <div className="form-group">
            <label>사용할 PW</label>
            <div className="input-wrapper">
              <span className="input-icon">🔒</span>
              <input
                name="password"
                type="password"
                value={form.password}
                onChange={handleChange}
                placeholder="비밀번호를 입력하세요"
                required
              />
            </div>
          </div>

          <div className="form-group">
            <label>PW 확인</label>
            <div className="input-wrapper">
              <span className="input-icon">🔒</span>
              <input
                name="confirmPassword"
                type="password"
                value={form.confirmPassword}
                onChange={handleChange}
                placeholder="비밀번호를 다시 입력하세요"
                required
              />
            </div>
          </div>

          <div className="form-group">
            <label>이메일</label>
            <div className="input-wrapper">
              <span className="input-icon">📧</span>
              <input
                name="email"
                type="email"
                value={form.email}
                onChange={handleChange}
                placeholder="이메일을 입력하세요"
                required
              />
            </div>
          </div>

          <div className="form-group">
            <label>생년월일</label>
            <div className="input-wrapper">
              <span className="input-icon">🎂</span>
              <input
                name="birth"
                type="date"
                value={form.birth}
                onChange={handleChange}
                required
              />
            </div>
          </div>

          <div className="form-group">
            <label>회사명</label>
            <div className="input-wrapper">
              <span className="input-icon">🏢</span>
              <input
                name="companyName"
                value={form.companyName}
                onChange={handleChange}
                placeholder="회사명을 입력하세요"
                required
              />
            </div>
          </div>

          {error && <div className="error-message">{error}</div>}

          <button type="submit" className="register-button">
            회원가입
          </button>
        </form>

        <div className="signup-link">
          이미 계정이 있으신가요? <a href="/login">로그인</a>
        </div>
      </div>
    </div>
  );
};

export default Register;
