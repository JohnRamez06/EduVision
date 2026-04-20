import React, { createContext, useState } from 'react'
export const AuthContext = createContext(null)
export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [token, setToken] = useState(localStorage.getItem('token'))
  const login = (userData, jwt) => { setUser(userData); setToken(jwt); localStorage.setItem('token', jwt) }
  const logout = () => { setUser(null); setToken(null); localStorage.removeItem('token') }
  return <AuthContext.Provider value={{ user, token, login, logout }}>{children}</AuthContext.Provider>
}
