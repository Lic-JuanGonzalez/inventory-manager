import React, { useEffect, useState, useCallback } from 'react'
import {
  Box, Button, Card, CardContent, TextField, InputAdornment, Chip,
  Typography, IconButton, Tooltip, Stack
} from '@mui/material'
import { DataGrid } from '@mui/x-data-grid'
import AddIcon from '@mui/icons-material/Add'
import EditIcon from '@mui/icons-material/Edit'
import SearchIcon from '@mui/icons-material/Search'
import PowerSettingsNewIcon from '@mui/icons-material/PowerSettingsNew'
import { branchApi } from '../../api/branchApi'
import BranchFormDialog from './BranchFormDialog'
import { useAuth } from '../../context/AuthContext'

export default function BranchesPage() {
  const { hasRole } = useAuth()
  const canEdit = hasRole(['ADMIN'])
  const [rows, setRows] = useState([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(false)
  const [search, setSearch] = useState('')
  const [page, setPage] = useState(0)
  const [pageSize, setPageSize] = useState(20)
  const [dialogOpen, setDialogOpen] = useState(false)
  const [selected, setSelected] = useState(null)

  const load = useCallback(() => {
    setLoading(true)
    branchApi.getAll({ search: search || undefined, page, size: pageSize })
      .then(r => { setRows(r.data.content); setTotal(r.data.totalElements) })
      .catch(console.error)
      .finally(() => setLoading(false))
  }, [search, page, pageSize])

  useEffect(() => { load() }, [load])

  const handleToggle = async (id) => { await branchApi.toggleActive(id); load() }

  const columns = [
    { field: 'name', headerName: 'Name', flex: 1, minWidth: 160 },
    { field: 'address', headerName: 'Address', flex: 1, minWidth: 180 },
    { field: 'phone', headerName: 'Phone', width: 130 },
    { field: 'email', headerName: 'Email', width: 200 },
    {
      field: 'active', headerName: 'Status', width: 100,
      renderCell: ({ value }) => (
        <Chip label={value ? 'Active' : 'Inactive'} color={value ? 'success' : 'default'} size="small" />
      )
    },
    {
      field: 'actions', headerName: 'Actions', width: 100, sortable: false,
      renderCell: ({ row }) => canEdit && (
        <Stack direction="row">
          <Tooltip title="Edit">
            <IconButton size="small" onClick={() => { setSelected(row); setDialogOpen(true) }}>
              <EditIcon fontSize="small" />
            </IconButton>
          </Tooltip>
          <Tooltip title={row.active ? 'Deactivate' : 'Activate'}>
            <IconButton size="small" onClick={() => handleToggle(row.id)}>
              <PowerSettingsNewIcon fontSize="small" color={row.active ? 'success' : 'disabled'} />
            </IconButton>
          </Tooltip>
        </Stack>
      )
    }
  ]

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h5" fontWeight={700}>Branches</Typography>
        {canEdit && (
          <Button variant="contained" startIcon={<AddIcon />}
            onClick={() => { setSelected(null); setDialogOpen(true) }}>
            New Branch
          </Button>
        )}
      </Box>
      <Card sx={{ mb: 2 }}>
        <CardContent>
          <TextField size="small" placeholder="Search branch..."
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
      <BranchFormDialog open={dialogOpen} branch={selected}
        onClose={() => setDialogOpen(false)} onSaved={() => { setDialogOpen(false); load() }} />
    </Box>
  )
}
