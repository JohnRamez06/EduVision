import React, { useEffect, useRef, useState } from 'react'
import {
  Users, Plus, Search, Pencil, Trash2, X, AlertTriangle, Check, ChevronDown, Camera,
} from 'lucide-react'
import AdminLayout from '../../layouts/AdminLayout'
import adminService from '../../services/adminService'

const Skeleton = ({ className = '' }) => (
  <div className={`animate-pulse rounded-xl bg-slate-800/60 ${className}`} />
)

const ROLE_COLORS = {
  admin:    'bg-rose-500/10 text-rose-400 border-rose-500/20',
  lecturer: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20',
  student:  'bg-blue-500/10 text-blue-400 border-blue-500/20',
}

const STATUS_COLORS = {
  active:               'bg-emerald-500/10 text-emerald-400',
  inactive:             'bg-slate-700/40 text-slate-400',
  suspended:            'bg-rose-500/10 text-rose-400',
  pending_verification: 'bg-amber-500/10 text-amber-400',
}

// Reusable avatar — shows photo if available, otherwise gradient initials
function Avatar({ src, name, size = 32, className = '' }) {
  const [imgErr, setImgErr] = useState(false)
  const initial = (name?.[0] ?? '?').toUpperCase()

  if (src && !imgErr) {
    return (
      <img
        src={src}
        alt={name}
        style={{ width: size, height: size }}
        className={`rounded-full object-cover shrink-0 ${className}`}
        onError={() => setImgErr(true)}
      />
    )
  }
  return (
    <div
      style={{ width: size, height: size, fontSize: size * 0.38 }}
      className={`rounded-full bg-gradient-to-br from-violet-500 to-purple-600 flex items-center justify-center font-bold text-white shrink-0 ${className}`}
    >
      {initial}
    </div>
  )
}

