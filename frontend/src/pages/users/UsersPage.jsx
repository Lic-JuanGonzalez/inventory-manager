import React, { useEffect, useState, useCallback } from 'react'
import {
  Box, Button, Card, CardContent, TextField, InputAdornment, Chip,
  Typography, IconButton, Tooltip, Stack
} from '@mui/material'
import { DataGrid } from '@mui/x-data-grid'
import AddIcon from '@mui/icons-material/Add'
import EditIcon from '@mui/icons-material/Edit'
import SearchIcon from '@mui/icons-material/Search'
import PersonOffIcon from '@mui/icons-material/PersonOff'
import api from '../../api/axiosConfig'

export default function UsersPage() {
  const [rows, setRows] = useState([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(false)
  const [search, setSearch] = useState('')
  const [page, setPage] = useState(0)
  const [pageSize, setPageSize] = useState(20)

  const load = useCallback(() => {
    setLoading(true)
    api.get('/users', { params: { search: search || undefined, page, size: pageSize } })
      .then(r => { setRows(r.data.content); setTotal(r.data.totalElements) })
      .catch(console.error)
      .finally(() => setLoading(false))
  }, [search, page, pageSize])

  useEffect(() => { load() }, [load])

  const handleDeactivate = async (id) => {
    if (!confirm('¿Desactivar usuario?')) return
    try { await api.delete(`/users/${id}`); load() } catch (err) { alert(err.response?.data?.detail || 'Error') }
  }

  const ROLE_COLORS = { ADMIN: 'error', OPERATOR: 'primary', AUDITOR: 'warning' }

  const columns = [
    { field: 'fullName', headerName: 'Nombre Completo', flex: 1, minWidth: 160 },
    { field: 'email', headerName: 'Email', width: 220 },
    {
      field: 'role', headerName: 'Rol', width: 110,
      renderCell: ({ value }) => <Chip label={value} color={ROLE_COLORS[value]} size="small" />
    },
    {
      field: 'active', headerName: 'Estado', width: 100,
      renderCell: ({ value }) => <Chip label={value ? 'Activo' : 'Inactivo'} color={value ? 'success' : 'default'} size="small" />
    },
    {
      field: 'createdAt', headerName: 'Creado', width: 140,
      valueFormatter: ({ value }) => value ? new Date(value).toLocaleDateString('es-MX') : ''
    },
    {
      field: 'actions', headerName: 'Acciones', width: 100, sortable: false,
      renderCell: ({ row }) => row.active && (
        <Tooltip title="Desactivar">
          <IconButton size="small" color="error" onClick={() => handleDeactivate(row.id)}>
            <PersonOffIcon fontSize="small" />
          </IconButton>
        </Tooltip>
      )
    }
  ]

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h5" fontWeight={700}>Usuarios</Typography>
      </Box>
      <Card sx={{ mb: 2 }}>
        <CardContent>
          <TextField size="small" placeholder="Buscar por nombre o email..."
            value={search} onChange={e => { setSearch(e.target.value); setPage(0) }}
            InputProps={{ startAdornment: <InputAdornment position="start"><SearchIcon /></InputAdornment> }}
            sx={{ width: { xs: '100%', sm: 360 } }} />
        </CardContent>
      </Card>
      <Card>
        <DataGrid rows={rows} columns={columns} rowCount={total} loading={loading}
          paginationMode="server" paginationModel={{ page, pageSize }}
          onPaginationModelChange={m => { setPage(m.page); setPageSize(m.pageSize) }}
          pageSizeOptions={[10, 20, 50]} disableRowSelectionOnClick autoHeight sx={{ border: 0 }} />
      </Card>
    </Box>
  )
}
