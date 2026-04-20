import { type FormEvent, useEffect, useState } from 'react'

import { getFieldError, normalizeError, type AppError } from '../../api/errors'
import TreeDataTable, {
  type TreeTableColumn,
  type TreeTableRow,
} from '../../components/tree-data-table/TreeDataTable'
import {
  cancelInboundOrder,
  confirmInboundOrderArrival,
  createInboundOrder,
  getInboundOrderDetail,
  getInboundOrderList,
  updateInboundOrder,
} from '../../features/inboundorder/api'
import type {
  InboundOrderDetail,
  InboundOrderFormPayload,
  InboundOrderListItem,
  InboundOrderListQuery,
} from '../../features/inboundorder/types'
import { getProductArchiveOptions } from '../../features/productarchive/api'
import type { ProductArchiveOption } from '../../features/productarchive/types'
import { getSupplierOptions } from '../../features/supplier/api'
import type { SupplierOption } from '../../features/supplier/types'
import { getWarehouseOptions } from '../../features/warehouse/api'
import type { WarehouseOption } from '../../features/warehouse/types'
import {
  preventNativeNumberInputStep,
  preventNativeNumberInputWheel,
} from '../../utils/number-input'
import { buildQuantityStep } from '../../utils/quantity'
import './InboundOrderPage.css'

type InboundOrderRow = TreeTableRow & InboundOrderListItem

interface InboundOrderFormItemState {
  productId: string
  quantity: string
  sortOrder: string
  remarks: string
}

interface InboundOrderFormState {
  supplierId: string
  warehouseId: string
  expectedArrivalAt: string
  remarks: string
  items: InboundOrderFormItemState[]
}

const initialItemState: InboundOrderFormItemState = {
  productId: '',
  quantity: '',
  sortOrder: '0',
  remarks: '',
}

const initialFormState: InboundOrderFormState = {
  supplierId: '',
  warehouseId: '',
  expectedArrivalAt: '',
  remarks: '',
  items: [{ ...initialItemState, sortOrder: '1' }],
}

