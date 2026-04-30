import React from 'react'
import { BookOpen } from 'lucide-react'

export default function MyCoursesList({ courses = [] }) {
  return (
    <div className="glass rounded-2xl p-5">
      <h3 className="font-semibold text-white mb-4 flex items-center gap-2"><BookOpen size={16} className="text-blue-400" /> My Courses</h3>
      <div className="space-y-3">
        {courses.length === 0 ? <div className="text-sm text-slate-600 py-8 text-center">No courses available.</div> : courses.map((course) => (
          <div key={course.courseId} className="rounded-xl bg-navy-900/50 border border-slate-800/60 p-3">
            <p className="text-sm font-medium text-slate-200">{course.title ?? course.code}</p>
            <p className="text-xs text-slate-500 mt-1">{course.code}</p>
            <div className="mt-2 h-1.5 rounded-full bg-slate-800 overflow-hidden">
              <div className="h-full rounded-full bg-gradient-to-r from-blue-500 to-violet-500" style={{ width: `${Math.min(100, Math.round(((course.attendedSessions ?? 0) / Math.max(1, course.totalSessions ?? 1)) * 100))}%` }} />
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}