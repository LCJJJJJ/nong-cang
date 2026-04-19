import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'

import AuthSessionProvider from '../features/auth/AuthSessionProvider'
import GuestOnlyRoute from '../features/auth/GuestOnlyRoute'
import RequireAuth from '../features/auth/RequireAuth'
import AuthLayout from '../layouts/auth/AuthLayout'
import MainLayout from '../layouts/main/MainLayout'
import LoginPage from '../pages/auth/login/LoginPage'
import HomePage from '../pages/home/HomePage'
import CustomerPage from '../pages/customer/CustomerPage'
import InboundOrderPage from '../pages/inbound-order/InboundOrderPage'
import InboundRecordPage from '../pages/inbound-record/InboundRecordPage'
import OutboundOrderPage from '../pages/outbound-order/OutboundOrderPage'
import ProductArchivePage from '../pages/product-archive/ProductArchivePage'
import ProductOriginPage from '../pages/product-origin/ProductOriginPage'
import ProductUnitPage from '../pages/product-unit/ProductUnitPage'
import PutawayTaskPage from '../pages/putaway-task/PutawayTaskPage'
import QualityGradePage from '../pages/quality-grade/QualityGradePage'
import StorageConditionPage from '../pages/storage-condition/StorageConditionPage'
import SupplierPage from '../pages/supplier/SupplierPage'
import WarehousePage from '../pages/warehouse/WarehousePage'
import WarehouseLocationPage from '../pages/warehouse-location/WarehouseLocationPage'
import WarehouseZonePage from '../pages/warehouse-zone/WarehouseZonePage'

function App() {
  return (
    <BrowserRouter>
      <AuthSessionProvider>
        <Routes>
          <Route element={<GuestOnlyRoute />}>
            <Route element={<AuthLayout />}>
              <Route path="/login" element={<LoginPage />} />
            </Route>
          </Route>

          <Route element={<RequireAuth />}>
            <Route element={<MainLayout />}>
              <Route path="/" element={<HomePage />} />
              <Route path="/product-archives" element={<ProductArchivePage />} />
              <Route path="/product-origins" element={<ProductOriginPage />} />
              <Route path="/product-units" element={<ProductUnitPage />} />
              <Route path="/quality-grades" element={<QualityGradePage />} />
              <Route path="/customers" element={<CustomerPage />} />
              <Route path="/inbound-orders" element={<InboundOrderPage />} />
              <Route path="/putaway-tasks" element={<PutawayTaskPage />} />
              <Route path="/inbound-records" element={<InboundRecordPage />} />
              <Route path="/outbound-orders" element={<OutboundOrderPage />} />
              <Route path="/storage-conditions" element={<StorageConditionPage />} />
              <Route path="/suppliers" element={<SupplierPage />} />
              <Route path="/warehouses" element={<WarehousePage />} />
              <Route
                path="/warehouse-locations"
                element={<WarehouseLocationPage />}
              />
              <Route path="/warehouse-zones" element={<WarehouseZonePage />} />
            </Route>
          </Route>

          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </AuthSessionProvider>
    </BrowserRouter>
  )
}

export default App
