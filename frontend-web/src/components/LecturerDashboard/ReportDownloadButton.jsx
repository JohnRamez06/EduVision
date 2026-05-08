import React, { useState } from 'react'
import { Download } from 'lucide-react'
import reportService from '../../services/reportService'

export default function ReportDownloadButton({ reportId, label = 'Download Report' }) {
	const [loading, setLoading] = useState(false)

	const handleDownload = async () => {
		if (!reportId) return
		setLoading(true)
		try {
			const blob = await reportService.downloadReport(reportId)
			const url = URL.createObjectURL(blob)
			const anchor = document.createElement('a')
			anchor.href = url
			anchor.download = `report-${reportId}.bin`
			anchor.click()
			URL.revokeObjectURL(url)
		} finally {
			setLoading(false)
		}
	}

	return (
		<button
			type="button"
			onClick={handleDownload}
			disabled={loading || !reportId}
			className="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl bg-[#16254F] hover:bg-[#667D9D] text-white text-sm font-medium transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
		>
			<Download size={14} />
			{loading ? 'Downloading…' : label}
		</button>
	)
}
