import React, { useContext, useEffect, useState } from 'react'
import { User, Mail, Hash, BookOpen, MapPin, Zap, AlertTriangle } from 'lucide-react'
import LecturerLayout from '../../layouts/LecturerLayout'
import { AuthContext } from '../../context/AuthContext'
import lecturerService from '../../services/lecturerService'

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

export default function LecturerProfile() {
  const { user: authUser } = useContext(AuthContext)
  const [profile, setProfile] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError]     = useState('')

  useEffect(() => {
    lecturerService.getDashboard()
      .then(d => setProfile(d.profile))
      .catch(e => setError(e.response?.data?.message ?? 'Failed to load profile.'))
      .finally(() => setLoading(false))
  }, [])

  const fullName = profile?.fullName ?? authUser?.fullName ?? 'Lecturer'
  const initials = fullName.split(' ').map(n => n[0]).join('').slice(0, 2).toUpperCase()

  return (
    <LecturerLayout>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-white flex items-center gap-2">
          <User size={22} className="text-emerald-400" /> My Profile
        </h1>
        <p className="text-sm text-slate-500 mt-1">Your lecturer account information</p>
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
              <div className="space-y-2 flex-1"><Skeleton className="h-5 w-40" /><Skeleton className="h-3 w-56" /></div>
            </>
          ) : (
            <>
              <div className="w-20 h-20 rounded-full bg-gradient-to-br from-emerald-500 to-teal-600 flex items-center justify-center text-2xl font-bold text-white shrink-0">
                {initials}
              </div>
              <div>
                <h2 className="text-xl font-bold text-white">{fullName}</h2>
                <p className="text-sm text-slate-400 mt-0.5">{profile?.email ?? authUser?.email}</p>
                <div className="flex items-center gap-2 mt-2 flex-wrap">
                  <span className="text-xs px-2 py-0.5 rounded-full bg-emerald-500/15 text-emerald-300 border border-emerald-500/20 font-medium">Lecturer</span>
                  {profile?.department && (
                    <span className="text-xs px-2 py-0.5 rounded-full bg-slate-700/60 text-slate-400 border border-slate-700">{profile.department}</span>
                  )}
                </div>
              </div>
            </>
          )}
        </div>

        {/* Info card */}
        <div className="glass rounded-2xl px-5 py-1">
          {loading ? (
            <div className="space-y-3 py-4">{[...Array(5)].map((_, i) => <Skeleton key={i} className="h-10" />)}</div>
          ) : (
            <>
              <InfoRow icon={User}    label="Full Name"       value={profile?.fullName ?? authUser?.fullName} />
              <InfoRow icon={Mail}    label="Email"           value={profile?.email ?? authUser?.email} />
              <InfoRow icon={Hash}    label="Employee ID"     value={profile?.employeeId} />
              <InfoRow icon={BookOpen} label="Department"     value={profile?.department} />
              <InfoRow icon={Zap}     label="Specialization"  value={profile?.specialization} />
              <InfoRow icon={MapPin}  label="Office Location" value={profile?.officeLocation} />
            </>
          )}
        </div>

        <p className="text-xs text-slate-600 text-center">
          To update your profile information, contact your institution administrator.
        </p>
      </div>
    </LecturerLayout>
  )
}
