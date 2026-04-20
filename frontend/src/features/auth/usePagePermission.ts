import { useLocation } from 'react-router-dom'

import { canManagePath, isReadOnlyPath, resolveRoleCode } from './role-access'
import { useAuthSession } from './useAuthSession'

export function usePagePermission() {
  const { user } = useAuthSession()
  const location = useLocation()
  const roleCode = resolveRoleCode(user)

  return {
    roleCode,
    canManage: canManagePath(roleCode, location.pathname),
    isReadOnly: isReadOnlyPath(roleCode, location.pathname),
  }
}
