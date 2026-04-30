import { useRef, useCallback, useState } from 'react'

export default function useCamera() {
  const videoRef = useRef(null)
  const canvasRef = useRef(document.createElement('canvas'))
  const [stream, setStream] = useState(null)
  const [error, setError] = useState('')

  const startCamera = useCallback(async () => {
    try {
      setError('')
      const mediaStream = await navigator.mediaDevices.getUserMedia({ 
        video: { width: 640, height: 480, facingMode: 'user' } 
      })
      
      if (videoRef.current) {
        videoRef.current.srcObject = mediaStream
      }
      
      setStream(mediaStream)
      return mediaStream
    } catch (err) {
      setError('Cannot access camera: ' + err.message)
      return null
    }
  }, [])

  const stopCamera = useCallback(() => {
    if (stream) {
      stream.getTracks().forEach(track => track.stop())
      setStream(null)
    }
    if (videoRef.current) {
      videoRef.current.srcObject = null
    }
  }, [stream])

  const captureFrame = useCallback(() => {
    if (!videoRef.current) return null
    
    const video = videoRef.current
    const canvas = canvasRef.current
    canvas.width = video.videoWidth
    canvas.height = video.videoHeight
    
    const ctx = canvas.getContext('2d')
    ctx.drawImage(video, 0, 0)
    
    return new Promise((resolve) => {
      canvas.toBlob((blob) => {
        resolve(blob)
      }, 'image/jpeg', 0.8)
    })
  }, [])

  return { videoRef, startCamera, stopCamera, captureFrame, error, isActive: !!stream }
}