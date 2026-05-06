import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { BookOpen, AlertTriangle, ChevronRight, Clock, Activity, Calendar } from 'lucide-react'
import LecturerLayout from '../../layouts/LecturerLayout'
import lecturerService from '../../services/lecturerService'

const Skeleton = ({ className = '' }) => (
  <div className={`animate-pulse rounded-xl bg-slate-800/60 ${className}`} />
)

export default function LecturerCourses() {
  const navigate = useNavigate()
  const [courses, setCourses] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    lecturerService.getDashboard()
      .then(d => setCourses(d.courses ?? []))
      .catch(e => setError(e.response?.data?.message ?? 'Failed to load courses.'))
      .finally(() => setLoading(false))
  }, [])

  const handleManualAttendance = (courseId, courseName) => {
    console.log('Navigating to manual attendance for course:', courseId, courseName)
    navigate(`/lecturer/courses/${courseId}/manual-attendance`, { state: { courseName } })
  }

  return (
    <LecturerLayout>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-slate-800 dark:text-white flex items-center gap-2">
          <BookOpen size={22} className="text-violet-400" /> My Courses
        </h1>
        <p className="text-sm text-slate-500 mt-1">All courses you are currently teaching</p>
      </div>

      {error && (
        <div className="flex items-center gap-2 px-4 py-3 mb-6 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400 text-sm">
          <AlertTriangle size={15} className="shrink-0" /> {error}
        </div>
      )}

      {loading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-5">
          {[...Array(4)].map((_, i) => <Skeleton key={i} className="h-56" />)}
        </div>
      ) : courses.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-24 text-slate-600">
          <BookOpen size={40} className="mb-3 opacity-30" />
          <p className="text-sm">No courses assigned yet.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-5">
          {courses.map(c => (
            <div key={c.courseId} className="glass rounded-2xl p-5 flex flex-col gap-4 hover:border-violet-500/30 transition-colors group">
              <div className="flex items-start justify-between">
                <div className="w-11 h-11 rounded-xl bg-violet-500/10 flex items-center justify-center shrink-0">
                  <BookOpen size={20} className="text-violet-400" />
                </div>
                {c.activeSessions > 0 && (
                  <span className="flex items-center gap-1.5 text-xs font-semibold text-emerald-400 bg-emerald-500/10 px-2.5 py-1 rounded-full border border-emerald-500/20">
                    <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse" />
                    Live
                  </span>
                )}
              </div>

              <div>
                <p className="text-xs font-mono text-violet-400 mb-0.5">{c.code}</p>
                <h3 className="font-semibold text-slate-800 dark:text-white leading-snug">{c.title}</h3>
                {c.department && <p className="text-xs text-slate-500 mt-0.5">{c.department}</p>}
              </div>

              <div className="flex items-center gap-4 text-xs text-slate-600 dark:text-slate-500">
                <span className="flex items-center gap-1">
                  <Clock size={11} /> {c.totalSessions} sessions
                </span>
                {c.semester && <span>{c.semester}</span>}
                {c.academicYear && <span>{c.academicYear}</span>}
              </div>

              <div className="flex flex-col gap-2 pt-1 border-t border-slate-200 dark:border-slate-800/60">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-1.5">
                    <Activity size={12} className="text-slate-500 dark:text-slate-500" />
                    <span className="text-xs text-slate-600 dark:text-slate-500">
                      {c.activeSessions > 0 ? `${c.activeSessions} active` : 'No active session'}
                    </span>
                  </div>
                  <ChevronRight size={14} className="text-slate-500 group-hover:text-violet-400 transition-colors" />
                </div>
                
                {/* Action Buttons */}
                <div className="flex gap-2 mt-1">
                  <button
                    onClick={() => handleManualAttendance(c.courseId, c.title)}
                    className="flex items-center justify-center gap-1.5 px-3 py-1.5 rounded-lg bg-purple-600/20 hover:bg-purple-600/30 text-purple-400 text-xs font-medium transition-all w-full"
                  >
                    <Calendar size={12} />
                    Manual Attendance
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </LecturerLayout>
  )
}