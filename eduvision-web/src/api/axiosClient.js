import axios from 'axios'
const axiosClient = axios.create({ baseURL: '/api/v1', timeout: 10000 })
axiosClient.interceptors.request.use(config => { const token = localStorage.getItem('token'); if (token) config.headers.Authorization = `Bearer ${token}`; return config })
export default axiosClient
