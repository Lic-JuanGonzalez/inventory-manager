import React, { useEffect } from 'react'
import {
  Dialog, DialogTitle, DialogContent, DialogActions,
  TextField, Button, Grid, CircularProgress, Alert, Box
} from '@mui/material'
import { useForm, Controller } from 'react-hook-form'
import { branchApi } from '../../api/branchApi'

export default function BranchFormDialog({ open, branch, onClose, onSaved }) {
  const isEdit = !!branch
  const { control, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm({
    defaultValues: { name: '', address: '', phone: '', email: '' }
  })
  const [error, setError] = React.useState('')

  useEffect(() => {
    if (open) {
      reset(branch ? { name: branch.name, address: branch.address, phone: branch.phone || '', email: branch.email || '' }
        : { name: '', address: '', phone: '', email: '' })
      setError('')
    }
  }, [open, branch, reset])

  const onSubmit = async (data) => {
    setError('')
    try {
      if (isEdit) await branchApi.update(branch.id, data)
      else await branchApi.create(data)
      onSaved()
    } catch (err) {
      setError(err.response?.data?.detail || 'Error al guardar sucursal')
    }
  }

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{isEdit ? 'Editar Sucursal' : 'Nueva Sucursal'}</DialogTitle>
      <Box component="form" onSubmit={handleSubmit(onSubmit)}>
        <DialogContent>
          {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
          <Grid container spacing={2}>
            <Grid item xs={12}>
              <Controller name="name" control={control} rules={{ required: 'Nombre requerido' }}
                render={({ field }) => (
                  <TextField {...field} label="Nombre *" fullWidth size="small"
                    error={!!errors.name} helperText={errors.name?.message} />
                )} />
            </Grid>
            <Grid item xs={12}>
              <Controller name="address" control={control} rules={{ required: 'Dirección requerida' }}
                render={({ field }) => (
                  <TextField {...field} label="Dirección *" fullWidth size="small"
                    error={!!errors.address} helperText={errors.address?.message} />
                )} />
            </Grid>
            <Grid item xs={6}>
              <Controller name="phone" control={control}
                render={({ field }) => <TextField {...field} label="Teléfono" fullWidth size="small" />} />
            </Grid>
            <Grid item xs={6}>
              <Controller name="email" control={control}
                render={({ field }) => <TextField {...field} label="Email" fullWidth size="small" type="email" />} />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 2 }}>
          <Button onClick={onClose}>Cancelar</Button>
          <Button type="submit" variant="contained" disabled={isSubmitting}>
            {isSubmitting ? <CircularProgress size={20} /> : isEdit ? 'Guardar' : 'Crear'}
          </Button>
        </DialogActions>
      </Box>
    </Dialog>
  )
}
