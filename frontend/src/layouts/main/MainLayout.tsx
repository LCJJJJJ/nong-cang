import { useMemo, useState } from 'react'
import { Outlet, useLocation, useNavigate } from 'react-router-dom'

import AssistantWidget from '../../features/assistant/AssistantWidget'
import { isPathAllowed } from '../../features/auth/role-access'
import { useAuthSession } from '../../features/auth/useAuthSession'
import './MainLayout.css'

type SidebarIconName =
  | 'user'
  | 'leaf'
  | 'warehouse'
  | 'group'
  | 'inbound'
  | 'outbound'
  | 'inventory'
  | 'shield'
  | 'alert'
  | 'chart'

interface NavigationChildItem {
  label: string
  path?: string
  active?: boolean
  allowedRoles?: string[]
}

interface NavigationSection {
  label: string
  icon: SidebarIconName
  expanded?: boolean
  children?: NavigationChildItem[]
}

const navigationSections: NavigationSection[] = [
  {
    label: '用户与权限管理',
    icon: 'user',
    expanded: true,
    children: [
      { label: '用户管理', path: '/system-users', allowedRoles: ['ADMIN'] },
      { label: '角色说明', path: '/role-overview', allowedRoles: ['ADMIN'] },
    ],
  },
  {
    label: '农产品基础信息管理',
    icon: 'leaf',
    expanded: true,
    children: [
      { label: '产品分类管理', path: '/' },
      { label: '产品档案管理', path: '/product-archives' },
      { label: '产品单位管理', path: '/product-units' },
      { label: '产地信息管理', path: '/product-origins' },
      { label: '储存条件管理', path: '/storage-conditions' },
      { label: '品质等级管理', path: '/quality-grades' },
    ],
  },
  {
    label: '仓库管理',
    icon: 'warehouse',
    expanded: true,
    children: [
      { label: '仓库信息管理', path: '/warehouses' },
      { label: '库区管理', path: '/warehouse-zones' },
      { label: '库位管理', path: '/warehouse-locations' },
    ],
  },
  {
    label: '供应商与客户管理',
    icon: 'group',
    expanded: true,
    children: [
      { label: '供应商管理', path: '/suppliers' },
      { label: '客户管理', path: '/customers' },
    ],
  },
  {
    label: '入库管理',
    icon: 'inbound',
    expanded: true,
    children: [
      { label: '入库单管理', path: '/inbound-orders' },
      { label: '上架任务管理', path: '/putaway-tasks' },
      { label: '入库记录查询', path: '/inbound-records' },
    ],
  },
  {
    label: '出库管理',
    icon: 'outbound',
    expanded: true,
    children: [
      { label: '出库单管理', path: '/outbound-orders' },
      { label: '拣货出库任务管理', path: '/outbound-tasks' },
      { label: '出库记录查询', path: '/outbound-records' },
    ],
  },
  {
    label: '库存管理',
    icon: 'inventory',
    expanded: true,
    children: [
      { label: '实时库存查询', path: '/inventory-stocks' },
      { label: '库存流水', path: '/inventory-transactions' },
      { label: '库存调整管理', path: '/inventory-adjustments' },
      { label: '库存盘点管理', path: '/inventory-stocktakings' },
    ],
  },
  {
    label: '质量检测与损耗管理',
    icon: 'shield',
    expanded: true,
    children: [
      { label: '质检单管理', path: '/quality-inspections' },
      { label: '异常库存管理', path: '/abnormal-stocks' },
      { label: '损耗登记管理', path: '/loss-records' },
    ],
  },
  {
    label: '预警与消息提醒',
    icon: 'alert',
    expanded: true,
    children: [
      { label: '预警规则管理', path: '/alert-rules' },
      { label: '预警中心', path: '/alerts' },
      { label: '消息中心', path: '/message-notices' },
    ],
  },
  {
    label: '统计分析与报表',
    icon: 'chart',
  },
]

