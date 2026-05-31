import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { CssBaseline, ThemeProvider, createTheme } from '@mui/material'
import App from './App'
import { AuthProvider } from './context/AuthContext'

const theme = createTheme({
  palette: {
    mode: 'light',
    primary:   { main: '#1976d2' },
    secondary: { main: '#9c27b0' },
    background: { default: '#f5f6fa' },
  },
  typography: {
    fontFamily: '"Inter", "Roboto", "Helvetica", "Arial", sans-serif',
  },
  shape: { borderRadius: 8 },
  components: {
    MuiButton:    { defaultProps: { disableElevation: true } },
    MuiCard:      { defaultProps: { elevation: 0 }, styleOverrides: { root: { border: '1px solid #e0e0e0' } } },
    MuiPaper:     { defaultProps: { elevation: 0 }, styleOverrides: { root: { border: '1px solid #e0e0e0' } } },
  },
})

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <BrowserRouter>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <AuthProvider>
          <App />
        </AuthProvider>
      </ThemeProvider>
    </BrowserRouter>
  </React.StrictMode>
)
