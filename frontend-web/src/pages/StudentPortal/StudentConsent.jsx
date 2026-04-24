import React, { useContext, useEffect, useState } from 'react'
import { ShieldCheck, ShieldOff, AlertTriangle, CheckCircle, Eye, Database, Lock, Cpu } from 'lucide-react'
import StudentLayout from '../../layouts/StudentLayout'
import { AuthContext } from '../../context/AuthContext'
import studentService from '../../services/studentService'

const POLICY_ID = 'emotion-tracking-v1'

const DATA_ITEMS = [
  { icon: Eye,      label: 'Facial expressions',      desc: 'Detected anonymously via webcam during sessions' },
  { icon: Cpu,      label: 'Concentration levels',    desc: 'Computed from expression patterns in real-time' },
  { icon: Database, label: 'Session summaries',       desc: 'Aggregated emotion & engagement statistics per lecture' },
  { icon: Lock,     label: 'Personal identifiers',    desc: 'Linked to your student account for personalised insights' },
]

export default function StudentConsent() {
  const { user } = useContext(AuthContext)
  const [status, setStatus]   = useState(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving]   = useState(false)
  const [error, setError]     = useState('')
  const [success, setSuccess] = useState('')

  useEffect(() => {
    if (!user?.email) return
    // use email as studentId since consent controller uses auth.getName() (email)
    studentService.getConsentStatus(user.email)
      .then(setStatus)
      .catch(() => setStatus({ hasConsented: false }))
      .finally(() => setLoading(false))
  }, [user])

  const handleToggle = async () => {
    setSaving(true)
    setError('')
    setSuccess('')
    try {
      if (status?.hasConsented) {
        await studentService.revokeConsent(POLICY_ID)
        setStatus(s => ({ ...s, hasConsented: false, consentedAt: null }))
        setSuccess('Consent revoked. Emotion tracking has been disabled.')
      } else {
        await studentService.grantConsent(POLICY_ID)
        setStatus(s => ({ ...s, hasConsented: true, consentedAt: new Date().toISOString() }))
        setSuccess('Consent granted. Emotion tracking is now active.')
      }
    } catch (e) {
      setError(e.response?.data?.message ?? 'Failed to update consent.')
    } finally {
      setSaving(false)
    }
  }

  const consented = status?.hasConsented ?? false

  return (
    <StudentLayout>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-white flex items-center gap-2">
          <ShieldCheck size={22} className="text-emerald-400" /> Data Consent
        </h1>
        <p className="text-sm text-slate-500 mt-1">Manage your emotion-tracking data permissions</p>
      </div>

      <div className="max-w-2xl space-y-5">

        {/* Status card */}
        <div className={`glass rounded-2xl p-6 border-2 transition-colors ${
          loading ? 'border-slate-700' :
          consented ? 'border-emerald-500/30' : 'border-slate-700'
        }`}>
          {loading ? (
            <div className="animate-pulse flex gap-4">
              <div className="w-14 h-14 rounded-2xl bg-slate-800" />
              <div className="flex-1 space-y-3">
                <div className="h-4 bg-slate-800 rounded w-40" />
                <div className="h-3 bg-slate-800 rounded w-56" />
              </div>
            </div>
          ) : (
            <div className="flex items-start gap-4">
              <div className={`w-14 h-14 rounded-2xl flex items-center justify-center shrink-0 ${
                consented ? 'bg-emerald-500/15' : 'bg-slate-800'
              }`}>
                {consented
                  ? <ShieldCheck size={26} className="text-emerald-400" />
                  : <ShieldOff  size={26} className="text-slate-500" />
                }
              </div>
              <div className="flex-1">
                <p className="font-semibold text-white text-lg">
                  {consented ? 'Consent Granted' : 'No Consent Given'}
                </p>
                <p className="text-sm text-slate-400 mt-0.5">
                  {consented
                    ? `Emotion tracking is active.${status?.consentedAt ? ` Granted on ${new Date(status.consentedAt).toLocaleDateString()}.` : ''}`
                    : 'Emotion data is not being collected.'}
                </p>
                {status?.policyVersion && (
                  <p className="text-xs text-slate-600 mt-1">Policy: {status.policyVersion}</p>
                )}
              </div>
              <button
                onClick={handleToggle}
                disabled={saving}
                className={`shrink-0 px-5 py-2.5 rounded-xl text-sm font-semibold transition-all ${
                  consented
                    ? 'bg-rose-500/15 text-rose-300 border border-rose-500/25 hover:bg-rose-500/25'
                    : 'bg-emerald-500/15 text-emerald-300 border border-emerald-500/25 hover:bg-emerald-500/25'
                } disabled:opacity-50 disabled:cursor-not-allowed`}
              >
                {saving ? 'Saving…' : consented ? 'Revoke' : 'Grant Consent'}
              </button>
            </div>
          )}
        </div>

        {/* Feedback */}
        {error && (
          <div className="flex items-center gap-2 px-4 py-3 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400 text-sm">
            <AlertTriangle size={14} className="shrink-0" /> {error}
          </div>
        )}
        {success && (
          <div className="flex items-center gap-2 px-4 py-3 rounded-xl bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 text-sm">
            <CheckCircle size={14} className="shrink-0" /> {success}
          </div>
        )}

        {/* What we collect */}
        <div className="glass rounded-2xl p-5">
          <h2 className="font-semibold text-white mb-4">What data is collected?</h2>
          <div className="space-y-4">
            {DATA_ITEMS.map(({ icon: Icon, label, desc }) => (
              <div key={label} className="flex items-start gap-3">
                <div className="w-8 h-8 rounded-lg bg-blue-500/10 flex items-center justify-center shrink-0 mt-0.5">
                  <Icon size={15} className="text-blue-400" />
                </div>
                <div>
                  <p className="text-sm font-medium text-slate-200">{label}</p>
                  <p className="text-xs text-slate-500 mt-0.5">{desc}</p>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Info note */}
        <div className="flex items-start gap-3 px-4 py-3 rounded-xl bg-slate-800/40 border border-slate-700/60">
          <Lock size={14} className="text-slate-500 mt-0.5 shrink-0" />
          <p className="text-xs text-slate-500 leading-relaxed">
            Your data is processed locally and never sold to third parties. You can revoke consent at any time.
            Revoking consent stops all future data collection but does not delete historical records.
          </p>
        </div>
      </div>
    </StudentLayout>
  )
}
