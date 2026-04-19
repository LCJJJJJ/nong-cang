import { type FormEvent, useEffect, useMemo, useState } from 'react'

import { getFieldError, normalizeError, type AppError } from '../../api/errors'
import TreeDataTable, {
  type TreeTableColumn,
  type TreeTableRow,
} from '../../components/tree-data-table/TreeDataTable'
import {
  createInventoryAdjustment,
  getInventoryAdjustmentList,
} from '../../features/inventoryadjustment/api'
import type {
  InventoryAdjustmentFormPayload,
  InventoryAdjustmentListItem,
  InventoryAdjustmentListQuery,
} from '../../features/inventoryadjustment/types'
import { getProductArchiveOptions } from '../../features/productarchive/api'
import type { ProductArchiveOption } from '../../features/productarchive/types'
import { getWarehouseOptions } from '../../features/warehouse/api'
import type { WarehouseOption } from '../../features/warehouse/types'
import { getWarehouseLocationOptions } from '../../features/warehouselocation/api'
import type { WarehouseLocationOption } from '../../features/warehouselocation/types'
import { getWarehouseZoneOptions } from '../../features/warehousezone/api'
import type { WarehouseZoneOption } from '../../features/warehousezone/types'
import './InventoryAdjustmentPage.css'

type InventoryAdjustmentRow = TreeTableRow & InventoryAdjustmentListItem

interface InventoryAdjustmentFormState {
  warehouseId: string
  zoneId: string
  locationId: string
  productId: string
  adjustmentType: string
  quantity: string
  reason: string
  remarks: string
}

const initialFormState: InventoryAdjustmentFormState = {
  warehouseId: '',
  zoneId: '',
  locationId: '',
  productId: '',
  adjustmentType: 'INCREASE',
  quantity: '',
  reason: '',
  remarks: '',
}

