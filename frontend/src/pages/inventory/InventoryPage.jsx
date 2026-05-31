import React, { useEffect, useState, useCallback } from 'react'
import {
  Box, Button, Card, CardContent, Typography, Chip, Stack,
  Select, MenuItem, FormControl, InputLabel, Alert
} from '@mui/material'
import { DataGrid } from '@mui/x-data-grid'
import AddIcon from '@mui/icons-material/Add'
import PlaylistAddIcon from '@mui/icons-material/PlaylistAdd'
import { inventoryApi } from '../../api/inventoryApi'
import { branchApi } from '../../api/branchApi'
import MovementFormDialog from './MovementFormDialog'
import InitInventoryDialog from './InitInventoryDialog'

export default function InventoryPage() {
  const [rows, setRows] = useState([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(false)
  const [branches, setBranches] = useState([])
  const [branchId, setBranchId] = useState('')
  const [page, setPage] = useState(0)
  const [pageSize, setPageSize] = useState(20)
  const [movementDialog, setMovementDialog] = useState({ open: false, inventory: null })
  const [initDialog, setInitDialog] = useState(false)

  useEffect(() => {
    branchApi.getAllActive().then(r => setBranches(r.data)).catch(console.error)
  }, [])

  const load = useCallback(() => {
    setLoading(true)
    inventoryApi.getAll({ branchId: branchId || undefined, page, size: pageSize })
      .then(r => { setRows(r.data.content); setTotal(r.data.totalElements) })
      .catch(console.error)
      .finally(() => setLoading(false))
  }, [branchId, page, pageSize])

  useEffect(() => { load() }, [load])

  const columns = [
    { field: 'productSku', headerName: 'SKU', width: 110 },
    { field: 'productName', headerName: 'Producto', flex: 1, minWidth: 180 },
    { field: 'branchName', headerName: 'Sucursal', width: 140 },
    { field: 'categoryName', headerName: 'Categoría', width: 120 },
    {
      field: 'currentStock', headerName: 'Stock Actual', width: 120, type: 'number',
      renderCell: ({ row }) => (
        <Chip label={row.currentStock}
          color={row.belowMinStock ? 'error' : row.currentStock <= row.minStock * 1.2 ? 'warning' : 'success'}
          size="small" />
      )
    },
    { field: 'minStock', headerName: 'Mín.', width: 80, type: 'number' },
    { field: 'maxStock', headerName: 'Máx.', width: 80, type: 'number' },
    { field: 'unitOfMeasure', headerName: 'Unidad', width: 80 },
    {
      field: 'stockValue', headerName: 'Valor', width: 120, type: 'number',
      valueFormatter: ({ value }) => `$${Number(value).toLocaleString('es-MX', { maximumFractionDigits: 0 })}`
    },
    {
      field: 'actions', headerName: 'Movimiento', width: 120, sortable: false,
      renderCell: ({ row }) => (
        <Button size="small" variant="outlined" onClick={() => setMovementDialog({ open: true, inventory: row })}>
          Registrar
        </Button>
      )
    }
  ]

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h5" fontWeight={700}>Inventario</Typography>
        <Stack direction="row" spacing={1}>
          <Button variant="outlined" startIcon={<PlaylistAddIcon />} onClick={() => setInitDialog(true)}>
            Inicializar
          </Button>
        </Stack>
      </Box>
      <Card sx={{ mb: 2 }}>
        <CardContent>
          <FormControl size="small" sx={{ minWidth: 200 }}>
            <InputLabel>Sucursal</InputLabel>
            <Select value={branchId} label="Sucursal" onChange={e => { setBranchId(e.target.value); setPage(0) }}>
              <MenuItem value="">Todas</MenuItem>
              {branches.map(b => <MenuItem key={b.id} value={b.id}>{b.name}</MenuItem>)}
            </Select>
          </FormControl>
        </CardContent>
      </Card>
      <Card>
        <DataGrid rows={rows} columns={columns} rowCount={total} loading={loading}
          paginationMode="server" paginationModel={{ page, pageSize }}
          onPaginationModelChange={m => { setPage(m.page); setPageSize(m.pageSize) }}
          pageSizeOptions={[10, 20, 50]} disableRowSelectionOnClick autoHeight sx={{ border: 0 }} />
      </Card>
      <MovementFormDialog open={movementDialog.open} inventory={movementDialog.inventory}
        onClose={() => setMovementDialog({ open: false, inventory: null })}
        onSaved={() => { setMovementDialog({ open: false, inventory: null }); load() }} />
      <InitInventoryDialog open={initDialog} onClose={() => setInitDialog(false)}
        onSaved={() => { setInitDialog(false); load() }} />
    </Box>
  )
}
