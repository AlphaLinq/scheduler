import { NavLink } from 'react-router-dom'

function Navbar() {
  return (
    <nav className="navbar">
      <NavLink to="/" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'} end>
        Home
      </NavLink>
      <NavLink to="/timetable" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>
        Timetable
      </NavLink>
      <NavLink to="/cloudbalance" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>
        Cloud Balance
      </NavLink>
      <NavLink to="/vehiclerouting" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>
        Vehicle Routing
      </NavLink>
    </nav>
  )
}

export default Navbar