function InventoryAdjustmentPage() {
  const [queryForm, setQueryForm] = useState({
    adjustmentCode: '',
    warehouseId: '',
    productId: '',
    adjustmentType: '',
  })
  const [adjustments, setAdjustments] = useState<InventoryAdjustmentRow[]>([])
  const [warehouseOptions, setWarehouseOptions] = useState<WarehouseOption[]>([])
  const [warehouseZoneOptions, setWarehouseZoneOptions] = useState<
    WarehouseZoneOption[]
  >([])
  const [warehouseLocationOptions, setWarehouseLocationOptions] = useState<
    WarehouseLocationOption[]
  >([])
  const [productOptions, setProductOptions] = useState<ProductArchiveOption[]>([])
  const [pageError, setPageError] = useState<AppError | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  const [isDialogOpen, setIsDialogOpen] = useState(false)
  const [formState, setFormState] = useState<InventoryAdjustmentFormState>(initialFormState)
  const [formError, setFormError] = useState<AppError | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)

  useEffect(() => {
    let isMounted = true

    const bootstrap = async () => {
      try {
        const [list, warehouses, zones, locations, products] = await Promise.all([
          getInventoryAdjustmentList(),
          getWarehouseOptions(),
          getWarehouseZoneOptions(),
          getWarehouseLocationOptions(),
          getProductArchiveOptions(),
        ])

        if (!isMounted) {
          return
        }

        setAdjustments(toTableRows(list))
        setWarehouseOptions(warehouses)
        setWarehouseZoneOptions(zones)
        setWarehouseLocationOptions(locations)
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

  async function loadInventoryAdjustmentList(query: InventoryAdjustmentListQuery) {
    setIsLoading(true)
    setPageError(null)

    try {
      const list = await getInventoryAdjustmentList(query)
      setAdjustments(toTableRows(list))
    } catch (error) {
      setPageError(normalizeError(error))
    } finally {
      setIsLoading(false)
    }
  }

  const filteredZoneOptions = useMemo(
    () =>
      formState.warehouseId
        ? warehouseZoneOptions.filter((option) => option.warehouseId === formState.warehouseId)
        : warehouseZoneOptions,
    [formState.warehouseId, warehouseZoneOptions],
  )

  const filteredLocationOptions = useMemo(
    () =>
      warehouseLocationOptions.filter(
        (option) =>
          option.warehouseId === formState.warehouseId && option.zoneId === formState.zoneId,
      ),
    [formState.warehouseId, formState.zoneId, warehouseLocationOptions],
  )

  const columns: TreeTableColumn<InventoryAdjustmentRow>[] = [
    {
      key: 'adjustmentCode',
      title: '调整单编号',
      minWidth: 180,
      render: (row) => row.adjustmentCode,
    },
    {
      key: 'product',
      title: '产品',
      minWidth: 220,
      render: (row) => (
        <div className="inventory-adjustment-page__name-cell">
          <strong>{row.productName}</strong>
          <span>{row.productCode}</span>
        </div>
      ),
    },
    {
      key: 'warehouse',
      title: '仓库',
      minWidth: 150,
      render: (row) => row.warehouseName,
    },
    {
      key: 'zone',
      title: '库区',
      minWidth: 150,
      render: (row) => row.zoneName,
    },
    {
      key: 'location',
      title: '库位',
      minWidth: 150,
      render: (row) => row.locationName,
    },
    {
      key: 'adjustmentType',
      title: '调整方向',
      minWidth: 120,
      render: (row) => row.adjustmentType,
    },
    {
      key: 'quantity',
      title: '调整数量',
      minWidth: 120,
      render: (row) => row.quantity,
    },
    {
      key: 'reason',
      title: '调整原因',
      minWidth: 160,
      render: (row) => row.reason,
    },
    {
      key: 'createdAt',
      title: '创建时间',
      minWidth: 170,
      render: (row) => formatDateTime(row.createdAt),
    },
    {
      key: 'remarks',
      title: '备注',
      minWidth: 180,
      render: (row) => row.remarks || '-',
    },
  ]

  const handleSearch = async () => {
    await loadInventoryAdjustmentList(buildQueryParams(queryForm))
  }

  const handleReset = async () => {
    const resetState = {
      adjustmentCode: '',
      warehouseId: '',
      productId: '',
      adjustmentType: '',
    }

    setQueryForm(resetState)
    await loadInventoryAdjustmentList({})
  }

  const handleCreate = () => {
    setFormState(initialFormState)
    setFormError(null)
    setIsDialogOpen(true)
  }

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setIsSubmitting(true)
    setFormError(null)

    try {
      const payload = mapFormStateToPayload(formState)
      await createInventoryAdjustment(payload)
      setIsDialogOpen(false)
      setFormState(initialFormState)
      await loadInventoryAdjustmentList(buildQueryParams(queryForm))
    } catch (error) {
      setFormError(normalizeError(error))
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <section className="inventory-adjustment-page">
      {pageError ? (
        <div className="inventory-adjustment-page__message inventory-adjustment-page__message--error">
          <strong>{pageError.message}</strong>
          {pageError.traceId ? <span>追踪编号：{pageError.traceId}</span> : null}
        </div>
      ) : null}

      <div className="inventory-adjustment-page__search">
        <div className="inventory-adjustment-page__search-grid">
          <label className="inventory-adjustment-page__field">
            <span>调整单编号</span>
            <input
              type="text"
              placeholder="输入调整单编号"
              value={queryForm.adjustmentCode}
              onChange={(event) =>
                setQueryForm((current) => ({
                  ...current,
                  adjustmentCode: event.target.value,
                }))
              }
            />
          </label>

          <label className="inventory-adjustment-page__field">
            <span>所属仓库</span>
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

          <label className="inventory-adjustment-page__field">
            <span>产品档案</span>
            <select
              value={queryForm.productId}
              onChange={(event) =>
                setQueryForm((current) => ({
                  ...current,
                  productId: event.target.value,
                }))
              }
            >
              <option value="">全部产品</option>
              {productOptions.map((option) => (
                <option key={option.id} value={option.id}>
                  {option.label}
                </option>
              ))}
            </select>
          </label>

          <label className="inventory-adjustment-page__field">
            <span>调整方向</span>
            <select
              value={queryForm.adjustmentType}
              onChange={(event) =>
                setQueryForm((current) => ({
                  ...current,
                  adjustmentType: event.target.value,
                }))
              }
            >
              <option value="">全部方向</option>
              <option value="INCREASE">INCREASE</option>
              <option value="DECREASE">DECREASE</option>
            </select>
          </label>

          <div className="inventory-adjustment-page__actions">
            <button type="button" className="is-ghost" onClick={() => void handleReset()}>
              重置
            </button>
            <button type="button" className="is-dark" onClick={() => void handleSearch()}>
              查询
            </button>
          </div>
        </div>
      </div>

      <div className="inventory-adjustment-page__toolbar">
        <div className="inventory-adjustment-page__toolbar-copy">
          <h3>库存调整列表</h3>
          <p>处理系统初始化修正、人工纠错等非业务单据导致的库存变更，并沉淀标准调整记录。</p>
        </div>

        <button type="button" className="inventory-adjustment-page__primary" onClick={handleCreate}>
          新增调整
        </button>
      </div>

      <div className="inventory-adjustment-page__table-shell">
        {isLoading ? (
          <div className="inventory-adjustment-page__loading">正在加载库存调整...</div>
        ) : (
          <TreeDataTable
            data={adjustments}
            columns={columns}
            emptyText="暂无库存调整记录"
          />
        )}
      </div>

      {isDialogOpen ? (
        <div className="inventory-adjustment-page__dialog-backdrop">
          <div className="inventory-adjustment-page__dialog">
            <div className="inventory-adjustment-page__dialog-header">
              <div>
                <h3>新增库存调整</h3>
                <p>库存调整保存后立即生效，同时更新库存快照并生成调整流水。</p>
              </div>
              <button
                type="button"
                className="inventory-adjustment-page__dialog-close"
                onClick={() => {
                  setIsDialogOpen(false)
                  setFormError(null)
                }}
              >
                ×
              </button>
            </div>

            <form className="inventory-adjustment-page__dialog-form" onSubmit={handleSubmit}>
              {formError ? (
                <div className="inventory-adjustment-page__message inventory-adjustment-page__message--error inventory-adjustment-page__field--full">
                  <strong>{formError.message}</strong>
                  {formError.traceId ? <span>追踪编号：{formError.traceId}</span> : null}
                </div>
              ) : null}

              <label className="inventory-adjustment-page__field">
                <span>仓库</span>
                <select
                  value={formState.warehouseId}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      warehouseId: event.target.value,
                      zoneId: '',
                      locationId: '',
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

              <label className="inventory-adjustment-page__field">
                <span>库区</span>
                <select
                  value={formState.zoneId}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      zoneId: event.target.value,
                      locationId: '',
                    }))
                  }
                  disabled={!formState.warehouseId}
                >
                  <option value="">选择库区</option>
                  {filteredZoneOptions.map((option) => (
                    <option key={option.id} value={option.id}>
                      {option.label}
                    </option>
                  ))}
                </select>
                <small>{getFieldError(formError?.fieldErrors, 'zoneId')}</small>
              </label>

              <label className="inventory-adjustment-page__field">
                <span>库位</span>
                <select
                  value={formState.locationId}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      locationId: event.target.value,
                    }))
                  }
                  disabled={!formState.zoneId}
                >
                  <option value="">选择库位</option>
                  {filteredLocationOptions.map((option) => (
                    <option key={option.id} value={option.id}>
                      {option.label}
                    </option>
                  ))}
                </select>
                <small>{getFieldError(formError?.fieldErrors, 'locationId')}</small>
              </label>

              <label className="inventory-adjustment-page__field">
                <span>产品档案</span>
                <select
                  value={formState.productId}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      productId: event.target.value,
                    }))
                  }
                >
                  <option value="">选择产品</option>
                  {productOptions.map((option) => (
                    <option key={option.id} value={option.id}>
                      {option.label}
                    </option>
                  ))}
                </select>
                <small>{getFieldError(formError?.fieldErrors, 'productId')}</small>
              </label>

              <label className="inventory-adjustment-page__field">
                <span>调整方向</span>
                <select
                  value={formState.adjustmentType}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      adjustmentType: event.target.value,
                    }))
                  }
                >
                  <option value="INCREASE">INCREASE</option>
                  <option value="DECREASE">DECREASE</option>
                </select>
                <small>{getFieldError(formError?.fieldErrors, 'adjustmentType')}</small>
              </label>

              <label className="inventory-adjustment-page__field">
                <span>调整数量</span>
                <input
                  type="number"
                  min="0"
                  step="0.001"
                  value={formState.quantity}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      quantity: event.target.value,
                    }))
                  }
                  placeholder="输入调整数量"
                />
                <small>{getFieldError(formError?.fieldErrors, 'quantity')}</small>
              </label>

              <label className="inventory-adjustment-page__field">
                <span>调整原因</span>
                <input
                  type="text"
                  value={formState.reason}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      reason: event.target.value,
                    }))
                  }
                  placeholder="例如人工纠错"
                />
                <small>{getFieldError(formError?.fieldErrors, 'reason')}</small>
              </label>

              <label className="inventory-adjustment-page__field inventory-adjustment-page__field--full">
                <span>备注</span>
                <textarea
                  value={formState.remarks}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      remarks: event.target.value,
                    }))
                  }
                  placeholder="填写补充说明"
                />
              </label>

              <div className="inventory-adjustment-page__dialog-actions">
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
                  {isSubmitting ? '提交中...' : '保存调整'}
                </button>
              </div>
            </form>
          </div>
        </div>
      ) : null}
    </section>
  )
}

function toTableRows(rows: InventoryAdjustmentListItem[]): InventoryAdjustmentRow[] {
  return rows.map((row) => ({
    ...row,
  }))
}

function buildQueryParams(queryForm: {
  adjustmentCode: string
  warehouseId: string
  productId: string
  adjustmentType: string
}): InventoryAdjustmentListQuery {
  return {
    adjustmentCode: queryForm.adjustmentCode.trim() || undefined,
    warehouseId: queryForm.warehouseId || undefined,
    productId: queryForm.productId || undefined,
    adjustmentType: queryForm.adjustmentType || undefined,
  }
}

function mapFormStateToPayload(formState: InventoryAdjustmentFormState): InventoryAdjustmentFormPayload {
  return {
    warehouseId: Number(formState.warehouseId),
    zoneId: Number(formState.zoneId),
    locationId: Number(formState.locationId),
    productId: Number(formState.productId),
    adjustmentType: formState.adjustmentType,
    quantity: Number(formState.quantity),
    reason: formState.reason.trim(),
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

export default InventoryAdjustmentPage
