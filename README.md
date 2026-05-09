# CPU Scheduling Comparator — Round Robin vs Preemptive Priority

> An Operating Systems-1 project that simulates and compares two CPU scheduling algorithms side-by-side: **Round Robin** and **Preemptive Priority Scheduling**.

---

## What This Project Does

The simulator accepts dynamic process input, runs both algorithms on the same workload, and presents:

- Side-by-side **Gantt charts** for each algorithm
- Per-process metrics: **Waiting Time (WT)**, **Turnaround Time (TAT)**, **Response Time (RT)**
- **Average metrics** and a **winner comparison table**
- Auto-generated **analysis**, **conclusion**, and **variant checklist**
- A **console mode** for terminal-based execution

---

## Algorithms

### Round Robin (RR)

Each process gets a fixed CPU slice (the _time quantum_). When the slice expires without completion, the process moves to the back of the ready queue. This rotation continues until all processes finish.

Key properties:

- Every process receives CPU time at regular intervals regardless of burst length or priority
- Response time is bounded: no process waits longer than (n−1) × quantum before its first turn
- A small quantum increases context-switch overhead; a large quantum converges toward FCFS

### Preemptive Priority Scheduling

Always runs the highest-priority process among those that have arrived. If a higher-priority process arrives while a lower-priority one is running, the running process is immediately preempted.

Tie-breaking rules (configurable):

1. Lower priority number = higher priority _(default, configurable)_
2. Earlier arrival time wins
3. Smaller process ID wins

---

## Metrics

| Metric                | Formula                        |
| --------------------- | ------------------------------ |
| Turnaround Time (TAT) | Completion Time − Arrival Time |
| Waiting Time (WT)     | TAT − Burst Time               |
| Response Time (RT)    | First CPU Time − Arrival Time  |

---

## Requirements

- **Java 21** or higher
- **NetBeans 21+** (or any IDE that supports Java)
- No external libraries required

---

## How to Run

### Option 1 — GUI (NetBeans)

1. Open NetBeans → **File > Open Project**
2. Select the `SchedulerComparator` folder
3. Right-click the project → **Run** (or press **F6**)

### Option 2 — GUI (Terminal)

```bash
# Compile
javac -d build/classes \
  src/scheduler/*.java \
  src/scheduler/algorithm/*.java \
  src/scheduler/model/*.java \
  src/scheduler/metrics/*.java \
  src/scheduler/validation/*.java \
  src/scheduler/gui/*.java

# Run
java -cp build/classes scheduler.App
```

### Option 3 — Console Simulator

```bash
# After compiling (see above)
java -cp build/classes scheduler.ConsoleSchedulerSimulator
# or
java -cp build/classes scheduler.App console
```

### Option 4 — Run Tests

```bash
# Compile tests
javac -d build/test/classes -cp build/classes tests/TestRunner.java

# Run
java -cp build/classes:build/test/classes TestRunner
```

---

## Test Scenarios

| Scenario | Description                             | Purpose                                               |
| -------- | --------------------------------------- | ----------------------------------------------------- |
| **A**    | Basic mixed workload (5 processes, Q=3) | Normal case                                           |
| **B**    | Urgency case (Q=2)                      | High-priority preemption behavior                     |
| **C**    | Fairness case — all arrive at t=0 (Q=2) | Equal burst, different priorities                     |
| **D**    | Starvation risk (Q=2)                   | Low-priority long job vs. many high-priority arrivals |
| **E**    | Validation demo                         | Invalid input rejection                               |

---

## Project Structure

