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
    '/',
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
    '/inventory-stocktakings',
    '/quality-inspections',
    '/abnormal-stocks',
    '/loss-records',
    '/alerts',
    '/message-notices',
  ],
  INVENTORY_ADMIN: [
    '/',
    '/product-archives',
    '/product-origins',
    '/product-units',
    '/quality-grades',
    '/storage-conditions',
    '/warehouses',
    '/inventory-stocks',
    '/inventory-transactions',
    '/inventory-adjustments',
    '/inventory-stocktakings',
    '/alerts',
    '/message-notices',
  ],
  QUALITY_ADMIN: [
    '/',
    '/product-archives',
    '/product-origins',
    '/product-units',
    '/quality-grades',
    '/storage-conditions',
    '/warehouses',
    '/quality-inspections',
    '/abnormal-stocks',
    '/loss-records',
    '/alerts',
    '/message-notices',
  ],
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

  const allowedPaths = ROLE_PATHS[roleCode] ?? []
  return allowedPaths[0] ?? '/login'
}

export function isAdminOnlyPath(path: string) {
  return ADMIN_ONLY_PATHS.includes(path)
}
