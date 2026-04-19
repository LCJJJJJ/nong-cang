import { type FormEvent, useEffect, useMemo, useState } from 'react'

import { getFieldError, normalizeError, type AppError } from '../../api/errors'
import TreeDataTable, {
  type TreeTableColumn,
  type TreeTableRow,
} from '../../components/tree-data-table/TreeDataTable'
import {
  cancelInventoryStocktaking,
  confirmInventoryStocktaking,
  createInventoryStocktaking,
  getInventoryStocktakingDetail,
  getInventoryStocktakingList,
  saveInventoryStocktakingItems,
} from '../../features/inventorystocktaking/api'
import type {
  InventoryStocktakingCreatePayload,
  InventoryStocktakingDetail,
  InventoryStocktakingItem,
  InventoryStocktakingItemSavePayload,
  InventoryStocktakingListItem,
  InventoryStocktakingListQuery,
} from '../../features/inventorystocktaking/types'
import { getWarehouseOptions } from '../../features/warehouse/api'
import type { WarehouseOption } from '../../features/warehouse/types'
import { getWarehouseZoneOptions } from '../../features/warehousezone/api'
import type { WarehouseZoneOption } from '../../features/warehousezone/types'
import { buildQuantityStep } from '../../utils/quantity'
import './InventoryStocktakingPage.css'

type InventoryStocktakingRow = TreeTableRow & InventoryStocktakingListItem

interface CreateFormState {
  warehouseId: string
  zoneId: string
  remarks: string
}

const initialCreateFormState: CreateFormState = {
  warehouseId: '',
  zoneId: '',
  remarks: '',
}

