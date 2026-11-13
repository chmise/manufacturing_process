import { useState } from 'react'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import Layout from './components/Layout'
import Dashboard from './components/Dashboard'
import Factory3D from './components/Factory3D'
import Inventory from './components/Inventory'
import ApiTest from './components/ApiTest'
import SimulatorControl from './components/Simulator/SimulatorControl'

import Login from './components/Login'
import Register from './components/Register'
import CompanyRegister from './components/CompanyRegister'
import ProtectedRoute from './components/ProtectedRoute'


function App() {
  // 로그인 여부는 ProtectedRoute가 localStorage로 확인한다. 여기서는 로그인/로그아웃 시 다시 그리기 위한 상태만 둔다
  const [, setIsLoggedIn] = useState(() => {
    return localStorage.getItem('isLoggedIn') === 'true'
  })

  const handleLogin = () => {
    localStorage.setItem('isLoggedIn', 'true')
    setIsLoggedIn(true)
  }

  const handleLogout = () => {
    localStorage.removeItem('isLoggedIn')
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('userData')
    setIsLoggedIn(false)
  }

  return (
    <BrowserRouter>

      <Routes>
        {/* 최초 진입 시 로그인 페이지로 리디렉션 */}
        <Route path="/" element={<Navigate to="/login" replace />} />

        <Route path="/login" element={<Login onLogin={handleLogin} />} />
        <Route path="/register" element={<Register />} />
        <Route path="/company-register" element={<CompanyRegister />} />

        <Route
          path="/:company/*"
          element={
            <ProtectedRoute>
              <Layout onLogout={handleLogout}>
                <Routes>
                  <Route path="/dashboard" element={<Dashboard />} />
                  <Route path="/factory3d" element={<Factory3D />} />
                  <Route path="/inventory" element={<Inventory />} />
                  <Route path="/simulator" element={<SimulatorControl />} />
                  {/* API 테스트 화면은 개발 모드(npm run dev)에서만 연다 */}
                  {import.meta.env.DEV && <Route path="/api-test" element={<ApiTest />} />}
                </Routes>
              </Layout>
            </ProtectedRoute>
          }
        />
      </Routes>

    </BrowserRouter>
  )
}

export default App
