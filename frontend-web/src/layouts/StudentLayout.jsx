import React, { useContext, useState } from 'react'
import { NavLink, useNavigate, useLocation } from 'react-router-dom'
import ThemeToggle from '../components/common/ThemeToggle'
import AlertBell from '../components/common/AlertBell'
import {
  GraduationCap, LayoutDashboard, BookOpen, BarChart3,
  Lightbulb, ShieldCheck, User, LogOut, Bell, Menu, ChevronRight,
} from 'lucide-react'
import { AuthContext } from '../context/AuthContext'
import authService from '../services/authService'

const PAGE_TITLE = {
  '/student':                  'Dashboard',
  '/student/courses':          'My Courses',
  '/student/analytics':        'Analytics',
  '/student/recommendations':  'Recommendations',
  '/student/consent':          'Privacy & Consent',
  '/student/profile':          'Profile',
}

const NAV = [
  { to: '/student',                icon: LayoutDashboard, label: 'Dashboard' },
  { to: '/student/courses',        icon: BookOpen,        label: 'My Courses' },
  { to: '/student/analytics',      icon: BarChart3,       label: 'Analytics' },
  { to: '/student/recommendations',icon: Lightbulb,       label: 'Recommendations' },
  { to: '/student/consent',        icon: ShieldCheck,     label: 'Consent' },
  { to: '/student/profile',        icon: User,            label: 'Profile' },
]

