import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { WS_BASE_URL } from '../config/api'
import { getToken } from '../utils/tokenManager'

function createClient({ onConnect, onDisconnect, onStompError } = {}) {
  const token = getToken()
  return new Client({
    webSocketFactory: () => new SockJS(WS_BASE_URL),
    connectHeaders: token ? { Authorization: `Bearer ${token}` } : {},
    reconnectDelay: 5000,
    heartbeatIncoming: 10000,
    heartbeatOutgoing: 10000,
    onConnect: onConnect || (() => {}),
    onDisconnect: onDisconnect || (() => {}),
    onStompError: onStompError || ((frame) => console.error('WebSocket error:', frame.headers?.message || 'Unknown error')),
  })
}

export function createAlertClient({ onAlert, onConnect, onDisconnect } = {}) {
  const client = createClient({
    onConnect: () => {
      client.subscribe('/topic/alerts', (frame) => {
        try { onAlert?.(JSON.parse(frame.body)) } catch (e) {}
      })
      onConnect?.()
    },
    onDisconnect,
  })
  client.activate()
  return client
}

export function createSessionClient({ sessionId, onMood, onAlert, onConnect, onDisconnect } = {}) {
  const client = createClient({
    onConnect: () => {
      client.subscribe(`/topic/lecture/${sessionId}/mood`, (frame) => {
        try { onMood?.(JSON.parse(frame.body)) } catch (e) { console.error('Mood parse error:', e) }
      })
      client.subscribe(`/topic/lecture/${sessionId}/alerts`, (frame) => {
        try { onAlert?.(JSON.parse(frame.body)) } catch (e) { console.error('Alert parse error:', e) }
      })
      onConnect?.()
    },
    onDisconnect,
  })
  client.activate()
  return client
}

export default {
  createAlertClient,
  createSessionClient,
}