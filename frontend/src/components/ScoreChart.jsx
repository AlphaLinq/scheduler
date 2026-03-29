import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ReferenceLine, ResponsiveContainer } from 'recharts'

function ScoreChart({ iterations }) {
  if (!iterations || iterations.length === 0) return null

  const chartData = iterations.map((iter, index) => ({
    iteration: index + 1,
    hardScore: iter.score?.hardScore ?? 0,
    softScore: iter.score?.softScore ?? 0,
    timeMs: iter.timeMillis,
    phase: iter.phaseName
  }))

  // Find the first Local Search iteration for a phase boundary marker
  const phaseTransitionIndex = chartData.findIndex(d => d.phase === 'Local Search')

  return (
    <div className="chart-container">
      <h3>Score Progression</h3>
      <ResponsiveContainer width="100%" height={300}>
        <LineChart data={chartData} margin={{ top: 5, right: 30, left: 20, bottom: 5 }}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="iteration" label={{ value: 'Iteration', position: 'insideBottom', offset: -5 }} />
          <YAxis />
          <Tooltip
            formatter={(value, name) => [value, name === 'hardScore' ? 'Hard Score' : 'Soft Score']}
            labelFormatter={(label) => {
              const d = chartData[label - 1]
              return d ? `Iteration ${label} (${d.phase}, ${d.timeMs}ms)` : `Iteration ${label}`
            }}
          />
          <Legend />
          {phaseTransitionIndex > 0 && (
            <ReferenceLine
              x={phaseTransitionIndex + 1}
              stroke="#666"
              strokeDasharray="5 5"
              label={{ value: 'Local Search', position: 'top', fill: '#666', fontSize: 12 }}
            />
          )}
          <Line type="monotone" dataKey="hardScore" stroke="#dc3545" strokeWidth={2} dot={false} name="Hard Score" />
          <Line type="monotone" dataKey="softScore" stroke="#007bff" strokeWidth={2} dot={false} name="Soft Score" />
        </LineChart>
      </ResponsiveContainer>
    </div>
  )
}

export default ScoreChart
