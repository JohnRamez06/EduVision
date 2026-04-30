const TOKEN_KEY = 'eduvision.token'
const USER_KEY = 'eduvision.user'

function safeParse(value) {
  if (!value) return null
  try {
    return JSON.parse(value)
  } catch {
    return null
  }
}

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token) {
  if (token) {
    localStorage.setItem(TOKEN_KEY, token)
  } else {
    localStorage.removeItem(TOKEN_KEY)
  }
}

export function getUser() {
  return safeParse(localStorage.getItem(USER_KEY))
}

export function setUser(user) {
  if (user) {
    localStorage.setItem(USER_KEY, JSON.stringify(user))
  } else {
    localStorage.removeItem(USER_KEY)
  }
}

export function clearAuth() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

export function saveAuth({ token, user }) {
  setToken(token)
  setUser(user)
}

export default {
  getToken,
  setToken,
  getUser,
  setUser,
  clearAuth,
  saveAuth,
}