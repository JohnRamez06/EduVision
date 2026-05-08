import React, { useContext, useEffect, useState } from 'react'
import { User, Mail, Hash, BookOpen, Calendar, GraduationCap, AlertTriangle } from 'lucide-react'
import StudentLayout from '../../layouts/StudentLayout'
import { AuthContext } from '../../context/AuthContext'
import studentService from '../../services/studentService'

const Skeleton = ({ className = '' }) => (
  <div className={`animate-pulse rounded-xl bg-slate-800/60 ${className}`} />
)

function InfoRow({ icon: Icon, label, value }) {
  return (
    <div className="flex items-center gap-3 py-3.5 border-b border-slate-800/60 last:border-0">
      <div className="w-8 h-8 rounded-lg bg-slate-800 flex items-center justify-center shrink-0">
        <Icon size={15} className="text-slate-400" />
      </div>
      <div className="flex-1 min-w-0">
        <p className="text-xs text-slate-500 mb-0.5">{label}</p>
        <p className="text-sm font-medium text-slate-200 truncate">{value ?? '—'}</p>
      </div>
    </div>
  )
}

export default function StudentProfile() {
  const { user: authUser } = useContext(AuthContext)
  const [profile, setProfile] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError]     = useState('')

  useEffect(() => {
    studentService.getDashboard()
      .then(d => setProfile(d.studentInfo))
      .catch(e => setError(e.response?.data?.message ?? 'Failed to load profile.'))
      .finally(() => setLoading(false))
  }, [])

  const fullName = profile?.fullName ?? authUser?.fullName ?? 'Student'
  const initials = fullName.split(' ').map(n => n[0]).join('').slice(0, 2).toUpperCase()

  return (
    <StudentLayout>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-white flex items-center gap-2">
          <User size={22} className="text-[#667D9D]" /> My Profile
        </h1>
        <p className="text-sm text-slate-500 mt-1">Your student account information</p>
      </div>

      {error && (
        <div className="flex items-center gap-2 px-4 py-3 mb-6 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400 text-sm">
          <AlertTriangle size={15} className="shrink-0" /> {error}
        </div>
      )}

      <div className="max-w-xl space-y-5">
        {/* Avatar card */}
        <div className="glass rounded-2xl p-6 flex items-center gap-5">
          {loading ? (
            <>
              <Skeleton className="w-20 h-20 rounded-full" />
              <div className="space-y-2 flex-1">
                <Skeleton className="h-5 w-40" />
                <Skeleton className="h-3 w-56" />
              </div>
            </>
          ) : (
            <>
              <div className="w-20 h-20 rounded-full bg-gradient-to-br from-[#16254F] to-[#667D9D] flex items-center justify-center text-2xl font-bold text-white shrink-0">
                {initials}
              </div>
              <div>
                <h2 className="text-xl font-bold text-white">{fullName}</h2>
                <p className="text-sm text-slate-400 mt-0.5">{profile?.email ?? authUser?.email}</p>
                <div className="flex items-center gap-2 mt-2">
                  <span className="text-xs px-2 py-0.5 rounded-full bg-[#667D9D]/15 text-[#ACBBC6] border border-[#667D9D]/20 font-medium">
                    Student
                  </span>
                  {profile?.program && (
                    <span className="text-xs px-2 py-0.5 rounded-full bg-slate-700/60 text-slate-400 border border-slate-700">
                      {profile.program}
                    </span>
                  )}
                </div>
              </div>
            </>
          )}
        </div>

        {/* Info card */}
        <div className="glass rounded-2xl px-5 py-1">
          {loading ? (
            <div className="space-y-3 py-4">
              {[...Array(5)].map((_, i) => <Skeleton key={i} className="h-10" />)}
            </div>
          ) : (
            <>
              <InfoRow icon={User}          label="Full Name"      value={profile?.fullName ?? authUser?.fullName} />
              <InfoRow icon={Mail}          label="Email"          value={profile?.email ?? authUser?.email} />
              <InfoRow icon={Hash}          label="Student Number" value={profile?.studentNumber} />
              <InfoRow icon={BookOpen}      label="Programme"      value={profile?.program} />
              <InfoRow icon={Calendar}      label="Year of Study"  value={profile?.yearOfStudy ? `Year ${profile.yearOfStudy}` : null} />
              <InfoRow icon={GraduationCap} label="Account Role"   value="Student" />
            </>
          )}
        </div>

        <p className="text-xs text-slate-600 text-center">
          To update your profile information, contact your institution administrator.
        </p>
      </div>
    </StudentLayout>
  )
}
