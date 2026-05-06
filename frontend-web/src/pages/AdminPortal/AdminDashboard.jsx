import React, { useContext, useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Users, UserCheck, BookOpen, ShieldCheck, TrendingUp, AlertTriangle,
  Clock, ArrowRight,
} from 'lucide-react'
import AdminLayout from '../../layouts/AdminLayout'
import { AuthContext } from '../../context/AuthContext'
import adminService from '../../services/adminService'

const Skeleton = ({ className = '' }) => (
  <div className={`animate-pulse rounded-xl bg-slate-800/60 ${className}`} />
)

const greet = name => {
  const h = new Date().getHours()
  const prefix = h < 12 ? 'Good morning' : h < 17 ? 'Good afternoon' : 'Good evening'
  return `${prefix}, ${name?.split(' ')[0] ?? 'Admin'}`
}

const StatCard = ({ icon: Icon, label, value, sub, color = 'violet' }) => {
  const colors = {
    violet:  { bg: 'bg-violet-500/12',  text: 'text-violet-400',  ring: 'group-hover:ring-violet-500/20'  },
    rose:    { bg: 'bg-rose-500/12',    text: 'text-rose-400',    ring: 'group-hover:ring-rose-500/20'    },
    emerald: { bg: 'bg-emerald-500/12', text: 'text-emerald-400', ring: 'group-hover:ring-emerald-500/20' },
    blue:    { bg: 'bg-blue-500/12',    text: 'text-blue-400',    ring: 'group-hover:ring-blue-500/20'    },
    amber:   { bg: 'bg-amber-500/12',   text: 'text-amber-400',   ring: 'group-hover:ring-amber-500/20'   },
  }
  const c = colors[color]
  return (
    <div className="group glass rounded-2xl p-5 flex items-center gap-4 hover:border-slate-700/60 transition-all duration-200">
      <div className={`w-11 h-11 rounded-xl flex items-center justify-center shrink-0 ring-2 ring-transparent transition-all duration-200 ${c.bg} ${c.text} ${c.ring}`}>
        <Icon size={19} />
      </div>
      <div className="min-w-0">
        <p className="text-xs text-slate-500 font-medium mb-1">{label}</p>
        <p className="text-2xl font-bold text-white leading-none tabular-nums">{value ?? '—'}</p>
        {sub && <p className="text-[11px] text-slate-600 mt-1">{sub}</p>}
      </div>
    </div>
  )
}

const ROLE_COLORS = {
  admin:    'bg-rose-500/10 text-rose-400 border-rose-500/20',
  lecturer: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20',
  student:  'bg-blue-500/10 text-blue-400 border-blue-500/20',
}

const STATUS_COLORS = {
  active:               'bg-emerald-500/10 text-emerald-400 border-emerald-500/20',
  inactive:             'bg-slate-700/40 text-slate-400 border-slate-700/50',
  suspended:            'bg-rose-500/10 text-rose-400 border-rose-500/20',
  pending_verification: 'bg-amber-500/10 text-amber-400 border-amber-500/20',
}

