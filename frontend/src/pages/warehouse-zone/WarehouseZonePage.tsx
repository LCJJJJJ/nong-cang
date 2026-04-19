import { type FormEvent, useEffect, useState } from 'react'

import { getFieldError, normalizeError, type AppError } from '../../api/errors'
import TreeDataTable, {
  type TreeTableColumn,
  type TreeTableRow,
} from '../../components/tree-data-table/TreeDataTable'
import { getWarehouseOptions } from '../../features/warehouse/api'
import type { WarehouseOption } from '../../features/warehouse/types'
import {
  createWarehouseZone,
  deleteWarehouseZone,
  getWarehouseZoneDetail,
  getWarehouseZoneList,
  updateWarehouseZone,
  updateWarehouseZoneStatus,
} from '../../features/warehousezone/api'
import type {
  WarehouseZoneDetail,
  WarehouseZoneFormPayload,
  WarehouseZoneListItem,
  WarehouseZoneListQuery,
} from '../../features/warehousezone/types'
import './WarehouseZonePage.css'

type WarehouseZoneRow = TreeTableRow & WarehouseZoneListItem

interface WarehouseZoneFormState {
  zoneCode: string
  warehouseId: string
  zoneName: string
  zoneType: string
  temperatureMin: string
  temperatureMax: string
  status: string
  sortOrder: string
  remarks: string
}

const initialFormState: WarehouseZoneFormState = {
  zoneCode: '',
  warehouseId: '',
  zoneName: '',
  zoneType: '',
  temperatureMin: '',
  temperatureMax: '',
  status: '1',
  sortOrder: '0',
  remarks: '',
}

const zoneTypeOptions = ['收货区', '暂存区', '常温区', '冷藏区', '冷冻区', '拣选区']

