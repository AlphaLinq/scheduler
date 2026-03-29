# Scheduler - Improvement Plan

> **Start date:** Feb 27, 2026 (4 weeks behind as of Mar 27, 2026)
> **Total duration:** 8 weeks

---

## Week 1 (Feb 27 - Mar 5): Testing & Error Handling

### Backend Testing
- [ ] Unit tests for all 3 ConstraintProviders using OptaPlanner's `ConstraintVerifier`
  - Timetable: room/teacher/studentGroup conflict tests, soft constraint tests
  - Cloud Balancing: CPU/memory/bandwidth capacity tests, cost minimization test
  - Vehicle Routing: capacity, due time, distance minimization tests
- [ ] Unit tests for deep copy methods in all 3 services (critical for iteration correctness)
- [ ] Integration tests for all REST endpoints (demo + custom solve)
- [ ] Test demo data generation validity

### Error Handling
- [ ] Add `@ControllerAdvice` global exception handler with meaningful error responses
- [ ] Add `@Valid` + Jakarta validation annotations on all `@RequestBody` parameters (`@NotNull`, `@NotEmpty`, `@Positive`, etc.)
- [ ] Add try/catch around `solver.solve()` calls with proper error propagation
- [ ] Add SLF4J logging in services (solver start, completion, errors, iteration count)

**Goal:** 70%+ backend test coverage, no unhandled exceptions.

---

## Week 2 (Mar 6 - Mar 12): Frontend Refactor & Component Decomposition

### Component Split
- [ ] `TimetablePage.jsx` — timetable UI, table, descriptions
- [ ] `CloudBalancePage.jsx` — cloud balancing UI, table, descriptions
- [ ] `VehicleRoutingPage.jsx` — vehicle routing UI, table, descriptions
- [ ] `IterationControls.jsx` — reusable slider/buttons (used by all 3 pages)
- [ ] `ScoreDisplay.jsx` — reusable score rendering component
- [ ] `Navbar.jsx` — navigation between the 3 problem pages

### Routing
- [ ] Install `react-router-dom`
- [ ] Routes: `/timetable`, `/cloudbalance`, `/vehiclerouting`
- [ ] Landing page with problem selection cards

### Custom Problem Input Forms
- [ ] Form UI for each problem's POST endpoints
- [ ] Client-side validation matching backend constraints
- [ ] Inline validation error display

**Goal:** Clean component architecture, proper routing, usable custom input forms.

---

## Week 3 (Mar 13 - Mar 19): Visualizations & Iteration Data

### Backend: Better Iteration Data

#### Phase Detection
- [ ] Detect solver phase automatically in `BestSolutionChangedEvent` listener instead of hardcoded "Solving Phase"
  - Check if all planning variables are assigned: if not -> "Construction Heuristic", if yes -> "Local Search"
  - Example for timetable:
    ```java
    boolean allAssigned = solution.getLessonList().stream()
        .allMatch(l -> l.getTimeSlot() != null && l.getRoom() != null);
    String phaseName = allAssigned ? "Local Search" : "Construction Heuristic";
    ```
  - Apply same pattern to CloudBalance (`process.getComputer() != null`) and VehicleRouting (`customer.getVehicle() != null`)

#### Constraint Match Details via ScoreManager
- [ ] Add `ScoreManager` beans for each domain in `OptaPlannerConfig.java`:
  ```java
  @Bean
  public ScoreManager<TimeTable, HardSoftScore> timeTableScoreManager(
          @Qualifier("timeTableSolverFactory") SolverFactory<TimeTable> sf) {
      return ScoreManager.create(sf);
  }
  ```
- [ ] Call `scoreManager.explainScore(solutionCopy)` inside each `BestSolutionChangedEvent` listener
- [ ] Store per-constraint score breakdown (constraint name + score impact) in each iteration object
- [ ] Add `Map<String, HardSoftScore>` or similar field to `TimeTableIteration`, `CloudBalanceIteration`, `VehicleRoutingIteration`

