import React, { useEffect, useState } from 'react'
import { Camera } from 'lucide-react'
import cameraService from '../../services/cameraService'

export default function CameraConfigManagement() {
  const [configs, setConfigs] = useState([])

  useEffect(() => {
    cameraService.getConfigs().then(setConfigs).catch(() => setConfigs([]))
  }, [])

  return (
    <div className="glass rounded-2xl p-5">
      <h3 className="font-semibold text-white mb-4 flex items-center gap-2"><Camera size={16} className="text-[#667D9D]" /> Camera Configurations</h3>
      <div className="space-y-2">
        {configs.length === 0 ? <div className="text-sm text-slate-600 py-8 text-center">No camera configurations available.</div> : configs.map((config) => (
          <div key={config.id} className="rounded-xl bg-navy-900/50 border border-slate-800/60 p-3">
            <p className="text-sm font-medium text-slate-200">{config.name}</p>
            <p className="text-xs text-slate-500 mt-1">{config.streamUrl ?? 'Local device'} · {config.active ? 'active' : 'inactive'}</p>
          </div>
        ))}
      </div>
    </div>
  )
}