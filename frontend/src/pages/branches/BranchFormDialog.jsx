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
      <DialogTitle>{isEdit ? 'Edit Branch' : 'New Branch'}</DialogTitle>
      <Box component="form" onSubmit={handleSubmit(onSubmit)}>
        <DialogContent>
          {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
          <Grid container spacing={2}>
            <Grid item xs={12}>
              <Controller name="name" control={control} rules={{ required: 'Name required' }}
                render={({ field }) => (
                  <TextField {...field} label="Name *" fullWidth size="small"
                    error={!!errors.name} helperText={errors.name?.message} />
                )} />
            </Grid>
            <Grid item xs={12}>
              <Controller name="address" control={control} rules={{ required: 'Address required' }}
                render={({ field }) => (
                  <TextField {...field} label="Address *" fullWidth size="small"
                    error={!!errors.address} helperText={errors.address?.message} />
                )} />
            </Grid>
            <Grid item xs={6}>
              <Controller name="phone" control={control}
                render={({ field }) => <TextField {...field} label="Phone" fullWidth size="small" />} />
            </Grid>
            <Grid item xs={6}>
              <Controller name="email" control={control}
                render={({ field }) => <TextField {...field} label="Email" fullWidth size="small" type="email" />} />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 2 }}>
          <Button onClick={onClose}>Cancel</Button>
          <Button type="submit" variant="contained" disabled={isSubmitting}>
            {isSubmitting ? <CircularProgress size={20} /> : isEdit ? 'Save' : 'Create'}
          </Button>
        </DialogActions>
      </Box>
    </Dialog>
  )
}
