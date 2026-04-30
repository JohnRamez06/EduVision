import React, { useEffect, useState } from 'react'
import { Users } from 'lucide-react'
import adminService from '../../services/adminService'

export default function UserManagement() {
  const [users, setUsers] = useState([])

  useEffect(() => {
    adminService.getUsers().then(setUsers).catch(() => setUsers([]))
  }, [])

  return (
    <div className="glass rounded-2xl p-5">
      <h3 className="font-semibold text-white mb-4 flex items-center gap-2"><Users size={16} className="text-violet-400" /> User Management</h3>
      <div className="space-y-2">
        {users.length === 0 ? <div className="text-sm text-slate-600 py-8 text-center">No users available.</div> : users.map((user) => (
          <div key={user.id} className="rounded-xl bg-navy-900/50 border border-slate-800/60 p-3 flex items-center justify-between gap-3">
            <div>
              <p className="text-sm font-medium text-slate-200">{user.fullName}</p>
              <p className="text-xs text-slate-500 mt-1">{user.email}</p>
            </div>
            <span className="text-xs px-2 py-1 rounded-full bg-slate-800 text-slate-300">{(user.roles ?? []).join(', ')}</span>
          </div>
        ))}
      </div>
    </div>
  )
}