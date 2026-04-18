import { type FormEvent, useEffect, useState } from 'react'

import { getFieldError, normalizeError, type AppError } from '../../api/errors'
import TreeDataTable, {
  type TreeTableColumn,
  type TreeTableRow,
} from '../../components/tree-data-table/TreeDataTable'
import {
  createStorageCondition,
  deleteStorageCondition,
  getStorageConditionDetail,
  getStorageConditionList,
  updateStorageCondition,
  updateStorageConditionStatus,
} from '../../features/storagecondition/api'
import type {
  StorageConditionDetail,
  StorageConditionFormPayload,
  StorageConditionListItem,
  StorageConditionListQuery,
} from '../../features/storagecondition/types'
import './StorageConditionPage.css'

type StorageConditionRow = TreeTableRow & StorageConditionListItem

interface StorageConditionFormState {
  conditionCode: string
  conditionName: string
  storageType: string
  temperatureMin: string
  temperatureMax: string
  humidityMin: string
  humidityMax: string
  lightRequirement: string
  ventilationRequirement: string
  status: string
  sortOrder: string
  remarks: string
}

const initialFormState: StorageConditionFormState = {
  conditionCode: '',
  conditionName: '',
  storageType: '',
  temperatureMin: '',
  temperatureMax: '',
  humidityMin: '',
  humidityMax: '',
  lightRequirement: '无特殊要求',
  ventilationRequirement: '普通通风',
  status: '1',
  sortOrder: '0',
  remarks: '',
}

const storageTypeOptions = ['冷藏', '冷冻', '常温', '阴凉干燥', '恒温']
const lightRequirementOptions = ['无特殊要求', '需避强光', '避免直射阳光', '全避光']
const ventilationRequirementOptions = ['无特殊要求', '普通通风', '强通风', '密闭']

