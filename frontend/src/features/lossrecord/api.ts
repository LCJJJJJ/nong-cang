import { request } from '../../api/http'

import type {
  LossRecordDetail,
  LossRecordDirectPayload,
  LossRecordListItem,
  LossRecordListQuery,
} from './types'

export function getLossRecordList(query?: LossRecordListQuery) {
  return request<LossRecordListItem[]>({
    method: 'GET',
    url: '/loss-record/list',
    params: query,
  })
}

export function createDirectLossRecord(payload: LossRecordDirectPayload) {
  return request<LossRecordDetail>({
    method: 'POST',
    url: '/loss-record/direct',
    data: payload,
  })
}
