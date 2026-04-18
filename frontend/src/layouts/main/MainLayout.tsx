import { Link, Outlet, useNavigate } from 'react-router-dom'

import { useAuthSession } from '../../features/auth/useAuthSession'
import './MainLayout.css'

function MainLayout() {
  const navigate = useNavigate()
  const { setLoggedOut, user: authUser } = useAuthSession()

  const handleLogout = () => {
    setLoggedOut()
    navigate('/login', { replace: true })
  }

  return (
    <div className="main-layout">
      <div className="main-layout__frame">
        <header className="main-layout__header">
          <div className="main-layout__brand">
            <span className="main-layout__brand-mark" />
            <div>
              <strong>农产品仓库管理系统</strong>
              <p>
                {authUser ? `当前登录：${authUser.displayName}` : '主业务布局示例'}
              </p>
            </div>
          </div>

          <nav className="main-layout__nav">
            <Link to="/">首页</Link>
            <button type="button" onClick={handleLogout}>
              退出登录
            </button>
          </nav>
        </header>

        <div className="main-layout__body">
          <Outlet />
        </div>
      </div>
    </div>
  )
}

export default MainLayout
