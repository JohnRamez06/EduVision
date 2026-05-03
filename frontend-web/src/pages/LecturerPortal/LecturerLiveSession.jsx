import React, { useCallback, useContext, useEffect, useRef, useState } from 'react'
import {
  Video, VideoOff, StopCircle, Users, Zap, Brain, AlertTriangle,
  CheckCircle, ChevronDown, Wifi, WifiOff, MapPin, BookOpen, Clock,
  Activity,
} from 'lucide-react'
import LecturerLayout from '../../layouts/LecturerLayout'
import { AuthContext } from '../../context/AuthContext'
import lecturerService from '../../services/lecturerService'
import sessionService   from '../../services/sessionService'
import emotionService   from '../../services/emotionService'
import { createSessionClient } from '../../services/websocket'

// ─── Constants ───────────────────────────────────────────────────────────────

const FRAME_INTERVAL_MS  = 3000
const SNAPSHOT_POLL_MS   = 10000

const EMOTION_META = {
  happy:     { label: 'Happy',     color: 'text-emerald-400', bg: 'bg-emerald-500/15', emoji: '😊' },
  engaged:   { label: 'Engaged',   color: 'text-violet-400',  bg: 'bg-violet-500/15',  emoji: '🎯' },
  neutral:   { label: 'Neutral',   color: 'text-slate-300',   bg: 'bg-slate-700/40',   emoji: '😐' },
  confused:  { label: 'Confused',  color: 'text-amber-400',   bg: 'bg-amber-500/15',   emoji: '🤔' },
  surprised: { label: 'Surprised', color: 'text-teal-400',    bg: 'bg-teal-500/15',    emoji: '😮' },
  sad:       { label: 'Sad',       color: 'text-blue-400',    bg: 'bg-blue-500/15',    emoji: '😔' },
  angry:     { label: 'Angry',     color: 'text-rose-400',    bg: 'bg-rose-500/15',    emoji: '😠' },
  fearful:   { label: 'Fearful',   color: 'text-orange-400',  bg: 'bg-orange-500/15',  emoji: '😨' },
  disgusted: { label: 'Disgusted', color: 'text-pink-400',    bg: 'bg-pink-500/15',    emoji: '🤢' },
}

const SEVERITY_STYLES = {
  critical: 'border-rose-500/40 bg-rose-500/10 text-rose-300',
  warning:  'border-amber-500/40 bg-amber-500/10 text-amber-300',
  info:     'border-blue-500/40 bg-blue-500/10 text-blue-300',
}

// Helper function to convert concentration to level string
const getConcentrationLevel = (concentration) => {
  if (typeof concentration === 'number') {
    if (concentration >= 0.7) return 'high'
    if (concentration >= 0.4) return 'medium'
    return 'low'
  }
  if (typeof concentration === 'string') {
    return concentration.toLowerCase()
  }
  return 'medium'
}

const CONCENTRATION_STYLE = {
  high:       'bg-emerald-500/20 text-emerald-300',
  medium:     'bg-amber-500/20   text-amber-300',
  low:        'bg-rose-500/20    text-rose-300',
  distracted: 'bg-rose-500/20    text-rose-300',
}

// ─── Sub-components ──────────────────────────────────────────────────────────

const Skeleton = ({ className = '' }) => (
  <div className={`animate-pulse rounded-xl bg-slate-800/60 ${className}`} />
)

function StatCard({ icon: Icon, label, value, sub, color = 'emerald', large = false }) {
  const colors = {
    emerald: 'bg-emerald-500/15 text-emerald-400',
    violet:  'bg-violet-500/15  text-violet-400',
    blue:    'bg-blue-500/15    text-blue-400',
    amber:   'bg-amber-500/15   text-amber-400',
    rose:    'bg-rose-500/15    text-rose-400',
  }
  return (
    <div className="glass rounded-2xl p-4 flex items-center gap-3">
      <div className={`rounded-xl flex items-center justify-center shrink-0 ${large ? 'w-12 h-12' : 'w-10 h-10'} ${colors[color]}`}>
        <Icon size={large ? 22 : 18} />
      </div>
      <div className="min-w-0">
        <p className="text-xs text-slate-500 font-medium">{label}</p>
        <p className={`font-bold text-white leading-tight ${large ? 'text-2xl' : 'text-xl'}`}>{value ?? '—'}</p>
        {sub && <p className="text-xs text-slate-500 mt-0.5">{sub}</p>}
      </div>
    </div>
  )
}

