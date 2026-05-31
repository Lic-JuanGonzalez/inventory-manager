import React, { createContext, useContext, useState, useCallback, useEffect } from 'react'
import api from '../api/axiosConfig'

const AuthContext = createContext(null)

const TOKEN_KEY    = 'inv_access_token'
const REFRESH_KEY  = 'inv_refresh_token'
const USER_KEY     = 'inv_user'

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    try { return JSON.parse(localStorage.getItem(USER_KEY)) } catch { return null }
  })
  const [loading, setLoading] = useState(false)

  const login = useCallback(async (email, password) => {
    setLoading(true)
    try {
      const { data } = await api.post('/auth/login', { email, password })
      localStorage.setItem(TOKEN_KEY, data.accessToken)
      localStorage.setItem(REFRESH_KEY, data.refreshToken)
      const userData = {
        id: data.userId, email: data.email,
        fullName: data.fullName, role: data.role,
      }
      localStorage.setItem(USER_KEY, JSON.stringify(userData))
      setUser(userData)
      return { success: true }
    } catch (err) {
      return { success: false, error: err.response?.data?.detail || 'Credenciales inválidas' }
    } finally {
      setLoading(false)
    }
  }, [])

  const logout = useCallback(async () => {
    const refreshToken = localStorage.getItem(REFRESH_KEY)
    try {
      if (refreshToken) await api.post('/auth/logout', { refreshToken })
    } catch {}
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(REFRESH_KEY)
    localStorage.removeItem(USER_KEY)
    setUser(null)
  }, [])

  const hasRole = useCallback((roles) => {
    if (!user) return false
    return Array.isArray(roles) ? roles.includes(user.role) : user.role === roles
  }, [user])

  return (
    <AuthContext.Provider value={{ user, loading, login, logout, hasRole, isAuthenticated: !!user }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used inside AuthProvider')
  return ctx
}
