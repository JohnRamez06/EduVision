import api from './api'

const emotionService = {
  // Send webcam frame to Python for analysis
  async analyzeFrame(sessionId, imageBlob) {
    const formData = new FormData()
    formData.append('session_id', sessionId)
    formData.append('store', 'true')
    formData.append('file', imageBlob, 'frame.jpg')

    const response = await fetch('http://localhost:8000/analyze/frame', {
      method: 'POST',
      body: formData,
    })
    
    if (!response.ok) {
      throw new Error(`Python error: ${response.status}`)
    }
    
    return response.json()
  },

  // Save class snapshot to Spring Boot
  saveClassSnapshot: (snapshot) => 
    api.post('/emotion-data/class-snapshot', snapshot).then(r => r.data),

  // Save student snapshots to Spring Boot
  saveStudentSnapshots: (snapshotId, snapshots) => 
    api.post(`/emotion-data/student-snapshots?snapshotId=${encodeURIComponent(snapshotId)}`, snapshots).then(r => r.data),

  // Get latest snapshot for a session
  getLatest: (sessionId) => 
    api.get(`/emotion-data/session/${sessionId}?latest=true`).then(r => r.status === 204 ? null : r.data),

  // Get full session history
  getHistory: (sessionId) => 
    api.get(`/emotion-data/session/${sessionId}?latest=false`).then(r => r.data),

  // Aliases
  getLatestSnapshot: (sessionId) => 
    api.get(`/emotion-data/session/${sessionId}?latest=true`).then(r => r.status === 204 ? null : r.data),

  getSessionHistory: (sessionId) => 
    api.get(`/emotion-data/session/${sessionId}?latest=false`).then(r => r.data),
}

export default emotionService