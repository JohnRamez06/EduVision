// src/services/websocket.js
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

class WebSocketService {
    constructor() {
        this.client = null;
        this.subscriptions = new Map();
        this.handlers = {
            onMood: null,
            onAlert: null,
            onConnect: null,
            onDisconnect: null,
            onAttendance: null,
            onEngagement: null
        };
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
        this.reconnectDelay = 3000;
    }

    connect(sessionId, token) {
        if (this.client && this.client.connected) {
            console.log('Already connected');
            return;
        }

        this.client = new Client({
            webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
            connectHeaders: {
                Authorization: `Bearer ${token}`
            },
            debug: (str) => {
                if (process.env.NODE_ENV === 'development') {
                    console.log('[STOMP]', str);
                }
            },
            reconnectDelay: this.reconnectDelay,
            heartbeatIncoming: 10000,
            heartbeatOutgoing: 10000,
            onConnect: () => {
                console.log('✅ WebSocket Connected successfully');
                this.reconnectAttempts = 0;
                
                // Subscribe to all session topics
                this.subscribeToTopics(sessionId);
                
                if (this.handlers.onConnect) {
                    this.handlers.onConnect();
                }
            },
            onDisconnect: () => {
                console.log('🔌 WebSocket Disconnected');
                if (this.handlers.onDisconnect) {
                    this.handlers.onDisconnect();
                }
            },
            onStompError: (frame) => {
                console.error('❌ STOMP Error:', frame);
                this.handleReconnect();
            },
            onWebSocketError: (error) => {
                console.error('❌ WebSocket Error:', error);
                this.handleReconnect();
            }
        });

        this.client.activate();
    }

    subscribeToTopics(sessionId) {
        // Unsubscribe from existing subscriptions
        this.subscriptions.forEach((sub, key) => {
            if (sub) sub.unsubscribe();
        });
        this.subscriptions.clear();

        // Subscribe to face/mood updates
        const moodSub = this.client.subscribe(`/topic/session.${sessionId}.faces`, (message) => {
            try {
                const data = JSON.parse(message.body);
                if (this.handlers.onMood) {
                    this.handlers.onMood(data);
                }
            } catch (e) {
                console.error('Failed to parse mood message:', e);
            }
        });
        this.subscriptions.set('mood', moodSub);

        // Subscribe to alerts
        const alertSub = this.client.subscribe(`/topic/session.${sessionId}.alerts`, (message) => {
            try {
                const data = JSON.parse(message.body);
                if (this.handlers.onAlert) {
                    this.handlers.onAlert(data);
                }
            } catch (e) {
                console.error('Failed to parse alert message:', e);
            }
        });
        this.subscriptions.set('alerts', alertSub);

        // Subscribe to engagement updates
        const engagementSub = this.client.subscribe(`/topic/session.${sessionId}.engagement`, (message) => {
            try {
                const data = JSON.parse(message.body);
                if (this.handlers.onEngagement) {
                    this.handlers.onEngagement(data);
                }
            } catch (e) {
                console.error('Failed to parse engagement message:', e);
            }
        });
        this.subscriptions.set('engagement', engagementSub);

        // Subscribe to attendance updates
        const attendanceSub = this.client.subscribe(`/topic/session.${sessionId}.attendance`, (message) => {
            try {
                const data = JSON.parse(message.body);
                if (this.handlers.onAttendance) {
                    this.handlers.onAttendance(data);
                }
            } catch (e) {
                console.error('Failed to parse attendance message:', e);
            }
        });
        this.subscriptions.set('attendance', attendanceSub);

        console.log(`📡 Subscribed to topics for session ${sessionId}`);
    }

    handleReconnect() {
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++;
            console.log(`🔄 Reconnecting attempt ${this.reconnectAttempts}/${this.maxReconnectAttempts}`);
            setTimeout(() => {
                if (this.client && !this.client.connected) {
                    this.client.activate();
                }
            }, this.reconnectDelay * this.reconnectAttempts);
        } else {
            console.error('❌ Max reconnection attempts reached');
            if (this.handlers.onDisconnect) {
                this.handlers.onDisconnect();
            }
        }
    }

    disconnect() {
        if (this.client) {
            this.subscriptions.forEach((sub) => {
                if (sub) sub.unsubscribe();
            });
            this.subscriptions.clear();
            
            if (this.client.connected) {
                this.client.deactivate();
            }
            this.client = null;
        }
    }

    setHandlers(handlers) {
        this.handlers = { ...this.handlers, ...handlers };
    }

    isConnected() {
        return this.client && this.client.connected;
    }

    sendMessage(destination, body) {
        if (this.client && this.client.connected) {
            this.client.publish({
                destination,
                body: JSON.stringify(body)
            });
        } else {
            console.warn('Cannot send message: WebSocket not connected');
        }
    }
}

// Singleton instance
const websocketService = new WebSocketService();

// Helper functions for components
export const createSessionClient = ({ sessionId, onMood, onAlert, onConnect, onDisconnect, onAttendance, onEngagement }) => {
    const token = localStorage.getItem('token');
    
    websocketService.setHandlers({
        onMood,
        onAlert,
        onConnect,
        onDisconnect,
        onAttendance,
        onEngagement
    });
    
    websocketService.connect(sessionId, token);
    
    return {
        deactivate: () => websocketService.disconnect(),
        isConnected: () => websocketService.isConnected(),
        sendMessage: (dest, body) => websocketService.sendMessage(dest, body)
    };
};

export const createAlertClient = ({ onAlert, onConnect, onDisconnect }) => {
    // This is for global alerts - simplified version
    const token = localStorage.getItem('token');
    
    const tempClient = new Client({
        webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
        connectHeaders: { Authorization: `Bearer ${token}` },
        onConnect: () => {
            const sub = tempClient.subscribe('/topic/global.alerts', (message) => {
                try {
                    const data = JSON.parse(message.body);
                    if (onAlert) onAlert(data);
                } catch (e) {}
            });
            tempClient.subscriptions = { global: sub };
            if (onConnect) onConnect();
        },
        onDisconnect: () => {
            if (onDisconnect) onDisconnect();
        }
    });
    
    tempClient.activate();
    
    return {
        activate: () => {},
        deactivate: () => {
            if (tempClient.subscriptions?.global) {
                tempClient.subscriptions.global.unsubscribe();
            }
            tempClient.deactivate();
        }
    };
};