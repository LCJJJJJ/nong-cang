import { type FormEvent, useEffect, useMemo, useState } from 'react'

import { getFieldError, normalizeError, type AppError } from '../../api/errors'
import TreeDataTable, {
  type TreeTableColumn,
  type TreeTableRow,
} from '../../components/tree-data-table/TreeDataTable'
import {
  createSystemUser,
  deleteSystemUser,
  getSystemUserDetail,
  getSystemUserList,
  getSystemUserRoleOptions,
  resetSystemUserPassword,
  updateSystemUser,
  updateSystemUserStatus,
} from '../../features/systemuser/api'
import type {
  SystemUserCreatePayload,
  SystemUserDetail,
  SystemUserListItem,
  SystemUserListQuery,
  SystemUserRoleOption,
  SystemUserUpdatePayload,
} from '../../features/systemuser/types'
import { getWarehouseOptions } from '../../features/warehouse/api'
import type { WarehouseOption } from '../../features/warehouse/types'
import './SystemUserPage.css'

type SystemUserRow = TreeTableRow & SystemUserListItem

interface SystemUserFormState {
  username: string
  displayName: string
  phone: string
  roleCode: string
  warehouseId: string
  status: string
  initialPassword: string
  remarks: string
}

const initialFormState: SystemUserFormState = {
  username: '',
  displayName: '',
  phone: '',
  roleCode: 'WAREHOUSE_ADMIN',
  warehouseId: '',
  status: '1',
  initialPassword: '',
  remarks: '',
}

