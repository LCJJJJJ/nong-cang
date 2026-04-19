import './RoleOverviewPage.css'

const roleCards = [
  {
    roleName: '管理员',
    description: '负责系统全局配置与全部业务操作，可访问全部菜单。',
    warehouseRule: '不限制仓库，可为空表示全仓权限',
    modules: ['用户与权限管理', '农产品基础信息管理', '仓库管理', '入库管理', '出库管理', '库存管理', '质量检测与损耗管理', '预警与消息提醒', '统计分析与报表'],
  },
  {
    roleName: '仓库管理员',
    description: '负责仓库空间、入库、出库和现场仓储作业。',
    warehouseRule: '必须指定一个仓库',
    modules: ['仓库管理', '入库管理', '出库管理', '库存查询与盘点', '仓储类预警', '基础信息只读'],
  },
  {
    roleName: '库存管理员',
    description: '负责库存准确性、库存调整、盘点与库存治理。',
    warehouseRule: '必须指定一个仓库',
    modules: ['库存管理', '库存类预警', '库存报表', '基础信息只读'],
  },
  {
    roleName: '质检管理员',
    description: '负责质检、异常库存和损耗处理。',
    warehouseRule: '必须指定一个仓库',
    modules: ['质量检测与损耗管理', '质检类预警', '基础信息只读', '仓库信息只读'],
  },
]

function RoleOverviewPage() {
  return (
    <section className="role-overview-page">
      <div className="role-overview-page__intro">
        <h3>角色说明</h3>
        <p>当前系统固定 4 类核心角色，管理员在分配用户时可参考以下职责与可访问模块。</p>
      </div>

      <div className="role-overview-page__grid">
        {roleCards.map((role) => (
          <article key={role.roleName} className="role-overview-page__card">
            <header>
              <h4>{role.roleName}</h4>
              <p>{role.description}</p>
            </header>
            <div className="role-overview-page__section">
              <strong>仓库规则</strong>
              <span>{role.warehouseRule}</span>
            </div>
            <div className="role-overview-page__section">
              <strong>可访问模块</strong>
              <ul>
                {role.modules.map((module) => (
                  <li key={module}>{module}</li>
                ))}
              </ul>
            </div>
          </article>
        ))}
      </div>
    </section>
  )
}

export default RoleOverviewPage
