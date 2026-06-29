import { describe, expect, it } from 'vitest'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { renderPage } from '../test/render'
import { server, http, HttpResponse, BASE } from '../test/server'
import ActionItemsList from './ActionItemsList'

const baseItem = {
  id: 1,
  title: 'Wire the thing',
  description: 'Initial description',
  status: 'OPEN',
  priority: 'MEDIUM',
  dueDate: '2026-12-31',
  meetingId: 5,
  assigneeId: null,
  assigneeName: null,
}

describe('ActionItemsList — status cycle', () => {
  it('clicking the status badge cycles OPEN → IN_PROGRESS via PUT', async () => {
    let putBody = null
    server.use(
      http.get(`${BASE}/action-items`, () => HttpResponse.json([baseItem])),
      http.put(`${BASE}/action-items/1`, async ({ request }) => {
        putBody = await request.json()
        return HttpResponse.json({ ...baseItem, status: putBody.status })
      }),
    )

    const user = userEvent.setup()
    renderPage(<ActionItemsList />, { route: '/action-items', path: '/action-items' })

    expect(await screen.findByText('Wire the thing')).toBeInTheDocument()

    const statusBtn = await screen.findByRole('button', {
      name: /Status: OPEN\. Click to cycle\./i,
    })
    await user.click(statusBtn)

    await screen.findByRole('button', {
      name: /Status: IN PROGRESS\. Click to cycle\./i,
    })

    expect(putBody).toMatchObject({
      title: 'Wire the thing',
      description: 'Initial description',
      dueDate: '2026-12-31',
      priority: 'MEDIUM',
      meetingId: 5,
      status: 'IN_PROGRESS',
    })
    expect(putBody.assigneeId).toBeUndefined()
  })

  it('cycle wraps CANCELLED → OPEN', async () => {
    const cancelledItem = { ...baseItem, status: 'CANCELLED' }
    let putBody = null
    server.use(
      http.get(`${BASE}/action-items`, () => HttpResponse.json([cancelledItem])),
      http.put(`${BASE}/action-items/1`, async ({ request }) => {
        putBody = await request.json()
        return HttpResponse.json({ ...cancelledItem, status: putBody.status })
      }),
    )

    const user = userEvent.setup()
    renderPage(<ActionItemsList />, { route: '/action-items', path: '/action-items' })

    const statusBtn = await screen.findByRole('button', {
      name: /Status: CANCELLED\. Click to cycle\./i,
    })
    await user.click(statusBtn)

    await screen.findByRole('button', {
      name: /Status: OPEN\. Click to cycle\./i,
    })
    expect(putBody.status).toBe('OPEN')
  })
})
