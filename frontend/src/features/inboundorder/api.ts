import { request } from '../../api/http'

import type {
  InboundOrderDetail,
  InboundOrderFormPayload,
  InboundOrderListItem,
  InboundOrderListQuery,
} from './types'

export function getInboundOrderList(query?: InboundOrderListQuery) {
  return request<InboundOrderListItem[]>({
    method: 'GET',
    url: '/inbound-order/list',
    params: query,
  })
}

export function getInboundOrderDetail(inboundOrderId: string) {
  return request<InboundOrderDetail>({
    method: 'GET',
    url: `/inbound-order/${inboundOrderId}`,
  })
}

export function createInboundOrder(payload: InboundOrderFormPayload) {
  return request<InboundOrderDetail>({
    method: 'POST',
    url: '/inbound-order',
    data: payload,
  })
}

export function updateInboundOrder(
  inboundOrderId: string,
  payload: InboundOrderFormPayload,
) {
  return request<InboundOrderDetail>({
    method: 'PUT',
    url: `/inbound-order/${inboundOrderId}`,
    data: payload,
  })
}

export function confirmInboundOrderArrival(inboundOrderId: string) {
  return request<void>({
    method: 'PATCH',
    url: `/inbound-order/${inboundOrderId}/arrive`,
  })
}

export function cancelInboundOrder(inboundOrderId: string) {
  return request<void>({
    method: 'PATCH',
    url: `/inbound-order/${inboundOrderId}/cancel`,
  })
}
