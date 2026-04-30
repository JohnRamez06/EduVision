import { useCallback, useEffect, useState } from 'react'
import sessionService from '../services/sessionService'

export default function useSession(sessionId) {
  const [session, setSession] = useState(null)
  const [loading, setLoading] = useState(Boolean(sessionId))
  const [error, setError] = useState('')

  const refresh = useCallback(async () => {
    if (!sessionId) return null
    setLoading(true)
    setError('')
    try {
      const data = await sessionService.getSessionStatus(sessionId)
      setSession(data)
      return data
    } catch (caughtError) {
      setError(caughtError.response?.data?.message ?? caughtError.message ?? 'Failed to load session.')
      return null
    } finally {
      setLoading(false)
    }
  }, [sessionId])

  useEffect(() => {
    refresh()
  }, [refresh])

  return { session, loading, error, refresh }
}