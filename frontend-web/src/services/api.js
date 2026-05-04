// frontend-web/src/services/api.js
import axios from 'axios'
import { API_BASE_URL } from '../config/api'
import { getToken } from '../utils/tokenManager'

const api = axios.create({
    baseURL: `${API_BASE_URL}/api/v1`,  // Add /api/v1 here
    timeout: 15000,
    headers: {
        'Content-Type': 'application/json',
    },
})

api.interceptors.request.use((config) => {
    const token = getToken()
    if (token) {
        config.headers = config.headers ?? {}
        config.headers.Authorization = `Bearer ${token}`
    }
    return config
})

export default api