import { type FormEvent, useEffect, useState } from 'react'

import { normalizeError, type AppError } from '../../api/errors'
import TreeDataTable, {
  type TreeTableColumn,
  type TreeTableRow,
} from '../../components/tree-data-table/TreeDataTable'
import {
  assignOutboundTask,
  cancelOutboundTask,
  completeOutboundTask,
  getOutboundTaskDetail,
  getOutboundTaskList,
  getOutboundTaskStockOptions,
  pickOutboundTask,
} from '../../features/outboundtask/api'
import type {
  OutboundTaskDetail,
  OutboundTaskListItem,
  OutboundTaskListQuery,
  OutboundTaskStockOption,
} from '../../features/outboundtask/types'
import { getWarehouseOptions } from '../../features/warehouse/api'
import type { WarehouseOption } from '../../features/warehouse/types'
import './OutboundTaskPage.css'

type OutboundTaskRow = TreeTableRow & OutboundTaskListItem

interface AssignFormState {
  zoneId: string
  locationId: string
}

const initialAssignFormState: AssignFormState = {
  zoneId: '',
  locationId: '',
}

function OutboundTaskPage() {
  const [queryForm, setQueryForm] = useState({
    taskCode: '',
    warehouseId: '',
    status: '',
  })
  const [outboundTasks, setOutboundTasks] = useState<OutboundTaskRow[]>([])
  const [warehouseOptions, setWarehouseOptions] = useState<WarehouseOption[]>([])
  const [stockOptions, setStockOptions] = useState<OutboundTaskStockOption[]>([])
  const [pageError, setPageError] = useState<AppError | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  const [isDialogOpen, setIsDialogOpen] = useState(false)
  const [currentTask, setCurrentTask] = useState<OutboundTaskDetail | null>(null)
  const [assignForm, setAssignForm] = useState<AssignFormState>(initialAssignFormState)
  const [isSubmitting, setIsSubmitting] = useState(false)

  useEffect(() => {
    let isMounted = true

    const bootstrap = async () => {
      try {
        const [list, warehouses] = await Promise.all([
          getOutboundTaskList(),
          getWarehouseOptions(),
        ])

        if (!isMounted) {
          return
        }

        setOutboundTasks(toTableRows(list))
        setWarehouseOptions(warehouses)
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

  async function loadOutboundTaskList(query: OutboundTaskListQuery) {
    setIsLoading(true)
    setPageError(null)

    try {
      const list = await getOutboundTaskList(query)
      setOutboundTasks(toTableRows(list))
    } catch (error) {
      setPageError(normalizeError(error))
    } finally {
      setIsLoading(false)
    }
  }

  const columns: TreeTableColumn<OutboundTaskRow>[] = [
    {
      key: 'taskCode',
      title: '任务编号',
      minWidth: 180,
      render: (row) => row.taskCode,
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
        <div className="outbound-task-page__name-cell">
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
      title: '仓库',
      minWidth: 160,
      render: (row) => row.warehouseName,
    },
    {
      key: 'zone',
      title: '库区',
      minWidth: 160,
      render: (row) => row.zoneName ?? '-',
    },
    {
      key: 'location',
      title: '库位',
      minWidth: 180,
      render: (row) => row.locationName ?? '-',
    },
    {
      key: 'quantity',
      title: '数量',
      minWidth: 100,
      render: (row) => row.quantity,
    },
    {
      key: 'status',
      title: '状态',
      minWidth: 120,
      render: (row) => (
        <span
          className={`outbound-task-page__status${
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
      minWidth: 360,
      width: 360,
      sticky: 'right',
      align: 'right',
      render: (row) => (
        <div className="outbound-task-page__row-actions">
          {row.status === 1 || row.status === 2 ? (
            <button type="button" onClick={() => handleAssign(row.id)}>
              分配库存
            </button>
          ) : null}
          {row.status === 2 ? (
            <button type="button" onClick={() => handlePick(row)}>
              确认拣货
            </button>
          ) : null}
          {row.status === 3 ? (
            <button type="button" onClick={() => handleComplete(row)}>
              确认出库
            </button>
          ) : null}
          {row.status !== 4 && row.status !== 5 ? (
            <button type="button" onClick={() => handleCancel(row)}>
              取消
            </button>
          ) : null}
        </div>
      ),
    },
  ]

  const currentTaskZoneOptions = deduplicateZoneOptions(stockOptions)

  const currentTaskLocationOptions = stockOptions.filter(
    (option) => option.zoneId === assignForm.zoneId,
  )

  const selectedStockOption =
    currentTaskLocationOptions.find((option) => option.locationId === assignForm.locationId) ??
    null

  const handleSearch = async () => {
    await loadOutboundTaskList(buildQueryParams(queryForm))
  }

  const handleReset = async () => {
    const resetState = {
      taskCode: '',
      warehouseId: '',
      status: '',
    }

    setQueryForm(resetState)
    await loadOutboundTaskList({})
  }

  const handleAssign = async (taskId: string) => {
    setIsDialogOpen(true)
    setIsSubmitting(true)
    setAssignForm(initialAssignFormState)
    setStockOptions([])

    try {
      const [detail, stockOptionList] = await Promise.all([
        getOutboundTaskDetail(taskId),
        getOutboundTaskStockOptions(taskId),
      ])
      setCurrentTask(detail)
      setStockOptions(stockOptionList)
      setAssignForm({
        zoneId: detail.zoneId ?? '',
        locationId: detail.locationId ?? '',
      })
    } catch (error) {
      setPageError(normalizeError(error))
      setIsDialogOpen(false)
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleAssignSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    if (!currentTask) {
      return
    }

    setIsSubmitting(true)

    try {
      await assignOutboundTask(currentTask.id, {
        zoneId: Number(assignForm.zoneId),
        locationId: Number(assignForm.locationId),
      })
      setIsDialogOpen(false)
      setCurrentTask(null)
      setStockOptions([])
      await loadOutboundTaskList(buildQueryParams(queryForm))
    } catch (error) {
      setPageError(normalizeError(error))
    } finally {
      setIsSubmitting(false)
    }
  }

  const handlePick = async (row: OutboundTaskRow) => {
    const confirmed = window.confirm(`确认任务“${row.taskCode}”已完成拣货吗？`)

    if (!confirmed) {
      return
    }

    try {
      await pickOutboundTask(row.id)
      await loadOutboundTaskList(buildQueryParams(queryForm))
    } catch (error) {
      setPageError(normalizeError(error))
    }
  }

  const handleComplete = async (row: OutboundTaskRow) => {
    const confirmed = window.confirm(`确认任务“${row.taskCode}”已完成出库吗？`)

    if (!confirmed) {
      return
    }

    try {
      await completeOutboundTask(row.id)
      await loadOutboundTaskList(buildQueryParams(queryForm))
    } catch (error) {
      setPageError(normalizeError(error))
    }
  }

  const handleCancel = async (row: OutboundTaskRow) => {
    const confirmed = window.confirm(`确定要取消任务“${row.taskCode}”吗？`)

    if (!confirmed) {
      return
    }

    try {
      await cancelOutboundTask(row.id)
      await loadOutboundTaskList(buildQueryParams(queryForm))
    } catch (error) {
      setPageError(normalizeError(error))
    }
  }

  return (
    <div className="outbound-task-page">
      {pageError ? (
        <div className="outbound-task-page__message outbound-task-page__message--error">
          <strong>{pageError.message}</strong>
          {pageError.traceId ? <span>追踪编号：{pageError.traceId}</span> : null}
        </div>
      ) : null}

      <div className="outbound-task-page__search">
        <div className="outbound-task-page__search-grid">
          <label className="outbound-task-page__field">
            <span>任务编号</span>
            <input
              type="text"
              placeholder="输入任务编号"
              value={queryForm.taskCode}
              onChange={(event) =>
                setQueryForm((current) => ({
                  ...current,
                  taskCode: event.target.value,
                }))
              }
            />
          </label>

          <label className="outbound-task-page__field">
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

          <label className="outbound-task-page__field">
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

          <div className="outbound-task-page__actions">
            <button type="button" className="is-ghost" onClick={() => void handleReset()}>
              重置
            </button>
            <button type="button" className="is-dark" onClick={() => void handleSearch()}>
              查询
            </button>
          </div>
        </div>
      </div>

      <div className="outbound-task-page__toolbar">
        <div className="outbound-task-page__toolbar-copy">
          <h3>拣货出库任务列表</h3>
          <p>承接出库单明细，完成库存分配、拣货确认和最终出库扣减，作为库存减少的直接入口。</p>
        </div>
      </div>

      <div className="outbound-task-page__table-shell">
        {isLoading ? (
          <div className="outbound-task-page__loading">正在加载拣货出库任务...</div>
        ) : (
          <TreeDataTable
            data={outboundTasks}
            columns={columns}
            emptyText="暂无拣货出库任务"
          />
        )}
      </div>

      {isDialogOpen ? (
        <div className="outbound-task-page__dialog-backdrop">
          <div className="outbound-task-page__dialog">
            <div className="outbound-task-page__dialog-header">
              <div>
                <h3>分配库存</h3>
                <p>从当前仓库的可用库存中选择库区库位，锁定该任务的拣货来源。</p>
              </div>
              <button
                type="button"
                className="outbound-task-page__dialog-close"
                onClick={() => {
                  setIsDialogOpen(false)
                  setCurrentTask(null)
                  setStockOptions([])
                }}
              >
                ×
              </button>
            </div>

            <form className="outbound-task-page__dialog-form" onSubmit={handleAssignSubmit}>
              <label className="outbound-task-page__field">
                <span>任务编号</span>
                <input type="text" value={currentTask?.taskCode ?? ''} readOnly />
              </label>

              <label className="outbound-task-page__field">
                <span>产品</span>
                <input
                  type="text"
                  value={currentTask ? `${currentTask.productName} / ${currentTask.productCode}` : ''}
                  readOnly
                />
              </label>

              <label className="outbound-task-page__field">
                <span>库区</span>
                <select
                  value={assignForm.zoneId}
                  onChange={(event) =>
                    setAssignForm({
                      zoneId: event.target.value,
                      locationId: '',
                    })
                  }
                >
                  <option value="">选择库区</option>
                  {currentTaskZoneOptions.map((option) => (
                    <option key={option.zoneId} value={option.zoneId}>
                      {option.zoneName}
                    </option>
                  ))}
                </select>
              </label>

              <label className="outbound-task-page__field">
                <span>库位</span>
                <select
                  value={assignForm.locationId}
                  onChange={(event) =>
                    setAssignForm((current) => ({
                      ...current,
                      locationId: event.target.value,
                    }))
                  }
                  disabled={!assignForm.zoneId}
                >
                  <option value="">选择库位</option>
                  {currentTaskLocationOptions.map((option) => (
                    <option key={option.locationId} value={option.locationId}>
                      {option.locationName}（可用 {option.availableQuantity}）
                    </option>
                  ))}
                </select>
              </label>

              <div className="outbound-task-page__stock-hint">
                <strong>可用库存</strong>
                <span>
                  {selectedStockOption
                    ? `${selectedStockOption.locationName} 当前可用 ${selectedStockOption.availableQuantity}`
                    : '请选择库区和库位查看可用库存'}
                </span>
              </div>

              <div className="outbound-task-page__dialog-actions">
                <button
                  type="button"
                  className="is-ghost"
                  onClick={() => {
                    setIsDialogOpen(false)
                    setCurrentTask(null)
                    setStockOptions([])
                  }}
                >
                  取消
                </button>
                <button type="submit" className="is-dark" disabled={isSubmitting}>
                  {isSubmitting ? '提交中...' : '确认分配'}
                </button>
              </div>
            </form>
          </div>
        </div>
      ) : null}
    </div>
  )
}

function deduplicateZoneOptions(stockOptions: OutboundTaskStockOption[]) {
  const zoneMap = new Map<string, { zoneId: string; zoneName: string }>()

  stockOptions.forEach((option) => {
    if (!zoneMap.has(option.zoneId)) {
      zoneMap.set(option.zoneId, {
        zoneId: option.zoneId,
        zoneName: option.zoneName,
      })
    }
  })

  return [...zoneMap.values()]
}

function toTableRows(rows: OutboundTaskListItem[]): OutboundTaskRow[] {
  return rows.map((row) => ({
    ...row,
  }))
}

function buildQueryParams(queryForm: {
  taskCode: string
  warehouseId: string
  status: string
}): OutboundTaskListQuery {
  return {
    taskCode: queryForm.taskCode.trim() || undefined,
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

export default OutboundTaskPage
