import { Link } from 'react-router-dom'

function HomePage() {
  return (
    <div className="home-container">
      <h1>Scheduler</h1>
      <p className="home-subtitle">OptaPlanner Constraint Solving Demonstrations</p>
      <div className="home-cards">
        <Link to="/timetable" className="problem-card">
          <h2>Timetable Scheduling</h2>
          <p>Assign lessons to timeslots and rooms while respecting teacher and student group constraints.</p>
          <span className="card-link">Solve &rarr;</span>
        </Link>
        <Link to="/cloudbalance" className="problem-card">
          <h2>Cloud Balancing</h2>
          <p>Assign processes to computers while minimizing cost and respecting resource capacity.</p>
          <span className="card-link">Solve &rarr;</span>
        </Link>
        <Link to="/vehiclerouting" className="problem-card">
          <h2>Vehicle Routing</h2>
          <p>Assign customers to vehicles while respecting capacity and time window constraints.</p>
          <span className="card-link">Solve &rarr;</span>
        </Link>
      </div>
    </div>
  )
}

export default HomePage
