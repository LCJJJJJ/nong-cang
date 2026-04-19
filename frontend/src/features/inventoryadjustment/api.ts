import { request } from '../../api/http'

import type {
  InventoryAdjustmentDetail,
  InventoryAdjustmentFormPayload,
  InventoryAdjustmentListItem,
  InventoryAdjustmentListQuery,
} from './types'

export function getInventoryAdjustmentList(query?: InventoryAdjustmentListQuery) {
  return request<InventoryAdjustmentListItem[]>({
    method: 'GET',
    url: '/inventory-adjustment/list',
    params: query,
  })
}

export function createInventoryAdjustment(payload: InventoryAdjustmentFormPayload) {
  return request<InventoryAdjustmentDetail>({
    method: 'POST',
    url: '/inventory-adjustment',
    data: payload,
  })
}
