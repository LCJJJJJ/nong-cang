import { type CSSProperties, Fragment, type ReactNode, useMemo, useState } from 'react'

import './TreeDataTable.css'

export interface TreeTableRow {
  id: string
  children?: TreeTableRow[]
}

type ColumnAlign = 'left' | 'center' | 'right'

interface TreeTableRenderContext {
  depth: number
  hasChildren: boolean
  isExpanded: boolean
  toggle: () => void
}

export interface TreeTableColumn<T extends TreeTableRow> {
  key: string
  title: string
  width?: number
  minWidth?: number
  align?: ColumnAlign
  tree?: boolean
  sticky?: 'right'
  render: (row: T, context: TreeTableRenderContext) => ReactNode
}

interface FlatTreeTableRow<T extends TreeTableRow> {
  row: T
  depth: number
}

interface TreeDataTableProps<T extends TreeTableRow> {
  data: T[]
  columns: TreeTableColumn<T>[]
  expandedKeys?: string[]
  defaultExpandedKeys?: string[]
  onExpandedKeysChange?: (expandedKeys: string[]) => void
  emptyText?: string
}

function TreeDataTable<T extends TreeTableRow>({
  data,
  columns,
  expandedKeys,
  defaultExpandedKeys,
  onExpandedKeysChange,
  emptyText = '暂无数据',
}: TreeDataTableProps<T>) {
  const [internalExpandedKeys, setInternalExpandedKeys] = useState<string[]>(
    defaultExpandedKeys ?? [],
  )

  const resolvedExpandedKeys = expandedKeys ?? internalExpandedKeys

  const expandedKeySet = useMemo(
    () => new Set(resolvedExpandedKeys),
    [resolvedExpandedKeys],
  )

  const flatRows = useMemo(
    () => flattenTreeRows(data, expandedKeySet),
    [data, expandedKeySet],
  )

  const stickyRightOffsets = useMemo(() => {
    let offset = 0
    const offsetMap = new Map<string, number>()

    for (let index = columns.length - 1; index >= 0; index -= 1) {
      const column = columns[index]

      if (column.sticky === 'right') {
        offsetMap.set(column.key, offset)
        offset += column.width ?? column.minWidth ?? 0
      }
    }

    return offsetMap
  }, [columns])

  const handleExpandedKeysChange = (nextExpandedKeys: string[]) => {
    if (expandedKeys === undefined) {
      setInternalExpandedKeys(nextExpandedKeys)
    }

    onExpandedKeysChange?.(nextExpandedKeys)
  }

  const handleToggle = (rowId: string) => {
    const nextExpandedKeys = expandedKeySet.has(rowId)
      ? resolvedExpandedKeys.filter((key) => key !== rowId)
      : [...resolvedExpandedKeys, rowId]

    handleExpandedKeysChange(nextExpandedKeys)
  }

  return (
    <div className="tree-data-table">
      <div className="tree-data-table__scroll">
        <table className="tree-data-table__table">
          <thead>
            <tr>
              {columns.map((column) => (
                <th
                  key={column.key}
                  className={buildCellClassName({
                    align: column.align,
                    sticky: column.sticky,
                    isHeader: true,
                  })}
                  style={buildCellStyle(column, stickyRightOffsets)}
                >
                  {column.title}
                </th>
              ))}
            </tr>
          </thead>

          <tbody>
            {flatRows.length === 0 ? (
              <tr>
                <td
                  className="tree-data-table__empty"
                  colSpan={columns.length}
                >
                  {emptyText}
                </td>
              </tr>
            ) : (
              flatRows.map(({ row, depth }) => (
                <tr key={row.id}>
                  {columns.map((column) => {
                    const hasChildren = Boolean(row.children?.length)
                    const isExpanded = expandedKeySet.has(row.id)
                    const context: TreeTableRenderContext = {
                      depth,
                      hasChildren,
                      isExpanded,
                      toggle: () => handleToggle(row.id),
                    }

                    return (
                      <td
                        key={column.key}
                        className={buildCellClassName({
                          align: column.align,
                          sticky: column.sticky,
                          isTree: column.tree,
                        })}
                        style={buildCellStyle(column, stickyRightOffsets)}
                      >
                        {column.tree ? (
                          <div
                            className="tree-data-table__tree-cell"
                            style={
                              {
                                '--tree-indent': `${depth * 28}px`,
                              } as CSSProperties
                            }
                          >
                            <button
                              type="button"
                              className={`tree-data-table__toggle${
                                !hasChildren ? ' is-placeholder' : ''
                              }`}
                              onClick={
                                hasChildren ? () => handleToggle(row.id) : undefined
                              }
                              aria-label={
                                hasChildren
                                  ? isExpanded
                                    ? '收起子节点'
                                    : '展开子节点'
                                  : undefined
                              }
                              aria-expanded={hasChildren ? isExpanded : undefined}
                              disabled={!hasChildren}
                            >
                              {hasChildren ? (isExpanded ? '▾' : '▸') : '–'}
                            </button>
                            <div className="tree-data-table__tree-content">
                              {column.render(row, context)}
                            </div>
                          </div>
                        ) : (
                          <Fragment>{column.render(row, context)}</Fragment>
                        )}
                      </td>
                    )
                  })}
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  )
}

function flattenTreeRows<T extends TreeTableRow>(
  rows: T[],
  expandedKeySet: Set<string>,
  depth = 0,
): FlatTreeTableRow<T>[] {
  return rows.flatMap((row) => {
    const currentRow: FlatTreeTableRow<T> = { row, depth }

    if (!row.children?.length || !expandedKeySet.has(row.id)) {
      return [currentRow]
    }

    return [
      currentRow,
      ...flattenTreeRows(row.children as T[], expandedKeySet, depth + 1),
    ]
  })
}

function buildCellClassName({
  align,
  sticky,
  isHeader,
  isTree,
}: {
  align?: ColumnAlign
  sticky?: 'right'
  isHeader?: boolean
  isTree?: boolean
}) {
  const classNames = ['tree-data-table__cell']

  if (isHeader) {
    classNames.push('is-header')
  }

  if (sticky === 'right') {
    classNames.push('is-sticky-right')
  }

  if (align) {
    classNames.push(`is-${align}`)
  }

  if (isTree) {
    classNames.push('is-tree')
  }

  return classNames.join(' ')
}

function buildCellStyle<T extends TreeTableRow>(
  column: TreeTableColumn<T>,
  stickyRightOffsets: Map<string, number>,
) {
  const style: CSSProperties = {}

  if (column.width) {
    style.width = `${column.width}px`
  }

  if (column.minWidth) {
    style.minWidth = `${column.minWidth}px`
  }

  if (column.sticky === 'right') {
    style.right = `${stickyRightOffsets.get(column.key) ?? 0}px`
  }

  return style
}

export default TreeDataTable
