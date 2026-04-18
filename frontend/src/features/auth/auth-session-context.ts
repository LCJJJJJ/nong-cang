import { createContext } from 'react'

import type { AuthUser } from './types'

export type AuthStatus = 'checking' | 'authenticated' | 'unauthenticated'

export interface AuthSessionContextValue {
  status: AuthStatus
  user: AuthUser | null
  setAuthenticated: (user: AuthUser) => void
  setLoggedOut: () => void
}

export const AuthSessionContext = createContext<AuthSessionContextValue | null>(
  null,
)
