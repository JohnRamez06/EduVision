const faceEnrollmentService = {
	enroll: async () => ({ status: 'unavailable', message: 'Face enrollment API is not available in this backend snapshot.' }),
	verify: async () => ({ status: 'unavailable', message: 'Face verification API is not available in this backend snapshot.' }),
}

export default faceEnrollmentService
