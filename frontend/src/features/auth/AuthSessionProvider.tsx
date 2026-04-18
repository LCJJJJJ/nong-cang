import {
  type PropsWithChildren,
  useEffect,
  useMemo,
  useState,
} from 'react'

import { getCurrentUser } from './api'
import {
  AuthSessionContext,
  type AuthSessionContextValue,
  type AuthStatus,
} from './auth-session-context'
import {
  clearAuthSession,
  getAuthUser,
  hasAuthSession,
  saveAuthUser,
} from './storage'
import type { AuthUser } from './types'

function AuthSessionProvider({ children }: PropsWithChildren) {
  const [status, setStatus] = useState<AuthStatus>('checking')
  const [user, setUser] = useState<AuthUser | null>(getAuthUser())

  useEffect(() => {
    let isMounted = true

    const initializeSession = async () => {
      if (!hasAuthSession()) {
        if (isMounted) {
          setUser(null)
          setStatus('unauthenticated')
        }

        return
      }

      if (isMounted) {
        setStatus('checking')
      }

      try {
        const currentUser = await getCurrentUser()

        if (!isMounted) {
          return
        }

        saveAuthUser(currentUser)
        setUser(currentUser)
        setStatus('authenticated')
      } catch {
        if (!isMounted) {
          return
        }

        clearAuthSession()
        setUser(null)
        setStatus('unauthenticated')
      }
    }

    void initializeSession()

    return () => {
      isMounted = false
    }
  }, [])

  const value = useMemo<AuthSessionContextValue>(
    () => ({
      status,
      user,
      setAuthenticated: (nextUser) => {
        saveAuthUser(nextUser)
        setUser(nextUser)
        setStatus('authenticated')
      },
      setLoggedOut: () => {
        clearAuthSession()
        setUser(null)
        setStatus('unauthenticated')
      },
    }),
    [status, user],
  )

  return (
    <AuthSessionContext.Provider value={value}>
      {children}
    </AuthSessionContext.Provider>
  )
}

export default AuthSessionProvider
