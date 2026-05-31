import api from './axiosConfig'

export const dashboardApi = {
  getDashboard: () => api.get('/dashboard'),
}
