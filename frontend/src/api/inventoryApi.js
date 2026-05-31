import api from './axiosConfig'

export const inventoryApi = {
  getAll: (params) => api.get('/inventory', { params }),
  getLowStock: () => api.get('/inventory/low-stock'),
  initialize: (data) => api.post('/inventory/initialize', data),
  registerMovement: (data) => api.post('/inventory/movement', data),
  getMovements: (params) => api.get('/inventory/movements', { params }),
}
