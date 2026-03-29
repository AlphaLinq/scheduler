function TimeWindowTimeline({ customers }) {
  if (!customers || customers.length === 0) return null

  // Find the max time for scaling
  const allTimes = customers.flatMap(c => [c.readyTime, c.dueTime].filter(t => t != null))
  const maxTime = Math.max(...allTimes, 240)

  const toPercent = (time) => (time / maxTime) * 100

  return (
    <div className="cloudbalance-section">
      <h3>Time Windows</h3>
      <div className="time-window-container">
        {customers.map(customer => {
          const ready = customer.readyTime ?? 0
          const due = customer.dueTime ?? maxTime
          const travelTime = customer.vehicle
            ? Math.round(Math.sqrt(
                Math.pow((customer.vehicle?.depot?.latitude || 0) - customer.location.latitude, 2) +
                Math.pow((customer.vehicle?.depot?.longitude || 0) - customer.location.longitude, 2)
              ) * 111 * 60 / 60)
            : null

          const windowLeft = toPercent(ready)
          const windowWidth = toPercent(due) - toPercent(ready)
          const arrivalPct = travelTime != null ? toPercent(travelTime) : null
          const isLate = travelTime != null && travelTime > due

          return (
            <div key={customer.id} className="time-window-row">
              <span className="time-window-label">{customer.name}</span>
              <div className="time-window-bar-track">
                <div
                  className="time-window-bar"
                  style={{
                    left: `${windowLeft}%`,
                    width: `${windowWidth}%`,
                    backgroundColor: '#007bff'
                  }}
                />
                {arrivalPct != null && (
                  <div
                    className="time-window-marker"
                    style={{
                      left: `${Math.min(arrivalPct, 100)}%`,
                      backgroundColor: isLate ? '#dc3545' : '#28a745'
                    }}
                    title={`Arrival: ${travelTime} min ${isLate ? '(LATE)' : '(on time)'}`}
                  />
                )}
              </div>
              <span style={{ minWidth: '80px', fontSize: '12px', color: '#666' }}>
                {ready}-{due} min
              </span>
            </div>
          )
        })}
      </div>
    </div>
  )
}

export default TimeWindowTimeline
