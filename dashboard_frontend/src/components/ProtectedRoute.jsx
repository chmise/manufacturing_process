import { Navigate } from 'react-router-dom'
import { useState, useEffect } from 'react'

const ProtectedRoute = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(null) // null = checking, true/false = result

  useEffect(() => {
    checkTokenValidity()
  }, [])

  const checkTokenValidity = () => {
    const token = localStorage.getItem('accessToken')
    
    if (!token) {
      setIsAuthenticated(false)
      return
    }

    try {
      // JWT 토큰 디코딩하여 만료 시간 확인
      const payload = JSON.parse(atob(token.split('.')[1]))
      const currentTime = Date.now() / 1000
      
      if (payload.exp < currentTime) {
        // 토큰 만료됨
        localStorage.removeItem('accessToken')
        localStorage.removeItem('refreshToken')
        localStorage.removeItem('userData')
        localStorage.removeItem('isLoggedIn')
        setIsAuthenticated(false)
      } else {
        // 토큰 유효함
        setIsAuthenticated(true)
      }
    } catch (error) {
      // 토큰 파싱 실패
      console.error('Invalid token format:', error)
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('userData')
      localStorage.removeItem('isLoggedIn')
      setIsAuthenticated(false)
    }
  }

  // 인증 상태 확인 중
  if (isAuthenticated === null) {
    return (
      <div style={{ 
        display: 'flex', 
        justifyContent: 'center', 
        alignItems: 'center', 
        height: '100vh',
        fontSize: '18px'
      }}>
        인증 확인 중...
      </div>
    )
  }

  // 인증되지 않음
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />
  }

  // 인증됨
  return children
}

export default ProtectedRoute;