function InboundOrderPage() {
  const [queryForm, setQueryForm] = useState({
    orderCode: '',
    supplierId: '',
    warehouseId: '',
    status: '',
  })
  const [inboundOrders, setInboundOrders] = useState<InboundOrderRow[]>([])
  const [supplierOptions, setSupplierOptions] = useState<SupplierOption[]>([])
  const [warehouseOptions, setWarehouseOptions] = useState<WarehouseOption[]>([])
  const [productOptions, setProductOptions] = useState<ProductArchiveOption[]>([])
  const [pageError, setPageError] = useState<AppError | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  const [isDialogOpen, setIsDialogOpen] = useState(false)
  const [dialogMode, setDialogMode] = useState<'create' | 'edit'>('create')
  const [editingId, setEditingId] = useState<string | null>(null)
  const [formState, setFormState] = useState<InboundOrderFormState>(initialFormState)
  const [formError, setFormError] = useState<AppError | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)

  useEffect(() => {
    let isMounted = true

    const bootstrap = async () => {
      try {
        const [list, suppliers, warehouses, products] = await Promise.all([
          getInboundOrderList(),
          getSupplierOptions(),
          getWarehouseOptions(),
          getProductArchiveOptions(),
        ])

        if (!isMounted) {
          return
        }

        setInboundOrders(toTableRows(list))
        setSupplierOptions(suppliers)
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

  async function loadInboundOrderList(query: InboundOrderListQuery) {
    setIsLoading(true)
    setPageError(null)

    try {
      const list = await getInboundOrderList(query)
      setInboundOrders(toTableRows(list))
    } catch (error) {
      setPageError(normalizeError(error))
    } finally {
      setIsLoading(false)
    }
  }

  const resolveProductPrecision = (productId: string) =>
    productOptions.find((option) => option.id === productId)?.precisionDigits

  const columns: TreeTableColumn<InboundOrderRow>[] = [
    {
      key: 'code',
      title: '入库单编号',
      minWidth: 180,
      render: (row) => row.orderCode,
    },
    {
      key: 'supplier',
      title: '供应商',
      minWidth: 200,
      render: (row) => row.supplierName,
    },
    {
      key: 'warehouse',
      title: '仓库',
      minWidth: 180,
      render: (row) => row.warehouseName,
    },
    {
      key: 'expectedArrivalAt',
      title: '预计到货',
      minWidth: 170,
      render: (row) => formatDateTime(row.expectedArrivalAt),
    },
    {
      key: 'actualArrivalAt',
      title: '实际到货',
      minWidth: 170,
      render: (row) => (row.actualArrivalAt ? formatDateTime(row.actualArrivalAt) : '-'),
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
          className={`inbound-order-page__status${
            row.status === 3 ? ' is-complete' : row.status === 4 ? ' is-cancelled' : ''
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
      minWidth: 320,
      width: 320,
      sticky: 'right',
      align: 'right',
      render: (row) => (
        <div className="inbound-order-page__row-actions">
          {row.status === 1 ? (
            <>
              <button type="button" onClick={() => handleEdit(row.id)}>
                编辑
              </button>
              <button type="button" onClick={() => handleArrive(row)}>
                到货确认
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
    await loadInboundOrderList(buildQueryParams(queryForm))
  }

  const handleReset = async () => {
    const resetState = {
      orderCode: '',
      supplierId: '',
      warehouseId: '',
      status: '',
    }

    setQueryForm(resetState)
    await loadInboundOrderList({})
  }

  const handleCreate = () => {
    setDialogMode('create')
    setEditingId(null)
    setFormError(null)
    setFormState(initialFormState)
    setIsDialogOpen(true)
  }

  const handleEdit = async (inboundOrderId: string) => {
    setDialogMode('edit')
    setEditingId(inboundOrderId)
    setFormError(null)
    setIsDialogOpen(true)
    setIsSubmitting(true)

    try {
      const detail = await getInboundOrderDetail(inboundOrderId)
      setFormState(mapDetailToFormState(detail))
    } catch (error) {
      setFormError(normalizeError(error))
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleArrive = async (row: InboundOrderRow) => {
    const confirmed = window.confirm(`确认“${row.orderCode}”已到货吗？`)

    if (!confirmed) {
      return
    }

    try {
      await confirmInboundOrderArrival(row.id)
      await loadInboundOrderList(buildQueryParams(queryForm))
    } catch (error) {
      setPageError(normalizeError(error))
    }
  }

  const handleCancel = async (row: InboundOrderRow) => {
    const confirmed = window.confirm(`确定要取消入库单“${row.orderCode}”吗？`)

    if (!confirmed) {
      return
    }

    try {
      await cancelInboundOrder(row.id)
      await loadInboundOrderList(buildQueryParams(queryForm))
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
        await createInboundOrder(payload)
      } else if (editingId) {
        await updateInboundOrder(editingId, payload)
      }

      setIsDialogOpen(false)
      setFormState(initialFormState)
      await loadInboundOrderList(buildQueryParams(queryForm))
    } catch (error) {
      setFormError(normalizeError(error))
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleItemChange = (
    index: number,
    field: keyof InboundOrderFormItemState,
    value: string,
  ) => {
    setFormState((current) => ({
      ...current,
      items: current.items.map((item, itemIndex) =>
        itemIndex === index ? { ...item, [field]: value } : item,
      ),
    }))
  }

  const handleAddItem = () => {
    setFormState((current) => ({
      ...current,
      items: [
        ...current.items,
        { ...initialItemState, sortOrder: String(current.items.length + 1) },
      ],
    }))
  }

  const handleRemoveItem = (index: number) => {
    setFormState((current) => {
      const nextItems = current.items.filter((_, itemIndex) => itemIndex !== index)
      return {
        ...current,
        items:
          nextItems.length > 0
            ? nextItems
            : [{ ...initialItemState, sortOrder: '1' }],
      }
    })
  }

  const itemFieldError = getFieldError(formError?.fieldErrors, 'items[0].productId')

  return (
    <div className="inbound-order-page">
      {pageError ? (
        <div className="inbound-order-page__message inbound-order-page__message--error">
          <strong>{pageError.message}</strong>
          {pageError.traceId ? <span>traceId: {pageError.traceId}</span> : null}
        </div>
      ) : null}

      <section className="inbound-order-page__search">
        <div className="inbound-order-page__search-grid">
          <label className="inbound-order-page__field">
            <span>入库单编号</span>
            <input
              type="text"
              placeholder="输入编号..."
              value={queryForm.orderCode}
              onChange={(event) =>
                setQueryForm((current) => ({
                  ...current,
                  orderCode: event.target.value,
                }))
              }
            />
          </label>

          <label className="inbound-order-page__field">
            <span>供应商</span>
            <select
              value={queryForm.supplierId}
              onChange={(event) =>
                setQueryForm((current) => ({
                  ...current,
                  supplierId: event.target.value,
                }))
              }
            >
              <option value="">全部供应商</option>
              {supplierOptions.map((option) => (
                <option key={option.id} value={option.id}>
                  {option.label}
                </option>
              ))}
            </select>
          </label>

          <label className="inbound-order-page__field">
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

          <label className="inbound-order-page__field">
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
              <option value="1">待到货</option>
              <option value="2">待上架</option>
              <option value="3">已完成</option>
              <option value="4">已取消</option>
            </select>
          </label>

          <div className="inbound-order-page__actions">
            <button type="button" className="is-ghost" onClick={() => void handleReset()}>
              重置
            </button>
            <button type="button" className="is-dark" onClick={() => void handleSearch()}>
              查询
            </button>
          </div>
        </div>
      </section>

      <section className="inbound-order-page__toolbar">
        <div className="inbound-order-page__toolbar-copy">
          <h3>入库单列表</h3>
          <p>维护到货前的入库业务单据，关联供应商、仓库和产品明细，为后续上架任务生成提供业务入口。</p>
        </div>

        <button type="button" className="inbound-order-page__primary" onClick={handleCreate}>
          新增入库单
        </button>
      </section>

      <section className="inbound-order-page__table-shell">
        {isLoading ? (
          <div className="inbound-order-page__loading">正在加载入库单...</div>
        ) : (
          <TreeDataTable
            data={inboundOrders}
            columns={columns}
            emptyText="暂无入库单数据"
          />
        )}
      </section>

      {isDialogOpen ? (
        <div className="inbound-order-page__dialog-backdrop">
          <div className="inbound-order-page__dialog">
            <div className="inbound-order-page__dialog-header">
              <div>
                <h3>{dialogMode === 'create' ? '新增入库单' : '编辑入库单'}</h3>
                <p>选择供应商、仓库并维护入库商品明细。</p>
              </div>
              <button
                type="button"
                className="inbound-order-page__dialog-close"
                onClick={() => setIsDialogOpen(false)}
              >
                ×
              </button>
            </div>

            <form className="inbound-order-page__dialog-form" onSubmit={handleDialogSubmit}>
              <label className="inbound-order-page__field">
                <span>供应商</span>
                <select
                  value={formState.supplierId}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      supplierId: event.target.value,
                    }))
                  }
                >
                  <option value="">请选择</option>
                  {supplierOptions.map((option) => (
                    <option key={option.id} value={option.id}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </label>

              <label className="inbound-order-page__field">
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
                  <option value="">请选择</option>
                  {warehouseOptions.map((option) => (
                    <option key={option.id} value={option.id}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </label>

              <label className="inbound-order-page__field">
                <span>预计到货时间</span>
                <input
                  type="datetime-local"
                  value={formState.expectedArrivalAt}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      expectedArrivalAt: event.target.value,
                    }))
                  }
                />
              </label>

              <label className="inbound-order-page__field inbound-order-page__field--full">
                <span>备注</span>
                <textarea
                  rows={3}
                  value={formState.remarks}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      remarks: event.target.value,
                    }))
                  }
                />
              </label>

              <div className="inbound-order-page__items">
                <div className="inbound-order-page__items-header">
                  <h4>入库明细</h4>
                  <button type="button" onClick={handleAddItem}>
                    新增明细
                  </button>
                </div>

                {formState.items.map((item, index) => (
                  <div key={`${index}-${item.sortOrder}`} className="inbound-order-page__item-row">
                    <label className="inbound-order-page__field">
                      <span>产品</span>
                      <select
                        value={item.productId}
                        onChange={(event) =>
                          handleItemChange(index, 'productId', event.target.value)
                        }
                      >
                        <option value="">请选择</option>
                        {productOptions.map((option) => (
                          <option key={option.id} value={option.id}>
                            {option.label}（{option.unitName}/{option.unitSymbol}）
                          </option>
                        ))}
                      </select>
                    </label>

                    <label className="inbound-order-page__field">
                      <span>数量</span>
                      <input
                        type="number"
                        step={buildQuantityStep(resolveProductPrecision(item.productId))}
                        value={item.quantity}
                        onWheel={preventNativeNumberInputWheel}
                        onKeyDown={preventNativeNumberInputStep}
                        onChange={(event) =>
                          handleItemChange(index, 'quantity', event.target.value)
                        }
                      />
                    </label>

                    <label className="inbound-order-page__field">
                      <span>排序值</span>
                      <input
                        type="number"
                        value={item.sortOrder}
                        onChange={(event) =>
                          handleItemChange(index, 'sortOrder', event.target.value)
                        }
                      />
                    </label>

                    <label className="inbound-order-page__field inbound-order-page__field--full">
                      <span>备注</span>
                      <input
                        value={item.remarks}
                        onChange={(event) =>
                          handleItemChange(index, 'remarks', event.target.value)
                        }
                      />
                    </label>

                    <button
                      type="button"
                      className="inbound-order-page__item-remove"
                      onClick={() => handleRemoveItem(index)}
                    >
                      删除明细
                    </button>
                  </div>
                ))}

                {itemFieldError ? (
                  <small className="inbound-order-page__field-error">{itemFieldError}</small>
                ) : null}
              </div>

              {formError && !formError.fieldErrors.length ? (
                <div className="inbound-order-page__message inbound-order-page__message--error">
                  <strong>{formError.message}</strong>
                  {formError.traceId ? <span>traceId: {formError.traceId}</span> : null}
                </div>
              ) : null}

              <div className="inbound-order-page__dialog-actions">
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
  orderCode: string
  supplierId: string
  warehouseId: string
  status: string
}): InboundOrderListQuery {
  return {
    orderCode: queryForm.orderCode.trim() || undefined,
    supplierId: queryForm.supplierId || undefined,
    warehouseId: queryForm.warehouseId || undefined,
    status: queryForm.status ? Number(queryForm.status) : undefined,
  }
}

function mapDetailToFormState(detail: InboundOrderDetail): InboundOrderFormState {
  return {
    supplierId: detail.supplierId,
    warehouseId: detail.warehouseId,
    expectedArrivalAt: toDateTimeLocal(detail.expectedArrivalAt),
    remarks: detail.remarks ?? '',
    items: detail.items.map((item) => ({
      productId: item.productId,
      quantity: String(item.quantity),
      sortOrder: String(item.sortOrder),
      remarks: item.remarks ?? '',
    })),
  }
}

function mapFormStateToPayload(formState: InboundOrderFormState): InboundOrderFormPayload {
  return {
    supplierId: Number(formState.supplierId),
    warehouseId: Number(formState.warehouseId),
    expectedArrivalAt: formState.expectedArrivalAt,
    remarks: formState.remarks.trim() || null,
    items: formState.items.map((item, index) => ({
      productId: Number(item.productId),
      quantity: Number(item.quantity),
      sortOrder: item.sortOrder ? Number(item.sortOrder) : index + 1,
      remarks: item.remarks.trim() || null,
    })),
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

function toDateTimeLocal(value: string) {
  const date = new Date(value)

  if (Number.isNaN(date.getTime())) {
    return value
  }

  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day}T${hour}:${minute}`
}

function toTableRows(inboundOrderList: InboundOrderListItem[]): InboundOrderRow[] {
  return inboundOrderList.map((item) => ({
    ...item,
    children: [],
  }))
}

export default InboundOrderPage
