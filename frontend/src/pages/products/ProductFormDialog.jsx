import React, { useEffect } from 'react'
import {
  Dialog, DialogTitle, DialogContent, DialogActions,
  TextField, Button, Grid, CircularProgress, Alert, Box
} from '@mui/material'
import { useForm, Controller } from 'react-hook-form'
import { productApi } from '../../api/productApi'

export default function ProductFormDialog({ open, product, onClose, onSaved }) {
  const isEdit = !!product
  const { control, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm({
    defaultValues: { sku: '', name: '', description: '', unitOfMeasure: 'UNIDAD', referencePrice: '' }
  })
  const [error, setError] = React.useState('')

  useEffect(() => {
    if (open) {
      reset(product ? {
        sku: product.sku,
        name: product.name,
        description: product.description || '',
        unitOfMeasure: product.unitOfMeasure,
        referencePrice: product.referencePrice,
      } : { sku: '', name: '', description: '', unitOfMeasure: 'UNIDAD', referencePrice: '' })
      setError('')
    }
  }, [open, product, reset])

  const onSubmit = async (data) => {
    setError('')
    try {
      const payload = { ...data, referencePrice: Number(data.referencePrice) }
      if (isEdit) await productApi.update(product.id, payload)
      else await productApi.create(payload)
      onSaved()
    } catch (err) {
      setError(err.response?.data?.detail || 'Error al guardar producto')
    }
  }

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{isEdit ? 'Edit Product' : 'New Product'}</DialogTitle>
      <Box component="form" onSubmit={handleSubmit(onSubmit)}>
        <DialogContent>
          {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
          <Grid container spacing={2}>
            <Grid item xs={12} sm={6}>
              <Controller name="sku" control={control} rules={{ required: 'SKU requerido' }}
                render={({ field }) => (
                  <TextField {...field} label="SKU *" fullWidth size="small"
                    error={!!errors.sku} helperText={errors.sku?.message}
                    disabled={isEdit} />
                )} />
            </Grid>
            <Grid item xs={12} sm={6}>
              <Controller name="unitOfMeasure" control={control} rules={{ required: 'Requerido' }}
                render={({ field }) => (
                  <TextField {...field} label="Unidad de Medida *" fullWidth size="small"
                    error={!!errors.unitOfMeasure} helperText={errors.unitOfMeasure?.message} />
                )} />
            </Grid>
            <Grid item xs={12}>
              <Controller name="name" control={control} rules={{ required: 'Name required' }}
                render={({ field }) => (
                  <TextField {...field} label="Name *" fullWidth size="small"
                    error={!!errors.name} helperText={errors.name?.message} />
                )} />
            </Grid>
            <Grid item xs={12}>
              <Controller name="description" control={control}
                render={({ field }) => (
                  <TextField {...field} label="Description" fullWidth size="small" multiline rows={3} />
                )} />
            </Grid>
            <Grid item xs={12} sm={6}>
              <Controller name="referencePrice" control={control}
                rules={{ required: 'Price required', min: { value: 0, message: 'Must be >= 0' } }}
                render={({ field }) => (
                  <TextField {...field} label="Reference Price *" fullWidth size="small" type="number"
                    inputProps={{ step: '0.01', min: '0' }}
                    error={!!errors.referencePrice} helperText={errors.referencePrice?.message} />
                )} />
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
