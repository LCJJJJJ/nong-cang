import { useMemo, useState } from 'react'

import TreeDataTable, {
  type TreeTableColumn,
  type TreeTableRow,
} from '../../components/tree-data-table/TreeDataTable'
import './HomePage.css'

interface CategoryTreeRow extends TreeTableRow {
  name: string
  code: string
  level: string
  storage: string
  shelfLife: string
  status: '启用' | '停用'
  sort: number
  createdAt: string
  children?: CategoryTreeRow[]
}

const categoryRows: CategoryTreeRow[] = [
  {
    id: 'CAT-A00',
    name: '新鲜蔬菜',
    code: 'CAT-A00',
    level: 'L1',
    storage: '-',
    shelfLife: '-',
    status: '启用',
    sort: 10,
    createdAt: '2023-10-01 08:30',
    children: [
      {
        id: 'CAT-A01',
        name: '叶菜类',
        code: 'CAT-A01',
        level: 'L2',
        storage: '冷藏 (2-8°C)',
        shelfLife: '5 天',
        status: '启用',
        sort: 1,
        createdAt: '2023-10-01 09:15',
        children: [
          {
            id: 'CAT-A01-01',
            name: '菠菜',
            code: 'CAT-A01-01',
            level: 'L3',
            storage: '冷藏 (2-8°C)',
            shelfLife: '4 天',
            status: '启用',
            sort: 1,
            createdAt: '2023-10-01 09:20',
          },
          {
            id: 'CAT-A01-02',
            name: '生菜',
            code: 'CAT-A01-02',
            level: 'L3',
            storage: '冷藏 (2-8°C)',
            shelfLife: '3 天',
            status: '启用',
            sort: 2,
            createdAt: '2023-10-01 09:28',
          },
        ],
      },
      {
        id: 'CAT-A02',
        name: '根茎类',
        code: 'CAT-A02',
        level: 'L2',
        storage: '阴凉干燥 (10-15°C)',
        shelfLife: '15 天',
        status: '停用',
        sort: 2,
        createdAt: '2023-10-01 10:05',
      },
    ],
  },
  {
    id: 'CAT-B00',
    name: '时令水果',
    code: 'CAT-B00',
    level: 'L1',
    storage: '-',
    shelfLife: '-',
    status: '启用',
    sort: 20,
    createdAt: '2023-10-02 14:20',
    children: [
      {
        id: 'CAT-B01',
        name: '柑橘类',
        code: 'CAT-B01',
        level: 'L2',
        storage: '冷藏 (4-8°C)',
        shelfLife: '12 天',
        status: '启用',
        sort: 1,
        createdAt: '2023-10-02 15:00',
      },
    ],
  },
]

function HomePage() {
  const expandableRowIds = useMemo(
    () => collectExpandableRowIds(categoryRows),
    [],
  )
  const [expandedKeys, setExpandedKeys] = useState<string[]>(() => [
    'CAT-A00',
    'CAT-B00',
  ])

  const columns = useMemo<TreeTableColumn<CategoryTreeRow>[]>(
    () => [
      {
        key: 'name',
        title: '分类名称',
        tree: true,
        minWidth: 320,
        render: (row, context) => (
          <div className="category-page__name">
            <span
              className={`category-page__name-icon${
                context.depth === 0 ? ' is-root' : ''
              }`}
            />
            <span>{row.name}</span>
          </div>
        ),
      },
      {
        key: 'code',
        title: '分类编号',
        minWidth: 140,
        render: (row) => row.code,
      },
      {
        key: 'level',
        title: '层级',
        align: 'center',
        minWidth: 90,
        render: (row) => (
          <span className="category-page__level">{row.level}</span>
        ),
      },
      {
        key: 'storage',
        title: '默认储存类型',
        minWidth: 200,
        render: (row) => row.storage,
      },
      {
        key: 'shelfLife',
        title: '保质期基准',
        minWidth: 150,
        render: (row) => row.shelfLife,
      },
      {
        key: 'status',
        title: '状态',
        minWidth: 120,
        render: (row) => (
          <span
            className={`category-page__status${
              row.status === '启用' ? ' is-enabled' : ' is-disabled'
            }`}
          >
            {row.status}
          </span>
        ),
      },
      {
        key: 'sort',
        title: '排序',
        minWidth: 90,
        align: 'center',
        render: (row) => row.sort,
      },
      {
        key: 'createdAt',
        title: '创建时间',
        minWidth: 170,
        render: (row) => row.createdAt,
      },
      {
        key: 'actions',
        title: '操作',
        minWidth: 220,
        width: 220,
        sticky: 'right',
        align: 'right',
        render: (row, context) => (
          <div className="category-page__row-actions">
            <button type="button">编辑</button>
            <button type="button">
              {context.hasChildren && context.depth === 0
                ? '新增子类'
                : row.status === '启用'
                  ? '停用'
                  : '启用'}
            </button>
          </div>
        ),
      },
    ],
    [],
  )

  const handleExpandAll = () => {
    setExpandedKeys(expandableRowIds)
  }

  const handleCollapseAll = () => {
    setExpandedKeys([])
  }

  return (
    <div className="category-page">
      <section className="category-page__search">
        <div className="category-page__search-grid">
          <label className="category-page__field">
            <span>分类编号</span>
            <input type="text" placeholder="输入编号..." />
          </label>

          <label className="category-page__field">
            <span>分类名称</span>
            <input type="text" placeholder="输入名称..." />
          </label>

          <label className="category-page__field">
            <span>上级分类</span>
            <select defaultValue="">
              <option value="">全部分类</option>
              <option value="1">新鲜蔬菜</option>
              <option value="2">时令水果</option>
            </select>
          </label>

          <div className="category-page__actions">
            <button type="button" className="is-ghost">
              重置
            </button>
            <button type="button" className="is-dark">
              查询
            </button>
          </div>
        </div>
      </section>

      <section className="category-page__toolbar">
        <div className="category-page__toolbar-left">
          <button type="button" onClick={handleExpandAll}>
            展开全部
          </button>
          <button type="button" onClick={handleCollapseAll}>
            收起全部
          </button>
        </div>

        <button type="button" className="category-page__primary">
          新增分类
        </button>
      </section>

      <section className="category-page__table-shell">
        <TreeDataTable
          data={categoryRows}
          columns={columns}
          expandedKeys={expandedKeys}
          onExpandedKeysChange={setExpandedKeys}
        />
      </section>
    </div>
  )
}

function collectExpandableRowIds(rows: CategoryTreeRow[]): string[] {
  return rows.flatMap((row) => {
    if (!row.children?.length) {
      return []
    }

    return [row.id, ...collectExpandableRowIds(row.children)]
  })
}

export default HomePage
