import React, { useState } from 'react';
import CameraSelector from '../components/LecturerDashboard/CameraSelector';

const TestCameraPage = () => {
  const [step, setStep] = useState('setup'); // 'setup' or 'complete'
  const [selectedConfig, setSelectedConfig] = useState(null);

  const handleCameraSelected = (config) => {
    setSelectedConfig(config);
    setStep('complete');
  };

  const handleAdjustSettings = () => {
    setStep('setup');
  };

  const handleStartSession = () => {
    // Navigate to live session or next page
    console.log('Starting live session with config:', selectedConfig);
    // You can add navigation here, e.g., navigate('/live-session', { state: selectedConfig })
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-[#ECECEC] to-[#ECECEC]">
      {/* Header */}
      <header className="bg-white shadow-sm border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-6 py-4 flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-gray-800">EduVision</h1>
            <p className="text-sm text-gray-500 mt-1">Camera Setup & Testing</p>
          </div>
          <div className="text-right">
            <p className="text-sm text-gray-600">
              {new Date().toLocaleDateString('en-US', {
                weekday: 'long',
                year: 'numeric',
                month: 'long',
                day: 'numeric',
              })}
            </p>
            <p className="text-xs text-gray-500 mt-1">
              {new Date().toLocaleTimeString('en-US', {
                hour: '2-digit',
                minute: '2-digit',
              })}
            </p>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-6 py-12">
        {step === 'setup' ? (
          // Setup Step
          <div className="animate-fade-in">
            <div className="mb-8">
              <h2 className="text-2xl font-bold text-gray-800">
                Step 1: Configure Your Camera
              </h2>
              <p className="text-gray-600 mt-2">
                Select your camera and configure the resolution and frame rate for
                optimal performance.
              </p>
            </div>
            <CameraSelector onCameraSelected={handleCameraSelected} />
          </div>
        ) : (
          // Completion Step
          <div className="animate-fade-in">
            <div className="mb-8">
              <h2 className="text-2xl font-bold text-gray-800">
                Step 2: Camera Configured
              </h2>
              <p className="text-gray-600 mt-2">
                Your camera is ready for the live session.
              </p>
            </div>

            {/* Configuration Summary */}
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
              {/* Camera Info Card */}
              <div className="lg:col-span-2">
                <div className="bg-white rounded-lg shadow-lg p-8 border-l-4 border-[#667D9D]">
                  <h3 className="text-xl font-bold text-gray-800 mb-6">
                    Camera Configuration Summary
                  </h3>

                  <div className="space-y-6">
                    {/* Camera Selection */}
                    <div className="flex items-start space-x-4">
                      <div className="flex-shrink-0">
                        <div className="flex items-center justify-center h-12 w-12 rounded-md bg-[#667D9D] text-white">
                          📷
                        </div>
                      </div>
                      <div className="flex-1">
                        <h4 className="font-semibold text-gray-800">
                          {selectedConfig.camera.name}
                        </h4>
                        <p className="text-sm text-gray-600 mt-1">
                          Type: <span className="font-medium capitalize">
                            {selectedConfig.camera.type}
                          </span>
                        </p>
                        <p className="text-sm text-gray-600">
                          Device ID: <span className="font-medium">
                            {selectedConfig.camera.id}
                          </span>
                        </p>
                      </div>
                      <div className="flex-shrink-0">
                        <span className="inline-flex items-center px-3 py-1 rounded-full text-sm font-medium bg-green-100 text-green-800">
                          ✓ Connected
                        </span>
                      </div>
                    </div>

                    {/* Resolution */}
                    <div className="flex items-start space-x-4 pt-4 border-t border-gray-200">
                      <div className="flex-shrink-0">
                        <div className="flex items-center justify-center h-12 w-12 rounded-md bg-[#667D9D] text-white">
                          🎬
                        </div>
                      </div>
                      <div className="flex-1">
                        <h4 className="font-semibold text-gray-800">Resolution</h4>
                        <p className="text-sm text-gray-600 mt-1">
                          {selectedConfig.resolution}
                        </p>
                        <p className="text-xs text-gray-500 mt-2">
                          {selectedConfig.resolution === '1920x1080' && '✓ Best quality'}
                          {selectedConfig.resolution === '1280x720' && '✓ Recommended'}
                          {selectedConfig.resolution === '640x480' && 'Lower bandwidth'}
                        </p>
                      </div>
                    </div>

                    {/* Frame Rate */}
                    <div className="flex items-start space-x-4 pt-4 border-t border-gray-200">
                      <div className="flex-shrink-0">
                        <div className="flex items-center justify-center h-12 w-12 rounded-md bg-[#667D9D] text-white">
                          ⚡
                        </div>
                      </div>
                      <div className="flex-1">
                        <h4 className="font-semibold text-gray-800">Frame Rate</h4>
                        <p className="text-sm text-gray-600 mt-1">
                          {selectedConfig.fps} FPS
                        </p>
                        <p className="text-xs text-gray-500 mt-2">
                          {selectedConfig.fps === 60 && '✓ Smooth, smooth'}
                          {selectedConfig.fps === 30 && '✓ Balanced performance'}
                          {selectedConfig.fps === 15 && 'Lower bandwidth usage'}
                        </p>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              {/* Quick Stats */}
              <div className="space-y-4">
                <div className="bg-white rounded-lg shadow-lg p-6 border-t-4 border-green-500">
                  <p className="text-sm text-gray-600 uppercase font-semibold tracking-wide">
                    Status
                  </p>
                  <p className="text-2xl font-bold text-green-600 mt-2">Ready</p>
                  <p className="text-xs text-gray-500 mt-3">Camera is connected and configured</p>
                </div>

                <div className="bg-white rounded-lg shadow-lg p-6 border-t-4 border-[#667D9D]">
                  <p className="text-sm text-gray-600 uppercase font-semibold tracking-wide">
                    Quality
                  </p>
                  <p className="text-2xl font-bold text-[#667D9D] mt-2">
                    {selectedConfig.resolution === '1920x1080' ? 'High' : 'Medium'}
                  </p>
                  <p className="text-xs text-gray-500 mt-3">
                    {selectedConfig.resolution} @ {selectedConfig.fps} FPS
                  </p>
                </div>

                <div className="bg-white rounded-lg shadow-lg p-6 border-t-4 border-[#667D9D]">
                  <p className="text-sm text-gray-600 uppercase font-semibold tracking-wide">
                    Next Step
                  </p>
                  <p className="text-sm font-semibold text-[#667D9D] mt-2">
                    Start Live Session
                  </p>
                  <p className="text-xs text-gray-500 mt-3">Begin classroom engagement tracking</p>
                </div>
              </div>
            </div>

            {/* Action Buttons */}
            <div className="flex gap-4 justify-end">
              <button
                onClick={handleAdjustSettings}
                className="px-6 py-3 rounded-lg font-semibold transition-all border-2 border-gray-300 text-gray-700 hover:border-gray-400 hover:bg-gray-50"
              >
                ← Adjust Settings
              </button>
              <button
                onClick={handleStartSession}
                className="px-8 py-3 rounded-lg font-semibold transition-all bg-[#16254F] text-white hover:bg-[#16254F] shadow-lg hover:shadow-xl"
              >
                Start Live Session →
              </button>
            </div>
          </div>
        )}
      </main>

      {/* Footer */}
      <footer className="bg-white border-t border-gray-200 mt-12">
        <div className="max-w-7xl mx-auto px-6 py-8 text-center text-sm text-gray-600">
          <p>EduVision • AI-powered classroom engagement platform</p>
          <p className="mt-2 text-xs text-gray-500">
            Version 1.0 • For optimal experience, use Chrome or Firefox
          </p>
        </div>
      </footer>

      {/* CSS for fade-in animation */}
      <style>{`
        @keyframes fadeIn {
          from {
            opacity: 0;
            transform: translateY(10px);
          }
          to {
            opacity: 1;
            transform: translateY(0);
          }
        }
        .animate-fade-in {
          animation: fadeIn 0.3s ease-in-out;
        }
      `}</style>
    </div>
  );
};

export default TestCameraPage;
