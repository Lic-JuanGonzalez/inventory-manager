import React, { useEffect, useState } from 'react'
import {
  Dialog, DialogTitle, DialogContent, DialogActions,
  TextField, Button, Grid, CircularProgress, Alert, Box,
  Select, MenuItem, FormControl, InputLabel
} from '@mui/material'
import { useForm, Controller } from 'react-hook-form'
import { inventoryApi } from '../../api/inventoryApi'
import { productApi } from '../../api/productApi'
import { branchApi } from '../../api/branchApi'

export default function InitInventoryDialog({ open, onClose, onSaved }) {
  const [products, setProducts] = useState([])
  const [branches, setBranches] = useState([])
  const [error, setError] = React.useState('')
  const { control, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm({
    defaultValues: { productId: '', branchId: '', currentStock: '0', minStock: '0', maxStock: '0' }
  })

  useEffect(() => {
    if (open) {
      reset({ productId: '', branchId: '', currentStock: '0', minStock: '0', maxStock: '0' })
      setError('')
      productApi.getAll({ active: true, size: 200 }).then(r => setProducts(r.data.content)).catch(console.error)
      branchApi.getAllActive().then(r => setBranches(r.data)).catch(console.error)
    }
  }, [open, reset])

  const onSubmit = async (data) => {
    setError('')
    try {
      await inventoryApi.initialize({
        productId: Number(data.productId),
        branchId: Number(data.branchId),
        currentStock: Number(data.currentStock),
        minStock: Number(data.minStock),
        maxStock: Number(data.maxStock),
      })
      onSaved()
    } catch (err) {
      setError(err.response?.data?.detail || 'Error al inicializar inventario')
    }
  }

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Inicializar Inventario</DialogTitle>
      <Box component="form" onSubmit={handleSubmit(onSubmit)}>
        <DialogContent>
          {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
          <Grid container spacing={2}>
            <Grid item xs={12}>
              <Controller name="productId" control={control} rules={{ required: 'Seleccione producto' }}
                render={({ field }) => (
                  <FormControl fullWidth size="small" error={!!errors.productId}>
                    <InputLabel>Producto *</InputLabel>
                    <Select {...field} label="Producto *">
                      {products.map(p => <MenuItem key={p.id} value={p.id}>{p.sku} - {p.name}</MenuItem>)}
                    </Select>
                  </FormControl>
                )} />
            </Grid>
            <Grid item xs={12}>
              <Controller name="branchId" control={control} rules={{ required: 'Seleccione sucursal' }}
                render={({ field }) => (
                  <FormControl fullWidth size="small" error={!!errors.branchId}>
                    <InputLabel>Sucursal *</InputLabel>
                    <Select {...field} label="Sucursal *">
                      {branches.map(b => <MenuItem key={b.id} value={b.id}>{b.name}</MenuItem>)}
                    </Select>
                  </FormControl>
                )} />
            </Grid>
            {['currentStock', 'minStock', 'maxStock'].map(field => (
              <Grid item xs={4} key={field}>
                <Controller name={field} control={control} rules={{ required: 'Requerido', min: 0 }}
                  render={({ field: f }) => (
                    <TextField {...f} label={{ currentStock: 'Stock Inicial', minStock: 'Stock Mín.', maxStock: 'Stock Máx.' }[field]}
                      fullWidth size="small" type="number" inputProps={{ min: '0', step: '0.001' }}
                      error={!!errors[field]} helperText={errors[field]?.message} />
                  )} />
              </Grid>
            ))}
          </Grid>
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 2 }}>
          <Button onClick={onClose}>Cancelar</Button>
          <Button type="submit" variant="contained" disabled={isSubmitting}>
            {isSubmitting ? <CircularProgress size={20} /> : 'Inicializar'}
          </Button>
        </DialogActions>
      </Box>
    </Dialog>
  )
}
