import { Navigate, Outlet } from 'react-router-dom'

import { useAuthSession } from './useAuthSession'

function GuestOnlyRoute() {
  const { status } = useAuthSession()

  if (status === 'checking') {
    return <div className="auth-route-placeholder">正在校验登录状态...</div>
  }

  if (status === 'authenticated') {
    return <Navigate to="/" replace />
  }

  return <Outlet />
}

export default GuestOnlyRoute
