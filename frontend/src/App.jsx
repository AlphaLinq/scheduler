import { useState } from 'react'
import './App.css'

function App() {
  const [timetable, setTimetable] = useState(null)
  const [cloudbalance, setCloudBalanceTable] = useState(null)
  const [vehiclerouting, setVehicleRouting] = useState(null)
  const [loadingTimetable, setLoadingTimetable] = useState(false)
  const [loadingCloudBalance, setLoadingCloudBalance] = useState(false)
  const [loadingVehicleRouting, setLoadingVehicleRouting] = useState(false)
  const [error, setError] = useState(null)

  const generateTimetable = async () => {
    setLoadingTimetable(true)
    setError(null)
    try {
      const response = await fetch('http://localhost:8080/api/timetable/demo')
      if (!response.ok) {
        throw new Error('Failed to generate timetable')
      }
      const data = await response.json()
      setTimetable(data)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoadingTimetable(false)
    }
  }


  /*
    Backend adja:
    lessonList = [
      { subject: "Math", timeSlot: "MONDAY-09:00", room: "A101" },
      { subject: "English", timeSlot: "MONDAY-09:00", room: "A102" }
    ]

    Eredmény:
    {
      "MONDAY-09:00": {
        timeSlot: { dayOfWeek: "MONDAY", startTime: "09:00" },
        rooms: {
          "A101": [{ subject: "Math", ... }],
          "A102": [{ subject: "English", ... }]
        }
      }
    }
   */
  const groupLessonsByTimeSlotAndRoom = () => {
    if (!timetable) return {}
    
    const grouped = {}
    timetable.lessonList.forEach(lesson => {
      if (lesson.timeSlot && lesson.room) {
        const timeSlotKey = `${lesson.timeSlot.dayOfWeek}-${lesson.timeSlot.startTime}`
        if (!grouped[timeSlotKey]) {
          grouped[timeSlotKey] = {
            timeSlot: lesson.timeSlot,
            rooms: {}
          }
        }
        if (!grouped[timeSlotKey].rooms[lesson.room.roomName]) {
          grouped[timeSlotKey].rooms[lesson.room.roomName] = []
        }
        grouped[timeSlotKey].rooms[lesson.room.roomName].push(lesson)
      }
    })
    return grouped
  }

  const getUnassignedLessons = () => {
    if (!timetable) return []
    return timetable.lessonList.filter(lesson => !lesson.timeSlot || !lesson.room)
  }

  const groupedLessons = groupLessonsByTimeSlotAndRoom()
  const unassignedLessons = getUnassignedLessons()

    const generateCloudBalance = async () => {
        setLoadingCloudBalance(true)
        setError(null)
        try {
            const response = await fetch("http://localhost:8080/api/cloudbalance/demo")
            if (!response.ok){
                throw new Error('Failed to generate Cloud balance table')
            }
            const data = await response.json()
            setCloudBalanceTable(data)
        } catch (err) {
            setError(err.message)
        } finally {
            setLoadingCloudBalance(false)
        }
    }

    const generateVehicleRouting = async () => {
        setLoadingVehicleRouting(true)
        setError(null)
        try {
            const response = await fetch("http://localhost:8080/api/vehiclerouting/demo")
            if (!response.ok){
                throw new Error('Failed to generate Vehicle Routing solution')
            }
            const data = await response.json()
            setVehicleRouting(data)
        } catch (err) {
            setError(err.message)
        } finally {
            setLoadingVehicleRouting(false)
        }
    }
  return (
    <div className="app-container">
      <h1>Scheduler</h1>

      <div className="controls">
        <button
          onClick={generateTimetable}
          disabled={loadingTimetable}
          className="generate-btn"
        >
          {loadingTimetable ? 'Generating...' : 'Generate Timetable'}
        </button>
          <button
              onClick={generateCloudBalance}
              disabled={loadingCloudBalance}
              className="generate-btn"
          >
              {loadingCloudBalance ? 'Generating...' : 'Generate Cloud balancing task'}
          </button>
          <button
              onClick={generateVehicleRouting}
              disabled={loadingVehicleRouting}
              className="generate-btn"
          >
              {loadingVehicleRouting ? 'Generating...' : 'Generate Vehicle Routing'}
          </button>
      </div>

      {error && <div className="error">Error: {error}</div>}

      {timetable && (
        <div className="timetable-container">
            <div className="section-header">
                <h2>Time Table Solution</h2>
                <p className="descrtiption">
                    Your application will assign Lesson instances to Timeslot and Room instances automatically by using AI to adhere to hard and soft scheduling constraints, for example:
                    <ul>
                        <li>A room can have at most one lesson at the same time.</li>
                        <li>A teacher can teach at most one lesson at the same time.</li>
                        <li>A student can attend at most one lesson at the same time.</li>
                        <li>A teacher prefers to teach all lessons in the same room.</li>
                        <li>A teacher prefers to teach sequential lessons and dislikes gaps between lessons.</li>
                        <li>A student dislikes sequential lessons on the same subject.</li>
                    </ul>
                    <br/>
                    Mathematically speaking, school timetabling is an NP-hard problem. This means it is difficult to scale. Simply brute force iterating through all possible combinations takes millions of years for a non-trivial dataset, even on a supercomputer. Fortunately, AI constraint solvers such as OptaPlanner have advanced algorithms that deliver a near-optimal solution in a reasonable amount of time.
                    <br/> <br/>
                    Score meanings:
                        <ul>
                            <li><b>Hard constraints</b> must not be broken. For example: A room can have at most one lesson at the same time.</li>
                            <li><b>Soft constraints</b> should not be broken. For example: A teacher prefers to teach in a single room.</li>
                        </ul>
                </p>
            </div>
          <div className="score-info">
            <h3>Score: {timetable.score?.hardScore || 0} hard / {timetable.score?.softScore || 0} soft</h3>
          </div>

          <div className="table-wrapper">
            <table className="timetable">
              <thead>
                <tr>
                  <th>Time</th>
                  {timetable.roomList.map(room => (
                    <th key={room.roomName}>{room.roomName}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {timetable.timeSlotList.map(timeSlot => {
                  const timeSlotKey = `${timeSlot.dayOfWeek}-${timeSlot.startTime}`
                  const timeSlotData = groupedLessons[timeSlotKey]

                  return (
                    <tr key={timeSlotKey}>
                      <td className="time-cell">
                        <div className="day">{timeSlot.dayOfWeek.substring(0, 3)}</div>
                        <div className="time">{timeSlot.startTime}</div>
                      </td>
                      {timetable.roomList.map(room => {
                        const lessons = timeSlotData?.rooms[room.roomName] || []
                        return (
                          <td key={room.roomName} className="lesson-cell">
                            {lessons.map(lesson => (
                              <div key={lesson.id} className="lesson">
                                <div className="subject">{lesson.subject}</div>
                                <div className="teacher">{lesson.teacher}</div>
                                <div className="student-group">{lesson.studentGroup}</div>
                              </div>
                            ))}
                          </td>
                        )
                      })}
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>

          {unassignedLessons.length > 0 && (
            <div className="unassigned-section">
              <h3>Unassigned Lessons</h3>
              <ul>
                {unassignedLessons.map(lesson => (
                  <li key={lesson.id}>
                    {lesson.subject} - {lesson.teacher} - {lesson.studentGroup}
                  </li>
                ))}
              </ul>
            </div>
          )}
        </div>
      )}

        {cloudbalance && (
            <div className="cloudbalance-container">
                <div className="section-header">
                    <h2>Cloud Balance Solution</h2>
                    <p className="descrtiption">
                        Suppose your company owns a number of cloud computers and needs to run a number of processes on those computers. Assign each process to a computer.
                        <br/>
                        The following hard constraints must be fulfilled:
                        <ul>
                            <li> Every computer must be able to handle the minimum hardware requirements of the sum of its processes:</li>
                            <ul>
                                <li>CPU capacity: The CPU power of a computer must be at least the sum of the CPU power required by the processes assigned to that computer.</li>
                                <li>Memory capacity: The RAM memory of a computer must be at least the sum of the RAM memory required by the processes assigned to that computer.</li>
                                <li>Network capacity: The network bandwidth of a computer must be at least the sum of the network bandwidth required by the processes assigned to that computer.</li>
                            </ul>
                        </ul>

                        The following soft constraints should be optimized:
                        <ul>
                            <li>Each computer that has one or more processes assigned, incurs a maintenance cost (which is fixed per computer).</li>
                            <ul>
                                <li>Cost: Minimize the total maintenance cost.</li>
                            </ul>
                        </ul>
                        This problem is a form of bin packing. The following is a simplified example:
                        <br/>
                        OptaPlanner assigned {cloudbalance.processList?.length || 0} processes
                        to {cloudbalance.computerList?.filter(c =>
                        cloudbalance.processList?.some(p => p.computer?.id === c.id)
                    ).length || 0} computers
                    </p>
                </div>
                <div className="score-info">
                    <h3>Score: {cloudbalance.score?.hardScore || 0} hard / {cloudbalance.score?.softScore || 0} soft</h3>
                </div>

                <div className="cloudbalance-section">
                    <h3>Computers ({cloudbalance.computerList?.length || 0})</h3>
                    <div className="table-wrapper">
                        <table className="cloudbalance-table">
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>CPU Power</th>
                                    <th>Memory</th>
                                    <th>Cost</th>
                                    <th>Assigned Processes</th>
                                </tr>
                            </thead>
                            <tbody>
                                {cloudbalance.computerList?.map(computer => {
                                    const assignedProcesses = cloudbalance.processList?.filter(
                                        p => p.computer?.id === computer.id
                                    ) || []
                                    return (
                                        <tr key={computer.id}>
                                            <td>{computer.id}</td>
                                            <td>{computer.cpuPower}</td>
                                            <td>{computer.memory}</td>
                                            <td>{computer.cost}</td>
                                            <td>
                                                {assignedProcesses.length > 0 ? (
                                                    <ul style={{ margin: 0, paddingLeft: '20px' }}>
                                                        {assignedProcesses.map(p => (
                                                            <li key={p.id}>
                                                                Process {p.id} (CPU: {p.requiredCpuPower}, Mem: {p.requiredMemory})
                                                            </li>
                                                        ))}
                                                    </ul>
                                                ) : (
                                                    <span style={{ color: '#999' }}>No processes</span>
                                                )}
                                            </td>
                                        </tr>
                                    )
                                })}
                            </tbody>
                        </table>
                    </div>
                </div>

                <div className="cloudbalance-section">
                    <h3>Processes ({cloudbalance.processList?.length || 0})</h3>
                    <div className="table-wrapper">
                        <table className="cloudbalance-table">
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Required CPU</th>
                                    <th>Required Memory</th>
                                    <th>Assigned Computer</th>
                                </tr>
                            </thead>
                            <tbody>
                                {cloudbalance.processList?.map(process => (
                                    <tr key={process.id} className={!process.computer ? 'unassigned' : ''}>
                                        <td>{process.id}</td>
                                        <td>{process.requiredCpuPower}</td>
                                        <td>{process.requiredMemory}</td>
                                        <td>
                                            {process.computer ? (
                                                <span>Computer {process.computer.id}</span>
                                            ) : (
                                                <span style={{ color: 'red', fontWeight: 'bold' }}>NOT ASSIGNED</span>
                                            )}
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        )}

        {vehiclerouting && (
            <div className="vehiclerouting-container">
                <div className="section-header">
                    <h2>Vehicle Routing Solution (CVRPTW)</h2>
                    <p className="descrtiption">
                        The Capacitated Vehicle Routing Problem with Time Windows (CVRPTW) is a classic optimization problem
                        where vehicles must visit a set of customers, starting and ending at a depot.
                        The goal is to find the optimal routes for a fleet of vehicles to serve all customers.
                        <br/><br/>
                        The following <b>hard constraints</b> must be fulfilled:
                        <ul>
                            <li><b>Vehicle capacity:</b> A vehicle cannot carry more items than its capacity.</li>
                            <li><b>Customer ready time:</b> A vehicle may arrive before the customer's ready time, but it must wait until the ready time before servicing.</li>
                            <li><b>Customer due time:</b> A vehicle must arrive on time, before the customer's due time.</li>
                            <li><b>Service duration:</b> A vehicle must stay at the customer for the length of the service duration.</li>
                            <li><b>Travel time:</b> Traveling from one location to another takes time.</li>
                        </ul>

                        The following <b>soft constraints</b> should be optimized:
                        <ul>
                            <li><b>Total distance:</b> Minimize the total distance driven (fuel consumption) of all vehicles.</li>
                        </ul>
                        <br/>
                        OptaPlanner assigned {vehiclerouting.customerList?.length || 0} customers
                        to {vehiclerouting.vehicleList?.filter(v =>
                            vehiclerouting.customerList?.some(c => c.vehicle?.id === v.id)
                        ).length || 0} vehicles
                    </p>
                </div>
                <div className="score-info">
                    <h3>Score: {vehiclerouting.score?.hardScore || 0} hard / {vehiclerouting.score?.softScore || 0} soft</h3>
                </div>

                <div className="cloudbalance-section">
                    <h3>Vehicles ({vehiclerouting.vehicleList?.length || 0})</h3>
                    <div className="table-wrapper">
                        <table className="cloudbalance-table">
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Name</th>
                                    <th>Capacity</th>
                                    <th>Depot</th>
                                    <th>Assigned Customers</th>
                                    <th>Total Demand</th>
                                </tr>
                            </thead>
                            <tbody>
                                {vehiclerouting.vehicleList?.map(vehicle => {
                                    const assignedCustomers = vehiclerouting.customerList?.filter(
                                        c => c.vehicle?.id === vehicle.id
                                    ) || []
                                    const totalDemand = assignedCustomers.reduce((sum, c) => sum + c.demand, 0)
                                    return (
                                        <tr key={vehicle.id}>
                                            <td>{vehicle.id}</td>
                                            <td>{vehicle.name}</td>
                                            <td>{vehicle.capacity}</td>
                                            <td>{vehicle.depot.name}</td>
                                            <td>
                                                {assignedCustomers.length > 0 ? (
                                                    <ul style={{ margin: 0, paddingLeft: '20px' }}>
                                                        {assignedCustomers.map(c => (
                                                            <li key={c.id}>
                                                                {c.name} (Demand: {c.demand})
                                                            </li>
                                                        ))}
                                                    </ul>
                                                ) : (
                                                    <span style={{ color: '#999' }}>No customers</span>
                                                )}
                                            </td>
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
                    <div className="table-wrapper">
                        <table className="cloudbalance-table">
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Name</th>
                                    <th>Location</th>
                                    <th>Demand</th>
                                    <th>Service Duration</th>
                                    <th>Ready Time</th>
                                    <th>Due Time</th>
                                    <th>Assigned Vehicle</th>
                                </tr>
                            </thead>
                            <tbody>
                                {vehiclerouting.customerList?.map(customer => (
                                    <tr key={customer.id} className={!customer.vehicle ? 'unassigned' : ''}>
                                        <td>{customer.id}</td>
                                        <td>{customer.name}</td>
                                        <td>
                                            {customer.location.name}
                                            <br/>
                                            <small style={{ color: '#666' }}>
                                                ({customer.location.latitude.toFixed(4)}, {customer.location.longitude.toFixed(4)})
                                            </small>
                                        </td>
                                        <td>{customer.demand}</td>
                                        <td>{customer.serviceDuration} min</td>
                                        <td>{customer.readyTime !== null ? `${customer.readyTime} min` : 'N/A'}</td>
                                        <td>{customer.dueTime !== null ? `${customer.dueTime} min` : 'N/A'}</td>
                                        <td>
                                            {customer.vehicle ? (
                                                <span>{customer.vehicle.name}</span>
                                            ) : (
                                                <span style={{ color: 'red', fontWeight: 'bold' }}>NOT ASSIGNED</span>
                                            )}
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        )}

    </div>
  )
}

export default App
