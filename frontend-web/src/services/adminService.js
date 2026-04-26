import api from './api'

const adminService = {
  getUsers:           ()         => api.get('/admin/users').then(r => r.data),
  getUserById:        (id)       => api.get(`/admin/users/${id}`).then(r => r.data),
  createUser:         (body)     => api.post('/admin/users', body).then(r => r.data),
  updateUser:         (id, body) => api.put(`/admin/users/${id}`, body).then(r => r.data),
  deleteUser:         (id)       => api.delete(`/admin/users/${id}`),
  uploadPicture: (id, file) => {
    const fd = new FormData()
    fd.append('file', file)
    return api.post(`/admin/users/${id}/picture`, fd, {
      headers: { 'Content-Type': 'multipart/form-data' },
    }).then(r => r.data)
  },
  getRoles:              ()                   => api.get('/admin/roles').then(r => r.data),
  getRolePermissions:    (roleId)             => api.get(`/admin/roles/${roleId}/permissions`).then(r => r.data),
  updateRolePermissions: (roleId, permIds)    => api.put(`/admin/roles/${roleId}/permissions`, permIds).then(r => r.data),
  assignRole:            (body)               => api.post('/admin/roles/assign', body).then(r => r.data),
  getAllPermissions:      ()                   => api.get('/admin/permissions').then(r => r.data),
}

export default adminService
