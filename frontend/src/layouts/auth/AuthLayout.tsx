import { useEffect, useRef } from 'react'
import { Outlet } from 'react-router-dom'

import './AuthLayout.css'

function AuthLayout() {
  const backgroundRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    const current = backgroundRef.current

    if (!current) {
      return
    }

    const handleMouseMove = (event: MouseEvent) => {
      const x = event.clientX / window.innerWidth
      const y = event.clientY / window.innerHeight
      const moveX = (x - 0.5) * 30
      const moveY = (y - 0.5) * 30

      current.style.transform = `translate(${moveX}px, ${moveY}px)`
    }

    window.addEventListener('mousemove', handleMouseMove)

    return () => {
      window.removeEventListener('mousemove', handleMouseMove)
    }
  }, [])

  return (
    <div className="auth-layout">
      <div className="auth-layout__background" ref={backgroundRef}>
        <div className="auth-layout__shape auth-layout__shape--one" />
        <div className="auth-layout__shape auth-layout__shape--two" />
        <div className="auth-layout__grid" />
        <div className="auth-layout__line auth-layout__line--one" />
        <div className="auth-layout__line auth-layout__line--two" />
      </div>

      <div className="auth-layout__frame">
        <aside className="auth-layout__brand">
          <div className="auth-layout__brand-ornament" />
          <div className="auth-layout__brand-copy">
            <span className="auth-layout__brand-icon" aria-hidden="true">
              <svg viewBox="0 0 24 24" focusable="false">
                <path d="M12 3C9 5.2 7.5 8 7.5 11.4c0 4.3 3 7.7 7.2 9.1.5.1.8-.3.8-.8v-4.1c0-2.9 1.7-5.7 4.6-7.2-1.2-2.4-3.3-4.2-6.1-5.4ZM6 13.4c-1.3.6-2.3 1.8-2.3 3.4 0 2.2 1.8 4 4 4 1.5 0 2.9-.9 3.5-2.2A10.5 10.5 0 0 1 6 13.4Z" />
              </svg>
            </span>
            <h1>
              农产品仓库
              <br />
              管理系统
            </h1>
            <p>专业的农业物流数字化解决方案</p>
          </div>

          <div className="auth-layout__brand-meta">
            <div className="auth-layout__meta-stack">
              <p>智能物流</p>
              <p>数据驱动决策</p>
            </div>

            <div className="auth-layout__meta-footer">
              <div className="auth-layout__meta-divider" />
              <p>仓库管理控制协议</p>
              <div className="auth-layout__meta-row">
                <span>V2.4.0</span>
                <span>系统状态: 在线</span>
              </div>
            </div>
          </div>
        </aside>

        <section className="auth-layout__panel">
          <Outlet />
        </section>
      </div>
    </div>
  )
}

export default AuthLayout
