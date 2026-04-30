import api from './api'

const sessionService = {
  // Start a new lecture session
  start: (body) => 
    api.post('/sessions/start', body).then(r => r.data),

  // Start alias
  startSession: (body) => 
    api.post('/sessions/start', body).then(r => r.data),

  // End a session
  end: (sessionId) => 
    api.post(`/sessions/${sessionId}/end`, { 
      sessionId, 
      actualEnd: new Date().toISOString() 
    }).then(r => r.data),

  // End alias
  endSession: (sessionId, actualEnd = new Date().toISOString()) => 
    api.post(`/sessions/${sessionId}/end`, { sessionId, actualEnd }).then(r => r.data),

  // Get session status
  getStatus: (sessionId) => 
    api.get(`/sessions/${sessionId}/status`).then(r => r.data),

  // Get session status alias
  getSessionStatus: (sessionId) => 
    api.get(`/sessions/${sessionId}/status`).then(r => r.data),

  // Get all active sessions
  getActive: () => 
    api.get('/sessions/active').then(r => r.data),

  // Get active sessions alias
  getActiveSessions: () => 
    api.get('/sessions/active').then(r => r.data),
}

export default sessionService