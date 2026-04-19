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
  actionCard?: AssistantActionCard | null
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

export interface AssistantActionFieldPrompt {
  field: string
  label: string
  hint?: string | null
}

export interface AssistantActionFieldValue {
  field: string
  label: string
  value: string
}

export interface AssistantActionCard {
  actionCode: string
  status: string
  resourceType: string
  resourceLabel: string
  actionType: string
  actionLabel: string
  targetLabel?: string | null
  summary: string
  riskLevel: string
  confirmationMode: string
  confirmationTextHint?: string | null
  missingFields: AssistantActionFieldPrompt[]
  previewFields: AssistantActionFieldValue[]
}

export interface AssistantActionExecuteResponse {
  session: AssistantSessionListItem
  assistantMessage: AssistantMessage
}
