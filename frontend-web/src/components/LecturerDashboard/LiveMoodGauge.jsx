import React, { useState, useCallback } from 'react'
import CameraControlPanel from './CameraControlPanel'
import LiveMoodGauge from './LiveMoodGauge'
import AlertBanner from './AlertBanner'
import sessionService from '../../services/sessionService'
import useWebSocket from '../../hooks/useWebSocket'

export default function LecturerLiveSession() {
    const [sessionId, setSessionId] = useState(null)
    const [cameraOn, setCameraOn] = useState(false)
    const [cameraError, setCameraError] = useState('')
    const { connected: wsConnected, mood, alerts, clearAlerts } = useWebSocket(sessionId)

    const handleStart = useCallback(async (courseId) => {
        setCameraError('')
        try {
            const session = await sessionService.startSession({
                courseId,
                scheduledStart: new Date().toISOString(),
                scheduledEnd: new Date(Date.now() + 2 * 60 * 60 * 1000).toISOString(),
                cameraType: 'usb',
                roomLocation: 'Room 101'
            })
            setSessionId(session.sessionId)
            setCameraOn(true)
            // Also start Python camera
            await fetch('http://localhost:8000/analyze/frame', { method: 'POST' }).catch(() => {})
        } catch (err) {
            setCameraError(err.response?.data?.message || 'Failed to start session')
        }
    }, [])

    const handleStop = useCallback(async () => {
        if (!sessionId) return
        try {
            await sessionService.endSession(sessionId)
            setCameraOn(false)
            setSessionId(null)
        } catch (err) {
            setCameraError(err.response?.data?.message || 'Failed to end session')
        }
    }, [sessionId])

    return (
        <div className="space-y-6">
            <CameraControlPanel
                cameraOn={cameraOn}
                wsConnected={wsConnected}
                cameraError={cameraError}
                onStart={handleStart}
                onStop={handleStop}
            />

            {sessionId && (
                <>
                    <AlertBanner alerts={alerts} onClear={clearAlerts} />
                    <LiveMoodGauge mood={mood} />
                </>
            )}
        </div>
    )
}