import { useEffect, useState } from 'react'

import { normalizeError, type AppError } from '../../api/errors'
import TreeDataTable, {
  type TreeTableColumn,
  type TreeTableRow,
} from '../../components/tree-data-table/TreeDataTable'
import { getOutboundRecordList } from '../../features/outboundrecord/api'
import type {
  OutboundRecordListItem,
  OutboundRecordListQuery,
} from '../../features/outboundrecord/types'
import { getProductArchiveOptions } from '../../features/productarchive/api'
import type { ProductArchiveOption } from '../../features/productarchive/types'
import { getWarehouseOptions } from '../../features/warehouse/api'
import type { WarehouseOption } from '../../features/warehouse/types'
import './OutboundRecordPage.css'

type OutboundRecordRow = TreeTableRow & OutboundRecordListItem

function OutboundRecordPage() {
  const [queryForm, setQueryForm] = useState({
    recordCode: '',
    orderCode: '',
    warehouseId: '',
    productId: '',
  })
  const [outboundRecords, setOutboundRecords] = useState<OutboundRecordRow[]>([])
  const [warehouseOptions, setWarehouseOptions] = useState<WarehouseOption[]>([])
  const [productOptions, setProductOptions] = useState<ProductArchiveOption[]>([])
  const [pageError, setPageError] = useState<AppError | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    let isMounted = true

    const bootstrap = async () => {
      try {
        const [list, warehouses, products] = await Promise.all([
          getOutboundRecordList(),
          getWarehouseOptions(),
          getProductArchiveOptions(),
        ])

        if (!isMounted) {
          return
        }

        setOutboundRecords(toTableRows(list))
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

  async function loadOutboundRecordList(query: OutboundRecordListQuery) {
    setIsLoading(true)
    setPageError(null)

    try {
      const list = await getOutboundRecordList(query)
      setOutboundRecords(toTableRows(list))
    } catch (error) {
      setPageError(normalizeError(error))
    } finally {
      setIsLoading(false)
    }
  }

  const columns: TreeTableColumn<OutboundRecordRow>[] = [
    {
      key: 'recordCode',
      title: '记录编号',
      minWidth: 180,
      render: (row) => row.recordCode,
    },
    {
      key: 'orderCode',
      title: '出库单',
      minWidth: 180,
      render: (row) => row.outboundOrderCode,
    },
    {
      key: 'product',
      title: '产品',
      minWidth: 220,
      render: (row) => (
        <div className="outbound-record-page__name-cell">
          <strong>{row.productName}</strong>
          <span>{row.productCode}</span>
        </div>
      ),
    },
    {
      key: 'customer',
      title: '客户',
      minWidth: 180,
      render: (row) => row.customerName,
    },
    {
      key: 'warehouse',
      title: '所属仓库',
      minWidth: 150,
      render: (row) => row.warehouseName,
    },
    {
      key: 'zone',
      title: '所属库区',
      minWidth: 150,
      render: (row) => row.zoneName,
    },
    {
      key: 'location',
      title: '所属库位',
      minWidth: 150,
      render: (row) => row.locationName,
    },
    {
      key: 'quantity',
      title: '出库数量',
      minWidth: 120,
      render: (row) => row.quantity,
    },
    {
      key: 'occurredAt',
      title: '出库时间',
      minWidth: 170,
      render: (row) => formatDateTime(row.occurredAt),
    },
    {
      key: 'remarks',
      title: '备注',
      minWidth: 180,
      render: (row) => row.remarks || '-',
    },
  ]

  const handleSearch = async () => {
    await loadOutboundRecordList(buildQueryParams(queryForm))
  }

  const handleReset = async () => {
    const resetState = {
      recordCode: '',
      orderCode: '',
      warehouseId: '',
      productId: '',
    }

    setQueryForm(resetState)
    await loadOutboundRecordList({})
  }

  return (
    <section className="outbound-record-page">
      {pageError ? (
        <div className="outbound-record-page__message outbound-record-page__message--error">
          <strong>{pageError.message}</strong>
          {pageError.traceId ? <span>追踪编号：{pageError.traceId}</span> : null}
        </div>
      ) : null}

      <div className="outbound-record-page__search">
        <div className="outbound-record-page__search-grid">
          <label className="outbound-record-page__field">
            <span>记录编号</span>
            <input
              type="text"
              placeholder="输入记录编号"
              value={queryForm.recordCode}
              onChange={(event) =>
                setQueryForm((current) => ({
                  ...current,
                  recordCode: event.target.value,
                }))
              }
            />
          </label>

          <label className="outbound-record-page__field">
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

          <label className="outbound-record-page__field">
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

          <label className="outbound-record-page__field">
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

          <div className="outbound-record-page__actions">
            <button type="button" className="is-ghost" onClick={() => void handleReset()}>
              重置
            </button>
            <button type="button" className="is-dark" onClick={() => void handleSearch()}>
              查询
            </button>
          </div>
        </div>
      </div>

      <div className="outbound-record-page__toolbar">
        <div className="outbound-record-page__toolbar-copy">
          <h3>出库记录列表</h3>
          <p>展示已完成出库并扣减库存的最终结果，支撑后续库存追溯、客户分析和统计报表。</p>
        </div>
      </div>

      <div className="outbound-record-page__table-shell">
        {isLoading ? (
          <div className="outbound-record-page__loading">正在加载出库记录...</div>
        ) : (
          <TreeDataTable
            data={outboundRecords}
            columns={columns}
            emptyText="暂无出库记录"
          />
        )}
      </div>
    </section>
  )
}

function toTableRows(rows: OutboundRecordListItem[]): OutboundRecordRow[] {
  return rows.map((row) => ({
    ...row,
  }))
}

function buildQueryParams(queryForm: {
  recordCode: string
  orderCode: string
  warehouseId: string
  productId: string
}): OutboundRecordListQuery {
  return {
    recordCode: queryForm.recordCode.trim() || undefined,
    orderCode: queryForm.orderCode.trim() || undefined,
    warehouseId: queryForm.warehouseId || undefined,
    productId: queryForm.productId || undefined,
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

export default OutboundRecordPage
