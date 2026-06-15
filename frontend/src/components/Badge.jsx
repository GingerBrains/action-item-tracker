const STATUS_CLASSES = {
  SCHEDULED: 'badge badge-scheduled',
  IN_PROGRESS: 'badge badge-in-progress',
  COMPLETED: 'badge badge-completed',
  CANCELLED: 'badge badge-cancelled',
}

const PRIORITY_CLASSES = {
  CRITICAL: 'badge badge-critical',
  HIGH: 'badge badge-high',
  MEDIUM: 'badge badge-medium',
  LOW: 'badge badge-low',
}

export function StatusBadge({ value }) {
  const cls = STATUS_CLASSES[value] || 'badge badge-cancelled'
  return <span className={cls}>{value?.replace('_', ' ')}</span>
}

export function PriorityBadge({ value }) {
  const cls = PRIORITY_CLASSES[value] || 'badge badge-low'
  return <span className={cls}>{value}</span>
}
