import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

// Generic global alerts client (used by layout notification bell)
export function createWebSocketClient(onMessage) {
  const token = localStorage.getItem('token')
  const client = new Client({
    webSocketFactory: () => new SockJS('/ws'),
    connectHeaders: token ? { Authorization: `Bearer ${token}` } : {},
    onConnect: () => client.subscribe('/topic/alerts', onMessage),
    reconnectDelay: 5000,
  })
  return client
}

// Session-scoped client for the live session page
export function createSessionClient({ sessionId, onMood, onAlert, onConnect, onDisconnect }) {
  const token = localStorage.getItem('token')
  const client = new Client({
    webSocketFactory: () => new SockJS('/ws'),
    connectHeaders:   token ? { Authorization: `Bearer ${token}` } : {},
    reconnectDelay:   5000,
    onConnect: () => {
      client.subscribe(`/topic/session/${sessionId}/mood`,   frame => onMood?.(JSON.parse(frame.body)))
      client.subscribe(`/topic/session/${sessionId}/alerts`, frame => onAlert?.(JSON.parse(frame.body)))
      onConnect?.()
    },
    onDisconnect: () => onDisconnect?.(),
  })
  return client
}
