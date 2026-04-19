import { request } from '../../api/http'

import type {
  SystemUserCreatePayload,
  SystemUserDetail,
  SystemUserListItem,
  SystemUserListQuery,
  SystemUserRoleOption,
  SystemUserUpdatePayload,
} from './types'

export function getSystemUserList(query?: SystemUserListQuery) {
  return request<SystemUserListItem[]>({
    method: 'GET',
    url: '/system-user/list',
    params: query,
  })
}

export function getSystemUserDetail(userId: string) {
  return request<SystemUserDetail>({
    method: 'GET',
    url: `/system-user/${userId}`,
  })
}

export function getSystemUserRoleOptions() {
  return request<SystemUserRoleOption[]>({
    method: 'GET',
    url: '/system-user/role-options',
  })
}

export function createSystemUser(payload: SystemUserCreatePayload) {
  return request<SystemUserDetail>({
    method: 'POST',
    url: '/system-user',
    data: payload,
  })
}

export function updateSystemUser(userId: string, payload: SystemUserUpdatePayload) {
  return request<SystemUserDetail>({
    method: 'PUT',
    url: `/system-user/${userId}`,
    data: payload,
  })
}

export function updateSystemUserStatus(userId: string, status: number) {
  return request<void>({
    method: 'PATCH',
    url: `/system-user/${userId}/status`,
    data: { status },
  })
}

export function resetSystemUserPassword(userId: string, newPassword: string) {
  return request<void>({
    method: 'PATCH',
    url: `/system-user/${userId}/reset-password`,
    data: { newPassword },
  })
}

export function deleteSystemUser(userId: string) {
  return request<void>({
    method: 'DELETE',
    url: `/system-user/${userId}`,
  })
}
