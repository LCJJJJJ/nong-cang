import { type FormEvent, useEffect, useState } from 'react'

import { getFieldError, normalizeError, type AppError } from '../../api/errors'
import TreeDataTable, {
  type TreeTableColumn,
  type TreeTableRow,
} from '../../components/tree-data-table/TreeDataTable'
import {
  createWarehouse,
  deleteWarehouse,
  getWarehouseDetail,
  getWarehouseList,
  updateWarehouse,
  updateWarehouseStatus,
} from '../../features/warehouse/api'
import type {
  WarehouseDetail,
  WarehouseFormPayload,
  WarehouseListItem,
  WarehouseListQuery,
} from '../../features/warehouse/types'
import { usePagePermission } from '../../features/auth/usePagePermission'
import './WarehousePage.css'

type WarehouseRow = TreeTableRow & WarehouseListItem

interface WarehouseFormState {
  warehouseCode: string
  warehouseName: string
  warehouseType: string
  managerName: string
  contactPhone: string
  address: string
  status: string
  sortOrder: string
  remarks: string
}

const initialFormState: WarehouseFormState = {
  warehouseCode: '',
  warehouseName: '',
  warehouseType: '',
  managerName: '',
  contactPhone: '',
  address: '',
  status: '1',
  sortOrder: '0',
  remarks: '',
}

const warehouseTypeOptions = ['综合仓', '常温仓', '冷藏仓', '冷冻仓']

