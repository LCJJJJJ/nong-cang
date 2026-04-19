export interface SystemUserListItem {
  id: string
  userCode: string
  username: string
  displayName: string
  phone: string
  roleCode: string
  roleName: string
  warehouseId: string | null
  warehouseName: string | null
  status: number
  statusLabel: string
  remarks: string | null
  createdAt: string
  updatedAt: string
}

export type SystemUserDetail = SystemUserListItem

export interface SystemUserListQuery {
  username?: string
  displayName?: string
  roleCode?: string
  warehouseId?: string
  status?: number
}

export interface SystemUserRoleOption {
  roleCode: string
  roleName: string
  description: string
  warehouseRequired: boolean
}

export interface SystemUserCreatePayload {
  username: string
  displayName: string
  phone: string
  roleCode: string
  warehouseId: number | null
  status: number
  initialPassword: string
  remarks: string | null
}

export interface SystemUserUpdatePayload {
  displayName: string
  phone: string
  roleCode: string
  warehouseId: number | null
  status: number
  remarks: string | null
}
