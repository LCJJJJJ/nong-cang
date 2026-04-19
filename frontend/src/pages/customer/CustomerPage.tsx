import { type FormEvent, useEffect, useState } from 'react'

import { getFieldError, normalizeError, type AppError } from '../../api/errors'
import TreeDataTable, {
  type TreeTableColumn,
  type TreeTableRow,
} from '../../components/tree-data-table/TreeDataTable'
import {
  createCustomer,
  deleteCustomer,
  getCustomerDetail,
  getCustomerList,
  updateCustomer,
  updateCustomerStatus,
} from '../../features/customer/api'
import type {
  CustomerDetail,
  CustomerFormPayload,
  CustomerListItem,
  CustomerListQuery,
} from '../../features/customer/types'
import './CustomerPage.css'

type CustomerRow = TreeTableRow & CustomerListItem

interface CustomerFormState {
  customerCode: string
  customerName: string
  customerType: string
  contactName: string
  contactPhone: string
  regionName: string
  address: string
  status: string
  sortOrder: string
  remarks: string
}

const initialFormState: CustomerFormState = {
  customerCode: '',
  customerName: '',
  customerType: '',
  contactName: '',
  contactPhone: '',
  regionName: '',
  address: '',
  status: '1',
  sortOrder: '0',
  remarks: '',
}

const customerTypeOptions = ['批发客户', '商超客户', '团购客户', '直营网点']

