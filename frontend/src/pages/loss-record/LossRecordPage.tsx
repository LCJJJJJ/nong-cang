import { type FormEvent, useEffect, useMemo, useState } from 'react'

import { getFieldError, normalizeError, type AppError } from '../../api/errors'
import TreeDataTable, {
  type TreeTableColumn,
  type TreeTableRow,
} from '../../components/tree-data-table/TreeDataTable'
import {
  disposeAbnormalStockToLoss,
  getAbnormalStockOptions,
} from '../../features/abnormalstock/api'
import type { AbnormalStockOption } from '../../features/abnormalstock/types'
import { createDirectLossRecord, getLossRecordList } from '../../features/lossrecord/api'
import type {
  LossRecordDirectPayload,
  LossRecordListItem,
  LossRecordListQuery,
} from '../../features/lossrecord/types'
import { getProductArchiveOptions } from '../../features/productarchive/api'
import type { ProductArchiveOption } from '../../features/productarchive/types'
import { getWarehouseOptions } from '../../features/warehouse/api'
import type { WarehouseOption } from '../../features/warehouse/types'
import { getWarehouseLocationOptions } from '../../features/warehouselocation/api'
import type { WarehouseLocationOption } from '../../features/warehouselocation/types'
import { getWarehouseZoneOptions } from '../../features/warehousezone/api'
import type { WarehouseZoneOption } from '../../features/warehousezone/types'
import { buildQuantityStep } from '../../utils/quantity'
import './LossRecordPage.css'

type LossRecordRow = TreeTableRow & LossRecordListItem

interface LossFormState {
  sourceMode: 'DIRECT' | 'ABNORMAL'
  warehouseId: string
  zoneId: string
  locationId: string
  productId: string
  quantity: string
  lossReason: string
  remarks: string
  abnormalStockId: string
}

const initialFormState: LossFormState = {
  sourceMode: 'DIRECT',
  warehouseId: '',
  zoneId: '',
  locationId: '',
  productId: '',
  quantity: '',
  lossReason: '',
  remarks: '',
  abnormalStockId: '',
}

