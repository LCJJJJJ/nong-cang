import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'

import AuthSessionProvider from '../features/auth/AuthSessionProvider'
import GuestOnlyRoute from '../features/auth/GuestOnlyRoute'
import RequireAuth from '../features/auth/RequireAuth'
import AuthLayout from '../layouts/auth/AuthLayout'
import MainLayout from '../layouts/main/MainLayout'
import LoginPage from '../pages/auth/login/LoginPage'
import HomePage from '../pages/home/HomePage'
import ProductOriginPage from '../pages/product-origin/ProductOriginPage'
import ProductUnitPage from '../pages/product-unit/ProductUnitPage'
import QualityGradePage from '../pages/quality-grade/QualityGradePage'
import ShelfLifeRulePage from '../pages/shelf-life-rule/ShelfLifeRulePage'
import StorageConditionPage from '../pages/storage-condition/StorageConditionPage'

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
            <Route path="/product-origins" element={<ProductOriginPage />} />
            <Route path="/product-units" element={<ProductUnitPage />} />
            <Route path="/quality-grades" element={<QualityGradePage />} />
            <Route path="/shelf-life-rules" element={<ShelfLifeRulePage />} />
            <Route path="/storage-conditions" element={<StorageConditionPage />} />
          </Route>
          </Route>

          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </AuthSessionProvider>
    </BrowserRouter>
  )
}

export default App
