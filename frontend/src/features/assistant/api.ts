import { request } from '../../api/http'
import { AppError } from '../../api/errors'
import { getAccessToken } from '../auth/storage'

import type {
  AssistantActionExecuteResponse,
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

interface AssistantStreamHandlers {
  onSession?: (session: AssistantSessionListItem) => void
  onStatus?: (message: string) => void
  onDelta?: (content: string) => void
  onDone?: (response: AssistantChatResponse) => void
}

export async function streamAssistantChat(
  payload: AssistantChatPayload,
  handlers: AssistantStreamHandlers,
) {
  const accessToken = getAccessToken()
  const response = await fetch(resolveApiUrl('/assistant/chat/stream'), {
    method: 'POST',
    headers: {
      Accept: 'text/event-stream',
      'Content-Type': 'application/json',
      ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}),
    },
    body: JSON.stringify(payload),
  })

  if (!response.ok) {
    const errorPayload = (await safeParseJson(response)) as
      | { code?: string; message?: string; traceId?: string }
      | undefined

    throw AppError.fromPayload(errorPayload ?? {}, response.status)
  }

  if (!response.body) {
    throw new AppError({
      code: 'STREAM_UNAVAILABLE',
      message: '智能助手流式连接不可用',
      status: response.status,
    })
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder()
  let buffer = ''
  let streamCompleted = false

  while (true) {
    const { done, value } = await reader.read()
    buffer += decoder.decode(value ?? new Uint8Array(), { stream: !done })

    let separatorIndex = buffer.indexOf('\n\n')
    while (separatorIndex >= 0) {
      const rawEvent = buffer.slice(0, separatorIndex)
      buffer = buffer.slice(separatorIndex + 2)
      streamCompleted = handleSseEvent(rawEvent, handlers) || streamCompleted
      if (streamCompleted) {
        await reader.cancel()
        return
      }
      separatorIndex = buffer.indexOf('\n\n')
    }

    if (done) {
      break
    }
  }
}

export function executeAssistantAction(
  actionCode: string,
  confirmationText?: string,
) {
  return request<AssistantActionExecuteResponse>({
    method: 'PATCH',
    url: `/assistant/action-plans/${actionCode}/execute`,
    data: confirmationText ? { confirmationText } : {},
  })
}

function handleSseEvent(rawEvent: string, handlers: AssistantStreamHandlers) {
  const lines = rawEvent.split('\n')
  const eventName = lines
    .find((line) => line.startsWith('event:'))
    ?.slice('event:'.length)
    .trim()
  const dataText = lines
    .filter((line) => line.startsWith('data:'))
    .map((line) => line.slice('data:'.length).trim())
    .join('\n')

  if (!eventName || !dataText) {
    return false
  }

  const data = JSON.parse(dataText) as
    | { message?: string }
    | AssistantSessionListItem
    | AssistantChatResponse

  if (eventName === 'session' && handlers.onSession) {
    handlers.onSession(data as AssistantSessionListItem)
  }

  if (eventName === 'status' && handlers.onStatus) {
    handlers.onStatus((data as { message?: string }).message ?? '')
  }

  if (eventName === 'delta' && handlers.onDelta) {
    handlers.onDelta((data as { message?: string }).message ?? '')
  }

  if (eventName === 'done' && handlers.onDone) {
    handlers.onDone(data as AssistantChatResponse)
    return true
  }

  if (eventName === 'error') {
    throw new AppError({
      code: 'STREAM_ERROR',
      message: (data as { message?: string }).message ?? '智能助手流式调用失败',
    })
  }

  return false
}

async function safeParseJson(response: Response) {
  try {
    return await response.json()
  } catch {
    return undefined
  }
}

function resolveApiUrl(path: string) {
  const baseUrl = import.meta.env.VITE_API_BASE_URL ?? '/api'

  if (baseUrl.startsWith('http://') || baseUrl.startsWith('https://')) {
    return `${baseUrl}${path}`
  }

  return `${baseUrl}${path}`
}
