import { useCallback, useEffect, useState } from 'react'
import notificationService from '../services/notificationService'

export default function useNotifications(userId) {
  const [notifications, setNotifications] = useState([])
  const [loading, setLoading] = useState(Boolean(userId))
  const [error, setError] = useState('')

  const refresh = useCallback(async () => {
    if (!userId) return []
    setLoading(true)
    setError('')
    try {
      const data = await notificationService.getUserNotifications(userId)
      setNotifications(data)
      return data
    } catch (caughtError) {
      setError(caughtError.response?.data?.message ?? caughtError.message ?? 'Failed to load notifications.')
      return []
    } finally {
      setLoading(false)
    }
  }, [userId])

  useEffect(() => {
    refresh()
  }, [refresh])

  const markAsRead = async (id) => {
    await notificationService.markAsRead(id)
    setNotifications((current) => current.map((item) => (item.id === id ? { ...item, read: true, isRead: true } : item)))
  }

  return { notifications, loading, error, refresh, markAsRead }
}