const pageMetaMap: Record<string, { title: string; description: string }> = {
  '/': {
    title: '产品分类管理',
    description:
      '管理农产品的主分类与子分类体系，设置默认储存环境标准以确保数据一致性。',
  },
  '/product-archives': {
    title: '产品档案管理',
    description:
      '维护农产品主数据档案，统一挂接分类、单位、产地、储存条件和品质等级，并直接维护保质期信息。',
  },
  '/system-users': {
    title: '用户管理',
    description:
      '维护系统用户、分配固定角色并绑定负责仓库，当前采用一人一角色一仓库的权限模型。',
  },
  '/role-overview': {
    title: '角色说明',
    description:
      '查看当前 4 类核心角色的职责边界、仓库规则和可访问模块，便于管理员分配用户。',
  },
  '/storage-conditions': {
    title: '储存条件管理',
    description:
      '维护仓内存放环境与作业规则，为产品分类、产品档案和预警模块提供统一标准。',
  },
  '/product-units': {
    title: '产品单位管理',
    description:
      '维护农产品通用计量单位，统一产品档案和后续出入库、库存业务的单位口径。',
  },
  '/product-origins': {
    title: '产地信息管理',
    description:
      '维护标准产地信息，为产品档案、质量追溯和统计分析提供统一产地口径。',
  },
  '/quality-grades': {
    title: '品质等级管理',
    description:
      '维护统一品质等级口径，为产品档案、质检与损耗分析提供标准等级规则。',
  },
  '/warehouses': {
    title: '仓库信息管理',
    description:
      '维护仓库主数据，为库区、库位以及后续入库、库存和出库业务提供统一仓库维度。',
  },
  '/warehouse-zones': {
    title: '库区管理',
    description:
      '维护仓库内部的功能分区，为库位管理、入库上架和库存作业提供统一库区维度。',
  },
  '/warehouse-locations': {
    title: '库位管理',
    description:
      '维护仓库内部实际可存放货物的库位信息，为上架、拣选和库存定位提供细粒度空间维度。',
  },
  '/suppliers': {
    title: '供应商管理',
    description:
      '维护供应商主数据，为后续入库业务、到货协同和供应来源分析提供统一对象口径。',
  },
  '/customers': {
    title: '客户管理',
    description:
      '维护客户主数据，为后续出库业务、配送协同和客户去向分析提供统一对象口径。',
  },
  '/inbound-orders': {
    title: '入库单管理',
    description:
      '维护到货前的入库业务单据，关联供应商、仓库和产品明细，为后续上架任务生成提供业务入口。',
  },
  '/putaway-tasks': {
    title: '上架任务管理',
    description:
      '承接已到货的入库单明细，分配库区库位并完成上架，是库存形成的直接入口。',
  },
  '/inbound-records': {
    title: '入库记录查询',
    description:
      '查询已完成上架的入库结果，统一追踪供应商、仓库、库位、产品和实际入库时间。',
  },
  '/outbound-orders': {
    title: '出库单管理',
    description:
      '维护待出库业务单据，统一关联客户、仓库和产品明细，为后续拣货出库任务提供业务入口。',
  },
  '/outbound-tasks': {
    title: '拣货出库任务管理',
    description:
      '承接出库单明细，完成库存分配、拣货确认和最终出库扣减，作为库存减少的直接入口。',
  },
  '/outbound-records': {
    title: '出库记录查询',
    description:
      '查询已完成出库的最终结果，统一追踪客户、仓库、库位、产品和实际出库时间。',
  },
  '/inventory-stocks': {
    title: '实时库存查询',
    description:
      '查看产品在仓库、库区、库位维度上的现存、预留和可用数量，为出库分配与盘点提供统一库存视图。',
  },
  '/inventory-transactions': {
    title: '库存流水',
    description:
      '统一查询库存增减记录，覆盖入库、出库、调整和盘点等库存变化来源。',
  },
  '/inventory-adjustments': {
    title: '库存调整管理',
    description:
      '处理系统初始化修正、人工纠错等非业务单据导致的库存变更，并沉淀标准调整记录和流水。',
  },
  '/inventory-stocktakings': {
    title: '库存盘点管理',
    description:
      '创建盘点范围、录入实盘数量并确认差异回写库存，是库存治理闭环的最终环节。',
  },
  '/quality-inspections': {
    title: '质检单管理',
    description:
      '登记入库质检和在库抽检结果，不合格数量会自动进入异常锁定库存。',
  },
  '/abnormal-stocks': {
    title: '异常库存管理',
    description:
      '查看由质检单锁定的不合格库存，并为后续释放回库或转损耗处理提供来源数据。',
  },
  '/loss-records': {
    title: '损耗登记管理',
    description:
      '统一查看异常转损耗和人工直接登记的损耗记录，损耗入账后会同步减少库存。',
  },
  '/alert-rules': {
    title: '预警规则管理',
    description:
      '维护系统内置预警阈值和严重级别，并可手动刷新预警实例。',
  },
  '/alerts': {
    title: '预警中心',
    description:
      '聚合当前活跃、已忽略和已恢复的预警记录，并支持手动刷新与忽略处理。',
  },
  '/message-notices': {
    title: '消息中心',
    description:
      '查看由预警实例生成的系统消息，并支持单条已读和全部已读。',
  },
}

