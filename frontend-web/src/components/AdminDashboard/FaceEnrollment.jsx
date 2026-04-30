import React, { useState } from 'react'
import { Camera, ScanFace } from 'lucide-react'
import faceEnrollmentService from '../../services/faceEnrollmentService'

export default function FaceEnrollment() {
  const [status, setStatus] = useState(null)

  const handleEnroll = async () => {
    setStatus(await faceEnrollmentService.enroll())
  }

  return (
    <div className="glass rounded-2xl p-5">
      <h3 className="font-semibold text-white mb-4 flex items-center gap-2"><ScanFace size={16} className="text-violet-400" /> Face Enrollment</h3>
      <button onClick={handleEnroll} className="px-4 py-2.5 rounded-xl bg-violet-600 text-white flex items-center gap-2 text-sm">
        <Camera size={14} /> Enroll Face
      </button>
      {status ? <p className="text-xs text-slate-500 mt-3">{status.message}</p> : null}
    </div>
  )
}