function StorageConditionPage() {
  const [queryForm, setQueryForm] = useState({
    conditionCode: '',
    conditionName: '',
    storageType: '',
    status: '',
  })
  const [storageConditions, setStorageConditions] = useState<StorageConditionRow[]>(
    [],
  )
  const [pageError, setPageError] = useState<AppError | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  const [isDialogOpen, setIsDialogOpen] = useState(false)
  const [dialogMode, setDialogMode] = useState<'create' | 'edit'>('create')
  const [editingId, setEditingId] = useState<string | null>(null)
  const [formState, setFormState] =
    useState<StorageConditionFormState>(initialFormState)
  const [formError, setFormError] = useState<AppError | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)

  useEffect(() => {
    let isMounted = true

    const bootstrap = async () => {
      try {
        const list = await getStorageConditionList()

        if (!isMounted) {
          return
        }

        setStorageConditions(toTableRows(list))
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

  async function loadStorageConditionList(query: StorageConditionListQuery) {
    setIsLoading(true)
    setPageError(null)

    try {
      const list = await getStorageConditionList(query)
      setStorageConditions(toTableRows(list))
    } catch (error) {
      setPageError(normalizeError(error))
    } finally {
      setIsLoading(false)
    }
  }

  const columns: TreeTableColumn<StorageConditionRow>[] = [
    {
      key: 'name',
      title: '条件名称',
      minWidth: 240,
      render: (row) => (
        <div className="storage-condition-page__name-cell">
          <strong>{row.conditionName}</strong>
          <span>{row.storageType}</span>
        </div>
      ),
    },
    {
      key: 'code',
      title: '条件编号',
      minWidth: 180,
      render: (row) => row.conditionCode,
    },
    {
      key: 'temperature',
      title: '温度范围',
      minWidth: 150,
      render: (row) => formatRange(row.temperatureMin, row.temperatureMax, '°C'),
    },
    {
      key: 'humidity',
      title: '湿度范围',
      minWidth: 150,
      render: (row) => formatRange(row.humidityMin, row.humidityMax, '%'),
    },
    {
      key: 'environment',
      title: '环境要求',
      minWidth: 220,
      render: (row) =>
        [
          row.lightRequirement,
          row.ventilationRequirement,
        ]
          .filter(Boolean)
          .join(' / ') || '-',
    },
    {
      key: 'status',
      title: '状态',
      minWidth: 120,
      render: (row) => (
        <span
          className={`storage-condition-page__status${
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
        <div className="storage-condition-page__row-actions">
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
    await loadStorageConditionList(buildQueryParams(queryForm))
  }

  const handleReset = async () => {
    const resetState = {
      conditionCode: '',
      conditionName: '',
      storageType: '',
      status: '',
    }

    setQueryForm(resetState)
    await loadStorageConditionList({})
  }

  const handleCreate = () => {
    setDialogMode('create')
    setEditingId(null)
    setFormError(null)
    setFormState(initialFormState)
    setIsDialogOpen(true)
  }

  const handleEdit = async (storageConditionId: string) => {
    setDialogMode('edit')
    setEditingId(storageConditionId)
    setFormError(null)
    setIsDialogOpen(true)
    setIsSubmitting(true)

    try {
      const detail = await getStorageConditionDetail(storageConditionId)
      setFormState(mapDetailToFormState(detail))
    } catch (error) {
      setFormError(normalizeError(error))
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleToggleStatus = async (row: StorageConditionRow) => {
    const nextStatus = row.status === 1 ? 0 : 1
    const confirmed = window.confirm(
      `确定要将“${row.conditionName}”${nextStatus === 1 ? '启用' : '停用'}吗？`,
    )

    if (!confirmed) {
      return
    }

    try {
      await updateStorageConditionStatus(row.id, nextStatus)
      await loadStorageConditionList(buildQueryParams(queryForm))
    } catch (error) {
      setPageError(normalizeError(error))
    }
  }

  const handleDelete = async (row: StorageConditionRow) => {
    const confirmed = window.confirm(`确定要删除储存条件“${row.conditionName}”吗？`)

    if (!confirmed) {
      return
    }

    try {
      await deleteStorageCondition(row.id)
      await loadStorageConditionList(buildQueryParams(queryForm))
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
        await createStorageCondition(payload)
      } else if (editingId) {
        await updateStorageCondition(editingId, payload)
      }

      setIsDialogOpen(false)
      setFormState(initialFormState)
      await loadStorageConditionList(buildQueryParams(queryForm))
    } catch (error) {
      setFormError(normalizeError(error))
    } finally {
      setIsSubmitting(false)
    }
  }

  const nameFieldError = getFieldError(formError?.fieldErrors, 'conditionName')

  return (
    <div className="storage-condition-page">
      {pageError ? (
        <div className="storage-condition-page__message storage-condition-page__message--error">
          <strong>{pageError.message}</strong>
          {pageError.traceId ? (
            <span>traceId: {pageError.traceId}</span>
          ) : null}
        </div>
      ) : null}

      <section className="storage-condition-page__search">
        <div className="storage-condition-page__search-grid">
          <label className="storage-condition-page__field">
            <span>条件编号</span>
            <input
              type="text"
              placeholder="输入编号..."
              value={queryForm.conditionCode}
              onChange={(event) =>
                setQueryForm((current) => ({
                  ...current,
                  conditionCode: event.target.value,
                }))
              }
            />
          </label>

          <label className="storage-condition-page__field">
            <span>条件名称</span>
            <input
              type="text"
              placeholder="输入名称..."
              value={queryForm.conditionName}
              onChange={(event) =>
                setQueryForm((current) => ({
                  ...current,
                  conditionName: event.target.value,
                }))
              }
            />
          </label>

          <label className="storage-condition-page__field">
            <span>储存类型</span>
            <select
              value={queryForm.storageType}
              onChange={(event) =>
                setQueryForm((current) => ({
                  ...current,
                  storageType: event.target.value,
                }))
              }
            >
              <option value="">全部类型</option>
              {storageTypeOptions.map((storageType) => (
                <option key={storageType} value={storageType}>
                  {storageType}
                </option>
              ))}
            </select>
          </label>

          <label className="storage-condition-page__field">
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

          <div className="storage-condition-page__actions">
            <button type="button" className="is-ghost" onClick={() => void handleReset()}>
              重置
            </button>
            <button type="button" className="is-dark" onClick={() => void handleSearch()}>
              查询
            </button>
          </div>
        </div>
      </section>

      <section className="storage-condition-page__toolbar">
        <div className="storage-condition-page__toolbar-copy">
          <h3>储存条件列表</h3>
          <p>维护仓内存放环境标准，为产品分类和产品档案提供统一规则。</p>
        </div>

        <button type="button" className="storage-condition-page__primary" onClick={handleCreate}>
          新增储存条件
        </button>
      </section>

      <section className="storage-condition-page__table-shell">
        {isLoading ? (
          <div className="storage-condition-page__loading">正在加载储存条件...</div>
        ) : (
          <TreeDataTable
            data={storageConditions}
            columns={columns}
            emptyText="暂无储存条件数据"
          />
        )}
      </section>

      {isDialogOpen ? (
        <div className="storage-condition-page__dialog-backdrop">
          <div className="storage-condition-page__dialog">
            <div className="storage-condition-page__dialog-header">
              <div>
                <h3>{dialogMode === 'create' ? '新增储存条件' : '编辑储存条件'}</h3>
                <p>维护温湿度、避光、通风和存放规则。</p>
              </div>
              <button
                type="button"
                className="storage-condition-page__dialog-close"
                onClick={() => setIsDialogOpen(false)}
              >
                ×
              </button>
            </div>

            <form className="storage-condition-page__dialog-form" onSubmit={handleDialogSubmit}>
              <label className="storage-condition-page__field">
                <span>条件编号</span>
                <input value={formState.conditionCode} placeholder="系统自动生成" readOnly disabled />
                <small className="storage-condition-page__field-hint">
                  {dialogMode === 'create' ? '新增时由系统自动生成编号' : '条件编号创建后不可修改'}
                </small>
              </label>

              <label className="storage-condition-page__field">
                <span>条件名称</span>
                <input
                  value={formState.conditionName}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      conditionName: event.target.value,
                    }))
                  }
                />
                {nameFieldError ? (
                  <small className="storage-condition-page__field-error">{nameFieldError}</small>
                ) : null}
              </label>

              <label className="storage-condition-page__field">
                <span>储存类型</span>
                <select
                  value={formState.storageType}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      storageType: event.target.value,
                    }))
                  }
                >
                  <option value="">请选择</option>
                  {storageTypeOptions.map((storageType) => (
                    <option key={storageType} value={storageType}>
                      {storageType}
                    </option>
                  ))}
                </select>
              </label>

              <label className="storage-condition-page__field">
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

              <label className="storage-condition-page__field">
                <span>最低温度（°C）</span>
                <input
                  type="number"
                  step="0.1"
                  value={formState.temperatureMin}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      temperatureMin: event.target.value,
                    }))
                  }
                />
              </label>

              <label className="storage-condition-page__field">
                <span>最高温度（°C）</span>
                <input
                  type="number"
                  step="0.1"
                  value={formState.temperatureMax}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      temperatureMax: event.target.value,
                    }))
                  }
                />
              </label>

              <label className="storage-condition-page__field">
                <span>最低湿度（%）</span>
                <input
                  type="number"
                  step="0.1"
                  value={formState.humidityMin}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      humidityMin: event.target.value,
                    }))
                  }
                />
              </label>

              <label className="storage-condition-page__field">
                <span>最高湿度（%）</span>
                <input
                  type="number"
                  step="0.1"
                  value={formState.humidityMax}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      humidityMax: event.target.value,
                    }))
                  }
                />
              </label>

              <label className="storage-condition-page__field">
                <span>避光要求</span>
                <select
                  value={formState.lightRequirement}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      lightRequirement: event.target.value,
                    }))
                  }
                >
                  {lightRequirementOptions.map((option) => (
                    <option key={option} value={option}>
                      {option}
                    </option>
                  ))}
                </select>
              </label>

              <label className="storage-condition-page__field">
                <span>通风要求</span>
                <select
                  value={formState.ventilationRequirement}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      ventilationRequirement: event.target.value,
                    }))
                  }
                >
                  {ventilationRequirementOptions.map((option) => (
                    <option key={option} value={option}>
                      {option}
                    </option>
                  ))}
                </select>
              </label>

              <label className="storage-condition-page__field">
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

              <label className="storage-condition-page__field storage-condition-page__field--full">
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
                <div className="storage-condition-page__message storage-condition-page__message--error">
                  <strong>{formError.message}</strong>
                  {formError.traceId ? (
                    <span>traceId: {formError.traceId}</span>
                  ) : null}
                </div>
              ) : null}

              <div className="storage-condition-page__dialog-actions">
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
  conditionCode: string
  conditionName: string
  storageType: string
  status: string
}): StorageConditionListQuery {
  return {
    conditionCode: queryForm.conditionCode.trim() || undefined,
    conditionName: queryForm.conditionName.trim() || undefined,
    storageType: queryForm.storageType || undefined,
    status: queryForm.status ? Number(queryForm.status) : undefined,
  }
}

function mapDetailToFormState(detail: StorageConditionDetail): StorageConditionFormState {
  return {
    conditionCode: detail.conditionCode,
    conditionName: detail.conditionName,
    storageType: detail.storageType,
    temperatureMin: detail.temperatureMin != null ? String(detail.temperatureMin) : '',
    temperatureMax: detail.temperatureMax != null ? String(detail.temperatureMax) : '',
    humidityMin: detail.humidityMin != null ? String(detail.humidityMin) : '',
    humidityMax: detail.humidityMax != null ? String(detail.humidityMax) : '',
    lightRequirement: detail.lightRequirement ?? '',
    ventilationRequirement: detail.ventilationRequirement ?? '',
    status: String(detail.status),
    sortOrder: String(detail.sortOrder),
    remarks: detail.remarks ?? '',
  }
}

function mapFormStateToPayload(
  formState: StorageConditionFormState,
): StorageConditionFormPayload {
  return {
    conditionName: formState.conditionName.trim(),
    storageType: formState.storageType.trim(),
    temperatureMin: formState.temperatureMin ? Number(formState.temperatureMin) : null,
    temperatureMax: formState.temperatureMax ? Number(formState.temperatureMax) : null,
    humidityMin: formState.humidityMin ? Number(formState.humidityMin) : null,
    humidityMax: formState.humidityMax ? Number(formState.humidityMax) : null,
    lightRequirement: formState.lightRequirement.trim() || null,
    ventilationRequirement: formState.ventilationRequirement.trim() || null,
    status: Number(formState.status),
    sortOrder: Number(formState.sortOrder),
    remarks: formState.remarks.trim() || null,
  }
}

function formatRange(min: number | null, max: number | null, unit: string) {
  if (min != null && max != null) {
    return `${min} ~ ${max}${unit}`
  }

  if (min != null) {
    return `≥ ${min}${unit}`
  }

  if (max != null) {
    return `≤ ${max}${unit}`
  }

  return '-'
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

function toTableRows(
  storageConditionList: StorageConditionListItem[],
): StorageConditionRow[] {
  return storageConditionList.map((item) => ({
    ...item,
    children: [],
  }))
}

export default StorageConditionPage
