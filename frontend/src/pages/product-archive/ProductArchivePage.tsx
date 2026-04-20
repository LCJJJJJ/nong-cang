import { type FormEvent, useEffect, useMemo, useState } from 'react'

import { getFieldError, normalizeError, type AppError } from '../../api/errors'
import TreeDataTable, {
  type TreeTableColumn,
  type TreeTableRow,
} from '../../components/tree-data-table/TreeDataTable'
import { getCategoryOptions } from '../../features/category/api'
import type { CategoryOption } from '../../features/category/types'
import {
  createProductArchive,
  deleteProductArchive,
  getProductArchiveDetail,
  getProductArchiveList,
  updateProductArchive,
  updateProductArchiveStatus,
} from '../../features/productarchive/api'
import type {
  ProductArchiveDetail,
  ProductArchiveFormPayload,
  ProductArchiveListItem,
  ProductArchiveListQuery,
} from '../../features/productarchive/types'
import { usePagePermission } from '../../features/auth/usePagePermission'
import { getProductOriginOptions } from '../../features/productorigin/api'
import type { ProductOriginOption } from '../../features/productorigin/types'
import { getProductUnitOptions } from '../../features/productunit/api'
import type { ProductUnitOption } from '../../features/productunit/types'
import { getQualityGradeOptions } from '../../features/qualitygrade/api'
import type { QualityGradeOption } from '../../features/qualitygrade/types'
import { getStorageConditionOptions } from '../../features/storagecondition/api'
import type { StorageConditionOption } from '../../features/storagecondition/types'
import './ProductArchivePage.css'

type ProductArchiveRow = TreeTableRow & ProductArchiveListItem

interface ProductArchiveFormState {
  productCode: string
  productName: string
  productSpecification: string
  categoryId: string
  unitId: string
  originId: string
  storageConditionId: string
  shelfLifeDays: string
  warningDays: string
  qualityGradeId: string
  status: string
  sortOrder: string
  remarks: string
}

const initialFormState: ProductArchiveFormState = {
  productCode: '',
  productName: '',
  productSpecification: '',
  categoryId: '',
  unitId: '',
  originId: '',
  storageConditionId: '',
  shelfLifeDays: '',
  warningDays: '0',
  qualityGradeId: '',
  status: '1',
  sortOrder: '0',
  remarks: '',
}

