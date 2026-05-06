import React, { useContext, useState } from 'react'
import { NavLink, useNavigate, useLocation } from 'react-router-dom'
import AlertBell from '../components/common/AlertBell'
import ThemeToggle from '../components/common/ThemeToggle'
import {
  GraduationCap, LayoutDashboard, BookOpen, Clock,
  User, LogOut, Menu, Video, FileText, ChevronRight,
} from 'lucide-react'
import { AuthContext } from '../context/AuthContext'
import authService from '../services/authService'

const NAV = [
  { to: '/lecturer',          icon: LayoutDashboard, label: 'Dashboard',    end: true  },
  { to: '/lecturer/courses',  icon: BookOpen,        label: 'My Courses',   end: false },
  { to: '/lecturer/live',     icon: Video,           label: 'Live Session', end: false },
  { to: '/lecturer/sessions', icon: Clock,           label: 'Sessions',     end: false },
  { to: '/lecturer/reports',  icon: FileText,        label: 'Reports',      end: false },
  { to: '/lecturer/profile',  icon: User,            label: 'Profile',      end: false },
]

const PAGE_TITLE = {
  '/lecturer':          'Dashboard',
  '/lecturer/courses':  'My Courses',
  '/lecturer/live':     'Live Session',
  '/lecturer/sessions': 'Sessions',
  '/lecturer/reports':  'Reports',
  '/lecturer/profile':  'Profile',
}

export default function LecturerLayout({ children, unread = 0 }) {
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
    : 'L'

  const pageTitle = PAGE_TITLE[location.pathname] ?? 'Lecturer Portal'

  const Sidebar = ({ mobile = false }) => (
    <aside className={`sidebar flex flex-col h-full border-r transition-colors duration-200 border-slate-200/80 dark:border-slate-800/50 ${mobile ? 'w-full' : 'w-60'}`}>
      {/* Logo */}
      <div className="flex items-center gap-3 px-5 py-[18px] border-b border-slate-200/80 dark:border-slate-800/50">
        <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-violet-500 to-purple-600 flex items-center justify-center shrink-0 shadow-lg shadow-violet-500/20">
          <GraduationCap size={16} className="text-white" />
        </div>
        <div>
          <span className="font-bold tracking-tight text-sm block leading-none text-slate-900 dark:text-white">EduVision</span>
          <span className="text-[10px] font-medium tracking-wide uppercase mt-0.5 block text-violet-600/80 dark:text-violet-400/80">Lecturer</span>
        </div>
      </div>

      {/* Nav */}
      <nav className="flex-1 px-2.5 py-4 space-y-0.5 overflow-y-auto">
        {NAV.map(({ to, icon: Icon, label, end }) => (
          <NavLink
            key={to}
            to={to}
            end={end}
            onClick={() => setOpen(false)}
            className={({ isActive }) =>
              `group flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-all duration-150 cursor-pointer ${
                isActive
                  ? 'bg-violet-100 dark:bg-violet-500/20 text-violet-800 dark:text-violet-100 border border-violet-200/90 dark:border-violet-400/40 shadow-sm'
                  : 'text-slate-500 dark:text-slate-400 hover:text-slate-800 dark:hover:text-slate-100 hover:bg-slate-100 dark:hover:bg-slate-800/60 border border-transparent'
              }`
            }
          >
            {({ isActive }) => (
              <>
                <span className={`shrink-0 transition-colors duration-150 ${
                  isActive
                    ? 'text-violet-700 dark:text-violet-200'
                    : 'text-slate-400 dark:text-slate-500 group-hover:text-slate-600 dark:group-hover:text-slate-300'
                }`}>
                  <Icon size={16} />
                </span>
                <span className="flex-1">{label}</span>
                {isActive && <ChevronRight size={12} className="text-violet-600/80 dark:text-violet-200/80 shrink-0" />}
              </>
            )}
          </NavLink>
        ))}
      </nav>

      {/* User */}
      <div className="px-2.5 pt-3 pb-4 border-t border-slate-200/80 dark:border-slate-800/50 space-y-1">
        <div className="flex items-center gap-3 px-3 py-2.5 rounded-xl bg-slate-100 dark:bg-slate-800/30">
          <div className="w-7 h-7 rounded-full bg-gradient-to-br from-violet-500 to-purple-600 flex items-center justify-center text-[10px] font-bold text-white shrink-0 ring-2 ring-violet-500/20">
            {initials}
          </div>
          <div className="min-w-0 flex-1">
            <p className="text-xs font-semibold truncate leading-tight text-slate-800 dark:text-slate-200">{user?.fullName ?? 'Lecturer'}</p>
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
    <div className="flex h-screen overflow-hidden transition-colors duration-200" style={{ backgroundColor: 'var(--bg-app)' }}>
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
        {/* Topbar */}
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
          <div className="flex items-center gap-1">
            <ThemeToggle />
            <AlertBell />
            <div className="w-8 h-8 rounded-full bg-gradient-to-br from-violet-500 to-purple-600 flex items-center justify-center text-[11px] font-bold text-white ring-2 ring-transparent hover:ring-violet-500/30 transition-all cursor-pointer ml-1">
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
