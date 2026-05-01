import React, { useEffect, useState, useRef } from 'react'
import notificationService from '../../services/notificationService'
import { useNavigate } from 'react-router-dom'

export default function NotificationBell() {
  const [unreadCount, setUnreadCount] = useState(0)
  const [notifications, setNotifications] = useState([])
  const [showDropdown, setShowDropdown] = useState(false)
  const dropdownRef = useRef(null)
  const navigate = useNavigate()

  useEffect(() => {
    fetchUnread()
    const interval = setInterval(fetchUnread, 30000)
    return () => clearInterval(interval)
  }, [])

  useEffect(() => {
    function handleClickOutside(event) {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setShowDropdown(false)
      }
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  const fetchUnread = async () => {
    try {
      const data = await notificationService.getUnread()
      setUnreadCount(data.length)
      setNotifications(data.slice(0, 5))
    } catch (e) {
      console.error('Failed to fetch notifications:', e)
    }
  }

  const handleMarkRead = async (id) => {
    try {
      await notificationService.markRead(id)
      setUnreadCount((prev) => Math.max(0, prev - 1))
      setNotifications((prev) => prev.filter((n) => (n.notification_id || n.id) !== id))
    } catch (e) {
      console.error('Failed to mark as read:', e)
    }
  }

  const formatTimeAgo = (dt) => {
    if (!dt) return ''
    const diff = Date.now() - new Date(dt).getTime()
    const mins = Math.floor(diff / 60000)
    if (mins < 1) return 'Just now'
    if (mins < 60) return `${mins}m ago`
    const hours = Math.floor(mins / 60)
    if (hours < 24) return `${hours}h ago`
    return `${Math.floor(hours / 24)}d ago`
  }

  return (
    <div className="relative" ref={dropdownRef}>
      <button
        onClick={() => setShowDropdown(!showDropdown)}
        className="relative p-2 rounded-lg hover:bg-slate-800 transition-colors"
      >
        <span className="text-lg">🔔</span>
        {unreadCount > 0 && (
          <span className="absolute -top-0.5 -right-0.5 w-5 h-5 rounded-full bg-rose-500 text-white text-xs flex items-center justify-center font-bold">
            {unreadCount > 9 ? '9+' : unreadCount}
          </span>
        )}
      </button>

      {showDropdown && (
        <div className="absolute right-0 top-12 w-80 bg-slate-900 border border-slate-700 rounded-2xl shadow-2xl z-50 overflow-hidden">
          <div className="p-3 border-b border-slate-700/50">
            <div className="flex items-center justify-between">
              <h4 className="text-sm font-semibold text-white">Notifications</h4>
              {unreadCount > 0 && (
                <span className="text-xs text-slate-500">{unreadCount} unread</span>
              )}
            </div>
          </div>

          <div className="max-h-64 overflow-y-auto">
            {notifications.length === 0 ? (
              <div className="py-8 text-center text-slate-600">
                <span className="text-2xl block mb-2">🔔</span>
                <p className="text-xs">No new notifications</p>
              </div>
            ) : (
              notifications.map((n) => (
                <button
                  key={n.notification_id || n.id}
                  onClick={() => handleMarkRead(n.notification_id || n.id)}
                  className={`w-full text-left p-3 hover:bg-slate-800/60 transition-colors border-b border-slate-700/30 ${
                    n.status === 'unread' ? 'border-l-2 border-l-blue-500' : ''
                  }`}
                >
                  <p className="text-xs font-medium text-slate-200 truncate">{n.title}</p>
                  <p className="text-xs text-slate-500 truncate mt-0.5">{n.message}</p>
                  <p className="text-xs text-slate-600 mt-1">{formatTimeAgo(n.created_at || n.createdAt)}</p>
                </button>
              ))
            )}
          </div>

          <div className="p-2 border-t border-slate-700/50">
            <button
              onClick={() => { setShowDropdown(false); navigate('/notifications') }}
              className="w-full text-center text-xs text-blue-400 hover:text-blue-300 py-1.5 transition-colors"
            >
              View All
            </button>
          </div>
        </div>
      )}
    </div>
  )
}