export default function AdminUsers() {
  const [users, setUsers]           = useState([])
  const [roles, setRoles]           = useState([])
  const [loading, setLoading]       = useState(true)
  const [error, setError]           = useState('')
  const [search, setSearch]         = useState('')
  const [modal, setModal]           = useState(null)     // null | { mode: 'create'|'edit', user? }
  const [delConfirm, setDelConfirm] = useState(null)     // userId

  const load = () => {
    setLoading(true)
    Promise.all([adminService.getUsers(), adminService.getRoles()])
      .then(([u, r]) => { setUsers(u); setRoles(r) })
      .catch(e => setError(e.response?.data?.message ?? 'Failed to load.'))
      .finally(() => setLoading(false))
  }

  useEffect(load, [])

  const filtered = users.filter(u => {
    const q = search.toLowerCase()
    return (
      u.email?.toLowerCase().includes(q) ||
      u.fullName?.toLowerCase().includes(q) ||
      u.roles?.some(r => r.toLowerCase().includes(q))
    )
  })

  const handleDelete = async id => {
    try {
      await adminService.deleteUser(id)
      setUsers(prev => prev.filter(u => u.id !== id))
      setDelConfirm(null)
    } catch (e) {
      setError(e.response?.data?.message ?? 'Delete failed.')
    }
  }

  return (
    <AdminLayout>
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-white">User Management</h1>
          <p className="text-sm text-slate-500 mt-1">
            {loading ? '…' : `${users.length} total users`}
          </p>
        </div>
        <button
          onClick={() => setModal({ mode: 'create' })}
          className="flex items-center gap-2 px-4 py-2.5 rounded-xl bg-violet-600 hover:bg-violet-500 text-white text-sm font-medium transition-colors"
        >
          <Plus size={15} /> Add User
        </button>
      </div>

      {error && (
        <div className="flex items-center gap-2 px-4 py-3 mb-4 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400 text-sm">
          <AlertTriangle size={15} className="shrink-0" /> {error}
          <button onClick={() => setError('')} className="ml-auto text-rose-400/60 hover:text-rose-400">
            <X size={14} />
          </button>
        </div>
      )}

      {/* Search */}
      <div className="relative mb-5">
        <Search size={14} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-500 pointer-events-none" />
        <input
          value={search}
          onChange={e => setSearch(e.target.value)}
          placeholder="Search by name, email, or role…"
          className="w-full pl-9 pr-4 py-2.5 rounded-xl bg-navy-900 border border-slate-800 text-sm text-slate-200 placeholder-slate-600 focus:outline-none focus:border-violet-500/50 transition-colors"
        />
      </div>

      {/* Table */}
      <div className="glass rounded-2xl overflow-hidden">
        {loading ? (
          <div className="p-5 space-y-3">{[...Array(6)].map((_, i) => <Skeleton key={i} className="h-14" />)}</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="border-b border-slate-800">
                <tr className="text-xs text-slate-500">
                  <th className="text-left px-5 py-3.5 font-medium">User</th>
                  <th className="text-left px-5 py-3.5 font-medium">Email</th>
                  <th className="text-left px-5 py-3.5 font-medium">Role</th>
                  <th className="text-left px-5 py-3.5 font-medium">Status</th>
                  <th className="text-left px-5 py-3.5 font-medium hidden lg:table-cell">Joined</th>
                  <th className="text-right px-5 py-3.5 font-medium">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60">
                {filtered.length === 0 ? (
                  <tr>
                    <td colSpan={6} className="px-5 py-14 text-center text-slate-600">
                      <Users size={28} className="mx-auto mb-2 opacity-30" />
                      No users found
                    </td>
                  </tr>
                ) : filtered.map(u => (
                  <tr key={u.id} className="hover:bg-slate-800/20 transition-colors">
                    <td className="px-5 py-3.5">
                      <div className="flex items-center gap-3">
                        <Avatar src={u.profilePictureUrl} name={u.fullName} size={32} />
                        <span className="font-medium text-slate-200">{u.fullName}</span>
                      </div>
                    </td>
                    <td className="px-5 py-3.5 text-slate-400 text-xs">{u.email}</td>
                    <td className="px-5 py-3.5">
                      <div className="flex flex-wrap gap-1">
                        {u.roles?.map(r => (
                          <span
                            key={r}
                            className={`text-xs px-2 py-0.5 rounded-full border font-medium ${ROLE_COLORS[r.toLowerCase()] ?? 'bg-slate-700 text-slate-300 border-slate-600'}`}
                          >
                            {r}
                          </span>
                        ))}
                      </div>
                    </td>
                    <td className="px-5 py-3.5">
                      <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${STATUS_COLORS[u.isActive ? 'active' : 'inactive']}`}>
                        {u.isActive ? 'active' : 'inactive'}
                      </span>
                    </td>
                    <td className="px-5 py-3.5 text-slate-500 text-xs hidden lg:table-cell">
                      {u.createdAt
                        ? new Date(u.createdAt).toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' })
                        : '—'}
                    </td>
                    <td className="px-5 py-3.5 text-right">
                      <div className="flex items-center justify-end gap-1">
                        <button
                          onClick={() => setModal({ mode: 'edit', user: u })}
                          className="p-1.5 rounded-lg text-slate-500 hover:text-violet-400 hover:bg-violet-500/10 transition-all"
                          title="Edit"
                        >
                          <Pencil size={14} />
                        </button>
                        <button
                          onClick={() => setDelConfirm(u.id)}
                          className="p-1.5 rounded-lg text-slate-500 hover:text-rose-400 hover:bg-rose-500/10 transition-all"
                          title="Delete"
                        >
                          <Trash2 size={14} />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Create / Edit Modal */}
      {modal && (
        <UserModal
          mode={modal.mode}
          user={modal.user}
          roles={roles}
          onClose={() => setModal(null)}
          onSaved={() => { setModal(null); load() }}
        />
      )}

      {/* Delete confirmation */}
      {delConfirm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div className="fixed inset-0 bg-black/60 backdrop-blur-sm" onClick={() => setDelConfirm(null)} />
          <div className="relative glass rounded-2xl p-6 w-full max-w-sm z-10">
            <h3 className="text-lg font-semibold text-white mb-2">Delete user?</h3>
            <p className="text-sm text-slate-400 mb-5">
              The account will be soft-deleted. The user will no longer be able to log in.
            </p>
            <div className="flex gap-3">
              <button
                onClick={() => setDelConfirm(null)}
                className="flex-1 py-2.5 rounded-xl border border-slate-700 text-slate-300 text-sm hover:bg-slate-800 transition-colors"
              >
                Cancel
              </button>
              <button
                onClick={() => handleDelete(delConfirm)}
                className="flex-1 py-2.5 rounded-xl bg-rose-600 hover:bg-rose-500 text-white text-sm font-medium transition-colors"
              >
                Delete
              </button>
            </div>
          </div>
        </div>
      )}
    </AdminLayout>
  )
}

// ─── Create / Edit modal ─────────────────────────────────────────────────────

function UserModal({ mode, user, roles, onClose, onSaved }) {
  const [form, setForm] = useState({
    firstName: user?.fullName?.split(' ')[0] ?? '',
    lastName:  user?.fullName?.split(' ').slice(1).join(' ') ?? '',
    email:     user?.email   ?? '',
    password:  '',
    roleName:  user?.roles?.[0] ?? '',
    isActive:  user?.isActive ?? true,
  })

  // Picture state — picDataUrl holds a compressed base64 JPEG data URL
  const [picDataUrl, setPicDataUrl] = useState(user?.profilePictureUrl ?? null)
  const [picFileName, setPicFileName] = useState(null)
  const fileInputRef = useRef(null)

  const [saving, setSaving] = useState(false)
  const [err, setErr]       = useState('')

  const set = (k, v) => setForm(f => ({ ...f, [k]: v }))

  // Compress the picked image to max 256×256 px JPEG via canvas (~10-30 KB)
  const compressImage = file => new Promise(resolve => {
    const img = new Image()
    const url = URL.createObjectURL(file)
    img.onload = () => {
      const MAX = 256
      const scale = Math.min(MAX / img.width, MAX / img.height, 1)
      const w = Math.round(img.width  * scale)
      const h = Math.round(img.height * scale)
      const canvas = document.createElement('canvas')
      canvas.width  = w
      canvas.height = h
      canvas.getContext('2d').drawImage(img, 0, 0, w, h)
      URL.revokeObjectURL(url)
      resolve(canvas.toDataURL('image/jpeg', 0.88))
    }
    img.src = url
  })

  const handleFileChange = async e => {
    const file = e.target.files?.[0]
    if (!file) return
    setPicFileName(file.name)
    const dataUrl = await compressImage(file)
    setPicDataUrl(dataUrl)
  }

  const handleSubmit = async e => {
    e.preventDefault()
    setSaving(true)
    setErr('')
    try {
      // Determine whether the picture changed (skip sending if unchanged)
      const picPayload = picDataUrl !== (user?.profilePictureUrl ?? null)
        ? picDataUrl
        : undefined

      if (mode === 'create') {
        await adminService.createUser({
          firstName:          form.firstName,
          lastName:           form.lastName,
          email:              form.email,
          password:           form.password,
          roleName:           form.roleName.toUpperCase(),
          profilePictureUrl:  picPayload,
        })
      } else {
        await adminService.updateUser(user.id, {
          firstName:          form.firstName,
          lastName:           form.lastName,
          roleName:           form.roleName ? form.roleName.toUpperCase() : undefined,
          isActive:           form.isActive,
          profilePictureUrl:  picPayload,
        })
      }
      onSaved()
    } catch (e) {
      setErr(e.response?.data?.message ?? 'Save failed.')
    } finally {
      setSaving(false)
    }
  }

  const displayName = form.firstName
    ? `${form.firstName} ${form.lastName}`.trim()
    : (user?.fullName ?? '')

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div className="fixed inset-0 bg-black/60 backdrop-blur-sm" onClick={onClose} />
      <div className="relative glass rounded-2xl p-6 w-full max-w-md z-10 max-h-[90vh] overflow-y-auto">
        <div className="flex items-center justify-between mb-5">
          <h3 className="text-lg font-semibold text-white">
            {mode === 'create' ? 'Add User' : 'Edit User'}
          </h3>
          <button onClick={onClose} className="text-slate-500 hover:text-white transition-colors">
            <X size={18} />
          </button>
        </div>

        {err && (
          <div className="flex items-center gap-2 px-3 py-2 mb-4 rounded-lg bg-rose-500/10 border border-rose-500/20 text-rose-400 text-xs">
            <AlertTriangle size={12} /> {err}
          </div>
        )}

        {/* ── Avatar picker ─────────────────────────────────────────────── */}
        <div className="flex flex-col items-center mb-6">
          <div className="relative group cursor-pointer" onClick={() => fileInputRef.current?.click()}>
            {/* Avatar circle — shows photo or gradient initials */}
            {picDataUrl ? (
              <img
                src={picDataUrl}
                alt="Profile preview"
                className="w-24 h-24 rounded-full object-cover ring-2 ring-violet-500/40"
              />
            ) : (
              <div className="w-24 h-24 rounded-full bg-gradient-to-br from-violet-500 to-purple-600 flex items-center justify-center text-3xl font-bold text-white ring-2 ring-violet-500/20">
                {(displayName?.[0] ?? '?').toUpperCase()}
              </div>
            )}

            {/* Camera overlay on hover */}
            <div className="absolute inset-0 rounded-full bg-black/50 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity">
              <Camera size={22} className="text-white" />
            </div>
          </div>

          <button
            type="button"
            onClick={() => fileInputRef.current?.click()}
            className="mt-2 text-xs text-violet-400 hover:text-violet-300 transition-colors"
          >
            {picDataUrl ? 'Change photo' : 'Add photo'}
          </button>

          {/* Hidden file input */}
          <input
            ref={fileInputRef}
            type="file"
            accept="image/*"
            className="hidden"
            onChange={handleFileChange}
          />

          {picFileName && (
            <span className="mt-1 text-xs text-slate-500 truncate max-w-[200px]">
              {picFileName}
            </span>
          )}
        </div>

        {/* ── Form fields ───────────────────────────────────────────────── */}
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-2 gap-3">
            <Field label="First Name" value={form.firstName} onChange={v => set('firstName', v)} required />
            <Field label="Last Name"  value={form.lastName}  onChange={v => set('lastName', v)}  required />
          </div>

          <Field
            label="Email"
            type="email"
            value={form.email}
            onChange={v => set('email', v)}
            required
            disabled={mode === 'edit'}
          />

          {mode === 'create' && (
            <Field
              label="Password"
              type="password"
              value={form.password}
              onChange={v => set('password', v)}
              required
              placeholder="Min. 8 characters"
            />
          )}

          {/* Role selector */}
          <div>
            <label className="block text-xs font-medium text-slate-400 mb-1.5">Role</label>
            <div className="relative">
              <select
                value={form.roleName}
                onChange={e => set('roleName', e.target.value)}
                required
                className="w-full px-3 py-2.5 rounded-xl bg-navy-900 border border-slate-700 text-sm text-slate-200 appearance-none focus:outline-none focus:border-violet-500/50 transition-colors"
              >
                <option value="">Select role…</option>
                {roles.map(r => (
                  <option key={r.id} value={r.name}>{r.name}</option>
                ))}
              </select>
              <ChevronDown size={14} className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-500 pointer-events-none" />
            </div>
          </div>

          {/* Active toggle (edit only) */}
          {mode === 'edit' && (
            <button
              type="button"
              onClick={() => set('isActive', !form.isActive)}
              className="flex items-center gap-3 w-full"
            >
              <div className={`w-10 h-6 rounded-full transition-colors relative flex items-center ${form.isActive ? 'bg-violet-600' : 'bg-slate-700'}`}>
                <span className={`absolute w-4 h-4 rounded-full bg-white shadow transition-transform ${form.isActive ? 'translate-x-5' : 'translate-x-1'}`} />
              </div>
              <span className="text-sm text-slate-300">Account active</span>
            </button>
          )}

          <div className="flex gap-3 pt-1">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 py-2.5 rounded-xl border border-slate-700 text-slate-300 text-sm hover:bg-slate-800 transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={saving}
              className="flex-1 py-2.5 rounded-xl bg-violet-600 hover:bg-violet-500 disabled:opacity-60 text-white text-sm font-medium transition-colors flex items-center justify-center gap-2"
            >
              {saving ? 'Saving…' : <><Check size={14} /> {mode === 'create' ? 'Create' : 'Save'}</>}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

function Field({ label, type = 'text', value, onChange, required, disabled, placeholder }) {
  return (
    <div>
      <label className="block text-xs font-medium text-slate-400 mb-1.5">{label}</label>
      <input
        type={type}
        value={value}
        onChange={e => onChange(e.target.value)}
        required={required}
        disabled={disabled}
        placeholder={placeholder}
        className="w-full px-3 py-2.5 rounded-xl bg-navy-900 border border-slate-700 text-sm text-slate-200 placeholder-slate-600 focus:outline-none focus:border-violet-500/50 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
      />
    </div>
  )
}
