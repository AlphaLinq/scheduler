# Scheduler - OptaPlanner Alapú Ütemezési Rendszer

## Projekt Áttekintés

Ez a projekt egy komplex ütemezési rendszer, amely az **OptaPlanner 9.43.0.Final** constraint solver keretrendszert használja többféle optimalizációs probléma megoldására. A projekt Spring Boot backendet és React frontendet kombinál, három különböző ütemezési problémát kezel:

1. **Timetable (Órarend ütemezés)** - Iskolai órarend generálás
2. **Cloud Balancing (Felhő erőforrás kiegyensúlyozás)** - Folyamatok számítógépekhez rendelése
3. **Vehicle Routing (Járműútvonal tervezés)** - Járművek és útvonalak optimalizálása

## Technológiai Stack

### Backend
- **Spring Boot 3.5.7** - Alkalmazás keretrendszer
- **OptaPlanner 9.43.0.Final** - AI constraint solver
- **Java 17.0.16 (OpenJDK)** - Programozási nyelv
- **Lombok** - Boilerplate kód csökkentés
- **Maven** - Build management

### Frontend
- **React 19.1.1** - UI keretrendszer
- **Vite 7.1.7** - Build tool és dev server
- **JavaScript (JSX)** - Programozási nyelv

## Projekt Struktúra

```
scheduler/
├── backend/
│   ├── src/main/java/com/szakdolgozat/scheduler/
│   │   ├── SchedulerApplication.java          # Spring Boot alkalmazás belépési pont
│   │   ├── config/
│   │   │   └── OptaPlannerConfig.java         # OptaPlanner konfigurációk
│   │   ├── cloudbalancing/
│   │   │   ├── domain/                         # Domain modellek
│   │   │   ├── solver/                         # Constraint provider
│   │   │   ├── service/                        # Üzleti logika
│   │   │   └── controller/                     # REST API
│   │   ├── timetable/
│   │   │   ├── domain/
│   │   │   ├── solver/
│   │   │   ├── service/
│   │   │   └── controller/
│   │   └── vehiclerouting/
│   │       ├── domain/
│   │       ├── solver/
│   │       └── controller/
│   └── src/main/resources/
│       └── application.properties              # Spring konfiguráció
└── frontend/
    └── src/
        ├── App.jsx                             # Fő React komponens
        └── main.jsx                            # React alkalmazás belépési pont
```

---

## OptaPlanner Használata - Részletes Dokumentáció

### Mi az OptaPlanner?

Az OptaPlanner egy nyílt forráskódú AI constraint solver, amely NP-hard optimalizációs problémákat old meg. Ezek olyan problémák, amelyek brute force megközelítéssel millió évig tartanának még szuperszámítógépen is, de az OptaPlanner fejlett algoritmusai (mint a Tabu Search, Simulated Annealing, Late Acceptance) segítségével ésszerű idő alatt közel-optimális megoldást talál.

### OptaPlanner Alapfogalmak

#### 1. **Planning Solution (@PlanningSolution)**
A teljes problémát reprezentálja, beleértve az input adatokat és az output megoldást.

#### 2. **Planning Entity (@PlanningEntity)**
Olyan objektumok, amelyek értékei megváltoznak a megoldás során (pl. egy óra, amit el kell helyezni).

#### 3. **Planning Variable (@PlanningVariable)**
Az entitáson belül azok a mezők, amelyeket az OptaPlanner módosít (pl. melyik teremben, melyik időpontban).

#### 4. **Problem Fact**
Olyan adatok, amelyek nem változnak a megoldás során (pl. rendelkezésre álló termek listája).

#### 5. **Score (@PlanningScore)**
Egy megoldás minőségét értékeli. Két része van:
- **Hard Score**: Kötelező megszorítások (nem sérülhetnek)
- **Soft Score**: Kívánatos megszorítások (optimalizálandók)

#### 6. **Constraint Provider (ConstraintProvider)**
Definiálja az üzleti szabályokat, amelyeket a megoldásnak követnie kell.

---

## Implementált Megoldások

### 1. Cloud Balancing (Felhő Erőforrás Kiegyensúlyozás)

