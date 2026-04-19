import { type FormEvent, useEffect, useState } from 'react'

import { getFieldError, normalizeError, type AppError } from '../../api/errors'
import TreeDataTable, {
  type TreeTableColumn,
  type TreeTableRow,
} from '../../components/tree-data-table/TreeDataTable'
import { getWarehouseOptions } from '../../features/warehouse/api'
import type { WarehouseOption } from '../../features/warehouse/types'
import { getWarehouseZoneOptions } from '../../features/warehousezone/api'
import type { WarehouseZoneOption } from '../../features/warehousezone/types'
import {
  createWarehouseLocation,
  deleteWarehouseLocation,
  getWarehouseLocationDetail,
  getWarehouseLocationList,
  updateWarehouseLocation,
  updateWarehouseLocationStatus,
} from '../../features/warehouselocation/api'
import type {
  WarehouseLocationDetail,
  WarehouseLocationFormPayload,
  WarehouseLocationListItem,
  WarehouseLocationListQuery,
} from '../../features/warehouselocation/types'
import './WarehouseLocationPage.css'

type WarehouseLocationRow = TreeTableRow & WarehouseLocationListItem

interface WarehouseLocationFormState {
  locationCode: string
  warehouseId: string
  zoneId: string
  locationName: string
  locationType: string
  capacity: string
  status: string
  sortOrder: string
  remarks: string
}

const initialFormState: WarehouseLocationFormState = {
  locationCode: '',
  warehouseId: '',
  zoneId: '',
  locationName: '',
  locationType: '',
  capacity: '',
  status: '1',
  sortOrder: '0',
  remarks: '',
}

const locationTypeOptions = ['货架位', '托盘位', '地堆位', '拣选位', '暂存位']

