import { type FormEvent, useEffect, useState } from 'react'

import { getFieldError, normalizeError, type AppError } from '../../api/errors'
import TreeDataTable, {
  type TreeTableColumn,
  type TreeTableRow,
} from '../../components/tree-data-table/TreeDataTable'
import {
  createProductUnit,
  deleteProductUnit,
  getProductUnitDetail,
  getProductUnitList,
  updateProductUnit,
  updateProductUnitStatus,
} from '../../features/productunit/api'
import type {
  ProductUnitDetail,
  ProductUnitFormPayload,
  ProductUnitListItem,
  ProductUnitListQuery,
} from '../../features/productunit/types'
import './ProductUnitPage.css'

type ProductUnitRow = TreeTableRow & ProductUnitListItem

interface ProductUnitFormState {
  unitCode: string
  unitName: string
  unitSymbol: string
  unitType: string
  precisionDigits: string
  status: string
  sortOrder: string
  remarks: string
}

const initialFormState: ProductUnitFormState = {
  unitCode: '',
  unitName: '',
  unitSymbol: '',
  unitType: '',
  precisionDigits: '0',
  status: '1',
  sortOrder: '0',
  remarks: '',
}

const unitTypeOptions = ['重量', '包装', '数量', '体积']

function ProductUnitPage() {
  const [queryForm, setQueryForm] = useState({
    unitCode: '',
    unitName: '',
    unitType: '',
    status: '',
  })
  const [productUnits, setProductUnits] = useState<ProductUnitRow[]>([])
  const [pageError, setPageError] = useState<AppError | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  const [isDialogOpen, setIsDialogOpen] = useState(false)
  const [dialogMode, setDialogMode] = useState<'create' | 'edit'>('create')
  const [editingId, setEditingId] = useState<string | null>(null)
  const [formState, setFormState] = useState<ProductUnitFormState>(initialFormState)
  const [formError, setFormError] = useState<AppError | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)

  useEffect(() => {
    let isMounted = true

    const bootstrap = async () => {
      try {
        const list = await getProductUnitList()

        if (!isMounted) {
          return
        }

        setProductUnits(toTableRows(list))
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

  async function loadProductUnitList(query: ProductUnitListQuery) {
    setIsLoading(true)
    setPageError(null)

    try {
      const list = await getProductUnitList(query)
      setProductUnits(toTableRows(list))
    } catch (error) {
      setPageError(normalizeError(error))
    } finally {
      setIsLoading(false)
    }
  }

  const columns: TreeTableColumn<ProductUnitRow>[] = [
    {
      key: 'name',
      title: '单位名称',
      minWidth: 220,
      render: (row) => (
        <div className="product-unit-page__name-cell">
          <strong>{row.unitName}</strong>
          <span>{row.unitSymbol}</span>
        </div>
      ),
    },
    {
      key: 'code',
      title: '单位编号',
      minWidth: 180,
      render: (row) => row.unitCode,
    },
    {
      key: 'type',
      title: '单位类型',
      minWidth: 120,
      render: (row) => row.unitType,
    },
    {
      key: 'precision',
      title: '精度位数',
      minWidth: 110,
      align: 'center',
      render: (row) => row.precisionDigits,
    },
    {
      key: 'status',
      title: '状态',
      minWidth: 120,
      render: (row) => (
        <span
          className={`product-unit-page__status${
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
        <div className="product-unit-page__row-actions">
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
    await loadProductUnitList(buildQueryParams(queryForm))
  }

  const handleReset = async () => {
    const resetState = {
      unitCode: '',
      unitName: '',
      unitType: '',
      status: '',
    }

    setQueryForm(resetState)
    await loadProductUnitList({})
  }

  const handleCreate = () => {
    setDialogMode('create')
    setEditingId(null)
    setFormError(null)
    setFormState(initialFormState)
    setIsDialogOpen(true)
  }

  const handleEdit = async (productUnitId: string) => {
    setDialogMode('edit')
    setEditingId(productUnitId)
    setFormError(null)
    setIsDialogOpen(true)
    setIsSubmitting(true)

    try {
      const detail = await getProductUnitDetail(productUnitId)
      setFormState(mapDetailToFormState(detail))
    } catch (error) {
      setFormError(normalizeError(error))
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleToggleStatus = async (row: ProductUnitRow) => {
    const nextStatus = row.status === 1 ? 0 : 1
    const confirmed = window.confirm(
      `确定要将“${row.unitName}”${nextStatus === 1 ? '启用' : '停用'}吗？`,
    )

    if (!confirmed) {
      return
    }

    try {
      await updateProductUnitStatus(row.id, nextStatus)
      await loadProductUnitList(buildQueryParams(queryForm))
    } catch (error) {
      setPageError(normalizeError(error))
    }
  }

  const handleDelete = async (row: ProductUnitRow) => {
    const confirmed = window.confirm(`确定要删除产品单位“${row.unitName}”吗？`)

    if (!confirmed) {
      return
    }

    try {
      await deleteProductUnit(row.id)
      await loadProductUnitList(buildQueryParams(queryForm))
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
        await createProductUnit(payload)
      } else if (editingId) {
        await updateProductUnit(editingId, payload)
      }

      setIsDialogOpen(false)
      setFormState(initialFormState)
      await loadProductUnitList(buildQueryParams(queryForm))
    } catch (error) {
      setFormError(normalizeError(error))
    } finally {
      setIsSubmitting(false)
    }
  }

  const nameFieldError = getFieldError(formError?.fieldErrors, 'unitName')

  return (
    <div className="product-unit-page">
      {pageError ? (
        <div className="product-unit-page__message product-unit-page__message--error">
          <strong>{pageError.message}</strong>
          {pageError.traceId ? <span>traceId: {pageError.traceId}</span> : null}
        </div>
      ) : null}

      <section className="product-unit-page__search">
        <div className="product-unit-page__search-grid">
          <label className="product-unit-page__field">
            <span>单位编号</span>
            <input
              type="text"
              placeholder="输入编号..."
              value={queryForm.unitCode}
              onChange={(event) =>
                setQueryForm((current) => ({
                  ...current,
                  unitCode: event.target.value,
                }))
              }
            />
          </label>

          <label className="product-unit-page__field">
            <span>单位名称</span>
            <input
              type="text"
              placeholder="输入名称..."
              value={queryForm.unitName}
              onChange={(event) =>
                setQueryForm((current) => ({
                  ...current,
                  unitName: event.target.value,
                }))
              }
            />
          </label>

          <label className="product-unit-page__field">
            <span>单位类型</span>
            <select
              value={queryForm.unitType}
              onChange={(event) =>
                setQueryForm((current) => ({
                  ...current,
                  unitType: event.target.value,
                }))
              }
            >
              <option value="">全部类型</option>
              {unitTypeOptions.map((unitType) => (
                <option key={unitType} value={unitType}>
                  {unitType}
                </option>
              ))}
            </select>
          </label>

          <label className="product-unit-page__field">
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

          <div className="product-unit-page__actions">
            <button type="button" className="is-ghost" onClick={() => void handleReset()}>
              重置
            </button>
            <button type="button" className="is-dark" onClick={() => void handleSearch()}>
              查询
            </button>
          </div>
        </div>
      </section>

      <section className="product-unit-page__toolbar">
        <div className="product-unit-page__toolbar-copy">
          <h3>产品单位列表</h3>
          <p>维护农产品通用计量单位，为产品档案和业务单据提供统一口径。</p>
        </div>

        <button type="button" className="product-unit-page__primary" onClick={handleCreate}>
          新增产品单位
        </button>
      </section>

      <section className="product-unit-page__table-shell">
        {isLoading ? (
          <div className="product-unit-page__loading">正在加载产品单位...</div>
        ) : (
          <TreeDataTable
            data={productUnits}
            columns={columns}
            emptyText="暂无产品单位数据"
          />
        )}
      </section>

      {isDialogOpen ? (
        <div className="product-unit-page__dialog-backdrop">
          <div className="product-unit-page__dialog">
            <div className="product-unit-page__dialog-header">
              <div>
                <h3>{dialogMode === 'create' ? '新增产品单位' : '编辑产品单位'}</h3>
                <p>维护计量单位名称、符号、类型与精度规则。</p>
              </div>
              <button
                type="button"
                className="product-unit-page__dialog-close"
                onClick={() => setIsDialogOpen(false)}
              >
                ×
              </button>
            </div>

            <form className="product-unit-page__dialog-form" onSubmit={handleDialogSubmit}>
              <label className="product-unit-page__field">
                <span>单位编号</span>
                <input value={formState.unitCode} placeholder="系统自动生成" readOnly disabled />
                <small className="product-unit-page__field-hint">
                  {dialogMode === 'create' ? '新增时由系统自动生成编号' : '单位编号创建后不可修改'}
                </small>
              </label>

              <label className="product-unit-page__field">
                <span>单位名称</span>
                <input
                  value={formState.unitName}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      unitName: event.target.value,
                    }))
                  }
                />
                {nameFieldError ? (
                  <small className="product-unit-page__field-error">{nameFieldError}</small>
                ) : null}
              </label>

              <label className="product-unit-page__field">
                <span>单位符号</span>
                <input
                  value={formState.unitSymbol}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      unitSymbol: event.target.value,
                    }))
                  }
                />
              </label>

              <label className="product-unit-page__field">
                <span>单位类型</span>
                <select
                  value={formState.unitType}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      unitType: event.target.value,
                    }))
                  }
                >
                  <option value="">请选择</option>
                  {unitTypeOptions.map((unitType) => (
                    <option key={unitType} value={unitType}>
                      {unitType}
                    </option>
                  ))}
                </select>
              </label>

              <label className="product-unit-page__field">
                <span>精度位数</span>
                <input
                  type="number"
                  min="0"
                  value={formState.precisionDigits}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      precisionDigits: event.target.value,
                    }))
                  }
                />
              </label>

              <label className="product-unit-page__field">
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

              <label className="product-unit-page__field">
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

              <label className="product-unit-page__field product-unit-page__field--full">
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
                <div className="product-unit-page__message product-unit-page__message--error">
                  <strong>{formError.message}</strong>
                  {formError.traceId ? <span>traceId: {formError.traceId}</span> : null}
                </div>
              ) : null}

              <div className="product-unit-page__dialog-actions">
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
  unitCode: string
  unitName: string
  unitType: string
  status: string
}): ProductUnitListQuery {
  return {
    unitCode: queryForm.unitCode.trim() || undefined,
    unitName: queryForm.unitName.trim() || undefined,
    unitType: queryForm.unitType || undefined,
    status: queryForm.status ? Number(queryForm.status) : undefined,
  }
}

function mapDetailToFormState(detail: ProductUnitDetail): ProductUnitFormState {
  return {
    unitCode: detail.unitCode,
    unitName: detail.unitName,
    unitSymbol: detail.unitSymbol,
    unitType: detail.unitType,
    precisionDigits: String(detail.precisionDigits),
    status: String(detail.status),
    sortOrder: String(detail.sortOrder),
    remarks: detail.remarks ?? '',
  }
}

function mapFormStateToPayload(formState: ProductUnitFormState): ProductUnitFormPayload {
  return {
    unitName: formState.unitName.trim(),
    unitSymbol: formState.unitSymbol.trim(),
    unitType: formState.unitType.trim(),
    precisionDigits: Number(formState.precisionDigits),
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

function toTableRows(productUnitList: ProductUnitListItem[]): ProductUnitRow[] {
  return productUnitList.map((item) => ({
    ...item,
    children: [],
  }))
}

export default ProductUnitPage
