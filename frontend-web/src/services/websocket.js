import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
export function createWebSocketClient(onMessage) {
  const client = new Client({ webSocketFactory: () => new SockJS('/ws'), onConnect: () => { client.subscribe('/topic/alerts', onMessage) } })
  return client
}