export default function StudentLayout({ children, unread = 0 }) {
  const { user, logout } = useContext(AuthContext)
  const navigate = useNavigate()
  const location = useLocation()
  const [open, setOpen] = useState(false)

  const handleLogout = async () => {
    await authService.logout()
    logout()
    navigate('/login')
  }

  const initials = user?.fullName
    ? user.fullName.split(' ').map(n => n[0]).join('').slice(0, 2).toUpperCase()
    : 'S'

  const pageTitle = PAGE_TITLE[location.pathname] ?? 'Student Portal'

  const Sidebar = ({ mobile = false }) => (
    <aside className={`sidebar flex flex-col h-full border-r transition-colors duration-200 border-slate-200/80 dark:border-slate-800/50 ${mobile ? 'w-full' : 'w-60'}`}>
      {/* Logo */}
      <div className="flex items-center gap-3 px-5 py-[18px] border-b border-slate-200/80 dark:border-slate-800/50">
        <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-[#16254F] to-[#667D9D] flex items-center justify-center shrink-0 shadow-lg shadow-[#667D9D]/20">
          <GraduationCap size={16} className="text-white" />
        </div>
        <div>
          <span className="font-bold tracking-tight text-sm block leading-none text-slate-900 dark:text-white">EduVision</span>
          <span className="text-[10px] font-medium tracking-wide uppercase mt-0.5 block text-[#667D9D]/80 dark:text-[#667D9D]/80">Student</span>
        </div>
      </div>

      {/* Nav */}
      <nav className="flex-1 px-2.5 py-4 space-y-0.5 overflow-y-auto">
        {NAV.map(({ to, icon: Icon, label }) => (
          <NavLink
            key={to}
            to={to}
            end={to === '/student'}
            onClick={() => setOpen(false)}
            className={({ isActive }) =>
              `group flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-all duration-150 cursor-pointer ${
                isActive
                  ? 'bg-[#ECECEC] dark:bg-[#667D9D]/12 text-[#16254F] dark:text-[#ACBBC6] border border-[#ACBBC6]/80 dark:border-[#667D9D]/20 shadow-sm'
                  : 'text-slate-500 dark:text-slate-400 hover:text-slate-800 dark:hover:text-slate-100 hover:bg-slate-100 dark:hover:bg-slate-800/60 border border-transparent'
              }`
            }
          >
            {({ isActive }) => (
              <>
                <span className={`shrink-0 transition-colors duration-150 ${
                  isActive
                    ? 'text-[#667D9D] dark:text-[#667D9D]'
                    : 'text-slate-400 dark:text-slate-500 group-hover:text-slate-600 dark:group-hover:text-slate-300'
                }`}>
                  <Icon size={16} />
                </span>
                <span className="flex-1">{label}</span>
                {isActive && <ChevronRight size={12} className="text-[#667D9D]/70 dark:text-[#667D9D]/60 shrink-0" />}
              </>
            )}
          </NavLink>
        ))}
      </nav>

      {/* User */}
      <div className="px-2.5 pt-3 pb-4 border-t border-slate-200/80 dark:border-slate-800/50 space-y-1">
        <div className="flex items-center gap-3 px-3 py-2.5 rounded-xl bg-slate-100 dark:bg-slate-800/30">
          <div className="w-7 h-7 rounded-full bg-gradient-to-br from-[#16254F] to-[#667D9D] flex items-center justify-center text-[10px] font-bold text-white shrink-0 ring-2 ring-[#667D9D]/20">
            {initials}
          </div>
          <div className="min-w-0 flex-1">
            <p className="text-xs font-semibold truncate leading-tight text-slate-800 dark:text-slate-200">{user?.fullName ?? 'Student'}</p>
            <p className="text-[10px] text-slate-500 truncate mt-0.5">{user?.email ?? ''}</p>
          </div>
        </div>
        <button
          onClick={handleLogout}
          className="flex items-center gap-3 w-full px-3 py-2 rounded-xl text-xs font-medium transition-all duration-150 cursor-pointer text-slate-500 hover:text-rose-600 dark:hover:text-rose-400 hover:bg-rose-50 dark:hover:bg-rose-500/8"
        >
          <LogOut size={14} />
          Sign out
        </button>
      </div>
    </aside>
  )

  return (
    <div className="flex h-screen overflow-hidden" style={{ backgroundColor: 'var(--bg-app)' }}>
      <div className="hidden md:flex">
        <Sidebar />
      </div>

      {open && (
        <div className="fixed inset-0 z-50 flex md:hidden">
          <div className="fixed inset-0 bg-black/60 backdrop-blur-sm" onClick={() => setOpen(false)} />
          <div className="relative w-64 h-full z-10">
            <Sidebar mobile />
          </div>
        </div>
      )}

      <div className="flex-1 flex flex-col min-w-0 overflow-hidden">
        <header
          className="flex items-center justify-between px-5 py-3 border-b backdrop-blur-md shrink-0 transition-colors duration-200 border-slate-200/80 dark:border-slate-800/50"
          style={{ backgroundColor: 'var(--bg-header)' }}
        >
          <div className="flex items-center gap-3">
            <button
              className="md:hidden transition-colors cursor-pointer text-slate-500 dark:text-slate-400 hover:text-slate-800 dark:hover:text-white"
              onClick={() => setOpen(true)}
              aria-label="Open menu"
            >
              <Menu size={20} />
            </button>
            <h2 className="text-sm font-semibold hidden md:block text-slate-700 dark:text-slate-200">{pageTitle}</h2>
          </div>
          <div className="flex items-center gap-2">
            <button
              className="relative w-8 h-8 rounded-lg flex items-center justify-center transition-all cursor-pointer text-slate-400 hover:text-slate-700 dark:hover:text-white hover:bg-slate-200/70 dark:hover:bg-slate-800/70"
              aria-label="Notifications"
            >
              <Bell size={16} />
              {unread > 0 && (
                <span className="absolute top-1 right-1 w-1.5 h-1.5 rounded-full bg-rose-500" />
              )}
            </button>
            <div className="w-8 h-8 rounded-full bg-gradient-to-br from-[#16254F] to-[#667D9D] flex items-center justify-center text-[11px] font-bold text-white ring-2 ring-transparent hover:ring-[#667D9D]/30 transition-all cursor-pointer">
              {initials}
            </div>
          </div>
        </header>

        <main className="flex-1 overflow-y-auto p-5 md:p-6">
          {children}
        </main>
      </div>
    </div>
  )
}
