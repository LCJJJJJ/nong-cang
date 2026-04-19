export interface AssistantSessionListItem {
  id: string
  sessionCode: string
  title: string
  routePath: string | null
  routeTitle: string | null
  lastMessagePreview: string | null
  updatedAt: string
}

export interface AssistantColumn {
  key: string
  label: string
}

export interface AssistantResultBlock {
  title: string
  summary: string
  routePath: string | null
  routeLabel: string | null
  columns: AssistantColumn[]
  rows: Record<string, string>[]
}

export interface AssistantMessage {
  id: string
  role: 'user' | 'assistant'
  content: string
  messageType: string
  resultBlocks?: AssistantResultBlock[] | null
  createdAt: string
}

export interface AssistantChatResponse {
  session: AssistantSessionListItem
  userMessage: AssistantMessage
  assistantMessage: AssistantMessage
}

export interface AssistantChatPayload {
  sessionId?: number
  message: string
  routePath?: string
  routeTitle?: string
}
