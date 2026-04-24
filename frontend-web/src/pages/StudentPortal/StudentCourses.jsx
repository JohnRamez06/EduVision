import React, { useEffect, useState } from 'react'
import { BookOpen, Calendar, CheckCircle, AlertTriangle, ChevronRight, Clock } from 'lucide-react'
import StudentLayout from '../../layouts/StudentLayout'
import studentService from '../../services/studentService'

const Skeleton = ({ className = '' }) => (
  <div className={`animate-pulse rounded-xl bg-slate-800/60 ${className}`} />
)

function AttendanceBadge({ pct }) {
  if (pct >= 80) return <span className="text-xs font-semibold text-emerald-400 bg-emerald-500/10 px-2 py-0.5 rounded-full">Excellent</span>
  if (pct >= 60) return <span className="text-xs font-semibold text-amber-400 bg-amber-500/10 px-2 py-0.5 rounded-full">Good</span>
  return <span className="text-xs font-semibold text-rose-400 bg-rose-500/10 px-2 py-0.5 rounded-full">Low</span>
}

export default function StudentCourses() {
  const [courses, setCourses] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError]     = useState('')

  useEffect(() => {
    studentService.getCourses()
      .catch(() => studentService.getDashboard().then(d => d.enrolledCourses ?? []))
      .then(setCourses)
      .catch(e => setError(e.response?.data?.message ?? 'Failed to load courses.'))
      .finally(() => setLoading(false))
  }, [])

  return (
    <StudentLayout>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-white flex items-center gap-2">
          <BookOpen size={22} className="text-blue-400" /> My Courses
        </h1>
        <p className="text-sm text-slate-500 mt-1">Your enrolled courses and attendance progress</p>
      </div>

      {error && (
        <div className="flex items-center gap-2 px-4 py-3 mb-6 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400 text-sm">
          <AlertTriangle size={15} className="shrink-0" /> {error}
        </div>
      )}

      {loading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-5">
          {[...Array(6)].map((_, i) => <Skeleton key={i} className="h-52" />)}
        </div>
      ) : courses.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-24 text-slate-600">
          <BookOpen size={40} className="mb-3 opacity-30" />
          <p className="text-sm">You are not enrolled in any courses yet.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-5">
          {courses.map(c => {
            const attended = c.attendedSessions ?? 0
            const total    = c.totalSessions ?? 0
            const pct      = total > 0 ? Math.round((attended / total) * 100) : 0
            const barColor = pct >= 80 ? 'from-emerald-500 to-teal-500'
                           : pct >= 60 ? 'from-amber-500 to-orange-500'
                           :             'from-rose-500 to-pink-500'

            return (
              <div key={c.courseId} className="glass rounded-2xl p-5 flex flex-col gap-4 hover:border-blue-500/30 transition-colors group cursor-pointer">
                {/* Header */}
                <div className="flex items-start justify-between gap-3">
                  <div className="w-11 h-11 rounded-xl bg-blue-500/15 flex items-center justify-center shrink-0">
                    <BookOpen size={20} className="text-blue-400" />
                  </div>
                  <AttendanceBadge pct={pct} />
                </div>

                {/* Course info */}
                <div>
                  <p className="text-xs font-mono text-blue-400 mb-0.5">{c.code}</p>
                  <h3 className="font-semibold text-white leading-snug">{c.title}</h3>
                  {c.department && (
                    <p className="text-xs text-slate-500 mt-0.5">{c.department}</p>
                  )}
                </div>

                {/* Attendance bar */}
                <div>
                  <div className="flex justify-between text-xs text-slate-500 mb-1.5">
                    <span className="flex items-center gap-1"><Clock size={11} /> Attendance</span>
                    <span className="font-medium text-slate-300">{attended}/{total} sessions</span>
                  </div>
                  <div className="h-2 bg-slate-800 rounded-full overflow-hidden">
                    <div
                      className={`h-full rounded-full bg-gradient-to-r ${barColor} transition-all duration-700`}
                      style={{ width: `${pct}%` }}
                    />
                  </div>
                  <p className="text-xs text-slate-500 mt-1 text-right">{pct}% attended</p>
                </div>

                {/* Footer */}
                <div className="flex items-center justify-between pt-1 border-t border-slate-800/60">
                  <div className="flex items-center gap-3">
                    <span className="flex items-center gap-1 text-xs text-slate-500">
                      <CheckCircle size={11} className="text-emerald-500" />
                      {attended} attended
                    </span>
                    <span className="flex items-center gap-1 text-xs text-slate-500">
                      <Calendar size={11} />
                      {total} total
                    </span>
                  </div>
                  <ChevronRight size={14} className="text-slate-600 group-hover:text-blue-400 transition-colors" />
                </div>
              </div>
            )
          })}
        </div>
      )}
    </StudentLayout>
  )
}
