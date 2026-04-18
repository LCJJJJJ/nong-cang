import { type FormEvent, useEffect, useState } from 'react'

import { getFieldError, normalizeError, type AppError } from '../../api/errors'
import TreeDataTable, {
  type TreeTableColumn,
  type TreeTableRow,
} from '../../components/tree-data-table/TreeDataTable'
import {
  createProductOrigin,
  deleteProductOrigin,
  getProductOriginDetail,
  getProductOriginList,
  updateProductOrigin,
  updateProductOriginStatus,
} from '../../features/productorigin/api'
import type {
  ProductOriginDetail,
  ProductOriginFormPayload,
  ProductOriginListItem,
  ProductOriginListQuery,
} from '../../features/productorigin/types'
import './ProductOriginPage.css'

type ProductOriginRow = TreeTableRow & ProductOriginListItem

interface ProductOriginFormState {
  originCode: string
  originName: string
  countryName: string
  provinceName: string
  cityName: string
  status: string
  sortOrder: string
  remarks: string
}

const initialFormState: ProductOriginFormState = {
  originCode: '',
  originName: '',
  countryName: '中国',
  provinceName: '',
  cityName: '',
  status: '1',
  sortOrder: '0',
  remarks: '',
}

function ProductOriginPage() {
  const [queryForm, setQueryForm] = useState({
    originCode: '',
    originName: '',
    provinceName: '',
    status: '',
  })
  const [productOrigins, setProductOrigins] = useState<ProductOriginRow[]>([])
  const [pageError, setPageError] = useState<AppError | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  const [isDialogOpen, setIsDialogOpen] = useState(false)
  const [dialogMode, setDialogMode] = useState<'create' | 'edit'>('create')
  const [editingId, setEditingId] = useState<string | null>(null)
  const [formState, setFormState] = useState<ProductOriginFormState>(initialFormState)
  const [formError, setFormError] = useState<AppError | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)

  useEffect(() => {
    let isMounted = true

    const bootstrap = async () => {
      try {
        const list = await getProductOriginList()

        if (!isMounted) {
          return
        }

        setProductOrigins(toTableRows(list))
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

  async function loadProductOriginList(query: ProductOriginListQuery) {
    setIsLoading(true)
    setPageError(null)

    try {
      const list = await getProductOriginList(query)
      setProductOrigins(toTableRows(list))
    } catch (error) {
      setPageError(normalizeError(error))
    } finally {
      setIsLoading(false)
    }
  }

  const columns: TreeTableColumn<ProductOriginRow>[] = [
    {
      key: 'name',
      title: '产地名称',
      minWidth: 240,
      render: (row) => (
        <div className="product-origin-page__name-cell">
          <strong>{row.originName}</strong>
          <span>{formatLocation(row)}</span>
        </div>
      ),
    },
    {
      key: 'code',
      title: '产地编号',
      minWidth: 180,
      render: (row) => row.originCode,
    },
    {
      key: 'province',
      title: '省份',
      minWidth: 140,
      render: (row) => row.provinceName,
    },
    {
      key: 'city',
      title: '城市',
      minWidth: 140,
      render: (row) => row.cityName ?? '-',
    },
    {
      key: 'status',
      title: '状态',
      minWidth: 120,
      render: (row) => (
        <span
          className={`product-origin-page__status${
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
        <div className="product-origin-page__row-actions">
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
    await loadProductOriginList(buildQueryParams(queryForm))
  }

  const handleReset = async () => {
    const resetState = {
      originCode: '',
      originName: '',
      provinceName: '',
      status: '',
    }

    setQueryForm(resetState)
    await loadProductOriginList({})
  }

  const handleCreate = () => {
    setDialogMode('create')
    setEditingId(null)
    setFormError(null)
    setFormState(initialFormState)
    setIsDialogOpen(true)
  }

  const handleEdit = async (productOriginId: string) => {
    setDialogMode('edit')
    setEditingId(productOriginId)
    setFormError(null)
    setIsDialogOpen(true)
    setIsSubmitting(true)

    try {
      const detail = await getProductOriginDetail(productOriginId)
      setFormState(mapDetailToFormState(detail))
    } catch (error) {
      setFormError(normalizeError(error))
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleToggleStatus = async (row: ProductOriginRow) => {
    const nextStatus = row.status === 1 ? 0 : 1
    const confirmed = window.confirm(
      `确定要将“${row.originName}”${nextStatus === 1 ? '启用' : '停用'}吗？`,
    )

    if (!confirmed) {
      return
    }

    try {
      await updateProductOriginStatus(row.id, nextStatus)
      await loadProductOriginList(buildQueryParams(queryForm))
    } catch (error) {
      setPageError(normalizeError(error))
    }
  }

  const handleDelete = async (row: ProductOriginRow) => {
    const confirmed = window.confirm(`确定要删除产地“${row.originName}”吗？`)

    if (!confirmed) {
      return
    }

    try {
      await deleteProductOrigin(row.id)
      await loadProductOriginList(buildQueryParams(queryForm))
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
        await createProductOrigin(payload)
      } else if (editingId) {
        await updateProductOrigin(editingId, payload)
      }

      setIsDialogOpen(false)
      setFormState(initialFormState)
      await loadProductOriginList(buildQueryParams(queryForm))
    } catch (error) {
      setFormError(normalizeError(error))
    } finally {
      setIsSubmitting(false)
    }
  }

  const nameFieldError = getFieldError(formError?.fieldErrors, 'originName')

  return (
    <div className="product-origin-page">
      {pageError ? (
        <div className="product-origin-page__message product-origin-page__message--error">
          <strong>{pageError.message}</strong>
          {pageError.traceId ? <span>traceId: {pageError.traceId}</span> : null}
        </div>
      ) : null}

      <section className="product-origin-page__search">
        <div className="product-origin-page__search-grid">
          <label className="product-origin-page__field">
            <span>产地编号</span>
            <input
              type="text"
              placeholder="输入编号..."
              value={queryForm.originCode}
              onChange={(event) =>
                setQueryForm((current) => ({
                  ...current,
                  originCode: event.target.value,
                }))
              }
            />
          </label>

          <label className="product-origin-page__field">
            <span>产地名称</span>
            <input
              type="text"
              placeholder="输入名称..."
              value={queryForm.originName}
              onChange={(event) =>
                setQueryForm((current) => ({
                  ...current,
                  originName: event.target.value,
                }))
              }
            />
          </label>

          <label className="product-origin-page__field">
            <span>省份名称</span>
            <input
              type="text"
              placeholder="输入省份..."
              value={queryForm.provinceName}
              onChange={(event) =>
                setQueryForm((current) => ({
                  ...current,
                  provinceName: event.target.value,
                }))
              }
            />
          </label>

          <label className="product-origin-page__field">
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

          <div className="product-origin-page__actions">
            <button type="button" className="is-ghost" onClick={() => void handleReset()}>
              重置
            </button>
            <button type="button" className="is-dark" onClick={() => void handleSearch()}>
              查询
            </button>
          </div>
        </div>
      </section>

      <section className="product-origin-page__toolbar">
        <div className="product-origin-page__toolbar-copy">
          <h3>产地信息列表</h3>
          <p>维护标准产地口径，为产品档案、追溯和统计分析提供一致数据来源。</p>
        </div>

        <button type="button" className="product-origin-page__primary" onClick={handleCreate}>
          新增产地信息
        </button>
      </section>

      <section className="product-origin-page__table-shell">
        {isLoading ? (
          <div className="product-origin-page__loading">正在加载产地信息...</div>
        ) : (
          <TreeDataTable
            data={productOrigins}
            columns={columns}
            emptyText="暂无产地信息数据"
          />
        )}
      </section>

      {isDialogOpen ? (
        <div className="product-origin-page__dialog-backdrop">
          <div className="product-origin-page__dialog">
            <div className="product-origin-page__dialog-header">
              <div>
                <h3>{dialogMode === 'create' ? '新增产地信息' : '编辑产地信息'}</h3>
                <p>维护标准产地名称与行政区域信息。</p>
              </div>
              <button
                type="button"
                className="product-origin-page__dialog-close"
                onClick={() => setIsDialogOpen(false)}
              >
                ×
              </button>
            </div>

            <form className="product-origin-page__dialog-form" onSubmit={handleDialogSubmit}>
              <label className="product-origin-page__field">
                <span>产地编号</span>
                <input value={formState.originCode} placeholder="系统自动生成" readOnly disabled />
                <small className="product-origin-page__field-hint">
                  {dialogMode === 'create' ? '新增时由系统自动生成编号' : '产地编号创建后不可修改'}
                </small>
              </label>

              <label className="product-origin-page__field">
                <span>产地名称</span>
                <input
                  value={formState.originName}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      originName: event.target.value,
                    }))
                  }
                />
                {nameFieldError ? (
                  <small className="product-origin-page__field-error">{nameFieldError}</small>
                ) : null}
              </label>

              <label className="product-origin-page__field">
                <span>国家名称</span>
                <input
                  value={formState.countryName}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      countryName: event.target.value,
                    }))
                  }
                />
              </label>

              <label className="product-origin-page__field">
                <span>省份名称</span>
                <input
                  value={formState.provinceName}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      provinceName: event.target.value,
                    }))
                  }
                />
              </label>

              <label className="product-origin-page__field">
                <span>城市名称</span>
                <input
                  value={formState.cityName}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      cityName: event.target.value,
                    }))
                  }
                />
              </label>

              <label className="product-origin-page__field">
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

              <label className="product-origin-page__field">
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

              <label className="product-origin-page__field product-origin-page__field--full">
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
                <div className="product-origin-page__message product-origin-page__message--error">
                  <strong>{formError.message}</strong>
                  {formError.traceId ? <span>traceId: {formError.traceId}</span> : null}
                </div>
              ) : null}

              <div className="product-origin-page__dialog-actions">
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
  originCode: string
  originName: string
  provinceName: string
  status: string
}): ProductOriginListQuery {
  return {
    originCode: queryForm.originCode.trim() || undefined,
    originName: queryForm.originName.trim() || undefined,
    provinceName: queryForm.provinceName.trim() || undefined,
    status: queryForm.status ? Number(queryForm.status) : undefined,
  }
}

function mapDetailToFormState(detail: ProductOriginDetail): ProductOriginFormState {
  return {
    originCode: detail.originCode,
    originName: detail.originName,
    countryName: detail.countryName,
    provinceName: detail.provinceName,
    cityName: detail.cityName ?? '',
    status: String(detail.status),
    sortOrder: String(detail.sortOrder),
    remarks: detail.remarks ?? '',
  }
}

function mapFormStateToPayload(
  formState: ProductOriginFormState,
): ProductOriginFormPayload {
  return {
    originName: formState.originName.trim(),
    countryName: formState.countryName.trim(),
    provinceName: formState.provinceName.trim(),
    cityName: formState.cityName.trim() || null,
    status: Number(formState.status),
    sortOrder: Number(formState.sortOrder),
    remarks: formState.remarks.trim() || null,
  }
}

function formatLocation(row: ProductOriginRow) {
  return row.cityName ? `${row.provinceName} / ${row.cityName}` : row.provinceName
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

function toTableRows(productOriginList: ProductOriginListItem[]): ProductOriginRow[] {
  return productOriginList.map((item) => ({
    ...item,
    children: [],
  }))
}

export default ProductOriginPage
