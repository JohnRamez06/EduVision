import React, { useEffect, useState } from 'react'
import attendanceService from '../../services/attendanceService'

const EXIT_TYPE_STYLES = {
  bathroom_break: 'bg-[#667D9D]/20 text-[#667D9D]',
  left_early: 'bg-rose-500/20 text-rose-400',
  technical_issue: 'bg-amber-500/20 text-amber-400',
  unknown: 'bg-slate-500/20 text-slate-400',
}

export default function StudentExitList({ sessionId }) {
  const [exits, setExits] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (!sessionId) return
    setLoading(true)
    attendanceService.getSessionExits(sessionId)
      .then(setExits)
      .catch(console.error)
      .finally(() => setLoading(false))
  }, [sessionId])

  const formatTime = (dt) => {
    if (!dt) return '—'
    return new Date(dt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  }

  if (loading) {
    return (
      <div className="glass rounded-2xl p-5">
        <div className="animate-pulse space-y-3">
          <div className="h-4 bg-slate-700 rounded w-1/3" />
          <div className="h-8 bg-slate-700 rounded w-full" />
        </div>
      </div>
    )
  }

  return (
    <div className="glass rounded-2xl p-5">
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-sm font-semibold text-white">🚪 Student Exits</h3>
        {exits.length > 0 && (
          <span className="text-xs px-2 py-0.5 rounded-full bg-slate-700 text-slate-400">
            {exits.length} total
          </span>
        )}
      </div>

      {exits.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-4 text-slate-600">
          <span className="text-lg mb-1 opacity-40">⚠️</span>
          <p className="text-xs">No students left during this session</p>
        </div>
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full text-xs">
            <thead>
              <tr className="text-slate-500 border-b border-slate-700/50">
                <th className="text-left py-2 pr-3">Student</th>
                <th className="text-center py-2 px-2">Exit Time</th>
                <th className="text-center py-2 px-2">Return Time</th>
                <th className="text-center py-2 px-2">Duration</th>
                <th className="text-center py-2 pl-2">Type</th>
              </tr>
            </thead>
            <tbody>
              {exits.map((exit, i) => (
                <tr key={i} className={`border-b border-slate-700/30 ${!exit.returnTime ? 'bg-rose-500/5' : ''}`}>
                  <td className="py-2.5 pr-3 text-slate-300">
                    {exit.studentName || exit.student_name || 'Unknown'}
                    {!exit.returnTime && <span className="text-rose-400 text-xs ml-1">(not returned)</span>}
                  </td>
                  <td className="text-center py-2.5 px-2 text-slate-400">{formatTime(exit.exitTime)}</td>
                  <td className="text-center py-2.5 px-2 text-slate-400">{formatTime(exit.returnTime)}</td>
                  <td className="text-center py-2.5 px-2 text-slate-400">
                    {exit.durationMinutes != null ? `${exit.durationMinutes}m` : '—'}
                  </td>
                  <td className="text-center py-2.5 pl-2">
                    <span className={`text-xs px-2 py-0.5 rounded-full ${EXIT_TYPE_STYLES[exit.exitType] || EXIT_TYPE_STYLES.unknown}`}>
                      {(exit.exitType || 'unknown').replace('_', ' ')}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}