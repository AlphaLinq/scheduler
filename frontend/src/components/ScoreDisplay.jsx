function ScoreDisplay({ score }) {
  return (
    <div className="score-info">
      <h3>Score: {score?.hardScore || 0} hard / {score?.softScore || 0} soft</h3>
    </div>
  )
}

export default ScoreDisplay
