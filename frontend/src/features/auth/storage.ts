import type { AuthTokenResponse, AuthUser } from './types'

const ACCESS_TOKEN_KEY = 'nong-cang.access-token'
const REFRESH_TOKEN_KEY = 'nong-cang.refresh-token'
const AUTH_USER_KEY = 'nong-cang.auth-user'

function getStorage() {
  if (typeof window === 'undefined') {
    return null
  }

  return window.localStorage
}

export function saveAuthSession(session: AuthTokenResponse) {
  const storage = getStorage()

  if (!storage) {
    return
  }

  storage.setItem(ACCESS_TOKEN_KEY, session.accessToken)
  storage.setItem(REFRESH_TOKEN_KEY, session.refreshToken)
  storage.setItem(AUTH_USER_KEY, JSON.stringify(session.user))
}

export function saveAuthUser(user: AuthUser) {
  const storage = getStorage()

  if (!storage) {
    return
  }

  storage.setItem(AUTH_USER_KEY, JSON.stringify(user))
}

export function clearAuthSession() {
  const storage = getStorage()

  if (!storage) {
    return
  }

  storage.removeItem(ACCESS_TOKEN_KEY)
  storage.removeItem(REFRESH_TOKEN_KEY)
  storage.removeItem(AUTH_USER_KEY)
}

export function getAccessToken() {
  return getStorage()?.getItem(ACCESS_TOKEN_KEY) ?? null
}

export function getRefreshToken() {
  return getStorage()?.getItem(REFRESH_TOKEN_KEY) ?? null
}

export function getAuthUser(): AuthUser | null {
  const rawUser = getStorage()?.getItem(AUTH_USER_KEY)

  if (!rawUser) {
    return null
  }

  try {
    return JSON.parse(rawUser) as AuthUser
  } catch {
    clearAuthSession()
    return null
  }
}

export function hasAuthSession() {
  return Boolean(getAccessToken() || getRefreshToken())
}
