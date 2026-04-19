import { type FormEvent, useEffect, useState } from 'react'

import { normalizeError, type AppError } from '../../api/errors'
import TreeDataTable, {
  type TreeTableColumn,
  type TreeTableRow,
} from '../../components/tree-data-table/TreeDataTable'
import {
  assignPutawayTask,
  cancelPutawayTask,
  completePutawayTask,
  getPutawayTaskDetail,
  getPutawayTaskList,
} from '../../features/putawaytask/api'
import type {
  PutawayTaskDetail,
  PutawayTaskListItem,
  PutawayTaskListQuery,
} from '../../features/putawaytask/types'
import { getWarehouseOptions } from '../../features/warehouse/api'
import type { WarehouseOption } from '../../features/warehouse/types'
import { getWarehouseLocationOptions } from '../../features/warehouselocation/api'
import type { WarehouseLocationOption } from '../../features/warehouselocation/types'
import { getWarehouseZoneOptions } from '../../features/warehousezone/api'
import type { WarehouseZoneOption } from '../../features/warehousezone/types'
import './PutawayTaskPage.css'

type PutawayTaskRow = TreeTableRow & PutawayTaskListItem

interface AssignFormState {
  zoneId: string
  locationId: string
}

const initialAssignFormState: AssignFormState = {
  zoneId: '',
  locationId: '',
}

