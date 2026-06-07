import React, { useEffect, useState } from 'react'
import {
  Dialog, DialogTitle, DialogContent, DialogActions,
  TextField, Button, Grid, CircularProgress, Alert, Box,
  Select, MenuItem, FormControl, InputLabel
} from '@mui/material'
import { useForm, Controller } from 'react-hook-form'
import { transferApi } from '../../api/transferApi'
import { productApi } from '../../api/productApi'
import { branchApi } from '../../api/branchApi'

export default function TransferFormDialog({ open, onClose, onSaved }) {
  const [products, setProducts] = useState([])
  const [branches, setBranches] = useState([])
  const [error, setError] = React.useState('')
  const { control, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm({
    defaultValues: { originBranchId: '', destinationBranchId: '', productId: '', quantity: '', notes: '' }
  })

  useEffect(() => {
    if (open) {
      reset({ originBranchId: '', destinationBranchId: '', productId: '', quantity: '', notes: '' })
      setError('')
      productApi.getAll({ active: true, size: 200 }).then(r => setProducts(r.data.content)).catch(console.error)
      branchApi.getAllActive().then(r => setBranches(r.data)).catch(console.error)
    }
  }, [open, reset])

  const onSubmit = async (data) => {
    setError('')
    try {
      await transferApi.create({
        originBranchId: Number(data.originBranchId),
        destinationBranchId: Number(data.destinationBranchId),
        productId: Number(data.productId),
        quantity: Number(data.quantity),
        notes: data.notes || null,
      })
      onSaved()
    } catch (err) {
      setError(err.response?.data?.detail || 'Error al crear transferencia')
    }
  }

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>New Transfer</DialogTitle>
      <Box component="form" onSubmit={handleSubmit(onSubmit)}>
        <DialogContent>
          {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
          <Grid container spacing={2}>
            <Grid item xs={12} sm={6}>
              <Controller name="originBranchId" control={control} rules={{ required: 'Requerido' }}
                render={({ field }) => (
                  <FormControl fullWidth size="small" error={!!errors.originBranchId}>
                    <InputLabel>Origin Branch *</InputLabel>
                    <Select {...field} label="Origin Branch *">
                      {branches.map(b => <MenuItem key={b.id} value={b.id}>{b.name}</MenuItem>)}
                    </Select>
                  </FormControl>
                )} />
            </Grid>
            <Grid item xs={12} sm={6}>
              <Controller name="destinationBranchId" control={control} rules={{ required: 'Requerido' }}
                render={({ field }) => (
                  <FormControl fullWidth size="small" error={!!errors.destinationBranchId}>
                    <InputLabel>Destination Branch *</InputLabel>
                    <Select {...field} label="Destination Branch *">
                      {branches.map(b => <MenuItem key={b.id} value={b.id}>{b.name}</MenuItem>)}
                    </Select>
                  </FormControl>
                )} />
            </Grid>
            <Grid item xs={12}>
              <Controller name="productId" control={control} rules={{ required: 'Requerido' }}
                render={({ field }) => (
                  <FormControl fullWidth size="small" error={!!errors.productId}>
                    <InputLabel>Product *</InputLabel>
                    <Select {...field} label="Product *">
                      {products.map(p => <MenuItem key={p.id} value={p.id}>{p.sku} - {p.name}</MenuItem>)}
                    </Select>
                  </FormControl>
                )} />
            </Grid>
            <Grid item xs={12} sm={6}>
              <Controller name="quantity" control={control}
                rules={{ required: 'Quantity required', min: { value: 0.001, message: 'Must be > 0' } }}
                render={({ field }) => (
                  <TextField {...field} label="Quantity *" fullWidth size="small" type="number"
                    inputProps={{ step: '0.001', min: '0.001' }}
                    error={!!errors.quantity} helperText={errors.quantity?.message} />
                )} />
            </Grid>
            <Grid item xs={12}>
              <Controller name="notes" control={control}
                render={({ field }) => <TextField {...field} label="Notas" fullWidth size="small" multiline rows={2} />} />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 2 }}>
          <Button onClick={onClose}>Cancel</Button>
          <Button type="submit" variant="contained" disabled={isSubmitting}>
            {isSubmitting ? <CircularProgress size={20} /> : 'Request Transfer'}
          </Button>
        </DialogActions>
      </Box>
    </Dialog>
  )
}
