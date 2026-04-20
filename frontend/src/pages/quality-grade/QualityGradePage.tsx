import { type FormEvent, useEffect, useState } from 'react'

import { getFieldError, normalizeError, type AppError } from '../../api/errors'
import TreeDataTable, {
  type TreeTableColumn,
  type TreeTableRow,
} from '../../components/tree-data-table/TreeDataTable'
import {
  createQualityGrade,
  deleteQualityGrade,
  getQualityGradeDetail,
  getQualityGradeList,
  updateQualityGrade,
  updateQualityGradeStatus,
} from '../../features/qualitygrade/api'
import type {
  QualityGradeDetail,
  QualityGradeFormPayload,
  QualityGradeListItem,
  QualityGradeListQuery,
} from '../../features/qualitygrade/types'
import { usePagePermission } from '../../features/auth/usePagePermission'
import './QualityGradePage.css'

type QualityGradeRow = TreeTableRow & QualityGradeListItem

interface QualityGradeFormState {
  gradeCode: string
  gradeName: string
  scoreMin: string
  scoreMax: string
  status: string
  sortOrder: string
  remarks: string
}

const initialFormState: QualityGradeFormState = {
  gradeCode: '',
  gradeName: '',
  scoreMin: '',
  scoreMax: '',
  status: '1',
  sortOrder: '0',
  remarks: '',
}

