import { useEffect, useMemo, useRef, useState } from 'react'
import Markdown from 'react-markdown'
import { useNavigate } from 'react-router-dom'
import remarkGfm from 'remark-gfm'

import { normalizeError, type AppError } from '../../api/errors'
import {
  chatWithAssistant,
  getAssistantMessages,
  getAssistantSessions,
} from './api'
import type {
  AssistantChatResponse,
  AssistantMessage,
  AssistantResultBlock,
  AssistantSessionListItem,
} from './types'
import './AssistantWidget.css'

interface AssistantWidgetProps {
  routePath: string
  routeTitle: string
}

function AssistantWidget({ routePath, routeTitle }: AssistantWidgetProps) {
  const navigate = useNavigate()
  const messagesRef = useRef<HTMLDivElement | null>(null)
  const [isOpen, setIsOpen] = useState(false)
  const [sessions, setSessions] = useState<AssistantSessionListItem[]>([])
  const [activeSessionId, setActiveSessionId] = useState<string | null>(null)
  const [messages, setMessages] = useState<AssistantMessage[]>([])
  const [draft, setDraft] = useState('')
  const [pageError, setPageError] = useState<AppError | null>(null)
  const [isLoadingSessions, setIsLoadingSessions] = useState(false)
  const [isLoadingMessages, setIsLoadingMessages] = useState(false)
  const [isSending, setIsSending] = useState(false)

  useEffect(() => {
    if (!isOpen) {
      return
    }

    let isMounted = true

    const bootstrap = async () => {
      setIsLoadingSessions(true)
      setPageError(null)

      try {
        const sessionList = await getAssistantSessions()

        if (!isMounted) {
          return
        }

        setSessions(sessionList)

        if (sessionList.length > 0) {
          setActiveSessionId((current) => current ?? sessionList[0].id)
        }
      } catch (error) {
        if (isMounted) {
          setPageError(normalizeError(error))
        }
      } finally {
        if (isMounted) {
          setIsLoadingSessions(false)
        }
      }
    }

    void bootstrap()
    return () => {
      isMounted = false
    }
  }, [isOpen])

  useEffect(() => {
    if (!isOpen || !activeSessionId) {
      return
    }

    let isMounted = true

    const loadMessages = async () => {
      setIsLoadingMessages(true)
      setPageError(null)

      try {
        const sessionMessages = await getAssistantMessages(activeSessionId)

        if (!isMounted) {
          return
        }

        setMessages(sessionMessages)
      } catch (error) {
        if (isMounted) {
          setPageError(normalizeError(error))
        }
      } finally {
        if (isMounted) {
          setIsLoadingMessages(false)
        }
      }
    }

    void loadMessages()
    return () => {
      isMounted = false
    }
  }, [activeSessionId, isOpen])

  useEffect(() => {
    if (!isOpen) {
      return
    }

    messagesRef.current?.scrollTo({
      top: messagesRef.current.scrollHeight,
      behavior: 'smooth',
    })
  }, [isOpen, messages])

  const suggestionItems = useMemo(
    () => buildSuggestions(routePath, routeTitle),
    [routePath, routeTitle],
  )

  const handleSend = async () => {
    const content = draft.trim()

    if (!content || isSending) {
      return
    }

    setIsSending(true)
    setPageError(null)

    try {
      const response = await chatWithAssistant({
        sessionId: activeSessionId ? Number(activeSessionId) : undefined,
        message: content,
        routePath,
        routeTitle,
      })

      setDraft('')
      setActiveSessionId(response.session.id)
      setMessages((current) =>
        activeSessionId === response.session.id
          ? [...current, response.userMessage, response.assistantMessage]
          : [response.userMessage, response.assistantMessage],
      )
      setSessions((current) => upsertSession(current, response))
    } catch (error) {
      setPageError(normalizeError(error))
    } finally {
      setIsSending(false)
    }
  }

  const activeSession = sessions.find((item) => item.id === activeSessionId) ?? null

  return (
    <>
      <button
        type="button"
        className="assistant-widget__fab"
        aria-label="打开智能助手"
        onClick={() => setIsOpen((current) => !current)}
      >
        <span className="assistant-widget__fab-icon" aria-hidden="true">
          <svg viewBox="0 0 24 24" focusable="false">
            <path d="M12 2a8 8 0 0 0-8 8v4.38l-1.55 2.33A1 1 0 0 0 3.28 18H9v2a2 2 0 0 0 2 2h2a2 2 0 0 0 2-2v-2h5.72a1 1 0 0 0 .83-1.29L20 14.38V10a8 8 0 0 0-8-8Zm-3 8a1.5 1.5 0 1 1 1.5 1.5A1.5 1.5 0 0 1 9 10Zm4.5 0A1.5 1.5 0 1 1 15 11.5 1.5 1.5 0 0 1 13.5 10ZM8 7h8v1.5H8Z" />
          </svg>
        </span>
        <span className="assistant-widget__fab-text">智能助手</span>
      </button>

      {isOpen ? (
        <div className="assistant-widget__panel" role="dialog" aria-modal="false">
          <header className="assistant-widget__header">
            <div>
              <strong>智能助手</strong>
              <span>
                当前上下文：{routeTitle} · {routePath}
              </span>
            </div>
            <div className="assistant-widget__header-actions">
              <button
                type="button"
                className="assistant-widget__ghost-button"
                onClick={() => {
                  setActiveSessionId(null)
                  setMessages([])
                  setPageError(null)
                }}
              >
                新对话
              </button>
              <button
                type="button"
                className="assistant-widget__icon-button"
                aria-label="关闭智能助手"
                onClick={() => setIsOpen(false)}
              >
                ×
              </button>
            </div>
          </header>

          <div className="assistant-widget__body">
            <aside className="assistant-widget__sessions">
              <div className="assistant-widget__sessions-title">最近会话</div>
              {isLoadingSessions ? (
                <div className="assistant-widget__placeholder">正在加载会话...</div>
              ) : sessions.length > 0 ? (
                sessions.map((session) => (
                  <button
                    key={session.id}
                    type="button"
                    className={`assistant-widget__session-item${
                      session.id === activeSessionId ? ' is-active' : ''
                    }`}
                    onClick={() => setActiveSessionId(session.id)}
                  >
                    <strong>{session.title}</strong>
                    <span>{resolveSessionOperationSummary(session)}</span>
                    <em>{formatDateTime(session.updatedAt)}</em>
                  </button>
                ))
              ) : (
                <div className="assistant-widget__placeholder">暂无历史会话</div>
              )}
            </aside>

            <section className="assistant-widget__conversation">
              <div className="assistant-widget__conversation-head">
                <div>
                  <strong>{activeSession?.title ?? '新对话'}</strong>
                  <span>{activeSession?.routeTitle ?? routeTitle}</span>
                </div>
              </div>

              {pageError ? (
                <div className="assistant-widget__error">
                  <strong>{pageError.message}</strong>
                  {pageError.traceId ? <span>追踪编号：{pageError.traceId}</span> : null}
                </div>
              ) : null}

              <div className="assistant-widget__messages" ref={messagesRef}>
                {isLoadingMessages ? (
                  <div className="assistant-widget__placeholder">正在加载消息...</div>
                ) : messages.length > 0 ? (
                  messages.map((message) => (
                    <article
                      key={message.id}
                      className={`assistant-widget__message assistant-widget__message--${message.role}`}
                    >
                      <div className="assistant-widget__message-bubble">
                        <div className="assistant-widget__message-meta">
                          <strong>{message.role === 'assistant' ? '助手' : '你'}</strong>
                          <span>{formatDateTime(message.createdAt)}</span>
                        </div>
                        {message.role === 'assistant' ? (
                          <div className="assistant-widget__markdown">
                            <Markdown
                              remarkPlugins={[remarkGfm]}
                              components={{
                                a: (props) => (
                                  <a {...props} target="_blank" rel="noreferrer" />
                                ),
                              }}
                            >
                              {message.content}
                            </Markdown>
                          </div>
                        ) : (
                          <p>{message.content}</p>
                        )}
                        {message.resultBlocks?.map((block, index) => (
                          <AssistantResultCard
                            key={`${message.id}-${index}`}
                            block={block}
                            onNavigate={(path) => {
                              navigate(path)
                              setIsOpen(false)
                            }}
                          />
                        ))}
                      </div>
                    </article>
                  ))
                ) : (
                  <div className="assistant-widget__empty">
                    <strong>可以直接问系统数据</strong>
                    <p>例如：查内酯豆腐库存、有哪些仓库、查某仓库的库区库位。</p>
                    <div className="assistant-widget__suggestions">
                      {suggestionItems.map((item) => (
                        <button
                          key={item}
                          type="button"
                          onClick={() => setDraft(item)}
                        >
                          {item}
                        </button>
                      ))}
                    </div>
                  </div>
                )}
              </div>

              <div className="assistant-widget__composer">
                <textarea
                  value={draft}
                  rows={4}
                  placeholder="请输入自然语言问题，例如：查内酯豆腐当前库存"
                  onChange={(event) => setDraft(event.target.value)}
                  onKeyDown={(event) => {
                    if (event.key === 'Enter' && (event.metaKey || event.ctrlKey)) {
                      event.preventDefault()
                      void handleSend()
                    }
                  }}
                />
                <div className="assistant-widget__composer-actions">
                  <span>Ctrl/⌘ + Enter 发送</span>
                  <button type="button" disabled={isSending} onClick={() => void handleSend()}>
                    {isSending ? '发送中...' : '发送'}
                  </button>
                </div>
              </div>
            </section>
          </div>
        </div>
      ) : null}
    </>
  )
}

