import React, { useEffect, useState, useCallback } from 'react'
import {
  Box, Button, Card, CardContent, Typography, Chip, Stack,
  Select, MenuItem, FormControl, InputLabel, IconButton, Tooltip
} from '@mui/material'
import { DataGrid } from '@mui/x-data-grid'
import AddIcon from '@mui/icons-material/Add'
import CheckIcon from '@mui/icons-material/Check'
import LocalShippingIcon from '@mui/icons-material/LocalShipping'
import MoveToInboxIcon from '@mui/icons-material/MoveToInbox'
import CancelIcon from '@mui/icons-material/Cancel'
import { transferApi } from '../../api/transferApi'
import TransferFormDialog from './TransferFormDialog'
import { useAuth } from '../../context/AuthContext'

const STATUS_COLORS = {
  PENDIENTE: 'warning', APROBADA: 'info', EN_TRANSITO: 'primary', RECIBIDA: 'success', CANCELADA: 'default'
}

export default function TransfersPage() {
  const { hasRole } = useAuth()
  const isAdmin = hasRole('ADMIN')
  const [rows, setRows] = useState([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(false)
  const [status, setStatus] = useState('')
  const [page, setPage] = useState(0)
  const [pageSize, setPageSize] = useState(20)
  const [dialogOpen, setDialogOpen] = useState(false)

  const load = useCallback(() => {
    setLoading(true)
    transferApi.getAll({ status: status || undefined, page, size: pageSize })
      .then(r => { setRows(r.data.content); setTotal(r.data.totalElements) })
      .catch(console.error)
      .finally(() => setLoading(false))
  }, [status, page, pageSize])

  useEffect(() => { load() }, [load])

  const doAction = async (action, id) => {
    try { await action(id); load() } catch (err) { alert(err.response?.data?.detail || 'Error') }
  }

  const columns = [
    {
      field: 'requestDate', headerName: 'Fecha', width: 150,
      valueFormatter: ({ value }) => new Date(value).toLocaleDateString('es-MX')
    },
    { field: 'originBranchName', headerName: 'Origen', width: 130 },
    { field: 'destinationBranchName', headerName: 'Destino', width: 130 },
    { field: 'productName', headerName: 'Producto', flex: 1, minWidth: 150 },
    { field: 'quantity', headerName: 'Cantidad', width: 90, type: 'number' },
    {
      field: 'status', headerName: 'Estado', width: 120,
      renderCell: ({ value }) => <Chip label={value} color={STATUS_COLORS[value]} size="small" />
    },
    { field: 'requestedByName', headerName: 'Solicitante', width: 140 },
    {
      field: 'actions', headerName: 'Acciones', width: 160, sortable: false,
      renderCell: ({ row }) => (
        <Stack direction="row" spacing={0.5}>
          {row.status === 'PENDIENTE' && isAdmin && (
            <Tooltip title="Aprobar">
              <IconButton size="small" color="success" onClick={() => doAction(transferApi.approve, row.id)}>
                <CheckIcon fontSize="small" />
              </IconButton>
            </Tooltip>
          )}
          {row.status === 'APROBADA' && (
            <Tooltip title="Enviar">
              <IconButton size="small" color="primary" onClick={() => doAction(transferApi.ship, row.id)}>
                <LocalShippingIcon fontSize="small" />
              </IconButton>
            </Tooltip>
          )}
          {row.status === 'EN_TRANSITO' && (
            <Tooltip title="Recibir">
              <IconButton size="small" color="success" onClick={() => doAction(transferApi.receive, row.id)}>
                <MoveToInboxIcon fontSize="small" />
              </IconButton>
            </Tooltip>
          )}
          {['PENDIENTE', 'APROBADA'].includes(row.status) && (
            <Tooltip title="Cancelar">
              <IconButton size="small" color="error" onClick={() => doAction(transferApi.cancel, row.id)}>
                <CancelIcon fontSize="small" />
              </IconButton>
            </Tooltip>
          )}
        </Stack>
      )
    }
  ]

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h5" fontWeight={700}>Transferencias</Typography>
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => setDialogOpen(true)}>
          Nueva Transferencia
        </Button>
      </Box>
      <Card sx={{ mb: 2 }}>
        <CardContent>
          <FormControl size="small" sx={{ minWidth: 180 }}>
            <InputLabel>Estado</InputLabel>
            <Select value={status} label="Estado" onChange={e => { setStatus(e.target.value); setPage(0) }}>
              <MenuItem value="">Todos</MenuItem>
              {['PENDIENTE','APROBADA','EN_TRANSITO','RECIBIDA','CANCELADA'].map(s => (
                <MenuItem key={s} value={s}>{s}</MenuItem>
              ))}
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
      <TransferFormDialog open={dialogOpen} onClose={() => setDialogOpen(false)}
        onSaved={() => { setDialogOpen(false); load() }} />
    </Box>
  )
}
