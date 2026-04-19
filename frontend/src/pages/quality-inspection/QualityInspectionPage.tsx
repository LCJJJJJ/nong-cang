import { type FormEvent, useEffect, useMemo, useState } from 'react'

import { getFieldError, normalizeError, type AppError } from '../../api/errors'
import TreeDataTable, {
  type TreeTableColumn,
  type TreeTableRow,
} from '../../components/tree-data-table/TreeDataTable'
import { getInboundRecordList } from '../../features/inboundrecord/api'
import type { InboundRecordListItem } from '../../features/inboundrecord/types'
import { getInventoryStockList } from '../../features/inventorystock/api'
import type { InventoryStockListItem } from '../../features/inventorystock/types'
import { getProductArchiveOptions } from '../../features/productarchive/api'
import type { ProductArchiveOption } from '../../features/productarchive/types'
import {
  createQualityInspection,
  getQualityInspectionList,
} from '../../features/qualityinspection/api'
import type {
  QualityInspectionFormPayload,
  QualityInspectionListItem,
  QualityInspectionListQuery,
} from '../../features/qualityinspection/types'
import { buildQuantityStep } from '../../utils/quantity'
import './QualityInspectionPage.css'

type QualityInspectionRow = TreeTableRow & QualityInspectionListItem

interface QualityInspectionFormState {
  sourceType: string
  sourceId: string
  inspectQuantity: string
  unqualifiedQuantity: string
  remarks: string
}

const initialFormState: QualityInspectionFormState = {
  sourceType: 'INVENTORY_STOCK',
  sourceId: '',
  inspectQuantity: '',
  unqualifiedQuantity: '0',
  remarks: '',
}