#### Probléma Leírása
Egy cégnek van `N` számítógépe és `M` folyamata. A feladat: rendelj minden folyamatot egy számítógéphez úgy, hogy:
- **Hard megszorítások** (kötelezőek):
  - Minden számítógép CPU kapacitása ≥ hozzárendelt folyamatok CPU igénye
  - Minden számítógép memória kapacitása ≥ hozzárendelt folyamatok memória igénye
  - Minden számítógép hálózati sávszélessége ≥ hozzárendelt folyamatok sávszélesség igénye
- **Soft megszorítások** (optimalizálandó):
  - Minimalizáld a használt számítógépek költségét

#### Domain Modellek

**Computer.java** (Problem Fact - nem változik)
```java
@Getter
public class Computer {
    @PlanningId
    private int id;
    private int cpuPower;              // CPU kapacitás
    private int memory;                // Memória kapacitás
    private int networkBandwidth;      // Hálózati sávszélesség
    private int cost;                  // Karbantartási költség
}
```

**CloudProcess.java** (Planning Entity - változik)
```java
@Getter
@PlanningEntity
public class CloudProcess {
    @PlanningId
    private Long id;
    
    // Problem properties (nem változnak)
    private int requiredCpuPower;
    private int requiredMemory;
    private int requiredBandwidth;
    
    // Planning variable (ezt optimalizálja az OptaPlanner)
    @PlanningVariable(valueRangeProviderRefs = "computerList")
    @Setter
    private Computer computer;
}
```

**CloudBalance.java** (Planning Solution)
```java
@Getter
@PlanningSolution
public class CloudBalance {
    // Input: elérhető számítógépek
    @ValueRangeProvider(id = "computerList")
    @ProblemFactCollectionProperty
    private List<Computer> computerList;
    
    // Input/Output: folyamatok (computer mező módosul)
    @PlanningEntityCollectionProperty
    private List<CloudProcess> cloudProcessList;
    
    // Output: megoldás minősége
    @PlanningScore
    private HardSoftScore score;
}
```

#### Constraint Provider

**CloudBalancingConstraintProvider.java**
```java
public class CloudBalancingConstraintProvider implements ConstraintProvider {
    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
            requiredCpuPowerTotal(constraintFactory),      // Hard
            requiredMemoryTotal(constraintFactory),        // Hard
            requiredBandwidthTotal(constraintFactory),     // Hard
            computerCost(constraintFactory)                // Soft
        };
    }
    
    // Ha egy számítógép CPU igénye meghaladja a kapacitást -> hard penalty
    private Constraint requiredCpuPowerTotal(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(CloudProcess.class)
                .groupBy(CloudProcess::getComputer, sum(CloudProcess::getRequiredCpuPower))
                .filter((computer, requiredCpuPower) -> requiredCpuPower > computer.getCpuPower())
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("requiredCpuPowerTotal constraint");
    }
    
    // Minden használt számítógép növeli a költséget -> soft penalty
    private Constraint computerCost(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(Computer.class)
                .ifExists(CloudProcess.class, equal(Function.identity(), CloudProcess::getComputer))
                .penalize(HardSoftScore.ONE_SOFT, Computer::getCost)
                .asConstraint("computerCost constraint");
    }
}
```

#### Service és Controller

**CloudBalanceService.java**
```java
@Service
public class CloudBalanceService {
    private final SolverFactory<CloudBalance> solverFactory;
    
    public CloudBalance solve(CloudBalance problem) {
        Solver<CloudBalance> solver = solverFactory.buildSolver();
        return solver.solve(problem);  // OptaPlanner megoldja
    }
    
    public CloudBalance generateDemoData() {
        // 4 számítógép különböző kapacitásokkal
        List<Computer> computerList = new ArrayList<>();
        computerList.add(new Computer(0, 4, 8, 10, 100));
        computerList.add(new Computer(1, 8, 16, 20, 200));
        computerList.add(new Computer(2, 12, 24, 30, 300));
        computerList.add(new Computer(3, 16, 32, 40, 500));
        
        // 6 folyamat különböző igényekkel
        List<CloudProcess> processList = new ArrayList<>();
        processList.add(new CloudProcess(0L, 1, 2, 2, null));
        processList.add(new CloudProcess(1L, 2, 4, 3, null));
        // ... további folyamatok
        
        return new CloudBalance(processList, computerList);
    }
}
```

