import axios, { type AxiosRequestConfig, type InternalAxiosRequestConfig } from 'axios'

import type { ApiResponse } from './contracts'
import { AppError, normalizeError } from './errors'
import { clearAuthSession, getAccessToken, getRefreshToken, saveAuthSession } from '../features/auth/storage'
import type { AuthTokenResponse } from '../features/auth/types'

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '/api',
  timeout: 10000,
  headers: {
    Accept: 'application/json',
    'Content-Type': 'application/json',
  },
})

const refreshClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '/api',
  timeout: 10000,
  headers: {
    Accept: 'application/json',
    'Content-Type': 'application/json',
  },
})

let refreshPromise: Promise<AuthTokenResponse> | null = null

http.interceptors.request.use((config) => {
  const accessToken = getAccessToken()

  if (accessToken) {
    config.headers = config.headers ?? {}
    config.headers.Authorization = `Bearer ${accessToken}`
  }

  return config
})

http.interceptors.response.use(
  (response) => response,
  async (error: unknown) => {
    if (!axios.isAxiosError(error)) {
      return Promise.reject(normalizeError(error))
    }

    const originalRequest = error.config as
      | (InternalAxiosRequestConfig & { _retry?: boolean })
      | undefined
    const status = error.response?.status
    const requestUrl = originalRequest?.url ?? ''
    const isAuthEndpoint =
      requestUrl.includes('/auth/login') || requestUrl.includes('/auth/refresh')
    const refreshToken = getRefreshToken()

    if (
      status === 401 &&
      originalRequest &&
      !originalRequest._retry &&
      !isAuthEndpoint &&
      refreshToken
    ) {
      originalRequest._retry = true

      try {
        const refreshedSession = await refreshAccessToken(refreshToken)
        originalRequest.headers = originalRequest.headers ?? {}
        originalRequest.headers.Authorization = `Bearer ${refreshedSession.accessToken}`
        return http(originalRequest)
      } catch (refreshError) {
        clearAuthSession()
        redirectToLogin()
        return Promise.reject(normalizeError(refreshError))
      }
    }

    if (status === 401 && !isAuthEndpoint) {
      clearAuthSession()
      redirectToLogin()
    }

    return Promise.reject(normalizeError(error))
  },
)

export async function request<T>(config: AxiosRequestConfig) {
  try {
    const response = await http.request<ApiResponse<T>>(config)
    const payload = response.data as ApiResponse<T> | string | null

    if (response.status === 204 || payload === '' || payload == null) {
      return undefined as T
    }

    if (typeof payload !== 'object') {
      throw new AppError({
        code: 'INVALID_RESPONSE',
        message: '接口响应格式不正确',
        status: response.status,
      })
    }

    if (!payload.success) {
      throw AppError.fromPayload(payload, response.status)
    }

    return payload.data as T
  } catch (error) {
    throw normalizeError(error)
  }
}

export { http }

async function refreshAccessToken(refreshToken: string) {
  if (!refreshPromise) {
    refreshPromise = refreshClient
      .post<ApiResponse<AuthTokenResponse>>('/auth/refresh', {
        refreshToken,
      })
      .then((response) => {
        const payload = response.data

        if (!payload.success || !payload.data) {
          throw AppError.fromPayload(payload, response.status)
        }

        saveAuthSession(payload.data)
        return payload.data
      })
      .finally(() => {
        refreshPromise = null
      })
  }

  return refreshPromise
}

function redirectToLogin() {
  if (typeof window === 'undefined') {
    return
  }

  if (window.location.pathname !== '/login') {
    window.location.replace('/login')
  }
}
