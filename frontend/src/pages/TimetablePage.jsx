import { useState, useRef } from 'react'
import IterationControls from '../components/IterationControls'
import ScoreDisplay from '../components/ScoreDisplay'
import ScoreChart from '../components/ScoreChart'
import ConstraintBreakdown from '../components/ConstraintBreakdown'

function TimetablePage() {
  const [timetable, setTimetable] = useState(null)
  const [timetableIterations, setTimetableIterations] = useState([])
  const [currentIterationIndex, setCurrentIterationIndex] = useState(null)
  const [loading, setLoading] = useState(false)
  const [solving, setSolving] = useState(false)
  const [error, setError] = useState(null)
  const iterationsRef = useRef([])

  const generateTimetable = () => {
    setLoading(true)
    setSolving(true)
    setError(null)
    setTimetable(null)
    setTimetableIterations([])
    setCurrentIterationIndex(null)
    iterationsRef.current = []

    const eventSource = new EventSource('http://localhost:8080/api/timetable/demo/stream')

    eventSource.addEventListener('iteration', (e) => {
      const iteration = JSON.parse(e.data)
      iterationsRef.current = [...iterationsRef.current, iteration]
      setTimetableIterations([...iterationsRef.current])
      setTimetable(iteration.solution)
      setCurrentIterationIndex(iterationsRef.current.length - 1)
    })

    eventSource.addEventListener('complete', (e) => {
      const data = JSON.parse(e.data)
      setTimetable(data.finalSolution)
      setTimetableIterations(data.iterations)
      setCurrentIterationIndex(data.iterations.length - 1)
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
    if (index >= 0 && index < timetableIterations.length) {
      setCurrentIterationIndex(index)
      setTimetable(timetableIterations[index].solution)
    }
  }

  const showFinalSolution = () => {
    if (timetableIterations.length > 0) {
      goToIteration(timetableIterations.length - 1)
    }
  }

  const groupLessonsByTimeSlotAndRoom = () => {
    if (!timetable) return {}
    const grouped = {}
    timetable.lessonList.forEach(lesson => {
      if (lesson.timeSlot && lesson.room) {
        const timeSlotKey = `${lesson.timeSlot.dayOfWeek}-${lesson.timeSlot.startTime}`
        if (!grouped[timeSlotKey]) {
          grouped[timeSlotKey] = { timeSlot: lesson.timeSlot, rooms: {} }
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
    <div className="page-layout">
      <div className="page-header">
        <h2>Timetable Solution</h2>
        <p className="description">
          Assigns lessons to timeslots and rooms using AI.
          <b> Hard:</b> No room/teacher/student group conflicts.
          <b> Soft:</b> Teacher room stability, consecutive lessons.
        </p>
        <div className="controls">
          <button onClick={generateTimetable} disabled={loading} className="generate-btn">
            {solving ? 'Solving...' : 'Generate Timetable'}
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

      {timetable && (
        <>
          <div className="page-stats">
            <ScoreDisplay score={timetable.score} />
            <div className="stat-pills">
              <span className="stat-pill">{timetable.lessonList?.length || 0} lessons</span>
              <span className="stat-pill">{timetable.roomList?.length || 0} rooms</span>
              <span className="stat-pill">{timetable.timeSlotList?.length || 0} timeslots</span>
              {unassignedLessons.length > 0 && (
                <span className="stat-pill stat-pill-warn">{unassignedLessons.length} unassigned</span>
              )}
            </div>
          </div>

          <IterationControls
            iterations={timetableIterations}
            currentIndex={currentIterationIndex}
            onGoToIteration={goToIteration}
            onPrev={() => goToIteration(currentIterationIndex - 1)}
            onNext={() => goToIteration(currentIterationIndex + 1)}
            onShowFinal={showFinalSolution}
            solving={solving}
          />

          <div className="page-grid">
            <div className="page-grid-main">
              <ScoreChart iterations={timetableIterations} />

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
                            const hasConflict = lessons.length > 1
                            return (
                              <td key={room.roomName} className={`lesson-cell ${hasConflict ? 'conflict-cell' : ''}`}>
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
            </div>

            <div className="page-grid-side">
              {currentIterationIndex !== null && timetableIterations[currentIterationIndex]?.constraintScores && (
                <ConstraintBreakdown constraintScores={timetableIterations[currentIterationIndex].constraintScores} />
              )}

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
          </div>
        </>
      )}
    </div>
  )
}

export default TimetablePage
