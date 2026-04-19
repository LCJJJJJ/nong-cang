import { useEffect, useMemo, useState } from 'react'

import { normalizeError, type AppError } from '../../api/errors'
import TreeDataTable, {
  type TreeTableColumn,
  type TreeTableRow,
} from '../../components/tree-data-table/TreeDataTable'
import { getInventoryStockList } from '../../features/inventorystock/api'
import type {
  InventoryStockListItem,
  InventoryStockListQuery,
} from '../../features/inventorystock/types'
import { getProductArchiveOptions } from '../../features/productarchive/api'
import type { ProductArchiveOption } from '../../features/productarchive/types'
import { getWarehouseOptions } from '../../features/warehouse/api'
import type { WarehouseOption } from '../../features/warehouse/types'
import { getWarehouseZoneOptions } from '../../features/warehousezone/api'
import type { WarehouseZoneOption } from '../../features/warehousezone/types'
import './InventoryStockPage.css'

type InventoryStockRow = TreeTableRow & InventoryStockListItem

function InventoryStockPage() {
  const [queryForm, setQueryForm] = useState({
    productId: '',
    warehouseId: '',
    zoneId: '',
  })
  const [inventoryStocks, setInventoryStocks] = useState<InventoryStockRow[]>([])
  const [warehouseOptions, setWarehouseOptions] = useState<WarehouseOption[]>([])
  const [warehouseZoneOptions, setWarehouseZoneOptions] = useState<
    WarehouseZoneOption[]
  >([])
  const [productOptions, setProductOptions] = useState<ProductArchiveOption[]>([])
  const [pageError, setPageError] = useState<AppError | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    let isMounted = true

    const bootstrap = async () => {
      try {
        const [list, warehouses, zones, products] = await Promise.all([
          getInventoryStockList(),
          getWarehouseOptions(),
          getWarehouseZoneOptions(),
          getProductArchiveOptions(),
        ])

        if (!isMounted) {
          return
        }

        setInventoryStocks(toTableRows(list))
        setWarehouseOptions(warehouses)
        setWarehouseZoneOptions(zones)
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

  async function loadInventoryStockList(query: InventoryStockListQuery) {
    setIsLoading(true)
    setPageError(null)

    try {
      const list = await getInventoryStockList(query)
      setInventoryStocks(toTableRows(list))
    } catch (error) {
      setPageError(normalizeError(error))
    } finally {
      setIsLoading(false)
    }
  }

  const filteredZoneOptions = useMemo(
    () =>
      queryForm.warehouseId
        ? warehouseZoneOptions.filter((option) => option.warehouseId === queryForm.warehouseId)
        : warehouseZoneOptions,
    [warehouseZoneOptions, queryForm.warehouseId],
  )

  const columns: TreeTableColumn<InventoryStockRow>[] = [
    {
      key: 'product',
      title: '产品',
      minWidth: 240,
      render: (row) => (
        <div className="inventory-stock-page__name-cell">
          <strong>{row.productName}</strong>
          <span>{`${row.productCode} / ${row.unitName}${row.unitSymbol ? ` (${row.unitSymbol})` : ''}`}</span>
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
      minWidth: 160,
      render: (row) => row.locationName,
    },
    {
      key: 'stockQuantity',
      title: '现存数量',
      minWidth: 120,
      render: (row) => row.stockQuantity,
    },
    {
      key: 'reservedQuantity',
      title: '预留数量',
      minWidth: 120,
      render: (row) => row.reservedQuantity,
    },
    {
      key: 'availableQuantity',
      title: '可用数量',
      minWidth: 120,
      render: (row) => (
        <strong className="inventory-stock-page__available">
          {row.availableQuantity}
        </strong>
      ),
    },
    {
      key: 'updatedAt',
      title: '更新时间',
      minWidth: 170,
      render: (row) => formatDateTime(row.updatedAt),
    },
  ]

  const handleSearch = async () => {
    await loadInventoryStockList(buildQueryParams(queryForm))
  }

  const handleReset = async () => {
    const resetState = {
      productId: '',
      warehouseId: '',
      zoneId: '',
    }

    setQueryForm(resetState)
    await loadInventoryStockList({})
  }

  return (
    <section className="inventory-stock-page">
      {pageError ? (
        <div className="inventory-stock-page__message inventory-stock-page__message--error">
          <strong>{pageError.message}</strong>
          {pageError.traceId ? <span>追踪编号：{pageError.traceId}</span> : null}
        </div>
      ) : null}

      <div className="inventory-stock-page__search">
        <div className="inventory-stock-page__search-grid">
          <label className="inventory-stock-page__field">
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

          <label className="inventory-stock-page__field">
            <span>所属仓库</span>
            <select
              value={queryForm.warehouseId}
              onChange={(event) =>
                setQueryForm((current) => ({
                  ...current,
                  warehouseId: event.target.value,
                  zoneId: '',
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

          <label className="inventory-stock-page__field">
            <span>所属库区</span>
            <select
              value={queryForm.zoneId}
              onChange={(event) =>
                setQueryForm((current) => ({
                  ...current,
                  zoneId: event.target.value,
                }))
              }
            >
              <option value="">全部库区</option>
              {filteredZoneOptions.map((option) => (
                <option key={option.id} value={option.id}>
                  {option.label}
                </option>
              ))}
            </select>
          </label>

          <div className="inventory-stock-page__actions">
            <button type="button" className="is-ghost" onClick={() => void handleReset()}>
              重置
            </button>
            <button type="button" className="is-dark" onClick={() => void handleSearch()}>
              查询
            </button>
          </div>
        </div>
      </div>

      <div className="inventory-stock-page__toolbar">
        <div className="inventory-stock-page__toolbar-copy">
          <h3>实时库存列表</h3>
          <p>展示当前库存、任务预留和可用数量，为出库分配、盘点和预警提供统一库存视图。</p>
        </div>
      </div>

      <div className="inventory-stock-page__table-shell">
        {isLoading ? (
          <div className="inventory-stock-page__loading">正在加载实时库存...</div>
        ) : (
          <TreeDataTable
            data={inventoryStocks}
            columns={columns}
            emptyText="暂无库存数据"
          />
        )}
      </div>
    </section>
  )
}

function toTableRows(rows: InventoryStockListItem[]): InventoryStockRow[] {
  return rows.map((row) => ({
    ...row,
  }))
}

function buildQueryParams(queryForm: {
  productId: string
  warehouseId: string
  zoneId: string
}): InventoryStockListQuery {
  return {
    productId: queryForm.productId || undefined,
    warehouseId: queryForm.warehouseId || undefined,
    zoneId: queryForm.zoneId || undefined,
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

export default InventoryStockPage
