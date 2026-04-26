import api from './api'
import axios from 'axios'

const emotionService = {
  getLatest:  (sessionId) => api.get(`/emotion-data/session/${sessionId}?latest=true`).then(r => r.status === 204 ? null : r.data),
  getHistory: (sessionId) => api.get(`/emotion-data/session/${sessionId}?latest=false`).then(r => r.data),

  // Send a captured video frame to the Python vision engine
  analyzeFrame: (sessionId, blob) => {
    const fd = new FormData()
    fd.append('file', blob, 'frame.jpg')
    return axios.post(`/vision/analyze/frame?session_id=${sessionId}&store=true`, fd, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 8000,
    }).then(r => r.data)
  },
}

export default emotionService
