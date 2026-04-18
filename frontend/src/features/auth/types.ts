export interface LoginRequest {
  account: string
  password: string
  rememberMe: boolean
}

export interface AuthUser {
  userId: string
  username: string
  displayName: string
  phone: string
  roles: string[]
}

export interface AuthTokenResponse {
  tokenType: string
  accessToken: string
  refreshToken: string
  accessTokenExpiresAt: string
  refreshTokenExpiresAt: string
  user: AuthUser
}
