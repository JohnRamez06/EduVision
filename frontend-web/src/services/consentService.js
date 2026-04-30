import api from './api'

const consentService = {
	getConsentStatus: (studentId) => api.get(`/consent/status/${studentId}`).then((response) => response.data),
	grantConsent: (policyId) => api.post('/consent/grant', { policyId }).then((response) => response.data),
	revokeConsent: (policyId) => api.post('/consent/revoke', { policyId }).then((response) => response.data),
}

export default consentService
