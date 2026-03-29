function ResourceUtilizationChart({ computers, processes }) {
  if (!computers || !processes) return null

  const getUtilization = (computer) => {
    const assigned = processes.filter(p => p.computer?.id === computer.id)
    const cpuUsed = assigned.reduce((sum, p) => sum + p.requiredCpuPower, 0)
    const memUsed = assigned.reduce((sum, p) => sum + p.requiredMemory, 0)
    const bwUsed = assigned.reduce((sum, p) => sum + p.requiredBandwidth, 0)
    return { cpuUsed, memUsed, bwUsed, processCount: assigned.length }
  }

  const UtilBar = ({ used, capacity, label }) => {
    const pct = capacity > 0 ? Math.min((used / capacity) * 100, 150) : 0
    const overflow = used > capacity
    return (
      <div className="utilization-bar-container">
        <div className="utilization-bar-label">
          <span>{label}</span>
          <span>{used} / {capacity} {overflow ? '(OVER)' : ''}</span>
        </div>
        <div className="utilization-bar-track">
          <div
            className={`utilization-bar-fill ${overflow ? 'danger' : pct > 80 ? 'warning' : 'ok'}`}
            style={{ width: `${Math.min(pct, 100)}%` }}
          />
        </div>
      </div>
    )
  }

  return (
    <div className="cloudbalance-section">
      <h3>Resource Utilization</h3>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '20px' }}>
        {computers.map(computer => {
          const util = getUtilization(computer)
          return (
            <div key={computer.id} style={{
              padding: '16px',
              backgroundColor: util.processCount === 0 ? '#f5f5f5' : 'white',
              borderRadius: '8px',
              border: '1px solid #e0e0e0',
              opacity: util.processCount === 0 ? 0.5 : 1
            }}>
              <div style={{ fontWeight: 'bold', marginBottom: '12px', color: '#0056b3' }}>
                Computer {computer.id} {util.processCount === 0 ? '(idle)' : `(${util.processCount} processes)`}
              </div>
              <UtilBar used={util.cpuUsed} capacity={computer.cpuPower} label="CPU" />
              <UtilBar used={util.memUsed} capacity={computer.memory} label="Memory" />
              <UtilBar used={util.bwUsed} capacity={computer.networkBandwidth} label="Bandwidth" />
            </div>
          )
        })}
      </div>
    </div>
  )
}

export default ResourceUtilizationChart
