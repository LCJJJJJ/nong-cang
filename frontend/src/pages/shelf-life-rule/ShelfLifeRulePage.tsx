import { type FormEvent, useEffect, useMemo, useState } from 'react'

import { getFieldError, normalizeError, type AppError } from '../../api/errors'
import TreeDataTable, {
  type TreeTableColumn,
  type TreeTableRow,
} from '../../components/tree-data-table/TreeDataTable'
import { getCategoryOptions } from '../../features/category/api'
import type { CategoryOption } from '../../features/category/types'
import {
  createShelfLifeRule,
  deleteShelfLifeRule,
  getShelfLifeRuleDetail,
  getShelfLifeRuleList,
  updateShelfLifeRule,
  updateShelfLifeRuleStatus,
} from '../../features/shelfliferule/api'
import type {
  ShelfLifeRuleDetail,
  ShelfLifeRuleFormPayload,
  ShelfLifeRuleListItem,
  ShelfLifeRuleListQuery,
} from '../../features/shelfliferule/types'
import { getStorageConditionOptions } from '../../features/storagecondition/api'
import type { StorageConditionOption } from '../../features/storagecondition/types'
import './ShelfLifeRulePage.css'

type ShelfLifeRuleRow = TreeTableRow & ShelfLifeRuleListItem

interface ShelfLifeRuleFormState {
  ruleCode: string
  ruleName: string
  categoryId: string
  storageConditionId: string
  shelfLifeDays: string
  warningDays: string
  status: string
  sortOrder: string
  remarks: string
}

const initialFormState: ShelfLifeRuleFormState = {
  ruleCode: '',
  ruleName: '',
  categoryId: '',
  storageConditionId: '',
  shelfLifeDays: '',
  warningDays: '0',
  status: '1',
  sortOrder: '0',
  remarks: '',
}

