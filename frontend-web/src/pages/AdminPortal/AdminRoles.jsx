import React, { useEffect, useState } from 'react'
import {
  ShieldCheck, Lock, ChevronDown, ChevronUp, AlertTriangle,
  Pencil, Check, X, Search,
} from 'lucide-react'
import AdminLayout from '../../layouts/AdminLayout'
import adminService from '../../services/adminService'

const Skeleton = ({ className = '' }) => (
  <div className={`animate-pulse rounded-xl bg-slate-800/60 ${className}`} />
)

const ROLE_STYLES = {
  admin:    { text: 'text-rose-400',    border: 'border-rose-500/20',    icon: 'bg-rose-500/15'    },
  lecturer: { text: 'text-emerald-400', border: 'border-emerald-500/20', icon: 'bg-emerald-500/15' },
  student:  { text: 'text-[#667D9D]',    border: 'border-[#667D9D]/20',    icon: 'bg-[#667D9D]/15'    },
}
const DEFAULT_STYLE = { text: 'text-[#667D9D]', border: 'border-[#667D9D]/20', icon: 'bg-[#667D9D]/15' }

// Group permissions by resource for cleaner UI
const groupByResource = perms =>
  perms.reduce((acc, p) => {
    const key = p.resource ?? 'other'
    acc[key] = acc[key] ? [...acc[key], p] : [p]
    return acc
  }, {})

