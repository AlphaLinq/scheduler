import { Routes, Route } from 'react-router-dom'
import Navbar from './components/Navbar'
import HomePage from './pages/HomePage'
import TimetablePage from './pages/TimetablePage'
import CloudBalancePage from './pages/CloudBalancePage'
import VehicleRoutingPage from './pages/VehicleRoutingPage'
import './App.css'

function App() {
  return (
    <div className="app-container">
      <Navbar />
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/timetable" element={<TimetablePage />} />
        <Route path="/cloudbalance" element={<CloudBalancePage />} />
        <Route path="/vehiclerouting" element={<VehicleRoutingPage />} />
      </Routes>
    </div>
  )
}

export default App
