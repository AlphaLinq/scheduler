function VehicleCapacityGauge({ vehicles, customers }) {
  if (!vehicles || !customers) return null

  return (
    <div className="cloudbalance-section">
      <h3>Vehicle Capacity Utilization</h3>
      <div style={{ display: 'grid', gap: '12px' }}>
        {vehicles.map(vehicle => {
          const assigned = customers.filter(c => c.vehicle?.id === vehicle.id)
          const totalDemand = assigned.reduce((sum, c) => sum + c.demand, 0)
          const pct = vehicle.capacity > 0 ? (totalDemand / vehicle.capacity) * 100 : 0
          const overflow = totalDemand > vehicle.capacity

          return (
            <div key={vehicle.id} className="capacity-gauge">
              <span style={{ minWidth: '100px', fontWeight: 500, color: '#333' }}>
                {vehicle.name}
              </span>
              <div className="capacity-gauge-bar">
                <div
                  className="capacity-gauge-fill"
                  style={{
                    width: `${Math.min(pct, 100)}%`,
                    backgroundColor: overflow ? '#dc3545' : pct > 80 ? '#ffc107' : '#28a745'
                  }}
                />
              </div>
              <span className="capacity-gauge-text" style={{ color: overflow ? '#dc3545' : '#333' }}>
                {totalDemand} / {vehicle.capacity}
              </span>
            </div>
          )
        })}
      </div>
    </div>
  )
}

export default VehicleCapacityGauge
