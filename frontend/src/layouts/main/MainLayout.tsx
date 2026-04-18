import { useMemo, useState } from 'react'
import { Outlet, useLocation, useNavigate } from 'react-router-dom'

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
  active?: boolean
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
  },
  {
    label: '农产品基础信息管理',
    icon: 'leaf',
    expanded: true,
    children: [
      { label: '产品分类管理', active: true },
      { label: '产品档案管理' },
      { label: '产品单位管理' },
      { label: '产地信息管理' },
      { label: '储存条件管理' },
      { label: '保质期规则管理' },
      { label: '品质等级管理' },
    ],
  },
  {
    label: '仓库管理',
    icon: 'warehouse',
  },
  {
    label: '供应商与客户管理',
    icon: 'group',
  },
  {
    label: '入库管理',
    icon: 'inbound',
  },
  {
    label: '出库管理',
    icon: 'outbound',
  },
  {
    label: '库存管理',
    icon: 'inventory',
  },
  {
    label: '质量检测与损耗管理',
    icon: 'shield',
  },
  {
    label: '预警与消息提醒',
    icon: 'alert',
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
        isExpanded: section.children?.length
          ? Boolean(expandedSections[section.label])
          : false,
      })),
    [expandedSections],
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

  return (
    <div className="main-layout">
      <aside className="main-layout__sidebar">
        <div className="main-layout__brand">
          <div className="main-layout__brand-mark">
            <span aria-hidden="true">▣</span>
          </div>
          <div>
            <h1>Alpha仓储</h1>
            <p>产品层级体系</p>
          </div>
        </div>

        <nav className="main-layout__menu" aria-label="主导航">
          {resolvedSections.map((section) => (
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
                    >
                      {child.label}
                    </button>
                  ))}
                </div>
              ) : null}
            </div>
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
            <button type="button" className="main-layout__icon-button" aria-label="通知">
              <SidebarIcon name="bell" />
            </button>
            <button type="button" className="main-layout__icon-button" aria-label="设置">
              <SidebarIcon name="settings" />
            </button>

            <div className="main-layout__user">
              <div className="main-layout__avatar" aria-hidden="true">
                {user?.displayName.slice(0, 1) ?? 'A'}
              </div>
              <div className="main-layout__user-copy">
                <strong>{user?.displayName ?? '管理员'}</strong>
                <span>{user?.username ?? 'admin'}</span>
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
          <button type="button" className="is-active">
            产品分类管理
          </button>
          <button type="button">产品档案管理</button>
          <button type="button">仓库管理</button>
          <button type="button">库存管理</button>
        </div>

        <main className="main-layout__canvas">
          <Outlet />
        </main>
      </div>
    </div>
  )
}

function SidebarIcon({ name }: { name: SidebarIconName | 'bell' | 'settings' }) {
  const paths: Record<string, string> = {
    user: 'M12 12a4 4 0 1 0-4-4 4 4 0 0 0 4 4Zm0 2c-4.42 0-8 2.24-8 5v1h16v-1c0-2.76-3.58-5-8-5Z',
    leaf: 'M12 3c-3 2.2-4.5 5-4.5 8.4 0 4.3 3 7.7 7.2 9.1.5.1.8-.3.8-.8v-4.1c0-2.9 1.7-5.7 4.6-7.2-1.2-2.4-3.3-4.2-6.1-5.4Z',
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
    bell:
      'M12 22a2.5 2.5 0 0 0 2.45-2h-4.9A2.5 2.5 0 0 0 12 22Zm6-6v-5a6 6 0 1 0-12 0v5l-2 2v1h16v-1l-2-2Z',
    settings:
      'm19.14 12.94.04-.94-.04-.94 2.03-1.58-1.92-3.32-2.39.96a7.54 7.54 0 0 0-1.63-.94L14.96 2h-3.92l-.27 2.18c-.58.22-1.12.53-1.63.94l-2.39-.96L4.83 7.48l2.03 1.58-.04.94.04.94-2.03 1.58 1.92 3.32 2.39-.96c.5.41 1.05.72 1.63.94L11.04 22h3.92l.27-2.18c.58-.22 1.12-.53 1.63-.94l2.39.96 1.92-3.32-2.03-1.58ZM13 15h-2a3 3 0 1 1 2-6 3 3 0 0 1 0 6Z',
  }

  return (
    <span className="main-layout__icon" aria-hidden="true">
      <svg viewBox="0 0 24 24" focusable="false">
        <path d={paths[name]} />
      </svg>
    </span>
  )
}

export default MainLayout
