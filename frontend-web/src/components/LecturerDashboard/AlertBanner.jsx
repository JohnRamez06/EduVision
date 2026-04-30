import React from 'react'
import { AlertTriangle, Info, ShieldAlert } from 'lucide-react'

export default function AlertBanner({ alert }) {
	if (!alert) {
		return null
	}

	const tone = alert.severity === 'critical'
		? 'border-rose-500/30 bg-rose-500/10 text-rose-300'
		: alert.severity === 'warning'
			? 'border-amber-500/30 bg-amber-500/10 text-amber-300'
			: 'border-blue-500/30 bg-blue-500/10 text-blue-300'

	const Icon = alert.severity === 'critical'
		? ShieldAlert
		: alert.severity === 'warning'
			? AlertTriangle
			: Info

	return (
		<div className={`rounded-2xl border p-4 ${tone}`}>
			<div className="flex items-start gap-3">
				<div className="mt-0.5">
					<Icon size={16} />
				</div>
				<div className="flex-1 min-w-0">
					<p className="text-sm font-semibold">{alert.title ?? 'Alert'}</p>
					<p className="text-xs opacity-80 mt-1 leading-relaxed">{alert.message ?? ''}</p>
				</div>
				<span className="text-xs opacity-60 shrink-0">
					{alert.timestamp ? new Date(alert.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : ''}
				</span>
			</div>
		</div>
	)
}
