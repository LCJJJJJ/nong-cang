import './HomePage.css'

const categoryRows = [
  {
    name: '新鲜蔬菜',
    code: 'CAT-A00',
    level: 'L1',
    storage: '-',
    shelfLife: '-',
    status: '启用',
    sort: 10,
    createdAt: '2023-10-01 08:30',
    indent: 0,
    expandable: true,
    expanded: true,
  },
  {
    name: '叶菜类',
    code: 'CAT-A01',
    level: 'L2',
    storage: '冷藏 (2-8°C)',
    shelfLife: '5 天',
    status: '启用',
    sort: 1,
    createdAt: '2023-10-01 09:15',
    indent: 1,
    expandable: true,
    expanded: false,
  },
  {
    name: '根茎类',
    code: 'CAT-A02',
    level: 'L2',
    storage: '阴凉干燥 (10-15°C)',
    shelfLife: '15 天',
    status: '停用',
    sort: 2,
    createdAt: '2023-10-01 10:05',
    indent: 1,
    expandable: false,
    expanded: false,
  },
  {
    name: '时令水果',
    code: 'CAT-B00',
    level: 'L1',
    storage: '-',
    shelfLife: '-',
    status: '启用',
    sort: 20,
    createdAt: '2023-10-02 14:20',
    indent: 0,
    expandable: false,
    expanded: false,
  },
] as const

function HomePage() {
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
          <button type="button">展开全部</button>
          <button type="button">收起全部</button>
        </div>

        <button type="button" className="category-page__primary">
          新增分类
        </button>
      </section>

      <section className="category-page__table-shell">
        <table className="category-page__table">
          <thead>
            <tr>
              <th>分类名称</th>
              <th>分类编号</th>
              <th>层级</th>
              <th>默认储存类型</th>
              <th>保质期基准</th>
              <th>状态</th>
              <th>排序</th>
              <th>创建时间</th>
              <th className="is-right">操作</th>
            </tr>
          </thead>

          <tbody>
            {categoryRows.map((row) => (
              <tr key={row.code}>
                <td>
                  <div
                    className="category-page__name"
                    style={{ paddingLeft: `${row.indent * 28}px` }}
                  >
                    <span className="category-page__caret" aria-hidden="true">
                      {row.expandable ? (row.expanded ? '▾' : '▸') : '–'}
                    </span>
                    <span
                      className={`category-page__name-icon${
                        row.indent === 0 ? ' is-root' : ''
                      }`}
                    />
                    <span>{row.name}</span>
                  </div>
                </td>
                <td>{row.code}</td>
                <td>
                  <span className="category-page__level">{row.level}</span>
                </td>
                <td>{row.storage}</td>
                <td>{row.shelfLife}</td>
                <td>
                  <span
                    className={`category-page__status${
                      row.status === '启用' ? ' is-enabled' : ' is-disabled'
                    }`}
                  >
                    {row.status}
                  </span>
                </td>
                <td>{row.sort}</td>
                <td>{row.createdAt}</td>
                <td className="is-right">
                  <div className="category-page__row-actions">
                    <button type="button">编辑</button>
                    <button type="button">
                      {row.indent === 0 ? '新增子类' : row.status === '启用' ? '停用' : '启用'}
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
    </div>
  )
}

export default HomePage