export default function AdminRoles() {
  const [roles, setRoles]               = useState([])
  const [allPerms, setAllPerms]         = useState([])
  const [rolePerms, setRolePerms]       = useState({})   // roleId -> Permission[]
  const [expanded, setExpanded]         = useState(null)
  const [loadingPerms, setLoadingPerms] = useState(null)
  const [loading, setLoading]           = useState(true)
  const [error, setError]               = useState('')
  const [editModal, setEditModal]       = useState(null)  // role object | null

  useEffect(() => {
    Promise.all([adminService.getRoles(), adminService.getAllPermissions()])
      .then(([r, p]) => { setRoles(r); setAllPerms(p) })
      .catch(e => setError(e.response?.data?.message ?? 'Failed to load.'))
      .finally(() => setLoading(false))
  }, [])

  const loadRolePerms = async roleId => {
    if (rolePerms[roleId]) return
    setLoadingPerms(roleId)
    try {
      const perms = await adminService.getRolePermissions(roleId)
      setRolePerms(prev => ({ ...prev, [roleId]: perms }))
    } catch {
      setError('Failed to load permissions for this role.')
    } finally {
      setLoadingPerms(null)
    }
  }

  const toggleExpand = async roleId => {
    if (expanded === roleId) { setExpanded(null); return }
    setExpanded(roleId)
    await loadRolePerms(roleId)
  }

  const openEdit = async role => {
    await loadRolePerms(role.id)
    setEditModal(role)
  }

  const onSaved = (roleId, updatedPerms) => {
    setRolePerms(prev => ({ ...prev, [roleId]: updatedPerms }))
    setEditModal(null)
  }

  return (
    <AdminLayout>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-white">Roles & Permissions</h1>
        <p className="text-sm text-slate-500 mt-1">
          {loading ? '…' : `${roles.length} role${roles.length !== 1 ? 's' : ''} · ${allPerms.length} permissions available`}
        </p>
      </div>

      {error && (
        <div className="flex items-center gap-2 px-4 py-3 mb-4 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400 text-sm">
          <AlertTriangle size={15} className="shrink-0" /> {error}
          <button onClick={() => setError('')} className="ml-auto text-rose-400/60 hover:text-rose-400">
            <X size={14} />
          </button>
        </div>
      )}

      {loading ? (
        <div className="space-y-3">{[...Array(3)].map((_, i) => <Skeleton key={i} className="h-20" />)}</div>
      ) : (
        <div className="space-y-3">
          {roles.map(role => {
            const style    = ROLE_STYLES[role.name?.toLowerCase()] ?? DEFAULT_STYLE
            const isOpen   = expanded === role.id
            const perms    = rolePerms[role.id] ?? []
            const isLoadingThis = loadingPerms === role.id
            const grouped  = groupByResource(perms)

            return (
              <div
                key={role.id}
                className={`rounded-2xl border overflow-hidden bg-navy-900/60 backdrop-blur-sm ${style.border}`}
              >
                {/* Header */}
                <div className="flex items-center gap-4 px-5 py-4">
                  {/* Expand trigger */}
                  <button
                    onClick={() => toggleExpand(role.id)}
                    className="flex items-center gap-4 flex-1 min-w-0 text-left"
                  >
                    <div className={`w-10 h-10 rounded-xl flex items-center justify-center shrink-0 ${style.icon}`}>
                      <ShieldCheck size={18} className={style.text} />
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2 mb-0.5">
                        <span className={`font-semibold text-base capitalize ${style.text}`}>{role.name}</span>
                        {role.isSystem && (
                          <span className="text-xs px-1.5 py-0.5 rounded bg-slate-700/60 text-slate-400">system</span>
                        )}
                        {!role.isActive && (
                          <span className="text-xs px-1.5 py-0.5 rounded bg-rose-500/10 text-rose-400">inactive</span>
                        )}
                      </div>
                      <p className="text-sm text-slate-500 truncate">{role.description ?? 'No description'}</p>
                    </div>
                  </button>

                  {/* Actions */}
                  <div className="flex items-center gap-2 shrink-0">
                    <span className="text-xs text-slate-600 hidden sm:block">
                      {isOpen && perms.length > 0 ? `${perms.length} perm${perms.length !== 1 ? 's' : ''}` : ''}
                    </span>
                    <button
                      onClick={() => openEdit(role)}
                      className="p-1.5 rounded-lg text-slate-500 hover:text-[#667D9D] hover:bg-[#667D9D]/10 transition-all"
                      title="Edit permissions"
                    >
                      <Pencil size={14} />
                    </button>
                    <button
                      onClick={() => toggleExpand(role.id)}
                      className="p-1.5 rounded-lg text-slate-500 hover:text-slate-300 hover:bg-slate-800 transition-all"
                    >
                      {isOpen ? <ChevronUp size={16} /> : <ChevronDown size={16} />}
                    </button>
                  </div>
                </div>

                {/* Permissions panel */}
                {isOpen && (
                  <div className="px-5 pb-5 pt-4 border-t border-slate-800/60">
                    <div className="flex items-center justify-between mb-3">
                      <h4 className="text-xs font-medium text-slate-500 flex items-center gap-1.5">
                        <Lock size={11} /> Permissions
                      </h4>
                      <button
                        onClick={() => openEdit(role)}
                        className="text-xs text-[#667D9D] hover:text-[#ACBBC6] transition-colors flex items-center gap-1"
                      >
                        <Pencil size={10} /> Edit
                      </button>
                    </div>

                    {isLoadingThis ? (
                      <div className="flex gap-2 flex-wrap">
                        {[...Array(5)].map((_, i) => <Skeleton key={i} className="h-7 w-28" />)}
                      </div>
                    ) : perms.length === 0 ? (
                      <p className="text-xs text-slate-600 italic">No permissions assigned.</p>
                    ) : (
                      <div className="space-y-3">
                        {Object.entries(grouped).map(([resource, items]) => (
                          <div key={resource}>
                            <p className="text-xs text-slate-600 font-medium uppercase tracking-wide mb-1.5">{resource}</p>
                            <div className="flex flex-wrap gap-1.5">
                              {items.map(p => (
                                <span
                                  key={p.id}
                                  title={p.description}
                                  className="text-xs px-2.5 py-1 rounded-lg bg-slate-800 border border-slate-700/60 text-slate-300 font-mono"
                                >
                                  {p.action}
                                </span>
                              ))}
                            </div>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                )}
              </div>
            )
          })}
        </div>
      )}

      {editModal && (
        <EditPermissionsModal
          role={editModal}
          allPerms={allPerms}
          currentPerms={rolePerms[editModal.id] ?? []}
          onClose={() => setEditModal(null)}
          onSaved={onSaved}
        />
      )}
    </AdminLayout>
  )
}

// ─── Edit permissions modal ───────────────────────────────────────────────────

function EditPermissionsModal({ role, allPerms, currentPerms, onClose, onSaved }) {
  const style = ROLE_STYLES[role.name?.toLowerCase()] ?? DEFAULT_STYLE

  const [selected, setSelected] = useState(() => new Set(currentPerms.map(p => p.id)))
  const [search, setSearch]     = useState('')
  const [saving, setSaving]     = useState(false)
  const [err, setErr]           = useState('')

  const grouped = groupByResource(
    allPerms.filter(p =>
      search === '' ||
      p.name.toLowerCase().includes(search.toLowerCase()) ||
      p.resource.toLowerCase().includes(search.toLowerCase())
    )
  )

  const toggle = id =>
    setSelected(prev => {
      const next = new Set(prev)
      next.has(id) ? next.delete(id) : next.add(id)
      return next
    })

  const toggleResource = (items) => {
    const ids = items.map(p => p.id)
    const allChecked = ids.every(id => selected.has(id))
    setSelected(prev => {
      const next = new Set(prev)
      ids.forEach(id => allChecked ? next.delete(id) : next.add(id))
      return next
    })
  }

  const handleSave = async () => {
    setSaving(true)
    setErr('')
    try {
      const updated = await adminService.updateRolePermissions(role.id, [...selected])
      onSaved(role.id, updated)
    } catch (e) {
      setErr(e.response?.data?.message ?? 'Save failed.')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div className="fixed inset-0 bg-black/60 backdrop-blur-sm" onClick={onClose} />
      <div className="relative glass rounded-2xl w-full max-w-lg z-10 flex flex-col max-h-[85vh]">

        {/* Modal header */}
        <div className="flex items-center justify-between p-5 border-b border-slate-800/60 shrink-0">
          <div>
            <h3 className="font-semibold text-white flex items-center gap-2">
              <ShieldCheck size={16} className={style.text} />
              <span className={`capitalize ${style.text}`}>{role.name}</span>
              <span className="text-slate-400 font-normal">— Edit Permissions</span>
            </h3>
            <p className="text-xs text-slate-500 mt-0.5">{selected.size} of {allPerms.length} selected</p>
          </div>
          <button onClick={onClose} className="text-slate-500 hover:text-white transition-colors">
            <X size={18} />
          </button>
        </div>

        {/* Search */}
        <div className="px-5 pt-4 pb-2 shrink-0">
          <div className="relative">
            <Search size={13} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-500 pointer-events-none" />
            <input
              value={search}
              onChange={e => setSearch(e.target.value)}
              placeholder="Filter permissions…"
              className="w-full pl-8 pr-4 py-2 rounded-xl bg-navy-900 border border-slate-700 text-sm text-slate-200 placeholder-slate-600 focus:outline-none focus:border-[#667D9D]/50 transition-colors"
            />
          </div>
        </div>

        {err && (
          <div className="mx-5 mb-2 flex items-center gap-2 px-3 py-2 rounded-lg bg-rose-500/10 border border-rose-500/20 text-rose-400 text-xs shrink-0">
            <AlertTriangle size={12} /> {err}
          </div>
        )}

        {/* Permission list */}
        <div className="flex-1 overflow-y-auto px-5 py-3 space-y-4">
          {Object.keys(grouped).length === 0 ? (
            <p className="text-sm text-slate-600 text-center py-6">No permissions match your filter.</p>
          ) : Object.entries(grouped).map(([resource, items]) => {
            const allChecked  = items.every(p => selected.has(p.id))
            const someChecked = items.some(p => selected.has(p.id))
            return (
              <div key={resource}>
                {/* Resource group header with select-all */}
                <button
                  onClick={() => toggleResource(items)}
                  className="flex items-center gap-2 mb-2 group w-full"
                >
                  <div className={`w-4 h-4 rounded border flex items-center justify-center transition-colors shrink-0 ${
                    allChecked  ? 'bg-[#16254F] border-[#16254F]' :
                    someChecked ? 'bg-[#16254F]/40 border-[#667D9D]' :
                                  'border-slate-600 bg-transparent'
                  }`}>
                    {(allChecked || someChecked) && <Check size={10} className="text-white" strokeWidth={3} />}
                  </div>
                  <span className="text-xs font-semibold uppercase tracking-wider text-slate-400 group-hover:text-slate-200 transition-colors">
                    {resource}
                  </span>
                  <span className="text-xs text-slate-600 ml-1">
                    {items.filter(p => selected.has(p.id)).length}/{items.length}
                  </span>
                </button>

                {/* Individual permissions */}
                <div className="space-y-1 pl-1">
                  {items.map(p => (
                    <button
                      key={p.id}
                      onClick={() => toggle(p.id)}
                      className="flex items-center gap-3 w-full px-3 py-2 rounded-xl hover:bg-slate-800/50 transition-colors text-left group"
                    >
                      <div className={`w-4 h-4 rounded border flex items-center justify-center transition-colors shrink-0 ${
                        selected.has(p.id)
                          ? 'bg-[#16254F] border-[#16254F]'
                          : 'border-slate-600 group-hover:border-slate-400'
                      }`}>
                        {selected.has(p.id) && <Check size={10} className="text-white" strokeWidth={3} />}
                      </div>
                      <div className="flex-1 min-w-0">
                        <span className="text-sm font-mono text-slate-200">{p.name}</span>
                        {p.description && (
                          <span className="text-xs text-slate-500 ml-2">{p.description}</span>
                        )}
                      </div>
                    </button>
                  ))}
                </div>
              </div>
            )
          })}
        </div>

        {/* Footer */}
        <div className="flex gap-3 p-5 border-t border-slate-800/60 shrink-0">
          <button
            onClick={onClose}
            className="flex-1 py-2.5 rounded-xl border border-slate-700 text-slate-300 text-sm hover:bg-slate-800 transition-colors"
          >
            Cancel
          </button>
          <button
            onClick={handleSave}
            disabled={saving}
            className="flex-1 py-2.5 rounded-xl bg-[#16254F] hover:bg-[#667D9D] disabled:opacity-60 text-white text-sm font-medium transition-colors flex items-center justify-center gap-2"
          >
            {saving ? 'Saving…' : <><Check size={14} /> Save Permissions</>}
          </button>
        </div>
      </div>
    </div>
  )
}