function ShelfLifeRulePage() {
  const [queryForm, setQueryForm] = useState({
    ruleCode: '',
    ruleName: '',
    categoryId: '',
    status: '',
  })
  const [shelfLifeRules, setShelfLifeRules] = useState<ShelfLifeRuleRow[]>([])
  const [categoryOptions, setCategoryOptions] = useState<CategoryOption[]>([])
  const [storageConditionOptions, setStorageConditionOptions] = useState<
    StorageConditionOption[]
  >([])
  const [pageError, setPageError] = useState<AppError | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  const [isDialogOpen, setIsDialogOpen] = useState(false)
  const [dialogMode, setDialogMode] = useState<'create' | 'edit'>('create')
  const [editingId, setEditingId] = useState<string | null>(null)
  const [formState, setFormState] = useState<ShelfLifeRuleFormState>(initialFormState)
  const [formError, setFormError] = useState<AppError | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)

  const flattenedCategoryOptions = useMemo(
    () => flattenCategoryOptions(categoryOptions),
    [categoryOptions],
  )

  useEffect(() => {
    let isMounted = true

    const bootstrap = async () => {
      try {
        const [list, categories, storageConditions] = await Promise.all([
          getShelfLifeRuleList(),
          getCategoryOptions(),
          getStorageConditionOptions(),
        ])

        if (!isMounted) {
          return
        }

        setShelfLifeRules(toTableRows(list))
        setCategoryOptions(categories)
        setStorageConditionOptions(storageConditions)
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

  async function loadShelfLifeRuleList(query: ShelfLifeRuleListQuery) {
    setIsLoading(true)
    setPageError(null)

    try {
      const list = await getShelfLifeRuleList(query)
      setShelfLifeRules(toTableRows(list))
    } catch (error) {
      setPageError(normalizeError(error))
    } finally {
      setIsLoading(false)
    }
  }

  const columns: TreeTableColumn<ShelfLifeRuleRow>[] = [
    {
      key: 'name',
      title: '规则名称',
      minWidth: 260,
      render: (row) => (
        <div className="shelf-life-rule-page__name-cell">
          <strong>{row.ruleName}</strong>
          <span>
            {row.categoryName ?? '全部分类'} / {row.storageConditionName ?? '全部储存条件'}
          </span>
        </div>
      ),
    },
    {
      key: 'code',
      title: '规则编号',
      minWidth: 180,
      render: (row) => row.ruleCode,
    },
    {
      key: 'shelfLife',
      title: '保质期',
      minWidth: 110,
      render: (row) => `${row.shelfLifeDays} 天`,
    },
    {
      key: 'warning',
      title: '预警提前',
      minWidth: 120,
      render: (row) => `${row.warningDays} 天`,
    },
    {
      key: 'status',
      title: '状态',
      minWidth: 120,
      render: (row) => (
        <span
          className={`shelf-life-rule-page__status${
            row.status === 1 ? ' is-enabled' : ' is-disabled'
          }`}
        >
          {row.statusLabel}
        </span>
      ),
    },
    {
      key: 'updatedAt',
      title: '更新时间',
      minWidth: 170,
      render: (row) => formatDateTime(row.updatedAt),
    },
    {
      key: 'actions',
      title: '操作',
      minWidth: 220,
      width: 220,
      sticky: 'right',
      align: 'right',
      render: (row) => (
        <div className="shelf-life-rule-page__row-actions">
          <button type="button" onClick={() => handleEdit(row.id)}>
            编辑
          </button>
          <button type="button" onClick={() => handleToggleStatus(row)}>
            {row.status === 1 ? '停用' : '启用'}
          </button>
          <button type="button" onClick={() => handleDelete(row)}>
            删除
          </button>
        </div>
      ),
    },
  ]

  const handleSearch = async () => {
    await loadShelfLifeRuleList(buildQueryParams(queryForm))
  }

  const handleReset = async () => {
    const resetState = {
      ruleCode: '',
      ruleName: '',
      categoryId: '',
      status: '',
    }

    setQueryForm(resetState)
    await loadShelfLifeRuleList({})
  }

  const handleCreate = () => {
    setDialogMode('create')
    setEditingId(null)
    setFormError(null)
    setFormState(initialFormState)
    setIsDialogOpen(true)
  }

  const handleEdit = async (shelfLifeRuleId: string) => {
    setDialogMode('edit')
    setEditingId(shelfLifeRuleId)
    setFormError(null)
    setIsDialogOpen(true)
    setIsSubmitting(true)

    try {
      const detail = await getShelfLifeRuleDetail(shelfLifeRuleId)
      setFormState(mapDetailToFormState(detail))
    } catch (error) {
      setFormError(normalizeError(error))
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleToggleStatus = async (row: ShelfLifeRuleRow) => {
    const nextStatus = row.status === 1 ? 0 : 1
    const confirmed = window.confirm(
      `确定要将“${row.ruleName}”${nextStatus === 1 ? '启用' : '停用'}吗？`,
    )

    if (!confirmed) {
      return
    }

    try {
      await updateShelfLifeRuleStatus(row.id, nextStatus)
      await loadShelfLifeRuleList(buildQueryParams(queryForm))
    } catch (error) {
      setPageError(normalizeError(error))
    }
  }

  const handleDelete = async (row: ShelfLifeRuleRow) => {
    const confirmed = window.confirm(`确定要删除保质期规则“${row.ruleName}”吗？`)

    if (!confirmed) {
      return
    }

    try {
      await deleteShelfLifeRule(row.id)
      await loadShelfLifeRuleList(buildQueryParams(queryForm))
    } catch (error) {
      setPageError(normalizeError(error))
    }
  }

  const handleDialogSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setFormError(null)
    setIsSubmitting(true)

    try {
      const payload = mapFormStateToPayload(formState)

      if (dialogMode === 'create') {
        await createShelfLifeRule(payload)
      } else if (editingId) {
        await updateShelfLifeRule(editingId, payload)
      }

      setIsDialogOpen(false)
      setFormState(initialFormState)
      await loadShelfLifeRuleList(buildQueryParams(queryForm))
    } catch (error) {
      setFormError(normalizeError(error))
    } finally {
      setIsSubmitting(false)
    }
  }

  const nameFieldError = getFieldError(formError?.fieldErrors, 'ruleName')

  return (
    <div className="shelf-life-rule-page">
      {pageError ? (
        <div className="shelf-life-rule-page__message shelf-life-rule-page__message--error">
          <strong>{pageError.message}</strong>
          {pageError.traceId ? <span>traceId: {pageError.traceId}</span> : null}
        </div>
      ) : null}

      <section className="shelf-life-rule-page__search">
        <div className="shelf-life-rule-page__search-grid">
          <label className="shelf-life-rule-page__field">
            <span>规则编号</span>
            <input
              type="text"
              placeholder="输入编号..."
              value={queryForm.ruleCode}
              onChange={(event) =>
                setQueryForm((current) => ({
                  ...current,
                  ruleCode: event.target.value,
                }))
              }
            />
          </label>

          <label className="shelf-life-rule-page__field">
            <span>规则名称</span>
            <input
              type="text"
              placeholder="输入名称..."
              value={queryForm.ruleName}
              onChange={(event) =>
                setQueryForm((current) => ({
                  ...current,
                  ruleName: event.target.value,
                }))
              }
            />
          </label>

          <label className="shelf-life-rule-page__field">
            <span>适用分类</span>
            <select
              value={queryForm.categoryId}
              onChange={(event) =>
                setQueryForm((current) => ({
                  ...current,
                  categoryId: event.target.value,
                }))
              }
            >
              <option value="">全部分类</option>
              {flattenedCategoryOptions.map((option) => (
                <option key={option.id} value={option.id}>
                  {option.label}
                </option>
              ))}
            </select>
          </label>

          <label className="shelf-life-rule-page__field">
            <span>状态</span>
            <select
              value={queryForm.status}
              onChange={(event) =>
                setQueryForm((current) => ({
                  ...current,
                  status: event.target.value,
                }))
              }
            >
              <option value="">全部状态</option>
              <option value="1">启用</option>
              <option value="0">停用</option>
            </select>
          </label>

          <div className="shelf-life-rule-page__actions">
            <button type="button" className="is-ghost" onClick={() => void handleReset()}>
              重置
            </button>
            <button type="button" className="is-dark" onClick={() => void handleSearch()}>
              查询
            </button>
          </div>
        </div>
      </section>

      <section className="shelf-life-rule-page__toolbar">
        <div className="shelf-life-rule-page__toolbar-copy">
          <h3>保质期规则列表</h3>
          <p>维护分类与储存条件组合下的保质期和预警规则，为产品档案提供统一默认值。</p>
        </div>

        <button type="button" className="shelf-life-rule-page__primary" onClick={handleCreate}>
          新增保质期规则
        </button>
      </section>

      <section className="shelf-life-rule-page__table-shell">
        {isLoading ? (
          <div className="shelf-life-rule-page__loading">正在加载保质期规则...</div>
        ) : (
          <TreeDataTable
            data={shelfLifeRules}
            columns={columns}
            emptyText="暂无保质期规则数据"
          />
        )}
      </section>

      {isDialogOpen ? (
        <div className="shelf-life-rule-page__dialog-backdrop">
          <div className="shelf-life-rule-page__dialog">
            <div className="shelf-life-rule-page__dialog-header">
              <div>
                <h3>{dialogMode === 'create' ? '新增保质期规则' : '编辑保质期规则'}</h3>
                <p>维护适用分类、储存条件、保质期和预警天数。</p>
              </div>
              <button
                type="button"
                className="shelf-life-rule-page__dialog-close"
                onClick={() => setIsDialogOpen(false)}
              >
                ×
              </button>
            </div>

            <form className="shelf-life-rule-page__dialog-form" onSubmit={handleDialogSubmit}>
              <label className="shelf-life-rule-page__field">
                <span>规则编号</span>
                <input value={formState.ruleCode} placeholder="系统自动生成" readOnly disabled />
                <small className="shelf-life-rule-page__field-hint">
                  {dialogMode === 'create' ? '新增时由系统自动生成编号' : '规则编号创建后不可修改'}
                </small>
              </label>

              <label className="shelf-life-rule-page__field">
                <span>规则名称</span>
                <input
                  value={formState.ruleName}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      ruleName: event.target.value,
                    }))
                  }
                />
                {nameFieldError ? (
                  <small className="shelf-life-rule-page__field-error">{nameFieldError}</small>
                ) : null}
              </label>

              <label className="shelf-life-rule-page__field">
                <span>适用分类</span>
                <select
                  value={formState.categoryId}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      categoryId: event.target.value,
                    }))
                  }
                >
                  <option value="">请选择</option>
                  {flattenedCategoryOptions.map((option) => (
                    <option key={option.id} value={option.id}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </label>

              <label className="shelf-life-rule-page__field">
                <span>储存条件</span>
                <select
                  value={formState.storageConditionId}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      storageConditionId: event.target.value,
                    }))
                  }
                >
                  <option value="">请选择</option>
                  {storageConditionOptions.map((option) => (
                    <option key={option.id} value={option.id}>
                      {option.label}（{option.storageType}）
                    </option>
                  ))}
                </select>
              </label>

              <label className="shelf-life-rule-page__field">
                <span>保质期天数</span>
                <input
                  type="number"
                  min="1"
                  value={formState.shelfLifeDays}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      shelfLifeDays: event.target.value,
                    }))
                  }
                />
              </label>

              <label className="shelf-life-rule-page__field">
                <span>预警提前天数</span>
                <input
                  type="number"
                  min="0"
                  value={formState.warningDays}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      warningDays: event.target.value,
                    }))
                  }
                />
              </label>

              <label className="shelf-life-rule-page__field">
                <span>排序值</span>
                <input
                  type="number"
                  value={formState.sortOrder}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      sortOrder: event.target.value,
                    }))
                  }
                />
              </label>

              <label className="shelf-life-rule-page__field">
                <span>状态</span>
                <select
                  value={formState.status}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      status: event.target.value,
                    }))
                  }
                >
                  <option value="1">启用</option>
                  <option value="0">停用</option>
                </select>
              </label>

              <label className="shelf-life-rule-page__field shelf-life-rule-page__field--full">
                <span>备注</span>
                <textarea
                  rows={4}
                  value={formState.remarks}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      remarks: event.target.value,
                    }))
                  }
                />
              </label>

              {formError && !formError.fieldErrors.length ? (
                <div className="shelf-life-rule-page__message shelf-life-rule-page__message--error">
                  <strong>{formError.message}</strong>
                  {formError.traceId ? <span>traceId: {formError.traceId}</span> : null}
                </div>
              ) : null}

              <div className="shelf-life-rule-page__dialog-actions">
                <button
                  type="button"
                  className="is-ghost"
                  onClick={() => setIsDialogOpen(false)}
                >
                  取消
                </button>
                <button type="submit" className="is-dark" disabled={isSubmitting}>
                  {isSubmitting
                    ? '提交中...'
                    : dialogMode === 'create'
                      ? '确认新增'
                      : '确认更新'}
                </button>
              </div>
            </form>
          </div>
        </div>
      ) : null}
    </div>
  )
}