export default function AdminDashboard() {
  const { user } = useContext(AuthContext)
  const navigate = useNavigate()
  const [users, setUsers]   = useState(null)
  const [roles, setRoles]   = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError]   = useState('')

  useEffect(() => {
    Promise.all([adminService.getUsers(), adminService.getRoles()])
      .then(([u, r]) => { setUsers(u); setRoles(r) })
      .catch(e => setError(e.response?.data?.message ?? 'Failed to load dashboard.'))
      .finally(() => setLoading(false))
  }, [])

  const countByRole = name =>
    users?.filter(u => u.roles?.map(r => r.toLowerCase()).includes(name)).length ?? 0

  const stats = [
    { icon: Users,       label: 'Total Users',  value: users?.length,          sub: 'registered accounts',   color: 'violet'  },
    { icon: UserCheck,   label: 'Admins',        value: countByRole('admin'),   sub: 'system administrators', color: 'rose'    },
    { icon: BookOpen,    label: 'Lecturers',     value: countByRole('lecturer'),sub: 'faculty members',       color: 'emerald' },
    { icon: ShieldCheck, label: 'Students',      value: countByRole('student'), sub: 'enrolled students',     color: 'blue'    },
    { icon: TrendingUp,  label: 'Roles Defined', value: roles?.length,          sub: 'permission groups',     color: 'amber'   },
  ]

  return (
    <AdminLayout>
      {/* Greeting */}
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-white">
          {loading ? 'Loading…' : greet(user?.fullName)}
        </h1>
        <p className="text-sm text-slate-500 mt-1">System overview — manage users, roles, and permissions.</p>
      </div>

      {error && (
        <div className="flex items-center gap-2 px-4 py-3 mb-6 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400 text-sm">
          <AlertTriangle size={15} className="shrink-0" /> {error}
        </div>
      )}

      {/* KPI row */}
      {loading ? (
        <div className="grid grid-cols-2 lg:grid-cols-5 gap-4 mb-6">
          {[...Array(5)].map((_, i) => <Skeleton key={i} className="h-24" />)}
        </div>
      ) : (
        <div className="grid grid-cols-2 lg:grid-cols-5 gap-4 mb-6">
          {stats.map(s => <StatCard key={s.label} {...s} />)}
        </div>
      )}

      {/* Recent users table */}
      <div className="glass rounded-2xl p-5">
        <div className="flex items-center justify-between mb-4">
          <h2 className="font-semibold text-white flex items-center gap-2">
            <Users size={16} className="text-violet-400" /> Recent Users
          </h2>
          <button
            onClick={() => navigate('/admin/users')}
            className="flex items-center gap-1 text-xs text-violet-400 hover:text-violet-300 transition-colors"
          >
            View all <ArrowRight size={12} />
          </button>
        </div>

        {loading ? (
          <div className="space-y-3">{[...Array(5)].map((_, i) => <Skeleton key={i} className="h-14" />)}</div>
        ) : (users ?? []).length === 0 ? (
          <div className="flex flex-col items-center justify-center h-32 text-slate-600 text-sm">
            <Users size={28} className="mb-2 opacity-30" />
            No users yet
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="text-[11px] text-slate-500 uppercase tracking-wide">
                  <th className="text-left pb-3 font-semibold pr-4">Name</th>
                  <th className="text-left pb-3 font-semibold pr-4">Email</th>
                  <th className="text-left pb-3 font-semibold pr-4">Role</th>
                  <th className="text-left pb-3 font-semibold pr-4">Status</th>
                  <th className="text-left pb-3 font-semibold hidden lg:table-cell">Joined</th>
                </tr>
              </thead>
              <tbody>
                {(users ?? []).slice(0, 8).map(u => (
                  <tr
                    key={u.id}
                    onClick={() => navigate('/admin/users')}
                    className="border-t border-slate-800/40 hover:bg-slate-800/30 transition-colors cursor-pointer"
                  >
                    <td className="py-3 pr-4">
                      <div className="flex items-center gap-3">
                        <div className="w-7 h-7 rounded-full bg-gradient-to-br from-violet-500 to-purple-600 flex items-center justify-center text-xs font-bold text-white shrink-0">
                          {(u.fullName?.[0] ?? '?').toUpperCase()}
                        </div>
                        <span className="font-medium text-slate-200 truncate max-w-[120px]">{u.fullName}</span>
                      </div>
                    </td>
                    <td className="py-3 pr-4 text-slate-400 text-xs truncate max-w-[160px]">{u.email}</td>
                    <td className="py-3 pr-4">
                      <div className="flex flex-wrap gap-1">
                        {u.roles?.map(r => (
                          <span key={r} className={`text-xs px-2 py-0.5 rounded-full border font-medium ${ROLE_COLORS[r.toLowerCase()] ?? 'bg-slate-700 text-slate-300 border-slate-600'}`}>
                            {r}
                          </span>
                        ))}
                      </div>
                    </td>
                    <td className="py-3 pr-4">
                      <span className={`text-xs px-2.5 py-0.5 rounded-full font-semibold border ${STATUS_COLORS[u.isActive ? 'active' : 'inactive']}`}>
                        {u.isActive ? 'active' : 'inactive'}
                      </span>
                    </td>
                    <td className="py-3 pr-4 text-slate-500 text-xs hidden lg:table-cell">
                      {u.createdAt ? new Date(u.createdAt).toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' }) : '—'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </AdminLayout>
  )
}
