import React, { useEffect, useState, useCallback } from 'react'
import {
  Box, Card, CardContent, Typography, Chip, Stack,
  Select, MenuItem, FormControl, InputLabel
} from '@mui/material'
import { DataGrid } from '@mui/x-data-grid'
import { inventoryApi } from '../../api/inventoryApi'
import { branchApi } from '../../api/branchApi'

const TYPE_COLORS = { ENTRADA: 'success', SALIDA: 'error' }

export default function MovementsPage() {
  const [rows, setRows] = useState([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(false)
  const [branches, setBranches] = useState([])
  const [branchId, setBranchId] = useState('')
  const [type, setType] = useState('')
  const [page, setPage] = useState(0)
  const [pageSize, setPageSize] = useState(20)

  useEffect(() => {
    branchApi.getAllActive().then(r => setBranches(r.data)).catch(console.error)
  }, [])

  const load = useCallback(() => {
    setLoading(true)
    inventoryApi.getMovements({ branchId: branchId || undefined, type: type || undefined, page, size: pageSize })
      .then(r => { setRows(r.data.content); setTotal(r.data.totalElements) })
      .catch(console.error)
      .finally(() => setLoading(false))
  }, [branchId, type, page, pageSize])

  useEffect(() => { load() }, [load])

  const columns = [
    {
      field: 'createdAt', headerName: 'Fecha', width: 160,
      valueFormatter: ({ value }) => new Date(value).toLocaleString('es-MX')
    },
    {
      field: 'type', headerName: 'Tipo', width: 90,
      renderCell: ({ value }) => <Chip label={value} color={TYPE_COLORS[value]} size="small" />
    },
    { field: 'reason', headerName: 'Motivo', width: 180 },
    { field: 'productName', headerName: 'Producto', flex: 1, minWidth: 150 },
    { field: 'branchName', headerName: 'Sucursal', width: 130 },
    { field: 'quantity', headerName: 'Cantidad', width: 100, type: 'number' },
    { field: 'stockBefore', headerName: 'Antes', width: 90, type: 'number' },
    { field: 'stockAfter', headerName: 'Después', width: 90, type: 'number' },
    { field: 'userFullName', headerName: 'Usuario', width: 150 },
    { field: 'observations', headerName: 'Observaciones', flex: 1, minWidth: 150 },
  ]

  return (
    <Box>
      <Typography variant="h5" fontWeight={700} mb={3}>Historial de Movimientos</Typography>
      <Card sx={{ mb: 2 }}>
        <CardContent>
          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
            <FormControl size="small" sx={{ minWidth: 200 }}>
              <InputLabel>Sucursal</InputLabel>
              <Select value={branchId} label="Sucursal" onChange={e => { setBranchId(e.target.value); setPage(0) }}>
                <MenuItem value="">Todas</MenuItem>
                {branches.map(b => <MenuItem key={b.id} value={b.id}>{b.name}</MenuItem>)}
              </Select>
            </FormControl>
            <FormControl size="small" sx={{ minWidth: 140 }}>
              <InputLabel>Tipo</InputLabel>
              <Select value={type} label="Tipo" onChange={e => { setType(e.target.value); setPage(0) }}>
                <MenuItem value="">Todos</MenuItem>
                <MenuItem value="ENTRADA">Entrada</MenuItem>
                <MenuItem value="SALIDA">Salida</MenuItem>
              </Select>
            </FormControl>
          </Stack>
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