function SystemUserPage() {
  const [queryForm, setQueryForm] = useState({
    username: '',
    displayName: '',
    roleCode: '',
    warehouseId: '',
    status: '',
  })
  const [users, setUsers] = useState<SystemUserRow[]>([])
  const [roleOptions, setRoleOptions] = useState<SystemUserRoleOption[]>([])
  const [warehouseOptions, setWarehouseOptions] = useState<WarehouseOption[]>([])
  const [pageError, setPageError] = useState<AppError | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  const [isDialogOpen, setIsDialogOpen] = useState(false)
  const [dialogMode, setDialogMode] = useState<'create' | 'edit'>('create')
  const [editingId, setEditingId] = useState<string | null>(null)
  const [formState, setFormState] = useState<SystemUserFormState>(initialFormState)
  const [formError, setFormError] = useState<AppError | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [resetPasswordTarget, setResetPasswordTarget] = useState<SystemUserRow | null>(null)
  const [resetPasswordValue, setResetPasswordValue] = useState('')

  useEffect(() => {
    let isMounted = true

    const bootstrap = async () => {
      try {
        const [list, roles, warehouses] = await Promise.all([
          getSystemUserList(),
          getSystemUserRoleOptions(),
          getWarehouseOptions(),
        ])

        if (!isMounted) {
          return
        }

        setUsers(toTableRows(list))
        setRoleOptions(roles)
        setWarehouseOptions(warehouses)
      } catch (error) {
        if (isMounted) {
          setPageError(normalizeError(error))
        }
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

  async function loadSystemUserList(query: SystemUserListQuery) {
    setIsLoading(true)
    setPageError(null)

    try {
      const list = await getSystemUserList(query)
      setUsers(toTableRows(list))
    } catch (error) {
      setPageError(normalizeError(error))
    } finally {
      setIsLoading(false)
    }
  }

  const currentRoleOption = useMemo(
    () => roleOptions.find((option) => option.roleCode === formState.roleCode) ?? null,
    [formState.roleCode, roleOptions],
  )

  const columns: TreeTableColumn<SystemUserRow>[] = [
    {
      key: 'displayName',
      title: '用户',
      minWidth: 220,
      render: (row) => (
        <div className="system-user-page__name-cell">
          <strong>{row.displayName}</strong>
          <span>{row.username}</span>
        </div>
      ),
    },
    {
      key: 'userCode',
      title: '用户编号',
      minWidth: 180,
      render: (row) => row.userCode,
    },
    {
      key: 'phone',
      title: '手机号',
      minWidth: 150,
      render: (row) => row.phone,
    },
    {
      key: 'roleName',
      title: '角色',
      minWidth: 140,
      render: (row) => row.roleName,
    },
    {
      key: 'warehouseName',
      title: '负责仓库',
      minWidth: 160,
      render: (row) => row.warehouseName ?? '全仓权限',
    },
    {
      key: 'status',
      title: '状态',
      minWidth: 110,
      render: (row) => (
        <span className={`system-user-page__status${row.status === 1 ? ' is-enabled' : ' is-disabled'}`}>
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
      minWidth: 260,
      width: 260,
      sticky: 'right',
      align: 'right',
      render: (row) => (
        <div className="system-user-page__row-actions">
          <button type="button" onClick={() => void handleEdit(row.id)}>
            编辑
          </button>
          <button type="button" onClick={() => void handleToggleStatus(row)}>
            {row.status === 1 ? '停用' : '启用'}
          </button>
          <button
            type="button"
            onClick={() => {
              setResetPasswordTarget(row)
              setResetPasswordValue('')
            }}
          >
            重置密码
          </button>
          <button type="button" onClick={() => void handleDelete(row)}>
            删除
          </button>
        </div>
      ),
    },
  ]

  const handleSearch = async () => {
    await loadSystemUserList(buildQueryParams(queryForm))
  }

  const handleReset = async () => {
    const resetState = {
      username: '',
      displayName: '',
      roleCode: '',
      warehouseId: '',
      status: '',
    }
    setQueryForm(resetState)
    await loadSystemUserList({})
  }

  const handleCreate = () => {
    setDialogMode('create')
    setEditingId(null)
    setFormError(null)
    setFormState(initialFormState)
    setIsDialogOpen(true)
  }

  const handleEdit = async (userId: string) => {
    setDialogMode('edit')
    setEditingId(userId)
    setFormError(null)
    setIsDialogOpen(true)
    setIsSubmitting(true)

    try {
      const detail = await getSystemUserDetail(userId)
      setFormState(mapDetailToFormState(detail))
    } catch (error) {
      setFormError(normalizeError(error))
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleToggleStatus = async (row: SystemUserRow) => {
    try {
      await updateSystemUserStatus(row.id, row.status === 1 ? 0 : 1)
      await loadSystemUserList(buildQueryParams(queryForm))
    } catch (error) {
      setPageError(normalizeError(error))
    }
  }

  const handleDelete = async (row: SystemUserRow) => {
    const confirmed = window.confirm(`确定要删除用户“${row.displayName}”吗？`)

    if (!confirmed) {
      return
    }

    try {
      await deleteSystemUser(row.id)
      await loadSystemUserList(buildQueryParams(queryForm))
    } catch (error) {
      setPageError(normalizeError(error))
    }
  }

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setIsSubmitting(true)
    setFormError(null)

    try {
      if (dialogMode === 'create') {
        await createSystemUser(mapCreatePayload(formState))
      } else if (editingId) {
        await updateSystemUser(editingId, mapUpdatePayload(formState))
      }

      setIsDialogOpen(false)
      setFormState(initialFormState)
      await loadSystemUserList(buildQueryParams(queryForm))
    } catch (error) {
      setFormError(normalizeError(error))
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleResetPassword = async () => {
    if (!resetPasswordTarget) {
      return
    }

    try {
      await resetSystemUserPassword(resetPasswordTarget.id, resetPasswordValue.trim())
      setResetPasswordTarget(null)
      setResetPasswordValue('')
      await loadSystemUserList(buildQueryParams(queryForm))
    } catch (error) {
      setPageError(normalizeError(error))
    }
  }

  return (
    <section className="system-user-page">
      {pageError ? (
        <div className="system-user-page__message system-user-page__message--error">
          <strong>{pageError.message}</strong>
          {pageError.traceId ? <span>追踪编号：{pageError.traceId}</span> : null}
        </div>
      ) : null}

      <div className="system-user-page__search">
        <div className="system-user-page__search-grid">
          <label className="system-user-page__field">
            <span>登录账号</span>
            <input value={queryForm.username} onChange={(event) => setQueryForm((current) => ({ ...current, username: event.target.value }))} />
          </label>
          <label className="system-user-page__field">
            <span>姓名</span>
            <input value={queryForm.displayName} onChange={(event) => setQueryForm((current) => ({ ...current, displayName: event.target.value }))} />
          </label>
          <label className="system-user-page__field">
            <span>角色</span>
            <select value={queryForm.roleCode} onChange={(event) => setQueryForm((current) => ({ ...current, roleCode: event.target.value }))}>
              <option value="">全部角色</option>
              {roleOptions.map((option) => (
                <option key={option.roleCode} value={option.roleCode}>
                  {option.roleName}
                </option>
              ))}
            </select>
          </label>
          <label className="system-user-page__field">
            <span>仓库</span>
            <select value={queryForm.warehouseId} onChange={(event) => setQueryForm((current) => ({ ...current, warehouseId: event.target.value }))}>
              <option value="">全部仓库</option>
              {warehouseOptions.map((option) => (
                <option key={option.id} value={option.id}>
                  {option.label}
                </option>
              ))}
            </select>
          </label>
          <label className="system-user-page__field">
            <span>状态</span>
            <select value={queryForm.status} onChange={(event) => setQueryForm((current) => ({ ...current, status: event.target.value }))}>
              <option value="">全部状态</option>
              <option value="1">启用</option>
              <option value="0">停用</option>
            </select>
          </label>
          <div className="system-user-page__actions">
            <button type="button" className="is-ghost" onClick={() => void handleReset()}>
              重置
            </button>
            <button type="button" className="is-dark" onClick={() => void handleSearch()}>
              查询
            </button>
          </div>
        </div>
      </div>

      <div className="system-user-page__toolbar">
        <div className="system-user-page__toolbar-copy">
          <h3>用户列表</h3>
          <p>维护系统用户、角色和负责仓库。一个用户仅分配一个角色和一个仓库。</p>
        </div>
        <button type="button" className="system-user-page__primary" onClick={handleCreate}>
          新增用户
        </button>
      </div>

      <div className="system-user-page__table-shell">
        {isLoading ? <div className="system-user-page__loading">正在加载用户...</div> : <TreeDataTable data={toTableRows(users)} columns={columns} emptyText="暂无用户" />}
      </div>

      {isDialogOpen ? (
        <div className="system-user-page__dialog-backdrop">
          <div className="system-user-page__dialog">
            <div className="system-user-page__dialog-header">
              <div>
                <h3>{dialogMode === 'create' ? '新增用户' : '编辑用户'}</h3>
                <p>统一维护登录账号、角色和负责仓库。</p>
              </div>
              <button type="button" className="system-user-page__dialog-close" onClick={() => setIsDialogOpen(false)}>
                ×
              </button>
            </div>

            <form className="system-user-page__dialog-form" onSubmit={handleSubmit}>
              {formError ? (
                <div className="system-user-page__message system-user-page__message--error system-user-page__field--full">
                  <strong>{formError.message}</strong>
                </div>
              ) : null}

              <label className="system-user-page__field">
                <span>登录账号</span>
                <input
                  value={formState.username}
                  onChange={(event) => setFormState((current) => ({ ...current, username: event.target.value }))}
                  disabled={dialogMode === 'edit'}
                />
                <small>{getFieldError(formError?.fieldErrors, 'username')}</small>
              </label>

              <label className="system-user-page__field">
                <span>姓名</span>
                <input value={formState.displayName} onChange={(event) => setFormState((current) => ({ ...current, displayName: event.target.value }))} />
                <small>{getFieldError(formError?.fieldErrors, 'displayName')}</small>
              </label>

              <label className="system-user-page__field">
                <span>手机号</span>
                <input value={formState.phone} onChange={(event) => setFormState((current) => ({ ...current, phone: event.target.value }))} />
                <small>{getFieldError(formError?.fieldErrors, 'phone')}</small>
              </label>

              <label className="system-user-page__field">
                <span>角色</span>
                <select
                  value={formState.roleCode}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      roleCode: event.target.value,
                      warehouseId:
                        roleOptions.find((option) => option.roleCode === event.target.value)?.warehouseRequired
                          ? current.warehouseId
                          : '',
                    }))
                  }
                >
                  {roleOptions.map((option) => (
                    <option key={option.roleCode} value={option.roleCode}>
                      {option.roleName}
                    </option>
                  ))}
                </select>
              </label>

              <label className="system-user-page__field">
                <span>负责仓库</span>
                <select
                  value={formState.warehouseId}
                  onChange={(event) => setFormState((current) => ({ ...current, warehouseId: event.target.value }))}
                  disabled={!currentRoleOption?.warehouseRequired}
                >
                  <option value="">{currentRoleOption?.warehouseRequired ? '请选择仓库' : '全仓权限'}</option>
                  {warehouseOptions.map((option) => (
                    <option key={option.id} value={option.id}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </label>

              <label className="system-user-page__field">
                <span>状态</span>
                <select value={formState.status} onChange={(event) => setFormState((current) => ({ ...current, status: event.target.value }))}>
                  <option value="1">启用</option>
                  <option value="0">停用</option>
                </select>
              </label>

              {dialogMode === 'create' ? (
                <label className="system-user-page__field">
                  <span>初始密码</span>
                  <input
                    type="password"
                    value={formState.initialPassword}
                    onChange={(event) => setFormState((current) => ({ ...current, initialPassword: event.target.value }))}
                  />
                  <small>{getFieldError(formError?.fieldErrors, 'initialPassword')}</small>
                </label>
              ) : null}

              <label className="system-user-page__field system-user-page__field--full">
                <span>备注</span>
                <textarea value={formState.remarks} onChange={(event) => setFormState((current) => ({ ...current, remarks: event.target.value }))} />
              </label>

              <div className="system-user-page__dialog-actions">
                <button type="button" className="is-ghost" onClick={() => setIsDialogOpen(false)}>
                  取消
                </button>
                <button type="submit" className="is-dark" disabled={isSubmitting}>
                  {isSubmitting ? '提交中...' : '保存'}
                </button>
              </div>
            </form>
          </div>
        </div>
      ) : null}

      {resetPasswordTarget ? (
        <div className="system-user-page__dialog-backdrop">
          <div className="system-user-page__dialog system-user-page__dialog--small">
            <div className="system-user-page__dialog-header">
              <div>
                <h3>重置密码</h3>
                <p>{resetPasswordTarget.displayName}</p>
              </div>
              <button type="button" className="system-user-page__dialog-close" onClick={() => setResetPasswordTarget(null)}>
                ×
              </button>
            </div>
            <div className="system-user-page__dialog-form">
              <label className="system-user-page__field system-user-page__field--full">
                <span>新密码</span>
                <input type="password" value={resetPasswordValue} onChange={(event) => setResetPasswordValue(event.target.value)} />
              </label>
              <div className="system-user-page__dialog-actions">
                <button type="button" className="is-ghost" onClick={() => setResetPasswordTarget(null)}>
                  取消
                </button>
                <button type="button" className="is-dark" disabled={!resetPasswordValue.trim()} onClick={() => void handleResetPassword()}>
                  确认重置
                </button>
              </div>
            </div>
          </div>
        </div>
      ) : null}
    </section>
  )
}

function toTableRows(rows: SystemUserListItem[]): SystemUserRow[] {
  return rows.map((row) => ({ ...row }))
}

function buildQueryParams(queryForm: {
  username: string
  displayName: string
  roleCode: string
  warehouseId: string
  status: string
}): SystemUserListQuery {
  return {
    username: queryForm.username.trim() || undefined,
    displayName: queryForm.displayName.trim() || undefined,
    roleCode: queryForm.roleCode || undefined,
    warehouseId: queryForm.warehouseId || undefined,
    status: queryForm.status ? Number(queryForm.status) : undefined,
  }
}

function mapDetailToFormState(detail: SystemUserDetail): SystemUserFormState {
  return {
    username: detail.username,
    displayName: detail.displayName,
    phone: detail.phone,
    roleCode: detail.roleCode,
    warehouseId: detail.warehouseId ?? '',
    status: String(detail.status),
    initialPassword: '',
    remarks: detail.remarks ?? '',
  }
}

function mapCreatePayload(formState: SystemUserFormState): SystemUserCreatePayload {
  return {
    username: formState.username.trim(),
    displayName: formState.displayName.trim(),
    phone: formState.phone.trim(),
    roleCode: formState.roleCode,
    warehouseId: formState.warehouseId ? Number(formState.warehouseId) : null,
    status: Number(formState.status),
    initialPassword: formState.initialPassword.trim(),
    remarks: formState.remarks.trim() || null,
  }
}

function mapUpdatePayload(formState: SystemUserFormState): SystemUserUpdatePayload {
  return {
    displayName: formState.displayName.trim(),
    phone: formState.phone.trim(),
    roleCode: formState.roleCode,
    warehouseId: formState.warehouseId ? Number(formState.warehouseId) : null,
    status: Number(formState.status),
    remarks: formState.remarks.trim() || null,
  }
}

function formatDateTime(value: string) {
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value))
}

export default SystemUserPage
