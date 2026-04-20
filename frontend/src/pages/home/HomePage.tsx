import { type FormEvent, useEffect, useMemo, useState } from 'react'

import { getFieldError, normalizeError, type AppError } from '../../api/errors'
import TreeDataTable, {
  type TreeTableColumn,
  type TreeTableRow,
} from '../../components/tree-data-table/TreeDataTable'
import {
  createCategory,
  deleteCategory,
  getCategoryDetail,
  getCategoryOptions,
  getCategoryTree,
  updateCategory,
  updateCategoryStatus,
} from '../../features/category/api'
import type {
  CategoryDetail,
  CategoryFormPayload,
  CategoryOption,
  CategoryTreeItem,
  CategoryTreeQuery,
} from '../../features/category/types'
import { getStorageConditionOptions } from '../../features/storagecondition/api'
import type { StorageConditionOption } from '../../features/storagecondition/types'
import './HomePage.css'

type CategoryPageRow = Omit<CategoryTreeItem, 'children'> &
  TreeTableRow & {
    children: CategoryPageRow[]
  }

interface CategoryFormState {
  categoryCode: string
  categoryName: string
  parentId: string
  sortOrder: string
  status: string
  defaultStorageConditionId: string
  remarks: string
}

const initialFormState: CategoryFormState = {
  categoryCode: '',
  categoryName: '',
  parentId: '',
  sortOrder: '0',
  status: '1',
  defaultStorageConditionId: '',
  remarks: '',
}

