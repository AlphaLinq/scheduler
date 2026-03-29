function ConstraintBreakdown({ constraintScores }) {
  if (!constraintScores || Object.keys(constraintScores).length === 0) return null

  const parseScore = (scoreStr) => {
    // Scores look like "0hard/-3soft" or "0hard/0soft"
    const hardMatch = scoreStr.match(/(-?\d+)hard/)
    const softMatch = scoreStr.match(/(-?\d+)soft/)
    return {
      hard: hardMatch ? parseInt(hardMatch[1]) : 0,
      soft: softMatch ? parseInt(softMatch[1]) : 0
    }
  }

  return (
    <div className="constraint-breakdown">
      <h3>Constraint Breakdown</h3>
      {Object.entries(constraintScores).map(([name, scoreStr]) => {
        const score = parseScore(scoreStr)
        const isViolated = score.hard < 0 || score.soft < 0
        return (
          <div key={name} className="constraint-item">
            <span className="constraint-name">{name}</span>
            <span className={`constraint-score ${isViolated ? 'violated' : 'satisfied'}`}>
              {score.hard !== 0 && <span>{score.hard} hard </span>}
              {score.soft !== 0 && <span>{score.soft} soft</span>}
              {score.hard === 0 && score.soft === 0 && <span>OK</span>}
            </span>
          </div>
        )
      })}
    </div>
  )
}

export default ConstraintBreakdown
