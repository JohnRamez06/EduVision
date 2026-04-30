import React, { createContext, useMemo, useState } from 'react'
import { clearAuth, getToken, getUser, saveAuth } from '../utils/tokenManager'
import { getRole } from '../utils/roleChecker'

export const AuthContext = createContext(null)

const normalizeUser = (user, token) => {
  if (!user) return null
  const role = getRole(user)
  return {
    ...user,
    token: token ?? user.token,
    role,
  }
}

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => getToken())
  const [user, setUser] = useState(() => normalizeUser(getUser(), getToken()))

  const login = (userData, jwt) => {
    const nextUser = normalizeUser(userData, jwt)
    setUser(nextUser)
    setToken(jwt)
    saveAuth({ token: jwt, user: nextUser })
  }

  const logout = () => {
    setUser(null)
    setToken(null)
    clearAuth()
  }

  const value = useMemo(() => ({
    user,
    token,
    role: user?.role ?? '',
    isAuthenticated: Boolean(token),
    login,
    logout,
  }), [user, token])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
