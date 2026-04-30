import React, { createContext, useContext, useEffect, useMemo, useState } from 'react'
import { createAlertClient } from '../services/websocket'
import { AuthContext } from './AuthContext'

export const WebSocketContext = createContext(null)

export function WebSocketProvider({ children }) {
  const [globalAlerts, setGlobalAlerts] = useState([])
  const [connected, setConnected] = useState(false)
  const { token } = useContext(AuthContext)

  useEffect(() => {
    if (!token) {
      setConnected(false)
      setGlobalAlerts([])
      return undefined
    }

    const client = createAlertClient({
      onAlert: (alert) => {
        setGlobalAlerts((current) => [alert, ...current].slice(0, 25))
      },
      onConnect: () => setConnected(true),
      onDisconnect: () => setConnected(false),
    })

    client.activate()
    return () => client.deactivate()
  }, [token])

  const value = useMemo(() => ({
    globalAlerts,
    unreadCount: globalAlerts.length,
    connected,
  }), [globalAlerts, connected])

  return <WebSocketContext.Provider value={value}>{children}</WebSocketContext.Provider>
}

export default WebSocketProvider