function ProductArchivePage() {
  const [queryForm, setQueryForm] = useState({
    productCode: '',
    productName: '',
    categoryId: '',
    status: '',
  })
  const [productArchives, setProductArchives] = useState<ProductArchiveRow[]>([])
  const [categoryOptions, setCategoryOptions] = useState<CategoryOption[]>([])
  const [productUnitOptions, setProductUnitOptions] = useState<ProductUnitOption[]>([])
  const [productOriginOptions, setProductOriginOptions] = useState<ProductOriginOption[]>([])
  const [storageConditionOptions, setStorageConditionOptions] = useState<
    StorageConditionOption[]
  >([])
  const [qualityGradeOptions, setQualityGradeOptions] = useState<QualityGradeOption[]>(
    [],
  )
  const [pageError, setPageError] = useState<AppError | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  const [isDialogOpen, setIsDialogOpen] = useState(false)
  const [dialogMode, setDialogMode] = useState<'create' | 'edit'>('create')
  const [editingId, setEditingId] = useState<string | null>(null)
  const [formState, setFormState] = useState<ProductArchiveFormState>(initialFormState)
  const [formError, setFormError] = useState<AppError | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const { canManage } = usePagePermission()

  const flattenedCategoryOptions = useMemo(
    () => flattenCategoryOptions(categoryOptions),
    [categoryOptions],
  )

  useEffect(() => {
    let isMounted = true

    const bootstrap = async () => {
      try {
        const [list, categories, units, origins, storageConditions, qualityGrades] =
          await Promise.all([
            getProductArchiveList(),
            getCategoryOptions(),
            getProductUnitOptions(),
            getProductOriginOptions(),
            getStorageConditionOptions(),
            getQualityGradeOptions(),
          ])

        if (!isMounted) {
          return
        }

        setProductArchives(toTableRows(list))
        setCategoryOptions(categories)
        setProductUnitOptions(units)
        setProductOriginOptions(origins)
        setStorageConditionOptions(storageConditions)
        setQualityGradeOptions(qualityGrades)
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

  async function loadProductArchiveList(query: ProductArchiveListQuery) {
    setIsLoading(true)
    setPageError(null)

    try {
      const list = await getProductArchiveList(query)
      setProductArchives(toTableRows(list))
    } catch (error) {
      setPageError(normalizeError(error))
    } finally {
      setIsLoading(false)
    }
  }

  const columns: TreeTableColumn<ProductArchiveRow>[] = [
    {
      key: 'name',
      title: '产品名称',
      minWidth: 260,
      render: (row) => (
        <div className="product-archive-page__name-cell">
          <strong>{row.productName}</strong>
          <span>{row.productSpecification ?? '未设置规格'}</span>
        </div>
      ),
    },
    {
      key: 'code',
      title: '产品编号',
      minWidth: 180,
      render: (row) => row.productCode,
    },
    {
      key: 'category',
      title: '产品分类',
      minWidth: 180,
      render: (row) => row.categoryName,
    },
    {
      key: 'unit',
      title: '产品单位',
      minWidth: 120,
      render: (row) => `${row.unitName}（${row.unitSymbol}）`,
    },
    {
      key: 'origin',
      title: '产地信息',
      minWidth: 160,
      render: (row) => row.originName,
    },
    {
      key: 'storage',
      title: '储存条件',
      minWidth: 180,
      render: (row) => row.storageConditionName,
    },
    {
      key: 'shelfLife',
      title: '保质期',
      minWidth: 120,
      render: (row) => `${row.shelfLifeDays} 天`,
    },
    {
      key: 'warning',
      title: '预警提前',
      minWidth: 120,
      render: (row) => `${row.warningDays} 天`,
    },
    {
      key: 'grade',
      title: '品质等级',
      minWidth: 120,
      render: (row) => row.qualityGradeName,
    },
    {
      key: 'status',
      title: '状态',
      minWidth: 120,
      render: (row) => (
        <span
          className={`product-archive-page__status${
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
            render: (row: ProductArchiveRow) => (
              <div className="product-archive-page__row-actions">
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
    await loadProductArchiveList(buildQueryParams(queryForm))
  }

  const handleReset = async () => {
    const resetState = {
      productCode: '',
      productName: '',
      categoryId: '',
      status: '',
    }

    setQueryForm(resetState)
    await loadProductArchiveList({})
  }

  const handleCreate = () => {
    setDialogMode('create')
    setEditingId(null)
    setFormError(null)
    setFormState(initialFormState)
    setIsDialogOpen(true)
  }

  const handleEdit = async (productArchiveId: string) => {
    setDialogMode('edit')
    setEditingId(productArchiveId)
    setFormError(null)
    setIsDialogOpen(true)
    setIsSubmitting(true)

    try {
      const detail = await getProductArchiveDetail(productArchiveId)
      setFormState(mapDetailToFormState(detail))
    } catch (error) {
      setFormError(normalizeError(error))
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleToggleStatus = async (row: ProductArchiveRow) => {
    const nextStatus = row.status === 1 ? 0 : 1
    const confirmed = window.confirm(
      `确定要将“${row.productName}”${nextStatus === 1 ? '启用' : '停用'}吗？`,
    )

    if (!confirmed) {
      return
    }

    try {
      await updateProductArchiveStatus(row.id, nextStatus)
      await loadProductArchiveList(buildQueryParams(queryForm))
    } catch (error) {
      setPageError(normalizeError(error))
    }
  }

  const handleDelete = async (row: ProductArchiveRow) => {
    const confirmed = window.confirm(`确定要删除产品档案“${row.productName}”吗？`)

    if (!confirmed) {
      return
    }

    try {
      await deleteProductArchive(row.id)
      await loadProductArchiveList(buildQueryParams(queryForm))
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
        await createProductArchive(payload)
      } else if (editingId) {
        await updateProductArchive(editingId, payload)
      }

      setIsDialogOpen(false)
      setFormState(initialFormState)
      await loadProductArchiveList(buildQueryParams(queryForm))
    } catch (error) {
      setFormError(normalizeError(error))
    } finally {
      setIsSubmitting(false)
    }
  }

  const nameFieldError = getFieldError(formError?.fieldErrors, 'productName')

  return (
    <div className="product-archive-page">
      {pageError ? (
        <div className="product-archive-page__message product-archive-page__message--error">
          <strong>{pageError.message}</strong>
          {pageError.traceId ? <span>traceId: {pageError.traceId}</span> : null}
        </div>
      ) : null}

      <section className="product-archive-page__search">
        <div className="product-archive-page__search-grid">
          <label className="product-archive-page__field">
            <span>产品编号</span>
            <input
              type="text"
              placeholder="输入编号..."
              value={queryForm.productCode}
              onChange={(event) =>
                setQueryForm((current) => ({
                  ...current,
                  productCode: event.target.value,
                }))
              }
            />
          </label>

          <label className="product-archive-page__field">
            <span>产品名称</span>
            <input
              type="text"
              placeholder="输入名称..."
              value={queryForm.productName}
              onChange={(event) =>
                setQueryForm((current) => ({
                  ...current,
                  productName: event.target.value,
                }))
              }
            />
          </label>

          <label className="product-archive-page__field">
            <span>产品分类</span>
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

          <label className="product-archive-page__field">
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

          <div className="product-archive-page__actions">
            <button type="button" className="is-ghost" onClick={() => void handleReset()}>
              重置
            </button>
            <button type="button" className="is-dark" onClick={() => void handleSearch()}>
              查询
            </button>
          </div>
        </div>
      </section>

      <section className="product-archive-page__toolbar">
        <div className="product-archive-page__toolbar-copy">
          <h3>产品档案列表</h3>
          <p>维护农产品主数据档案，统一挂接分类、单位、产地、储存条件和品质等级，并直接维护保质期信息。</p>
        </div>

        {canManage ? (
          <button type="button" className="product-archive-page__primary" onClick={handleCreate}>
            新增产品档案
          </button>
        ) : null}
      </section>

      <section className="product-archive-page__table-shell">
        {isLoading ? (
          <div className="product-archive-page__loading">正在加载产品档案...</div>
        ) : (
          <TreeDataTable
            data={productArchives}
            columns={columns}
            emptyText="暂无产品档案数据"
          />
        )}
      </section>

      {isDialogOpen ? (
        <div className="product-archive-page__dialog-backdrop">
          <div className="product-archive-page__dialog">
            <div className="product-archive-page__dialog-header">
              <div>
                <h3>{dialogMode === 'create' ? '新增产品档案' : '编辑产品档案'}</h3>
                <p>维护产品主数据，并关联分类、单位、产地、储存条件和品质等级，同时直接填写保质期信息。</p>
              </div>
              <button
                type="button"
                className="product-archive-page__dialog-close"
                onClick={() => setIsDialogOpen(false)}
              >
                ×
              </button>
            </div>

            <form className="product-archive-page__dialog-form" onSubmit={handleDialogSubmit}>
              <label className="product-archive-page__field">
                <span>产品编号</span>
                <input value={formState.productCode} placeholder="系统自动生成" readOnly disabled />
                <small className="product-archive-page__field-hint">
                  {dialogMode === 'create' ? '新增时由系统自动生成编号' : '产品编号创建后不可修改'}
                </small>
              </label>

              <label className="product-archive-page__field">
                <span>产品名称</span>
                <input
                  value={formState.productName}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      productName: event.target.value,
                    }))
                  }
                />
                {nameFieldError ? (
                  <small className="product-archive-page__field-error">{nameFieldError}</small>
                ) : null}
              </label>

              <label className="product-archive-page__field">
                <span>产品规格</span>
                <input
                  value={formState.productSpecification}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      productSpecification: event.target.value,
                    }))
                  }
                />
              </label>

              <label className="product-archive-page__field">
                <span>产品分类</span>
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

              <label className="product-archive-page__field">
                <span>产品单位</span>
                <select
                  value={formState.unitId}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      unitId: event.target.value,
                    }))
                  }
                >
                  <option value="">请选择</option>
                  {productUnitOptions.map((option) => (
                    <option key={option.id} value={option.id}>
                      {option.label}（{option.unitSymbol}）
                    </option>
                  ))}
                </select>
              </label>

              <label className="product-archive-page__field">
                <span>产地信息</span>
                <select
                  value={formState.originId}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      originId: event.target.value,
                    }))
                  }
                >
                  <option value="">请选择</option>
                  {productOriginOptions.map((option) => (
                    <option key={option.id} value={option.id}>
                      {option.label}（{option.locationText}）
                    </option>
                  ))}
                </select>
              </label>

              <label className="product-archive-page__field">
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

              <label className="product-archive-page__field">
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

              <label className="product-archive-page__field">
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

              <label className="product-archive-page__field">
                <span>品质等级</span>
                <select
                  value={formState.qualityGradeId}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      qualityGradeId: event.target.value,
                    }))
                  }
                >
                  <option value="">请选择</option>
                  {qualityGradeOptions.map((option) => (
                    <option key={option.id} value={option.id}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </label>

              <label className="product-archive-page__field">
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

              <label className="product-archive-page__field">
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

              <label className="product-archive-page__field product-archive-page__field--full">
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
                <div className="product-archive-page__message product-archive-page__message--error">
                  <strong>{formError.message}</strong>
                  {formError.traceId ? <span>traceId: {formError.traceId}</span> : null}
                </div>
              ) : null}

              <div className="product-archive-page__dialog-actions">
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
  productCode: string
  productName: string
  categoryId: string
  status: string
}): ProductArchiveListQuery {
  return {
    productCode: queryForm.productCode.trim() || undefined,
    productName: queryForm.productName.trim() || undefined,
    categoryId: queryForm.categoryId || undefined,
    status: queryForm.status ? Number(queryForm.status) : undefined,
  }
}

function mapDetailToFormState(detail: ProductArchiveDetail): ProductArchiveFormState {
  return {
    productCode: detail.productCode,
    productName: detail.productName,
    productSpecification: detail.productSpecification ?? '',
    categoryId: detail.categoryId,
    unitId: detail.unitId,
    originId: detail.originId,
    storageConditionId: detail.storageConditionId,
    shelfLifeDays: String(detail.shelfLifeDays),
    warningDays: String(detail.warningDays),
    qualityGradeId: detail.qualityGradeId,
    status: String(detail.status),
    sortOrder: String(detail.sortOrder),
    remarks: detail.remarks ?? '',
  }
}

function mapFormStateToPayload(
  formState: ProductArchiveFormState,
): ProductArchiveFormPayload {
  return {
    productName: formState.productName.trim(),
    productSpecification: formState.productSpecification.trim() || null,
    categoryId: Number(formState.categoryId),
    unitId: Number(formState.unitId),
    originId: Number(formState.originId),
    storageConditionId: Number(formState.storageConditionId),
    shelfLifeDays: Number(formState.shelfLifeDays),
    warningDays: Number(formState.warningDays),
    qualityGradeId: Number(formState.qualityGradeId),
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

function toTableRows(productArchiveList: ProductArchiveListItem[]): ProductArchiveRow[] {
  return productArchiveList.map((item) => ({
    ...item,
    children: [],
  }))
}

export default ProductArchivePage