**CloudBalanceController.java**
```java
@RestController
@RequestMapping("api/cloudbalance")
public class CloudBalanceController {
    @Autowired
    private CloudBalanceService cloudBalanceService;
    
    @GetMapping("/demo")
    public CloudBalance solveDemoData() {
        CloudBalance problem = cloudBalanceService.generateDemoData();
        return cloudBalanceService.solve(problem);
    }
}
```

---

### 2. Timetable (Órarend Ütemezés)

#### Probléma Leírása
Iskolai órarend generálás, ahol órákat kell időpontokhoz és termekhez rendelni.

- **Hard megszorítások**:
  - Egy teremben egy időpontban max 1 óra lehet
  - Egy tanár egy időpontban max 1 órát tarthat
  - Egy diák csoport egy időpontban max 1 órán lehet
  
- **Soft megszorítások**:
  - Tanár előnyben részesíti ugyanabban a teremben tanítani
  - Kerülendők a tanár óraközi lyukak
  - Diákoknak ne legyen ugyanaz a tantárgy egymás után

#### Domain Modellek

**Lesson.java** (Planning Entity)
```java
@PlanningEntity
public class Lesson {
    @PlanningId
    private Long id;
    
    // Problem properties
    @Getter private String subject;         // Tantárgy
    @Getter private String teacher;         // Tanár
    @Getter private String studentGroup;    // Diák csoport
    
    // Planning variables
    @Setter @PlanningVariable
    private TimeSlot timeSlot;              // Melyik időpont?
    
    @Setter @PlanningVariable
    private Room room;                      // Melyik terem?
}
```

**TimeSlot.java** (Problem Fact)
```java
public class TimeSlot {
    @PlanningId
    private Long id;
    private DayOfWeek dayOfWeek;    // pl. HÉTFŐ
    private LocalTime startTime;     // pl. 09:00
    private LocalTime endTime;       // pl. 09:50
}
```

**Room.java** (Problem Fact)
```java
public class Room {
    @PlanningId
    private Long id;
    private String roomName;         // pl. "A101"
}
```

**TimeTable.java** (Planning Solution)
```java
@PlanningSolution
public class TimeTable {
    // Input: elérhető időpontok
    @ValueRangeProvider
    @ProblemFactCollectionProperty
    private List<TimeSlot> timeSlotList;
    
    // Input: elérhető termek
    @ValueRangeProvider
    @ProblemFactCollectionProperty
    private List<Room> roomList;
    
    // Input/Output: órák (timeSlot és room módosul)
    @PlanningEntityCollectionProperty
    private List<Lesson> lessonList;
    
    // Output: score
    @PlanningScore
    private HardSoftScore score;
}
```

#### Constraint Provider

**TimeTableConstraintProvider.java**
```java
public class TimeTableConstraintProvider implements ConstraintProvider {
    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
            roomConflict(constraintFactory),
            teacherConflict(constraintFactory),
            studentGroupConflict(constraintFactory)
        };
    }
    
    // Egy terem + egy időpont -> max 1 óra
    private Constraint roomConflict(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(Lesson.class)
                .join(Lesson.class,
                      Joiners.equal(Lesson::getTimeSlot),      // Ugyanaz az időpont
                      Joiners.equal(Lesson::getRoom),          // Ugyanaz a terem
                      Joiners.lessThan(Lesson::getId))         // Különböző órák
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Room conflict");
    }
    
    // Egy tanár + egy időpont -> max 1 óra
    private Constraint teacherConflict(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(Lesson.class)
                .join(Lesson.class,
                      Joiners.equal(Lesson::getTimeSlot),
                      Joiners.equal(Lesson::getTeacher),
                      Joiners.lessThan(Lesson::getId))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Teacher conflict");
    }
}
```

---

## OptaPlanner Konfiguráció

