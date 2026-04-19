import { useEffect, useState } from 'react'

import { normalizeError, type AppError } from '../../api/errors'
import TreeDataTable, {
  type TreeTableColumn,
  type TreeTableRow,
} from '../../components/tree-data-table/TreeDataTable'
import { getAbnormalStockList } from '../../features/abnormalstock/api'
import type {
  AbnormalStockListItem,
  AbnormalStockListQuery,
} from '../../features/abnormalstock/types'
import { getProductArchiveOptions } from '../../features/productarchive/api'
import type { ProductArchiveOption } from '../../features/productarchive/types'
import { getWarehouseOptions } from '../../features/warehouse/api'
import type { WarehouseOption } from '../../features/warehouse/types'
import './AbnormalStockPage.css'

type AbnormalStockRow = TreeTableRow & AbnormalStockListItem

function AbnormalStockPage() {
  const [queryForm, setQueryForm] = useState({
    abnormalCode: '',
    productId: '',
    warehouseId: '',
    status: '',
  })
  const [abnormalStocks, setAbnormalStocks] = useState<AbnormalStockRow[]>([])
  const [warehouseOptions, setWarehouseOptions] = useState<WarehouseOption[]>([])
  const [productOptions, setProductOptions] = useState<ProductArchiveOption[]>([])
  const [pageError, setPageError] = useState<AppError | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    let isMounted = true

    const bootstrap = async () => {
      try {
        const [list, warehouses, products] = await Promise.all([
          getAbnormalStockList(),
          getWarehouseOptions(),
          getProductArchiveOptions(),
        ])

        if (!isMounted) {
          return
        }

        setAbnormalStocks(toTableRows(list))
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

  async function loadAbnormalStockList(query: AbnormalStockListQuery) {
    setIsLoading(true)
    setPageError(null)

    try {
      const list = await getAbnormalStockList(query)
      setAbnormalStocks(toTableRows(list))
    } catch (error) {
      setPageError(normalizeError(error))
    } finally {
      setIsLoading(false)
    }
  }

  const columns: TreeTableColumn<AbnormalStockRow>[] = [
    {
      key: 'abnormalCode',
      title: '异常库存编号',
      minWidth: 180,
      render: (row) => row.abnormalCode,
    },
    {
      key: 'inspectionCode',
      title: '来源质检单',
      minWidth: 180,
      render: (row) => row.inspectionCode,
    },
    {
      key: 'product',
      title: '产品',
      minWidth: 220,
      render: (row) => (
        <div className="abnormal-stock-page__name-cell">
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
      key: 'lockedQuantity',
      title: '锁定数量',
      minWidth: 120,
      render: (row) => row.lockedQuantity,
    },
    {
      key: 'status',
      title: '状态',
      minWidth: 120,
      render: (row) => (
        <span
          className={`abnormal-stock-page__status${
            row.status === 1 ? ' is-locked' : row.status === 2 ? ' is-released' : ' is-disposed'
          }`}
        >
          {row.statusLabel}
        </span>
      ),
    },
    {
      key: 'reason',
      title: '异常原因',
      minWidth: 160,
      render: (row) => row.reason,
    },
    {
      key: 'createdAt',
      title: '创建时间',
      minWidth: 170,
      render: (row) => formatDateTime(row.createdAt),
    },
  ]

  const handleSearch = async () => {
    await loadAbnormalStockList(buildQueryParams(queryForm))
  }

  const handleReset = async () => {
    const resetState = {
      abnormalCode: '',
      productId: '',
      warehouseId: '',
      status: '',
    }

    setQueryForm(resetState)
    await loadAbnormalStockList({})
  }

  return (
    <section className="abnormal-stock-page">
      {pageError ? (
        <div className="abnormal-stock-page__message abnormal-stock-page__message--error">
          <strong>{pageError.message}</strong>
          {pageError.traceId ? <span>追踪编号：{pageError.traceId}</span> : null}
        </div>
      ) : null}

      <div className="abnormal-stock-page__search">
        <div className="abnormal-stock-page__search-grid">
          <label className="abnormal-stock-page__field">
            <span>异常库存编号</span>
            <input
              type="text"
              value={queryForm.abnormalCode}
              onChange={(event) =>
                setQueryForm((current) => ({
                  ...current,
                  abnormalCode: event.target.value,
                }))
              }
            />
          </label>

          <label className="abnormal-stock-page__field">
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

          <label className="abnormal-stock-page__field">
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

          <label className="abnormal-stock-page__field">
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
              <option value="1">锁定中</option>
              <option value="2">已释放</option>
              <option value="3">已转损耗</option>
            </select>
          </label>

          <div className="abnormal-stock-page__actions">
            <button type="button" className="is-ghost" onClick={() => void handleReset()}>
              重置
            </button>
            <button type="button" className="is-dark" onClick={() => void handleSearch()}>
              查询
            </button>
          </div>
        </div>
      </div>

      <div className="abnormal-stock-page__toolbar">
        <div className="abnormal-stock-page__toolbar-copy">
          <h3>异常库存列表</h3>
          <p>展示由质检单锁定的不合格库存，后续会在这里执行释放或转损耗处理。</p>
        </div>
      </div>

      <div className="abnormal-stock-page__table-shell">
        {isLoading ? (
          <div className="abnormal-stock-page__loading">正在加载异常库存...</div>
        ) : (
          <TreeDataTable
            data={abnormalStocks}
            columns={columns}
            emptyText="暂无异常库存"
          />
        )}
      </div>
    </section>
  )
}

function toTableRows(rows: AbnormalStockListItem[]): AbnormalStockRow[] {
  return rows.map((row) => ({
    ...row,
  }))
}

function buildQueryParams(queryForm: {
  abnormalCode: string
  productId: string
  warehouseId: string
  status: string
}): AbnormalStockListQuery {
  return {
    abnormalCode: queryForm.abnormalCode.trim() || undefined,
    productId: queryForm.productId || undefined,
    warehouseId: queryForm.warehouseId || undefined,
    status: queryForm.status ? Number(queryForm.status) : undefined,
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

export default AbnormalStockPage
