import { request } from '../../api/http'

import type {
  SystemEchoRequest,
  SystemEchoResponse,
  SystemPingResponse,
} from './types'

export function getSystemPing() {
  return request<SystemPingResponse>({
    method: 'GET',
    url: '/system/ping',
  })
}

export function postSystemEcho(payload: SystemEchoRequest) {
  return request<SystemEchoResponse>({
    method: 'POST',
    url: '/system/echo',
    data: payload,
  })
}

export function triggerSystemBusinessError() {
  return request<void>({
    method: 'GET',
    url: '/system/business-error',
  })
}
