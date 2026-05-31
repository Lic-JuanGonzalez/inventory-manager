import api from './axiosConfig'

export const auditApi = {
  getAll: (params) => api.get('/audit', { params }),
}