function HomePage() {
  const [queryForm, setQueryForm] = useState({
    categoryCode: '',
    categoryName: '',
    parentId: '',
  })
  const [categoryTree, setCategoryTree] = useState<CategoryPageRow[]>([])
  const [categoryOptions, setCategoryOptions] = useState<CategoryOption[]>([])
  const [storageConditionOptions, setStorageConditionOptions] = useState<
    StorageConditionOption[]
  >([])
  const [expandedKeys, setExpandedKeys] = useState<string[]>([])
  const [pageError, setPageError] = useState<AppError | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  const [isDialogOpen, setIsDialogOpen] = useState(false)
  const [dialogMode, setDialogMode] = useState<'create' | 'edit'>('create')
  const [editingCategoryId, setEditingCategoryId] = useState<string | null>(null)
  const [formState, setFormState] = useState<CategoryFormState>(initialFormState)
  const [formError, setFormError] = useState<AppError | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)

  const flattenedOptions = useMemo(
    () => flattenCategoryOptions(categoryOptions),
    [categoryOptions],
  )

  const expandableRowIds = useMemo(
    () => collectExpandableRowIds(categoryTree),
    [categoryTree],
  )

  async function loadCategoryTreeByQuery(query: CategoryTreeQuery) {
    setIsLoading(true)
    setPageError(null)

    try {
      const tree = await getCategoryTree(query)
      setCategoryTree(tree as CategoryPageRow[])
      setExpandedKeys(collectInitialExpandedKeys(tree))
    } catch (error) {
      setPageError(normalizeError(error))
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    let isMounted = true

    const bootstrap = async () => {
      try {
        const [tree, options, storageConditions] = await Promise.all([
          getCategoryTree(),
          getCategoryOptions(),
          getStorageConditionOptions(),
        ])

        if (!isMounted) {
          return
        }

        setCategoryTree(tree as CategoryPageRow[])
        setCategoryOptions(options)
        setStorageConditionOptions(storageConditions)
        setExpandedKeys(collectInitialExpandedKeys(tree))
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

  const handleSearch = async () => {
    await loadCategoryTreeByQuery(buildQueryParams(queryForm))
  }

  const handleReset = async () => {
    const resetForm = {
      categoryCode: '',
      categoryName: '',
      parentId: '',
    }

    setQueryForm(resetForm)
    await loadCategoryTreeByQuery({})
  }

  const handleExpandAll = () => {
    setExpandedKeys(expandableRowIds)
  }

  const handleCollapseAll = () => {
    setExpandedKeys([])
  }

  const handleCreateRoot = () => {
    setDialogMode('create')
    setEditingCategoryId(null)
    setFormError(null)
    setFormState(initialFormState)
    setIsDialogOpen(true)
  }

  const handleCreateChild = (row: CategoryPageRow) => {
    setDialogMode('create')
    setEditingCategoryId(null)
    setFormError(null)
    setFormState({
      ...initialFormState,
      parentId: row.id,
      sortOrder: '0',
    })
    setIsDialogOpen(true)
  }

  const handleEdit = async (categoryId: string) => {
    setDialogMode('edit')
    setEditingCategoryId(categoryId)
    setFormError(null)
    setIsDialogOpen(true)
    setIsSubmitting(true)

    try {
      const categoryDetail = await getCategoryDetail(categoryId)
      setFormState(mapDetailToFormState(categoryDetail))
    } catch (error) {
      setFormError(normalizeError(error))
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleToggleStatus = async (row: CategoryPageRow) => {
    const nextStatus = row.status === 1 ? 0 : 1
    const confirmed = window.confirm(
      `确定要将“${row.categoryName}”${nextStatus === 1 ? '启用' : '停用'}吗？`,
    )

    if (!confirmed) {
      return
    }

    try {
      await updateCategoryStatus(row.id, nextStatus)
      await loadCategoryTreeByQuery(buildQueryParams(queryForm))
      await refreshCategoryOptions()
    } catch (error) {
      setPageError(normalizeError(error))
    }
  }

  const handleDelete = async (row: CategoryPageRow) => {
    const confirmed = window.confirm(`确定要删除分类“${row.categoryName}”吗？`)

    if (!confirmed) {
      return
    }

    try {
      await deleteCategory(row.id)
      await loadCategoryTreeByQuery(buildQueryParams(queryForm))
      await refreshCategoryOptions()
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
        await createCategory(payload)
      } else if (editingCategoryId) {
        await updateCategory(editingCategoryId, payload)
      }

      setIsDialogOpen(false)
      setFormState(initialFormState)
      setEditingCategoryId(null)
      await loadCategoryTreeByQuery(buildQueryParams(queryForm))
      await refreshCategoryOptions()
    } catch (error) {
      setFormError(normalizeError(error))
    } finally {
      setIsSubmitting(false)
    }
  }

  async function refreshCategoryOptions() {
    const [options, storageConditions] = await Promise.all([
      getCategoryOptions(),
      getStorageConditionOptions(),
    ])
    setCategoryOptions(options)
    setStorageConditionOptions(storageConditions)
  }

  const columns: TreeTableColumn<CategoryPageRow>[] = [
    {
      key: 'name',
      title: '分类名称',
      tree: true,
      minWidth: 320,
      render: (row, context) => (
        <div className="category-page__name">
          <span
            className={`category-page__name-icon${
              context.depth === 0 ? ' is-root' : ''
            }`}
          />
          <span>{row.categoryName}</span>
        </div>
      ),
    },
    {
      key: 'code',
      title: '分类编号',
      minWidth: 140,
      render: (row) => row.categoryCode,
    },
    {
      key: 'level',
      title: '层级',
      align: 'center',
      minWidth: 90,
      render: (row) => (
        <span className="category-page__level">L{row.categoryLevel}</span>
      ),
    },
    {
      key: 'storage',
      title: '默认储存条件',
      minWidth: 210,
      render: (row) => joinStorageLabel(row),
    },
    {
      key: 'status',
      title: '状态',
      minWidth: 120,
      render: (row) => (
        <span
          className={`category-page__status${
            row.status === 1 ? ' is-enabled' : ' is-disabled'
          }`}
        >
          {row.statusLabel}
        </span>
      ),
    },
    {
      key: 'sort',
      title: '排序',
      minWidth: 90,
      align: 'center',
      render: (row) => row.sortOrder,
    },
    {
      key: 'createdAt',
      title: '创建时间',
      minWidth: 170,
      render: (row) => formatDateTime(row.createdAt),
    },
    {
      key: 'actions',
      title: '操作',
      minWidth: 260,
      width: 260,
      sticky: 'right',
      align: 'right',
      render: (row, context) => (
        <div className="category-page__row-actions">
          {context.depth < 2 ? (
            <button type="button" onClick={() => handleCreateChild(row)}>
              新增子类
            </button>
          ) : null}
          <button type="button" onClick={() => handleEdit(row.id)}>
            编辑
          </button>
          <button type="button" onClick={() => handleToggleStatus(row)}>
            {row.status === 1 ? '停用' : '启用'}
          </button>
          {!context.hasChildren ? (
            <button type="button" onClick={() => handleDelete(row)}>
              删除
            </button>
          ) : null}
        </div>
      ),
    },
  ]

  const parentIdFieldError = getFieldError(formError?.fieldErrors, 'parentId')
  const nameFieldError = getFieldError(formError?.fieldErrors, 'categoryName')
  const sortFieldError = getFieldError(formError?.fieldErrors, 'sortOrder')

  return (
    <div className="category-page">
      {pageError ? (
        <div className="category-page__message category-page__message--error">
          <strong>{pageError.message}</strong>
          {pageError.traceId ? <span>traceId: {pageError.traceId}</span> : null}
        </div>
      ) : null}

      <section className="category-page__search">
        <div className="category-page__search-grid">
          <label className="category-page__field">
            <span>分类编号</span>
            <input
              type="text"
              placeholder="输入编号..."
              value={queryForm.categoryCode}
              onChange={(event) =>
                setQueryForm((current) => ({
                  ...current,
                  categoryCode: event.target.value,
                }))
              }
            />
          </label>

          <label className="category-page__field">
            <span>分类名称</span>
            <input
              type="text"
              placeholder="输入名称..."
              value={queryForm.categoryName}
              onChange={(event) =>
                setQueryForm((current) => ({
                  ...current,
                  categoryName: event.target.value,
                }))
              }
            />
          </label>

          <label className="category-page__field">
            <span>上级分类</span>
            <select
              value={queryForm.parentId}
              onChange={(event) =>
                setQueryForm((current) => ({
                  ...current,
                  parentId: event.target.value,
                }))
              }
            >
              <option value="">全部分类</option>
              {flattenedOptions.map((option) => (
                <option key={option.id} value={option.id}>
                  {option.label}
                </option>
              ))}
            </select>
          </label>

          <div className="category-page__actions">
            <button type="button" className="is-ghost" onClick={() => void handleReset()}>
              重置
            </button>
            <button type="button" className="is-dark" onClick={() => void handleSearch()}>
              查询
            </button>
          </div>
        </div>
      </section>

      <section className="category-page__toolbar">
        <div className="category-page__toolbar-left">
          <button type="button" onClick={handleExpandAll}>
            展开全部
          </button>
          <button type="button" onClick={handleCollapseAll}>
            收起全部
          </button>
        </div>

        <button type="button" className="category-page__primary" onClick={handleCreateRoot}>
          新增分类
        </button>
      </section>

      <section className="category-page__table-shell">
        {isLoading ? (
          <div className="category-page__loading">正在加载分类数据...</div>
        ) : (
          <TreeDataTable
            data={categoryTree}
            columns={columns}
            expandedKeys={expandedKeys}
            onExpandedKeysChange={setExpandedKeys}
          />
        )}
      </section>

      {isDialogOpen ? (
        <div className="category-page__dialog-backdrop">
          <div className="category-page__dialog">
            <div className="category-page__dialog-header">
              <div>
                <h3>{dialogMode === 'create' ? '新增分类' : '编辑分类'}</h3>
                <p>维护分类树节点与默认储存规则。</p>
              </div>
              <button
                type="button"
                className="category-page__dialog-close"
                onClick={() => setIsDialogOpen(false)}
              >
                ×
              </button>
            </div>

            <form className="category-page__dialog-form" onSubmit={handleDialogSubmit}>
              <label className="category-page__field">
                <span>分类编号</span>
                <input
                  value={formState.categoryCode}
                  placeholder="系统自动生成"
                  readOnly
                  disabled
                />
                <small className="category-page__field-hint">
                  {dialogMode === 'create'
                    ? '新增分类时由系统自动生成编号'
                    : '分类编号创建后不可修改'}
                </small>
              </label>

              <label className="category-page__field">
                <span>分类名称</span>
                <input
                  value={formState.categoryName}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      categoryName: event.target.value,
                    }))
                  }
                  placeholder="输入分类名称"
                />
                {nameFieldError ? (
                  <small className="category-page__field-error">{nameFieldError}</small>
                ) : null}
              </label>

              <label className="category-page__field">
                <span>上级分类</span>
                <select
                  value={formState.parentId}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      parentId: event.target.value,
                    }))
                  }
                >
                  <option value="">顶级分类</option>
                  {flattenedOptions
                    .filter((option) =>
                      dialogMode === 'edit' && editingCategoryId
                        ? option.id !== editingCategoryId &&
                          !option.ancestorPath.includes(`/${editingCategoryId}/`)
                        : true,
                    )
                    .map((option) => (
                      <option key={option.id} value={option.id}>
                        {option.label}
                      </option>
                    ))}
                </select>
                {parentIdFieldError ? (
                  <small className="category-page__field-error">{parentIdFieldError}</small>
                ) : null}
              </label>

              <label className="category-page__field">
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
                {sortFieldError ? (
                  <small className="category-page__field-error">{sortFieldError}</small>
                ) : null}
              </label>

              <label className="category-page__field">
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

              <label className="category-page__field">
                <span>默认储存条件</span>
                <select
                  value={formState.defaultStorageConditionId}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      defaultStorageConditionId: event.target.value,
                    }))
                  }
                >
                  <option value="">不设置默认条件</option>
                  {storageConditionOptions.map((option) => (
                    <option key={option.id} value={option.id}>
                      {option.label}（{option.storageType}）
                    </option>
                  ))}
                </select>
                <small className="category-page__field-hint">
                  仅可选择系统中已启用的储存条件
                </small>
              </label>

              <label className="category-page__field category-page__field--full">
                <span>备注</span>
                <textarea
                  value={formState.remarks}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      remarks: event.target.value,
                    }))
                  }
                  rows={4}
                  placeholder="输入备注"
                />
              </label>

              {formError && !formError.fieldErrors.length ? (
                <div className="category-page__message category-page__message--error">
                  <strong>{formError.message}</strong>
                  {formError.traceId ? (
                    <span>traceId: {formError.traceId}</span>
                  ) : null}
                </div>
              ) : null}

              <div className="category-page__dialog-actions">
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

