import api from './api'

const authService = {
  async login(email, password) {
    const { data } = await api.post('/auth/login', { email, password })
    // data: { token, email, fullName, roles: ["STUDENT"|"LECTURER"|...], expiresIn }
    const role = data.roles?.[0] ?? [...(data.roles ?? [])][0] ?? ''
    return {
      token: data.token,
      user:  { email: data.email, fullName: data.fullName, role: role.toLowerCase(), roles: data.roles },
    }
  },

  async register(firstName, lastName, email, password, roleName) {
    const { data } = await api.post('/auth/register', {
      firstName,
      lastName,
      email,
      password,
      roleName: roleName.toUpperCase(),   // backend expects "STUDENT" | "LECTURER" | "ADMIN"
    })
    return data
  },

  async logout() {
    try { await api.post('/auth/logout') } catch { /* stateless — ignore */ }
  },
}

export default authService
