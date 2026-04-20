import { type FormEvent, useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'

import { type AppError, normalizeError } from '../../../api/errors'
import { login } from '../../../features/auth/api'
import { getFirstAllowedPath, resolveRoleCode } from '../../../features/auth/role-access'
import { useAuthSession } from '../../../features/auth/useAuthSession'
import { saveAuthSession } from '../../../features/auth/storage'
import './LoginPage.css'

function LoginPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const { setAuthenticated } = useAuthSession()
  const [showPassword, setShowPassword] = useState(false)
  const [account, setAccount] = useState('')
  const [password, setPassword] = useState('')
  const [rememberMe, setRememberMe] = useState(false)
  const [submitMessage, setSubmitMessage] = useState(
    '测试账号：admin / warehouse_admin / inventory_admin / quality_admin',
  )
  const [submitError, setSubmitError] = useState<AppError | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)

  const redirectTo = (
    location.state as { from?: { pathname?: string } } | undefined
  )?.from?.pathname

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setSubmitError(null)
    setSubmitMessage('')
    setIsSubmitting(true)

    try {
      const session = await login({
        account,
        password,
        rememberMe,
      })

      saveAuthSession(session)
      setAuthenticated(session.user)
      navigate(redirectTo ?? getFirstAllowedPath(resolveRoleCode(session.user)), { replace: true })
    } catch (error) {
      const appError = normalizeError(error)
      setSubmitError(appError)
      setSubmitMessage('')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="login-page">
      <div className="login-page__content">
        <h2>系统登录</h2>

        <form className="login-page__form" onSubmit={handleSubmit}>
          <div className="login-page__field">
            <label htmlFor="username">账号/手机号</label>
            <div className="login-page__input-wrap">
              <span className="login-page__icon" aria-hidden="true">
                <svg viewBox="0 0 24 24" focusable="false">
                  <path d="M12 12a4 4 0 1 0-4-4 4 4 0 0 0 4 4Zm0 2c-4.42 0-8 2.24-8 5v1h16v-1c0-2.76-3.58-5-8-5Z" />
                </svg>
              </span>
              <input
                id="username"
                name="username"
                type="text"
                placeholder="输入管理员账号"
                value={account}
                onChange={(event) => setAccount(event.target.value)}
                autoComplete="username"
                required
              />
            </div>
          </div>

          <div className="login-page__field">
            <label htmlFor="password">密码</label>
            <div className="login-page__input-wrap">
              <span className="login-page__icon" aria-hidden="true">
                <svg viewBox="0 0 24 24" focusable="false">
                  <path d="M17 9h-1V7a4 4 0 0 0-8 0v2H7a2 2 0 0 0-2 2v8a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2v-8a2 2 0 0 0-2-2Zm-6 6.73V17a1 1 0 1 0 2 0v-1.27a2 2 0 1 0-2 0ZM10 9V7a2 2 0 0 1 4 0v2Z" />
                </svg>
              </span>
              <input
                id="password"
                name="password"
                type={showPassword ? 'text' : 'password'}
                placeholder="输入密码"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                autoComplete="current-password"
                required
              />
              <button
                className="login-page__toggle"
                type="button"
                onClick={() => setShowPassword((current) => !current)}
                aria-label={showPassword ? '隐藏密码' : '显示密码'}
              >
                <svg viewBox="0 0 24 24" focusable="false">
                  {showPassword ? (
                    <path d="M12 5c-5 0-9.27 3.11-11 7 1.73 3.89 6 7 11 7s9.27-3.11 11-7c-1.73-3.89-6-7-11-7Zm0 11a4 4 0 1 1 4-4 4 4 0 0 1-4 4Z" />
                  ) : (
                    <path d="m2.81 2.81-1.42 1.42 3.09 3.09A11.8 11.8 0 0 0 1 12c1.73 3.89 6 7 11 7a11.1 11.1 0 0 0 5.45-1.43l3.74 3.74 1.42-1.42ZM12 17c-3.57 0-6.71-2.04-8.39-5A10.17 10.17 0 0 1 5.9 9.12l1.64 1.64A4 4 0 0 0 13.24 16l1.64 1.64A8.9 8.9 0 0 1 12 17Zm-.61-8.44-2.95-2.95A4 4 0 0 1 18.46 9.2l2.12 2.12A10.24 10.24 0 0 0 23 12c-1.73-3.89-6-7-11-7a10.8 10.8 0 0 0-3.23.49l2.01 2.01A4 4 0 0 1 16 12a4 4 0 0 1-.27 1.44l-1.57-1.57A2 2 0 0 0 12 9.86c-.22 0-.42.04-.61.11Z" />
                  )}
                </svg>
              </button>
            </div>
          </div>

          <div className="login-page__options">
            <label className="login-page__checkbox">
              <input
                type="checkbox"
                checked={rememberMe}
                onChange={(event) => setRememberMe(event.target.checked)}
              />
              <span>记住我</span>
            </label>

            <a href="#" onClick={(event) => event.preventDefault()}>
              忘记密码?
            </a>
          </div>

          <button className="login-page__submit" type="submit" disabled={isSubmitting}>
            {isSubmitting ? '登录中...' : '登 录'}
          </button>

          {submitMessage && (
            <p className="login-page__message login-page__message--info">
              {submitMessage}
            </p>
          )}

          {submitError && (
            <div className="login-page__message login-page__message--error">
              <strong>{submitError.message}</strong>
              {submitError.traceId && (
                <span>traceId: {submitError.traceId}</span>
              )}
            </div>
          )}
        </form>

        <div className="login-page__footer">
          <p>© 2023 农产品仓储管理系统. All rights reserved.</p>
        </div>
      </div>
    </div>
  )
}

export default LoginPage