function EngagementArc({ value = 0 }) {
  const pct   = Math.min(100, Math.max(0, Math.round(value * 100)))
  const color = pct >= 70 ? '#10b981' : pct >= 45 ? '#f59e0b' : '#f43f5e'
  const r     = 44
  const circ  = 2 * Math.PI * r
  const dash  = (pct / 100) * circ

  return (
    <div className="glass rounded-2xl p-5 flex flex-col items-center justify-center gap-2">
      <p className="text-xs text-slate-500 font-medium">Engagement</p>
      <div className="relative w-28 h-28">
        <svg viewBox="0 0 100 100" className="w-full h-full -rotate-90">
          <circle cx="50" cy="50" r={r} fill="none" stroke="#1e293b" strokeWidth="8" />
          <circle
            cx="50" cy="50" r={r} fill="none"
            stroke={color} strokeWidth="8"
            strokeLinecap="round"
            strokeDasharray={`${dash} ${circ}`}
            style={{ transition: 'stroke-dasharray 0.6s ease' }}
          />
        </svg>
        <div className="absolute inset-0 flex flex-col items-center justify-center">
          <span className="text-2xl font-bold text-white">{pct}%</span>
        </div>
      </div>
    </div>
  )
}

function ConcentrationBars({ high = 0, medium = 0, low = 0 }) {
  const total = high + medium + low || 1
  const bars  = [
    { label: 'High',   value: high,   pct: Math.round((high   / total) * 100), color: 'bg-emerald-500' },
    { label: 'Medium', value: medium, pct: Math.round((medium / total) * 100), color: 'bg-amber-500'   },
    { label: 'Low',    value: low,    pct: Math.round((low    / total) * 100), color: 'bg-rose-500'    },
  ]
  return (
    <div className="glass rounded-2xl p-4">
      <p className="text-xs text-slate-500 font-medium mb-3 flex items-center gap-1.5">
        <Brain size={12} /> Concentration
      </p>
      <div className="space-y-2.5">
        {bars.map(b => (
          <div key={b.label}>
            <div className="flex justify-between text-xs mb-1">
              <span className="text-slate-400">{b.label}</span>
              <span className="text-slate-500">{b.value} students</span>
            </div>
            <div className="h-1.5 bg-slate-800 rounded-full overflow-hidden">
              <div
                className={`h-full rounded-full ${b.color} transition-all duration-700`}
                style={{ width: `${b.pct}%` }}
              />
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}

function AlertItem({ alert }) {
  const style = SEVERITY_STYLES[alert.severity] ?? SEVERITY_STYLES.info
  const Icon  = alert.severity === 'critical' ? AlertTriangle
              : alert.severity === 'warning'  ? AlertTriangle
              : CheckCircle
  return (
    <div className={`flex items-start gap-2.5 px-3 py-2.5 rounded-xl border text-sm ${style}`}>
      <Icon size={14} className="shrink-0 mt-0.5" />
      <div className="min-w-0">
        <p className="font-medium text-xs leading-none mb-0.5">{alert.title}</p>
        <p className="text-xs opacity-80 leading-snug">{alert.message}</p>
      </div>
      <span className="text-xs opacity-50 shrink-0 ml-auto">
        {alert.timestamp ? new Date(alert.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : ''}
      </span>
    </div>
  )
}

function StudentRow({ student }) {
  const meta = EMOTION_META[student.emotion?.toLowerCase()] ?? EMOTION_META.neutral
  
  // Handle concentration safely - convert number to string if needed
  const concentrationLevel = getConcentrationLevel(student.concentration)
  const concStyle = CONCENTRATION_STYLE[concentrationLevel] ?? 'bg-slate-500/20 text-slate-300'
  
  const initials = (student.studentName ?? '?')
    .split(' ').map(p => p[0]).join('').toUpperCase().slice(0, 2)

  return (
    <div className="flex items-center gap-3 px-3 py-2.5 rounded-xl bg-slate-800/40 border border-slate-700/40">
      <div className="w-8 h-8 rounded-full bg-violet-500/20 text-violet-300 flex items-center justify-center text-xs font-bold shrink-0">
        {initials}
      </div>
      <div className="flex-1 min-w-0">
        <p className="text-sm font-medium text-slate-200 truncate">{student.studentName}</p>
        <span className={`inline-flex items-center gap-1 text-xs mt-0.5 ${meta.color}`}>
          <span>{meta.emoji}</span>
          <span>{meta.label}</span>
        </span>
      </div>
      <span className={`text-xs px-2 py-0.5 rounded-full capitalize shrink-0 ${concStyle}`}>
        {concentrationLevel}
      </span>
    </div>
  )
}

// ─── Duration hook ────────────────────────────────────────────────────────────

function useDuration(startTime) {
  const [elapsed, setElapsed] = useState(0)
  useEffect(() => {
    if (!startTime) return
    const tick = () => setElapsed(Math.floor((Date.now() - new Date(startTime).getTime()) / 1000))
    tick()
    const id = setInterval(tick, 1000)
    return () => clearInterval(id)
  }, [startTime])
  const h = String(Math.floor(elapsed / 3600)).padStart(2, '0')
  const m = String(Math.floor((elapsed % 3600) / 60)).padStart(2, '0')
  const s = String(elapsed % 60).padStart(2, '0')
  return `${h}:${m}:${s}`
}

// ─── Main component ───────────────────────────────────────────────────────────

export default function LecturerLiveSession() {
  const { user } = useContext(AuthContext)

  const [courses, setCourses]         = useState([])
  const [loadingCourses, setLoadingCourses] = useState(true)
  const [form, setForm]               = useState({ courseId: '', room: '', duration: '2' })
  const [starting, setStarting]       = useState(false)
  const [setupError, setSetupError]   = useState('')

  const [session, setSession]         = useState(null)
  const [checkingActive, setCheckingActive] = useState(true)

  const [mood, setMood]               = useState(null)
  const [alerts, setAlerts]           = useState([])
  const [snapshot, setSnapshot]       = useState(null)
  const [detectedStudents, setDetectedStudents] = useState(new Map())

  const [cameraOn, setCameraOn]       = useState(false)
  const [cameraError, setCameraError] = useState('')
  const [wsConnected, setWsConnected] = useState(false)

  const videoRef    = useRef(null)
  const streamRef   = useRef(null)
  const wsClientRef = useRef(null)
  const frameTimer  = useRef(null)
  const pollTimer   = useRef(null)

  const duration = useDuration(session?.startTime)

  // ── On mount ───────────────────────────────────────────────────────────────

  useEffect(() => {
    lecturerService.getDashboard()
      .then(d => setCourses(d.courses ?? []))
      .catch(() => {})
      .finally(() => setLoadingCourses(false))

    sessionService.getActive()
      .then(list => { if (list?.length > 0) activateSession(list[0]) })
      .catch(() => {})
      .finally(() => setCheckingActive(false))

    return () => cleanup()
  }, [])

  // ── Helpers ────────────────────────────────────────────────────────────────

  const activateSession = useCallback((sess) => {
    setSession(sess)
    startCamera()
    connectWebSocket(sess.sessionId)
    startSnapshotPoll(sess.sessionId)
  }, [])

  const cleanup = () => {
    clearInterval(frameTimer.current)
    clearInterval(pollTimer.current)
    wsClientRef.current?.deactivate()
    if (streamRef.current) {
      streamRef.current.getTracks().forEach(t => t.stop())
      streamRef.current = null
    }
  }

  // ── Camera ─────────────────────────────────────────────────────────────────

  const startCamera = async () => {
    setCameraError('')
    try {
      const stream = await navigator.mediaDevices.getUserMedia({
        video: { width: { ideal: 1280 }, height: { ideal: 720 }, facingMode: 'user' },
        audio: false,
      })
      streamRef.current = stream
      if (videoRef.current) {
        videoRef.current.srcObject = stream
        videoRef.current.play().catch(() => {})
      }
      setCameraOn(true)
    } catch (e) {
      setCameraError('Camera access denied — frame capture disabled.')
      setCameraOn(false)
    }
  }

  // ── WebSocket ──────────────────────────────────────────────────────────────

  const connectWebSocket = (sessionId) => {
    const client = createSessionClient({
      sessionId,
      onMood:       (data) => setMood(data),
      onAlert:      (data) => setAlerts(prev => [data, ...prev].slice(0, 20)),
      onConnect:    ()     => setWsConnected(true),
      onDisconnect: ()     => setWsConnected(false),
    })
    wsClientRef.current = client
  }

  // Helper to convert concentration from backend
  const parseConcentration = (concentration) => {
    if (typeof concentration === 'number') return concentration
    if (typeof concentration === 'string') {
      const map = { low: 0.3, medium: 0.6, high: 0.9 }
      return map[concentration.toLowerCase()] || 0.6
    }
    if (typeof concentration === 'object' && concentration?.level) {
      return parseConcentration(concentration.level)
    }
    return 0.6
  }

  // ── Frame capture loop ─────────────────────────────────────────────────────

  const startFrameCapture = useCallback((sessionId) => {
    clearInterval(frameTimer.current)
    frameTimer.current = setInterval(() => {
      const video = videoRef.current
      if (!video || !video.videoWidth) return
      const canvas = document.createElement('canvas')
      canvas.width  = video.videoWidth
      canvas.height = video.videoHeight
      canvas.getContext('2d').drawImage(video, 0, 0)
      canvas.toBlob(blob => {
        if (!blob) return
        const formData = new FormData()
        formData.append('session_id', sessionId)
        formData.append('store', 'true')
        formData.append('file', blob, 'frame.jpg')

        fetch('http://localhost:8000/analyze/frame', {
          method: 'POST',
          body: formData,
        })
          .then(r => r.ok ? r.json() : Promise.reject(r.status))
          .then(data => {
            const counts = data.emotion_counts ?? {}
            const dominant = Object.keys(counts).length > 0
              ? Object.entries(counts).sort((a, b) => b[1] - a[1])[0][0]
              : 'neutral'
            setMood({
              studentCount:    data.student_count   ?? 0,
              engagementScore: data.engagement_score ?? 0,
              dominantEmotion: dominant,
              concentration:   0.5,
            })

            // Update detected-students map from recognized faces
            const recognized = (data.people ?? []).filter(p => p.student_id)
            const physicalCount = data.student_count ?? 0

            if (recognized.length > 0) {
              setDetectedStudents(prev => {
                // Single-person lock logic
                if (physicalCount === 1 && prev.size >= 1) {
                  const locked = [...prev.values()][0]
                  const latest = recognized[0]
                  const next = new Map()
                  next.set(locked.studentId, {
                    ...locked,
                    emotion: latest.dominant_emotion ?? locked.emotion,
                    concentration: parseConcentration(latest.concentration),
                    lastSeen: Date.now(),
                  })
                  return next
                }

                // Multi-person / first detection
                const next = new Map(prev)
                recognized.forEach(p => {
                  next.set(p.student_id, {
                    studentId:   p.student_id,
                    studentName: p.student_name ?? p.student_id,
                    emotion:     p.dominant_emotion ?? 'neutral',
                    concentration: parseConcentration(p.concentration),
                    lastSeen: Date.now(),
                  })
                })
                return next
              })
            }
          })
          .catch(err => console.error('Frame send error:', err))
      }, 'image/jpeg', 0.8)
    }, FRAME_INTERVAL_MS)
  }, [])

  // ── Snapshot poll ──────────────────────────────────────────────────────────

  const startSnapshotPoll = (sessionId) => {
    clearInterval(pollTimer.current)
    const poll = () =>
      emotionService.getLatest(sessionId)
        .then(setSnapshot)
        .catch(() => {})
    poll()
    pollTimer.current = setInterval(poll, SNAPSHOT_POLL_MS)
  }

  useEffect(() => {
    if (session && cameraOn) startFrameCapture(session.sessionId)
    return () => clearInterval(frameTimer.current)
  }, [session, cameraOn, startFrameCapture])

  // ── Start session ──────────────────────────────────────────────────────────

  const handleStart = async e => {
    e.preventDefault()
    if (!form.courseId) { setSetupError('Please select a course.'); return }
    if (!form.room)     { setSetupError('Please enter a room location.'); return }
    setStarting(true)
    setSetupError('')
    const now = new Date()
    const end = new Date(now.getTime() + Number(form.duration) * 60 * 60 * 1000)
    try {
      const sess = await sessionService.start({
        courseId:       form.courseId,
        roomLocation:   form.room,
        cameraType:     'webcam',
        scheduledStart: now.toISOString().slice(0, 19),
        scheduledEnd:   end.toISOString().slice(0, 19),
      })
      activateSession(sess)
    } catch (e) {
      setSetupError(e.response?.data?.message ?? 'Failed to start session.')
    } finally {
      setStarting(false)
    }
  }

  // ── End session ────────────────────────────────────────────────────────────

  const handleEnd = async () => {
    try { await sessionService.end(session.sessionId) } catch {}
    cleanup()
    setSession(null)
    setCameraOn(false)
    setWsConnected(false)
    setMood(null)
    setAlerts([])
    setSnapshot(null)
    setDetectedStudents(new Map())
  }

  // ── Derived analytics ──────────────────────────────────────────────────────

  const engagement     = mood?.engagementScore ?? snapshot?.classSnapshot?.engagementScore ?? 0
  const dominantEmotion = mood?.dominantEmotion ?? snapshot?.classSnapshot?.dominantEmotion ?? 'neutral'
  const studentCount   = mood?.studentCount ?? session?.studentCount ?? 0
  const emoMeta        = EMOTION_META[dominantEmotion?.toLowerCase()] ?? EMOTION_META.neutral

  const concCounts = (() => {
    const students = snapshot?.studentSnapshots ?? []
    return {
      high:   students.filter(s => getConcentrationLevel(s.concentration) === 'high').length,
      medium: students.filter(s => getConcentrationLevel(s.concentration) === 'medium').length,
      low:    students.filter(s => getConcentrationLevel(s.concentration) === 'low').length,
    }
  })()

  // ── Render: loading ────────────────────────────────────────────────────────

  if (checkingActive) {
    return (
      <LecturerLayout>
        <div className="flex items-center justify-center h-64 text-slate-500 text-sm">
          Checking for active sessions…
        </div>
      </LecturerLayout>
    )
  }

  // ── Render: setup form ─────────────────────────────────────────────────────

  if (!session) {
    return (
      <LecturerLayout>
        <div className="max-w-lg mx-auto mt-10">
          <div className="flex items-center gap-3 mb-6">
            <div className="w-11 h-11 rounded-xl bg-emerald-500/15 flex items-center justify-center">
              <Video size={20} className="text-emerald-400" />
            </div>
            <div>
              <h1 className="text-2xl font-bold text-white">Start Live Session</h1>
              <p className="text-sm text-slate-500 mt-0.5">Open camera and begin real-time emotion detection</p>
            </div>
          </div>

          <div className="glass rounded-2xl p-6">
            {setupError && (
              <div className="flex items-center gap-2 px-3 py-2.5 mb-4 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400 text-sm">
                <AlertTriangle size={14} /> {setupError}
              </div>
            )}

            <form onSubmit={handleStart} className="space-y-4">
              <div>
                <label className="block text-xs font-medium text-slate-400 mb-1.5">Course</label>
                {loadingCourses ? (
                  <Skeleton className="h-10" />
                ) : (
                  <div className="relative">
                    <BookOpen size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-500 pointer-events-none" />
                    <select
                      value={form.courseId}
                      onChange={e => setForm(f => ({ ...f, courseId: e.target.value }))}
                      required
                      className="w-full pl-9 pr-8 py-2.5 rounded-xl bg-navy-900 border border-slate-700 text-sm text-slate-200 appearance-none focus:outline-none focus:border-emerald-500/50 transition-colors"
                    >
                      <option value="">Select a course…</option>
                      {courses.map(c => (
                        <option key={c.courseId} value={c.courseId}>
                          {c.code} — {c.title}
                        </option>
                      ))}
                    </select>
                    <ChevronDown size={13} className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-500 pointer-events-none" />
                  </div>
                )}
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-400 mb-1.5">Room / Location</label>
                <div className="relative">
                  <MapPin size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-500 pointer-events-none" />
                  <input
                    value={form.room}
                    onChange={e => setForm(f => ({ ...f, room: e.target.value }))}
                    placeholder="e.g. Hall B, Lab 3…"
                    required
                    className="w-full pl-9 pr-4 py-2.5 rounded-xl bg-navy-900 border border-slate-700 text-sm text-slate-200 placeholder-slate-600 focus:outline-none focus:border-emerald-500/50 transition-colors"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-400 mb-1.5">Planned Duration</label>
                <div className="relative">
                  <Clock size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-500 pointer-events-none" />
                  <select
                    value={form.duration}
                    onChange={e => setForm(f => ({ ...f, duration: e.target.value }))}
                    className="w-full pl-9 pr-8 py-2.5 rounded-xl bg-navy-900 border border-slate-700 text-sm text-slate-200 appearance-none focus:outline-none focus:border-emerald-500/50 transition-colors"
                  >
                    {[1, 1.5, 2, 2.5, 3].map(h => (
                      <option key={h} value={h}>{h} {h === 1 ? 'hour' : 'hours'}</option>
                    ))}
                  </select>
                  <ChevronDown size={13} className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-500 pointer-events-none" />
                </div>
              </div>

              <button
                type="submit"
                disabled={starting || loadingCourses}
                className="w-full py-3 rounded-xl bg-emerald-600 hover:bg-emerald-500 disabled:opacity-60 text-white font-semibold text-sm transition-colors flex items-center justify-center gap-2 mt-2"
              >
                {starting ? (
                  'Starting…'
                ) : (
                  <><Video size={15} /> Start Live Session</>
                )}
              </button>
            </form>
          </div>

          <p className="text-xs text-slate-600 text-center mt-4">
            Your browser camera will be used to detect student emotions in real time.
            Frames are analyzed every {FRAME_INTERVAL_MS / 1000}s.
          </p>
        </div>
      </LecturerLayout>
    )
  }

  // ── Render: active session ─────────────────────────────────────────────────

  return (
    <LecturerLayout>
      <div className="flex items-center justify-between mb-5">
        <div className="flex items-center gap-3 min-w-0">
          <div className="flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-rose-500/15 border border-rose-500/30 shrink-0">
            <span className="w-2 h-2 rounded-full bg-rose-500 animate-pulse" />
            <span className="text-xs font-semibold text-rose-400">LIVE</span>
          </div>
          <div className="min-w-0">
            <h1 className="text-lg font-bold text-white truncate">{session.courseName ?? 'Live Session'}</h1>
            <div className="flex items-center gap-3 text-xs text-slate-500 mt-0.5">
              <span className="flex items-center gap-1"><Clock size={11} /> {duration}</span>
              {wsConnected
                ? <span className="flex items-center gap-1 text-emerald-500"><Wifi size={11} /> Connected</span>
                : <span className="flex items-center gap-1 text-slate-600"><WifiOff size={11} /> Reconnecting…</span>
              }
              {cameraError && <span className="flex items-center gap-1 text-amber-500"><VideoOff size={11} /> {cameraError}</span>}
            </div>
          </div>
        </div>

        <button
          onClick={handleEnd}
          className="flex items-center gap-2 px-4 py-2 rounded-xl bg-rose-600/20 hover:bg-rose-600/40 border border-rose-500/30 text-rose-400 text-sm font-medium transition-all shrink-0"
        >
          <StopCircle size={15} /> End Session
        </button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-5 gap-5">
        <div className="lg:col-span-2 flex flex-col gap-4">
          <div className="glass rounded-2xl overflow-hidden aspect-video relative bg-slate-900/80">
            {cameraOn ? (
              <video
                ref={videoRef}
                autoPlay
                muted
                playsInline
                className="w-full h-full object-cover scale-x-[-1]"
              />
            ) : (
              <div className="absolute inset-0 flex flex-col items-center justify-center gap-2 text-slate-600">
                <VideoOff size={32} className="opacity-40" />
                <p className="text-xs">Camera unavailable</p>
              </div>
            )}
            {cameraOn && studentCount > 0 && (
              <div className="absolute bottom-2 left-2 flex items-center gap-1.5 px-2 py-1 rounded-lg bg-black/60 backdrop-blur-sm text-xs text-white">
                <Users size={12} className="text-emerald-400" />
                {studentCount} detected
              </div>
            )}
          </div>

          <ConcentrationBars {...concCounts} />
        </div>

        <div className="lg:col-span-3 flex flex-col gap-4">
          <div className="grid grid-cols-3 gap-3">
            <StatCard
              icon={Users}
              label="Students"
              value={studentCount}
              sub="detected"
              color="blue"
            />
            <EngagementArc value={engagement} />
            <div className={`glass rounded-2xl p-4 flex flex-col items-center justify-center gap-1 ${emoMeta.bg}`}>
              <p className="text-xs text-slate-500 font-medium">Mood</p>
              <span className="text-3xl leading-none">{emoMeta.emoji}</span>
              <span className={`text-sm font-semibold ${emoMeta.color}`}>{emoMeta.label}</span>
            </div>
          </div>

          {/* Detected Students */}
          <div className="glass rounded-2xl p-4">
            <div className="flex items-center justify-between mb-3">
              <p className="text-xs text-slate-500 font-medium flex items-center gap-1.5">
                <Users size={12} /> Detected Students
              </p>
              {detectedStudents.size > 0 && (
                <span className="text-xs px-1.5 py-0.5 rounded-full bg-violet-500/20 text-violet-300 font-medium">
                  {detectedStudents.size}
                </span>
              )}
            </div>
            {detectedStudents.size === 0 ? (
              <div className="flex flex-col items-center justify-center py-5 text-slate-700">
                <Users size={20} className="mb-1.5 opacity-30" />
                <p className="text-xs">No faces recognized yet</p>
              </div>
            ) : (
              <div className="space-y-2 max-h-52 overflow-y-auto pr-1">
                {[...detectedStudents.values()].map(s => (
                  <StudentRow key={s.studentId} student={s} />
                ))}
              </div>
            )}
          </div>

          {snapshot?.studentSnapshots?.length > 0 && (
            <div className="glass rounded-2xl p-4">
              <p className="text-xs text-slate-500 font-medium mb-3 flex items-center gap-1.5">
                <Activity size={12} /> Emotion Distribution
              </p>
              <div className="flex flex-wrap gap-2">
                {Object.entries(
                  snapshot.studentSnapshots.reduce((acc, s) => {
                    const k = s.emotion?.toLowerCase() ?? 'neutral'
                    acc[k] = (acc[k] ?? 0) + 1
                    return acc
                  }, {})
                )
                  .sort(([, a], [, b]) => b - a)
                  .map(([emotion, count]) => {
                    const m = EMOTION_META[emotion] ?? EMOTION_META.neutral
                    return (
                      <div key={emotion} className={`flex items-center gap-1.5 px-2.5 py-1.5 rounded-xl ${m.bg}`}>
                        <span className="text-sm">{m.emoji}</span>
                        <span className={`text-xs font-medium ${m.color}`}>{m.label}</span>
                        <span className="text-xs text-slate-500">{count}</span>
                      </div>
                    )
                  })}
              </div>
            </div>
          )}

          <div className="glass rounded-2xl p-4 flex-1 min-h-0">
            <p className="text-xs text-slate-500 font-medium mb-3 flex items-center gap-1.5">
              <AlertTriangle size={12} /> Live Alerts
              {alerts.length > 0 && (
                <span className="ml-auto text-xs px-1.5 py-0.5 rounded-full bg-rose-500/20 text-rose-400">{alerts.length}</span>
              )}
            </p>
            {alerts.length === 0 ? (
              <div className="flex flex-col items-center justify-center py-6 text-slate-700">
                <CheckCircle size={22} className="mb-1.5 opacity-40" />
                <p className="text-xs">No alerts yet — class is going well</p>
              </div>
            ) : (
              <div className="space-y-2 max-h-48 overflow-y-auto pr-1">
                {alerts.map((a, i) => <AlertItem key={i} alert={a} />)}
              </div>
            )}
          </div>
        </div>
      </div>
    </LecturerLayout>
  )
}