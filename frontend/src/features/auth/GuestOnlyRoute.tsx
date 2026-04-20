import { Navigate, Outlet } from 'react-router-dom'

import { getFirstAllowedPath, resolveRoleCode } from './role-access'
import { useAuthSession } from './useAuthSession'

function GuestOnlyRoute() {
  const { status, user } = useAuthSession()

  if (status === 'checking') {
    return <div className="auth-route-placeholder">正在校验登录状态...</div>
  }

  if (status === 'authenticated') {
    return <Navigate to={getFirstAllowedPath(resolveRoleCode(user))} replace />
  }

  return <Outlet />
}

export default GuestOnlyRoute