**OptaPlannerConfig.java**
```java
@Configuration
public class OptaPlannerConfig {
    
    // Órarend solver konfiguráció
    @Bean(name = "timeTableSolverFactory")
    public SolverFactory<TimeTable> timeTableSolverFactory() {
        SolverConfig config = new SolverConfig()
                .withSolutionClass(TimeTable.class)           // Mi a solution osztály?
                .withEntityClasses(Lesson.class)              // Mi a planning entity?
                .withConstraintProviderClass(                 // Mik a szabályok?
                    TimeTableConstraintProvider.class)
                .withTerminationSpentLimit(                   // Meddig fusson?
                    Duration.ofSeconds(30));
        
        return SolverFactory.<TimeTable>create(config);
    }
    
    // Cloud Balance solver konfiguráció
    @Bean(name = "cloudBalanceSolverFactory")
    public SolverFactory<CloudBalance> cloudBalanceSolverFactory() {
        SolverConfig config = new SolverConfig()
                .withSolutionClass(CloudBalance.class)
                .withEntityClasses(CloudProcess.class)
                .withConstraintProviderClass(
                    CloudBalancingConstraintProvider.class)
                .withTerminationSpentLimit(Duration.ofSeconds(20));
        
        return SolverFactory.<CloudBalance>create(config);
    }
}
```

**application.properties**
```properties
spring.application.name=scheduler
# Kikapcsoljuk az auto-konfigurációt, mert manuálisan konfiguráljuk
spring.autoconfigure.exclude=org.optaplanner.spring.boot.autoconfigure.OptaPlannerAutoConfiguration
```

---

## Hogyan Működik az OptaPlanner?

### 1. **Probléma Felépítése**

```java
// 1. Létrehozunk egy megoldatlan problémát
CloudBalance problem = new CloudBalance();
problem.setComputerList(computers);          // Problem facts
problem.setCloudProcessList(processes);      // Entities (computer = null)
```

### 2. **Solver Létrehozása és Futtatása**

```java
// 2. Solver factory-ból építünk egy solver-t
Solver<CloudBalance> solver = solverFactory.buildSolver();

// 3. Megoldjuk a problémát
CloudBalance solution = solver.solve(problem);
```

### 3. **Optimalizációs Folyamat**

Az OptaPlanner a következő lépéseket követi:

1. **Inicializáció**: Minden planning variable-nek véletlenszerűen ad értéket
   ```
   Process 0 -> Computer 2
   Process 1 -> Computer 0
   Process 2 -> Computer 3
   ...
   ```

2. **Score Kalkuláció**: Minden lépésnél kiszámolja a score-t
   ```
   Hard score: -3 (3 hard constraint sérül)
   Soft score: -500 (500 cost point)
   Total: -3hard/-500soft
   ```

3. **Move Generálás**: Próbál "lépéseket" tenni (pl. Process 0: Computer 2 -> Computer 1)

4. **Move Elfogadás/Elutasítás**: Metaheurisztikák alapján dönt
   - Tabu Search: Emlékezik a korábbi lépésekre
   - Simulated Annealing: Néha rosszabb megoldást is elfogad
   - Late Acceptance: Verziókat tárol és összehasonlít

5. **Iteráció**: Folyamatosan javít, amíg el nem telik az idő (termination limit)

6. **Best Solution Return**: Visszaadja a legjobb talált megoldást

### 4. **Score Számítás Részletei**

```java
// Minden constraint egy stream pipeline
forEach(CloudProcess.class)                           // Minden process-re
    .groupBy(CloudProcess::getComputer,               // Csoportosítás computer szerint
             sum(CloudProcess::getRequiredCpuPower))  // CPU igények összegzése
    .filter((computer, totalCpu) -> 
        totalCpu > computer.getCpuPower())            // Túllépés?
    .penalize(HardSoftScore.ONE_HARD)                 // -1 hard per túllépés
```

**Példa Score Kalkuláció:**
```
Computer 0: CPU: 4
  - Process 0: 1 CPU
  - Process 1: 2 CPU
  - Process 2: 3 CPU
  Total: 6 > 4 ❌ -> -1 hard

Computer 1: CPU: 8
  - Process 3: 4 CPU
  Total: 4 <= 8 ✅ -> 0 hard

Used computers: Computer 0 (cost: 100), Computer 1 (cost: 200)
  -> -300 soft

Final Score: -1hard/-300soft
```

---

## API Endpoints

### Cloud Balance
```
GET /api/cloudbalance/demo
- Generál és megold egy demo cloud balance problémát
- Response: CloudBalance objektum megoldva
```

### Timetable
```
GET /api/timetable/demo
- Generál és megold egy demo órarend problémát
- Response: TimeTable objektum megoldva
```

---