function WarehouseLocationPage() {
  const [queryForm, setQueryForm] = useState({
    locationCode: '',
    locationName: '',
    warehouseId: '',
    zoneId: '',
    status: '',
  })
  const [warehouseLocations, setWarehouseLocations] = useState<WarehouseLocationRow[]>(
    [],
  )
  const [warehouseOptions, setWarehouseOptions] = useState<WarehouseOption[]>([])
  const [warehouseZoneOptions, setWarehouseZoneOptions] = useState<
    WarehouseZoneOption[]
  >([])
  const [pageError, setPageError] = useState<AppError | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  const [isDialogOpen, setIsDialogOpen] = useState(false)
  const [dialogMode, setDialogMode] = useState<'create' | 'edit'>('create')
  const [editingId, setEditingId] = useState<string | null>(null)
  const [formState, setFormState] =
    useState<WarehouseLocationFormState>(initialFormState)
  const [formError, setFormError] = useState<AppError | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)

  useEffect(() => {
    let isMounted = true

    const bootstrap = async () => {
      try {
        const [list, warehouses, zones] = await Promise.all([
          getWarehouseLocationList(),
          getWarehouseOptions(),
          getWarehouseZoneOptions(),
        ])

        if (!isMounted) {
          return
        }

        setWarehouseLocations(toTableRows(list))
        setWarehouseOptions(warehouses)
        setWarehouseZoneOptions(zones)
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

  async function loadWarehouseLocationList(query: WarehouseLocationListQuery) {
    setIsLoading(true)
    setPageError(null)

    try {
      const list = await getWarehouseLocationList(query)
      setWarehouseLocations(toTableRows(list))
    } catch (error) {
      setPageError(normalizeError(error))
    } finally {
      setIsLoading(false)
    }
  }

  const columns: TreeTableColumn<WarehouseLocationRow>[] = [
    {
      key: 'name',
      title: '库位名称',
      minWidth: 240,
      render: (row) => (
        <div className="warehouse-location-page__name-cell">
          <strong>{row.locationName}</strong>
          <span>
            {row.warehouseName} / {row.zoneName}
          </span>
        </div>
      ),
    },
    {
      key: 'code',
      title: '库位编号',
      minWidth: 180,
      render: (row) => row.locationCode,
    },
    {
      key: 'type',
      title: '库位类型',
      minWidth: 120,
      render: (row) => row.locationType,
    },
    {
      key: 'capacity',
      title: '容量上限',
      minWidth: 120,
      render: (row) => (row.capacity != null ? row.capacity : '-'),
    },
    {
      key: 'status',
      title: '状态',
      minWidth: 120,
      render: (row) => (
        <span
          className={`warehouse-location-page__status${
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
        <div className="warehouse-location-page__row-actions">
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
    await loadWarehouseLocationList(buildQueryParams(queryForm))
  }

  const handleReset = async () => {
    const resetState = {
      locationCode: '',
      locationName: '',
      warehouseId: '',
      zoneId: '',
      status: '',
    }

    setQueryForm(resetState)
    await loadWarehouseLocationList({})
  }

  const handleCreate = () => {
    setDialogMode('create')
    setEditingId(null)
    setFormError(null)
    setFormState(initialFormState)
    setIsDialogOpen(true)
  }

  const handleEdit = async (warehouseLocationId: string) => {
    setDialogMode('edit')
    setEditingId(warehouseLocationId)
    setFormError(null)
    setIsDialogOpen(true)
    setIsSubmitting(true)

    try {
      const detail = await getWarehouseLocationDetail(warehouseLocationId)
      setFormState(mapDetailToFormState(detail))
    } catch (error) {
      setFormError(normalizeError(error))
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleToggleStatus = async (row: WarehouseLocationRow) => {
    const nextStatus = row.status === 1 ? 0 : 1
    const confirmed = window.confirm(
      `确定要将“${row.locationName}”${nextStatus === 1 ? '启用' : '停用'}吗？`,
    )

    if (!confirmed) {
      return
    }

    try {
      await updateWarehouseLocationStatus(row.id, nextStatus)
      await loadWarehouseLocationList(buildQueryParams(queryForm))
    } catch (error) {
      setPageError(normalizeError(error))
    }
  }

  const handleDelete = async (row: WarehouseLocationRow) => {
    const confirmed = window.confirm(`确定要删除库位“${row.locationName}”吗？`)

    if (!confirmed) {
      return
    }

    try {
      await deleteWarehouseLocation(row.id)
      await loadWarehouseLocationList(buildQueryParams(queryForm))
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
        await createWarehouseLocation(payload)
      } else if (editingId) {
        await updateWarehouseLocation(editingId, payload)
      }

      setIsDialogOpen(false)
      setFormState(initialFormState)
      await loadWarehouseLocationList(buildQueryParams(queryForm))
    } catch (error) {
      setFormError(normalizeError(error))
    } finally {
      setIsSubmitting(false)
    }
  }

  const nameFieldError = getFieldError(formError?.fieldErrors, 'locationName')

  return (
    <div className="warehouse-location-page">
      {pageError ? (
        <div className="warehouse-location-page__message warehouse-location-page__message--error">
          <strong>{pageError.message}</strong>
          {pageError.traceId ? <span>traceId: {pageError.traceId}</span> : null}
        </div>
      ) : null}

      <section className="warehouse-location-page__search">
        <div className="warehouse-location-page__search-grid">
          <label className="warehouse-location-page__field">
            <span>库位编号</span>
            <input
              type="text"
              placeholder="输入编号..."
              value={queryForm.locationCode}
              onChange={(event) =>
                setQueryForm((current) => ({
                  ...current,
                  locationCode: event.target.value,
                }))
              }
            />
          </label>

          <label className="warehouse-location-page__field">
            <span>库位名称</span>
            <input
              type="text"
              placeholder="输入名称..."
              value={queryForm.locationName}
              onChange={(event) =>
                setQueryForm((current) => ({
                  ...current,
                  locationName: event.target.value,
                }))
              }
            />
          </label>

          <label className="warehouse-location-page__field">
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

          <label className="warehouse-location-page__field">
            <span>所属库区</span>
            <select
              value={queryForm.zoneId}
              onChange={(event) =>
                setQueryForm((current) => ({
                  ...current,
                  zoneId: event.target.value,
                }))
              }
            >
              <option value="">全部库区</option>
              {warehouseZoneOptions.map((option) => (
                <option key={option.id} value={option.id}>
                  {option.label}
                </option>
              ))}
            </select>
          </label>

          <label className="warehouse-location-page__field">
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

          <div className="warehouse-location-page__actions">
            <button type="button" className="is-ghost" onClick={() => void handleReset()}>
              重置
            </button>
            <button type="button" className="is-dark" onClick={() => void handleSearch()}>
              查询
            </button>
          </div>
        </div>
      </section>

      <section className="warehouse-location-page__toolbar">
        <div className="warehouse-location-page__toolbar-copy">
          <h3>库位列表</h3>
          <p>维护仓库内部可实际存放货物的库位信息，为上架、拣选和库存定位提供细粒度空间维度。</p>
        </div>

        <button type="button" className="warehouse-location-page__primary" onClick={handleCreate}>
          新增库位
        </button>
      </section>

      <section className="warehouse-location-page__table-shell">
        {isLoading ? (
          <div className="warehouse-location-page__loading">正在加载库位数据...</div>
        ) : (
          <TreeDataTable
            data={warehouseLocations}
            columns={columns}
            emptyText="暂无库位数据"
          />
        )}
      </section>

      {isDialogOpen ? (
        <div className="warehouse-location-page__dialog-backdrop">
          <div className="warehouse-location-page__dialog">
            <div className="warehouse-location-page__dialog-header">
              <div>
                <h3>{dialogMode === 'create' ? '新增库位' : '编辑库位'}</h3>
                <p>维护所属仓库、所属库区、库位类型和容量信息。</p>
              </div>
              <button
                type="button"
                className="warehouse-location-page__dialog-close"
                onClick={() => setIsDialogOpen(false)}
              >
                ×
              </button>
            </div>

            <form className="warehouse-location-page__dialog-form" onSubmit={handleDialogSubmit}>
              <label className="warehouse-location-page__field">
                <span>库位编号</span>
                <input value={formState.locationCode} placeholder="系统自动生成" readOnly disabled />
                <small className="warehouse-location-page__field-hint">
                  {dialogMode === 'create' ? '新增时由系统自动生成编号' : '库位编号创建后不可修改'}
                </small>
              </label>

              <label className="warehouse-location-page__field">
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

              <label className="warehouse-location-page__field">
                <span>所属库区</span>
                <select
                  value={formState.zoneId}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      zoneId: event.target.value,
                    }))
                  }
                >
                  <option value="">请选择</option>
                  {warehouseZoneOptions.map((option) => (
                    <option key={option.id} value={option.id}>
                      {option.warehouseName} / {option.label}
                    </option>
                  ))}
                </select>
              </label>

              <label className="warehouse-location-page__field">
                <span>库位名称</span>
                <input
                  value={formState.locationName}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      locationName: event.target.value,
                    }))
                  }
                />
                {nameFieldError ? (
                  <small className="warehouse-location-page__field-error">{nameFieldError}</small>
                ) : null}
              </label>

              <label className="warehouse-location-page__field">
                <span>库位类型</span>
                <select
                  value={formState.locationType}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      locationType: event.target.value,
                    }))
                  }
                >
                  <option value="">请选择</option>
                  {locationTypeOptions.map((locationType) => (
                    <option key={locationType} value={locationType}>
                      {locationType}
                    </option>
                  ))}
                </select>
              </label>

              <label className="warehouse-location-page__field">
                <span>容量上限</span>
                <input
                  type="number"
                  min="0"
                  value={formState.capacity}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      capacity: event.target.value,
                    }))
                  }
                />
              </label>

              <label className="warehouse-location-page__field">
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

              <label className="warehouse-location-page__field">
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

              <label className="warehouse-location-page__field warehouse-location-page__field--full">
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
                <div className="warehouse-location-page__message warehouse-location-page__message--error">
                  <strong>{formError.message}</strong>
                  {formError.traceId ? <span>traceId: {formError.traceId}</span> : null}
                </div>
              ) : null}

              <div className="warehouse-location-page__dialog-actions">
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
  locationCode: string
  locationName: string
  warehouseId: string
  zoneId: string
  status: string
}): WarehouseLocationListQuery {
  return {
    locationCode: queryForm.locationCode.trim() || undefined,
    locationName: queryForm.locationName.trim() || undefined,
    warehouseId: queryForm.warehouseId || undefined,
    zoneId: queryForm.zoneId || undefined,
    status: queryForm.status ? Number(queryForm.status) : undefined,
  }
}

function mapDetailToFormState(detail: WarehouseLocationDetail): WarehouseLocationFormState {
  return {
    locationCode: detail.locationCode,
    warehouseId: String(detail.warehouseId),
    zoneId: String(detail.zoneId),
    locationName: detail.locationName,
    locationType: detail.locationType,
    capacity: detail.capacity != null ? String(detail.capacity) : '',
    status: String(detail.status),
    sortOrder: String(detail.sortOrder),
    remarks: detail.remarks ?? '',
  }
}

function mapFormStateToPayload(
  formState: WarehouseLocationFormState,
): WarehouseLocationFormPayload {
  return {
    warehouseId: Number(formState.warehouseId),
    zoneId: Number(formState.zoneId),
    locationName: formState.locationName.trim(),
    locationType: formState.locationType.trim(),
    capacity: formState.capacity ? Number(formState.capacity) : null,
    status: Number(formState.status),
    sortOrder: Number(formState.sortOrder),
    remarks: formState.remarks.trim() || null,
  }
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

function toTableRows(warehouseLocationList: WarehouseLocationListItem[]): WarehouseLocationRow[] {
  return warehouseLocationList.map((item) => ({
    ...item,
    children: [],
  }))
}

export default WarehouseLocationPage
