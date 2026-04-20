import { Navigate, Outlet, useLocation } from 'react-router-dom'

import { getFirstAllowedPath, isPathAllowed, resolveRoleCode } from './role-access'
import { useAuthSession } from './useAuthSession'

function RoleBasedRoute() {
  const location = useLocation()
  const { user } = useAuthSession()

  if (!user) {
    return <Outlet />
  }

  const roleCode = resolveRoleCode(user)

  if (isPathAllowed(roleCode, location.pathname)) {
    return <Outlet />
  }

  return <Navigate to={getFirstAllowedPath(roleCode)} replace />
}

export default RoleBasedRoute
