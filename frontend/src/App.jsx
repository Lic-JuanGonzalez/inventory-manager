import React from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'
import { useAuth } from './context/AuthContext'
import MainLayout from './components/Layout/MainLayout'
import LoginPage from './pages/auth/LoginPage'
import DashboardPage from './pages/DashboardPage'
import ProductsPage from './pages/products/ProductsPage'
import BranchesPage from './pages/branches/BranchesPage'
import InventoryPage from './pages/inventory/InventoryPage'
import MovementsPage from './pages/inventory/MovementsPage'
import TransfersPage from './pages/transfers/TransfersPage'
import AuditPage from './pages/audit/AuditPage'
import UsersPage from './pages/users/UsersPage'

const PrivateRoute = ({ children, roles }) => {
  const { isAuthenticated, hasRole } = useAuth()
  if (!isAuthenticated) return <Navigate to="/login" replace />
  if (roles && !hasRole(roles)) return <Navigate to="/dashboard" replace />
  return children
}

export default function App() {
  const { isAuthenticated } = useAuth()

  return (
    <Routes>
      <Route path="/login" element={
        isAuthenticated ? <Navigate to="/dashboard" replace /> : <LoginPage />
      } />

      <Route path="/" element={
        <PrivateRoute><MainLayout /></PrivateRoute>
      }>
        <Route index element={<Navigate to="/dashboard" replace />} />
        <Route path="dashboard" element={<DashboardPage />} />
        <Route path="products" element={<ProductsPage />} />
        <Route path="branches" element={<BranchesPage />} />
        <Route path="inventory" element={<InventoryPage />} />
        <Route path="inventory/movements" element={<MovementsPage />} />
        <Route path="transfers" element={<TransfersPage />} />
        <Route path="audit" element={
          <PrivateRoute roles={['ADMIN', 'AUDITOR']}><AuditPage /></PrivateRoute>
        } />
        <Route path="users" element={
          <PrivateRoute roles={['ADMIN']}><UsersPage /></PrivateRoute>
        } />
      </Route>

      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  )
}
