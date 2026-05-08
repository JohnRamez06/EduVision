import React, { useState, useEffect } from 'react'
import alertService from '../../services/alertService'

const SEVERITY_STYLES = {
  critical: 'border-red-500 bg-red-500/10 text-red-300',
  warning: 'border-amber-500 bg-amber-500/10 text-amber-300',
  info: 'border-[#667D9D] bg-[#667D9D]/10 text-[#ACBBC6]',
}

export default function AlertBanner({ alerts = [], onClear }) {
  const [visibleAlerts, setVisibleAlerts] = useState([])

  useEffect(() => {
    if (alerts.length > 0) {
      setVisibleAlerts(alerts.slice(0, 3))
    }
  }, [alerts])

  const handleDismiss = async (alert) => {
    try {
      const alertId = alert.id || alert.alert_id || alert.alertId
      await alertService.acknowledge(alertId)
    } catch (e) {
      console.error('Failed to acknowledge alert:', e)
    }
    setVisibleAlerts((prev) => prev.filter((a) => a !== alert))
    if (visibleAlerts.length <= 1 && onClear) onClear()
  }

  if (visibleAlerts.length === 0) {
    return (
      <div className="glass rounded-2xl p-4">
        <div className="flex flex-col items-center justify-center py-3 text-slate-600">
          <span className="text-lg mb-1 opacity-40">✅</span>
          <p className="text-xs">No alerts yet — class is going well</p>
        </div>
      </div>
    )
  }

  return (
    <div className="space-y-2">
      {visibleAlerts.map((alert, i) => {
        const severity = alert.severity || 'info'
        const style = SEVERITY_STYLES[severity] || SEVERITY_STYLES.info
        const icon = severity === 'critical' || severity === 'warning' ? '⚠️' : '✅'

        return (
          <div key={alert.id || alert.alert_id || alert.alertId || i}
            className={`flex items-start gap-3 px-4 py-3 rounded-xl border text-sm ${style}`}>
            <span className="shrink-0 mt-0.5">{icon}</span>
            <div className="flex-1 min-w-0">
              <p className="font-semibold text-xs mb-0.5">{alert.title || alert.alert_type || 'Alert'}</p>
              <p className="text-xs opacity-80">{alert.message || alert.description || ''}</p>
            </div>
            <button onClick={() => handleDismiss(alert)} className="shrink-0 opacity-60 hover:opacity-100 text-lg leading-none">
              ×
            </button>
          </div>
        )
      })}
    </div>
  )
}