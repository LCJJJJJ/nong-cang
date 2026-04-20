import { type FormEvent, useEffect, useState } from 'react'

import { getFieldError, normalizeError, type AppError } from '../../api/errors'
import TreeDataTable, {
  type TreeTableColumn,
  type TreeTableRow,
} from '../../components/tree-data-table/TreeDataTable'
import {
  getAlertRuleDetail,
  getAlertRuleList,
  refreshAlerts,
  updateAlertRule,
  updateAlertRuleStatus,
} from '../../features/alertrule/api'
import type {
  AlertRuleDetail,
  AlertRuleListItem,
  AlertRuleListQuery,
  AlertRuleUpdatePayload,
} from '../../features/alertrule/types'
import {
  getAlertSeverityLabel,
  getAlertThresholdUnitLabel,
  getAlertTypeLabel,
} from '../../utils/alert-labels'
import './AlertRulePage.css'

type AlertRuleRow = TreeTableRow & AlertRuleListItem

const timeoutAlertTypes = new Set([
  'PUTAWAY_TIMEOUT',
  'OUTBOUND_PICK_TIMEOUT',
  'OUTBOUND_SHIP_TIMEOUT',
  'ABNORMAL_STOCK_STAGNANT',
  'STOCKTAKING_CONFIRM_TIMEOUT',
  'INBOUND_PENDING_INSPECTION',
])

interface AlertRuleFormState {
  severity: string
  thresholdValue: string
  thresholdUnit: string
  description: string
  sortOrder: string
}

const initialFormState: AlertRuleFormState = {
  severity: 'MEDIUM',
  thresholdValue: '',
  thresholdUnit: 'HOUR',
  description: '',
  sortOrder: '0',
}

