import { request } from '../../api/http'

import type { AuthTokenResponse, LoginRequest } from './types'

export function login(payload: LoginRequest) {
  return request<AuthTokenResponse>({
    method: 'POST',
    url: '/auth/login',
    data: payload,
  })
}

export function getCurrentUser() {
  return request<AuthTokenResponse['user']>({
    method: 'GET',
    url: '/auth/me',
  })
}
