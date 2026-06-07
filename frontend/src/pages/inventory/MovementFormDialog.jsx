import React, { useEffect } from 'react'
import {
  Dialog, DialogTitle, DialogContent, DialogActions,
  TextField, Button, Grid, CircularProgress, Alert, Box,
  Select, MenuItem, FormControl, InputLabel, Typography
} from '@mui/material'
import { useForm, Controller } from 'react-hook-form'
import { inventoryApi } from '../../api/inventoryApi'

const REASONS = [
  { value: 'COMPRA', label: 'Compra', type: 'ENTRADA' },
  { value: 'RETURN_INBOUND', label: 'Return (inbound)', type: 'INBOUND' },
  { value: 'AJUSTE_POSITIVO', label: 'Ajuste positivo', type: 'ENTRADA' },
  { value: 'VENTA', label: 'Venta', type: 'SALIDA' },
  { value: 'LOSS', label: 'Loss', type: 'OUTBOUND' },
  { value: 'AJUSTE_NEGATIVO', label: 'Ajuste negativo', type: 'SALIDA' },
]

export default function MovementFormDialog({ open, inventory, onClose, onSaved }) {
  const { control, handleSubmit, reset, watch, formState: { errors, isSubmitting } } = useForm({
    defaultValues: { reason: '', quantity: '', observations: '' }
  })
  const [error, setError] = React.useState('')
  const reason = watch('reason')
  const selectedReason = REASONS.find(r => r.value === reason)

  useEffect(() => {
    if (open) { reset({ reason: '', quantity: '', observations: '' }); setError('') }
  }, [open, reset])

  const onSubmit = async (data) => {
    setError('')
    try {
      await inventoryApi.registerMovement({
        productId: inventory.productId,
        branchId: inventory.branchId,
        reason: data.reason,
        quantity: Number(data.quantity),
        observations: data.observations || null,
      })
      onSaved()
    } catch (err) {
      setError(err.response?.data?.detail || 'Error al registrar movimiento')
    }
  }

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Register Movement</DialogTitle>
      <Box component="form" onSubmit={handleSubmit(onSubmit)}>
        <DialogContent>
          {inventory && (
            <Box sx={{ mb: 2, p: 1.5, bgcolor: 'grey.50', borderRadius: 1 }}>
              <Typography variant="body2" fontWeight={600}>{inventory.productName}</Typography>
              <Typography variant="caption" color="text.secondary">
                {inventory.branchName} · Current stock: {inventory.currentStock} {inventory.unitOfMeasure}
              </Typography>
            </Box>
          )}
          {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
          <Grid container spacing={2}>
            <Grid item xs={12}>
              <Controller name="reason" control={control} rules={{ required: 'Motivo requerido' }}
                render={({ field }) => (
                  <FormControl fullWidth size="small" error={!!errors.reason}>
                    <InputLabel>Motivo *</InputLabel>
                    <Select {...field} label="Motivo *">
                      {REASONS.map(r => (
                        <MenuItem key={r.value} value={r.value}>
                          {r.label} ({r.type})
                        </MenuItem>
                      ))}
                    </Select>
                  </FormControl>
                )} />
            </Grid>
            <Grid item xs={12}>
              <Controller name="quantity" control={control}
                rules={{ required: 'Quantity required', min: { value: 0.001, message: 'Must be > 0' } }}
                render={({ field }) => (
                  <TextField {...field} label="Quantity *" fullWidth size="small" type="number"
                    inputProps={{ step: '0.001', min: '0.001' }}
                    error={!!errors.quantity} helperText={errors.quantity?.message} />
                )} />
            </Grid>
            <Grid item xs={12}>
              <Controller name="observations" control={control}
                render={({ field }) => (
                  <TextField {...field} label="Observaciones" fullWidth size="small" multiline rows={2} />
                )} />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 2 }}>
          <Button onClick={onClose}>Cancel</Button>
          <Button type="submit" variant="contained"
            color={selectedReason?.type === 'SALIDA' ? 'error' : 'primary'}
            disabled={isSubmitting}>
            {isSubmitting ? <CircularProgress size={20} /> : `Register ${selectedReason?.type || ''}`}
          </Button>
        </DialogActions>
      </Box>
    </Dialog>
  )
}