function collectExpandableRowIds(rows: CategoryPageRow[]): string[] {
  return rows.flatMap((row) => {
    if (!row.children?.length) {
      return []
    }

    return [row.id, ...collectExpandableRowIds(row.children)]
  })
}

function collectInitialExpandedKeys(rows: CategoryPageRow[]): string[] {
  return rows.filter((row) => row.categoryLevel === 1).map((row) => row.id)
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

function buildQueryParams(queryForm: {
  categoryCode: string
  categoryName: string
  parentId: string
}): CategoryTreeQuery {
  return {
    categoryCode: queryForm.categoryCode.trim() || undefined,
    categoryName: queryForm.categoryName.trim() || undefined,
    parentId: queryForm.parentId || undefined,
  }
}

function mapDetailToFormState(detail: CategoryDetail): CategoryFormState {
  return {
    categoryCode: detail.categoryCode,
    categoryName: detail.categoryName,
    parentId: detail.parentId ?? '',
    sortOrder: String(detail.sortOrder),
    status: String(detail.status),
    defaultStorageConditionId: detail.defaultStorageConditionId ?? '',
    remarks: detail.remarks ?? '',
  }
}

function mapFormStateToPayload(formState: CategoryFormState): CategoryFormPayload {
  return {
    categoryName: formState.categoryName.trim(),
    parentId: formState.parentId ? Number(formState.parentId) : null,
    sortOrder: Number(formState.sortOrder),
    status: Number(formState.status),
    defaultStorageConditionId: formState.defaultStorageConditionId
      ? Number(formState.defaultStorageConditionId)
      : null,
    remarks: formState.remarks.trim() || null,
  }
}

function joinStorageLabel(row: CategoryPageRow) {
  if (row.defaultStorageCondition) {
    if (row.defaultStorageType) {
      return `${row.defaultStorageCondition}（${row.defaultStorageType}）`
    }

    return row.defaultStorageCondition
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

export default HomePage
