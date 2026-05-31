import React, { useEffect, useState } from 'react'
import {
  Grid, Card, CardContent, Typography, Box, Chip, CircularProgress,
  Alert, Table, TableBody, TableCell, TableHead, TableRow, Paper
} from '@mui/material'
import WarningAmberIcon from '@mui/icons-material/WarningAmber'
import SwapHorizIcon from '@mui/icons-material/SwapHoriz'
import TrendingUpIcon from '@mui/icons-material/TrendingUp'
import AttachMoneyIcon from '@mui/icons-material/AttachMoney'
import { dashboardApi } from '../api/dashboardApi'
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer } from 'recharts'

const StatCard = ({ icon, label, value, color, subtitle }) => (
  <Card>
    <CardContent>
      <Box sx={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between' }}>
        <Box>
          <Typography variant="body2" color="text.secondary">{label}</Typography>
          <Typography variant="h4" fontWeight={700} sx={{ my: 0.5 }}>{value}</Typography>
          {subtitle && <Typography variant="caption" color="text.secondary">{subtitle}</Typography>}
        </Box>
        <Box sx={{ p: 1.5, borderRadius: 2, bgcolor: `${color}.lighter` || '#f5f5f5' }}>
          {React.cloneElement(icon, { sx: { color: `${color}.main`, fontSize: 28 } })}
        </Box>
      </Box>
    </CardContent>
  </Card>
)

export default function DashboardPage() {
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    dashboardApi.getDashboard()
      .then(r => setData(r.data))
      .catch(() => setError('Error cargando dashboard'))
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <Box sx={{ display: 'flex', justifyContent: 'center', mt: 8 }}><CircularProgress /></Box>
  if (error) return <Alert severity="error">{error}</Alert>

  const chartData = data?.topMovedProducts?.map(p => ({ name: p.productName.slice(0, 12), movimientos: p.movementCount })) || []

  return (
    <Box>
      <Typography variant="h5" fontWeight={700} mb={3}>Dashboard</Typography>

      <Grid container spacing={3} mb={3}>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard icon={<WarningAmberIcon />} label="Stock Bajo Mínimo"
            value={data?.lowStockCount ?? 0} color="warning" subtitle="productos" />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard icon={<SwapHorizIcon />} label="Transferencias Pendientes"
            value={data?.pendingTransfersCount ?? 0} color="info" subtitle="por aprobar" />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard icon={<TrendingUpIcon />} label="Movimientos del Mes"
            value={data?.monthlyMovementsCount ?? 0} color="success" subtitle="últimos 30 días" />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard icon={<AttachMoneyIcon />} label="Valor Total Stock"
            value={`$${(data?.totalStockValue ?? 0).toLocaleString('es-MX', { minimumFractionDigits: 0 })}`}
            color="primary" subtitle="valorizado" />
        </Grid>
      </Grid>

      <Grid container spacing={3}>
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" fontWeight={600} mb={2}>Productos con Bajo Stock</Typography>
              {data?.lowStockItems?.length === 0
                ? <Typography color="text.secondary">No hay productos con bajo stock</Typography>
                : (
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell>Producto</TableCell>
                        <TableCell>Sucursal</TableCell>
                        <TableCell align="right">Stock</TableCell>
                        <TableCell align="right">Mínimo</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {data?.lowStockItems?.slice(0, 8).map(item => (
                        <TableRow key={item.id}>
                          <TableCell>{item.productSku}</TableCell>
                          <TableCell>{item.branchName}</TableCell>
                          <TableCell align="right">
                            <Chip label={item.currentStock} color="error" size="small" />
                          </TableCell>
                          <TableCell align="right">{item.minStock}</TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                )
              }
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" fontWeight={600} mb={2}>Productos Más Movidos (30 días)</Typography>
              {chartData.length > 0 ? (
                <ResponsiveContainer width="100%" height={250}>
                  <BarChart data={chartData}>
                    <XAxis dataKey="name" />
                    <YAxis />
                    <Tooltip />
                    <Bar dataKey="movimientos" fill="#1976d2" radius={[4,4,0,0]} />
                  </BarChart>
                </ResponsiveContainer>
              ) : <Typography color="text.secondary">Sin datos de movimientos</Typography>}
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  )
}
