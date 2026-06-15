import { useState, useEffect } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import { useApi } from '../api/useApi'

const STATUSES = ['SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED']

const EMPTY = { title: '', description: '', meetingDate: '', status: 'SCHEDULED' }

export default function MeetingForm() {
  const { id } = useParams()
  const isEdit = Boolean(id)
  const api = useApi()
  const navigate = useNavigate()
  const [form, setForm] = useState(EMPTY)
  const [fieldErrors, setFieldErrors] = useState({})
  const [globalError, setGlobalError] = useState(null)
  const [loading, setLoading] = useState(false)
  const [fetching, setFetching] = useState(isEdit)

  useEffect(() => {
    if (!isEdit) return
    api.get(`/meetings/${id}`)
      .then((m) => setForm({ title: m.title, description: m.description || '', meetingDate: m.meetingDate, status: m.status }))
      .catch((err) => setGlobalError(err.message))
      .finally(() => setFetching(false))
  }, [id])

  function handleChange(e) {
    setForm({ ...form, [e.target.name]: e.target.value })
    setFieldErrors({ ...fieldErrors, [e.target.name]: undefined })
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setGlobalError(null)
    setFieldErrors({})
    setLoading(true)
    try {
      if (isEdit) {
        await api.put(`/meetings/${id}`, form)
        navigate(`/meetings/${id}`)
      } else {
        const created = await api.post('/meetings', form)
        navigate(`/meetings/${created.id}`)
      }
    } catch (err) {
      if (err.status === 400 && err.errors) {
        setFieldErrors(err.errors)
      } else {
        setGlobalError(err.message)
      }
    } finally {
      setLoading(false)
    }
  }

  if (fetching) return <div className="page-loading">Loading...</div>

  return (
    <div className="page">
      <div className="breadcrumb">
        <Link to="/meetings" className="link">Meetings</Link>
        <span className="breadcrumb-sep">/</span>
        <span>{isEdit ? 'Edit Meeting' : 'New Meeting'}</span>
      </div>

      <div className="page-header">
        <h1 className="page-title">{isEdit ? 'Edit Meeting' : 'New Meeting'}</h1>
      </div>

      <div className="card form-card">
        <form onSubmit={handleSubmit}>
          {globalError && <div className="alert alert-error">{globalError}</div>}

          <div className="form-group">
            <label className="form-label" htmlFor="title">Title <span className="required">*</span></label>
            <input
              id="title"
              name="title"
              type="text"
              required
              className={`form-input${fieldErrors.title ? ' input-error' : ''}`}
              value={form.title}
              onChange={handleChange}
            />
            {fieldErrors.title && <p className="field-error">{fieldErrors.title}</p>}
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="description">Description</label>
            <textarea
              id="description"
              name="description"
              rows={3}
              className="form-input"
              value={form.description}
              onChange={handleChange}
            />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label className="form-label" htmlFor="meetingDate">Date <span className="required">*</span></label>
              <input
                id="meetingDate"
                name="meetingDate"
                type="date"
                required
                className={`form-input${fieldErrors.meetingDate ? ' input-error' : ''}`}
                value={form.meetingDate}
                onChange={handleChange}
              />
              {fieldErrors.meetingDate && <p className="field-error">{fieldErrors.meetingDate}</p>}
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="status">Status <span className="required">*</span></label>
              <select
                id="status"
                name="status"
                className="form-input"
                value={form.status}
                onChange={handleChange}
              >
                {STATUSES.map((s) => (
                  <option key={s} value={s}>{s.replace('_', ' ')}</option>
                ))}
              </select>
            </div>
          </div>

          <div className="form-actions">
            <button type="button" className="btn btn-secondary" onClick={() => navigate(-1)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Saving...' : isEdit ? 'Save Changes' : 'Create Meeting'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
