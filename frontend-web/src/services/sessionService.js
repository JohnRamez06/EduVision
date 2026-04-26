import api from './api'

const sessionService = {
  start:     (body)  => api.post('/sessions/start', body).then(r => r.data),
  end:       (id)    => api.post(`/sessions/${id}/end`, { sessionId: id, actualEnd: new Date().toISOString() }).then(r => r.data),
  getStatus: (id)    => api.get(`/sessions/${id}/status`).then(r => r.data),
  getActive: ()      => api.get('/sessions/active').then(r => r.data),
}

export default sessionService