function MainLayout() {
  const navigate = useNavigate()
  const location = useLocation()
  const { setLoggedOut, user } = useAuthSession()
  const [expandedSections, setExpandedSections] = useState<Record<string, boolean>>(
    () =>
      Object.fromEntries(
        navigationSections
          .filter((section) => section.children?.length)
          .map((section) => [section.label, Boolean(section.expanded)]),
      ),
  )

  const pageMeta = pageMetaMap[location.pathname] ?? {
    title: '农产品仓储管理',
    description: '基础主布局已就绪，可继续扩展具体业务页面。',
  }

  const resolvedSections = useMemo(
    () =>
      navigationSections.map((section) => ({
        ...section,
        children: section.children
          ?.filter(
            (child) =>
              !child.path ||
              !child.allowedRoles ||
              child.allowedRoles.includes(user?.roleCode ?? ''),
          )
          .filter((child) => !child.path || isPathAllowed(user?.roleCode, child.path))
          .map((child) => ({
            ...child,
            active: child.path === location.pathname,
          })),
        isExpanded: section.children?.length
          ? Boolean(expandedSections[section.label]) ||
            Boolean(section.children?.some((child) => child.path === location.pathname))
          : false,
      })),
    [expandedSections, location.pathname, user?.roleCode],
  )

  const mobileQuickLinks = useMemo(
    () =>
      resolvedSections
        .flatMap((section) => section.children ?? [])
        .filter((child) => child.path)
        .slice(0, 5),
    [resolvedSections],
  )

  const handleLogout = () => {
    setLoggedOut()
    navigate('/login', { replace: true })
  }

  const handleToggleSection = (label: string) => {
    setExpandedSections((currentState) => ({
      ...currentState,
      [label]: !currentState[label],
    }))
  }

  const displayName = resolveDisplayName(user?.displayName)
  const loginName = resolveLoginName(user?.username)

  return (
    <div className="main-layout">
      <aside className="main-layout__sidebar">
        <div className="main-layout__brand">
          <div className="main-layout__brand-mark" aria-hidden="true">
            <svg viewBox="0 0 24 24" focusable="false">
              <path d="M3 10.5 12 4l9 6.5V20a1 1 0 0 1-1 1h-4v-6H8v6H4a1 1 0 0 1-1-1v-9.5Z" />
            </svg>
          </div>
          <div>
            <h1>Alpha仓储</h1>
            <p>产品层级体系</p>
          </div>
        </div>

        <nav className="main-layout__menu" aria-label="主导航">
          {resolvedSections.map((section) => (
            section.children && section.children.length === 0 ? null : (
            <div key={section.label} className="main-layout__menu-group">
              <button
                type="button"
                className={`main-layout__menu-item${
                  section.isExpanded ? ' is-expanded' : ''
                }${section.children?.length ? ' is-collapsible' : ''}`}
                onClick={() =>
                  section.children?.length
                    ? handleToggleSection(section.label)
                    : undefined
                }
                aria-expanded={
                  section.children?.length ? section.isExpanded : undefined
                }
                aria-controls={
                  section.children?.length
                    ? `submenu-${section.label}`
                    : undefined
                }
              >
                <span className="main-layout__menu-item-left">
                  <SidebarIcon name={section.icon} />
                  <span>{section.label}</span>
                </span>
                {section.children?.length ? (
                  <span className="main-layout__menu-arrow" aria-hidden="true">
                    {section.isExpanded ? '−' : '+'}
                  </span>
                ) : null}
              </button>

              {section.children ? (
                <div
                  id={`submenu-${section.label}`}
                  className={`main-layout__submenu${
                    section.isExpanded ? ' is-expanded' : ''
                  }`}
                >
                  {section.children.map((child) => (
                    <button
                      key={child.label}
                      type="button"
                      className={`main-layout__submenu-item${
                        child.active ? ' is-active' : ''
                      }`}
                      onClick={() => {
                        if (child.path) {
                          navigate(child.path)
                        }
                      }}
                    >
                      {child.label}
                    </button>
                  ))}
                </div>
              ) : null}
            </div>
            )
          ))}
        </nav>
      </aside>

      <div className="main-layout__workspace">
        <header className="main-layout__topbar">
          <div className="main-layout__topbar-main">
            <div>
              <h2>{pageMeta.title}</h2>
              <p>{pageMeta.description}</p>
            </div>
          </div>

          <div className="main-layout__topbar-actions">
            <button
              type="button"
              className="main-layout__icon-button"
              aria-label="通知"
            >
              <TopbarIcon name="bell" />
            </button>
            <button
              type="button"
              className="main-layout__icon-button"
              aria-label="设置"
            >
              <TopbarIcon name="settings" />
            </button>

            <div className="main-layout__user">
              <div className="main-layout__avatar" aria-hidden="true">
                <TopbarIcon name="avatar" />
              </div>
              <div className="main-layout__user-copy">
                <strong>{displayName}</strong>
                <span>{loginName}</span>
              </div>
            </div>

            <button
              type="button"
              className="main-layout__logout"
              onClick={handleLogout}
            >
              退出登录
            </button>
          </div>
        </header>

        <div className="main-layout__mobile-tabs" aria-label="移动端菜单">
          {mobileQuickLinks.map((item) => (
            <button
              key={item.path}
              type="button"
              className={location.pathname === item.path ? 'is-active' : ''}
              onClick={() => {
                if (item.path) {
                  navigate(item.path)
                }
              }}
            >
              {item.label}
            </button>
          ))}
        </div>

        <main className="main-layout__canvas">
          <Outlet />
        </main>
      </div>

      <AssistantWidget routePath={location.pathname} routeTitle={pageMeta.title} />
    </div>
  )
}