function WarehouseZonePage() {
  const [queryForm, setQueryForm] = useState({
    zoneCode: '',
    zoneName: '',
    warehouseId: '',
    status: '',
  })
  const [warehouseZones, setWarehouseZones] = useState<WarehouseZoneRow[]>([])
  const [warehouseOptions, setWarehouseOptions] = useState<WarehouseOption[]>([])
  const [pageError, setPageError] = useState<AppError | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  const [isDialogOpen, setIsDialogOpen] = useState(false)
  const [dialogMode, setDialogMode] = useState<'create' | 'edit'>('create')
  const [editingId, setEditingId] = useState<string | null>(null)
  const [formState, setFormState] = useState<WarehouseZoneFormState>(initialFormState)
  const [formError, setFormError] = useState<AppError | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)

  useEffect(() => {
    let isMounted = true

    const bootstrap = async () => {
      try {
        const [list, warehouses] = await Promise.all([
          getWarehouseZoneList(),
          getWarehouseOptions(),
        ])

        if (!isMounted) {
          return
        }

        setWarehouseZones(toTableRows(list))
        setWarehouseOptions(warehouses)
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

  async function loadWarehouseZoneList(query: WarehouseZoneListQuery) {
    setIsLoading(true)
    setPageError(null)

    try {
      const list = await getWarehouseZoneList(query)
      setWarehouseZones(toTableRows(list))
    } catch (error) {
      setPageError(normalizeError(error))
    } finally {
      setIsLoading(false)
    }
  }

  const columns: TreeTableColumn<WarehouseZoneRow>[] = [
    {
      key: 'name',
      title: '库区名称',
      minWidth: 240,
      render: (row) => (
        <div className="warehouse-zone-page__name-cell">
          <strong>{row.zoneName}</strong>
          <span>{row.warehouseName}</span>
        </div>
      ),
    },
    {
      key: 'code',
      title: '库区编号',
      minWidth: 180,
      render: (row) => row.zoneCode,
    },
    {
      key: 'type',
      title: '库区类型',
      minWidth: 120,
      render: (row) => row.zoneType,
    },
    {
      key: 'temperature',
      title: '温度范围',
      minWidth: 140,
      render: (row) => formatTemperatureRange(row.temperatureMin, row.temperatureMax),
    },
    {
      key: 'status',
      title: '状态',
      minWidth: 120,
      render: (row) => (
        <span
          className={`warehouse-zone-page__status${
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
        <div className="warehouse-zone-page__row-actions">
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
    await loadWarehouseZoneList(buildQueryParams(queryForm))
  }

  const handleReset = async () => {
    const resetState = {
      zoneCode: '',
      zoneName: '',
      warehouseId: '',
      status: '',
    }

    setQueryForm(resetState)
    await loadWarehouseZoneList({})
  }

  const handleCreate = () => {
    setDialogMode('create')
    setEditingId(null)
    setFormError(null)
    setFormState(initialFormState)
    setIsDialogOpen(true)
  }

  const handleEdit = async (warehouseZoneId: string) => {
    setDialogMode('edit')
    setEditingId(warehouseZoneId)
    setFormError(null)
    setIsDialogOpen(true)
    setIsSubmitting(true)

    try {
      const detail = await getWarehouseZoneDetail(warehouseZoneId)
      setFormState(mapDetailToFormState(detail))
    } catch (error) {
      setFormError(normalizeError(error))
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleToggleStatus = async (row: WarehouseZoneRow) => {
    const nextStatus = row.status === 1 ? 0 : 1
    const confirmed = window.confirm(
      `确定要将“${row.zoneName}”${nextStatus === 1 ? '启用' : '停用'}吗？`,
    )

    if (!confirmed) {
      return
    }

    try {
      await updateWarehouseZoneStatus(row.id, nextStatus)
      await loadWarehouseZoneList(buildQueryParams(queryForm))
    } catch (error) {
      setPageError(normalizeError(error))
    }
  }

  const handleDelete = async (row: WarehouseZoneRow) => {
    const confirmed = window.confirm(`确定要删除库区“${row.zoneName}”吗？`)

    if (!confirmed) {
      return
    }

    try {
      await deleteWarehouseZone(row.id)
      await loadWarehouseZoneList(buildQueryParams(queryForm))
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
        await createWarehouseZone(payload)
      } else if (editingId) {
        await updateWarehouseZone(editingId, payload)
      }

      setIsDialogOpen(false)
      setFormState(initialFormState)
      await loadWarehouseZoneList(buildQueryParams(queryForm))
    } catch (error) {
      setFormError(normalizeError(error))
    } finally {
      setIsSubmitting(false)
    }
  }

  const nameFieldError = getFieldError(formError?.fieldErrors, 'zoneName')

  return (
    <div className="warehouse-zone-page">
      {pageError ? (
        <div className="warehouse-zone-page__message warehouse-zone-page__message--error">
          <strong>{pageError.message}</strong>
          {pageError.traceId ? <span>traceId: {pageError.traceId}</span> : null}
        </div>
      ) : null}

      <section className="warehouse-zone-page__search">
        <div className="warehouse-zone-page__search-grid">
          <label className="warehouse-zone-page__field">
            <span>库区编号</span>
            <input
              type="text"
              placeholder="输入编号..."
              value={queryForm.zoneCode}
              onChange={(event) =>
                setQueryForm((current) => ({
                  ...current,
                  zoneCode: event.target.value,
                }))
              }
            />
          </label>

          <label className="warehouse-zone-page__field">
            <span>库区名称</span>
            <input
              type="text"
              placeholder="输入名称..."
              value={queryForm.zoneName}
              onChange={(event) =>
                setQueryForm((current) => ({
                  ...current,
                  zoneName: event.target.value,
                }))
              }
            />
          </label>

          <label className="warehouse-zone-page__field">
            <span>所属仓库</span>
            <select
              value={queryForm.warehouseId}
              onChange={(event) =>
                setQueryForm((current) => ({
                  ...current,
                  warehouseId: event.target.value,
                }))
              }
            >
              <option value="">全部仓库</option>
              {warehouseOptions.map((option) => (
                <option key={option.id} value={option.id}>
                  {option.label}
                </option>
              ))}
            </select>
          </label>

          <label className="warehouse-zone-page__field">
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

          <div className="warehouse-zone-page__actions">
            <button type="button" className="is-ghost" onClick={() => void handleReset()}>
              重置
            </button>
            <button type="button" className="is-dark" onClick={() => void handleSearch()}>
              查询
            </button>
          </div>
        </div>
      </section>

      <section className="warehouse-zone-page__toolbar">
        <div className="warehouse-zone-page__toolbar-copy">
          <h3>库区列表</h3>
          <p>维护仓库内部功能分区，为库位管理、入库上架和后续库存业务提供统一库区维度。</p>
        </div>

        <button type="button" className="warehouse-zone-page__primary" onClick={handleCreate}>
          新增库区
        </button>
      </section>

      <section className="warehouse-zone-page__table-shell">
        {isLoading ? (
          <div className="warehouse-zone-page__loading">正在加载库区数据...</div>
        ) : (
          <TreeDataTable
            data={warehouseZones}
            columns={columns}
            emptyText="暂无库区数据"
          />
        )}
      </section>

      {isDialogOpen ? (
        <div className="warehouse-zone-page__dialog-backdrop">
          <div className="warehouse-zone-page__dialog">
            <div className="warehouse-zone-page__dialog-header">
              <div>
                <h3>{dialogMode === 'create' ? '新增库区' : '编辑库区'}</h3>
                <p>维护所属仓库、库区类型和温度范围。</p>
              </div>
              <button
                type="button"
                className="warehouse-zone-page__dialog-close"
                onClick={() => setIsDialogOpen(false)}
              >
                ×
              </button>
            </div>

            <form className="warehouse-zone-page__dialog-form" onSubmit={handleDialogSubmit}>
              <label className="warehouse-zone-page__field">
                <span>库区编号</span>
                <input value={formState.zoneCode} placeholder="系统自动生成" readOnly disabled />
                <small className="warehouse-zone-page__field-hint">
                  {dialogMode === 'create' ? '新增时由系统自动生成编号' : '库区编号创建后不可修改'}
                </small>
              </label>

              <label className="warehouse-zone-page__field">
                <span>所属仓库</span>
                <select
                  value={formState.warehouseId}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      warehouseId: event.target.value,
                    }))
                  }
                >
                  <option value="">请选择</option>
                  {warehouseOptions.map((option) => (
                    <option key={option.id} value={option.id}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </label>

              <label className="warehouse-zone-page__field">
                <span>库区名称</span>
                <input
                  value={formState.zoneName}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      zoneName: event.target.value,
                    }))
                  }
                />
                {nameFieldError ? (
                  <small className="warehouse-zone-page__field-error">{nameFieldError}</small>
                ) : null}
              </label>

              <label className="warehouse-zone-page__field">
                <span>库区类型</span>
                <select
                  value={formState.zoneType}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      zoneType: event.target.value,
                    }))
                  }
                >
                  <option value="">请选择</option>
                  {zoneTypeOptions.map((zoneType) => (
                    <option key={zoneType} value={zoneType}>
                      {zoneType}
                    </option>
                  ))}
                </select>
              </label>

              <label className="warehouse-zone-page__field">
                <span>最低温度</span>
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

              <label className="warehouse-zone-page__field">
                <span>最高温度</span>
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

              <label className="warehouse-zone-page__field">
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

              <label className="warehouse-zone-page__field">
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

              <label className="warehouse-zone-page__field warehouse-zone-page__field--full">
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
                <div className="warehouse-zone-page__message warehouse-zone-page__message--error">
                  <strong>{formError.message}</strong>
                  {formError.traceId ? <span>traceId: {formError.traceId}</span> : null}
                </div>
              ) : null}

              <div className="warehouse-zone-page__dialog-actions">
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
  zoneCode: string
  zoneName: string
  warehouseId: string
  status: string
}): WarehouseZoneListQuery {
  return {
    zoneCode: queryForm.zoneCode.trim() || undefined,
    zoneName: queryForm.zoneName.trim() || undefined,
    warehouseId: queryForm.warehouseId || undefined,
    status: queryForm.status ? Number(queryForm.status) : undefined,
  }
}

function mapDetailToFormState(detail: WarehouseZoneDetail): WarehouseZoneFormState {
  return {
    zoneCode: detail.zoneCode,
    warehouseId: String(detail.warehouseId),
    zoneName: detail.zoneName,
    zoneType: detail.zoneType,
    temperatureMin: detail.temperatureMin != null ? String(detail.temperatureMin) : '',
    temperatureMax: detail.temperatureMax != null ? String(detail.temperatureMax) : '',
    status: String(detail.status),
    sortOrder: String(detail.sortOrder),
    remarks: detail.remarks ?? '',
  }
}

function mapFormStateToPayload(
  formState: WarehouseZoneFormState,
): WarehouseZoneFormPayload {
  return {
    warehouseId: Number(formState.warehouseId),
    zoneName: formState.zoneName.trim(),
    zoneType: formState.zoneType.trim(),
    temperatureMin: formState.temperatureMin ? Number(formState.temperatureMin) : null,
    temperatureMax: formState.temperatureMax ? Number(formState.temperatureMax) : null,
    status: Number(formState.status),
    sortOrder: Number(formState.sortOrder),
    remarks: formState.remarks.trim() || null,
  }
}

function formatTemperatureRange(min: number | null, max: number | null) {
  if (min != null && max != null) {
    return `${min} ~ ${max}°C`
  }

  if (min != null) {
    return `≥ ${min}°C`
  }

  if (max != null) {
    return `≤ ${max}°C`
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

function toTableRows(warehouseZoneList: WarehouseZoneListItem[]): WarehouseZoneRow[] {
  return warehouseZoneList.map((item) => ({
    ...item,
    children: [],
  }))
}

export default WarehouseZonePage
