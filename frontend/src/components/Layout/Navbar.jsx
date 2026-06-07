import React from 'react'
import { AppBar, Toolbar, Typography, IconButton, Avatar, Box, Chip, Tooltip } from '@mui/material'
import MenuIcon from '@mui/icons-material/Menu'
import LogoutIcon from '@mui/icons-material/Logout'
import InventoryIcon from '@mui/icons-material/Inventory'
import { useAuth } from '../../context/AuthContext'

const ROLE_COLORS = { ADMIN: 'error', OPERATOR: 'primary', AUDITOR: 'warning' }

export default function Navbar({ drawerWidth, onMenuClick }) {
  const { user, logout } = useAuth()

  return (
    <AppBar
      position="fixed"
      sx={{ width: { sm: `calc(100% - ${drawerWidth}px)` }, ml: { sm: `${drawerWidth}px` } }}
    >
      <Toolbar>
        <IconButton color="inherit" edge="start" onClick={onMenuClick} sx={{ mr: 2, display: { sm: 'none' } }}>
          <MenuIcon />
        </IconButton>

        <InventoryIcon sx={{ mr: 1 }} />
        <Typography variant="h6" noWrap sx={{ flexGrow: 1, fontWeight: 600 }}>
          Multi-Branch Inventory
        </Typography>

        {user && (
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
            <Chip
              label={user.role}
              size="small"
              color={ROLE_COLORS[user.role] || 'default'}
              variant="outlined"
              sx={{ color: 'white', borderColor: 'rgba(255,255,255,0.5)' }}
            />
            <Tooltip title={user.fullName}>
              <Avatar sx={{ width: 32, height: 32, bgcolor: 'secondary.main', fontSize: '0.875rem' }}>
                {user.fullName?.charAt(0).toUpperCase()}
              </Avatar>
            </Tooltip>
            <Tooltip title="Logout">
              <IconButton color="inherit" onClick={logout} size="small">
                <LogoutIcon />
              </IconButton>
            </Tooltip>
          </Box>
        )}
      </Toolbar>
    </AppBar>
  )
}