function PutawayTaskPage() {
  const [queryForm, setQueryForm] = useState({
    taskCode: '',
    warehouseId: '',
    status: '',
  })
  const [putawayTasks, setPutawayTasks] = useState<PutawayTaskRow[]>([])
  const [warehouseOptions, setWarehouseOptions] = useState<WarehouseOption[]>([])
  const [warehouseZoneOptions, setWarehouseZoneOptions] = useState<
    WarehouseZoneOption[]
  >([])
  const [warehouseLocationOptions, setWarehouseLocationOptions] = useState<
    WarehouseLocationOption[]
  >([])
  const [pageError, setPageError] = useState<AppError | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  const [isDialogOpen, setIsDialogOpen] = useState(false)
  const [currentTask, setCurrentTask] = useState<PutawayTaskDetail | null>(null)
  const [assignForm, setAssignForm] = useState<AssignFormState>(initialAssignFormState)
  const [isSubmitting, setIsSubmitting] = useState(false)

  useEffect(() => {
    let isMounted = true

    const bootstrap = async () => {
      try {
        const [list, warehouses, zones, locations] = await Promise.all([
          getPutawayTaskList(),
          getWarehouseOptions(),
          getWarehouseZoneOptions(),
          getWarehouseLocationOptions(),
        ])

        if (!isMounted) {
          return
        }

        setPutawayTasks(toTableRows(list))
        setWarehouseOptions(warehouses)
        setWarehouseZoneOptions(zones)
        setWarehouseLocationOptions(locations)
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

  async function loadPutawayTaskList(query: PutawayTaskListQuery) {
    setIsLoading(true)
    setPageError(null)

    try {
      const list = await getPutawayTaskList(query)
      setPutawayTasks(toTableRows(list))
    } catch (error) {
      setPageError(normalizeError(error))
    } finally {
      setIsLoading(false)
    }
  }

  const columns: TreeTableColumn<PutawayTaskRow>[] = [
    {
      key: 'taskCode',
      title: '任务编号',
      minWidth: 180,
      render: (row) => row.taskCode,
    },
    {
      key: 'orderCode',
      title: '入库单',
      minWidth: 180,
      render: (row) => row.inboundOrderCode,
    },
    {
      key: 'product',
      title: '产品',
      minWidth: 220,
      render: (row) => (
        <div className="putaway-task-page__name-cell">
          <strong>{row.productName}</strong>
          <span>{row.productCode}</span>
        </div>
      ),
    },
    {
      key: 'supplier',
      title: '供应商',
      minWidth: 180,
      render: (row) => row.supplierName,
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
      minWidth: 160,
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
          className={`putaway-task-page__status${
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
      minWidth: 300,
      width: 300,
      sticky: 'right',
      align: 'right',
      render: (row) => (
        <div className="putaway-task-page__row-actions">
          {row.status === 1 || row.status === 2 ? (
            <button type="button" onClick={() => handleAssign(row.id)}>
              分配库位
            </button>
          ) : null}
          {row.status === 2 ? (
            <button type="button" onClick={() => handleComplete(row)}>
              完成上架
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

  const currentTaskZoneOptions = warehouseZoneOptions.filter(
    (option) => option.warehouseId === currentTask?.warehouseId,
  )

  const currentTaskLocationOptions = warehouseLocationOptions.filter(
    (option) =>
      option.warehouseId === currentTask?.warehouseId &&
      option.zoneId === assignForm.zoneId,
  )

  const handleSearch = async () => {
    await loadPutawayTaskList(buildQueryParams(queryForm))
  }

  const handleReset = async () => {
    const resetState = {
      taskCode: '',
      warehouseId: '',
      status: '',
    }

    setQueryForm(resetState)
    await loadPutawayTaskList({})
  }

  const handleAssign = async (taskId: string) => {
    setIsDialogOpen(true)
    setIsSubmitting(true)
    setAssignForm(initialAssignFormState)

    try {
      const detail = await getPutawayTaskDetail(taskId)
      setCurrentTask(detail)
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
      await assignPutawayTask(currentTask.id, {
        zoneId: Number(assignForm.zoneId),
        locationId: Number(assignForm.locationId),
      })
      setIsDialogOpen(false)
      setCurrentTask(null)
      await loadPutawayTaskList(buildQueryParams(queryForm))
    } catch (error) {
      setPageError(normalizeError(error))
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleComplete = async (row: PutawayTaskRow) => {
    const confirmed = window.confirm(`确认任务“${row.taskCode}”已完成上架吗？`)

    if (!confirmed) {
      return
    }

    try {
      await completePutawayTask(row.id)
      await loadPutawayTaskList(buildQueryParams(queryForm))
    } catch (error) {
      setPageError(normalizeError(error))
    }
  }

  const handleCancel = async (row: PutawayTaskRow) => {
    const confirmed = window.confirm(`确定要取消任务“${row.taskCode}”吗？`)

    if (!confirmed) {
      return
    }

    try {
      await cancelPutawayTask(row.id)
      await loadPutawayTaskList(buildQueryParams(queryForm))
    } catch (error) {
      setPageError(normalizeError(error))
    }
  }

  return (
    <div className="putaway-task-page">
      {pageError ? (
        <div className="putaway-task-page__message putaway-task-page__message--error">
          <strong>{pageError.message}</strong>
          {pageError.traceId ? <span>traceId: {pageError.traceId}</span> : null}
        </div>
      ) : null}

      <section className="putaway-task-page__search">
        <div className="putaway-task-page__search-grid">
          <label className="putaway-task-page__field">
            <span>任务编号</span>
            <input
              type="text"
              placeholder="输入编号..."
              value={queryForm.taskCode}
              onChange={(event) =>
                setQueryForm((current) => ({
                  ...current,
                  taskCode: event.target.value,
                }))
              }
            />
          </label>

          <label className="putaway-task-page__field">
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

          <label className="putaway-task-page__field">
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
              <option value="2">待上架</option>
              <option value="3">已完成</option>
              <option value="4">已取消</option>
            </select>
          </label>

          <div className="putaway-task-page__actions">
            <button type="button" className="is-ghost" onClick={() => void handleReset()}>
              重置
            </button>
            <button type="button" className="is-dark" onClick={() => void handleSearch()}>
              查询
            </button>
          </div>
        </div>
      </section>

      <section className="putaway-task-page__toolbar">
        <div className="putaway-task-page__toolbar-copy">
          <h3>上架任务列表</h3>
          <p>承接已到货的入库单明细，分配库区库位并完成上架，是库存形成的直接入口。</p>
        </div>
      </section>

      <section className="putaway-task-page__table-shell">
        {isLoading ? (
          <div className="putaway-task-page__loading">正在加载上架任务...</div>
        ) : (
          <TreeDataTable
            data={putawayTasks}
            columns={columns}
            emptyText="暂无上架任务数据"
          />
        )}
      </section>

      {isDialogOpen && currentTask ? (
        <div className="putaway-task-page__dialog-backdrop">
          <div className="putaway-task-page__dialog">
            <div className="putaway-task-page__dialog-header">
              <div>
                <h3>分配库位</h3>
                <p>
                  {currentTask.taskCode} / {currentTask.productName}
                </p>
              </div>
              <button
                type="button"
                className="putaway-task-page__dialog-close"
                onClick={() => setIsDialogOpen(false)}
              >
                ×
              </button>
            </div>

            <form className="putaway-task-page__dialog-form" onSubmit={handleAssignSubmit}>
              <label className="putaway-task-page__field">
                <span>所属仓库</span>
                <input value={currentTask.warehouseName} readOnly disabled />
              </label>

              <label className="putaway-task-page__field">
                <span>库区</span>
                <select
                  value={assignForm.zoneId}
                  onChange={(event) =>
                    setAssignForm((current) => ({
                      ...current,
                      zoneId: event.target.value,
                      locationId: '',
                    }))
                  }
                >
                  <option value="">请选择</option>
                  {currentTaskZoneOptions.map((option) => (
                    <option key={option.id} value={option.id}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </label>

              <label className="putaway-task-page__field">
                <span>库位</span>
                <select
                  value={assignForm.locationId}
                  disabled={!assignForm.zoneId}
                  onChange={(event) =>
                    setAssignForm((current) => ({
                      ...current,
                      locationId: event.target.value,
                    }))
                  }
                >
                  <option value="">{assignForm.zoneId ? '请选择' : '请先选择库区'}</option>
                  {currentTaskLocationOptions.map((option) => (
                    <option key={option.id} value={option.id}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </label>

              <div className="putaway-task-page__dialog-actions">
                <button
                  type="button"
                  className="is-ghost"
                  onClick={() => setIsDialogOpen(false)}
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

function buildQueryParams(queryForm: {
  taskCode: string
  warehouseId: string
  status: string
}): PutawayTaskListQuery {
  return {
    taskCode: queryForm.taskCode.trim() || undefined,
    warehouseId: queryForm.warehouseId || undefined,
    status: queryForm.status ? Number(queryForm.status) : undefined,
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

function toTableRows(taskList: PutawayTaskListItem[]): PutawayTaskRow[] {
  return taskList.map((item) => ({
    ...item,
    children: [],
  }))
}

export default PutawayTaskPage
