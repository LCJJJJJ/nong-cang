import { Navigate, Outlet, useLocation } from 'react-router-dom'

import { useAuthSession } from './useAuthSession'

function RequireAuth() {
  const location = useLocation()
  const { status } = useAuthSession()

  if (status === 'checking') {
    return <div className="auth-route-placeholder">正在校验登录状态...</div>
  }

  if (status !== 'authenticated') {
    return <Navigate to="/login" replace state={{ from: location }} />
  }

  return <Outlet />
}

export default RequireAuth
