import { useState, useRef } from 'react'
import IterationControls from '../components/IterationControls'
import ScoreDisplay from '../components/ScoreDisplay'
import ScoreChart from '../components/ScoreChart'
import ConstraintBreakdown from '../components/ConstraintBreakdown'
import VehicleCapacityGauge from '../components/VehicleCapacityGauge'
import TimeWindowTimeline from '../components/TimeWindowTimeline'

function VehicleRoutingPage() {
  const [vehiclerouting, setVehicleRouting] = useState(null)
  const [iterations, setIterations] = useState([])
  const [currentIndex, setCurrentIndex] = useState(null)
  const [loading, setLoading] = useState(false)
  const [solving, setSolving] = useState(false)
  const [error, setError] = useState(null)
  const iterationsRef = useRef([])

  const generate = () => {
    setLoading(true)
    setSolving(true)
    setError(null)
    setVehicleRouting(null)
    setIterations([])
    setCurrentIndex(null)
    iterationsRef.current = []

    const eventSource = new EventSource('http://localhost:8080/api/vehiclerouting/demo/stream')

    eventSource.addEventListener('iteration', (e) => {
      const iteration = JSON.parse(e.data)
      iterationsRef.current = [...iterationsRef.current, iteration]
      setIterations([...iterationsRef.current])
      setVehicleRouting(iteration.solution)
      setCurrentIndex(iterationsRef.current.length - 1)
    })

    eventSource.addEventListener('complete', (e) => {
      const data = JSON.parse(e.data)
      setVehicleRouting(data.finalSolution)
      setIterations(data.iterations)
      setCurrentIndex(data.iterations.length - 1)
      setSolving(false)
      setLoading(false)
      eventSource.close()
    })

    eventSource.onerror = () => {
      if (solving) setError('Connection lost during solving')
      setSolving(false)
      setLoading(false)
      eventSource.close()
    }
  }

  const goToIteration = (index) => {
    if (index >= 0 && index < iterations.length) {
      setCurrentIndex(index)
      setVehicleRouting(iterations[index].solution)
    }
  }

  const activeVehicles = vehiclerouting?.vehicleList?.filter(v =>
    vehiclerouting.customerList?.some(c => c.vehicle?.id === v.id)
  ).length || 0

  return (
    <div className="page-layout">
      <div className="page-header">
        <h2>Vehicle Routing Solution (CVRPTW)</h2>
        <p className="description">
          Assign customers to vehicles while minimizing total travel distance.
          <b> Hard:</b> Vehicle capacity, customer due time.
          <b> Soft:</b> Minimize total distance.
        </p>
        <div className="controls">
          <button onClick={generate} disabled={loading} className="generate-btn">
            {solving ? 'Solving...' : 'Generate Vehicle Routing'}
          </button>
          {solving && (
            <div className="solving-indicator">
              <div className="solving-pulse" />
              <span>Solver running — watching live iterations</span>
            </div>
          )}
        </div>
      </div>

      {error && <div className="error">Error: {error}</div>}

      {vehiclerouting && (
        <>
          <div className="page-stats">
            <ScoreDisplay score={vehiclerouting.score} />
            <div className="stat-pills">
              <span className="stat-pill">{vehiclerouting.customerList?.length || 0} customers</span>
              <span className="stat-pill">{activeVehicles} / {vehiclerouting.vehicleList?.length || 0} vehicles active</span>
            </div>
          </div>

          <IterationControls
            iterations={iterations}
            currentIndex={currentIndex}
            onGoToIteration={goToIteration}
            onPrev={() => goToIteration(currentIndex - 1)}
            onNext={() => goToIteration(currentIndex + 1)}
            solving={solving}
          />

          <div className="page-grid">
            <div className="page-grid-main">
              <ScoreChart iterations={iterations} />

              <VehicleCapacityGauge
                vehicles={vehiclerouting.vehicleList}
                customers={vehiclerouting.customerList}
              />

              <TimeWindowTimeline customers={vehiclerouting.customerList} />
            </div>

            <div className="page-grid-side">
              {currentIndex !== null && iterations[currentIndex]?.constraintScores && (
                <ConstraintBreakdown constraintScores={iterations[currentIndex].constraintScores} />
              )}

              <div className="cloudbalance-section">
                <h3>Vehicles ({vehiclerouting.vehicleList?.length || 0})</h3>
                <div className="table-wrapper">
                  <table className="cloudbalance-table">
                    <thead>
                      <tr>
                        <th>ID</th>
                        <th>Name</th>
                        <th>Capacity</th>
                        <th>Customers</th>
                        <th>Demand</th>
                      </tr>
                    </thead>
                    <tbody>
                      {vehiclerouting.vehicleList?.map(vehicle => {
                        const assigned = vehiclerouting.customerList?.filter(
                          c => c.vehicle?.id === vehicle.id
                        ) || []
                        const totalDemand = assigned.reduce((sum, c) => sum + c.demand, 0)
                        return (
                          <tr key={vehicle.id}>
                            <td>{vehicle.id}</td>
                            <td>{vehicle.name}</td>
                            <td>{vehicle.capacity}</td>
                            <td>{assigned.length}</td>
                            <td style={{
                              fontWeight: 'bold',
                              color: totalDemand > vehicle.capacity ? 'red' : 'green'
                            }}>
                              {totalDemand} / {vehicle.capacity}
                            </td>
                          </tr>
                        )
                      })}
                    </tbody>
                  </table>
                </div>
              </div>

              <div className="cloudbalance-section">
                <h3>Customers ({vehiclerouting.customerList?.length || 0})</h3>
                <div className="table-wrapper table-scroll">
                  <table className="cloudbalance-table">
                    <thead>
                      <tr>
                        <th>Name</th>
                        <th>Demand</th>
                        <th>Due</th>
                        <th>Vehicle</th>
                      </tr>
                    </thead>
                    <tbody>
                      {vehiclerouting.customerList?.map(customer => (
                        <tr key={customer.id} className={!customer.vehicle ? 'unassigned' : ''}>
                          <td style={{ fontSize: '13px' }}>{customer.name}</td>
                          <td>{customer.demand}</td>
                          <td>{customer.dueTime != null ? `${customer.dueTime}m` : '-'}</td>
                          <td>
                            {customer.vehicle ? (
                              <span>{customer.vehicle.name}</span>
                            ) : (
                              <span style={{ color: 'red', fontWeight: 'bold' }}>-</span>
                            )}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </div>
        </>
      )}
    </div>
  )
}

export default VehicleRoutingPage
