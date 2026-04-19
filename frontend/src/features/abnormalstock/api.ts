import { request } from '../../api/http'

import type {
  AbnormalStockDetail,
  AbnormalStockListItem,
  AbnormalStockListQuery,
  AbnormalStockOption,
} from './types'

export function getAbnormalStockList(query?: AbnormalStockListQuery) {
  return request<AbnormalStockListItem[]>({
    method: 'GET',
    url: '/abnormal-stock/list',
    params: query,
  })
}

export function getAbnormalStockDetail(abnormalStockId: string) {
  return request<AbnormalStockDetail>({
    method: 'GET',
    url: `/abnormal-stock/${abnormalStockId}`,
  })
}

export function getAbnormalStockOptions() {
  return request<AbnormalStockOption[]>({
    method: 'GET',
    url: '/abnormal-stock/options',
  })
}