function buildQueryParams(queryForm: {
  ruleCode: string
  ruleName: string
  categoryId: string
  status: string
}): ShelfLifeRuleListQuery {
  return {
    ruleCode: queryForm.ruleCode.trim() || undefined,
    ruleName: queryForm.ruleName.trim() || undefined,
    categoryId: queryForm.categoryId || undefined,
    status: queryForm.status ? Number(queryForm.status) : undefined,
  }
}

function mapDetailToFormState(detail: ShelfLifeRuleDetail): ShelfLifeRuleFormState {
  return {
    ruleCode: detail.ruleCode,
    ruleName: detail.ruleName,
    categoryId: detail.categoryId ?? '',
    storageConditionId: detail.storageConditionId ?? '',
    shelfLifeDays: String(detail.shelfLifeDays),
    warningDays: String(detail.warningDays),
    status: String(detail.status),
    sortOrder: String(detail.sortOrder),
    remarks: detail.remarks ?? '',
  }
}

function mapFormStateToPayload(
  formState: ShelfLifeRuleFormState,
): ShelfLifeRuleFormPayload {
  return {
    ruleName: formState.ruleName.trim(),
    categoryId: formState.categoryId ? Number(formState.categoryId) : null,
    storageConditionId: formState.storageConditionId
      ? Number(formState.storageConditionId)
      : null,
    shelfLifeDays: Number(formState.shelfLifeDays),
    warningDays: Number(formState.warningDays),
    status: Number(formState.status),
    sortOrder: Number(formState.sortOrder),
    remarks: formState.remarks.trim() || null,
  }
}

function flattenCategoryOptions(
  options: CategoryOption[],
  depth = 0,
): Array<CategoryOption & { label: string }> {
  return options.flatMap((option) => [
    {
      ...option,
      label: `${'　'.repeat(depth)}${option.label}`,
    },
    ...flattenCategoryOptions(option.children, depth + 1),
  ])
}

function formatDateTime(value: string) {
  const date = new Date(value)

  if (Number.isNaN(date.getTime())) {
    return value
  }

  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  })
}

function toTableRows(shelfLifeRuleList: ShelfLifeRuleListItem[]): ShelfLifeRuleRow[] {
  return shelfLifeRuleList.map((item) => ({
    ...item,
    children: [],
  }))
}

export default ShelfLifeRulePage
