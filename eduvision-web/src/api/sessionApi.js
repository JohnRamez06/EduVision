import axiosClient from './axiosClient'
export const getSessions = () => axiosClient.get('/sessions')
export const startSession = (data) => axiosClient.post('/sessions/start', data)
export const stopSession = (id) => axiosClient.put(`/sessions/${id}/stop`)