function QualityGradePage() {
  const [queryForm, setQueryForm] = useState({
    gradeCode: '',
    gradeName: '',
    status: '',
  })
  const [qualityGrades, setQualityGrades] = useState<QualityGradeRow[]>([])
  const [pageError, setPageError] = useState<AppError | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  const [isDialogOpen, setIsDialogOpen] = useState(false)
  const [dialogMode, setDialogMode] = useState<'create' | 'edit'>('create')
  const [editingId, setEditingId] = useState<string | null>(null)
  const [formState, setFormState] = useState<QualityGradeFormState>(initialFormState)
  const [formError, setFormError] = useState<AppError | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const { canManage } = usePagePermission()

  useEffect(() => {
    let isMounted = true

    const bootstrap = async () => {
      try {
        const list = await getQualityGradeList()

        if (!isMounted) {
          return
        }

        setQualityGrades(toTableRows(list))
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

  async function loadQualityGradeList(query: QualityGradeListQuery) {
    setIsLoading(true)
    setPageError(null)

    try {
      const list = await getQualityGradeList(query)
      setQualityGrades(toTableRows(list))
    } catch (error) {
      setPageError(normalizeError(error))
    } finally {
      setIsLoading(false)
    }
  }

  const columns: TreeTableColumn<QualityGradeRow>[] = [
    {
      key: 'name',
      title: '等级名称',
      minWidth: 220,
      render: (row) => (
        <div className="quality-grade-page__name-cell">
          <strong>{row.gradeName}</strong>
          <span>{formatRange(row.scoreMin, row.scoreMax)}</span>
        </div>
      ),
    },
    {
      key: 'code',
      title: '等级编号',
      minWidth: 180,
      render: (row) => row.gradeCode,
    },
    {
      key: 'score',
      title: '分值范围',
      minWidth: 140,
      render: (row) => formatRange(row.scoreMin, row.scoreMax),
    },
    {
      key: 'status',
      title: '状态',
      minWidth: 120,
      render: (row) => (
        <span
          className={`quality-grade-page__status${
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
            render: (row: QualityGradeRow) => (
              <div className="quality-grade-page__row-actions">
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
    await loadQualityGradeList(buildQueryParams(queryForm))
  }

  const handleReset = async () => {
    const resetState = {
      gradeCode: '',
      gradeName: '',
      status: '',
    }

    setQueryForm(resetState)
    await loadQualityGradeList({})
  }

  const handleCreate = () => {
    setDialogMode('create')
    setEditingId(null)
    setFormError(null)
    setFormState(initialFormState)
    setIsDialogOpen(true)
  }

  const handleEdit = async (qualityGradeId: string) => {
    setDialogMode('edit')
    setEditingId(qualityGradeId)
    setFormError(null)
    setIsDialogOpen(true)
    setIsSubmitting(true)

    try {
      const detail = await getQualityGradeDetail(qualityGradeId)
      setFormState(mapDetailToFormState(detail))
    } catch (error) {
      setFormError(normalizeError(error))
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleToggleStatus = async (row: QualityGradeRow) => {
    const nextStatus = row.status === 1 ? 0 : 1
    const confirmed = window.confirm(
      `确定要将“${row.gradeName}”${nextStatus === 1 ? '启用' : '停用'}吗？`,
    )

    if (!confirmed) {
      return
    }

    try {
      await updateQualityGradeStatus(row.id, nextStatus)
      await loadQualityGradeList(buildQueryParams(queryForm))
    } catch (error) {
      setPageError(normalizeError(error))
    }
  }

  const handleDelete = async (row: QualityGradeRow) => {
    const confirmed = window.confirm(`确定要删除品质等级“${row.gradeName}”吗？`)

    if (!confirmed) {
      return
    }

    try {
      await deleteQualityGrade(row.id)
      await loadQualityGradeList(buildQueryParams(queryForm))
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
        await createQualityGrade(payload)
      } else if (editingId) {
        await updateQualityGrade(editingId, payload)
      }

      setIsDialogOpen(false)
      setFormState(initialFormState)
      await loadQualityGradeList(buildQueryParams(queryForm))
    } catch (error) {
      setFormError(normalizeError(error))
    } finally {
      setIsSubmitting(false)
    }
  }

  const nameFieldError = getFieldError(formError?.fieldErrors, 'gradeName')

  return (
    <div className="quality-grade-page">
      {pageError ? (
        <div className="quality-grade-page__message quality-grade-page__message--error">
          <strong>{pageError.message}</strong>
          {pageError.traceId ? <span>traceId: {pageError.traceId}</span> : null}
        </div>
      ) : null}

      <section className="quality-grade-page__search">
        <div className="quality-grade-page__search-grid">
          <label className="quality-grade-page__field">
            <span>等级编号</span>
            <input
              type="text"
              placeholder="输入编号..."
              value={queryForm.gradeCode}
              onChange={(event) =>
                setQueryForm((current) => ({
                  ...current,
                  gradeCode: event.target.value,
                }))
              }
            />
          </label>

          <label className="quality-grade-page__field">
            <span>等级名称</span>
            <input
              type="text"
              placeholder="输入名称..."
              value={queryForm.gradeName}
              onChange={(event) =>
                setQueryForm((current) => ({
                  ...current,
                  gradeName: event.target.value,
                }))
              }
            />
          </label>

          <label className="quality-grade-page__field">
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

          <div className="quality-grade-page__actions">
            <button type="button" className="is-ghost" onClick={() => void handleReset()}>
              重置
            </button>
            <button type="button" className="is-dark" onClick={() => void handleSearch()}>
              查询
            </button>
          </div>
        </div>
      </section>

      <section className="quality-grade-page__toolbar">
        <div className="quality-grade-page__toolbar-copy">
          <h3>品质等级列表</h3>
          <p>维护统一品质等级标准，为产品档案和后续质检业务提供明确等级口径。</p>
        </div>

        {canManage ? (
          <button type="button" className="quality-grade-page__primary" onClick={handleCreate}>
            新增品质等级
          </button>
        ) : null}
      </section>

      <section className="quality-grade-page__table-shell">
        {isLoading ? (
          <div className="quality-grade-page__loading">正在加载品质等级...</div>
        ) : (
          <TreeDataTable
            data={qualityGrades}
            columns={columns}
            emptyText="暂无品质等级数据"
          />
        )}
      </section>

      {isDialogOpen ? (
        <div className="quality-grade-page__dialog-backdrop">
          <div className="quality-grade-page__dialog">
            <div className="quality-grade-page__dialog-header">
              <div>
                <h3>{dialogMode === 'create' ? '新增品质等级' : '编辑品质等级'}</h3>
                <p>维护等级名称、分值范围与排序规则。</p>
              </div>
              <button
                type="button"
                className="quality-grade-page__dialog-close"
                onClick={() => setIsDialogOpen(false)}
              >
                ×
              </button>
            </div>

            <form className="quality-grade-page__dialog-form" onSubmit={handleDialogSubmit}>
              <label className="quality-grade-page__field">
                <span>等级编号</span>
                <input value={formState.gradeCode} placeholder="系统自动生成" readOnly disabled />
                <small className="quality-grade-page__field-hint">
                  {dialogMode === 'create' ? '新增时由系统自动生成编号' : '等级编号创建后不可修改'}
                </small>
              </label>

              <label className="quality-grade-page__field">
                <span>等级名称</span>
                <input
                  value={formState.gradeName}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      gradeName: event.target.value,
                    }))
                  }
                />
                {nameFieldError ? (
                  <small className="quality-grade-page__field-error">{nameFieldError}</small>
                ) : null}
              </label>

              <label className="quality-grade-page__field">
                <span>最低分值</span>
                <input
                  type="number"
                  step="0.1"
                  value={formState.scoreMin}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      scoreMin: event.target.value,
                    }))
                  }
                />
              </label>

              <label className="quality-grade-page__field">
                <span>最高分值</span>
                <input
                  type="number"
                  step="0.1"
                  value={formState.scoreMax}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      scoreMax: event.target.value,
                    }))
                  }
                />
              </label>

              <label className="quality-grade-page__field">
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

              <label className="quality-grade-page__field">
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

              <label className="quality-grade-page__field quality-grade-page__field--full">
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
                <div className="quality-grade-page__message quality-grade-page__message--error">
                  <strong>{formError.message}</strong>
                  {formError.traceId ? <span>traceId: {formError.traceId}</span> : null}
                </div>
              ) : null}

              <div className="quality-grade-page__dialog-actions">
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
  gradeCode: string
  gradeName: string
  status: string
}): QualityGradeListQuery {
  return {
    gradeCode: queryForm.gradeCode.trim() || undefined,
    gradeName: queryForm.gradeName.trim() || undefined,
    status: queryForm.status ? Number(queryForm.status) : undefined,
  }
}

function mapDetailToFormState(detail: QualityGradeDetail): QualityGradeFormState {
  return {
    gradeCode: detail.gradeCode,
    gradeName: detail.gradeName,
    scoreMin: detail.scoreMin != null ? String(detail.scoreMin) : '',
    scoreMax: detail.scoreMax != null ? String(detail.scoreMax) : '',
    status: String(detail.status),
    sortOrder: String(detail.sortOrder),
    remarks: detail.remarks ?? '',
  }
}

function mapFormStateToPayload(
  formState: QualityGradeFormState,
): QualityGradeFormPayload {
  return {
    gradeName: formState.gradeName.trim(),
    scoreMin: formState.scoreMin ? Number(formState.scoreMin) : null,
    scoreMax: formState.scoreMax ? Number(formState.scoreMax) : null,
    status: Number(formState.status),
    sortOrder: Number(formState.sortOrder),
    remarks: formState.remarks.trim() || null,
  }
}

function formatRange(scoreMin: number | null, scoreMax: number | null) {
  if (scoreMin != null && scoreMax != null) {
    return `${scoreMin} ~ ${scoreMax}`
  }

  if (scoreMin != null) {
    return `≥ ${scoreMin}`
  }

  if (scoreMax != null) {
    return `≤ ${scoreMax}`
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

function toTableRows(qualityGradeList: QualityGradeListItem[]): QualityGradeRow[] {
  return qualityGradeList.map((item) => ({
    ...item,
    children: [],
  }))
}

export default QualityGradePage
