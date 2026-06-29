import { describe, expect, it } from 'vitest'
import { screen } from '@testing-library/react'
import { renderPage } from '../test/render'

import Login from './Login'
import Register from './Register'
import Dashboard from './Dashboard'
import MeetingsList from './MeetingsList'
import MeetingDetail from './MeetingDetail'
import MeetingForm from './MeetingForm'
import ActionItemsList from './ActionItemsList'
import ActionItemForm from './ActionItemForm'
import UsersList from './UsersList'

describe('page smoke tests', () => {
  it('Login renders the sign-in form', async () => {
    renderPage(<Login />, { route: '/login', path: '/login' })
    expect(await screen.findByText('Sign in to your account')).toBeInTheDocument()
  })

  it('Register renders the create-account form', async () => {
    renderPage(<Register />, { route: '/register', path: '/register' })
    expect(await screen.findByText('Create your account')).toBeInTheDocument()
  })

  it('Dashboard renders after loading', async () => {
    renderPage(<Dashboard />, { route: '/', path: '/' })
    expect(await screen.findByRole('heading', { name: 'Dashboard' })).toBeInTheDocument()
  })

  it('MeetingsList renders', async () => {
    renderPage(<MeetingsList />, { route: '/meetings', path: '/meetings' })
    expect(await screen.findByRole('heading', { name: 'Meetings' })).toBeInTheDocument()
  })

  it('MeetingDetail renders the meeting title', async () => {
    renderPage(<MeetingDetail />, { route: '/meetings/1', path: '/meetings/:id' })
    expect(await screen.findByRole('heading', { name: 'Test Meeting' })).toBeInTheDocument()
  })

  it('MeetingForm renders in create mode', async () => {
    renderPage(<MeetingForm />, { route: '/meetings/new', path: '/meetings/new' })
    expect(await screen.findByRole('heading', { name: 'New Meeting' })).toBeInTheDocument()
  })

  it('ActionItemsList renders', async () => {
    renderPage(<ActionItemsList />, { route: '/action-items', path: '/action-items' })
    expect(await screen.findByRole('heading', { name: 'Action Items' })).toBeInTheDocument()
  })

  it('ActionItemForm renders in create mode', async () => {
    renderPage(<ActionItemForm />, { route: '/action-items/new', path: '/action-items/new' })
    expect(await screen.findByRole('heading', { name: 'New Action Item' })).toBeInTheDocument()
  })

  it('UsersList renders', async () => {
    renderPage(<UsersList />, { route: '/users', path: '/users' })
    expect(await screen.findByRole('heading', { name: 'Users' })).toBeInTheDocument()
  })
})
