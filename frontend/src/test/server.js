import { http, HttpResponse } from 'msw'
import { setupServer } from 'msw/node'

const BASE = 'http://test-api'

export const defaultHandlers = [
  http.post(`${BASE}/auth/refresh`, () =>
    HttpResponse.json({ accessToken: 'test-tok' }),
  ),
  http.get(`${BASE}/auth/me`, () =>
    HttpResponse.json({ email: 'admin@example.com', role: 'ADMIN' }),
  ),
  http.post(`${BASE}/auth/logout`, () => new HttpResponse(null, { status: 204 })),

  http.get(`${BASE}/meetings`, () => HttpResponse.json([])),
  http.get(`${BASE}/meetings/:id`, ({ params }) =>
    HttpResponse.json({
      id: Number(params.id),
      title: 'Test Meeting',
      meetingDate: '2026-01-01',
      status: 'SCHEDULED',
      description: '',
    }),
  ),

  http.get(`${BASE}/action-items`, () => HttpResponse.json([])),
  http.get(`${BASE}/action-items/:id`, ({ params }) =>
    HttpResponse.json({
      id: Number(params.id),
      title: 'Test Item',
      description: '',
      status: 'OPEN',
      priority: 'MEDIUM',
      dueDate: null,
      meetingId: null,
      assigneeId: null,
    }),
  ),

  http.get(`${BASE}/users`, () => HttpResponse.json([])),
  http.get(`${BASE}/users/:id`, ({ params }) =>
    HttpResponse.json({ id: Number(params.id), email: 'u@example.com', role: 'USER' }),
  ),
]

export const server = setupServer(...defaultHandlers)
export { http, HttpResponse, BASE }
