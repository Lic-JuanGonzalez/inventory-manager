import api from './axiosConfig'

export const branchApi = {
  getAll: (params) => api.get('/branches', { params }),
  getAllActive: () => api.get('/branches/active'),
  getById: (id) => api.get(`/branches/${id}`),
  create: (data) => api.post('/branches', data),
  update: (id, data) => api.put(`/branches/${id}`, data),
  toggleActive: (id) => api.patch(`/branches/${id}/toggle-active`),
}
