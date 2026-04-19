import { request } from '../../api/http'

import type { OutboundRecordListItem, OutboundRecordListQuery } from './types'

export function getOutboundRecordList(query?: OutboundRecordListQuery) {
  return request<OutboundRecordListItem[]>({
    method: 'GET',
    url: '/outbound-record/list',
    params: query,
  })
}
