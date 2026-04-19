import { useEffect, useState } from 'react'

import { normalizeError, type AppError } from '../../api/errors'
import TreeDataTable, {
  type TreeTableColumn,
  type TreeTableRow,
} from '../../components/tree-data-table/TreeDataTable'
import {
  getMessageNoticeList,
  markAllMessageNoticeRead,
  markMessageNoticeRead,
} from '../../features/messagenotice/api'
import type {
  MessageNoticeListItem,
  MessageNoticeListQuery,
} from '../../features/messagenotice/types'
import './MessageNoticePage.css'

type MessageNoticeRow = TreeTableRow & MessageNoticeListItem

function MessageNoticePage() {
  const [queryForm, setQueryForm] = useState({
    noticeCode: '',
    severity: '',
    status: '',
  })
  const [notices, setNotices] = useState<MessageNoticeRow[]>([])
  const [pageError, setPageError] = useState<AppError | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    let isMounted = true

    const bootstrap = async () => {
      try {
        const list = await getMessageNoticeList()

        if (!isMounted) {
          return
        }

        setNotices(toTableRows(list))
      } catch (error) {
        if (!isMounted) {
          return
        }

        setPageError(normalizeError(error))
      } finally {
        if (isMounted) {
          setIsLoading(false)
        }
      }
    }

    void bootstrap()
    return () => {
      isMounted = false
    }
  }, [])

  async function loadMessageNoticeList(query: MessageNoticeListQuery) {
    setIsLoading(true)
    setPageError(null)

    try {
      const list = await getMessageNoticeList(query)
      setNotices(toTableRows(list))
    } catch (error) {
      setPageError(normalizeError(error))
    } finally {
      setIsLoading(false)
    }
  }

  const columns: TreeTableColumn<MessageNoticeRow>[] = [
    {
      key: 'noticeCode',
      title: '消息编号',
      minWidth: 180,
      render: (row) => row.noticeCode,
    },
    {
      key: 'severity',
      title: '级别',
      minWidth: 100,
      render: (row) => (
        <span className={`message-notice-page__severity is-${row.severity.toLowerCase()}`}>
          {row.severity}
        </span>
      ),
    },
    {
      key: 'title',
      title: '标题',
      minWidth: 220,
      render: (row) => row.title,
    },
    {
      key: 'content',
      title: '内容',
      minWidth: 300,
      render: (row) => row.content,
    },
    {
      key: 'status',
      title: '状态',
      minWidth: 110,
      render: (row) => row.statusLabel,
    },
    {
      key: 'createdAt',
      title: '创建时间',
      minWidth: 170,
      render: (row) => formatDateTime(row.createdAt),
    },
    {
      key: 'actions',
      title: '操作',
      minWidth: 160,
      width: 160,
      sticky: 'right',
      align: 'right',
      render: (row) => (
        <div className="message-notice-page__row-actions">
          {row.status === 1 ? (
            <button type="button" onClick={() => void handleMarkRead(row)}>
              已读
            </button>
          ) : null}
        </div>
      ),
    },
  ]

  const handleSearch = async () => {
    await loadMessageNoticeList(buildQueryParams(queryForm))
  }

  const handleReset = async () => {
    const resetState = {
      noticeCode: '',
      severity: '',
      status: '',
    }
    setQueryForm(resetState)
    await loadMessageNoticeList({})
  }

  const handleMarkRead = async (row: MessageNoticeRow) => {
    try {
      await markMessageNoticeRead(row.id)
      await loadMessageNoticeList(buildQueryParams(queryForm))
    } catch (error) {
      setPageError(normalizeError(error))
    }
  }

  const handleReadAll = async () => {
    try {
      await markAllMessageNoticeRead()
      await loadMessageNoticeList(buildQueryParams(queryForm))
    } catch (error) {
      setPageError(normalizeError(error))
    }
  }

  return (
    <section className="message-notice-page">
      {pageError ? (
        <div className="message-notice-page__message message-notice-page__message--error">
          <strong>{pageError.message}</strong>
          {pageError.traceId ? <span>追踪编号：{pageError.traceId}</span> : null}
        </div>
      ) : null}

      <div className="message-notice-page__search">
        <div className="message-notice-page__search-grid">
          <label className="message-notice-page__field">
            <span>消息编号</span>
            <input
              value={queryForm.noticeCode}
              onChange={(event) =>
                setQueryForm((current) => ({ ...current, noticeCode: event.target.value }))
              }
            />
          </label>
          <label className="message-notice-page__field">
            <span>级别</span>
            <select
              value={queryForm.severity}
              onChange={(event) =>
                setQueryForm((current) => ({ ...current, severity: event.target.value }))
              }
            >
              <option value="">全部级别</option>
              <option value="LOW">LOW</option>
              <option value="MEDIUM">MEDIUM</option>
              <option value="HIGH">HIGH</option>
            </select>
          </label>
          <label className="message-notice-page__field">
            <span>状态</span>
            <select
              value={queryForm.status}
              onChange={(event) =>
                setQueryForm((current) => ({ ...current, status: event.target.value }))
              }
            >
              <option value="">全部状态</option>
              <option value="1">未读</option>
              <option value="2">已读</option>
            </select>
          </label>
          <div className="message-notice-page__actions">
            <button type="button" className="is-ghost" onClick={() => void handleReset()}>
              重置
            </button>
            <button type="button" className="is-dark" onClick={() => void handleSearch()}>
              查询
            </button>
          </div>
        </div>
      </div>

      <div className="message-notice-page__toolbar">
        <div className="message-notice-page__toolbar-copy">
          <h3>消息中心</h3>
          <p>查看由预警记录生成的系统消息，并支持单条已读和全部已读。</p>
        </div>
        <button type="button" className="message-notice-page__primary" onClick={() => void handleReadAll()}>
          全部已读
        </button>
      </div>

      <div className="message-notice-page__table-shell">
        {isLoading ? (
          <div className="message-notice-page__loading">正在加载消息...</div>
        ) : (
          <TreeDataTable data={notices} columns={columns} emptyText="暂无消息通知" />
        )}
      </div>
    </section>
  )
}

function toTableRows(rows: MessageNoticeListItem[]): MessageNoticeRow[] {
  return rows.map((row) => ({ ...row }))
}

function buildQueryParams(queryForm: {
  noticeCode: string
  severity: string
  status: string
}): MessageNoticeListQuery {
  return {
    noticeCode: queryForm.noticeCode.trim() || undefined,
    severity: queryForm.severity || undefined,
    status: queryForm.status ? Number(queryForm.status) : undefined,
  }
}

function formatDateTime(value: string) {
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value))
}

export default MessageNoticePage
