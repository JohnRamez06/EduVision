import { useCallback, useEffect, useState } from 'react'
import emotionService from '../services/emotionService'

export default function useEmotionData(sessionId) {
  const [latest, setLatest] = useState(null)
  const [history, setHistory] = useState([])
  const [loading, setLoading] = useState(Boolean(sessionId))
  const [error, setError] = useState('')

  const refresh = useCallback(async () => {
    if (!sessionId) return null
    setLoading(true)
    setError('')
    try {
      const [latestSnapshot, sessionHistory] = await Promise.all([
        emotionService.getLatestSnapshot(sessionId).catch(() => null),
        emotionService.getSessionHistory(sessionId).catch(() => []),
      ])
      setLatest(latestSnapshot)
      setHistory(Array.isArray(sessionHistory?.history) ? sessionHistory.history : sessionHistory)
      return { latestSnapshot, sessionHistory }
    } catch (caughtError) {
      setError(caughtError.response?.data?.message ?? caughtError.message ?? 'Failed to load emotion data.')
      return null
    } finally {
      setLoading(false)
    }
  }, [sessionId])

  useEffect(() => {
    refresh()
  }, [refresh])

  return { latest, history, loading, error, refresh }
}