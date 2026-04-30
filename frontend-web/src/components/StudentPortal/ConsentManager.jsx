import React, { useState } from 'react'
import { ShieldCheck, ShieldOff } from 'lucide-react'
import consentService from '../../services/consentService'

export default function ConsentManager({ status, policyId = 'emotion-tracking-v1', onChange }) {
  const [saving, setSaving] = useState(false)

  const consented = Boolean(status?.hasConsented)

  const toggle = async () => {
    setSaving(true)
    try {
      if (consented) {
        await consentService.revokeConsent(policyId)
      } else {
        await consentService.grantConsent(policyId)
      }
      onChange?.()
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="glass rounded-2xl p-5">
      <div className="flex items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <div className={`w-10 h-10 rounded-xl flex items-center justify-center ${consented ? 'bg-emerald-500/15 text-emerald-400' : 'bg-slate-800 text-slate-500'}`}>
            {consented ? <ShieldCheck size={18} /> : <ShieldOff size={18} />}
          </div>
          <div>
            <p className="text-sm font-semibold text-white">{consented ? 'Consent granted' : 'Consent not granted'}</p>
            <p className="text-xs text-slate-500 mt-1">{policyId}</p>
          </div>
        </div>
        <button
          onClick={toggle}
          disabled={saving}
          className={`px-4 py-2.5 rounded-xl text-sm font-medium transition-colors ${consented ? 'bg-rose-500/15 text-rose-300' : 'bg-emerald-500/15 text-emerald-300'} disabled:opacity-60`}
        >
          {saving ? 'Saving…' : consented ? 'Revoke' : 'Grant'}
        </button>
      </div>
    </div>
  )
}