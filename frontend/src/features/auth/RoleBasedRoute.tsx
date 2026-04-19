import { Navigate, Outlet, useLocation } from 'react-router-dom'

import { getFirstAllowedPath, isPathAllowed } from './role-access'
import { useAuthSession } from './useAuthSession'

function RoleBasedRoute() {
  const location = useLocation()
  const { user } = useAuthSession()

  if (!user) {
    return <Outlet />
  }

  if (isPathAllowed(user.roleCode, location.pathname)) {
    return <Outlet />
  }

  return <Navigate to={getFirstAllowedPath(user.roleCode)} replace />
}

export default RoleBasedRoute
