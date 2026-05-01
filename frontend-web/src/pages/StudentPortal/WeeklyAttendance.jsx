import React, { useEffect, useState } from 'react'
import attendanceService from '../../services/attendanceService'

const STATUS_STYLES = {
  regular: 'bg-emerald-500/20 text-emerald-400',
  at_risk: 'bg-amber-500/20 text-amber-400',
  absent: 'bg-rose-500/20 text-rose-400',
}

export default function WeeklyAttendance({ courseId, weekId }) {
  const [data, setData] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    setLoading(true)
    const fetchData = courseId
      ? attendanceService.getCourseWeeklyAttendance(courseId, weekId)
      : attendanceService.getWeeklyAttendance(weekId)

    fetchData
      .then(setData)
      .catch((e) => setError(e.response?.data?.message || 'Failed to load attendance'))
      .finally(() => setLoading(false))
  }, [courseId, weekId])

  if (loading) {
    return (
      <div className="glass rounded-2xl p-5">
        <div className="animate-pulse space-y-3">
          <div className="h-4 bg-slate-700 rounded w-1/4" />
          <div className="h-8 bg-slate-700 rounded w-full" />
          <div className="h-8 bg-slate-700 rounded w-full" />
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="glass rounded-2xl p-5 text-center text-rose-400 text-sm">{error}</div>
    )
  }

  if (!data || data.length === 0) {
    return (
      <div className="glass rounded-2xl p-5 text-center">
        <span className="text-2xl block mb-2">📚</span>
        <p className="text-sm text-slate-500">No attendance data for this week</p>
      </div>
    )
  }

  const getBarColor = (rate) => {
    if (rate >= 75) return 'bg-emerald-500'
    if (rate >= 50) return 'bg-amber-500'
    return 'bg-rose-500'
  }

  return (
    <div className="glass rounded-2xl p-5">
      <h3 className="text-sm font-semibold text-white mb-4">📊 Weekly Attendance</h3>

      <div className="overflow-x-auto">
        <table className="w-full text-xs">
          <thead>
            <tr className="text-slate-500 border-b border-slate-700/50">
              <th className="text-left py-2 pr-3">{courseId ? 'Student' : 'Course'}</th>
              <th className="text-center py-2 px-2">Held</th>
              <th className="text-center py-2 px-2">Attended</th>
              <th className="text-center py-2 px-2">Missed</th>
              <th className="text-center py-2 px-2">Exits</th>
              <th className="text-center py-2 px-2">Rate</th>
              <th className="text-center py-2 pl-2">Status</th>
            </tr>
          </thead>
          <tbody>
            {data.map((row, i) => (
              <tr key={i} className="border-b border-slate-700/30">
                <td className="py-2.5 pr-3 text-slate-300">{row.courseName || row.course_name || `Course ${row.courseId}`}</td>
                <td className="text-center py-2.5 px-2 text-slate-400">{row.sessionsHeld ?? row.sessions_held ?? 0}</td>
                <td className="text-center py-2.5 px-2 text-emerald-400">{row.sessionsAttended ?? row.sessions_attended ?? 0}</td>
                <td className="text-center py-2.5 px-2 text-rose-400">{row.sessionsMissed ?? row.sessions_missed ?? 0}</td>
                <td className="text-center py-2.5 px-2 text-slate-400">{row.totalExits ?? row.total_exits ?? 0}</td>
                <td className="text-center py-2.5 px-2">
                  <div className="flex items-center gap-1.5">
                    <div className="flex-1 h-1.5 bg-slate-700 rounded-full overflow-hidden">
                      <div className={`h-full rounded-full ${getBarColor(row.attendanceRate ?? row.attendance_rate ?? 0)}`}
                        style={{ width: `${Math.min(100, row.attendanceRate ?? row.attendance_rate ?? 0)}%` }} />
                    </div>
                    <span className="text-slate-400 w-8">{Math.round(row.attendanceRate ?? row.attendance_rate ?? 0)}%</span>
                  </div>
                </td>
                <td className="text-center py-2.5 pl-2">
                  <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${STATUS_STYLES[row.status] || STATUS_STYLES.regular}`}>
                    {row.status === 'regular' ? '✅' : row.status === 'at_risk' ? '⚠️' : '❌'}
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}