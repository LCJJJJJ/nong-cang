import { type FormEvent, useEffect, useState } from 'react'

import { getFieldError, normalizeError, type AppError } from '../../api/errors'
import TreeDataTable, {
  type TreeTableColumn,
  type TreeTableRow,
} from '../../components/tree-data-table/TreeDataTable'
import {
  createSupplier,
  deleteSupplier,
  getSupplierDetail,
  getSupplierList,
  updateSupplier,
  updateSupplierStatus,
} from '../../features/supplier/api'
import type {
  SupplierDetail,
  SupplierFormPayload,
  SupplierListItem,
  SupplierListQuery,
} from '../../features/supplier/types'
import './SupplierPage.css'

type SupplierRow = TreeTableRow & SupplierListItem

interface SupplierFormState {
  supplierCode: string
  supplierName: string
  supplierType: string
  contactName: string
  contactPhone: string
  regionName: string
  address: string
  status: string
  sortOrder: string
  remarks: string
}

const initialFormState: SupplierFormState = {
  supplierCode: '',
  supplierName: '',
  supplierType: '',
  contactName: '',
  contactPhone: '',
  regionName: '',
  address: '',
  status: '1',
  sortOrder: '0',
  remarks: '',
}

const supplierTypeOptions = ['产地直供', '贸易商', '合作社', '加工企业']

