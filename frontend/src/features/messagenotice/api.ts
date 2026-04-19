import { request } from '../../api/http'

import type { MessageNoticeListItem, MessageNoticeListQuery } from './types'

export function getMessageNoticeList(query?: MessageNoticeListQuery) {
  return request<MessageNoticeListItem[]>({
    method: 'GET',
    url: '/message-notice/list',
    params: query,
  })
}

export function markMessageNoticeRead(noticeId: string) {
  return request<void>({
    method: 'PATCH',
    url: `/message-notice/${noticeId}/read`,
  })
}

export function markAllMessageNoticeRead() {
  return request<void>({
    method: 'PATCH',
    url: '/message-notice/read-all',
  })
}
