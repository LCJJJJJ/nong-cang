import { request } from '../../api/http'

import type {
  AssistantChatPayload,
  AssistantChatResponse,
  AssistantMessage,
  AssistantSessionListItem,
} from './types'

export function getAssistantSessions() {
  return request<AssistantSessionListItem[]>({
    method: 'GET',
    url: '/assistant/sessions',
  })
}

export function getAssistantMessages(sessionId: string) {
  return request<AssistantMessage[]>({
    method: 'GET',
    url: `/assistant/sessions/${sessionId}/messages`,
  })
}

export function chatWithAssistant(payload: AssistantChatPayload) {
  return request<AssistantChatResponse>({
    method: 'POST',
    url: '/assistant/chat',
    data: payload,
  })
}
