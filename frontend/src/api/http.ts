import axios, { type AxiosRequestConfig } from 'axios'

import type { ApiResponse } from './contracts'
import { AppError, normalizeError } from './errors'

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '/api',
  timeout: 10000,
  headers: {
    Accept: 'application/json',
    'Content-Type': 'application/json',
  },
})

http.interceptors.response.use(
  (response) => response,
  (error: unknown) => Promise.reject(normalizeError(error)),
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
