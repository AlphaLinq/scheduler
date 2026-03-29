import { useState, useRef } from 'react'
import IterationControls from '../components/IterationControls'
import ScoreDisplay from '../components/ScoreDisplay'
import ScoreChart from '../components/ScoreChart'
import ConstraintBreakdown from '../components/ConstraintBreakdown'
import ResourceUtilizationChart from '../components/ResourceUtilizationChart'

function CloudBalancePage() {
  const [cloudbalance, setCloudBalance] = useState(null)
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
    setCloudBalance(null)
    setIterations([])
    setCurrentIndex(null)
    iterationsRef.current = []

    const eventSource = new EventSource('http://localhost:8080/api/cloudbalance/demo/stream')

    eventSource.addEventListener('iteration', (e) => {
      const iteration = JSON.parse(e.data)
      iterationsRef.current = [...iterationsRef.current, iteration]
      setIterations([...iterationsRef.current])
      setCloudBalance(iteration.solution)
      setCurrentIndex(iterationsRef.current.length - 1)
    })

    eventSource.addEventListener('complete', (e) => {
      const data = JSON.parse(e.data)
      setCloudBalance(data.finalSolution)
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
      setCloudBalance(iterations[index].solution)
    }
  }

  const activeComputers = cloudbalance?.computerList?.filter(c =>
    cloudbalance.processList?.some(p => p.computer?.id === c.id)
  ).length || 0

  return (
    <div className="page-layout">
      <div className="page-header">
        <h2>Cloud Balance Solution</h2>
        <p className="description">
          Assign processes to computers while minimizing maintenance cost.
          <b> Hard:</b> CPU, memory, bandwidth capacity.
          <b> Soft:</b> Minimize total cost of active computers.
        </p>
        <div className="controls">
          <button onClick={generate} disabled={loading} className="generate-btn">
            {solving ? 'Solving...' : 'Generate Cloud Balancing'}
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

      {cloudbalance && (
        <>
          <div className="page-stats">
            <ScoreDisplay score={cloudbalance.score} />
            <div className="stat-pills">
              <span className="stat-pill">{cloudbalance.processList?.length || 0} processes</span>
              <span className="stat-pill">{activeComputers} / {cloudbalance.computerList?.length || 0} computers active</span>
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

              <ResourceUtilizationChart
                computers={cloudbalance.computerList}
                processes={cloudbalance.processList}
              />
            </div>

            <div className="page-grid-side">
              {currentIndex !== null && iterations[currentIndex]?.constraintScores && (
                <ConstraintBreakdown constraintScores={iterations[currentIndex].constraintScores} />
              )}

              <div className="cloudbalance-section">
                <h3>Computers ({cloudbalance.computerList?.length || 0})</h3>
                <div className="table-wrapper">
                  <table className="cloudbalance-table">
                    <thead>
                      <tr>
                        <th>ID</th>
                        <th>CPU</th>
                        <th>Mem</th>
                        <th>BW</th>
                        <th>Cost</th>
                        <th>Processes</th>
                      </tr>
                    </thead>
                    <tbody>
                      {cloudbalance.computerList?.map(computer => {
                        const assigned = cloudbalance.processList?.filter(
                          p => p.computer?.id === computer.id
                        ) || []
                        return (
                          <tr key={computer.id} className={assigned.length === 0 ? 'inactive-computer' : ''}>
                            <td>{computer.id}</td>
                            <td>{computer.cpuPower}</td>
                            <td>{computer.memory}</td>
                            <td>{computer.networkBandwidth}</td>
                            <td>{computer.cost}</td>
                            <td>{assigned.length > 0 ? assigned.length : '-'}</td>
                          </tr>
                        )
                      })}
                    </tbody>
                  </table>
                </div>
              </div>

              <div className="cloudbalance-section">
                <h3>Processes ({cloudbalance.processList?.length || 0})</h3>
                <div className="table-wrapper table-scroll">
                  <table className="cloudbalance-table">
                    <thead>
                      <tr>
                        <th>ID</th>
                        <th>CPU</th>
                        <th>Mem</th>
                        <th>BW</th>
                        <th>Computer</th>
                      </tr>
                    </thead>
                    <tbody>
                      {cloudbalance.processList?.map(process => (
                        <tr key={process.id} className={!process.computer ? 'unassigned' : ''}>
                          <td>{process.id}</td>
                          <td>{process.requiredCpuPower}</td>
                          <td>{process.requiredMemory}</td>
                          <td>{process.requiredBandwidth}</td>
                          <td>
                            {process.computer ? (
                              <span>#{process.computer.id}</span>
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

export default CloudBalancePage
