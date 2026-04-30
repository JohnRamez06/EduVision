import { useEffect, useRef, useState, useCallback } from 'react'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

export default function useWebSocket(sessionId) {
    const [connected, setConnected] = useState(false)
    const [mood, setMood] = useState(null)
    const [alerts, setAlerts] = useState([])
    const clientRef = useRef(null)

    useEffect(() => {
        if (!sessionId) return

        const client = new Client({
            webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
            debug: (msg) => console.debug('[WS]', msg),
            onConnect: () => {
                setConnected(true)
                
                client.subscribe(`/topic/lecture/${sessionId}/mood`, (message) => {
                    const data = JSON.parse(message.body)
                    setMood(data)
                })
                
                client.subscribe(`/topic/lecture/${sessionId}/alerts`, (message) => {
                    const data = JSON.parse(message.body)
                    setAlerts((prev) => [data, ...prev])
                })
            },
            onDisconnect: () => setConnected(false),
            onStompError: (frame) => console.error('[WS Error]', frame),
        })

        client.activate()
        clientRef.current = client

        return () => {
            client.deactivate()
        }
    }, [sessionId])

    const clearAlerts = useCallback(() => setAlerts([]), [])

    return { connected, mood, alerts, clearAlerts }
}