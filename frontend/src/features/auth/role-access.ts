import type { AuthUser } from './types'

const ADMIN_ONLY_PATHS = ['/system-users', '/role-overview']

const ROLE_PATHS: Record<string, string[]> = {
  ADMIN: [
    '/',
    '/system-users',
    '/role-overview',
    '/product-archives',
    '/product-origins',
    '/product-units',
    '/quality-grades',
    '/storage-conditions',
    '/warehouses',
    '/warehouse-zones',
    '/warehouse-locations',
    '/suppliers',
    '/customers',
    '/inbound-orders',
    '/putaway-tasks',
    '/inbound-records',
    '/outbound-orders',
    '/outbound-tasks',
    '/outbound-records',
    '/inventory-stocks',
    '/inventory-transactions',
    '/inventory-adjustments',
    '/inventory-stocktakings',
    '/quality-inspections',
    '/abnormal-stocks',
    '/loss-records',
    '/alert-rules',
    '/alerts',
    '/message-notices',
  ],
  WAREHOUSE_ADMIN: [
    '/warehouses',
    '/warehouse-zones',
    '/warehouse-locations',
    '/',
    '/product-archives',
    '/product-origins',
    '/product-units',
    '/quality-grades',
    '/storage-conditions',
    '/inbound-orders',
    '/putaway-tasks',
    '/inbound-records',
    '/outbound-orders',
    '/outbound-tasks',
    '/outbound-records',
    '/inventory-stocks',
    '/inventory-stocktakings',
    '/alerts',
    '/message-notices',
  ],
  INVENTORY_ADMIN: [
    '/inventory-stocks',
    '/',
    '/product-archives',
    '/product-origins',
    '/product-units',
    '/quality-grades',
    '/storage-conditions',
    '/inventory-transactions',
    '/inventory-adjustments',
    '/inventory-stocktakings',
    '/alerts',
    '/message-notices',
  ],
  QUALITY_ADMIN: [
    '/quality-inspections',
    '/',
    '/product-archives',
    '/product-origins',
    '/product-units',
    '/quality-grades',
    '/storage-conditions',
    '/warehouses',
    '/warehouse-zones',
    '/warehouse-locations',
    '/quality-inspections',
    '/abnormal-stocks',
    '/loss-records',
    '/alerts',
    '/message-notices',
  ],
}

const ROLE_MANAGE_PATHS: Record<string, string[]> = {
  ADMIN: ROLE_PATHS.ADMIN,
  WAREHOUSE_ADMIN: [
    '/warehouses',
    '/warehouse-zones',
    '/warehouse-locations',
    '/inbound-orders',
    '/putaway-tasks',
    '/outbound-orders',
    '/outbound-tasks',
    '/inventory-stocktakings',
    '/alerts',
    '/message-notices',
  ],
  INVENTORY_ADMIN: [
    '/inventory-adjustments',
    '/inventory-stocktakings',
    '/alerts',
    '/message-notices',
  ],
  QUALITY_ADMIN: [
    '/quality-inspections',
    '/abnormal-stocks',
    '/loss-records',
    '/alerts',
    '/message-notices',
  ],
}

const ROLE_DEFAULT_PATHS: Record<string, string> = {
  ADMIN: '/',
  WAREHOUSE_ADMIN: '/warehouses',
  INVENTORY_ADMIN: '/inventory-stocks',
  QUALITY_ADMIN: '/quality-inspections',
}

export function isPathAllowed(roleCode: string | undefined, path: string) {
  if (!roleCode) {
    return false
  }

  const allowedPaths = ROLE_PATHS[roleCode] ?? []
  return allowedPaths.includes(path)
}

export function getFirstAllowedPath(roleCode: string | undefined) {
  if (!roleCode) {
    return '/login'
  }

  return ROLE_DEFAULT_PATHS[roleCode] ?? ROLE_PATHS[roleCode]?.[0] ?? '/login'
}

export function isAdminOnlyPath(path: string) {
  return ADMIN_ONLY_PATHS.includes(path)
}

export function canManagePath(roleCode: string | undefined, path: string) {
  if (!roleCode) {
    return false
  }

  const manageablePaths = ROLE_MANAGE_PATHS[roleCode] ?? []
  return manageablePaths.includes(path)
}

export function isReadOnlyPath(roleCode: string | undefined, path: string) {
  return isPathAllowed(roleCode, path) && !canManagePath(roleCode, path)
}

export function resolveRoleCode(user: AuthUser | null | undefined) {
  if (user?.roleCode) {
    return user.roleCode
  }

  return user?.roles?.[0]
}
