export interface FieldErrorItem {
  field: string
  message: string
}

export interface ApiResponse<T> {
  success: boolean
  code: string
  message: string
  data: T | null
  errors?: FieldErrorItem[] | null
  traceId?: string
}

export interface PageResponse<T> {
  items: T[]
  total: number
  page: number
  size: number
}

export interface ApiErrorPayload {
  code?: string
  message?: string
  errors?: FieldErrorItem[] | null
  traceId?: string
}