function CustomerPage() {
  const [queryForm, setQueryForm] = useState({
    customerCode: '',
    customerName: '',
    contactName: '',
    status: '',
  })
  const [customers, setCustomers] = useState<CustomerRow[]>([])
  const [pageError, setPageError] = useState<AppError | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  const [isDialogOpen, setIsDialogOpen] = useState(false)
  const [dialogMode, setDialogMode] = useState<'create' | 'edit'>('create')
  const [editingId, setEditingId] = useState<string | null>(null)
  const [formState, setFormState] = useState<CustomerFormState>(initialFormState)
  const [formError, setFormError] = useState<AppError | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)

  useEffect(() => {
    let isMounted = true

    const bootstrap = async () => {
      try {
        const list = await getCustomerList()

        if (!isMounted) {
          return
        }

        setCustomers(toTableRows(list))
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

  async function loadCustomerList(query: CustomerListQuery) {
    setIsLoading(true)
    setPageError(null)

    try {
      const list = await getCustomerList(query)
      setCustomers(toTableRows(list))
    } catch (error) {
      setPageError(normalizeError(error))
    } finally {
      setIsLoading(false)
    }
  }

  const columns: TreeTableColumn<CustomerRow>[] = [
    {
      key: 'name',
      title: '客户名称',
      minWidth: 260,
      render: (row) => (
        <div className="customer-page__name-cell">
          <strong>{row.customerName}</strong>
          <span>{row.customerType}</span>
        </div>
      ),
    },
    {
      key: 'code',
      title: '客户编号',
      minWidth: 180,
      render: (row) => row.customerCode,
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
          className={`customer-page__status${
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
        <div className="customer-page__row-actions">
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
    await loadCustomerList(buildQueryParams(queryForm))
  }

  const handleReset = async () => {
    const resetState = {
      customerCode: '',
      customerName: '',
      contactName: '',
      status: '',
    }

    setQueryForm(resetState)
    await loadCustomerList({})
  }

  const handleCreate = () => {
    setDialogMode('create')
    setEditingId(null)
    setFormError(null)
    setFormState(initialFormState)
    setIsDialogOpen(true)
  }

  const handleEdit = async (customerId: string) => {
    setDialogMode('edit')
    setEditingId(customerId)
    setFormError(null)
    setIsDialogOpen(true)
    setIsSubmitting(true)

    try {
      const detail = await getCustomerDetail(customerId)
      setFormState(mapDetailToFormState(detail))
    } catch (error) {
      setFormError(normalizeError(error))
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleToggleStatus = async (row: CustomerRow) => {
    const nextStatus = row.status === 1 ? 0 : 1
    const confirmed = window.confirm(
      `确定要将“${row.customerName}”${nextStatus === 1 ? '启用' : '停用'}吗？`,
    )

    if (!confirmed) {
      return
    }

    try {
      await updateCustomerStatus(row.id, nextStatus)
      await loadCustomerList(buildQueryParams(queryForm))
    } catch (error) {
      setPageError(normalizeError(error))
    }
  }

  const handleDelete = async (row: CustomerRow) => {
    const confirmed = window.confirm(`确定要删除客户“${row.customerName}”吗？`)

    if (!confirmed) {
      return
    }

    try {
      await deleteCustomer(row.id)
      await loadCustomerList(buildQueryParams(queryForm))
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
        await createCustomer(payload)
      } else if (editingId) {
        await updateCustomer(editingId, payload)
      }

      setIsDialogOpen(false)
      setFormState(initialFormState)
      await loadCustomerList(buildQueryParams(queryForm))
    } catch (error) {
      setFormError(normalizeError(error))
    } finally {
      setIsSubmitting(false)
    }
  }

  const nameFieldError = getFieldError(formError?.fieldErrors, 'customerName')

  return (
    <div className="customer-page">
      {pageError ? (
        <div className="customer-page__message customer-page__message--error">
          <strong>{pageError.message}</strong>
          {pageError.traceId ? <span>traceId: {pageError.traceId}</span> : null}
        </div>
      ) : null}

      <section className="customer-page__search">
        <div className="customer-page__search-grid">
          <label className="customer-page__field">
            <span>客户编号</span>
            <input
              type="text"
              placeholder="输入编号..."
              value={queryForm.customerCode}
              onChange={(event) =>
                setQueryForm((current) => ({
                  ...current,
                  customerCode: event.target.value,
                }))
              }
            />
          </label>

          <label className="customer-page__field">
            <span>客户名称</span>
            <input
              type="text"
              placeholder="输入名称..."
              value={queryForm.customerName}
              onChange={(event) =>
                setQueryForm((current) => ({
                  ...current,
                  customerName: event.target.value,
                }))
              }
            />
          </label>

          <label className="customer-page__field">
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

          <label className="customer-page__field">
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

          <div className="customer-page__actions">
            <button type="button" className="is-ghost" onClick={() => void handleReset()}>
              重置
            </button>
            <button type="button" className="is-dark" onClick={() => void handleSearch()}>
              查询
            </button>
          </div>
        </div>
      </section>

      <section className="customer-page__toolbar">
        <div className="customer-page__toolbar-copy">
          <h3>客户列表</h3>
          <p>维护出库业务使用的客户主数据，为配送协同和客户去向分析提供统一对象口径。</p>
        </div>

        <button type="button" className="customer-page__primary" onClick={handleCreate}>
          新增客户
        </button>
      </section>

      <section className="customer-page__table-shell">
        {isLoading ? (
          <div className="customer-page__loading">正在加载客户数据...</div>
        ) : (
          <TreeDataTable data={customers} columns={columns} emptyText="暂无客户数据" />
        )}
      </section>

      {isDialogOpen ? (
        <div className="customer-page__dialog-backdrop">
          <div className="customer-page__dialog">
            <div className="customer-page__dialog-header">
              <div>
                <h3>{dialogMode === 'create' ? '新增客户' : '编辑客户'}</h3>
                <p>维护客户基本信息、联系人和地区地址。</p>
              </div>
              <button
                type="button"
                className="customer-page__dialog-close"
                onClick={() => setIsDialogOpen(false)}
              >
                ×
              </button>
            </div>

            <form className="customer-page__dialog-form" onSubmit={handleDialogSubmit}>
              <label className="customer-page__field">
                <span>客户编号</span>
                <input value={formState.customerCode} placeholder="系统自动生成" readOnly disabled />
                <small className="customer-page__field-hint">
                  {dialogMode === 'create' ? '新增时由系统自动生成编号' : '客户编号创建后不可修改'}
                </small>
              </label>

              <label className="customer-page__field">
                <span>客户名称</span>
                <input
                  value={formState.customerName}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      customerName: event.target.value,
                    }))
                  }
                />
                {nameFieldError ? (
                  <small className="customer-page__field-error">{nameFieldError}</small>
                ) : null}
              </label>

              <label className="customer-page__field">
                <span>客户类型</span>
                <select
                  value={formState.customerType}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      customerType: event.target.value,
                    }))
                  }
                >
                  <option value="">请选择</option>
                  {customerTypeOptions.map((customerType) => (
                    <option key={customerType} value={customerType}>
                      {customerType}
                    </option>
                  ))}
                </select>
              </label>

              <label className="customer-page__field">
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

              <label className="customer-page__field">
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

              <label className="customer-page__field">
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

              <label className="customer-page__field customer-page__field--full">
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

              <label className="customer-page__field">
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

              <label className="customer-page__field">
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

              <label className="customer-page__field customer-page__field--full">
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
                <div className="customer-page__message customer-page__message--error">
                  <strong>{formError.message}</strong>
                  {formError.traceId ? <span>traceId: {formError.traceId}</span> : null}
                </div>
              ) : null}

              <div className="customer-page__dialog-actions">
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
  customerCode: string
  customerName: string
  contactName: string
  status: string
}): CustomerListQuery {
  return {
    customerCode: queryForm.customerCode.trim() || undefined,
    customerName: queryForm.customerName.trim() || undefined,
    contactName: queryForm.contactName.trim() || undefined,
    status: queryForm.status ? Number(queryForm.status) : undefined,
  }
}

function mapDetailToFormState(detail: CustomerDetail): CustomerFormState {
  return {
    customerCode: detail.customerCode,
    customerName: detail.customerName,
    customerType: detail.customerType,
    contactName: detail.contactName ?? '',
    contactPhone: detail.contactPhone ?? '',
    regionName: detail.regionName ?? '',
    address: detail.address ?? '',
    status: String(detail.status),
    sortOrder: String(detail.sortOrder),
    remarks: detail.remarks ?? '',
  }
}

function mapFormStateToPayload(formState: CustomerFormState): CustomerFormPayload {
  return {
    customerName: formState.customerName.trim(),
    customerType: formState.customerType.trim(),
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

function toTableRows(customerList: CustomerListItem[]): CustomerRow[] {
  return customerList.map((item) => ({
    ...item,
    children: [],
  }))
}

export default CustomerPage
