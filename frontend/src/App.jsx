import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import PrivateRoute from './components/PrivateRoute'
import Layout from './components/Layout'
import Login from './pages/Login'
import Register from './pages/Register'
import Dashboard from './pages/Dashboard'
import MeetingsList from './pages/MeetingsList'
import MeetingDetail from './pages/MeetingDetail'
import MeetingForm from './pages/MeetingForm'
import ActionItemsList from './pages/ActionItemsList'
import ActionItemForm from './pages/ActionItemForm'
import UsersList from './pages/UsersList'

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route element={<PrivateRoute />}>
            <Route element={<Layout />}>
              <Route path="/" element={<Dashboard />} />
              <Route path="/meetings" element={<MeetingsList />} />
              <Route path="/meetings/new" element={<MeetingForm />} />
              <Route path="/meetings/:id" element={<MeetingDetail />} />
              <Route path="/meetings/:id/edit" element={<MeetingForm />} />
              <Route path="/action-items" element={<ActionItemsList />} />
              <Route path="/action-items/new" element={<ActionItemForm />} />
              <Route path="/action-items/:id/edit" element={<ActionItemForm />} />
              <Route path="/users" element={<UsersList />} />
            </Route>
          </Route>
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}
