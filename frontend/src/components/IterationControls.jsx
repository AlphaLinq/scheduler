function IterationControls({ iterations, currentIndex, onGoToIteration, onPrev, onNext, onShowFinal, solving }) {
  if (!iterations || iterations.length === 0) return null

  const current = currentIndex !== null ? iterations[currentIndex] : null

  return (
    <div className={`iteration-controls ${solving ? 'solving-active' : ''}`}>
      <div className="iteration-info">
        <p>
          Iteration: {currentIndex !== null ? currentIndex + 1 : 0} / {iterations.length}
          {solving && <span className="live-badge">LIVE</span>}
        </p>
        {current && (
          <div className="iteration-details">
            <p>Phase: {current.phaseName}</p>
            <p>Steps: {current.stepCount}</p>
            <p>Time: {current.timeMillis}ms</p>
          </div>
        )}
      </div>
      <div className="iteration-buttons">
        <button
          onClick={onPrev}
          disabled={currentIndex === null || currentIndex === 0}
          className="nav-btn"
        >
          &larr; Previous
        </button>
        <input
          type="range"
          min="0"
          max={iterations.length - 1}
          value={currentIndex !== null ? currentIndex : 0}
          onChange={(e) => onGoToIteration(parseInt(e.target.value))}
          className="iteration-slider"
        />
        <button
          onClick={onNext}
          disabled={currentIndex === null || currentIndex === iterations.length - 1}
          className="nav-btn"
        >
          Next &rarr;
        </button>
        {onShowFinal && !solving && (
          <button onClick={onShowFinal} className="nav-btn final-btn">
            Final Solution
          </button>
        )}
      </div>
    </div>
  )
}

export default IterationControls
