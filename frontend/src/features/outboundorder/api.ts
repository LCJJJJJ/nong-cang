import { request } from '../../api/http'

import type {
  OutboundOrderDetail,
  OutboundOrderFormPayload,
  OutboundOrderListItem,
  OutboundOrderListQuery,
} from './types'

export function getOutboundOrderList(query?: OutboundOrderListQuery) {
  return request<OutboundOrderListItem[]>({
    method: 'GET',
    url: '/outbound-order/list',
    params: query,
  })
}

export function getOutboundOrderDetail(outboundOrderId: string) {
  return request<OutboundOrderDetail>({
    method: 'GET',
    url: `/outbound-order/${outboundOrderId}`,
  })
}

export function createOutboundOrder(payload: OutboundOrderFormPayload) {
  return request<OutboundOrderDetail>({
    method: 'POST',
    url: '/outbound-order',
    data: payload,
  })
}

export function updateOutboundOrder(
  outboundOrderId: string,
  payload: OutboundOrderFormPayload,
) {
  return request<OutboundOrderDetail>({
    method: 'PUT',
    url: `/outbound-order/${outboundOrderId}`,
    data: payload,
  })
}

export function cancelOutboundOrder(outboundOrderId: string) {
  return request<void>({
    method: 'PATCH',
    url: `/outbound-order/${outboundOrderId}/cancel`,
  })
}
