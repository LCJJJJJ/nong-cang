import { useEffect, useState } from 'react'

import { normalizeError, type AppError } from '../../api/errors'
import TreeDataTable, {
  type TreeTableColumn,
  type TreeTableRow,
} from '../../components/tree-data-table/TreeDataTable'
import {
  getAlertRecordList,
  ignoreAlertRecord,
  refreshAlertRecords,
} from '../../features/alertrecord/api'
import type {
  AlertRecordListItem,
  AlertRecordListQuery,
} from '../../features/alertrecord/types'
import {
  getAlertSeverityLabel,
  getAlertSourceTypeLabel,
  getAlertTypeLabel,
} from '../../utils/alert-labels'
import './AlertCenterPage.css'

type AlertRecordRow = TreeTableRow & AlertRecordListItem

function AlertCenterPage() {
  const [queryForm, setQueryForm] = useState({
    alertCode: '',
    alertType: '',
    severity: '',
    status: '',
  })
  const [alerts, setAlerts] = useState<AlertRecordRow[]>([])
  const [pageError, setPageError] = useState<AppError | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    let isMounted = true

    const bootstrap = async () => {
      try {
        await refreshAlertRecords()
        const list = await getAlertRecordList()

        if (!isMounted) {
          return
        }

        setAlerts(toTableRows(list))
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

  async function loadAlertRecordList(query: AlertRecordListQuery) {
    setIsLoading(true)
    setPageError(null)

    try {
      const list = await getAlertRecordList(query)
      setAlerts(toTableRows(list))
    } catch (error) {
      setPageError(normalizeError(error))
    } finally {
      setIsLoading(false)
    }
  }

  const columns: TreeTableColumn<AlertRecordRow>[] = [
    {
      key: 'alertCode',
      title: '预警编号',
      minWidth: 180,
      render: (row) => row.alertCode,
    },
    {
      key: 'severity',
      title: '级别',
      minWidth: 100,
      render: (row) => (
        <span className={`alert-center-page__severity is-${row.severity.toLowerCase()}`}>
          {getAlertSeverityLabel(row.severity)}
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
      key: 'source',
      title: '来源',
      minWidth: 180,
      render: (row) => `${getAlertSourceTypeLabel(row.sourceType)} / ${row.sourceCode}`,
    },
    {
      key: 'status',
      title: '状态',
      minWidth: 110,
      render: (row) => row.statusLabel,
    },
    {
      key: 'occurredAt',
      title: '触发时间',
      minWidth: 170,
      render: (row) => formatDateTime(row.occurredAt),
    },
    {
      key: 'actions',
      title: '操作',
      minWidth: 160,
      width: 160,
      sticky: 'right',
      align: 'right',
      render: (row) => (
        <div className="alert-center-page__row-actions">
          {row.status === 1 ? (
            <button type="button" onClick={() => void handleIgnore(row)}>
              忽略
            </button>
          ) : null}
        </div>
      ),
    },
  ]

  const handleSearch = async () => {
    await loadAlertRecordList(buildQueryParams(queryForm))
  }

  const handleReset = async () => {
    const resetState = {
      alertCode: '',
      alertType: '',
      severity: '',
      status: '',
    }

    setQueryForm(resetState)
    await loadAlertRecordList({})
  }

  const handleRefresh = async () => {
    try {
      const result = await refreshAlertRecords()
      await loadAlertRecordList(buildQueryParams(queryForm))
      window.alert(
        `刷新完成：新增 ${result.createdCount} 条，恢复 ${result.resolvedCount} 条，当前活跃 ${result.activeCount} 条`,
      )
    } catch (error) {
      setPageError(normalizeError(error))
    }
  }

  const handleIgnore = async (row: AlertRecordRow) => {
    try {
      await ignoreAlertRecord(row.id)
      await loadAlertRecordList(buildQueryParams(queryForm))
    } catch (error) {
      setPageError(normalizeError(error))
    }
  }

  return (
    <section className="alert-center-page">
      {pageError ? (
        <div className="alert-center-page__message alert-center-page__message--error">
          <strong>{pageError.message}</strong>
          {pageError.traceId ? <span>追踪编号：{pageError.traceId}</span> : null}
        </div>
      ) : null}

      <div className="alert-center-page__search">
        <div className="alert-center-page__search-grid">
          <label className="alert-center-page__field">
            <span>预警编号</span>
            <input
              value={queryForm.alertCode}
              onChange={(event) =>
                setQueryForm((current) => ({ ...current, alertCode: event.target.value }))
              }
            />
          </label>
          <label className="alert-center-page__field">
            <span>预警类型</span>
            <select
              value={queryForm.alertType}
              onChange={(event) =>
                setQueryForm((current) => ({ ...current, alertType: event.target.value }))
              }
            >
              <option value="">全部类型</option>
              <option value="LOW_STOCK">{getAlertTypeLabel('LOW_STOCK')}</option>
              <option value="PUTAWAY_TIMEOUT">{getAlertTypeLabel('PUTAWAY_TIMEOUT')}</option>
              <option value="OUTBOUND_PICK_TIMEOUT">{getAlertTypeLabel('OUTBOUND_PICK_TIMEOUT')}</option>
              <option value="OUTBOUND_SHIP_TIMEOUT">{getAlertTypeLabel('OUTBOUND_SHIP_TIMEOUT')}</option>
              <option value="ABNORMAL_STOCK_STAGNANT">{getAlertTypeLabel('ABNORMAL_STOCK_STAGNANT')}</option>
              <option value="STOCKTAKING_CONFIRM_TIMEOUT">{getAlertTypeLabel('STOCKTAKING_CONFIRM_TIMEOUT')}</option>
              <option value="INBOUND_PENDING_INSPECTION">{getAlertTypeLabel('INBOUND_PENDING_INSPECTION')}</option>
            </select>
          </label>
          <label className="alert-center-page__field">
            <span>级别</span>
            <select
              value={queryForm.severity}
              onChange={(event) =>
                setQueryForm((current) => ({ ...current, severity: event.target.value }))
              }
            >
              <option value="">全部级别</option>
              <option value="LOW">{getAlertSeverityLabel('LOW')}</option>
              <option value="MEDIUM">{getAlertSeverityLabel('MEDIUM')}</option>
              <option value="HIGH">{getAlertSeverityLabel('HIGH')}</option>
            </select>
          </label>
          <label className="alert-center-page__field">
            <span>状态</span>
            <select
              value={queryForm.status}
              onChange={(event) =>
                setQueryForm((current) => ({ ...current, status: event.target.value }))
              }
            >
              <option value="">全部状态</option>
              <option value="1">活跃</option>
              <option value="2">已忽略</option>
              <option value="3">已恢复</option>
            </select>
          </label>
          <div className="alert-center-page__actions">
            <button type="button" className="is-ghost" onClick={() => void handleReset()}>
              重置
            </button>
            <button type="button" className="is-dark" onClick={() => void handleSearch()}>
              查询
            </button>
          </div>
        </div>
      </div>

      <div className="alert-center-page__toolbar">
        <div className="alert-center-page__toolbar-copy">
          <h3>预警中心</h3>
          <p>聚合当前活跃、已忽略和已恢复的预警记录，并支持手动刷新与忽略处理。</p>
        </div>
        <button type="button" className="alert-center-page__primary" onClick={() => void handleRefresh()}>
          刷新预警
        </button>
      </div>

      <div className="alert-center-page__table-shell">
        {isLoading ? (
          <div className="alert-center-page__loading">正在加载预警记录...</div>
        ) : (
          <TreeDataTable data={alerts} columns={columns} emptyText="暂无预警记录" />
        )}
      </div>
    </section>
  )
}

function toTableRows(rows: AlertRecordListItem[]): AlertRecordRow[] {
  return rows.map((row) => ({ ...row }))
}

function buildQueryParams(queryForm: {
  alertCode: string
  alertType: string
  severity: string
  status: string
}): AlertRecordListQuery {
  return {
    alertCode: queryForm.alertCode.trim() || undefined,
    alertType: queryForm.alertType.trim() || undefined,
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

export default AlertCenterPage