function SidebarIcon({ name }: { name: SidebarIconName }) {
  const paths: Record<SidebarIconName, string> = {
    user: 'M12 12a4 4 0 1 0-4-4 4 4 0 0 0 4 4Zm0 2c-4.42 0-8 2.24-8 5v1h16v-1c0-2.76-3.58-5-8-5Z',
    leaf: 'M18.94 4.94c-4.3-.3-7.41.87-9.5 2.96-2.9 2.9-2.74 6.98-2.34 8.93l-2.8 2.8 1.41 1.41 2.81-2.81c1.95.39 6.02.56 8.92-2.34 2.1-2.09 3.27-5.2 2.97-9.5l-.05-.69-.69-.05Z',
    warehouse:
      'M3 10.5 12 4l9 6.5V20a1 1 0 0 1-1 1h-4v-6H8v6H4a1 1 0 0 1-1-1v-9.5Z',
    group:
      'M16 11c1.66 0 3-1.57 3-3.5S17.66 4 16 4s-3 1.57-3 3.5 1.34 3.5 3 3.5Zm-8 0c1.66 0 3-1.57 3-3.5S9.66 4 8 4 5 5.57 5 7.5 6.34 11 8 11Zm0 2c-2.67 0-8 1.34-8 4v2h10v-2c0-1.07.38-2.03 1.03-2.84C10.13 13.44 8.95 13 8 13Zm8 0c-.95 0-2.13.44-3.03 1.16A4.45 4.45 0 0 1 14 17v2h10v-2c0-2.66-5.33-4-8-4Z',
    inbound: 'M11 4v8H8l4 4 4-4h-3V4h-2Zm-6 14h14v2H5v-2Z',
    outbound: 'M13 20v-8h3l-4-4-4 4h3v8h2Zm-8 0h14v-2H5v2Z',
    inventory:
      'M4 6h16v4H4V6Zm1 6h14v8H5v-8Zm2 2v4h4v-4H7Zm6 0v4h4v-4h-4Z',
    shield:
      'M12 2 5 5v6c0 4.55 3.05 8.8 7 10 3.95-1.2 7-5.45 7-10V5l-7-3Zm0 5a3 3 0 1 1 0 6 3 3 0 0 1 0-6Z',
    alert:
      'M12 4 2 20h20L12 4Zm1 11h-2v-4h2v4Zm0 4h-2v-2h2v2Z',
    chart:
      'M5 19h14v2H3V5h2v14Zm2-4h2v4H7v-4Zm4-6h2v10h-2V9Zm4 3h2v7h-2v-7Z',
  }

  return (
    <span className="main-layout__icon" aria-hidden="true">
      <svg viewBox="0 0 24 24" focusable="false">
        <path d={paths[name]} />
      </svg>
    </span>
  )
}

