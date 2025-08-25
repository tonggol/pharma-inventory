import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { Box } from '@mui/material';

import Layout from './components/Layout';
import PrivateRoute from './components/PrivateRoute';

// Pages
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import MedicineList from './pages/Medicine/MedicineList';
import MedicineDetail from './pages/Medicine/MedicineDetail';
import MedicineForm from './pages/Medicine/MedicineForm';
import StockList from './pages/Stock/StockList';
import StockInbound from './pages/Stock/StockInbound';
import StockOutbound from './pages/Stock/StockOutbound';
import TransactionList from './pages/Transaction/TransactionList';
import Reports from './pages/Reports';
import Settings from './pages/Settings';
import NotFound from './pages/NotFound';

function App() {
  return (
    <Box sx={{ display: 'flex', minHeight: '100vh' }}>
      <Routes>
        {/* Public Routes */}
        <Route path="/login" element={<Login />} />
        
        {/* Private Routes */}
        <Route
          path="/"
          element={
            <PrivateRoute>
              <Layout />
            </PrivateRoute>
          }
        >
          <Route index element={<Navigate to="/dashboard" replace />} />
          <Route path="dashboard" element={<Dashboard />} />
          
          {/* Medicine Routes */}
          <Route path="medicines">
            <Route index element={<MedicineList />} />
            <Route path="new" element={<MedicineForm />} />
            <Route path=":id" element={<MedicineDetail />} />
            <Route path=":id/edit" element={<MedicineForm />} />
          </Route>
          
          {/* Stock Routes */}
          <Route path="stocks">
            <Route index element={<StockList />} />
            <Route path="inbound" element={<StockInbound />} />
            <Route path="outbound" element={<StockOutbound />} />
          </Route>
          
          {/* Transaction Routes */}
          <Route path="transactions" element={<TransactionList />} />
          
          {/* Reports */}
          <Route path="reports" element={<Reports />} />
          
          {/* Settings */}
          <Route path="settings" element={<Settings />} />
        </Route>
        
        {/* 404 */}
        <Route path="*" element={<NotFound />} />
      </Routes>
    </Box>
  );
}

export default App;
