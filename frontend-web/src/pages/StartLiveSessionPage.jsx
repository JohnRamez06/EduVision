import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { CheckCircle, AlertCircle, Loader, Camera } from 'lucide-react';

export default function StartLiveSessionPage() {
  const navigate = useNavigate();
  const [step, setStep] = useState('camera'); // 'camera' or 'session-details'
  const [selectedCamera, setSelectedCamera] = useState(null);
  const [cameraConfig, setCameraConfig] = useState(null);
  const [course, setCourse] = useState('');
  const [location, setLocation] = useState('');
  const [duration, setDuration] = useState('2');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [courses, setCourses] = useState([]);
  const [availableCameras, setAvailableCameras] = useState([]);
  const [previewStream, setPreviewStream] = useState(null);
  const [resolution, setResolution] = useState('1280x720');
  const [fps, setFps] = useState(30);
  const videoRef = React.useRef(null);

  const resolutionOptions = ['640x480', '1280x720', '1920x1080'];
  const fpsOptions = [15, 30, 60];
  const durationOptions = [
    { value: '1', label: '1 hour' },
    { value: '1.5', label: '1.5 hours' },
    { value: '2', label: '2 hours' },
    { value: '3', label: '3 hours' },
  ];

  // Fetch available courses
  useEffect(() => {
    fetchCourses();
    getAvailableCameras();
  }, []);

  // Start preview when camera is selected
  useEffect(() => {
    if (selectedCamera && step === 'camera') {
      startPreview();
    }
    return () => stopPreview();
  }, [selectedCamera, resolution, fps]);

  const fetchCourses = async () => {
    try {
      const response = await fetch('http://localhost:8080/api/v1/lecturer/courses', {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });
      const data = await response.json();
      setCourses(data.courses || []);
    } catch (err) {
      console.error('Error fetching courses:', err);
    }
  };

  const getAvailableCameras = async () => {
    try {
      const devices = await navigator.mediaDevices.enumerateDevices();
      const videoDevices = devices.filter(device => device.kind === 'videoinput');
      const cameraList = videoDevices.map((device, index) => ({
        id: index,
        deviceId: device.deviceId,
        name: device.label || `Camera ${index + 1}`,
        type: index === 0 ? 'builtin' : 'usb',
        available: true
      }));
      setAvailableCameras(cameraList);
      if (cameraList.length > 0 && !selectedCamera) {
        setSelectedCamera(cameraList[0]);
      }
    } catch (err) {
      console.error('Error getting cameras:', err);
    }
  };

  const startPreview = async () => {
    stopPreview();
    if (!selectedCamera) return;

    try {
      const [width, height] = resolution.split('x').map(Number);
      const constraints = {
        video: {
          deviceId: selectedCamera.deviceId ? { exact: selectedCamera.deviceId } : undefined,
          width: { ideal: width },
          height: { ideal: height },
          frameRate: { ideal: fps }
        }
      };

      const stream = await navigator.mediaDevices.getUserMedia(constraints);
      setPreviewStream(stream);
      if (videoRef.current) {
        videoRef.current.srcObject = stream;
        videoRef.current.play();
      }
    } catch (err) {
      setError(`Camera preview error: ${err.message}`);
    }
  };

  const stopPreview = () => {
    if (previewStream) {
      previewStream.getTracks().forEach(track => track.stop());
      setPreviewStream(null);
    }
  };

  const handleCameraSelect = (camera) => {
    stopPreview();
    setSelectedCamera(camera);
    setError('');
  };

  const handleTestCamera = async () => {
    if (!selectedCamera) {
      setError('Please select a camera');
      return;
    }
    setLoading(true);
    setError('');
    
    try {
      const [width, height] = resolution.split('x').map(Number);
      const constraints = {
        video: {
          deviceId: selectedCamera.deviceId ? { exact: selectedCamera.deviceId } : undefined,
          width: { ideal: width },
          height: { ideal: height }
        }
      };
      const stream = await navigator.mediaDevices.getUserMedia(constraints);
      stream.getTracks().forEach(track => track.stop());
      
      // Save camera selection
      const cameraType = selectedCamera.type;
      const deviceId = selectedCamera.id;
      
      await fetch('http://localhost:8080/api/v1/camera/select', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        },
        body: JSON.stringify({ cameraType, deviceId, resolution, fps })
      });
      
      setCameraConfig({
        name: selectedCamera.name,
        type: selectedCamera.type,
        id: selectedCamera.id,
        resolution,
        fps
      });
      
      setTimeout(() => {
        setStep('session-details');
        setLoading(false);
      }, 1000);
    } catch (err) {
      setError('Camera test failed. Please check your camera connection.');
      setLoading(false);
    }
  };

  const handleStartSession = async () => {
    if (!course) {
      setError('Please select a course');
      return;
    }
    if (!location) {
      setError('Please enter room/location');
      return;
    }
    if (!cameraConfig) {
      setError('Please select a camera first');
      return;
    }

    setLoading(true);
    setError('');

    try {
      // Start session via API
      const startResponse = await fetch('http://localhost:8000/start-session', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          course_id: course,
          room_location: location,
          scheduled_duration: parseFloat(duration),
          camera_config: cameraConfig
        })
      });
      
      const sessionData = await startResponse.json();
      
      navigate('/lecturer/live', {
        state: {
          sessionId: sessionData.session_id,
          courseId: course,
          roomLocation: location,
          plannedDuration: duration,
          camera: cameraConfig
        }
      });
    } catch (err) {
      setError('Error starting session. Please try again.');
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900 p-8">
      <div className="max-w-6xl mx-auto">
        {/* Header */}
        <div className="mb-8">
          <div className="flex items-center gap-3 mb-2">
            <div className="w-10 h-10 bg-teal-500 rounded-lg flex items-center justify-center">
              <Camera className="w-6 h-6 text-white" />
            </div>
            <h1 className="text-3xl font-bold text-white">Start Live Session</h1>
          </div>
          <p className="text-slate-400">Configure your camera and session settings</p>
        </div>

        {/* Step Indicator */}
        <div className="flex items-center gap-4 mb-8 max-w-md">
          <div className={`flex items-center justify-center w-10 h-10 rounded-full font-bold ${
            step === 'camera' ? 'bg-teal-500 text-white' : 'bg-slate-700 text-slate-300'
          }`}>
            1
          </div>
          <div className="flex-1 h-1 bg-slate-700"></div>
          <div className={`flex items-center justify-center w-10 h-10 rounded-full font-bold ${
            step === 'session-details' ? 'bg-teal-500 text-white' : 'bg-slate-700 text-slate-300'
          }`}>
            2
          </div>
        </div>

        {/* Error Message */}
        {error && (
          <div className="mb-6 p-4 bg-red-900/30 border border-red-500/50 rounded-lg flex items-start gap-3">
            <AlertCircle className="w-5 h-5 text-red-400 flex-shrink-0 mt-0.5" />
            <p className="text-red-200 text-sm">{error}</p>
          </div>
        )}

        {/* Step 1: Camera Selection */}
        {step === 'camera' && (
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* Camera List */}
            <div className="bg-slate-800/50 border border-slate-700 rounded-xl p-6">
              <h2 className="text-xl font-semibold text-white mb-4">Select Camera</h2>
              <div className="space-y-3">
                {availableCameras.map((camera) => (
                  <button
                    key={camera.id}
                    onClick={() => handleCameraSelect(camera)}
                    className={`w-full p-4 rounded-lg border-2 transition-all text-left ${
                      selectedCamera?.id === camera.id
                        ? 'border-teal-500 bg-teal-500/10'
                        : 'border-slate-600 bg-slate-700/50 hover:border-slate-500'
                    }`}
                  >
                    <div className="flex items-center justify-between">
                      <div>
                        <p className="font-semibold text-white">{camera.name}</p>
                        <p className="text-sm text-slate-400 mt-1">
                          {camera.type === 'builtin' ? 'Built-in Webcam' : 'USB Webcam'}
                        </p>
                      </div>
                      {selectedCamera?.id === camera.id && (
                        <CheckCircle className="w-5 h-5 text-teal-500" />
                      )}
                    </div>
                  </button>
                ))}
              </div>
            </div>

            {/* Camera Preview & Settings */}
            <div className="space-y-6">
              {/* Live Preview */}
              <div className="bg-slate-800/50 border border-slate-700 rounded-xl p-6">
                <h3 className="text-lg font-semibold text-white mb-3">Live Preview</h3>
                <div className="bg-black rounded-lg overflow-hidden aspect-video">
                  <video
                    ref={videoRef}
                    autoPlay
                    playsInline
                    muted
                    className="w-full h-full object-cover"
                  />
                  {!previewStream && (
                    <div className="flex items-center justify-center h-full text-slate-500">
                      <p>Select a camera to preview</p>
                    </div>
                  )}
                </div>
              </div>

              {/* Settings */}
              <div className="bg-slate-800/50 border border-slate-700 rounded-xl p-6">
                <h3 className="text-lg font-semibold text-white mb-4">Camera Settings</h3>
                
                <div className="grid grid-cols-2 gap-4 mb-4">
                  <div>
                    <label className="block text-sm text-slate-400 mb-2">Resolution</label>
                    <select
                      value={resolution}
                      onChange={(e) => setResolution(e.target.value)}
                      className="w-full px-3 py-2 bg-slate-700 border border-slate-600 rounded-lg text-white"
                    >
                      {resolutionOptions.map(res => (
                        <option key={res} value={res}>{res}</option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label className="block text-sm text-slate-400 mb-2">Frame Rate (FPS)</label>
                    <select
                      value={fps}
                      onChange={(e) => setFps(parseInt(e.target.value))}
                      className="w-full px-3 py-2 bg-slate-700 border border-slate-600 rounded-lg text-white"
                    >
                      {fpsOptions.map(f => (
                        <option key={f} value={f}>{f} FPS</option>
                      ))}
                    </select>
                  </div>
                </div>

                <button
                  onClick={handleTestCamera}
                  disabled={loading || !selectedCamera}
                  className="w-full py-3 bg-teal-600 hover:bg-teal-500 disabled:bg-slate-600 disabled:cursor-not-allowed text-white font-semibold rounded-lg transition"
                >
                  {loading ? <Loader className="w-5 h-5 animate-spin mx-auto" /> : 'Test & Continue'}
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Step 2: Session Details */}
        {step === 'session-details' && cameraConfig && (
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <div className="bg-slate-800/50 border border-slate-700 rounded-xl p-6">
              <h2 className="text-xl font-semibold text-white mb-6">Session Details</h2>

              {/* Course Selection */}
              <div className="mb-5">
                <label className="block text-slate-300 font-medium mb-2">Course</label>
                <select
                  value={course}
                  onChange={(e) => setCourse(e.target.value)}
                  className="w-full px-4 py-3 bg-slate-700 border border-slate-600 rounded-lg text-white"
                >
                  <option value="">Select a course...</option>
                  {courses.map((c) => (
                    <option key={c.courseId || c.id} value={c.courseId || c.id}>
                      {c.code} - {c.title}
                    </option>
                  ))}
                </select>
              </div>

              {/* Location */}
              <div className="mb-5">
                <label className="block text-slate-300 font-medium mb-2">Room / Location</label>
                <input
                  type="text"
                  value={location}
                  onChange={(e) => setLocation(e.target.value)}
                  placeholder="e.g. Hall B, Lab 3..."
                  className="w-full px-4 py-3 bg-slate-700 border border-slate-600 rounded-lg text-white placeholder-slate-400"
                />
              </div>

              {/* Duration */}
              <div className="mb-8">
                <label className="block text-slate-300 font-medium mb-2">Duration</label>
                <select
                  value={duration}
                  onChange={(e) => setDuration(e.target.value)}
                  className="w-full px-4 py-3 bg-slate-700 border border-slate-600 rounded-lg text-white"
                >
                  {durationOptions.map(opt => (
                    <option key={opt.value} value={opt.value}>{opt.label}</option>
                  ))}
                </select>
              </div>

              <button
                onClick={handleStartSession}
                disabled={loading || !course || !location}
                className="w-full py-3 bg-teal-600 hover:bg-teal-500 disabled:bg-slate-600 disabled:cursor-not-allowed text-white font-semibold rounded-lg flex items-center justify-center gap-2 transition"
              >
                {loading ? <Loader className="w-5 h-5 animate-spin" /> : 'Start Live Session'}
              </button>

              <button
                onClick={() => setStep('camera')}
                className="w-full mt-3 py-3 bg-slate-700 hover:bg-slate-600 text-slate-300 font-semibold rounded-lg transition"
              >
                ← Change Camera
              </button>
            </div>

            {/* Summary Panel */}
            <div className="bg-slate-800/50 border border-slate-700 rounded-xl p-6">
              <h3 className="text-lg font-semibold text-white mb-4">Configuration Summary</h3>
              
              <div className="space-y-4">
                <div className="flex justify-between">
                  <span className="text-slate-400">Camera:</span>
                  <span className="text-teal-400 font-medium">{cameraConfig.name}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-400">Resolution:</span>
                  <span className="text-teal-400">{cameraConfig.resolution}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-400">Frame Rate:</span>
                  <span className="text-teal-400">{cameraConfig.fps} fps</span>
                </div>
                <div className="border-t border-slate-700 pt-4 mt-2">
                  <p className="text-slate-400 text-sm">Selected course will appear here once chosen</p>
                </div>
              </div>

              <div className="mt-6 p-3 bg-blue-900/30 border border-blue-500/50 rounded-lg">
                <p className="text-blue-200 text-xs leading-relaxed">
                  Your browser camera will be used to detect student emotions in real time.
                  Frames are analyzed every 3 seconds.
                </p>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}