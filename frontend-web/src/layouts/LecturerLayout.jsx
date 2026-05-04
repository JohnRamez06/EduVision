import React, { useContext, useState } from 'react'
import { NavLink, useNavigate } from 'react-router-dom'
import AlertBell from '../components/common/AlertBell';

import {
  GraduationCap, LayoutDashboard, BookOpen, Clock,
  User, LogOut, Bell, Menu, Video,
} from 'lucide-react'
import { AuthContext } from '../context/AuthContext'
import authService from '../services/authService'

const NAV = [
  { to: '/lecturer',              icon: LayoutDashboard, label: 'Dashboard',    end: true  },
  { to: '/lecturer/courses',      icon: BookOpen,        label: 'My Courses',   end: false },
  { to: '/lecturer/live',         icon: Video,           label: 'Live Session', end: false },
  { to: '/lecturer/sessions',     icon: Clock,           label: 'Sessions',     end: false },
  { to: '/lecturer/profile',      icon: User,            label: 'Profile',      end: false },
]

export default function LecturerLayout({ children, unread = 0 }) {
  const { user, logout } = useContext(AuthContext)
  const navigate = useNavigate()
  const [open, setOpen] = useState(false)

  const handleLogout = async () => {
    await authService.logout()
    logout()
    navigate('/login')
  }

  const initials = user?.fullName
    ? user.fullName.split(' ').map(n => n[0]).join('').slice(0, 2).toUpperCase()
    : 'L'

  const Sidebar = ({ mobile = false }) => (
    <aside className={`flex flex-col h-full bg-navy-900 border-r border-slate-800/60 ${mobile ? 'w-full' : 'w-60'}`}>
      {/* Logo */}
      <div className="flex items-center gap-2.5 px-5 py-5 border-b border-slate-800/60">
        <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-emerald-500 to-teal-600 flex items-center justify-center shrink-0">
          <GraduationCap size={16} className="text-white" />
        </div>
        <div>
          <span className="font-bold text-white tracking-tight block leading-none">EduVision</span>
          <span className="text-[10px] text-emerald-400 font-medium">Lecturer</span>
        </div>
      </div>

      {/* Nav */}
      <nav className="flex-1 px-3 py-4 space-y-0.5 overflow-y-auto">
        {NAV.map(({ to, icon: Icon, label, end }) => (
          <NavLink
            key={to}
            to={to}
            end={end}
            onClick={() => setOpen(false)}
            className={({ isActive }) =>
              `flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-all duration-150 ${
                isActive
                  ? 'bg-emerald-500/15 text-emerald-300 border border-emerald-500/20'
                  : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/50'
              }`
            }
          >
            <Icon size={16} />
            {label}
          </NavLink>
        ))}
      </nav>

      {/* User */}
      <div className="px-3 py-4 border-t border-slate-800/60">
        <div className="flex items-center gap-3 px-3 py-2 rounded-xl mb-1">
          <div className="w-8 h-8 rounded-full bg-gradient-to-br from-emerald-500 to-teal-600 flex items-center justify-center text-xs font-bold text-white shrink-0">
            {initials}
          </div>
          <div className="min-w-0">
            <p className="text-sm font-medium text-slate-200 truncate">{user?.fullName ?? 'Lecturer'}</p>
            <p className="text-xs text-slate-500 truncate">{user?.email ?? ''}</p>
          </div>
        </div>
        <button
          onClick={handleLogout}
          className="flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm text-slate-500 hover:text-rose-400 hover:bg-rose-500/10 transition-all w-full"
        >
          <LogOut size={15} />
          Sign out
        </button>
      </div>
    </aside>
  )

  return (
    <div className="flex h-screen bg-navy-950 overflow-hidden">
      {/* Desktop sidebar */}
      <div className="hidden md:flex">
        <Sidebar />
      </div>

      {/* Mobile drawer */}
      {open && (
        <div className="fixed inset-0 z-50 flex md:hidden">
          <div className="fixed inset-0 bg-black/60 backdrop-blur-sm" onClick={() => setOpen(false)} />
          <div className="relative w-64 h-full z-10">
            <Sidebar mobile />
          </div>
        </div>
      )}

      {/* Main */}
      <div className="flex-1 flex flex-col min-w-0 overflow-hidden">
        {/* Topbar */}
        <header className="flex items-center justify-between px-5 py-3.5 border-b border-slate-800/60 bg-navy-900/80 backdrop-blur-sm shrink-0">
          <button className="md:hidden text-slate-400 hover:text-white" onClick={() => setOpen(true)}>
            <Menu size={20} />
          </button>
          <div className="hidden md:block" />
          <div className="flex items-center gap-3">
            <AlertBell />
            <button className="relative w-9 h-9 rounded-xl flex items-center justify-center text-slate-400 hover:text-white hover:bg-slate-800 transition-all">
              <Bell size={17} />
              {unread > 0 && (
                <span className="absolute top-1.5 right-1.5 w-2 h-2 rounded-full bg-rose-500" />
              )}
            </button>
            <div className="w-8 h-8 rounded-full bg-gradient-to-br from-emerald-500 to-teal-600 flex items-center justify-center text-xs font-bold text-white">
              {initials}
            </div>
          </div>
        </header>

        {/* Content */}
        <main className="flex-1 overflow-y-auto bg-navy-950 p-5 md:p-7">
          {children}
        </main>
      </div>
    </div>
  )
}
