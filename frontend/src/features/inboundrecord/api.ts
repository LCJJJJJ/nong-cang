import { request } from '../../api/http'

import type { InboundRecordListItem, InboundRecordListQuery } from './types'

export function getInboundRecordList(query?: InboundRecordListQuery) {
  return request<InboundRecordListItem[]>({
    method: 'GET',
    url: '/inbound-record/list',
    params: query,
  })
}
