import { type FormEvent, useEffect, useState } from 'react'

import { type AppError, getFieldError, normalizeError } from '../../api/errors'
import {
  getSystemPing,
  postSystemEcho,
  triggerSystemBusinessError,
} from '../../features/system/api'
import type {
  SystemEchoResponse,
  SystemPingResponse,
} from '../../features/system/types'
import heroImg from '../../assets/images/hero.png'
import reactLogo from '../../assets/images/react.svg'
import viteLogo from '../../assets/images/vite.svg'
import './HomePage.css'

function HomePage() {
  const [pingData, setPingData] = useState<SystemPingResponse | null>(null)
  const [pingError, setPingError] = useState<AppError | null>(null)
  const [isPingLoading, setIsPingLoading] = useState(true)

  const [echoContent, setEchoContent] = useState('前后端统一响应结构')
  const [echoResult, setEchoResult] = useState<SystemEchoResponse | null>(null)
  const [echoError, setEchoError] = useState<AppError | null>(null)
  const [isEchoSubmitting, setIsEchoSubmitting] = useState(false)

  const [businessError, setBusinessError] = useState<AppError | null>(null)
  const [isBusinessLoading, setIsBusinessLoading] = useState(false)

  useEffect(() => {
    let isMounted = true

    const loadPing = async () => {
      setIsPingLoading(true)
      setPingError(null)

      try {
        const data = await getSystemPing()
        if (isMounted) {
          setPingData(data)
        }
      } catch (error) {
        if (isMounted) {
          setPingError(normalizeError(error))
        }
      } finally {
        if (isMounted) {
          setIsPingLoading(false)
        }
      }
    }

    void loadPing()

    return () => {
      isMounted = false
    }
  }, [])

  const handleEchoSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setIsEchoSubmitting(true)
    setEchoError(null)
    setEchoResult(null)

    try {
      const data = await postSystemEcho({ content: echoContent })
      setEchoResult(data)
    } catch (error) {
      setEchoError(normalizeError(error))
    } finally {
      setIsEchoSubmitting(false)
    }
  }

  const handleBusinessErrorClick = async () => {
    setIsBusinessLoading(true)
    setBusinessError(null)

    try {
      await triggerSystemBusinessError()
    } catch (error) {
      setBusinessError(normalizeError(error))
    } finally {
      setIsBusinessLoading(false)
    }
  }

  const echoFieldError = getFieldError(echoError?.fieldErrors, 'content')

  return (
    <main className="home-page">
      <section className="hero-panel">
        <div className="hero-copy">
          <span className="eyebrow">React + Axios + Spring Boot 3</span>
          <h1>统一响应结构、类型约定与错误反馈</h1>
          <p className="hero-text">
            当前首页直接接入后端演示接口，用来验证前后端统一的
            <code>ApiResponse</code>
            、业务异常、参数校验和 <code>traceId</code> 反馈链路。
          </p>
          <div className="hero-tags">
            <span>统一响应体</span>
            <span>字段级校验</span>
            <span>业务异常</span>
            <span>友好提示</span>
          </div>
        </div>
        <div className="hero-visual">
          <div className="hero">
            <img
              src={heroImg}
              className="base"
              width="170"
              height="179"
              alt=""
            />
            <img src={reactLogo} className="framework" alt="React logo" />
            <img src={viteLogo} className="vite" alt="Vite logo" />
          </div>
        </div>
      </section>

      <div className="ticks"></div>

      <section className="demo-grid">
        <article className="demo-card">
          <div className="card-head">
            <span className="card-kicker">GET /api/system/ping</span>
            <h2>系统连通性</h2>
          </div>
          <p className="card-text">
            用于验证统一成功响应结构、<code>traceId</code> 透传和 Axios
            解包逻辑。
          </p>

          {isPingLoading && (
            <div className="feedback feedback-info">正在读取后端状态...</div>
          )}

          {!isPingLoading && pingError && (
            <div className="feedback feedback-error">
              <strong>{pingError.message}</strong>
              {pingError.traceId && (
                <span>traceId: {pingError.traceId}</span>
              )}
            </div>
          )}

          {!isPingLoading && pingData && (
            <dl className="status-list">
              <div>
                <dt>示例 Long ID</dt>
                <dd>{pingData.demoId}</dd>
              </div>
              <div>
                <dt>服务名</dt>
                <dd>{pingData.service}</dd>
              </div>
              <div>
                <dt>状态</dt>
                <dd className="status-pill">{pingData.status}</dd>
              </div>
              <div>
                <dt>服务时间</dt>
                <dd>{pingData.serverTime}</dd>
              </div>
            </dl>
          )}
        </article>

        <article className="demo-card">
          <div className="card-head">
            <span className="card-kicker">POST /api/system/echo</span>
            <h2>入参与字段错误反馈</h2>
          </div>
          <p className="card-text">
            提交请求后由后端执行 <code>@Valid</code> 校验，字段错误会被归一化回显到表单。
          </p>

          <form className="demo-form" onSubmit={handleEchoSubmit}>
            <label className="field">
              <span>content</span>
              <input
                value={echoContent}
                onChange={(event) => setEchoContent(event.target.value)}
                placeholder="请输入最多 64 个字符"
              />
              {echoFieldError && (
                <small className="field-error">{echoFieldError}</small>
              )}
            </label>

            <button className="action-button" disabled={isEchoSubmitting}>
              {isEchoSubmitting ? '提交中...' : '提交 Echo 请求'}
            </button>
          </form>

          {echoError && !echoFieldError && (
            <div className="feedback feedback-error">
              <strong>{echoError.message}</strong>
              {echoError.traceId && <span>traceId: {echoError.traceId}</span>}
            </div>
          )}

          {echoResult && (
            <div className="feedback feedback-success">
              <strong>请求成功</strong>
              <span>内容：{echoResult.content}</span>
              <span>长度：{echoResult.length}</span>
            </div>
          )}
        </article>

        <article className="demo-card">
          <div className="card-head">
            <span className="card-kicker">GET /api/system/business-error</span>
            <h2>业务错误与友好提示</h2>
          </div>
          <p className="card-text">
            后端抛出 <code>BusinessException</code> 后，前端会统一归一化为 <code>AppError</code> 并展示业务友好文案。
          </p>

          <button
            className="action-button action-button-ghost"
            onClick={handleBusinessErrorClick}
            disabled={isBusinessLoading}
          >
            {isBusinessLoading ? '触发中...' : '触发演示业务错误'}
          </button>

          {businessError && (
            <div className="feedback feedback-error">
              <strong>{businessError.message}</strong>
              <span>错误码：{businessError.code}</span>
              {businessError.traceId && (
                <span>traceId: {businessError.traceId}</span>
              )}
            </div>
          )}
        </article>
      </section>

      <div className="ticks"></div>

      <section id="next-steps">
        <div id="docs">
          <svg className="icon" role="presentation" aria-hidden="true">
            <use href="/icons.svg#documentation-icon"></use>
          </svg>
          <h2>文档约定</h2>
          <p>统一规范已经写入前后端 README，可直接作为团队开发约定。</p>
          <ul>
            <li>
              <a href="https://vite.dev/" target="_blank" rel="noreferrer">
                <img className="logo" src={viteLogo} alt="" />
                Explore Vite
              </a>
            </li>
            <li>
              <a href="https://react.dev/" target="_blank" rel="noreferrer">
                <img className="button-icon" src={reactLogo} alt="" />
                Learn more
              </a>
            </li>
          </ul>
        </div>
        <div id="social">
          <svg className="icon" role="presentation" aria-hidden="true">
            <use href="/icons.svg#social-icon"></use>
          </svg>
          <h2>落地方式</h2>
          <p>后续新增业务模块时，直接按 <code>features</code> 与 <code>modules</code> 目录扩展。</p>
          <ul>
            <li>
              <a
                href="https://github.com/vitejs/vite"
                target="_blank"
                rel="noreferrer"
              >
                <svg
                  className="button-icon"
                  role="presentation"
                  aria-hidden="true"
                >
                  <use href="/icons.svg#github-icon"></use>
                </svg>
                GitHub
              </a>
            </li>
            <li>
              <a href="https://chat.vite.dev/" target="_blank" rel="noreferrer">
                <svg
                  className="button-icon"
                  role="presentation"
                  aria-hidden="true"
                >
                  <use href="/icons.svg#discord-icon"></use>
                </svg>
                Discord
              </a>
            </li>
          </ul>
        </div>
      </section>

      <div className="ticks"></div>
      <section id="spacer"></section>
    </main>
  )
}

export default HomePage
