import React from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import {
  Drawer, Box, List, ListItem, ListItemButton, ListItemIcon, ListItemText,
  Toolbar, Typography, Divider, Tooltip
} from '@mui/material'
import DashboardIcon from '@mui/icons-material/Dashboard'
import InventoryIcon from '@mui/icons-material/Inventory'
import StorefrontIcon from '@mui/icons-material/Storefront'
import CategoryIcon from '@mui/icons-material/Category'
import SwapHorizIcon from '@mui/icons-material/SwapHoriz'
import AssessmentIcon from '@mui/icons-material/Assessment'
import SecurityIcon from '@mui/icons-material/Security'
import PeopleIcon from '@mui/icons-material/People'
import MoveToInboxIcon from '@mui/icons-material/MoveToInbox'
import { useAuth } from '../../context/AuthContext'

const NAV_ITEMS = [
  { label: 'Dashboard',     icon: <DashboardIcon />,  path: '/dashboard',            roles: ['ADMIN','OPERATOR','AUDITOR'] },
  { label: 'Productos',     icon: <CategoryIcon />,   path: '/products',             roles: ['ADMIN','OPERATOR','AUDITOR'] },
  { label: 'Sucursales',    icon: <StorefrontIcon />, path: '/branches',             roles: ['ADMIN','OPERATOR','AUDITOR'] },
  { label: 'Inventario',    icon: <InventoryIcon />,  path: '/inventory',            roles: ['ADMIN','OPERATOR','AUDITOR'] },
  { label: 'Movimientos',   icon: <MoveToInboxIcon />,path: '/inventory/movements',  roles: ['ADMIN','OPERATOR','AUDITOR'] },
  { label: 'Transferencias',icon: <SwapHorizIcon />,  path: '/transfers',            roles: ['ADMIN','OPERATOR'] },
  { label: 'Reportes',      icon: <AssessmentIcon />, path: '/reports',              roles: ['ADMIN','AUDITOR'] },
  { label: 'Auditoría',     icon: <SecurityIcon />,   path: '/audit',                roles: ['ADMIN','AUDITOR'] },
  { label: 'Usuarios',      icon: <PeopleIcon />,     path: '/users',                roles: ['ADMIN'] },
]

export default function Sidebar({ drawerWidth, mobileOpen, onClose }) {
  const { hasRole } = useAuth()
  const navigate = useNavigate()
  const { pathname } = useLocation()

  const drawerContent = (
    <Box>
      <Toolbar sx={{ bgcolor: 'primary.main' }}>
        <InventoryIcon sx={{ color: 'white', mr: 1 }} />
        <Typography variant="subtitle1" sx={{ color: 'white', fontWeight: 700 }}>
          Inventario
        </Typography>
      </Toolbar>
      <Divider />
      <List dense>
        {NAV_ITEMS.filter(item => hasRole(item.roles)).map(item => (
          <ListItem key={item.path} disablePadding>
            <ListItemButton
              selected={pathname === item.path || pathname.startsWith(item.path + '/')}
              onClick={() => { navigate(item.path); onClose() }}
              sx={{
                borderRadius: 1, mx: 1, my: 0.25,
                '&.Mui-selected': {
                  bgcolor: 'primary.lighter',
                  '& .MuiListItemIcon-root': { color: 'primary.main' },
                  '& .MuiListItemText-primary': { fontWeight: 600, color: 'primary.main' },
                },
              }}
            >
              <ListItemIcon sx={{ minWidth: 36 }}>{item.icon}</ListItemIcon>
              <ListItemText primary={item.label} />
            </ListItemButton>
          </ListItem>
        ))}
      </List>
    </Box>
  )

  return (
    <Box component="nav" sx={{ width: { sm: drawerWidth }, flexShrink: { sm: 0 } }}>
      <Drawer variant="temporary" open={mobileOpen} onClose={onClose}
        ModalProps={{ keepMounted: true }}
        sx={{ display: { xs: 'block', sm: 'none' }, '& .MuiDrawer-paper': { width: drawerWidth } }}>
        {drawerContent}
      </Drawer>
      <Drawer variant="permanent"
        sx={{ display: { xs: 'none', sm: 'block' }, '& .MuiDrawer-paper': { width: drawerWidth, boxSizing: 'border-box' } }}
        open>
        {drawerContent}
      </Drawer>
    </Box>
  )
}