function AlertRulePage() {
  const [queryForm, setQueryForm] = useState({
    ruleCode: '',
    ruleName: '',
    enabled: '',
  })
  const [rules, setRules] = useState<AlertRuleRow[]>([])
  const [pageError, setPageError] = useState<AppError | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  const [isDialogOpen, setIsDialogOpen] = useState(false)
  const [editingId, setEditingId] = useState<string | null>(null)
  const [formState, setFormState] = useState<AlertRuleFormState>(initialFormState)
  const [formError, setFormError] = useState<AppError | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)

  useEffect(() => {
    let isMounted = true

    const bootstrap = async () => {
      try {
        const list = await getAlertRuleList()

        if (!isMounted) {
          return
        }

        setRules(toTableRows(list))
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

  async function loadAlertRuleList(query: AlertRuleListQuery) {
    setIsLoading(true)
    setPageError(null)

    try {
      const list = await getAlertRuleList(query)
      setRules(toTableRows(list))
    } catch (error) {
      setPageError(normalizeError(error))
    } finally {
      setIsLoading(false)
    }
  }

  const columns: TreeTableColumn<AlertRuleRow>[] = [
    {
      key: 'ruleName',
      title: '规则名称',
      minWidth: 180,
      render: (row) => row.ruleName,
    },
    {
      key: 'ruleCode',
      title: '规则编号',
      minWidth: 180,
      render: (row) => row.ruleCode,
    },
    {
      key: 'alertType',
      title: '预警类型',
      minWidth: 180,
      render: (row) => getAlertTypeLabel(row.alertType),
    },
    {
      key: 'severity',
      title: '严重级别',
      minWidth: 120,
      render: (row) => getAlertSeverityLabel(row.severity),
    },
    {
      key: 'thresholdValue',
      title: '阈值',
      minWidth: 140,
      render: (row) => `${row.thresholdValue} ${getAlertThresholdUnitLabel(row.thresholdUnit)}`,
    },
    {
      key: 'enabled',
      title: '状态',
      minWidth: 120,
      render: (row) => (
        <span
          className={`alert-rule-page__status${row.enabled === 1 ? ' is-enabled' : ' is-disabled'}`}
        >
          {row.enabledLabel}
        </span>
      ),
    },
    {
      key: 'description',
      title: '规则说明',
      minWidth: 220,
      render: (row) => row.description || '-',
    },
    {
      key: 'actions',
      title: '操作',
      minWidth: 220,
      width: 220,
      sticky: 'right',
      align: 'right',
      render: (row) => (
        <div className="alert-rule-page__row-actions">
          <button type="button" onClick={() => handleEdit(row.id)}>
            编辑
          </button>
          <button type="button" onClick={() => void handleToggleStatus(row)}>
            {row.enabled === 1 ? '停用' : '启用'}
          </button>
        </div>
      ),
    },
  ]

  const handleSearch = async () => {
    await loadAlertRuleList(buildQueryParams(queryForm))
  }

  const handleReset = async () => {
    const resetState = {
      ruleCode: '',
      ruleName: '',
      enabled: '',
    }
    setQueryForm(resetState)
    await loadAlertRuleList({})
  }

  const handleEdit = async (ruleId: string) => {
    setEditingId(ruleId)
    setIsDialogOpen(true)
    setIsSubmitting(true)
    setFormError(null)

    try {
      const detail = await getAlertRuleDetail(ruleId)
      setFormState(mapDetailToFormState(detail))
    } catch (error) {
      setFormError(normalizeError(error))
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleToggleStatus = async (row: AlertRuleRow) => {
    try {
      await updateAlertRuleStatus(row.id, row.enabled === 1 ? 0 : 1)
      await loadAlertRuleList(buildQueryParams(queryForm))
    } catch (error) {
      setPageError(normalizeError(error))
    }
  }

  const handleRefresh = async () => {
    try {
      const result = await refreshAlerts()
      await loadAlertRuleList(buildQueryParams(queryForm))
      window.alert(
        `刷新完成：新增 ${result.createdCount} 条，恢复 ${result.resolvedCount} 条，当前活跃 ${result.activeCount} 条`,
      )
    } catch (error) {
      setPageError(normalizeError(error))
    }
  }

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    if (!editingId) {
      return
    }

    setIsSubmitting(true)
    setFormError(null)

    try {
      await updateAlertRule(editingId, mapFormStateToPayload(formState))
      setIsDialogOpen(false)
      setEditingId(null)
      await loadAlertRuleList(buildQueryParams(queryForm))
    } catch (error) {
      setFormError(normalizeError(error))
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <section className="alert-rule-page">
      {pageError ? (
        <div className="alert-rule-page__message alert-rule-page__message--error">
          <strong>{pageError.message}</strong>
          {pageError.traceId ? <span>追踪编号：{pageError.traceId}</span> : null}
        </div>
      ) : null}

      <div className="alert-rule-page__search">
        <div className="alert-rule-page__search-grid">
          <label className="alert-rule-page__field">
            <span>规则编号</span>
            <input
              value={queryForm.ruleCode}
              onChange={(event) =>
                setQueryForm((current) => ({
                  ...current,
                  ruleCode: event.target.value,
                }))
              }
            />
          </label>

          <label className="alert-rule-page__field">
            <span>规则名称</span>
            <input
              value={queryForm.ruleName}
              onChange={(event) =>
                setQueryForm((current) => ({
                  ...current,
                  ruleName: event.target.value,
                }))
              }
            />
          </label>

          <label className="alert-rule-page__field">
            <span>状态</span>
            <select
              value={queryForm.enabled}
              onChange={(event) =>
                setQueryForm((current) => ({
                  ...current,
                  enabled: event.target.value,
                }))
              }
            >
              <option value="">全部状态</option>
              <option value="1">启用</option>
              <option value="0">停用</option>
            </select>
          </label>

          <div className="alert-rule-page__actions">
            <button type="button" className="is-ghost" onClick={() => void handleReset()}>
              重置
            </button>
            <button type="button" className="is-dark" onClick={() => void handleSearch()}>
              查询
            </button>
          </div>
        </div>
      </div>

      <div className="alert-rule-page__toolbar">
        <div className="alert-rule-page__toolbar-copy">
          <h3>预警规则列表</h3>
          <p>维护系统内置预警阈值和严重级别，并可手动刷新预警实例。</p>
        </div>
        <button type="button" className="alert-rule-page__primary" onClick={() => void handleRefresh()}>
          立即刷新预警
        </button>
      </div>

      <div className="alert-rule-page__table-shell">
        {isLoading ? (
          <div className="alert-rule-page__loading">正在加载预警规则...</div>
        ) : (
          <TreeDataTable data={rules} columns={columns} emptyText="暂无预警规则" />
        )}
      </div>

      {isDialogOpen ? (
        <div className="alert-rule-page__dialog-backdrop">
          <div className="alert-rule-page__dialog">
            <div className="alert-rule-page__dialog-header">
              <div>
                <h3>编辑预警规则</h3>
                <p>调整阈值、严重级别和规则说明，系统会在刷新时按新规则生成预警。</p>
              </div>
              <button
                type="button"
                className="alert-rule-page__dialog-close"
                onClick={() => setIsDialogOpen(false)}
              >
                ×
              </button>
            </div>

            <form className="alert-rule-page__dialog-form" onSubmit={handleSubmit}>
              {formError ? (
                <div className="alert-rule-page__message alert-rule-page__message--error alert-rule-page__field--full">
                  <strong>{formError.message}</strong>
                </div>
              ) : null}

              <label className="alert-rule-page__field">
                <span>严重级别</span>
                <select
                  value={formState.severity}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      severity: event.target.value,
                    }))
                  }
                >
                  <option value="LOW">{getAlertSeverityLabel('LOW')}</option>
                  <option value="MEDIUM">{getAlertSeverityLabel('MEDIUM')}</option>
                  <option value="HIGH">{getAlertSeverityLabel('HIGH')}</option>
                </select>
                <small>{getFieldError(formError?.fieldErrors, 'severity')}</small>
              </label>

              <label className="alert-rule-page__field">
                <span>阈值</span>
                <input
                  type="number"
                  min="0"
                  step="0.001"
                  value={formState.thresholdValue}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      thresholdValue: event.target.value,
                    }))
                  }
                />
                <small>{getFieldError(formError?.fieldErrors, 'thresholdValue')}</small>
              </label>

              <label className="alert-rule-page__field">
                <span>单位</span>
                {editingId &&
                timeoutAlertTypes.has(rules.find((item) => item.id === editingId)?.alertType ?? '') ? (
                  <>
                    <select
                      value={formState.thresholdUnit}
                      onChange={(event) =>
                        setFormState((current) => ({
                          ...current,
                          thresholdUnit: event.target.value,
                        }))
                      }
                    >
                      <option value="MINUTE">{getAlertThresholdUnitLabel('MINUTE')}</option>
                      <option value="HOUR">{getAlertThresholdUnitLabel('HOUR')}</option>
                    </select>
                    <small>切换单位不会自动换算阈值，请同步确认阈值数值。</small>
                  </>
                ) : (
                  <>
                    <input
                      value={getAlertThresholdUnitLabel(formState.thresholdUnit)}
                      readOnly
                      disabled
                    />
                    <small>当前规则单位固定，不可修改。</small>
                  </>
                )}
              </label>

              <label className="alert-rule-page__field">
                <span>排序值</span>
                <input
                  type="number"
                  min="0"
                  value={formState.sortOrder}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      sortOrder: event.target.value,
                    }))
                  }
                />
              </label>

              <label className="alert-rule-page__field alert-rule-page__field--full">
                <span>规则说明</span>
                <textarea
                  value={formState.description}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      description: event.target.value,
                    }))
                  }
                />
              </label>

              <div className="alert-rule-page__dialog-actions">
                <button type="button" className="is-ghost" onClick={() => setIsDialogOpen(false)}>
                  取消
                </button>
                <button type="submit" className="is-dark" disabled={isSubmitting}>
                  {isSubmitting ? '提交中...' : '保存规则'}
                </button>
              </div>
            </form>
          </div>
        </div>
      ) : null}
    </section>
  )
}

function toTableRows(rows: AlertRuleListItem[]): AlertRuleRow[] {
  return rows.map((row) => ({
    ...row,
  }))
}

function buildQueryParams(queryForm: {
  ruleCode: string
  ruleName: string
  enabled: string
}): AlertRuleListQuery {
  return {
    ruleCode: queryForm.ruleCode.trim() || undefined,
    ruleName: queryForm.ruleName.trim() || undefined,
    enabled: queryForm.enabled ? Number(queryForm.enabled) : undefined,
  }
}

function mapDetailToFormState(detail: AlertRuleDetail) {
  return {
    severity: detail.severity,
    thresholdValue: String(detail.thresholdValue),
    thresholdUnit: detail.thresholdUnit,
    description: detail.description ?? '',
    sortOrder: String(detail.sortOrder),
  }
}

function mapFormStateToPayload(formState: AlertRuleFormState): AlertRuleUpdatePayload {
  return {
    severity: formState.severity,
    thresholdValue: Number(formState.thresholdValue),
    thresholdUnit: formState.thresholdUnit,
    description: formState.description.trim() || null,
    sortOrder: Number(formState.sortOrder || '0'),
  }
}

export default AlertRulePage