function LossRecordPage() {
  const [queryForm, setQueryForm] = useState({
    lossCode: '',
    sourceType: '',
    warehouseId: '',
    productId: '',
  })
  const [lossRecords, setLossRecords] = useState<LossRecordRow[]>([])
  const [warehouseOptions, setWarehouseOptions] = useState<WarehouseOption[]>([])
  const [warehouseZoneOptions, setWarehouseZoneOptions] = useState<WarehouseZoneOption[]>([])
  const [warehouseLocationOptions, setWarehouseLocationOptions] = useState<
    WarehouseLocationOption[]
  >([])
  const [productOptions, setProductOptions] = useState<ProductArchiveOption[]>([])
  const [abnormalStockOptions, setAbnormalStockOptions] = useState<AbnormalStockOption[]>([])
  const [pageError, setPageError] = useState<AppError | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  const [isDialogOpen, setIsDialogOpen] = useState(false)
  const [formState, setFormState] = useState<LossFormState>(initialFormState)
  const [formError, setFormError] = useState<AppError | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)

  useEffect(() => {
    let isMounted = true

    const bootstrap = async () => {
      try {
        const [list, warehouses, zones, locations, products, abnormalOptions] = await Promise.all([
          getLossRecordList(),
          getWarehouseOptions(),
          getWarehouseZoneOptions(),
          getWarehouseLocationOptions(),
          getProductArchiveOptions(),
          getAbnormalStockOptions(),
        ])

        if (!isMounted) {
          return
        }

        setLossRecords(toTableRows(list))
        setWarehouseOptions(warehouses)
        setWarehouseZoneOptions(zones)
        setWarehouseLocationOptions(locations)
        setProductOptions(products)
        setAbnormalStockOptions(abnormalOptions)
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

  async function loadLossRecordList(query: LossRecordListQuery) {
    setIsLoading(true)
    setPageError(null)

    try {
      const list = await getLossRecordList(query)
      setLossRecords(toTableRows(list))
      setAbnormalStockOptions(await getAbnormalStockOptions())
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

  const selectedProductPrecision = useMemo(
    () => productOptions.find((option) => option.id === formState.productId)?.precisionDigits,
    [formState.productId, productOptions],
  )

  const columns: TreeTableColumn<LossRecordRow>[] = [
    {
      key: 'lossCode',
      title: '损耗记录编号',
      minWidth: 180,
      render: (row) => row.lossCode,
    },
    {
      key: 'sourceType',
      title: '来源类型',
      minWidth: 140,
      render: (row) => row.sourceType,
    },
    {
      key: 'product',
      title: '产品',
      minWidth: 220,
      render: (row) => (
        <div className="loss-record-page__name-cell">
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
      key: 'location',
      title: '库位',
      minWidth: 170,
      render: (row) => `${row.zoneName} / ${row.locationName}`,
    },
    {
      key: 'quantity',
      title: '损耗数量',
      minWidth: 120,
      render: (row) => row.quantity,
    },
    {
      key: 'lossReason',
      title: '损耗原因',
      minWidth: 160,
      render: (row) => row.lossReason,
    },
    {
      key: 'createdAt',
      title: '创建时间',
      minWidth: 170,
      render: (row) => formatDateTime(row.createdAt),
    },
  ]

  const handleSearch = async () => {
    await loadLossRecordList(buildQueryParams(queryForm))
  }

  const handleReset = async () => {
    const resetState = {
      lossCode: '',
      sourceType: '',
      warehouseId: '',
      productId: '',
    }

    setQueryForm(resetState)
    await loadLossRecordList({})
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
      if (formState.sourceMode === 'DIRECT') {
        await createDirectLossRecord(mapDirectPayload(formState))
      } else if (formState.abnormalStockId) {
        await disposeAbnormalStockToLoss(formState.abnormalStockId, {
          lossReason: formState.lossReason.trim(),
          remarks: formState.remarks.trim() || null,
        })
      }

      setIsDialogOpen(false)
      setFormState(initialFormState)
      await loadLossRecordList(buildQueryParams(queryForm))
    } catch (error) {
      setFormError(normalizeError(error))
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <section className="loss-record-page">
      {pageError ? (
        <div className="loss-record-page__message loss-record-page__message--error">
          <strong>{pageError.message}</strong>
          {pageError.traceId ? <span>追踪编号：{pageError.traceId}</span> : null}
        </div>
      ) : null}

      <div className="loss-record-page__search">
        <div className="loss-record-page__search-grid">
          <label className="loss-record-page__field">
            <span>损耗记录编号</span>
            <input
              value={queryForm.lossCode}
              onChange={(event) =>
                setQueryForm((current) => ({
                  ...current,
                  lossCode: event.target.value,
                }))
              }
            />
          </label>

          <label className="loss-record-page__field">
            <span>来源类型</span>
            <select
              value={queryForm.sourceType}
              onChange={(event) =>
                setQueryForm((current) => ({
                  ...current,
                  sourceType: event.target.value,
                }))
              }
            >
              <option value="">全部来源</option>
              <option value="DIRECT">DIRECT</option>
              <option value="ABNORMAL_STOCK">ABNORMAL_STOCK</option>
            </select>
          </label>

          <label className="loss-record-page__field">
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

          <label className="loss-record-page__field">
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

          <div className="loss-record-page__actions">
            <button type="button" className="is-ghost" onClick={() => void handleReset()}>
              重置
            </button>
            <button type="button" className="is-dark" onClick={() => void handleSearch()}>
              查询
            </button>
          </div>
        </div>
      </div>

      <div className="loss-record-page__toolbar">
        <div className="loss-record-page__toolbar-copy">
          <h3>损耗记录列表</h3>
          <p>统一查看异常转损耗和人工直接登记的损耗记录，损耗入账后会同步减少库存。</p>
        </div>
        <button type="button" className="loss-record-page__primary" onClick={handleCreate}>
          新增损耗
        </button>
      </div>

      <div className="loss-record-page__table-shell">
        {isLoading ? (
          <div className="loss-record-page__loading">正在加载损耗记录...</div>
        ) : (
          <TreeDataTable
            data={lossRecords}
            columns={columns}
            emptyText="暂无损耗记录"
          />
        )}
      </div>

      {isDialogOpen ? (
        <div className="loss-record-page__dialog-backdrop">
          <div className="loss-record-page__dialog">
            <div className="loss-record-page__dialog-header">
              <div>
                <h3>新增损耗</h3>
                <p>支持人工直接登记损耗，也支持从异常库存快速转损耗。</p>
              </div>
              <button
                type="button"
                className="loss-record-page__dialog-close"
                onClick={() => setIsDialogOpen(false)}
              >
                ×
              </button>
            </div>

            <form className="loss-record-page__dialog-form" onSubmit={handleSubmit}>
              {formError ? (
                <div className="loss-record-page__message loss-record-page__message--error loss-record-page__field--full">
                  <strong>{formError.message}</strong>
                  {formError.traceId ? <span>追踪编号：{formError.traceId}</span> : null}
                </div>
              ) : null}

              <label className="loss-record-page__field">
                <span>来源模式</span>
                <select
                  value={formState.sourceMode}
                  onChange={(event) =>
                    setFormState({
                      ...initialFormState,
                      sourceMode: event.target.value as 'DIRECT' | 'ABNORMAL',
                    })
                  }
                >
                  <option value="DIRECT">DIRECT</option>
                  <option value="ABNORMAL">ABNORMAL</option>
                </select>
              </label>

              {formState.sourceMode === 'ABNORMAL' ? (
                <label className="loss-record-page__field loss-record-page__field--full">
                  <span>异常库存</span>
                  <select
                    value={formState.abnormalStockId}
                    onChange={(event) =>
                      setFormState((current) => ({
                        ...current,
                        abnormalStockId: event.target.value,
                      }))
                    }
                  >
                    <option value="">选择异常库存</option>
                    {abnormalStockOptions.map((option) => (
                      <option key={option.id} value={option.id}>
                        {`${option.label} / 锁定 ${option.lockedQuantity}`}
                      </option>
                    ))}
                  </select>
                </label>
              ) : (
                <>
                  <label className="loss-record-page__field">
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
                  </label>

                  <label className="loss-record-page__field">
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
                  </label>

                  <label className="loss-record-page__field">
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
                  </label>

                  <label className="loss-record-page__field">
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
                  </label>

                  <label className="loss-record-page__field">
                    <span>损耗数量</span>
                    <input
                      type="number"
                      min="0"
                      step={buildQuantityStep(selectedProductPrecision)}
                      value={formState.quantity}
                      onChange={(event) =>
                        setFormState((current) => ({
                          ...current,
                          quantity: event.target.value,
                        }))
                      }
                    />
                    <small>{getFieldError(formError?.fieldErrors, 'quantity')}</small>
                  </label>
                </>
              )}

              <label className="loss-record-page__field loss-record-page__field--full">
                <span>损耗原因</span>
                <input
                  value={formState.lossReason}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      lossReason: event.target.value,
                    }))
                  }
                />
                <small>{getFieldError(formError?.fieldErrors, 'lossReason')}</small>
              </label>

              <label className="loss-record-page__field loss-record-page__field--full">
                <span>备注</span>
                <textarea
                  value={formState.remarks}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      remarks: event.target.value,
                    }))
                  }
                />
              </label>

              <div className="loss-record-page__dialog-actions">
                <button
                  type="button"
                  className="is-ghost"
                  onClick={() => setIsDialogOpen(false)}
                >
                  取消
                </button>
                <button type="submit" className="is-dark" disabled={isSubmitting}>
                  {isSubmitting ? '提交中...' : '保存损耗'}
                </button>
              </div>
            </form>
          </div>
        </div>
      ) : null}
    </section>
  )
}

function toTableRows(rows: LossRecordListItem[]): LossRecordRow[] {
  return rows.map((row) => ({
    ...row,
  }))
}

function buildQueryParams(queryForm: {
  lossCode: string
  sourceType: string
  warehouseId: string
  productId: string
}): LossRecordListQuery {
  return {
    lossCode: queryForm.lossCode.trim() || undefined,
    sourceType: queryForm.sourceType || undefined,
    warehouseId: queryForm.warehouseId || undefined,
    productId: queryForm.productId || undefined,
  }
}

function mapDirectPayload(formState: LossFormState): LossRecordDirectPayload {
  return {
    warehouseId: Number(formState.warehouseId),
    zoneId: Number(formState.zoneId),
    locationId: Number(formState.locationId),
    productId: Number(formState.productId),
    quantity: Number(formState.quantity),
    lossReason: formState.lossReason.trim(),
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

export default LossRecordPage
