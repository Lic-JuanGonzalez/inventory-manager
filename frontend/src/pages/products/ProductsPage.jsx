import React, { useEffect, useState, useCallback } from 'react'
import {
  Box, Button, Card, CardContent, TextField, InputAdornment, Chip,
  Typography, IconButton, Tooltip, Stack, Select, MenuItem, FormControl, InputLabel
} from '@mui/material'
import { DataGrid } from '@mui/x-data-grid'
import AddIcon from '@mui/icons-material/Add'
import EditIcon from '@mui/icons-material/Edit'
import SearchIcon from '@mui/icons-material/Search'
import PowerSettingsNewIcon from '@mui/icons-material/PowerSettingsNew'
import { productApi } from '../../api/productApi'
import ProductFormDialog from './ProductFormDialog'
import { useAuth } from '../../context/AuthContext'

export default function ProductsPage() {
  const { hasRole } = useAuth()
  const canEdit = hasRole(['ADMIN'])
  const [rows, setRows] = useState([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(false)
  const [search, setSearch] = useState('')
  const [active, setActive] = useState('')
  const [page, setPage] = useState(0)
  const [pageSize, setPageSize] = useState(20)
  const [dialogOpen, setDialogOpen] = useState(false)
  const [selected, setSelected] = useState(null)

  const load = useCallback(() => {
    setLoading(true)
    productApi.getAll({ search: search || undefined, active: active !== '' ? active : undefined, page, size: pageSize })
      .then(r => { setRows(r.data.content); setTotal(r.data.totalElements) })
      .catch(console.error)
      .finally(() => setLoading(false))
  }, [search, active, page, pageSize])

  useEffect(() => { load() }, [load])

  const handleToggle = async (id) => {
    await productApi.toggleActive(id)
    load()
  }

  const columns = [
    { field: 'sku', headerName: 'SKU', width: 120 },
    { field: 'name', headerName: 'Nombre', flex: 1, minWidth: 180 },
    { field: 'categoryName', headerName: 'Categoría', width: 130 },
    { field: 'unitOfMeasure', headerName: 'Unidad', width: 100 },
    {
      field: 'referencePrice', headerName: 'Precio Ref.', width: 120, type: 'number',
      valueFormatter: ({ value }) => `$${Number(value).toLocaleString('es-MX', { minimumFractionDigits: 2 })}`
    },
    {
      field: 'active', headerName: 'Estado', width: 100,
      renderCell: ({ value }) => (
        <Chip label={value ? 'Activo' : 'Inactivo'} color={value ? 'success' : 'default'} size="small" />
      )
    },
    {
      field: 'actions', headerName: 'Acciones', width: 100, sortable: false,
      renderCell: ({ row }) => canEdit && (
        <Stack direction="row">
          <Tooltip title="Editar">
            <IconButton size="small" onClick={() => { setSelected(row); setDialogOpen(true) }}>
              <EditIcon fontSize="small" />
            </IconButton>
          </Tooltip>
          <Tooltip title={row.active ? 'Desactivar' : 'Activar'}>
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
        <Typography variant="h5" fontWeight={700}>Productos</Typography>
        {canEdit && (
          <Button variant="contained" startIcon={<AddIcon />}
            onClick={() => { setSelected(null); setDialogOpen(true) }}>
            Nuevo Producto
          </Button>
        )}
      </Box>

      <Card sx={{ mb: 2 }}>
        <CardContent>
          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
            <TextField size="small" placeholder="Buscar por nombre o SKU..."
              value={search} onChange={e => { setSearch(e.target.value); setPage(0) }}
              InputProps={{ startAdornment: <InputAdornment position="start"><SearchIcon /></InputAdornment> }}
              sx={{ flex: 1 }} />
            <FormControl size="small" sx={{ minWidth: 140 }}>
              <InputLabel>Estado</InputLabel>
              <Select value={active} label="Estado" onChange={e => { setActive(e.target.value); setPage(0) }}>
                <MenuItem value="">Todos</MenuItem>
                <MenuItem value="true">Activos</MenuItem>
                <MenuItem value="false">Inactivos</MenuItem>
              </Select>
            </FormControl>
          </Stack>
        </CardContent>
      </Card>

      <Card>
        <DataGrid
          rows={rows}
          columns={columns}
          rowCount={total}
          loading={loading}
          paginationMode="server"
          paginationModel={{ page, pageSize }}
          onPaginationModelChange={m => { setPage(m.page); setPageSize(m.pageSize) }}
          pageSizeOptions={[10, 20, 50]}
          disableRowSelectionOnClick
          autoHeight
          sx={{ border: 0 }}
        />
      </Card>

      <ProductFormDialog
        open={dialogOpen}
        product={selected}
        onClose={() => setDialogOpen(false)}
        onSaved={() => { setDialogOpen(false); load() }}
      />
    </Box>
  )
}
