export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1'
export const WS_BASE_URL = import.meta.env.VITE_WS_URL || 'http://localhost:8080/ws'
export const PYTHON_BASE_URL = import.meta.env.VITE_PYTHON_URL || 'http://localhost:8000'
export const SHINY_BASE_URL = import.meta.env.VITE_SHINY_BASE_URL || 'http://localhost:3838'

export const buildApiUrl = (path = '') => {
	if (!path) return API_BASE_URL
	return `${API_BASE_URL}${path.startsWith('/') ? path : `/${path}`}`
}
