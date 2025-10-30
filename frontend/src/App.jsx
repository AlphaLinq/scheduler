import { useState } from 'react'
import './App.css'

function App() {
  const [timetable, setTimetable] = useState(null)
  const [cloudbalance, setCloudBalanceTable] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  const generateTimetable = async () => {
    setLoading(true)
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
      setLoading(false)
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
        setLoading(true)
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
            setLoading(false)
        }
    }

  return (
    <div className="app-container">
      <h1>Scheduler</h1>

      <div className="controls">
        <button
          onClick={generateTimetable}
          disabled={loading}
          className="generate-btn"
        >
          {loading ? 'Generating...' : 'Generate Timetable'}
        </button>
          <button
              onClick={generateCloudBalance}
              disabled={loading}
              className="generate-btn"
          >
              {loading ? 'Generating...' : 'Generate Cloud balancing task'}
          </button>
      </div>

      {error && <div className="error">Error: {error}</div>}

      {timetable && (
        <div className="timetable-container">
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
            <div style={{ marginTop: '40px', padding: '20px', border: '2px solid #333', background: '#f5f5f5' }}>
                <h2>Cloud Balance RAW Data:</h2>
                <pre style={{ background: '#fff', padding: '10px', overflow: 'auto' }}>
      {JSON.stringify(cloudbalance, null, 2)}
    </pre>

                <h3>Quick Access:</h3>
                <ul>
                    <li><strong>Computers:</strong> {cloudbalance.computerList?.length || 0} db</li>
                    <li><strong>Processes:</strong> {cloudbalance.processList?.length || 0} db</li>
                    <li><strong>Score:</strong> {cloudbalance.score?.hardScore || 'N/A'} hard / {cloudbalance.score?.softScore || 'N/A'} soft</li>
                </ul>

                <h4>Computers:</h4>
                <ul>
                    {cloudbalance.computerList?.map(c => (
                        <li key={c.id}>ID: {c.id}, CPU: {c.cpuPower}, Mem: {c.memory}, Cost: {c.cost}</li>
                    ))}
                </ul>

                <h4>Processes:</h4>
                <ul>
                    {cloudbalance.processList?.map(p => (
                        <li key={p.id}>
                            ID: {p.id}, CPU: {p.requiredCpuPower}, Mem: {p.requiredMemory},
                            Computer: {p.computer?.id || 'NOT ASSIGNED'}
                        </li>
                    ))}
                </ul>
            </div>
        )}

    </div>
  )
}

export default App
