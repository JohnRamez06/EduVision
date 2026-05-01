import React, { useEffect, useState } from 'react'
import attendanceService from '../../services/attendanceService'

export default function AttendanceTracker({ studentId, sessionId }) {
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!studentId || !sessionId) return
    setLoading(true)
    attendanceService.getStudentSessionAttendance(studentId, sessionId)
      .then(setData)
      .catch((e) => setError(e.response?.data?.message || 'Failed to load attendance'))
      .finally(() => setLoading(false))
  }, [studentId, sessionId])

  if (loading) {
    return (
      <div className="glass rounded-2xl p-5">
        <div className="animate-pulse space-y-3">
          <div className="h-4 bg-slate-700 rounded w-1/3" />
          <div className="h-3 bg-slate-700 rounded w-2/3" />
          <div className="h-3 bg-slate-700 rounded w-1/2" />
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="glass rounded-2xl p-5 text-center text-rose-400 text-sm">{error}</div>
    )
  }

  if (!data) {
    return (
      <div className="glass rounded-2xl p-5 text-center text-slate-500 text-sm">
        No attendance data available
      </div>
    )
  }

  const formatTime = (dt) => {
    if (!dt) return 'N/A'
    return new Date(dt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  }

  return (
    <div className="glass rounded-2xl p-5">
      <h3 className="text-sm font-semibold text-white mb-4">
        🕐 Session Attendance
      </h3>

      <div className="space-y-3">
        <div className="flex items-center justify-between">
          <span className="text-xs text-slate-400">Status</span>
          <span className={`text-xs px-2.5 py-1 rounded-full font-medium ${
            data.present || data.status === 'present'
              ? 'bg-emerald-500/20 text-emerald-400'
              : data.status === 'late'
              ? 'bg-amber-500/20 text-amber-400'
              : 'bg-rose-500/20 text-rose-400'
          }`}>
            {data.present ? '✅ Present' : data.status || '❌ Absent'}
          </span>
        </div>

        {data.joinedAt && (
          <div className="flex items-center justify-between">
            <span className="text-xs text-slate-400">Joined</span>
            <span className="text-xs text-slate-300">{formatTime(data.joinedAt)}</span>
          </div>
        )}

        {data.leftAt && (
          <div className="flex items-center justify-between">
            <span className="text-xs text-slate-400">Left</span>
            <span className="text-xs text-slate-300">{formatTime(data.leftAt)}</span>
          </div>
        )}

        <div className="flex items-center justify-between pt-2 border-t border-slate-700/50">
          <span className="text-xs text-slate-400">🚪 Exits</span>
          <span className={`text-sm font-bold ${(data.totalExits || 0) > 2 ? 'text-rose-400' : 'text-slate-300'}`}>
            {data.totalExits || 0}
          </span>
        </div>
      </div>

      {data.exits && data.exits.length > 0 && (
        <div className="mt-4 pt-3 border-t border-slate-700/50">
          <p className="text-xs text-slate-500 mb-2">🔄 Exit Timeline</p>
          <div className="space-y-2 max-h-32 overflow-y-auto">
            {data.exits.map((exit, i) => (
              <div key={i} className="flex items-center justify-between text-xs bg-slate-800/40 rounded-lg px-3 py-2">
                <div className="text-slate-400">
                  <span>{formatTime(exit.exitTime)}</span>
                  {exit.returnTime && <span className="mx-1">→</span>}
                  {exit.returnTime && <span>{formatTime(exit.returnTime)}</span>}
                  {!exit.returnTime && <span className="text-rose-400 ml-1">(not returned)</span>}
                </div>
                <span className="text-slate-500">
                  {exit.durationMinutes != null ? `${exit.durationMinutes} min` : '—'}
                </span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}