## Frontend Integráció

A React frontend HTTP kérésekkel kommunikál a backenddel:

```javascript
const generateCloudBalance = async () => {
    setLoadingCloudBalance(true);
    try {
        const response = await fetch("http://localhost:8080/api/cloudbalance/demo");
        const data = await response.json();
        setCloudBalanceTable(data);
    } catch (err) {
        setError(err.message);
    } finally {
        setLoadingCloudBalance(false);
    }
}
```

A megoldást táblázatos formában jeleníti meg:
- **Score információk**: Hard/Soft score megjelenítése
- **Computers tábla**: Mely számítógépekhez mely folyamatok vannak rendelve
- **Processes tábla**: Minden folyamat hova lett allokálva
- **Magyarázó szövegek**: A probléma és a megszorítások leírása

---

## Projekt Build és Futtatás

### Előfeltételek
- **Java 17 JDK** (Telepítve: OpenJDK 17.0.16)
  - Ha hiányzik: `sudo apt install openjdk-17-jdk`
  - Ellenőrzés: `java -version` és `javac -version`
  - Részletek: [JAVA_SETUP.md](./JAVA_SETUP.md)
- **Maven 3.6+**
- **Node.js 18+** (frontend)

### Backend (Spring Boot)
```bash
cd backend
mvn clean install
mvn spring-boot:run
```
A backend elindul a `http://localhost:8080` címen.

### Frontend (React + Vite)
```bash
cd frontend
npm install
npm run dev
```
A frontend elindul a `http://localhost:5173` címen.

### Production Build
```bash
# Frontend build
cd frontend
npm run build

# Backend build (belefordítja a frontend-et)
cd ../backend
mvn clean package

# Futtatás
java -jar target/scheduler-0.0.1-SNAPSHOT.jar
```

---

## Optimalizációs Stratégiák és Best Practices

### 1. **Termination Konfigurálás**
```java
// Időkorlát
.withTerminationSpentLimit(Duration.ofSeconds(30))

// Score alapú
.withTerminationConfig(new TerminationConfig()
    .withBestScoreLimit("0hard/-500soft"))

// Kombinált
.withTerminationConfig(new TerminationConfig()
    .withSecondsSpentLimit(30L)
    .withUnimprovedSecondsSpentLimit(5L))
```

### 2. **Constraint Súlyozás**
```java
// Erős hard constraint
.penalize(HardSoftScore.ofHard(10))

// Gyenge soft constraint
.penalize(HardSoftScore.ofSoft(1))
```

### 3. **Value Range Szűkítés**
Ha tudjuk, hogy bizonyos entitásoknak csak bizonyos értékeket lehet adni, használjunk `@ValueRangeProvider` szűrést.

### 4. **PlanningId Használata**
Mindig használj `@PlanningId` annotációt egyedi azonosításhoz - ez javítja a performanciát.

### 5. **Immutable Problem Facts**
A problem fact objektumok legyenek immutable-ök, ne változzanak futás közben.

---

## Gyakori Problémák és Megoldásaik

### Probléma: A solver nem talál feasible (hard score = 0) megoldást

**Okok:**
- Túl szigorú hard constraintek
- Nem elég erőforrás (pl. kevés számítógép, kevés időpont)
- Ellentmondó megszorítások

**Megoldás:**
- Növeld az erőforrásokat (több számítógép, több időpont)
- Lazítsd a hard constrainteket vagy tedd soft-tá őket
- Ellenőrizd, hogy nincs-e ellentmondás a szabályokban

### Probléma: A megoldás nem optimális

**Okok:**
- Túl rövid solver idő
- Rossz constraint súlyozás
- Túl nagy keresési tér

**Megoldás:**
- Növeld a `terminationSpentLimit` értékét
- Finomhangold a constraint súlyokat
- Használj konstrukciós heurisztikákat (custom phase)

---

## Tesztelés

**In Progress**

---


## További Források

- **OptaPlanner Dokumentáció**: https://docs.optaplanner.org/
- **OptaPlanner GitHub**: https://github.com/kiegroup/optaplanner
- **Spring Boot Integration**: https://docs.optaplanner.org/latestFinal/optaplanner-docs/html_single/index.html#springBootIntegration

---

**Verzió**: 0.0.1-SNAPSHOT 

