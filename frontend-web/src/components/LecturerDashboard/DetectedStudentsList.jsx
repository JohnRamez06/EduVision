import React, { useEffect, useState, useCallback } from 'react'
import { Users, RefreshCw } from 'lucide-react'
import lecturerService from '../../services/lecturerService'

const EMOTION_STYLE = {
  happy:     'bg-green-500/15 text-green-300',
  engaged:   'bg-blue-500/15 text-blue-300',
  neutral:   'bg-slate-500/15 text-slate-300',
  confused:  'bg-amber-500/15 text-amber-300',
  sad:       'bg-sky-500/15 text-sky-300',
  angry:     'bg-red-500/15 text-red-300',
  surprised: 'bg-yellow-500/15 text-yellow-300',
  disgusted: 'bg-purple-500/15 text-purple-300',
  fearful:   'bg-orange-500/15 text-orange-300',
}

const CONCENTRATION_STYLE = {
  high:       'bg-green-500/20 text-green-300',
  medium:     'bg-amber-500/20 text-amber-300',
  low:        'bg-red-500/20 text-red-300',
  distracted: 'bg-red-500/20 text-red-300',
}

function Avatar({ name, url }) {
  if (url) {
    return <img src={url} alt={name} className="w-9 h-9 rounded-full object-cover shrink-0" />
  }
  const initials = (name ?? '?')
    .split(' ')
    .map(p => p[0])
    .join('')
    .toUpperCase()
    .slice(0, 2)
  return (
    <div className="w-9 h-9 rounded-full bg-violet-500/20 text-violet-300 flex items-center justify-center text-xs font-bold shrink-0">
      {initials}
    </div>
  )
}

export default function DetectedStudentsList({ sessionId }) {
  const [students, setStudents] = useState([])
  const [loading, setLoading] = useState(false)
  const [lastUpdated, setLastUpdated] = useState(null)

  const refresh = useCallback(() => {
    if (!sessionId) return
    setLoading(true)
    lecturerService.getDetectedStudents(sessionId)
      .then(data => {
        setStudents(data ?? [])
        setLastUpdated(new Date())
      })
      .catch(() => {})
      .finally(() => setLoading(false))
  }, [sessionId])

  useEffect(() => {
    refresh()
    const id = setInterval(refresh, 10000)
    return () => clearInterval(id)
  }, [refresh])

  return (
    <div className="glass rounded-2xl p-5">
      <div className="flex items-center justify-between mb-4">
        <h3 className="font-semibold text-white flex items-center gap-2">
          <Users size={16} className="text-violet-400" />
          Detected Students
          {students.length > 0 && (
            <span className="text-xs px-2 py-0.5 rounded-full bg-violet-500/20 text-violet-300 font-medium">
              {students.length}
            </span>
          )}
        </h3>
        <button
          onClick={refresh}
          disabled={loading || !sessionId}
          className="text-slate-500 hover:text-slate-300 transition-colors disabled:opacity-30"
          title="Refresh"
        >
          <RefreshCw size={14} className={loading ? 'animate-spin' : ''} />
        </button>
      </div>

      <div className="space-y-2 max-h-80 overflow-y-auto pr-1 scrollbar-thin scrollbar-thumb-slate-700">
        {!sessionId ? (
          <p className="text-sm text-slate-500 text-center py-6">No active session.</p>
        ) : students.length === 0 ? (
          <p className="text-sm text-slate-500 text-center py-6">
            {loading ? 'Scanning…' : 'No faces recognized yet.'}
          </p>
        ) : students.map(student => (
          <div
            key={student.studentId}
            className="rounded-xl bg-slate-800/40 border border-slate-700/40 p-3 flex items-center gap-3"
          >
            <Avatar name={student.studentName} url={student.profilePictureUrl} />
            <div className="flex-1 min-w-0">
              <p className="text-sm font-medium text-slate-200 truncate">{student.studentName}</p>
              <span className={`inline-block mt-1 px-1.5 py-0.5 rounded text-xs capitalize ${EMOTION_STYLE[student.emotion] ?? 'bg-slate-500/15 text-slate-300'}`}>
                {student.emotion}
              </span>
            </div>
            <span className={`text-xs px-2 py-1 rounded-full capitalize shrink-0 ${CONCENTRATION_STYLE[student.concentration] ?? 'bg-slate-500/20 text-slate-300'}`}>
              {student.concentration}
            </span>
          </div>
        ))}
      </div>

      {lastUpdated && (
        <p className="text-xs text-slate-600 mt-3 text-right">
          Updated {lastUpdated.toLocaleTimeString()}
        </p>
      )}
    </div>
  )
}
