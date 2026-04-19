import { type FormEvent, useEffect, useState } from 'react'

import { getFieldError, normalizeError, type AppError } from '../../api/errors'
import TreeDataTable, {
  type TreeTableColumn,
  type TreeTableRow,
} from '../../components/tree-data-table/TreeDataTable'
import { getCustomerOptions } from '../../features/customer/api'
import type { CustomerOption } from '../../features/customer/types'
import {
  cancelOutboundOrder,
  createOutboundOrder,
  dispatchOutboundOrder,
  getOutboundOrderDetail,
  getOutboundOrderList,
  updateOutboundOrder,
} from '../../features/outboundorder/api'
import type {
  OutboundOrderDetail,
  OutboundOrderFormPayload,
  OutboundOrderListItem,
  OutboundOrderListQuery,
} from '../../features/outboundorder/types'
import { getProductArchiveOptions } from '../../features/productarchive/api'
import type { ProductArchiveOption } from '../../features/productarchive/types'
import { getWarehouseOptions } from '../../features/warehouse/api'
import type { WarehouseOption } from '../../features/warehouse/types'
import { buildQuantityStep } from '../../utils/quantity'
import './OutboundOrderPage.css'

type OutboundOrderRow = TreeTableRow & OutboundOrderListItem

interface OutboundOrderFormItemState {
  productId: string
  quantity: string
  sortOrder: string
  remarks: string
}

interface OutboundOrderFormState {
  customerId: string
  warehouseId: string
  expectedDeliveryAt: string
  remarks: string
  items: OutboundOrderFormItemState[]
}

const initialItemState: OutboundOrderFormItemState = {
  productId: '',
  quantity: '',
  sortOrder: '0',
  remarks: '',
}

const initialFormState: OutboundOrderFormState = {
  customerId: '',
  warehouseId: '',
  expectedDeliveryAt: '',
  remarks: '',
  items: [{ ...initialItemState, sortOrder: '1' }],
}