function TopbarIcon({ name }: { name: 'bell' | 'settings' | 'avatar' }) {
  const paths: Record<'bell' | 'settings' | 'avatar', string> = {
    bell:
      'M12 22a2.5 2.5 0 0 0 2.45-2h-4.9A2.5 2.5 0 0 0 12 22Zm6-6v-5a6 6 0 1 0-12 0v5l-2 2v1h16v-1l-2-2Z',
    settings:
      'm19.14 12.94.04-.94-.04-.94 2.03-1.58-1.92-3.32-2.39.96a7.54 7.54 0 0 0-1.63-.94L14.96 2h-3.92l-.27 2.18c-.58.22-1.12.53-1.63.94l-2.39-.96L4.83 7.48l2.03 1.58-.04.94.04.94-2.03 1.58 1.92 3.32 2.39-.96c.5.41 1.05.72 1.63.94L11.04 22h3.92l.27-2.18c.58-.22 1.12-.53 1.63-.94l2.39.96 1.92-3.32-2.03-1.58ZM13 15h-2a3 3 0 1 1 2-6 3 3 0 0 1 0 6Z',
    avatar:
      'M12 12a4 4 0 1 0-4-4 4 4 0 0 0 4 4Zm0 2c-4.42 0-8 2.24-8 5v1h16v-1c0-2.76-3.58-5-8-5Z',
  }

  return (
    <span className="main-layout__icon" aria-hidden="true">
      <svg viewBox="0 0 24 24" focusable="false">
        <path d={paths[name]} />
      </svg>
    </span>
  )
}

function resolveDisplayName(name?: string | null) {
  if (!name || isBrokenText(name)) {
    return '系统管理员'
  }

  return name
}

function resolveLoginName(username?: string | null) {
  if (!username || isBrokenText(username)) {
    return 'admin'
  }

  return username
}

function isBrokenText(value: string) {
  return /�|Ã|Â|ð|ï|æ|å/.test(value)
}

export default MainLayout
