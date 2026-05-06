import React, { useState, useEffect, useRef } from 'react';
import { getCameraList, selectCamera, testCamera, getMediaConstraints } from '../../services/cameraService';

const CameraSelector = ({ onCameraSelected }) => {
  const [cameras, setCameras] = useState([]);
  const [selectedCamera, setSelectedCamera] = useState(null);
  const [resolution, setResolution] = useState('1280x720');
  const [fps, setFps] = useState(30);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [testing, setTesting] = useState(false);
  const [testSuccess, setTestSuccess] = useState(false);
  const [showPreview, setShowPreview] = useState(false);
  const videoRef = useRef(null);
  const streamRef = useRef(null);

  const resolutionOptions = ['640x480', '1280x720', '1920x1080'];
  const fpsOptions = [15, 30, 60];

  // Fetch available cameras on mount
  useEffect(() => {
    fetchCameras();
  }, []);

  const fetchCameras = async () => {
    setLoading(true);
    setError(null);
    const result = await getCameraList();
    if (result.success && result.data?.cameras) {
      setCameras(result.data.cameras);
      // Auto-select first available camera
      if (result.data.cameras.length > 0) {
        setSelectedCamera(result.data.cameras[0]);
      }
    } else {
      setError(result.error || 'Failed to fetch cameras');
    }
    setLoading(false);
  };

  // Start live preview when camera is selected
  useEffect(() => {
    if (selectedCamera && showPreview) {
      startPreview();
    }
    return () => stopPreview();
  }, [selectedCamera, showPreview, resolution, fps]);

  const startPreview = async () => {
    try {
      const constraints = getMediaConstraints({
        deviceId: selectedCamera.id,
        resolution,
        fps,
      });

      const stream = await navigator.mediaDevices.getUserMedia(constraints);
      streamRef.current = stream;

      if (videoRef.current) {
        videoRef.current.srcObject = stream;
      }
      setError(null);
    } catch (err) {
      setError(`Camera access error: ${err.message}`);
    }
  };

  const stopPreview = () => {
    if (streamRef.current) {
      streamRef.current.getTracks().forEach(track => track.stop());
      streamRef.current = null;
    }
  };

  const handleCameraSelect = (camera) => {
    stopPreview();
    setSelectedCamera(camera);
    setShowPreview(false);
    setTestSuccess(false);
  };

  const handleTestCamera = async () => {
    if (!selectedCamera) return;

    setTesting(true);
    setError(null);
    const result = await testCamera({
      cameraType: selectedCamera.type,
      deviceId: selectedCamera.id,
    });

    if (result.success) {
      setTestSuccess(true);
      setShowPreview(true);
    } else {
      setError(result.error || 'Camera test failed');
    }
    setTesting(false);
  };

  const handleSaveCamera = async () => {
    if (!selectedCamera) {
      setError('Please select a camera');
      return;
    }

    setLoading(true);
    setError(null);
    const result = await selectCamera({
      cameraType: selectedCamera.type,
      deviceId: selectedCamera.id,
      resolution,
      fps,
    });

    if (result.success) {
      stopPreview();
      onCameraSelected?.({
        camera: selectedCamera,
        resolution,
        fps,
      });
    } else {
      setError(result.error || 'Failed to save camera selection');
    }
    setLoading(false);
  };

  if (loading && cameras.length === 0) {
    return (
      <div className="flex items-center justify-center p-8">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto p-6 space-y-6">
      {/* Header */}
      <div className="bg-gradient-to-r from-blue-600 to-blue-800 rounded-lg p-6 text-white">
        <h2 className="text-3xl font-bold">Camera Setup</h2>
        <p className="text-blue-100 mt-2">Select and test your camera before starting a session</p>
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 rounded-lg p-4 flex items-center justify-between">
          <div>
            <p className="text-red-800 font-semibold">Error</p>
            <p className="text-red-600 text-sm">{error}</p>
          </div>
          <button
            onClick={fetchCameras}
            className="bg-red-600 hover:bg-red-700 text-white px-4 py-2 rounded-lg text-sm font-semibold transition"
          >
            Retry
          </button>
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Camera Selection */}
        <div className="space-y-4">
          <div className="bg-white rounded-lg shadow-md p-6">
            <h3 className="text-xl font-bold text-gray-800 mb-4">Select Camera</h3>

            {cameras.length === 0 ? (
              <p className="text-gray-600 text-center py-8">No cameras found</p>
            ) : (
              <div className="space-y-2">
                {cameras.map((camera) => (
                  <button
                    key={camera.id}
                    onClick={() => handleCameraSelect(camera)}
                    className={`w-full p-4 rounded-lg border-2 transition text-left ${
                      selectedCamera?.id === camera.id
                        ? 'border-blue-600 bg-blue-50'
                        : 'border-gray-300 bg-gray-50 hover:border-blue-400'
                    }`}
                  >
                    <div className="flex items-center justify-between">
                      <div>
                        <p className="font-semibold text-gray-800">{camera.name}</p>
                        <p className="text-sm text-gray-600">ID: {camera.id} • {camera.type}</p>
                      </div>
                      <div className={`w-4 h-4 rounded-full border-2 ${
                        selectedCamera?.id === camera.id
                          ? 'bg-blue-600 border-blue-600'
                          : 'border-gray-400'
                      }`}></div>
                    </div>
                    <div className="mt-2 flex items-center gap-2">
                      <div className={`w-2 h-2 rounded-full ${camera.available ? 'bg-green-500' : 'bg-red-500'}`}></div>
                      <span className={`text-xs font-semibold ${camera.available ? 'text-green-600' : 'text-red-600'}`}>
                        {camera.available ? 'Connected' : 'Disconnected'}
                      </span>
                    </div>
                  </button>
                ))}
              </div>
            )}
          </div>

          {/* Settings */}
          {selectedCamera && (
            <div className="bg-white rounded-lg shadow-md p-6 space-y-4">
              <h3 className="text-lg font-bold text-gray-800">Settings</h3>

              <div>
                <label className="block text-sm font-semibold text-gray-700 mb-2">Resolution</label>
                <select
                  value={resolution}
                  onChange={(e) => setResolution(e.target.value)}
                  className="w-full border border-gray-300 rounded-lg px-4 py-2 text-gray-800"
                >
                  {resolutionOptions.map((res) => (
                    <option key={res} value={res}>{res}</option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-sm font-semibold text-gray-700 mb-2">FPS (Frames Per Second)</label>
                <select
                  value={fps}
                  onChange={(e) => setFps(parseInt(e.target.value))}
                  className="w-full border border-gray-300 rounded-lg px-4 py-2 text-gray-800"
                >
                  {fpsOptions.map((f) => (
                    <option key={f} value={f}>{f} FPS</option>
                  ))}
                </select>
              </div>

              <button
                onClick={handleTestCamera}
                disabled={testing || !selectedCamera?.available}
                className={`w-full py-3 rounded-lg font-semibold transition ${
                  testing || !selectedCamera?.available
                    ? 'bg-gray-300 text-gray-600 cursor-not-allowed'
                    : 'bg-orange-500 hover:bg-orange-600 text-white'
                }`}
              >
                {testing ? 'Testing Camera...' : 'Test Camera'}
              </button>

              {testSuccess && (
                <div className="bg-green-50 border border-green-200 rounded-lg p-3 flex items-center gap-2">
                  <div className="w-2 h-2 bg-green-500 rounded-full"></div>
                  <p className="text-green-700 font-semibold text-sm">Camera test successful!</p>
                </div>
              )}
            </div>
          )}
        </div>

        {/* Preview */}
        <div className="space-y-4">
          <div className="bg-white rounded-lg shadow-md p-6">
            <h3 className="text-lg font-bold text-gray-800 mb-4">Live Preview</h3>
            {showPreview ? (
              <div className="bg-black rounded-lg overflow-hidden">
                <video
                  ref={videoRef}
                  autoPlay
                  playsInline
                  className="w-full h-64 object-cover"
                />
              </div>
            ) : (
              <div className="bg-gray-200 rounded-lg h-64 flex items-center justify-center">
                <div className="text-center">
                  <p className="text-gray-600 font-semibold">No preview</p>
                  <p className="text-gray-500 text-sm">Click "Test Camera" to see live preview</p>
                </div>
              </div>
            )}
          </div>

          {selectedCamera && (
            <div className="bg-white rounded-lg shadow-md p-6">
              <h3 className="text-lg font-bold text-gray-800 mb-4">Camera Info</h3>
              <div className="space-y-3">
                <div className="flex justify-between">
                  <span className="text-gray-600">Camera Name:</span>
                  <span className="font-semibold text-gray-800">{selectedCamera.name}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">Device ID:</span>
                  <span className="font-semibold text-gray-800">{selectedCamera.id}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">Type:</span>
                  <span className="font-semibold text-gray-800 capitalize">{selectedCamera.type}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">Resolution:</span>
                  <span className="font-semibold text-gray-800">{resolution}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">FPS:</span>
                  <span className="font-semibold text-gray-800">{fps}</span>
                </div>
              </div>
            </div>
          )}

          {selectedCamera && (
            <button
              onClick={handleSaveCamera}
              disabled={loading || !selectedCamera}
              className={`w-full py-3 rounded-lg font-semibold text-white transition text-lg ${
                loading || !selectedCamera
                  ? 'bg-gray-400 cursor-not-allowed'
                  : 'bg-green-600 hover:bg-green-700'
              }`}
            >
              {loading ? 'Saving...' : 'Save & Continue'}
            </button>
          )}
        </div>
      </div>
    </div>
  );
};

export default CameraSelector;