```
SchedulerComparator/
├── .gitignore                     # Ignores generated build output and private IDE files
├── JavaApplication6/              # Legacy NetBeans sample project; not part of the scheduler app
├── src/scheduler/
│   ├── App.java                        # Entry point (GUI or console mode)
│   ├── ConsoleSchedulerSimulator.java  # Terminal-based simulator
│   ├── algorithm/
│   │   ├── Scheduler.java              # Common interface
│   │   ├── RoundRobinScheduler.java    # RR simulation logic
│   │   └── PriorityScheduler.java      # Preemptive Priority logic
│   ├── model/
│   │   ├── Process.java                # Process data model
│   │   ├── GanttEntry.java             # One timeline block
│   │   └── ScheduleResult.java         # Full simulation output
│   ├── metrics/
│   │   └── MetricsCalculator.java      # WT / TAT / RT + averages
│   ├── validation/
│   │   └── InputValidator.java         # Input validation rules
│   └── gui/
│       ├── MainWindow.java             # Main JFrame
│       ├── SimulationPanel.java        # Input controls + side-by-side results
│       ├── GanttChartPanel.java        # Gantt chart drawing panel
│       ├── MetricsTablePanel.java      # Per-process metrics table
│       ├── ComparisonPanel.java        # Bar chart + written analysis
│       └── ProcessTableModel.java      # Editable process table model
├── tests/
    └── TestRunner.java                 # Plain Java PASS/FAIL test runner
```

---

## Algorithm Assumptions

- Priority scheduling is **preemptive** — a running process is interrupted immediately when a higher-priority process arrives
- Default priority rule: **lower number = higher priority** (configurable from the GUI)
- Ties broken by arrival time first, then by process ID
- Round Robin quantum default: **3** (configurable)
- Both algorithms receive a **deep copy** of the same input — neither modifies the original data
- Idle CPU time is recorded and rendered as a grey Gantt block

---

## Key Findings (Cross-Scenario Summary)

| Metric                   | Scenario A      | Scenario B      | Scenario C      | Scenario D      |
| ------------------------ | --------------- | --------------- | --------------- | --------------- |
| Avg Waiting Time winner  | Tie             | **Priority**    | **Priority**    | **Priority**    |
| Avg Turnaround winner    | Tie             | **Priority**    | **Priority**    | **Priority**    |
| Avg Response Time winner | **Round Robin** | **Round Robin** | **Round Robin** | **Round Robin** |
| Starvation risk observed | No              | Yes (P5)        | Yes (P1)        | Yes (P1)        |

**Round Robin** wins on average response time in every scenario — time-slicing gives every process a first turn quickly.  
**Preemptive Priority** wins on average waiting and turnaround time whenever priorities differ — urgent jobs finish sooner.

---

## When to Use Each Algorithm

| Use Case                              | Recommended Algorithm                               |
| ------------------------------------- | --------------------------------------------------- |
| Interactive / time-sharing systems    | **Round Robin**                                     |
| Real-time / mission-critical systems  | **Preemptive Priority**                             |
| Desktop environments                  | **Round Robin**                                     |
| OS kernel threads, interrupt handlers | **Preemptive Priority**                             |
| Mixed workloads (production OS)       | Hybrid — priority classes with RR within each class |

---

## Team Members

| No. | Student Name   | Student ID |
| --- | -------------- | ---------- |
| 1   | Tomas Nageh    | 20240231   |
| 2   | Geovany George | 20240266   |
| 3   | Barthina Reda  | 20240188   |
| 4   | Jolie Fayez    | 20240262   |
| 5   | Macarius Emad  | 20240991   |
| 6   | Beshoy Saleh   | 20240205   |
| 7   | Mina Tharwat   | 20241040   |

---

## Screenshots

> _(Add screenshots here after running the application)_

- <img width="1917" height="1015" alt="image" src="https://github.com/user-attachments/assets/6c2ec6e8-51c5-4741-850e-73a1a95cc7e5" />
  — Main simulation panel
- <img width="951" height="473" alt="image" src="https://github.com/user-attachments/assets/72f33830-43b6-4a66-9797-89bb6de2d070" />
  — Round Robin Gantt chart
- <img width="953" height="466" alt="image" src="https://github.com/user-attachments/assets/77eeeda8-e0a8-4843-bbf6-1ff2e4251847" />
  — Priority Scheduling Gantt chart
- <img width="953" height="466" alt="image" src="https://github.com/user-attachments/assets/df4c3507-c51f-47cf-a8be-acdc21fff403" />
  — Comparison summary and bar chart