function QualityInspectionPage() {
  const [queryForm, setQueryForm] = useState({
    inspectionCode: '',
    sourceType: '',
    productId: '',
    resultStatus: '',
  })
  const [inspections, setInspections] = useState<QualityInspectionRow[]>([])
  const [inventoryStocks, setInventoryStocks] = useState<InventoryStockListItem[]>([])
  const [inboundRecords, setInboundRecords] = useState<InboundRecordListItem[]>([])
  const [productOptions, setProductOptions] = useState<ProductArchiveOption[]>([])
  const [pageError, setPageError] = useState<AppError | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  const [isDialogOpen, setIsDialogOpen] = useState(false)
  const [formState, setFormState] = useState<QualityInspectionFormState>(initialFormState)
  const [formError, setFormError] = useState<AppError | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)

  useEffect(() => {
    let isMounted = true

    const bootstrap = async () => {
      try {
        const [list, stocks, records, products] = await Promise.all([
          getQualityInspectionList(),
          getInventoryStockList(),
          getInboundRecordList(),
          getProductArchiveOptions(),
        ])

        if (!isMounted) {
          return
        }

        setInspections(toTableRows(list))
        setInventoryStocks(stocks)
        setInboundRecords(records)
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

  async function loadQualityInspectionList(query: QualityInspectionListQuery) {
    setIsLoading(true)
    setPageError(null)

    try {
      const list = await getQualityInspectionList(query)
      setInspections(toTableRows(list))
    } catch (error) {
      setPageError(normalizeError(error))
    } finally {
      setIsLoading(false)
    }
  }

  const sourceOptions = useMemo(() => {
    if (formState.sourceType === 'INBOUND_RECORD') {
      return inboundRecords.map((item) => ({
        id: item.id,
        label: `${item.recordCode} / ${item.productName} / ${item.quantity}`,
        quantity: item.quantity,
        productId: item.productId,
      }))
    }

    return inventoryStocks.map((item) => ({
      id: item.id,
      label: `${item.productName} / ${item.locationName} / 可检 ${item.availableQuantity}`,
      quantity: item.availableQuantity,
      productId: item.productId,
    }))
  }, [formState.sourceType, inboundRecords, inventoryStocks])

  const selectedSource = sourceOptions.find((option) => option.id === formState.sourceId) ?? null
  const selectedProductPrecision = productOptions.find(
    (option) => option.id === selectedSource?.productId,
  )?.precisionDigits

  const columns: TreeTableColumn<QualityInspectionRow>[] = [
    {
      key: 'inspectionCode',
      title: '质检单编号',
      minWidth: 180,
      render: (row) => row.inspectionCode,
    },
    {
      key: 'source',
      title: '质检来源',
      minWidth: 220,
      render: (row) => `${row.sourceType} / ${row.sourceCode}`,
    },
    {
      key: 'product',
      title: '产品',
      minWidth: 220,
      render: (row) => (
        <div className="quality-inspection-page__name-cell">
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
      key: 'inspectQuantity',
      title: '送检数量',
      minWidth: 120,
      render: (row) => row.inspectQuantity,
    },
    {
      key: 'unqualifiedQuantity',
      title: '不合格数量',
      minWidth: 120,
      render: (row) => row.unqualifiedQuantity,
    },
    {
      key: 'resultStatus',
      title: '结果',
      minWidth: 130,
      render: (row) => (
        <span
          className={`quality-inspection-page__status${
            row.resultStatus === 1 ? ' is-pass' : row.resultStatus === 2 ? ' is-partial' : ' is-fail'
          }`}
        >
          {row.resultStatusLabel}
        </span>
      ),
    },
    {
      key: 'createdAt',
      title: '创建时间',
      minWidth: 170,
      render: (row) => formatDateTime(row.createdAt),
    },
  ]

  const handleSearch = async () => {
    await loadQualityInspectionList(buildQueryParams(queryForm))
  }

  const handleReset = async () => {
    const resetState = {
      inspectionCode: '',
      sourceType: '',
      productId: '',
      resultStatus: '',
    }

    setQueryForm(resetState)
    await loadQualityInspectionList({})
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
      await createQualityInspection(payload)
      setIsDialogOpen(false)
      setFormState(initialFormState)
      await loadQualityInspectionList(buildQueryParams(queryForm))
    } catch (error) {
      setFormError(normalizeError(error))
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <section className="quality-inspection-page">
      {pageError ? (
        <div className="quality-inspection-page__message quality-inspection-page__message--error">
          <strong>{pageError.message}</strong>
          {pageError.traceId ? <span>追踪编号：{pageError.traceId}</span> : null}
        </div>
      ) : null}

      <div className="quality-inspection-page__search">
        <div className="quality-inspection-page__search-grid">
          <label className="quality-inspection-page__field">
            <span>质检单编号</span>
            <input
              type="text"
              value={queryForm.inspectionCode}
              onChange={(event) =>
                setQueryForm((current) => ({
                  ...current,
                  inspectionCode: event.target.value,
                }))
              }
            />
          </label>

          <label className="quality-inspection-page__field">
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
              <option value="INBOUND_RECORD">INBOUND_RECORD</option>
              <option value="INVENTORY_STOCK">INVENTORY_STOCK</option>
            </select>
          </label>

          <label className="quality-inspection-page__field">
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

          <label className="quality-inspection-page__field">
            <span>质检结果</span>
            <select
              value={queryForm.resultStatus}
              onChange={(event) =>
                setQueryForm((current) => ({
                  ...current,
                  resultStatus: event.target.value,
                }))
              }
            >
              <option value="">全部结果</option>
              <option value="1">合格</option>
              <option value="2">部分不合格</option>
              <option value="3">不合格</option>
            </select>
          </label>

          <div className="quality-inspection-page__actions">
            <button type="button" className="is-ghost" onClick={() => void handleReset()}>
              重置
            </button>
            <button type="button" className="is-dark" onClick={() => void handleSearch()}>
              查询
            </button>
          </div>
        </div>
      </div>

      <div className="quality-inspection-page__toolbar">
        <div className="quality-inspection-page__toolbar-copy">
          <h3>质检单列表</h3>
          <p>登记入库质检和在库抽检结果，不合格数量会自动进入异常锁定库存。</p>
        </div>
        <button type="button" className="quality-inspection-page__primary" onClick={handleCreate}>
          新增质检单
        </button>
      </div>

      <div className="quality-inspection-page__table-shell">
        {isLoading ? (
          <div className="quality-inspection-page__loading">正在加载质检单...</div>
        ) : (
          <TreeDataTable
            data={inspections}
            columns={columns}
            emptyText="暂无质检单"
          />
        )}
      </div>

      {isDialogOpen ? (
        <div className="quality-inspection-page__dialog-backdrop">
          <div className="quality-inspection-page__dialog">
            <div className="quality-inspection-page__dialog-header">
              <div>
                <h3>新增质检单</h3>
                <p>选择质检来源并填写送检结果，系统会自动处理异常锁定库存。</p>
              </div>
              <button
                type="button"
                className="quality-inspection-page__dialog-close"
                onClick={() => {
                  setIsDialogOpen(false)
                  setFormError(null)
                }}
              >
                ×
              </button>
            </div>

            <form className="quality-inspection-page__dialog-form" onSubmit={handleSubmit}>
              {formError ? (
                <div className="quality-inspection-page__message quality-inspection-page__message--error quality-inspection-page__field--full">
                  <strong>{formError.message}</strong>
                  {formError.traceId ? <span>追踪编号：{formError.traceId}</span> : null}
                </div>
              ) : null}

              <label className="quality-inspection-page__field">
                <span>来源类型</span>
                <select
                  value={formState.sourceType}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      sourceType: event.target.value,
                      sourceId: '',
                      inspectQuantity: '',
                      unqualifiedQuantity: '0',
                    }))
                  }
                >
                  <option value="INVENTORY_STOCK">INVENTORY_STOCK</option>
                  <option value="INBOUND_RECORD">INBOUND_RECORD</option>
                </select>
              </label>

              <label className="quality-inspection-page__field">
                <span>来源记录</span>
                <select
                  value={formState.sourceId}
                  onChange={(event) => {
                    const selected = sourceOptions.find((option) => option.id === event.target.value)
                    setFormState((current) => ({
                      ...current,
                      sourceId: event.target.value,
                      inspectQuantity: selected ? String(selected.quantity) : '',
                      unqualifiedQuantity: '0',
                    }))
                  }}
                >
                  <option value="">选择来源</option>
                  {sourceOptions.map((option) => (
                    <option key={option.id} value={option.id}>
                      {option.label}
                    </option>
                  ))}
                </select>
                <small>{getFieldError(formError?.fieldErrors, 'sourceId')}</small>
              </label>

              <label className="quality-inspection-page__field">
                <span>送检数量</span>
                <input
                  type="number"
                  min="0"
                  step={buildQuantityStep(selectedProductPrecision)}
                  value={formState.inspectQuantity}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      inspectQuantity: event.target.value,
                    }))
                  }
                />
                <small>{getFieldError(formError?.fieldErrors, 'inspectQuantity')}</small>
              </label>

              <label className="quality-inspection-page__field">
                <span>不合格数量</span>
                <input
                  type="number"
                  min="0"
                  step={buildQuantityStep(selectedProductPrecision)}
                  value={formState.unqualifiedQuantity}
                  onChange={(event) =>
                    setFormState((current) => ({
                      ...current,
                      unqualifiedQuantity: event.target.value,
                    }))
                  }
                />
                <small>{getFieldError(formError?.fieldErrors, 'unqualifiedQuantity')}</small>
              </label>

              <label className="quality-inspection-page__field quality-inspection-page__field--full">
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

              <div className="quality-inspection-page__dialog-actions">
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
                  {isSubmitting ? '提交中...' : '保存质检单'}
                </button>
              </div>
            </form>
          </div>
        </div>
      ) : null}
    </section>
  )
}

function toTableRows(rows: QualityInspectionListItem[]): QualityInspectionRow[] {
  return rows.map((row) => ({
    ...row,
  }))
}

function buildQueryParams(queryForm: {
  inspectionCode: string
  sourceType: string
  productId: string
  resultStatus: string
}): QualityInspectionListQuery {
  return {
    inspectionCode: queryForm.inspectionCode.trim() || undefined,
    sourceType: queryForm.sourceType || undefined,
    productId: queryForm.productId || undefined,
    resultStatus: queryForm.resultStatus ? Number(queryForm.resultStatus) : undefined,
  }
}

function mapFormStateToPayload(formState: QualityInspectionFormState): QualityInspectionFormPayload {
  return {
    sourceType: formState.sourceType,
    sourceId: Number(formState.sourceId),
    inspectQuantity: Number(formState.inspectQuantity),
    unqualifiedQuantity: Number(formState.unqualifiedQuantity),
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

export default QualityInspectionPage
