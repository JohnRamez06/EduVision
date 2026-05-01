import { useEffect, useRef, useState, useCallback } from 'react'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { WS_BASE_URL } from '../config/api'
import { getToken } from '../utils/tokenManager'

export default function useWebSocket(sessionId) {
  const [connected, setConnected] = useState(false)
  const [mood, setMood] = useState(null)
  const [alerts, setAlerts] = useState([])
  const clientRef = useRef(null)

  useEffect(() => {
    if (!sessionId) return

    const token = getToken()
    const client = new Client({
      webSocketFactory: () => new SockJS(WS_BASE_URL),
      connectHeaders: token ? { Authorization: `Bearer ${token}` } : {},
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      onConnect: () => {
        setConnected(true)

        client.subscribe(`/topic/lecture/${sessionId}/mood`, (message) => {
          try {
            const data = JSON.parse(message.body)
            setMood(data)
          } catch (e) {
            console.error('Mood parse error:', e)
          }
        })

        client.subscribe(`/topic/lecture/${sessionId}/alerts`, (message) => {
          try {
            const data = JSON.parse(message.body)
            setAlerts((prev) => [data, ...prev].slice(0, 50))
          } catch (e) {
            console.error('Alert parse error:', e)
          }
        })
      },
      onDisconnect: () => setConnected(false),
      onStompError: (frame) => console.error('WebSocket error:', frame.headers?.message),
    })

    client.activate()
    clientRef.current = client

    return () => {
      client.deactivate()
      setConnected(false)
    }
  }, [sessionId])

  const clearAlerts = useCallback(() => setAlerts([]), [])

  return { connected, mood, alerts, clearAlerts }
}