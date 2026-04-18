import axios from 'axios'

import type { ApiErrorPayload, FieldErrorItem } from './contracts'

interface AppErrorOptions {
  code: string
  message: string
  status?: number
  traceId?: string
  fieldErrors?: FieldErrorItem[]
  isNetworkError?: boolean
}

export class AppError extends Error {
  code: string
  status?: number
  traceId?: string
  fieldErrors: FieldErrorItem[]
  isNetworkError: boolean

  constructor(options: AppErrorOptions) {
    super(options.message)
    this.name = 'AppError'
    this.code = options.code
    this.status = options.status
    this.traceId = options.traceId
    this.fieldErrors = options.fieldErrors ?? []
    this.isNetworkError = options.isNetworkError ?? false
  }

  static fromPayload(payload: ApiErrorPayload, status?: number) {
    return new AppError({
      code: payload.code ?? 'UNKNOWN_ERROR',
      message: resolveFriendlyMessage(status, payload.message),
      status,
      traceId: payload.traceId,
      fieldErrors: payload.errors ?? [],
    })
  }
}

export function normalizeError(error: unknown): AppError {
  if (error instanceof AppError) {
    return error
  }

  if (axios.isAxiosError<ApiErrorPayload>(error)) {
    if (!error.response) {
      return new AppError({
        code: 'NETWORK_ERROR',
        message: '网络连接失败，请检查后端服务是否可用',
        isNetworkError: true,
      })
    }

    return AppError.fromPayload(error.response.data ?? {}, error.response.status)
  }

  return new AppError({
    code: 'UNKNOWN_ERROR',
    message: '系统繁忙，请稍后再试',
  })
}

export function getFieldError(
  fieldErrors: FieldErrorItem[] | undefined,
  field: string,
) {
  return fieldErrors?.find((item) => item.field === field)?.message
}

function resolveFriendlyMessage(status?: number, message?: string) {
  if (message?.trim()) {
    return message
  }

  if (status === 401) {
    return '登录已失效，请重新登录'
  }

  if (status === 403) {
    return '当前账号无权执行该操作'
  }

  if (status !== undefined && status >= 500) {
    return '系统繁忙，请稍后再试'
  }

  return '请求失败，请稍后再试'
}
