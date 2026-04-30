import api from './api'

const authService = {
  async login(email, password) {
    try {
      const { data } = await api.post('/auth/login', { email, password })
      return data
    } catch (error) {
      // Log the exact error for debugging
      if (error.response) {
        console.error('Login failed:', {
          status: error.response.status,
          data: error.response.data,
        })
        throw new Error(error.response.data?.message || 'Invalid email or password')
      }
      throw error
    }
  },

  async register(payload) {
    try {
      const request = {
        ...payload,
        roleName: String(payload.roleName ?? '').toUpperCase(),
      }
      const { data } = await api.post('/auth/register', request)
      return data
    } catch (error) {
      if (error.response) {
        throw new Error(error.response.data?.message || 'Registration failed')
      }
      throw error
    }
  },

  async logout() {
    try {
      await api.post('/auth/logout')
    } catch {
      // Stateless JWT: client removes token
    }
  },
}

export default authService