function WarehousePage() {
  const [queryForm, setQueryForm] = useState({
    warehouseCode: '',
    warehouseName: '',
    warehouseType: '',
    status: '',
  })
  const [warehouses, setWarehouses] = useState<WarehouseRow[]>([])
  const [pageError, setPageError] = useState<AppError | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  const [isDialogOpen, setIsDialogOpen] = useState(false)
  const [dialogMode, setDialogMode] = useState<'create' | 'edit'>('create')
  const [editingId, setEditingId] = useState<string | null>(null)
  const [formState, setFormState] = useState<WarehouseFormState>(initialFormState)
  const [formError, setFormError] = useState<AppError | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const { canManage } = usePagePermission()

  useEffect(() => {
    let isMounted = true

    const bootstrap = async () => {
      try {
        const list = await getWarehouseList()

        if (!isMounted) {
          return
        }

        setWarehouses(toTableRows(list))
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

  async function loadWarehouseList(query: WarehouseListQuery) {
    setIsLoading(true)
    setPageError(null)

    try {
      const list = await getWarehouseList(query)
      setWarehouses(toTableRows(list))
    } catch (error) {
      setPageError(normalizeError(error))
    } finally {
      setIsLoading(false)
    }
  }

  const columns: TreeTableColumn<WarehouseRow>[] = [
    {
      key: 'name',
      title: '仓库名称',
      minWidth: 240,
      render: (row) => (
        <div className="warehouse-page__name-cell">
          <strong>{row.warehouseName}</strong>
          <span>{row.warehouseType}</span>
        </div>
      ),
    },
    {
      key: 'code',
      title: '仓库编号',
      minWidth: 180,
      render: (row) => row.warehouseCode,
    },
    {
      key: 'manager',
      title: '负责人',
      minWidth: 120,
      render: (row) => row.managerName ?? '-',
    },
    {
      key: 'phone',
      title: '联系电话',
      minWidth: 140,
      render: (row) => row.contactPhone ?? '-',
    },
    {
      key: 'status',
      title: '状态',
      minWidth: 120,
      render: (row) => (
        <span
          className={`warehouse-page__status${
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
    ...(canManage
      ? [
          {
            key: 'actions',
            title: '操作',
            minWidth: 220,
            width: 220,
            sticky: 'right' as const,
            align: 'right' as const,
            render: (row: WarehouseRow) => (
              <div className="warehouse-page__row-actions">
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
      : []),
  ]

  const handleSearch = async () => {
    await loadWarehouseList(buildQueryParams(queryForm))
  }

  const handleReset = async () => {
    const resetState = {
      warehouseCode: '',
      warehouseName: '',
      warehouseType: '',
      status: '',
    }

    setQueryForm(resetState)
    await loadWarehouseList({})
  }

  const handleCreate = () => {
    setDialogMode('create')
    setEditingId(null)
    setFormError(null)
    setFormState(initialFormState)
    setIsDialogOpen(true)
  }

  const handleEdit = async (warehouseId: string) => {
    setDialogMode('edit')
    setEditingId(warehouseId)
    setFormError(null)
    setIsDialogOpen(true)
    setIsSubmitting(true)

    try {
      const detail = await getWarehouseDetail(warehouseId)
      setFormState(mapDetailToFormState(detail))
    } catch (error) {
      setFormError(normalizeError(error))
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleToggleStatus = async (row: WarehouseRow) => {
    const nextStatus = row.status === 1 ? 0 : 1
    const confirmed = window.confirm(
      `确定要将“${row.warehouseName}”${nextStatus === 1 ? '启用' : '停用'}吗？`,
    )

    if (!confirmed) {
      return
    }

    try {
      await updateWarehouseStatus(row.id, nextStatus)
      await loadWarehouseList(buildQueryParams(queryForm))
    } catch (error) {
      setPageError(normalizeError(error))
    }
  }

  const handleDelete = async (row: WarehouseRow) => {
    const confirmed = window.confirm(`确定要删除仓库“${row.warehouseName}”吗？`)

    if (!confirmed) {
      return
    }

    try {
      await deleteWarehouse(row.id)
      await loadWarehouseList(buildQueryParams(queryForm))
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
        await createWarehouse(payload)
      } else if (editingId) {
        await updateWarehouse(editingId, payload)
      }

      setIsDialogOpen(false)
      setFormState(initialFormState)
      await loadWarehouseList(buildQueryParams(queryForm))
    } catch (error) {
      setFormError(normalizeError(error))
    } finally {
      setIsSubmitting(false)
    }
  }

  const nameFieldError = getFieldError(formError?.fieldErrors, 'warehouseName')

  return (
    <div className="warehouse-page">
      {pageError ? (
        <div className="warehouse-page__message warehouse-page__message--error">
          <strong>{pageError.message}</strong>
          {pageError.traceId ? <span>traceId: {pageError.traceId}</span> : null}
        </div>
      ) : null}

      <section className="warehouse-page__search">
        <div className="warehouse-page__search-grid">
          <label className="warehouse-page__field">
            <span>仓库编号</span>
            <input
              type="text"
              placeholder="输入编号..."
              value={queryForm.warehouseCode}
              onChange={(event) =>
                setQueryForm((current) => ({
                  ...current,
                  warehouseCode: event.target.value,
                }))
              }
            />
          </label>

          <label className="warehouse-page__field">
            <span>仓库名称</span>
            <input
              type="text"
              placeholder="输入名称..."
              value={queryForm.warehouseName}
              onChange={(event) =>
                setQueryForm((current) => ({
                  ...current,
                  warehouseName: event.target.value,
                }))
              }
            />
          </label>

          <label className="warehouse-page__field">
            <span>仓库类型</span>
            <select
              value={queryForm.warehouseType}
              onChange={(event) =>
                setQueryForm((current) => ({
                  ...current,
                  warehouseType: event.target.value,
                }))
              }
            >
              <option value="">全部类型</option>
              {warehouseTypeOptions.map((warehouseType) => (
                <option key={warehouseType} value={warehouseType}>
                  {warehouseType}
                </option>
              ))}
            </select>
          </label>

          <label className="warehouse-page__field">
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

          <div className="warehouse-page__actions">
            <button type="button" className="is-ghost" onClick={() => void handleReset()}>
              重置
            </button>
            <button type="button" className="is-dark" onClick={() => void handleSearch()}>
              查询
            </button>
          </div>
        </div>
      </section>

      <section className="warehouse-page__toolbar">
        <div className="warehouse-page__toolbar-copy">
          <h3>仓库列表</h3>
          <p>维护仓库主数据，为库区、库位和后续出入库业务提供统一仓库维度。</p>
        </div>

        {canManage ? (
          <button type="button" className="warehouse-page__primary" onClick={handleCreate}>
            新增仓库
          </button>
        ) : null}
      </section>

      <section className="warehouse-page__table-shell">
        {isLoading ? (
          <div className="warehouse-page__loading">正在加载仓库数据...</div>
        ) : (
          <TreeDataTable
            data={warehouses}
            columns={columns}
            emptyText="暂无仓库数据"
          />
        )}
      </section>

      {isDialogOpen ? (
        <div className="warehouse-page__dialog-backdrop">
          <div className="warehouse-page__dialog">
            <div className="warehouse-page__dialog-header">
              <div>
                <h3>{dialogMode === 'create' ? '新增仓库' : '编辑仓库'}</h3>
                <p>维护仓库名称、类型、负责人和联系方式。</p>
              </div>
              <button
                type="button"
                className="warehouse-page__dialog-close"
                onClick={() => setIsDialogOpen(false)}
              >
                ×
              </button>
            </div>

            <form className="warehouse-page__dialog-form" onSubmit={handleDialogSubmit}>
              <label className="warehouse-page__field">
                <span>仓库编号</span>
                <input value={formState.warehouseCode} placeholder="系统自动生成" readOnly disabled />
                <small className="warehouse-page__field-hint">
                  {dialogMode === 'create' ? '新增时由系统自动生成编号' : '仓库编号创建后不可修改'}
                </small>
              </label>

              <label className="warehouse-page__field">
                <span>仓库名称</span>
                <input
                  value={formState.warehouseName}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      warehouseName: event.target.value,
                    }))
                  }
                />
                {nameFieldError ? (
                  <small className="warehouse-page__field-error">{nameFieldError}</small>
                ) : null}
              </label>

              <label className="warehouse-page__field">
                <span>仓库类型</span>
                <select
                  value={formState.warehouseType}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      warehouseType: event.target.value,
                    }))
                  }
                >
                  <option value="">请选择</option>
                  {warehouseTypeOptions.map((warehouseType) => (
                    <option key={warehouseType} value={warehouseType}>
                      {warehouseType}
                    </option>
                  ))}
                </select>
              </label>

              <label className="warehouse-page__field">
                <span>负责人</span>
                <input
                  value={formState.managerName}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      managerName: event.target.value,
                    }))
                  }
                />
              </label>

              <label className="warehouse-page__field">
                <span>联系电话</span>
                <input
                  value={formState.contactPhone}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      contactPhone: event.target.value,
                    }))
                  }
                />
              </label>

              <label className="warehouse-page__field">
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

              <label className="warehouse-page__field warehouse-page__field--full">
                <span>仓库地址</span>
                <input
                  value={formState.address}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      address: event.target.value,
                    }))
                  }
                />
              </label>

              <label className="warehouse-page__field">
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

              <label className="warehouse-page__field warehouse-page__field--full">
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
                <div className="warehouse-page__message warehouse-page__message--error">
                  <strong>{formError.message}</strong>
                  {formError.traceId ? <span>traceId: {formError.traceId}</span> : null}
                </div>
              ) : null}

              <div className="warehouse-page__dialog-actions">
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
  warehouseCode: string
  warehouseName: string
  warehouseType: string
  status: string
}): WarehouseListQuery {
  return {
    warehouseCode: queryForm.warehouseCode.trim() || undefined,
    warehouseName: queryForm.warehouseName.trim() || undefined,
    warehouseType: queryForm.warehouseType || undefined,
    status: queryForm.status ? Number(queryForm.status) : undefined,
  }
}

function mapDetailToFormState(detail: WarehouseDetail): WarehouseFormState {
  return {
    warehouseCode: detail.warehouseCode,
    warehouseName: detail.warehouseName,
    warehouseType: detail.warehouseType,
    managerName: detail.managerName ?? '',
    contactPhone: detail.contactPhone ?? '',
    address: detail.address ?? '',
    status: String(detail.status),
    sortOrder: String(detail.sortOrder),
    remarks: detail.remarks ?? '',
  }
}

function mapFormStateToPayload(formState: WarehouseFormState): WarehouseFormPayload {
  return {
    warehouseName: formState.warehouseName.trim(),
    warehouseType: formState.warehouseType.trim(),
    managerName: formState.managerName.trim() || null,
    contactPhone: formState.contactPhone.trim() || null,
    address: formState.address.trim() || null,
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

function toTableRows(warehouseList: WarehouseListItem[]): WarehouseRow[] {
  return warehouseList.map((item) => ({
    ...item,
    children: [],
  }))
}

export default WarehousePage
