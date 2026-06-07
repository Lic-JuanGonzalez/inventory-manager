import React, { useEffect, useState, useCallback } from 'react'
import {
  Box, Card, CardContent, Typography, Chip, Stack, TextField, InputAdornment
} from '@mui/material'
import { DataGrid } from '@mui/x-data-grid'
import SearchIcon from '@mui/icons-material/Search'
import { auditApi } from '../../api/auditApi'

export default function AuditPage() {
  const [rows, setRows] = useState([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(false)
  const [action, setAction] = useState('')
  const [page, setPage] = useState(0)
  const [pageSize, setPageSize] = useState(50)

  const load = useCallback(() => {
    setLoading(true)
    auditApi.getAll({ action: action || undefined, page, size: pageSize })
      .then(r => { setRows(r.data.content); setTotal(r.data.totalElements) })
      .catch(console.error)
      .finally(() => setLoading(false))
  }, [action, page, pageSize])

  useEffect(() => { load() }, [load])

  const columns = [
    {
      field: 'createdAt', headerName: 'Date', width: 160,
      valueFormatter: ({ value }) => new Date(value).toLocaleString('es-MX')
    },
    { field: 'userEmail', headerName: 'User', width: 200 },
    { field: 'action', headerName: 'Action', width: 160 },
    { field: 'entityName', headerName: 'Entity', width: 130 },
    { field: 'entityId', headerName: 'Entity ID', width: 100 },
    {
      field: 'status', headerName: 'Status', width: 100,
      renderCell: ({ value }) => (
        <Chip label={value} color={value === 'SUCCESS' ? 'success' : 'error'} size="small" />
      )
    },
    { field: 'ipAddress', headerName: 'IP', width: 120 },
    { field: 'newValues', headerName: 'Nuevos Valores', flex: 1, minWidth: 200 },
  ]

  return (
    <Box>
      <Typography variant="h5" fontWeight={700} mb={3}>Audit</Typography>
      <Card sx={{ mb: 2 }}>
        <CardContent>
          <TextField size="small" placeholder="Filter by action..."
            value={action} onChange={e => { setAction(e.target.value); setPage(0) }}
            InputProps={{ startAdornment: <InputAdornment position="start"><SearchIcon /></InputAdornment> }}
            sx={{ width: { xs: '100%', sm: 360 } }} />
        </CardContent>
      </Card>
      <Card>
        <DataGrid rows={rows} columns={columns} rowCount={total} loading={loading}
          paginationMode="server" paginationModel={{ page, pageSize }}
          onPaginationModelChange={m => { setPage(m.page); setPageSize(m.pageSize) }}
          pageSizeOptions={[20, 50, 100]} disableRowSelectionOnClick autoHeight sx={{ border: 0 }} />
      </Card>
    </Box>
  )
}