### Frontend: Score Progression Chart
- [ ] Install charting library (Recharts or Chart.js)
- [ ] Line chart showing hard score and soft score over iterations (X = iteration/time, Y = score)
- [ ] Display alongside iteration slider for each problem
- [ ] Mark phase transitions on the chart (Construction Heuristic -> Local Search boundary)

### Frontend: Timetable Visualization
- [ ] Conflict highlighting in the grid: when two lessons occupy the same room+timeslot, mark the cell with a red border/warning icon
- [ ] Per-teacher schedule view: show each teacher's week as a horizontal timeline so gaps (soft constraint) are visible
- [ ] Dim/fade unassigned lessons in early iterations to show Construction Heuristic progress (slots filling up)

### Frontend: Cloud Balancing Visualization
- [ ] Stacked bar charts per computer for CPU, memory, and bandwidth usage vs capacity
  - Each process is a colored segment in the bar
  - Bar exceeding 100% = hard constraint violation (red overflow indicator)
- [ ] Dim/grey out computers with 0 assigned processes (they don't contribute to cost)
- [ ] Show total cost prominently (sum of active computer costs = the soft score being minimized)

### Frontend: Vehicle Routing Visualization
- [ ] Capacity utilization bar/gauge per vehicle (demand sum vs capacity, red when over)
- [ ] Time window timeline: horizontal bars per customer showing readyTime-dueTime range with arrival time marker
  - Green marker = on time, red marker = late (hard constraint violation)
- [ ] Color-coded vehicle assignment groups (customers visually grouped by assigned vehicle)

### Frontend: Constraint Breakdown Panel (all 3 domains)
- [ ] Render the per-constraint score data from `ScoreManager.explainScore()`
- [ ] Show each constraint name with its score impact (e.g., "Room conflict: -2 hard", "Computer cost: -300 soft")
- [ ] Visual indicator: green check (satisfied) / red X with penalty amount (violated)
- [ ] Updates per iteration step as user navigates with the slider

**Goal:** Thesis-presentation-ready visualizations that show exactly what the solver is doing at each step and why.

---

## Week 4 (Mar 20 - Mar 26): Polish, Documentation & Configurability

### Configurable Demo Data
- [ ] Add problem size parameters to demo endpoints (e.g., `GET /demo?processes=12&computers=6`)
- [ ] Random data generation with seed control for reproducibility
- [ ] Scaling support for larger problem instances

### Solver Configuration
- [ ] Expose termination time as a request parameter or config property
- [ ] Report actual solver phase names (Construction Heuristic vs Local Search) in iteration data instead of hardcoded "Solving Phase"

### API Documentation
- [ ] Add `springdoc-openapi-starter-webmvc-ui` dependency
- [ ] Annotate all endpoints with `@Operation`, `@ApiResponse`, request/response examples
- [ ] Swagger UI available at `/swagger-ui.html`

### Written Documentation
- [ ] Update DOCUMENTATION.md testing section (currently "In Progress")
- [ ] Add performance observations section
- [ ] Add known limitations and future work section
- [ ] Update README with new features and screenshots

**Goal:** Production-ready polish, complete documentation, defensible thesis project.

---

## Week 5 (Mar 27 - Apr 2): Database Persistence & History

### Database Layer
- [ ] Add Spring Data JPA + H2 (dev) / PostgreSQL (prod) dependencies
- [ ] Create entity mappings for all domain objects (separate from OptaPlanner domain classes — use DTOs)
- [ ] Repository interfaces for each domain
- [ ] Service layer methods for CRUD operations

### Solution History
- [ ] Save every solved problem + result to database with timestamp
- [ ] API endpoints: `GET /api/{domain}/history` — list past solutions
- [ ] API endpoint: `GET /api/{domain}/history/{id}` — retrieve specific past solution with iterations
- [ ] Frontend: history sidebar/page showing past runs with score, timestamp, problem size

### DTOs & Mapping
- [ ] Create request/response DTOs separate from OptaPlanner domain classes
- [ ] MapStruct or manual mappers between DTOs and domain objects
- [ ] Decouple API contract from solver internals

**Goal:** Persistent solution history, clean API contract, data survives restarts.

---

## Week 6 (Apr 3 - Apr 9): Performance Benchmarking & Comparison

### Benchmarking Suite
- [ ] Create parameterized benchmark runner that solves problems of increasing size (e.g., 10/50/100/500 lessons)
- [ ] Record: solve time, score, iteration count, memory usage per run
- [ ] Store benchmark results in database or CSV export

### Solver Configuration Comparison
- [ ] Allow selecting solver algorithm (Construction Heuristic only, Tabu Search, Simulated Annealing, Late Acceptance)
- [ ] Side-by-side comparison of same problem solved with different configurations
- [ ] Frontend: comparison table and chart showing algorithm performance differences

### Performance Dashboard
- [ ] Dedicated `/benchmark` page in frontend
- [ ] Charts: solve time vs problem size, score vs time, algorithm comparison
- [ ] Export results as CSV/JSON

**Goal:** Quantitative data showing how OptaPlanner scales, strong thesis content for evaluation chapter.

---

## Week 7 (Apr 10 - Apr 16): Real-Time Solving & UX Improvements

### Real-Time Solver Progress (WebSocket/SSE)
- [ ] Replace synchronous solve with async solving using `SolverManager`
- [ ] Server-Sent Events (SSE) or WebSocket endpoint streaming `BestSolutionChangedEvent` live
- [ ] Frontend: live-updating score, table, and visualizations as solver runs
- [ ] Start/stop/terminate controls during solving

### UX Improvements
- [ ] Dark mode / theme toggle
- [ ] Responsive design improvements for mobile/tablet
- [ ] Loading skeletons instead of plain "Loading..." text
- [ ] Toast notifications for solve completion and errors
- [ ] Keyboard shortcuts for iteration navigation (arrow keys)

### Accessibility
- [ ] ARIA labels on all interactive elements
- [ ] Keyboard navigation support
- [ ] Screen reader friendly table structures
- [ ] Color-blind safe palette for map routes and charts

**Goal:** Live solver experience, polished UX worthy of a demo presentation.

---

## Week 8 (Apr 17 - Apr 23): Deployment, CI/CD & Final Polish

### Docker & Deployment
- [ ] `Dockerfile` for backend (multi-stage: build + runtime)
- [ ] `Dockerfile` for frontend (nginx serving built assets)
- [ ] `docker-compose.yml` orchestrating backend + frontend + PostgreSQL
- [ ] Environment-based configuration (`application-dev.properties`, `application-prod.properties`)

### CI/CD Pipeline
- [ ] GitHub Actions workflow: build, test, lint on every push/PR
- [ ] Backend: `mvn test` with coverage report (JaCoCo)
- [ ] Frontend: `npm run lint` + `npx vitest run` (add Vitest + React Testing Library)
- [ ] Fail pipeline on test failure or coverage drop below threshold

### Frontend Testing
- [ ] Install Vitest + React Testing Library
- [ ] Component tests for each page component
- [ ] Integration tests for API call flows
- [ ] Snapshot tests for table rendering

### Final Polish
- [ ] Review and clean up all TODO/FIXME comments
- [ ] Consistent error messages (Hungarian or English — pick one)
- [ ] Final README update with screenshots, architecture diagram, and full setup guide
- [ ] Tag release `v1.0.0`

**Goal:** Deployable, tested, CI-protected project ready for thesis submission.

---

## Priority Order (if time is tight)

If you fall behind, prioritize in this order — each item is high-impact for a thesis:

1. **Constraint provider tests** (Week 1) — proves correctness, most expected by reviewers
2. **Vehicle routing map** (Week 3) — biggest visual impact for presentation
3. **Score progression chart** (Week 3) — demonstrates solver behavior understanding
4. **Frontend component split + routing** (Week 2) — shows software engineering quality
5. **Error handling + validation** (Week 1) — shows production-mindedness
6. **Benchmarking & algorithm comparison** (Week 6) — strong thesis evaluation content
7. **Real-time solving with SSE** (Week 7) — impressive live demo
8. **Swagger docs** (Week 4) — quick win, looks professional
9. **Database persistence** (Week 5) — expected for a complete application
10. **Docker + CI/CD** (Week 8) — nice to have, shows DevOps awareness