function SupplierPage() {
  const [queryForm, setQueryForm] = useState({
    supplierCode: '',
    supplierName: '',
    contactName: '',
    status: '',
  })
  const [suppliers, setSuppliers] = useState<SupplierRow[]>([])
  const [pageError, setPageError] = useState<AppError | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  const [isDialogOpen, setIsDialogOpen] = useState(false)
  const [dialogMode, setDialogMode] = useState<'create' | 'edit'>('create')
  const [editingId, setEditingId] = useState<string | null>(null)
  const [formState, setFormState] = useState<SupplierFormState>(initialFormState)
  const [formError, setFormError] = useState<AppError | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)

  useEffect(() => {
    let isMounted = true

    const bootstrap = async () => {
      try {
        const list = await getSupplierList()

        if (!isMounted) {
          return
        }

        setSuppliers(toTableRows(list))
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

  async function loadSupplierList(query: SupplierListQuery) {
    setIsLoading(true)
    setPageError(null)

    try {
      const list = await getSupplierList(query)
      setSuppliers(toTableRows(list))
    } catch (error) {
      setPageError(normalizeError(error))
    } finally {
      setIsLoading(false)
    }
  }

  const columns: TreeTableColumn<SupplierRow>[] = [
    {
      key: 'name',
      title: '供应商名称',
      minWidth: 260,
      render: (row) => (
        <div className="supplier-page__name-cell">
          <strong>{row.supplierName}</strong>
          <span>{row.supplierType}</span>
        </div>
      ),
    },
    {
      key: 'code',
      title: '供应商编号',
      minWidth: 180,
      render: (row) => row.supplierCode,
    },
    {
      key: 'contact',
      title: '联系人',
      minWidth: 120,
      render: (row) => row.contactName ?? '-',
    },
    {
      key: 'phone',
      title: '联系电话',
      minWidth: 140,
      render: (row) => row.contactPhone ?? '-',
    },
    {
      key: 'region',
      title: '所在地区',
      minWidth: 180,
      render: (row) => row.regionName ?? '-',
    },
    {
      key: 'status',
      title: '状态',
      minWidth: 120,
      render: (row) => (
        <span
          className={`supplier-page__status${
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
        <div className="supplier-page__row-actions">
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
    await loadSupplierList(buildQueryParams(queryForm))
  }

  const handleReset = async () => {
    const resetState = {
      supplierCode: '',
      supplierName: '',
      contactName: '',
      status: '',
    }

    setQueryForm(resetState)
    await loadSupplierList({})
  }

  const handleCreate = () => {
    setDialogMode('create')
    setEditingId(null)
    setFormError(null)
    setFormState(initialFormState)
    setIsDialogOpen(true)
  }

  const handleEdit = async (supplierId: string) => {
    setDialogMode('edit')
    setEditingId(supplierId)
    setFormError(null)
    setIsDialogOpen(true)
    setIsSubmitting(true)

    try {
      const detail = await getSupplierDetail(supplierId)
      setFormState(mapDetailToFormState(detail))
    } catch (error) {
      setFormError(normalizeError(error))
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleToggleStatus = async (row: SupplierRow) => {
    const nextStatus = row.status === 1 ? 0 : 1
    const confirmed = window.confirm(
      `确定要将“${row.supplierName}”${nextStatus === 1 ? '启用' : '停用'}吗？`,
    )

    if (!confirmed) {
      return
    }

    try {
      await updateSupplierStatus(row.id, nextStatus)
      await loadSupplierList(buildQueryParams(queryForm))
    } catch (error) {
      setPageError(normalizeError(error))
    }
  }

  const handleDelete = async (row: SupplierRow) => {
    const confirmed = window.confirm(`确定要删除供应商“${row.supplierName}”吗？`)

    if (!confirmed) {
      return
    }

    try {
      await deleteSupplier(row.id)
      await loadSupplierList(buildQueryParams(queryForm))
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
        await createSupplier(payload)
      } else if (editingId) {
        await updateSupplier(editingId, payload)
      }

      setIsDialogOpen(false)
      setFormState(initialFormState)
      await loadSupplierList(buildQueryParams(queryForm))
    } catch (error) {
      setFormError(normalizeError(error))
    } finally {
      setIsSubmitting(false)
    }
  }

  const nameFieldError = getFieldError(formError?.fieldErrors, 'supplierName')

  return (
    <div className="supplier-page">
      {pageError ? (
        <div className="supplier-page__message supplier-page__message--error">
          <strong>{pageError.message}</strong>
          {pageError.traceId ? <span>traceId: {pageError.traceId}</span> : null}
        </div>
      ) : null}

      <section className="supplier-page__search">
        <div className="supplier-page__search-grid">
          <label className="supplier-page__field">
            <span>供应商编号</span>
            <input
              type="text"
              placeholder="输入编号..."
              value={queryForm.supplierCode}
              onChange={(event) =>
                setQueryForm((current) => ({
                  ...current,
                  supplierCode: event.target.value,
                }))
              }
            />
          </label>

          <label className="supplier-page__field">
            <span>供应商名称</span>
            <input
              type="text"
              placeholder="输入名称..."
              value={queryForm.supplierName}
              onChange={(event) =>
                setQueryForm((current) => ({
                  ...current,
                  supplierName: event.target.value,
                }))
              }
            />
          </label>

          <label className="supplier-page__field">
            <span>联系人</span>
            <input
              type="text"
              placeholder="输入联系人..."
              value={queryForm.contactName}
              onChange={(event) =>
                setQueryForm((current) => ({
                  ...current,
                  contactName: event.target.value,
                }))
              }
            />
          </label>

          <label className="supplier-page__field">
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

          <div className="supplier-page__actions">
            <button type="button" className="is-ghost" onClick={() => void handleReset()}>
              重置
            </button>
            <button type="button" className="is-dark" onClick={() => void handleSearch()}>
              查询
            </button>
          </div>
        </div>
      </section>

      <section className="supplier-page__toolbar">
        <div className="supplier-page__toolbar-copy">
          <h3>供应商列表</h3>
          <p>维护入库业务使用的供应商主数据，为供应来源和到货协同提供统一对象口径。</p>
        </div>

        <button type="button" className="supplier-page__primary" onClick={handleCreate}>
          新增供应商
        </button>
      </section>

      <section className="supplier-page__table-shell">
        {isLoading ? (
          <div className="supplier-page__loading">正在加载供应商数据...</div>
        ) : (
          <TreeDataTable data={suppliers} columns={columns} emptyText="暂无供应商数据" />
        )}
      </section>

      {isDialogOpen ? (
        <div className="supplier-page__dialog-backdrop">
          <div className="supplier-page__dialog">
            <div className="supplier-page__dialog-header">
              <div>
                <h3>{dialogMode === 'create' ? '新增供应商' : '编辑供应商'}</h3>
                <p>维护供应商基本信息、联系人和地区地址。</p>
              </div>
              <button
                type="button"
                className="supplier-page__dialog-close"
                onClick={() => setIsDialogOpen(false)}
              >
                ×
              </button>
            </div>

            <form className="supplier-page__dialog-form" onSubmit={handleDialogSubmit}>
              <label className="supplier-page__field">
                <span>供应商编号</span>
                <input value={formState.supplierCode} placeholder="系统自动生成" readOnly disabled />
                <small className="supplier-page__field-hint">
                  {dialogMode === 'create' ? '新增时由系统自动生成编号' : '供应商编号创建后不可修改'}
                </small>
              </label>

              <label className="supplier-page__field">
                <span>供应商名称</span>
                <input
                  value={formState.supplierName}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      supplierName: event.target.value,
                    }))
                  }
                />
                {nameFieldError ? (
                  <small className="supplier-page__field-error">{nameFieldError}</small>
                ) : null}
              </label>

              <label className="supplier-page__field">
                <span>供应商类型</span>
                <select
                  value={formState.supplierType}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      supplierType: event.target.value,
                    }))
                  }
                >
                  <option value="">请选择</option>
                  {supplierTypeOptions.map((supplierType) => (
                    <option key={supplierType} value={supplierType}>
                      {supplierType}
                    </option>
                  ))}
                </select>
              </label>

              <label className="supplier-page__field">
                <span>联系人</span>
                <input
                  value={formState.contactName}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      contactName: event.target.value,
                    }))
                  }
                />
              </label>

              <label className="supplier-page__field">
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

              <label className="supplier-page__field">
                <span>所在地区</span>
                <input
                  value={formState.regionName}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      regionName: event.target.value,
                    }))
                  }
                />
              </label>

              <label className="supplier-page__field supplier-page__field--full">
                <span>详细地址</span>
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

              <label className="supplier-page__field">
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

              <label className="supplier-page__field">
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

              <label className="supplier-page__field supplier-page__field--full">
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
                <div className="supplier-page__message supplier-page__message--error">
                  <strong>{formError.message}</strong>
                  {formError.traceId ? <span>traceId: {formError.traceId}</span> : null}
                </div>
              ) : null}

              <div className="supplier-page__dialog-actions">
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
  supplierCode: string
  supplierName: string
  contactName: string
  status: string
}): SupplierListQuery {
  return {
    supplierCode: queryForm.supplierCode.trim() || undefined,
    supplierName: queryForm.supplierName.trim() || undefined,
    contactName: queryForm.contactName.trim() || undefined,
    status: queryForm.status ? Number(queryForm.status) : undefined,
  }
}

function mapDetailToFormState(detail: SupplierDetail): SupplierFormState {
  return {
    supplierCode: detail.supplierCode,
    supplierName: detail.supplierName,
    supplierType: detail.supplierType,
    contactName: detail.contactName ?? '',
    contactPhone: detail.contactPhone ?? '',
    regionName: detail.regionName ?? '',
    address: detail.address ?? '',
    status: String(detail.status),
    sortOrder: String(detail.sortOrder),
    remarks: detail.remarks ?? '',
  }
}

function mapFormStateToPayload(formState: SupplierFormState): SupplierFormPayload {
  return {
    supplierName: formState.supplierName.trim(),
    supplierType: formState.supplierType.trim(),
    contactName: formState.contactName.trim() || null,
    contactPhone: formState.contactPhone.trim() || null,
    regionName: formState.regionName.trim() || null,
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

function toTableRows(supplierList: SupplierListItem[]): SupplierRow[] {
  return supplierList.map((item) => ({
    ...item,
    children: [],
  }))
}

export default SupplierPage
