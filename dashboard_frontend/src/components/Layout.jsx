import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useState, useEffect } from 'react'
import { apiService } from '../service/apiService'

const Layout = ({ children }) => {
  const location = useLocation()
  const navigate = useNavigate()
  const [user, setUser] = useState(null)

  // 회사명을 포함한 URL 생성
  const getCompanyUrl = (path) => {
    if (!user?.companyName) return path
    return `/${user.companyName}${path}`
  }

  // 멀티테넌트 경로 체크
  const isActive = (path) => {
    const currentPath = location.pathname
    const companyPath = user?.companyName ? `/${user.companyName}${path}` : path
    
    // 현재 경로가 회사별 경로와 일치하는지 확인
    return currentPath === companyPath || 
           (path === '/dashboard' && (currentPath === '/' || currentPath.endsWith('/dashboard'))) ? 'active' : ''
  }

  useEffect(() => {
    const token = localStorage.getItem('accessToken')
    if (token) {
      fetchUserInfo(token)
    } else {
      navigate('/login')
    }
  }, [navigate])

  const fetchUserInfo = async (token) => {
    try {
      const userData = await apiService.auth.getCurrentUser()
      setUser(userData)
    } catch (error) {
      console.error('Failed to fetch user info:', error)
      // apiService에서 이미 토큰 정리와 리다이렉트를 처리함
      navigate('/login')
    }
  }

  const handleLogout = () => {
    apiService.auth.logout()
  }

  return (
    <div>
      <header className="navbar navbar-expand-md d-print-none">
        <div className="container-xl">
          <button
            className="navbar-toggler"
            type="button"
            data-bs-toggle="collapse"
            data-bs-target="#navbar-menu"
            aria-controls="navbar-menu"
            aria-expanded="false"
            aria-label="Toggle navigation"
          >
            <span className="navbar-toggler-icon"></span>
          </button>

          <Link to={getCompanyUrl('/dashboard')} aria-label="U1MOBIS" className="navbar-brand navbar-brand-autodark me-3">
            <img src="/LOGO.png" alt="로고" style={{ height: '32px' }} />
          </Link>
          <div className="collapse navbar-collapse" id="navbar-menu">
            <ul className="navbar-nav">
              <li className={`nav-item ${isActive('/dashboard')}`}>
                <Link className="nav-link" to={getCompanyUrl('/dashboard')}>
                  <span className="nav-link-icon">
                    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="icon icon-tabler icons-tabler-outline icon-tabler-layout-dashboard">
                      <path stroke="none" d="M0 0h24v24H0z" fill="none" />
                      <path d="M5 4h4a1 1 0 0 1 1 1v6a1 1 0 0 1 -1 1h-4a1 1 0 0 1 -1 -1v-6a1 1 0 0 1 1 -1" />
                      <path d="M5 16h4a1 1 0 0 1 1 1v2a1 1 0 0 1 -1 1h-4a1 1 0 0 1 -1 -1v-2a1 1 0 0 1 1 -1" />
                      <path d="M15 12h4a1 1 0 0 1 1 1v6a1 1 0 0 1 -1 1h-4a1 1 0 0 1 -1 -1v-6a1 1 0 0 1 1 -1" />
                      <path d="M15 4h4a1 1 0 0 1 1 1v2a1 1 0 0 1 -1 1h-4a1 1 0 0 1 -1 -1v-2a1 1 0 0 1 1 -1" />
                    </svg>
                  </span>
                  <span className="nav-link-title">대시보드</span>
                </Link>
              </li>
              <li className={`nav-item ${isActive('/factory3d')}`}>
                <Link className="nav-link" to={getCompanyUrl('/factory3d')}>
                  <span className="nav-link-icon">
                    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <path stroke="none" d="M0 0h24v24H0z" fill="none" />
                      <path d="M3 7a2 2 0 0 1 2 -2h14a2 2 0 0 1 2 2v10a2 2 0 0 1 -2 2h-14a2 2 0 0 1 -2 -2z" />
                      <path d="M7 9h2a1 1 0 0 1 1 1v1a1 1 0 0 1 -1 1h-1a1 1 0 0 0 -1 1v1a1 1 0 0 0 1 1h2" />
                      <path d="M14 9v6h1a2 2 0 0 0 2 -2v-2a2 2 0 0 0 -2 -2z" />
                    </svg>
                  </span>
                  <span className="nav-link-title">트윈</span>
                </Link>
              </li>
              <li className={`nav-item ${isActive('/inventory')}`}>
                <Link className="nav-link" to={getCompanyUrl('/inventory')}>
                  <span className="nav-link-icon">
                    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="icon icon-1">
                      <path d="M9 11l3 3l8 -8" />
                      <path d="M20 12v6a2 2 0 0 1 -2 2h-12a2 2 0 0 1 -2 -2v-12a2 2 0 0 1 2 -2h9" />
                    </svg>
                  </span>
                  <span className="nav-link-title">관리</span>
                </Link>
              </li>
            </ul>
          </div>

          <div className="navbar-nav flex-row order-md-last ms-auto">
            <div className="nav-item dropdown">
              <a href="#" className="nav-link d-flex lh-1 text-reset" data-bs-toggle="dropdown" aria-label="Open user menu">
                <span className="avatar avatar-sm">
                  <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="icon icon-tabler icons-tabler-outline icon-tabler-user-circle">
                    <path stroke="none" d="M0 0h24v24H0z" fill="none" />
                    <path d="M12 12m-9 0a9 9 0 1 0 18 0a9 9 0 1 0 -18 0" />
                    <path d="M12 10m-3 0a3 3 0 1 0 6 0a3 3 0 1 0 -6 0" />
                    <path d="M6.168 18.849a4 4 0 0 1 3.832 -2.849h4a4 4 0 0 1 3.834 2.855" />
                  </svg>
                </span>
                <div className="d-none d-xl-block ps-2">
                  <div>{user?.userName || 'Loading...'}</div>
                  <div className="mt-1 small text-secondary">{user?.companyName || ''}</div>
                </div>
              </a>
              <div className="dropdown-menu dropdown-menu-end dropdown-menu-arrow">
                <a href="#" className="dropdown-item">Profile</a>
                <div className="dropdown-divider"></div>
                <a href="#" className="dropdown-item" onClick={handleLogout}>Logout</a>
              </div>
            </div>
          </div>
        </div>
      </header>

      <div className="page">
        <div className="page-wrapper">
          <div className="page-body">
            <div className="container-xl">
              {children}
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default Layout