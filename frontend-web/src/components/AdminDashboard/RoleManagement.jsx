import React, { useEffect, useState } from 'react'
import { ShieldCheck } from 'lucide-react'
import adminService from '../../services/adminService'

export default function RoleManagement() {
  const [roles, setRoles] = useState([])

  useEffect(() => {
    adminService.getRoles().then(setRoles).catch(() => setRoles([]))
  }, [])

  return (
    <div className="glass rounded-2xl p-5">
      <h3 className="font-semibold text-white mb-4 flex items-center gap-2"><ShieldCheck size={16} className="text-violet-400" /> Role Management</h3>
      <div className="space-y-2">
        {roles.length === 0 ? <div className="text-sm text-slate-600 py-8 text-center">No roles available.</div> : roles.map((role) => (
          <div key={role.id} className="rounded-xl bg-navy-900/50 border border-slate-800/60 p-3">
            <p className="text-sm font-medium text-slate-200">{role.name}</p>
            <p className="text-xs text-slate-500 mt-1">{role.description ?? 'No description'}</p>
          </div>
        ))}
      </div>
    </div>
  )
}