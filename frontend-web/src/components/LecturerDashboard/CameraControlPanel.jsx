import React, { useState, useEffect } from 'react'
import { Camera, PlayCircle, Square, Video } from 'lucide-react'
import api from '../../services/api'

export default function CameraControlPanel({ cameraOn, wsConnected, cameraError, onStart, onStop }) {
    const [courses, setCourses] = useState([])
    const [selectedCourseId, setSelectedCourseId] = useState('')
    const [loading, setLoading] = useState(false)

    useEffect(() => {
        // Fetch lecturer's courses
        api.get('/lecturers/courses')
            .then(res => setCourses(res.data))
            .catch(err => console.error('Failed to load courses:', err))
    }, [])

    const handleStart = async () => {
        if (!selectedCourseId) {
            alert('Please select a course first')
            return
        }
        setLoading(true)
        try {
            await onStart(selectedCourseId)
        } finally {
            setLoading(false)
        }
    }

    return (
        <div className="glass rounded-2xl p-5">
            <div className="flex items-center justify-between mb-4">
                <h3 className="font-semibold text-white flex items-center gap-2">
                    <Camera size={16} className="text-teal-400" /> Camera Controls
                </h3>
                <span className={`text-xs px-2 py-1 rounded-full ${wsConnected ? 'bg-emerald-500/10 text-emerald-300' : 'bg-slate-700/40 text-slate-400'}`}>
                    {wsConnected ? 'Connected' : 'Disconnected'}
                </span>
            </div>

            {/* Course Selector */}
            <div className="mb-4">
                <label className="text-xs text-slate-400 block mb-1">Select Course</label>
                <select
                    value={selectedCourseId}
                    onChange={(e) => setSelectedCourseId(e.target.value)}
                    className="w-full bg-slate-800 border border-slate-700 rounded-lg px-3 py-2 text-white text-sm"
                >
                    <option value="">-- Choose a course --</option>
                    {courses.map(course => (
                        <option key={course.courseId} value={course.courseId}>
                            {course.code} - {course.title}
                        </option>
                    ))}
                </select>
            </div>

            {cameraError && <p className="text-xs text-rose-400 mb-3">{cameraError}</p>}

            <div className="flex flex-wrap gap-2">
                <button
                    onClick={handleStart}
                    disabled={!selectedCourseId || loading}
                    className="px-3 py-2 rounded-xl bg-emerald-500/15 text-emerald-300 text-sm flex items-center gap-2 disabled:opacity-50"
                >
                    <PlayCircle size={14} /> {loading ? 'Starting...' : 'Start Session'}
                </button>
                <button onClick={onStop} className="px-3 py-2 rounded-xl bg-rose-500/15 text-rose-300 text-sm flex items-center gap-2">
                    <Square size={14} /> End Session
                </button>
            </div>

            <p className="text-xs text-slate-500 mt-3">
                Camera is currently {cameraOn ? 'active' : 'inactive'}.
            </p>
        </div>
    )
}