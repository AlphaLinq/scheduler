import { useState } from 'react'
import './App.css'

function App() {
  const [timetable, setTimetable] = useState(null)
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

  return (
    <div className="app-container">
      <h1>School Timetable Scheduler</h1>

      <div className="controls">
        <button
          onClick={generateTimetable}
          disabled={loading}
          className="generate-btn"
        >
          {loading ? 'Generating...' : 'Generate Timetable'}
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
    </div>
  )
}

export default App