function AssistantResultCard({
  block,
  onNavigate,
}: {
  block: AssistantResultBlock
  onNavigate: (path: string) => void
}) {
  return (
    <section className="assistant-widget__result">
      <div className="assistant-widget__result-head">
        <div>
          <strong>{block.title}</strong>
          <span>{block.summary}</span>
        </div>
        {block.routePath ? (
          <button type="button" onClick={() => onNavigate(block.routePath!)}>
            打开页面
          </button>
        ) : null}
      </div>

      {block.columns.length > 0 && block.rows.length > 0 ? (
        <div className="assistant-widget__result-table-shell">
          <table className="assistant-widget__result-table">
            <thead>
              <tr>
                {block.columns.map((column) => (
                  <th key={column.key}>{column.label}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {block.rows.map((row, rowIndex) => (
                <tr key={`${block.title}-${rowIndex}`}>
                  {block.columns.map((column) => (
                    <td key={column.key}>{row[column.key] ?? '-'}</td>
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : null}
    </section>
  )
}

function upsertSession(
  currentSessions: AssistantSessionListItem[],
  response: AssistantChatResponse,
) {
  const nextItem = {
    ...response.session,
    lastMessagePreview: response.assistantMessage.content,
  }

  return [nextItem, ...currentSessions.filter((item) => item.id !== response.session.id)]
}

function buildSuggestions(routePath: string, routeTitle: string) {
  if (routePath === '/inventory-stocks') {
    return ['查当前页商品库存', '为什么这个商品可用库存和现存数量不一致']
  }

  if (routePath === '/outbound-records') {
    return ['查今天的出库记录', '查内酯豆腐的出库记录']
  }

  if (routePath === '/warehouses') {
    return ['有哪些仓库', '查一号综合仓的库区库位']
  }

  return [
    `查 ${routeTitle} 相关数据`,
    '查内酯豆腐当前库存',
    '有哪些仓库',
  ]
}

function formatDateTime(value?: string | null) {
  if (!value) {
    return '--'
  }

  const date = new Date(value)

  if (Number.isNaN(date.getTime())) {
    return value
  }

  return date.toLocaleString('zh-CN', {
    hour12: false,
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

function resolveSessionOperationSummary(session: AssistantSessionListItem) {
  const preview = session.lastMessagePreview?.trim() ?? ''

  if (preview.includes('预警刷新')) {
    return '刷新了系统的预警记录'
  }

  const subject = resolveRouteSubject(session.routePath, session.routeTitle)
  return `查询了系统的${subject}`
}

function resolveRouteSubject(routePath?: string | null, routeTitle?: string | null) {
  const subjectMap: Record<string, string> = {
    '/': '产品分类记录',
    '/product-archives': '产品档案记录',
    '/product-units': '产品单位记录',
    '/product-origins': '产地信息记录',
    '/storage-conditions': '储存条件记录',
    '/quality-grades': '品质等级记录',
    '/warehouses': '仓库记录',
    '/warehouse-zones': '库区记录',
    '/warehouse-locations': '库位记录',
    '/suppliers': '供应商记录',
    '/customers': '客户记录',
    '/inbound-orders': '入库单记录',
    '/putaway-tasks': '上架任务记录',
    '/inbound-records': '入库记录',
    '/outbound-orders': '出库单记录',
    '/outbound-tasks': '拣货出库任务记录',
    '/outbound-records': '出库记录',
    '/inventory-stocks': '实时库存记录',
    '/inventory-transactions': '库存流水记录',
    '/inventory-adjustments': '库存调整记录',
    '/inventory-stocktakings': '库存盘点记录',
    '/quality-inspections': '质检单记录',
    '/abnormal-stocks': '异常库存记录',
    '/loss-records': '损耗记录',
    '/alert-rules': '预警规则记录',
    '/alerts': '预警记录',
    '/message-notices': '消息通知记录',
  }

  if (routePath && subjectMap[routePath]) {
    return subjectMap[routePath]
  }

  if (routeTitle) {
    return routeTitle.replace(/管理|查询/g, '') + '记录'
  }

  return '业务记录'
}

export default AssistantWidget
