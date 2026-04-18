import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'

import AuthSessionProvider from '../features/auth/AuthSessionProvider'
import GuestOnlyRoute from '../features/auth/GuestOnlyRoute'
import RequireAuth from '../features/auth/RequireAuth'
import AuthLayout from '../layouts/auth/AuthLayout'
import MainLayout from '../layouts/main/MainLayout'
import LoginPage from '../pages/auth/login/LoginPage'
import HomePage from '../pages/home/HomePage'
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
