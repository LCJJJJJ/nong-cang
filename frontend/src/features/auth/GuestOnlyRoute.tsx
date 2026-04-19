import { Navigate, Outlet } from 'react-router-dom'

import { getFirstAllowedPath } from './role-access'
import { useAuthSession } from './useAuthSession'

function GuestOnlyRoute() {
  const { status, user } = useAuthSession()

  if (status === 'checking') {
    return <div className="auth-route-placeholder">正在校验登录状态...</div>
  }

  if (status === 'authenticated') {
    return <Navigate to={getFirstAllowedPath(user?.roleCode)} replace />
  }

  return <Outlet />
}

export default GuestOnlyRoute