function OutboundOrderPage() {
  const [queryForm, setQueryForm] = useState({
    orderCode: '',
    customerId: '',
    warehouseId: '',
    status: '',
  })
  const [outboundOrders, setOutboundOrders] = useState<OutboundOrderRow[]>([])
  const [customerOptions, setCustomerOptions] = useState<CustomerOption[]>([])
  const [warehouseOptions, setWarehouseOptions] = useState<WarehouseOption[]>([])
  const [productOptions, setProductOptions] = useState<ProductArchiveOption[]>([])
  const [pageError, setPageError] = useState<AppError | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  const [isDialogOpen, setIsDialogOpen] = useState(false)
  const [dialogMode, setDialogMode] = useState<'create' | 'edit'>('create')
  const [editingId, setEditingId] = useState<string | null>(null)
  const [formState, setFormState] = useState<OutboundOrderFormState>(initialFormState)
  const [formError, setFormError] = useState<AppError | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)

  useEffect(() => {
    let isMounted = true

    const bootstrap = async () => {
      try {
        const [list, customers, warehouses, products] = await Promise.all([
          getOutboundOrderList(),
          getCustomerOptions(),
          getWarehouseOptions(),
          getProductArchiveOptions(),
        ])

        if (!isMounted) {
          return
        }

        setOutboundOrders(toTableRows(list))
        setCustomerOptions(customers)
        setWarehouseOptions(warehouses)
        setProductOptions(products)
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

  async function loadOutboundOrderList(query: OutboundOrderListQuery) {
    setIsLoading(true)
    setPageError(null)

    try {
      const list = await getOutboundOrderList(query)
      setOutboundOrders(toTableRows(list))
    } catch (error) {
      setPageError(normalizeError(error))
    } finally {
      setIsLoading(false)
    }
  }

  const resolveProductPrecision = (productId: string) =>
    productOptions.find((option) => option.id === productId)?.precisionDigits

  const columns: TreeTableColumn<OutboundOrderRow>[] = [
    {
      key: 'code',
      title: '出库单编号',
      minWidth: 180,
      render: (row) => row.orderCode,
    },
    {
      key: 'customer',
      title: '客户',
      minWidth: 200,
      render: (row) => row.customerName,
    },
    {
      key: 'warehouse',
      title: '仓库',
      minWidth: 180,
      render: (row) => row.warehouseName,
    },
    {
      key: 'expectedDeliveryAt',
      title: '预计发货',
      minWidth: 170,
      render: (row) => formatDateTime(row.expectedDeliveryAt),
    },
    {
      key: 'actualOutboundAt',
      title: '实际出库',
      minWidth: 170,
      render: (row) => (row.actualOutboundAt ? formatDateTime(row.actualOutboundAt) : '-'),
    },
    {
      key: 'quantity',
      title: '总数量',
      minWidth: 120,
      render: (row) => row.totalQuantity,
    },
    {
      key: 'status',
      title: '状态',
      minWidth: 120,
      render: (row) => (
        <span
          className={`outbound-order-page__status${
            row.status === 4 ? ' is-complete' : row.status === 5 ? ' is-cancelled' : ''
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
        <div className="outbound-order-page__row-actions">
          {row.status === 1 ? (
            <>
              <button type="button" onClick={() => handleEdit(row.id)}>
                编辑
              </button>
              <button type="button" onClick={() => handleDispatch(row)}>
                生成任务
              </button>
              <button type="button" onClick={() => handleCancel(row)}>
                取消
              </button>
            </>
          ) : null}
        </div>
      ),
    },
  ]

  const handleSearch = async () => {
    await loadOutboundOrderList(buildQueryParams(queryForm))
  }

  const handleReset = async () => {
    const resetState = {
      orderCode: '',
      customerId: '',
      warehouseId: '',
      status: '',
    }

    setQueryForm(resetState)
    await loadOutboundOrderList({})
  }

  const handleCreate = () => {
    setDialogMode('create')
    setEditingId(null)
    setFormError(null)
    setFormState(initialFormState)
    setIsDialogOpen(true)
  }

  const handleEdit = async (outboundOrderId: string) => {
    setDialogMode('edit')
    setEditingId(outboundOrderId)
    setFormError(null)
    setIsDialogOpen(true)
    setIsSubmitting(true)

    try {
      const detail = await getOutboundOrderDetail(outboundOrderId)
      setFormState(mapDetailToFormState(detail))
    } catch (error) {
      setFormError(normalizeError(error))
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleCancel = async (row: OutboundOrderRow) => {
    const confirmed = window.confirm(`确认取消出库单“${row.orderCode}”吗？`)

    if (!confirmed) {
      return
    }

    try {
      await cancelOutboundOrder(row.id)
      await loadOutboundOrderList(buildQueryParams(queryForm))
    } catch (error) {
      setPageError(normalizeError(error))
    }
  }

  const handleDispatch = async (row: OutboundOrderRow) => {
    const confirmed = window.confirm(`确认基于“${row.orderCode}”生成拣货任务吗？`)

    if (!confirmed) {
      return
    }

    try {
      await dispatchOutboundOrder(row.id)
      await loadOutboundOrderList(buildQueryParams(queryForm))
    } catch (error) {
      setPageError(normalizeError(error))
    }
  }

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setFormError(null)
    setIsSubmitting(true)

    try {
      const payload = mapFormStateToPayload(formState)

      if (dialogMode === 'create') {
        await createOutboundOrder(payload)
      } else if (editingId) {
        await updateOutboundOrder(editingId, payload)
      }

      setIsDialogOpen(false)
      setFormState(initialFormState)
      await loadOutboundOrderList(buildQueryParams(queryForm))
    } catch (error) {
      setFormError(normalizeError(error))
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <section className="outbound-order-page">
      {pageError ? (
        <div className="outbound-order-page__message outbound-order-page__message--error">
          <strong>{pageError.message}</strong>
          {pageError.traceId ? <span>追踪编号：{pageError.traceId}</span> : null}
        </div>
      ) : null}

      <div className="outbound-order-page__search">
        <div className="outbound-order-page__search-grid">
          <label className="outbound-order-page__field">
            <span>出库单编号</span>
            <input
              type="text"
              placeholder="输入出库单编号"
              value={queryForm.orderCode}
              onChange={(event) =>
                setQueryForm((current) => ({
                  ...current,
                  orderCode: event.target.value,
                }))
              }
            />
          </label>

          <label className="outbound-order-page__field">
            <span>客户</span>
            <select
              value={queryForm.customerId}
              onChange={(event) =>
                setQueryForm((current) => ({
                  ...current,
                  customerId: event.target.value,
                }))
              }
            >
              <option value="">全部客户</option>
              {customerOptions.map((option) => (
                <option key={option.id} value={option.id}>
                  {option.label}
                </option>
              ))}
            </select>
          </label>

          <label className="outbound-order-page__field">
            <span>仓库</span>
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

          <label className="outbound-order-page__field">
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
              <option value="1">待分配</option>
              <option value="2">待拣货</option>
              <option value="3">待出库</option>
              <option value="4">已完成</option>
              <option value="5">已取消</option>
            </select>
          </label>

          <div className="outbound-order-page__actions">
            <button type="button" className="is-ghost" onClick={() => void handleReset()}>
              重置
            </button>
            <button type="button" className="is-dark" onClick={() => void handleSearch()}>
              查询
            </button>
          </div>
        </div>
      </div>

      <div className="outbound-order-page__toolbar">
        <div className="outbound-order-page__toolbar-copy">
          <h3>出库单列表</h3>
          <p>维护待出库业务单据，统一关联客户、仓库和产品明细，为后续拣货任务提供业务入口。</p>
        </div>

        <button type="button" className="outbound-order-page__primary" onClick={handleCreate}>
          新增出库单
        </button>
      </div>

      <div className="outbound-order-page__table-shell">
        {isLoading ? (
          <div className="outbound-order-page__loading">正在加载出库单...</div>
        ) : (
          <TreeDataTable
            data={outboundOrders}
            columns={columns}
            emptyText="暂无出库单"
          />
        )}
      </div>

      {isDialogOpen ? (
        <div className="outbound-order-page__dialog-backdrop">
          <div className="outbound-order-page__dialog">
            <div className="outbound-order-page__dialog-header">
              <div>
                <h3>{dialogMode === 'create' ? '新增出库单' : '编辑出库单'}</h3>
                <p>维护客户、仓库和出库商品明细，后续拣货任务会基于该单据生成。</p>
              </div>
              <button
                type="button"
                className="outbound-order-page__dialog-close"
                onClick={() => {
                  setIsDialogOpen(false)
                  setFormError(null)
                }}
              >
                ×
              </button>
            </div>

            <form className="outbound-order-page__dialog-form" onSubmit={handleSubmit}>
              {formError ? (
                <div className="outbound-order-page__message outbound-order-page__message--error outbound-order-page__field--full">
                  <strong>{formError.message}</strong>
                  {formError.traceId ? <span>追踪编号：{formError.traceId}</span> : null}
                </div>
              ) : null}

              <label className="outbound-order-page__field">
                <span>客户</span>
                <select
                  value={formState.customerId}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      customerId: event.target.value,
                    }))
                  }
                >
                  <option value="">选择客户</option>
                  {customerOptions.map((option) => (
                    <option key={option.id} value={option.id}>
                      {option.label}
                    </option>
                  ))}
                </select>
                <small>{getFieldError(formError?.fieldErrors, 'customerId')}</small>
              </label>

              <label className="outbound-order-page__field">
                <span>仓库</span>
                <select
                  value={formState.warehouseId}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      warehouseId: event.target.value,
                    }))
                  }
                >
                  <option value="">选择仓库</option>
                  {warehouseOptions.map((option) => (
                    <option key={option.id} value={option.id}>
                      {option.label}
                    </option>
                  ))}
                </select>
                <small>{getFieldError(formError?.fieldErrors, 'warehouseId')}</small>
              </label>

              <label className="outbound-order-page__field">
                <span>预计发货时间</span>
                <input
                  type="datetime-local"
                  value={formState.expectedDeliveryAt}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      expectedDeliveryAt: event.target.value,
                    }))
                  }
                />
                <small>{getFieldError(formError?.fieldErrors, 'expectedDeliveryAt')}</small>
              </label>

              <label className="outbound-order-page__field outbound-order-page__field--full">
                <span>备注</span>
                <textarea
                  value={formState.remarks}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      remarks: event.target.value,
                    }))
                  }
                  placeholder="填写出库单备注"
                />
              </label>

              <div className="outbound-order-page__items outbound-order-page__field--full">
                <div className="outbound-order-page__items-header">
                  <h4>商品明细</h4>
                  <button
                    type="button"
                    onClick={() =>
                      setFormState((current) => ({
                        ...current,
                        items: [
                          ...current.items,
                          {
                            ...initialItemState,
                            sortOrder: String(current.items.length + 1),
                          },
                        ],
                      }))
                    }
                  >
                    新增明细
                  </button>
                </div>

                <div className="outbound-order-page__items-list">
                  {formState.items.map((item, index) => (
                    <div key={`${index}-${item.productId}`} className="outbound-order-page__item-card">
                      <label className="outbound-order-page__field">
                        <span>产品</span>
                        <select
                          value={item.productId}
                          onChange={(event) =>
                            updateFormItem(index, 'productId', event.target.value)
                          }
                        >
                          <option value="">选择产品</option>
                          {productOptions.map((option) => (
                            <option key={option.id} value={option.id}>
                              {option.label}
                            </option>
                          ))}
                        </select>
                      </label>

                      <label className="outbound-order-page__field">
                        <span>出库数量</span>
                        <input
                          type="number"
                          min="0"
                          step={buildQuantityStep(resolveProductPrecision(item.productId))}
                          value={item.quantity}
                          onChange={(event) =>
                            updateFormItem(index, 'quantity', event.target.value)
                          }
                          placeholder="输入数量"
                        />
                      </label>

                      <label className="outbound-order-page__field">
                        <span>排序值</span>
                        <input
                          type="number"
                          min="0"
                          step="1"
                          value={item.sortOrder}
                          onChange={(event) =>
                            updateFormItem(index, 'sortOrder', event.target.value)
                          }
                          placeholder="输入排序值"
                        />
                      </label>

                      <label className="outbound-order-page__field">
                        <span>备注</span>
                        <input
                          type="text"
                          value={item.remarks}
                          onChange={(event) =>
                            updateFormItem(index, 'remarks', event.target.value)
                          }
                          placeholder="填写备注"
                        />
                      </label>

                      <button
                        type="button"
                        className="outbound-order-page__item-remove"
                        onClick={() =>
                          setFormState((current) => ({
                            ...current,
                            items:
                              current.items.length === 1
                                ? current.items
                                : current.items.filter((_, itemIndex) => itemIndex !== index),
                          }))
                        }
                      >
                        删除明细
                      </button>
                    </div>
                  ))}
                </div>
                <small>{getFieldError(formError?.fieldErrors, 'items')}</small>
              </div>

              <div className="outbound-order-page__dialog-actions">
                <button
                  type="button"
                  className="is-ghost"
                  onClick={() => {
                    setIsDialogOpen(false)
                    setFormError(null)
                  }}
                >
                  取消
                </button>
                <button type="submit" className="is-dark" disabled={isSubmitting}>
                  {isSubmitting ? '提交中...' : dialogMode === 'create' ? '保存出库单' : '更新出库单'}
                </button>
              </div>
            </form>
          </div>
        </div>
      ) : null}
    </section>
  )

  function updateFormItem(
    index: number,
    field: keyof OutboundOrderFormItemState,
    value: string,
  ) {
    setFormState((current) => ({
      ...current,
      items: current.items.map((item, itemIndex) =>
        itemIndex === index
          ? {
              ...item,
              [field]: value,
            }
          : item,
      ),
    }))
  }
}

function toTableRows(rows: OutboundOrderListItem[]): OutboundOrderRow[] {
  return rows.map((row) => ({
    ...row,
  }))
}

function buildQueryParams(queryForm: {
  orderCode: string
  customerId: string
  warehouseId: string
  status: string
}): OutboundOrderListQuery {
  return {
    orderCode: queryForm.orderCode.trim() || undefined,
    customerId: queryForm.customerId || undefined,
    warehouseId: queryForm.warehouseId || undefined,
    status: queryForm.status ? Number(queryForm.status) : undefined,
  }
}

function mapDetailToFormState(detail: OutboundOrderDetail): OutboundOrderFormState {
  return {
    customerId: detail.customerId,
    warehouseId: detail.warehouseId,
    expectedDeliveryAt: toDateTimeLocalValue(detail.expectedDeliveryAt),
    remarks: detail.remarks ?? '',
    items: detail.items.map((item) => ({
      productId: item.productId,
      quantity: String(item.quantity),
      sortOrder: String(item.sortOrder),
      remarks: item.remarks ?? '',
    })),
  }
}

function mapFormStateToPayload(formState: OutboundOrderFormState): OutboundOrderFormPayload {
  return {
    customerId: Number(formState.customerId),
    warehouseId: Number(formState.warehouseId),
    expectedDeliveryAt: normalizeDateTimeLocalValue(formState.expectedDeliveryAt),
    remarks: formState.remarks.trim() || null,
    items: formState.items.map((item) => ({
      productId: Number(item.productId),
      quantity: Number(item.quantity),
      sortOrder: Number(item.sortOrder || '0'),
      remarks: item.remarks.trim() || null,
    })),
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

function toDateTimeLocalValue(value: string) {
  const date = new Date(value)
  const offset = date.getTimezoneOffset()
  const localDate = new Date(date.getTime() - offset * 60 * 1000)
  return localDate.toISOString().slice(0, 16)
}

function normalizeDateTimeLocalValue(value: string) {
  return value.length === 16 ? `${value}:00` : value
}

export default OutboundOrderPage
