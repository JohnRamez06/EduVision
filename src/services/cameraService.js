import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api/v1';

// Configure axios instance with default headers
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
});

// Add request interceptor for token if using authentication
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('authToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Add response interceptor for error handling
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('authToken');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

/**
 * Fetch list of available cameras from backend
 * @returns {Promise<Object>} Object containing cameras array
 */
export const getCameraList = async () => {
  try {
    const response = await apiClient.get('/camera/list');
    return {
      success: true,
      data: response.data,
      error: null,
    };
  } catch (error) {
    console.error('Error fetching camera list:', error);
    return {
      success: false,
      data: null,
      error: error.response?.data?.message || 'Failed to fetch camera list',
      statusCode: error.response?.status,
    };
  }
};

/**
 * Get current camera configuration
 * @returns {Promise<Object>} Current camera configuration
 */
export const getCurrentCamera = async () => {
  try {
    const response = await apiClient.get('/camera/current');
    return {
      success: true,
      data: response.data,
      error: null,
    };
  } catch (error) {
    console.error('Error fetching current camera:', error);
    return {
      success: false,
      data: null,
      error: error.response?.data?.message || 'Failed to fetch current camera',
      statusCode: error.response?.status,
    };
  }
};

/**
 * Save selected camera configuration to backend
 * @param {Object} config - Camera configuration
 * @param {string} config.cameraType - Camera type (builtin or usb)
 * @param {number} config.deviceId - Device ID
 * @param {string} config.resolution - Resolution (e.g., 1280x720)
 * @param {number} config.fps - Frames per second
 * @returns {Promise<Object>} Result of save operation
 */
export const selectCamera = async (config) => {
  try {
    const { cameraType, deviceId, resolution, fps } = config;
    const params = new URLSearchParams({
      cameraType,
      deviceId,
      resolution,
      ...(fps && { fps }),
    });

    const response = await apiClient.post(`/camera/select?${params.toString()}`);
    return {
      success: true,
      data: response.data,
      error: null,
    };
  } catch (error) {
    console.error('Error selecting camera:', error);
    return {
      success: false,
      data: null,
      error: error.response?.data?.message || 'Failed to save camera selection',
      statusCode: error.response?.status,
    };
  }
};

/**
 * Test if camera is working
 * @param {Object} config - Camera configuration
 * @param {string} config.cameraType - Camera type (builtin or usb)
 * @param {number} config.deviceId - Device ID
 * @returns {Promise<Object>} Result of camera test
 */
export const testCamera = async (config) => {
  try {
    const { cameraType, deviceId } = config;
    const params = new URLSearchParams({
      cameraType,
      deviceId,
    });

    const response = await apiClient.post(`/camera/test?${params.toString()}`);
    return {
      success: true,
      data: response.data,
      error: null,
    };
  } catch (error) {
    console.error('Error testing camera:', error);
    return {
      success: false,
      data: null,
      error: error.response?.data?.message || 'Camera test failed',
      statusCode: error.response?.status,
    };
  }
};

/**
 * Get camera constraints for getUserMedia
 * @param {Object} config - Camera configuration
 * @param {number} config.deviceId - Device ID
 * @param {string} config.resolution - Resolution (e.g., 1280x720)
 * @param {number} config.fps - Frames per second
 * @returns {Object} Media constraints object
 */
export const getMediaConstraints = (config = {}) => {
  const { deviceId, resolution = '1280x720', fps = 30 } = config;
  const [width, height] = resolution.split('x').map(Number);

  return {
    video: {
      ...(deviceId && { deviceId: { exact: deviceId } }),
      width: { ideal: width },
      height: { ideal: height },
      frameRate: { ideal: fps },
    },
    audio: false,
  };
};

export default {
  getCameraList,
  getCurrentCamera,
  selectCamera,
  testCamera,
  getMediaConstraints,
};
