import { useEffect, useState } from 'react'

import { normalizeError, type AppError } from '../../api/errors'
import TreeDataTable, {
  type TreeTableColumn,
  type TreeTableRow,
} from '../../components/tree-data-table/TreeDataTable'
import { getInventoryTransactionList } from '../../features/inventorytransaction/api'
import type {
  InventoryTransactionListItem,
  InventoryTransactionListQuery,
} from '../../features/inventorytransaction/types'
import { getProductArchiveOptions } from '../../features/productarchive/api'
import type { ProductArchiveOption } from '../../features/productarchive/types'
import { getWarehouseOptions } from '../../features/warehouse/api'
import type { WarehouseOption } from '../../features/warehouse/types'
import './InventoryTransactionPage.css'

type InventoryTransactionRow = TreeTableRow & InventoryTransactionListItem

function InventoryTransactionPage() {
  const [queryForm, setQueryForm] = useState({
    transactionCode: '',
    warehouseId: '',
    productId: '',
    transactionType: '',
  })
  const [transactions, setTransactions] = useState<InventoryTransactionRow[]>([])
  const [warehouseOptions, setWarehouseOptions] = useState<WarehouseOption[]>([])
  const [productOptions, setProductOptions] = useState<ProductArchiveOption[]>([])
  const [pageError, setPageError] = useState<AppError | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    let isMounted = true

    const bootstrap = async () => {
      try {
        const [list, warehouses, products] = await Promise.all([
          getInventoryTransactionList(),
          getWarehouseOptions(),
          getProductArchiveOptions(),
        ])

        if (!isMounted) {
          return
        }

        setTransactions(toTableRows(list))
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

  async function loadInventoryTransactionList(query: InventoryTransactionListQuery) {
    setIsLoading(true)
    setPageError(null)

    try {
      const list = await getInventoryTransactionList(query)
      setTransactions(toTableRows(list))
    } catch (error) {
      setPageError(normalizeError(error))
    } finally {
      setIsLoading(false)
    }
  }

  const columns: TreeTableColumn<InventoryTransactionRow>[] = [
    {
      key: 'transactionCode',
      title: '流水编号',
      minWidth: 180,
      render: (row) => row.transactionCode,
    },
    {
      key: 'transactionType',
      title: '流水类型',
      minWidth: 120,
      render: (row) => row.transactionType,
    },
    {
      key: 'product',
      title: '产品',
      minWidth: 220,
      render: (row) => (
        <div className="inventory-transaction-page__name-cell">
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
      key: 'quantity',
      title: '数量变化',
      minWidth: 120,
      render: (row) => (
        <span
          className={`inventory-transaction-page__quantity${
            row.quantity > 0 ? ' is-positive' : ' is-negative'
          }`}
        >
          {row.quantity}
        </span>
      ),
    },
    {
      key: 'source',
      title: '来源',
      minWidth: 160,
      render: (row) => `${row.sourceType} / ${row.sourceId}`,
    },
    {
      key: 'occurredAt',
      title: '发生时间',
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
    await loadInventoryTransactionList(buildQueryParams(queryForm))
  }

  const handleReset = async () => {
    const resetState = {
      transactionCode: '',
      warehouseId: '',
      productId: '',
      transactionType: '',
    }

    setQueryForm(resetState)
    await loadInventoryTransactionList({})
  }

  return (
    <section className="inventory-transaction-page">
      {pageError ? (
        <div className="inventory-transaction-page__message inventory-transaction-page__message--error">
          <strong>{pageError.message}</strong>
          {pageError.traceId ? <span>追踪编号：{pageError.traceId}</span> : null}
        </div>
      ) : null}

      <div className="inventory-transaction-page__search">
        <div className="inventory-transaction-page__search-grid">
          <label className="inventory-transaction-page__field">
            <span>流水编号</span>
            <input
              type="text"
              placeholder="输入流水编号"
              value={queryForm.transactionCode}
              onChange={(event) =>
                setQueryForm((current) => ({
                  ...current,
                  transactionCode: event.target.value,
                }))
              }
            />
          </label>

          <label className="inventory-transaction-page__field">
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

          <label className="inventory-transaction-page__field">
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

          <label className="inventory-transaction-page__field">
            <span>流水类型</span>
            <select
              value={queryForm.transactionType}
              onChange={(event) =>
                setQueryForm((current) => ({
                  ...current,
                  transactionType: event.target.value,
                }))
              }
            >
              <option value="">全部类型</option>
              <option value="INBOUND">INBOUND</option>
              <option value="OUTBOUND">OUTBOUND</option>
              <option value="ADJUSTMENT">ADJUSTMENT</option>
              <option value="STOCKTAKING">STOCKTAKING</option>
            </select>
          </label>

          <div className="inventory-transaction-page__actions">
            <button type="button" className="is-ghost" onClick={() => void handleReset()}>
              重置
            </button>
            <button type="button" className="is-dark" onClick={() => void handleSearch()}>
              查询
            </button>
          </div>
        </div>
      </div>

      <div className="inventory-transaction-page__toolbar">
        <div className="inventory-transaction-page__toolbar-copy">
          <h3>库存流水列表</h3>
          <p>统一查看库存增减记录，覆盖入库、出库、调整和盘点等库存变化来源。</p>
        </div>
      </div>

      <div className="inventory-transaction-page__table-shell">
        {isLoading ? (
          <div className="inventory-transaction-page__loading">正在加载库存流水...</div>
        ) : (
          <TreeDataTable
            data={transactions}
            columns={columns}
            emptyText="暂无库存流水"
          />
        )}
      </div>
    </section>
  )
}

function toTableRows(rows: InventoryTransactionListItem[]): InventoryTransactionRow[] {
  return rows.map((row) => ({
    ...row,
  }))
}

function buildQueryParams(queryForm: {
  transactionCode: string
  warehouseId: string
  productId: string
  transactionType: string
}): InventoryTransactionListQuery {
  return {
    transactionCode: queryForm.transactionCode.trim() || undefined,
    warehouseId: queryForm.warehouseId || undefined,
    productId: queryForm.productId || undefined,
    transactionType: queryForm.transactionType || undefined,
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

export default InventoryTransactionPage
