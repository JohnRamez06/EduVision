import React, { useState } from 'react'
import reportService from '../../services/reportService'

export default function ReportDownloadButton({ reportType, id, weekId, label = 'Generate Report' }) {
  const [status, setStatus] = useState('idle')
  const [fileUrl, setFileUrl] = useState('')
  const [errorMsg, setErrorMsg] = useState('')

  const handleGenerate = async () => {
    setStatus('generating')
    setErrorMsg('')
    try {
      let result
      switch (reportType) {
        case 'student': result = await reportService.generateStudent(id, weekId); break
        case 'lecturer': result = await reportService.generateLecturer(id, weekId); break
        case 'dean': result = await reportService.generateDean(weekId); break
        case 'session': result = await reportService.generateSession(id); break
        default: throw new Error('Invalid report type')
      }
      setFileUrl(result.fileUrl || result.file_url || '')
      setStatus('ready')
    } catch (e) {
      setErrorMsg(e.response?.data?.message || 'Failed to generate report')
      setStatus('error')
      setTimeout(() => setStatus('idle'), 5000)
    }
  }

  const handleDownload = () => {
    if (fileUrl) {
      const fileName = fileUrl.split('/').pop()
      reportService.download(fileName).then((blob) => {
        const url = window.URL.createObjectURL(new Blob([blob]))
        const link = document.createElement('a')
        link.href = url
        link.setAttribute('download', fileName)
        document.body.appendChild(link)
        link.click()
        link.remove()
        window.URL.revokeObjectURL(url)
      }).catch(console.error)
    }
  }

  return (
    <div className="inline-flex flex-col gap-2">
      {status === 'idle' && (
        <button onClick={handleGenerate}
          className="flex items-center gap-2 px-4 py-2.5 rounded-xl bg-violet-600 hover:bg-violet-500 text-white text-sm font-medium transition-colors">
          📄 {label}
        </button>
      )}

      {status === 'generating' && (
        <button disabled className="flex items-center gap-2 px-4 py-2.5 rounded-xl bg-slate-700 text-slate-400 text-sm">
          ⏳ Generating...
        </button>
      )}

      {status === 'ready' && (
        <button onClick={handleDownload}
          className="flex items-center gap-2 px-4 py-2.5 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white text-sm font-medium transition-colors">
          📥 Download PDF
        </button>
      )}

      {status === 'error' && (
        <p className="text-xs text-rose-400">{errorMsg}</p>
      )}
    </div>
  )
}