function InventoryStocktakingPage() {
  const [queryForm, setQueryForm] = useState({
    stocktakingCode: '',
    warehouseId: '',
    status: '',
  })
  const [stocktakings, setStocktakings] = useState<InventoryStocktakingRow[]>([])
  const [warehouseOptions, setWarehouseOptions] = useState<WarehouseOption[]>([])
  const [warehouseZoneOptions, setWarehouseZoneOptions] = useState<
    WarehouseZoneOption[]
  >([])
  const [pageError, setPageError] = useState<AppError | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false)
  const [createForm, setCreateForm] = useState<CreateFormState>(initialCreateFormState)
  const [createError, setCreateError] = useState<AppError | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)

  const [isDetailDialogOpen, setIsDetailDialogOpen] = useState(false)
  const [detailError, setDetailError] = useState<AppError | null>(null)
  const [currentDetail, setCurrentDetail] = useState<InventoryStocktakingDetail | null>(null)
  const [isSavingDetail, setIsSavingDetail] = useState(false)

  useEffect(() => {
    let isMounted = true

    const bootstrap = async () => {
      try {
        const [list, warehouses, zones] = await Promise.all([
          getInventoryStocktakingList(),
          getWarehouseOptions(),
          getWarehouseZoneOptions(),
        ])

        if (!isMounted) {
          return
        }

        setStocktakings(toTableRows(list))
        setWarehouseOptions(warehouses)
        setWarehouseZoneOptions(zones)
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

  async function loadInventoryStocktakingList(query: InventoryStocktakingListQuery) {
    setIsLoading(true)
    setPageError(null)

    try {
      const list = await getInventoryStocktakingList(query)
      setStocktakings(toTableRows(list))
    } catch (error) {
      setPageError(normalizeError(error))
    } finally {
      setIsLoading(false)
    }
  }

  const filteredZoneOptions = useMemo(
    () =>
      createForm.warehouseId
        ? warehouseZoneOptions.filter((option) => option.warehouseId === createForm.warehouseId)
        : warehouseZoneOptions,
    [createForm.warehouseId, warehouseZoneOptions],
  )

  const columns: TreeTableColumn<InventoryStocktakingRow>[] = [
    {
      key: 'stocktakingCode',
      title: '盘点单编号',
      minWidth: 180,
      render: (row) => row.stocktakingCode,
    },
    {
      key: 'warehouse',
      title: '仓库',
      minWidth: 160,
      render: (row) => row.warehouseName,
    },
    {
      key: 'zone',
      title: '盘点范围',
      minWidth: 160,
      render: (row) => row.zoneName ?? '全部库区',
    },
    {
      key: 'status',
      title: '状态',
      minWidth: 120,
      render: (row) => (
        <span
          className={`inventory-stocktaking-page__status${
            row.status === 3 ? ' is-complete' : row.status === 4 ? ' is-cancelled' : ''
          }`}
        >
          {row.statusLabel}
        </span>
      ),
    },
    {
      key: 'itemCount',
      title: '明细数',
      minWidth: 100,
      render: (row) => row.itemCount,
    },
    {
      key: 'countedItemCount',
      title: '已录入',
      minWidth: 100,
      render: (row) => row.countedItemCount,
    },
    {
      key: 'totalDifferenceQuantity',
      title: '总差异',
      minWidth: 120,
      render: (row) => row.totalDifferenceQuantity,
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
      minWidth: 240,
      width: 240,
      sticky: 'right',
      align: 'right',
      render: (row) => (
        <div className="inventory-stocktaking-page__row-actions">
          {row.status === 1 || row.status === 2 ? (
            <button type="button" onClick={() => handleOpenDetail(row.id)}>
              盘点录入
            </button>
          ) : null}
          {row.status === 1 || row.status === 2 ? (
            <button type="button" onClick={() => handleCancel(row)}>
              取消
            </button>
          ) : null}
        </div>
      ),
    },
  ]

  const handleSearch = async () => {
    await loadInventoryStocktakingList(buildQueryParams(queryForm))
  }

  const handleReset = async () => {
    const resetState = {
      stocktakingCode: '',
      warehouseId: '',
      status: '',
    }

    setQueryForm(resetState)
    await loadInventoryStocktakingList({})
  }

  const handleCreate = () => {
    setCreateForm(initialCreateFormState)
    setCreateError(null)
    setIsCreateDialogOpen(true)
  }

  const handleCreateSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setIsSubmitting(true)
    setCreateError(null)

    try {
      const detail = await createInventoryStocktaking(mapCreateFormToPayload(createForm))
      setIsCreateDialogOpen(false)
      setCurrentDetail(detail)
      setDetailError(null)
      setIsDetailDialogOpen(true)
      await loadInventoryStocktakingList(buildQueryParams(queryForm))
    } catch (error) {
      setCreateError(normalizeError(error))
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleOpenDetail = async (stocktakingId: string) => {
    setIsDetailDialogOpen(true)
    setIsSavingDetail(true)
    setDetailError(null)

    try {
      const detail = await getInventoryStocktakingDetail(stocktakingId)
      setCurrentDetail(detail)
    } catch (error) {
      setDetailError(normalizeError(error))
      setIsDetailDialogOpen(false)
    } finally {
      setIsSavingDetail(false)
    }
  }

  const handleSaveCounts = async () => {
    if (!currentDetail) {
      return
    }

    setIsSavingDetail(true)
    setDetailError(null)

    try {
      const detail = await saveInventoryStocktakingItems(
        currentDetail.id,
        currentDetail.items.map(mapItemToSavePayload),
      )
      setCurrentDetail(detail)
      await loadInventoryStocktakingList(buildQueryParams(queryForm))
    } catch (error) {
      setDetailError(normalizeError(error))
    } finally {
      setIsSavingDetail(false)
    }
  }

  const handleConfirm = async () => {
    if (!currentDetail) {
      return
    }

    setIsSavingDetail(true)
    setDetailError(null)

    try {
      const savedDetail = await saveInventoryStocktakingItems(
        currentDetail.id,
        currentDetail.items.map(mapItemToSavePayload),
      )
      setCurrentDetail(savedDetail)
      await confirmInventoryStocktaking(savedDetail.id)
      setIsDetailDialogOpen(false)
      setCurrentDetail(null)
      await loadInventoryStocktakingList(buildQueryParams(queryForm))
    } catch (error) {
      setDetailError(normalizeError(error))
    } finally {
      setIsSavingDetail(false)
    }
  }

  const handleCancel = async (row: InventoryStocktakingRow) => {
    const confirmed = window.confirm(`确定要取消盘点单“${row.stocktakingCode}”吗？`)

    if (!confirmed) {
      return
    }

    try {
      await cancelInventoryStocktaking(row.id)
      await loadInventoryStocktakingList(buildQueryParams(queryForm))
    } catch (error) {
      setPageError(normalizeError(error))
    }
  }

  return (
    <section className="inventory-stocktaking-page">
      {pageError ? (
        <div className="inventory-stocktaking-page__message inventory-stocktaking-page__message--error">
          <strong>{pageError.message}</strong>
          {pageError.traceId ? <span>追踪编号：{pageError.traceId}</span> : null}
        </div>
      ) : null}

      <div className="inventory-stocktaking-page__search">
        <div className="inventory-stocktaking-page__search-grid">
          <label className="inventory-stocktaking-page__field">
            <span>盘点单编号</span>
            <input
              type="text"
              placeholder="输入盘点单编号"
              value={queryForm.stocktakingCode}
              onChange={(event) =>
                setQueryForm((current) => ({
                  ...current,
                  stocktakingCode: event.target.value,
                }))
              }
            />
          </label>

          <label className="inventory-stocktaking-page__field">
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

          <label className="inventory-stocktaking-page__field">
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
              <option value="1">待盘点</option>
              <option value="2">待确认</option>
              <option value="3">已完成</option>
              <option value="4">已取消</option>
            </select>
          </label>

          <div className="inventory-stocktaking-page__actions">
            <button type="button" className="is-ghost" onClick={() => void handleReset()}>
              重置
            </button>
            <button type="button" className="is-dark" onClick={() => void handleSearch()}>
              查询
            </button>
          </div>
        </div>
      </div>

      <div className="inventory-stocktaking-page__toolbar">
        <div className="inventory-stocktaking-page__toolbar-copy">
          <h3>库存盘点列表</h3>
          <p>创建盘点范围、录入实盘数量并确认差异回写库存，是库存治理闭环的最终环节。</p>
        </div>

        <button type="button" className="inventory-stocktaking-page__primary" onClick={handleCreate}>
          新增盘点单
        </button>
      </div>

      <div className="inventory-stocktaking-page__table-shell">
        {isLoading ? (
          <div className="inventory-stocktaking-page__loading">正在加载库存盘点...</div>
        ) : (
          <TreeDataTable
            data={stocktakings}
            columns={columns}
            emptyText="暂无库存盘点单"
          />
        )}
      </div>

      {isCreateDialogOpen ? (
        <div className="inventory-stocktaking-page__dialog-backdrop">
          <div className="inventory-stocktaking-page__dialog inventory-stocktaking-page__dialog--create">
            <div className="inventory-stocktaking-page__dialog-header">
              <div>
                <h3>新增盘点单</h3>
                <p>选择仓库和盘点范围后生成盘点明细，随后录入实盘结果。</p>
              </div>
              <button
                type="button"
                className="inventory-stocktaking-page__dialog-close"
                onClick={() => {
                  setIsCreateDialogOpen(false)
                  setCreateError(null)
                }}
              >
                ×
              </button>
            </div>

            <form className="inventory-stocktaking-page__dialog-form" onSubmit={handleCreateSubmit}>
              {createError ? (
                <div className="inventory-stocktaking-page__message inventory-stocktaking-page__message--error inventory-stocktaking-page__field--full">
                  <strong>{createError.message}</strong>
                  {createError.traceId ? <span>追踪编号：{createError.traceId}</span> : null}
                </div>
              ) : null}

              <label className="inventory-stocktaking-page__field">
                <span>仓库</span>
                <select
                  value={createForm.warehouseId}
                  onChange={(event) =>
                    setCreateForm((current) => ({
                      ...current,
                      warehouseId: event.target.value,
                      zoneId: '',
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
                <small>{getFieldError(createError?.fieldErrors, 'warehouseId')}</small>
              </label>

              <label className="inventory-stocktaking-page__field">
                <span>库区范围</span>
                <select
                  value={createForm.zoneId}
                  onChange={(event) =>
                    setCreateForm((current) => ({
                      ...current,
                      zoneId: event.target.value,
                    }))
                  }
                  disabled={!createForm.warehouseId}
                >
                  <option value="">全部库区</option>
                  {filteredZoneOptions.map((option) => (
                    <option key={option.id} value={option.id}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </label>

              <label className="inventory-stocktaking-page__field inventory-stocktaking-page__field--full">
                <span>备注</span>
                <textarea
                  value={createForm.remarks}
                  onChange={(event) =>
                    setCreateForm((current) => ({
                      ...current,
                      remarks: event.target.value,
                    }))
                  }
                  placeholder="填写盘点说明"
                />
              </label>

              <div className="inventory-stocktaking-page__dialog-actions">
                <button
                  type="button"
                  className="is-ghost"
                  onClick={() => {
                    setIsCreateDialogOpen(false)
                    setCreateError(null)
                  }}
                >
                  取消
                </button>
                <button type="submit" className="is-dark" disabled={isSubmitting}>
                  {isSubmitting ? '提交中...' : '生成盘点单'}
                </button>
              </div>
            </form>
          </div>
        </div>
      ) : null}

      {isDetailDialogOpen && currentDetail ? (
        <div className="inventory-stocktaking-page__dialog-backdrop">
          <div className="inventory-stocktaking-page__dialog inventory-stocktaking-page__dialog--detail">
            <div className="inventory-stocktaking-page__dialog-header">
              <div>
                <h3>盘点录入</h3>
                <p>{`${currentDetail.stocktakingCode} / ${currentDetail.warehouseName} / ${currentDetail.zoneName ?? '全部库区'}`}</p>
              </div>
              <button
                type="button"
                className="inventory-stocktaking-page__dialog-close"
                onClick={() => {
                  setIsDetailDialogOpen(false)
                  setCurrentDetail(null)
                  setDetailError(null)
                }}
              >
                ×
              </button>
            </div>

            <div className="inventory-stocktaking-page__detail-shell">
              {detailError ? (
                <div className="inventory-stocktaking-page__message inventory-stocktaking-page__message--error">
                  <strong>{detailError.message}</strong>
                  {detailError.traceId ? <span>追踪编号：{detailError.traceId}</span> : null}
                </div>
              ) : null}

              <div className="inventory-stocktaking-page__detail-summary">
                <span>{`状态：${currentDetail.statusLabel}`}</span>
                <span>{`明细数：${currentDetail.itemCount}`}</span>
                <span>{`已录入：${currentDetail.countedItemCount}`}</span>
                <span>{`总差异：${currentDetail.totalDifferenceQuantity}`}</span>
              </div>

              <div className="inventory-stocktaking-page__detail-table-shell">
                <table className="inventory-stocktaking-page__detail-table">
                  <thead>
                    <tr>
                      <th>产品</th>
                      <th>库位</th>
                      <th>系统数量</th>
                      <th>实盘数量</th>
                      <th>差异</th>
                      <th>备注</th>
                    </tr>
                  </thead>
                  <tbody>
                    {currentDetail.items.map((item, index) => (
                      <tr key={item.id}>
                        <td>
                          <div className="inventory-stocktaking-page__item-product">
                            <strong>{item.productName}</strong>
                            <span>{item.productCode}</span>
                          </div>
                        </td>
                        <td>{`${item.zoneName} / ${item.locationName}`}</td>
                        <td>{item.systemQuantity}</td>
                        <td>
                          <input
                            type="number"
                            min="0"
                            step={buildQuantityStep(item.precisionDigits)}
                            value={item.countedQuantity ?? ''}
                            onChange={(event) =>
                              updateCurrentDetailItem(index, 'countedQuantity', event.target.value)
                            }
                          />
                        </td>
                        <td>{calculateDifference(item.systemQuantity, item.countedQuantity)}</td>
                        <td>
                          <input
                            type="text"
                            value={item.remarks ?? ''}
                            onChange={(event) =>
                              updateCurrentDetailItem(index, 'remarks', event.target.value)
                            }
                          />
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              <div className="inventory-stocktaking-page__dialog-actions">
                <button
                  type="button"
                  className="is-ghost"
                  onClick={() => {
                    setIsDetailDialogOpen(false)
                    setCurrentDetail(null)
                    setDetailError(null)
                  }}
                >
                  关闭
                </button>
                {currentDetail.status !== 3 && currentDetail.status !== 4 ? (
                  <button
                    type="button"
                    className="is-ghost"
                    onClick={() => void handleSaveCounts()}
                    disabled={isSavingDetail}
                  >
                    {isSavingDetail ? '保存中...' : '保存盘点'}
                  </button>
                ) : null}
                {currentDetail.status !== 3 && currentDetail.status !== 4 ? (
                  <button
                    type="button"
                    className="is-dark"
                    onClick={() => void handleConfirm()}
                    disabled={isSavingDetail}
                  >
                    {isSavingDetail ? '处理中...' : '确认盘点'}
                  </button>
                ) : null}
              </div>
            </div>
          </div>
        </div>
      ) : null}
    </section>
  )

  function updateCurrentDetailItem(
    index: number,
    field: 'countedQuantity' | 'remarks',
    value: string,
  ) {
    setCurrentDetail((current) =>
      current
        ? {
            ...current,
            items: current.items.map((item, itemIndex) =>
              itemIndex === index
                ? {
                    ...item,
                    [field]:
                      field === 'countedQuantity'
                        ? value === ''
                          ? null
                          : Number(value)
                        : value,
                    differenceQuantity:
                      field === 'countedQuantity'
                        ? value === ''
                          ? null
                          : Number(value) - item.systemQuantity
                        : item.differenceQuantity,
                  }
                : item,
            ),
          }
        : current,
    )
  }
}

function toTableRows(rows: InventoryStocktakingListItem[]): InventoryStocktakingRow[] {
  return rows.map((row) => ({
    ...row,
  }))
}

function buildQueryParams(queryForm: {
  stocktakingCode: string
  warehouseId: string
  status: string
}): InventoryStocktakingListQuery {
  return {
    stocktakingCode: queryForm.stocktakingCode.trim() || undefined,
    warehouseId: queryForm.warehouseId || undefined,
    status: queryForm.status ? Number(queryForm.status) : undefined,
  }
}

function mapCreateFormToPayload(createForm: CreateFormState): InventoryStocktakingCreatePayload {
  return {
    warehouseId: Number(createForm.warehouseId),
    zoneId: createForm.zoneId ? Number(createForm.zoneId) : null,
    remarks: createForm.remarks.trim() || null,
  }
}

function mapItemToSavePayload(item: InventoryStocktakingItem): InventoryStocktakingItemSavePayload {
  return {
    itemId: Number(item.id),
    countedQuantity: Number(item.countedQuantity ?? 0),
    remarks: item.remarks?.trim() || null,
  }
}

function calculateDifference(systemQuantity: number, countedQuantity: number | null) {
  if (countedQuantity === null) {
    return '-'
  }

  return countedQuantity - systemQuantity
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

export default InventoryStocktakingPage
