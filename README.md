# Scheduler - OptaPlanner Demonstration Project

##  Projekt Áttekintés

Ez a projekt az **OptaPlanner 9.43.0.Final** használatát demonstrálja három különböző optimalizációs probléma megoldására:

1. ** Timetable** - Iskolai órarend generálás
2. **️ Cloud Balancing** - Folyamatok számítógépekhez rendelése
3. ** Vehicle Routing** - Járműútvonal optimalizáció (in progress)

##  Technológiák

**Backend:**
- Spring Boot 3.5.7
- OptaPlanner 9.43.0.Final
- Java 17.0.16 (OpenJDK)
- Maven

**Frontend:**
- React 19.1.1
- Vite 7.1.7
- JavaScript (JSX)

## Rendszerkövetelmények

**Backend:**
- Java 17+ (JDK) - Telepítve: OpenJDK 17.0.16
- Maven 3.6+
- Ubuntu 22.04 vagy kompatibilis Linux

**Frontend:**
- Node.js 18+
- npm 9+

**Java Telepítés** (ha hiányzik):
```bash
sudo apt update
sudo apt install -y openjdk-17-jdk
```
## Gyors Indítás

### Backend futtatása
```bash
cd backend
mvn clean install
mvn spring-boot:run
```
Backend elérhetősége: `http://localhost:8080`

### Frontend futtatása
```bash
cd frontend
npm install
npm run dev
```
Frontend elérhetősága: `http://localhost:5173`

##  API Endpoints

### Cloud Balancing
```
GET http://localhost:8080/api/cloudbalance/demo
```
Generál és megold egy demo cloud balancing problémát.

### Timetable
```
GET http://localhost:8080/api/timetable/demo
```
Generál és megold egy demo órarend problémát.


##  Projekt Struktúra

```
scheduler/
├── backend/                          # Spring Boot alkalmazás
│   └── src/main/java/.../scheduler/
│       ├── config/                   # OptaPlanner konfiguráció
│       ├── cloudbalancing/           # Cloud Balance modul
│       ├── timetable/                # Timetable modul
│       └── vehiclerouting/           # Vehicle Routing modul
└── frontend/                         # React alkalmazás
    └── src/
        └── App.jsx                   # Fő komponens
```

##  OptaPlanner Koncepciók

### Planning Solution
A teljes problémát reprezentálja (input + output).

### Planning Entity
Objektumok, amelyek értékei változnak (pl. óra, folyamat).

### Planning Variable
Az entitáson belüli mezők, amelyeket az OptaPlanner optimalizál.

### Constraint Provider
Az üzleti szabályokat definiálja (hard és soft megszorítások).

### Score
A megoldás minőségét értékeli:
- **Hard Score**: Kötelező megszorítások (nem sérülhetnek)
- **Soft Score**: Optimalizálandó megszorítások

##  Példa: Cloud Balancing

```java
// Domain osztályok
@PlanningEntity
public class CloudProcess {
    @PlanningVariable(valueRangeProviderRefs = "computerList")
    private Computer computer;  // Ezt optimalizálja az OptaPlanner
}

@PlanningSolution
public class CloudBalance {
    @ValueRangeProvider(id = "computerList")
    private List<Computer> computerList;
    
    @PlanningEntityCollectionProperty
    private List<CloudProcess> cloudProcessList;
    
    @PlanningScore
    private HardSoftScore score;
}

// Használat
Solver<CloudBalance> solver = solverFactory.buildSolver();
CloudBalance solution = solver.solve(problem);
```

##  Konfiguráció

```java
@Bean
public SolverFactory<CloudBalance> cloudBalanceSolverFactory() {
    return SolverFactory.create(new SolverConfig()
        .withSolutionClass(CloudBalance.class)
        .withEntityClasses(CloudProcess.class)
        .withConstraintProviderClass(CloudBalancingConstraintProvider.class)
        .withTerminationSpentLimit(Duration.ofSeconds(20)));
}
```

##  Score Példák

```
0hard/-300soft     ✅ Feasible megoldás, 300 soft cost
-2hard/-200soft    ❌ Infeasible megoldás (2 hard constraint sérül)
0hard/0soft        🏆 Tökéletes megoldás
```

##  Megjegyzések a Frontendhez

A UI leírások (descriptions) jelenleg hard-coded módon vannak a React komponensekben. Ez a későbbiekben dinamikusan kerül majd az oldalra:

##  További Információk

- [OptaPlanner Docs](https://docs.optaplanner.org/)
- [Spring Boot Integration Guide](https://docs.optaplanner.org/latestFinal/optaplanner-docs/html_single/index.html#springBootIntegration)

---

**Verzió**: 0.0.1-SNAPSHOT 
