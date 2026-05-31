import api from './axiosConfig'

export const transferApi = {
  getAll: (params) => api.get('/transfers', { params }),
  getPending: () => api.get('/transfers/pending'),
  getById: (id) => api.get(`/transfers/${id}`),
  create: (data) => api.post('/transfers', data),
  approve: (id) => api.put(`/transfers/${id}/approve`),
  ship: (id) => api.put(`/transfers/${id}/ship`),
  receive: (id) => api.put(`/transfers/${id}/receive`),
  cancel: (id) => api.put(`/transfers/${id}/cancel`),
}
