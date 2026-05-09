package scheduler;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * Terminal-based CPU Scheduling simulator.
 * Compares Round Robin and Preemptive Priority scheduling on same workload.
 */
public class ConsoleSchedulerSimulator {

    private static final int MAX_PROCESSES = 20;
    private static final int MIN_PROCESSES = 2;
    private static final int IDLE_PID = -1;

    public static void main(String[] args) {
        new ConsoleSchedulerSimulator().run();
    }

    private void run() {
        Scanner scanner = new Scanner(System.in);
        printWelcome();

        int menuChoice = readMenuChoice(scanner);
        if (menuChoice == 6) {
            runValidationDemo();
            return;
        }

        InputBundle inputBundle = (menuChoice == 1)
                ? readCustomInput(scanner)
                : buildScenario(menuChoice);

        if (inputBundle.scenarioName != null) {
            System.out.println();
            System.out.println("Loaded " + inputBundle.scenarioName + ".");
        }

        runComparison(inputBundle.processes, inputBundle.quantum);
    }

    private void printWelcome() {
        System.out.println("CPU Scheduling Simulator - Round Robin vs Preemptive Priority");
        System.out.println("============================================================");
        System.out.println("Priority tie-breaking: lower priority value first, then earlier arrival,");
        System.out.println("then process ID alphabetically.");
        System.out.println();
    }

    private int readMenuChoice(Scanner scanner) {
        System.out.println("Choose input mode:");
        System.out.println("[1] Enter custom processes");
        System.out.println("[2] Scenario A: Basic mixed workload");
        System.out.println("[3] Scenario B: Urgency case");
        System.out.println("[4] Scenario C: Fairness case");
        System.out.println("[5] Scenario D: Starvation case");
        System.out.println("[6] Scenario E: Validation demo");

        while (true) {
            System.out.print("Enter choice (1-6): ");
            String raw = scanner.nextLine().trim();
            try {
                int choice = Integer.parseInt(raw);
                if (choice >= 1 && choice <= 6) {
                    return choice;
                }
            } catch (NumberFormatException ignored) {
                // handled below
            }
            System.out.println("Invalid menu choice. Please enter a number from 1 to 6.");
        }
    }

    private InputBundle readCustomInput(Scanner scanner) {
        int processCount;
        while (true) {
            processCount = readPositiveInt(scanner, "Enter number of processes (2-20): ");
            if (processCount < MIN_PROCESSES || processCount > MAX_PROCESSES) {
                System.out.println("Process count must be between 2 and 20.");
                continue;
            }
            break;
        }

        List<ProcessData> processes = new ArrayList<>();
        Set<String> usedIds = new HashSet<>();

        for (int i = 0; i < processCount; i++) {
            System.out.println();
            System.out.println("Process " + (i + 1) + ":");

            String pid = readProcessId(scanner, usedIds);
            int arrival = readNonNegativeInt(scanner, "Arrival Time (>= 0): ");
            int burst = readPositiveInt(scanner, "Burst Time (> 0): ");
            int priority = readPositiveInt(scanner, "Priority (> 0, lower is higher): ");

            processes.add(new ProcessData(pid, arrival, burst, priority));
            usedIds.add(pid);
        }

        int quantum = readPositiveInt(scanner, "\nEnter Time Quantum for RR (> 0): ");
        return new InputBundle(processes, quantum);
    }

    private String readProcessId(Scanner scanner, Set<String> usedIds) {
        while (true) {
            System.out.print("Process ID (e.g., P1): ");
            String pid = scanner.nextLine().trim();

            if (pid.isEmpty()) {
                System.out.println("Process ID cannot be empty.");
                continue;
            }
            if (usedIds.contains(pid)) {
                System.out.println("Duplicate process ID. Each process ID must be unique.");
                continue;
            }
            return pid;
        }
    }

    private int readNonNegativeInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String raw = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(raw);
                if (value < 0) {
                    System.out.println("Value must be non-negative.");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a numeric integer value.");
            }
        }
    }

    private int readPositiveInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String raw = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(raw);
                if (value <= 0) {
                    System.out.println("Value must be positive.");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a numeric integer value.");
            }
        }
    }

    private InputBundle buildScenario(int menuChoice) {
        List<ProcessData> processes = new ArrayList<>();
        int quantum;

        switch (menuChoice) {
            case 2:
                processes.add(new ProcessData("P1", 0, 7, 3));
                processes.add(new ProcessData("P2", 2, 4, 1));
                processes.add(new ProcessData("P3", 4, 1, 4));
                processes.add(new ProcessData("P4", 5, 4, 2));
                processes.add(new ProcessData("P5", 6, 3, 5));
                quantum = 3;
                break;
            case 3:
                processes.add(new ProcessData("P1", 0, 9, 4));
                processes.add(new ProcessData("P2", 1, 3, 1));
                processes.add(new ProcessData("P3", 2, 5, 3));
                processes.add(new ProcessData("P4", 3, 2, 2));
                processes.add(new ProcessData("P5", 4, 4, 5));
                quantum = 2;
                break;
            case 4:
                processes.add(new ProcessData("P1", 0, 4, 5));
                processes.add(new ProcessData("P2", 0, 4, 1));
                processes.add(new ProcessData("P3", 0, 4, 3));
                processes.add(new ProcessData("P4", 0, 4, 2));
                processes.add(new ProcessData("P5", 0, 4, 4));
                quantum = 2;
                break;
            case 5:
                processes.add(new ProcessData("P1", 0, 25, 10));
                processes.add(new ProcessData("P2", 1, 2, 1));
                processes.add(new ProcessData("P3", 2, 2, 1));
                processes.add(new ProcessData("P4", 3, 2, 1));
                processes.add(new ProcessData("P5", 4, 2, 1));
                processes.add(new ProcessData("P6", 5, 2, 1));
                processes.add(new ProcessData("P7", 6, 2, 1));
                processes.add(new ProcessData("P8", 7, 2, 1));
                quantum = 2;
                break;
            default:
                throw new IllegalArgumentException("Unexpected scenario: " + menuChoice);
        }
        return new InputBundle(processes, quantum, getScenarioName(menuChoice));
    }

    private String getScenarioName(int menuChoice) {
        switch (menuChoice) {
            case 2:
                return "Scenario A (Basic mixed workload)";
            case 3:
                return "Scenario B (Urgency case)";
            case 4:
                return "Scenario C (Fairness case)";
            case 5:
                return "Scenario D (Starvation case)";
            default:
                return null;
        }
    }

    private void runValidationDemo() {
        System.out.println();
        System.out.println("=== Scenario E: Validation demo ===");
        System.out.println("This scenario demonstrates invalid input checks.");

        List<ProcessData> invalid = new ArrayList<>();
        invalid.add(new ProcessData("", 0, 3, 2));
        invalid.add(new ProcessData("P1", -1, 3, 2));
        invalid.add(new ProcessData("P1", 1, 0, -5));

        List<String> errors = validateInput(invalid, 0);
        for (String error : errors) {
            System.out.println("- " + error);
        }
        System.out.println("Validation demo finished.");
    }

    private List<String> validateInput(List<ProcessData> processes, int quantum) {
        List<String> errors = new ArrayList<>();

        if (processes.size() < MIN_PROCESSES) {
            errors.add("Minimum 2 processes required.");
        }
        if (processes.size() > MAX_PROCESSES) {
            errors.add("Maximum 20 processes allowed.");
        }
        if (quantum <= 0) {
            errors.add("Time quantum must be positive.");
        }

        Set<String> seenIds = new HashSet<>();
        for (ProcessData process : processes) {
            if (process.pid == null || process.pid.trim().isEmpty()) {
                errors.add("Empty process ID is not allowed.");
            } else if (seenIds.contains(process.pid)) {
                errors.add("Duplicate process ID found: " + process.pid);
            } else {
                seenIds.add(process.pid);
            }

            if (process.arrival < 0) {
                errors.add("Process " + process.pid + ": arrival time must be non-negative.");
            }
            if (process.burst <= 0) {
                errors.add("Process " + process.pid + ": burst time must be positive.");
            }
            if (process.priority <= 0) {
                errors.add("Process " + process.pid + ": priority value must be positive.");
            }
        }
        return errors;
    }

    private void runComparison(List<ProcessData> processes, int quantum) {
        List<String> validationErrors = validateInput(processes, quantum);
        if (!validationErrors.isEmpty()) {
            System.out.println("Input is invalid:");
            for (String err : validationErrors) {
                System.out.println("- " + err);
            }
            return;
        }

        printInputTable(processes, quantum);

        ScheduleResult rr = simulateRoundRobin(processes, quantum);
        ScheduleResult pr = simulatePriorityPreemptive(processes);

        System.out.println();
        System.out.println("=== Round Robin (Quantum = " + quantum + ") ===");
        printGanttChart(rr.gantt);
        printRRTable(rr);

        System.out.println();
        System.out.println("=== Priority Scheduling (Preemptive) ===");
        System.out.println("Tie-break rule: lower priority value first, then earlier arrival, then PID alphabetically.");
        printGanttChart(pr.gantt);
        printPriorityTable(pr);

        System.out.println();
        System.out.println("=== Comparison Summary ===");
        printComparison(rr, pr);

        System.out.println();
        System.out.println("=== Analysis ===");
        printAnalysis(rr, pr);

        System.out.println();
        System.out.println("=== Conclusion ===");
        printConclusion(rr, pr);
    }

    private void printInputTable(List<ProcessData> processes, int quantum) {
        System.out.println();
        System.out.println("=== Input Processes ===");
        System.out.printf("%-6s %-6s %-6s %-8s%n", "PID", "AT", "BT", "Priority");
        for (ProcessData process : sortByArrivalThenPid(processes)) {
            System.out.printf("%-6s %-6d %-6d %-8d%n",
                    process.pid, process.arrival, process.burst, process.priority);
        }
        System.out.println("Time Quantum = " + quantum);
    }

    private ScheduleResult simulateRoundRobin(List<ProcessData> input, int quantum) {
        List<ProcessRuntime> jobs = copyForRuntime(input);
        jobs.sort(Comparator.comparingInt((ProcessRuntime p) -> p.arrival).thenComparing(p -> p.pid));

        Deque<ProcessRuntime> queue = new ArrayDeque<>();
        List<GanttSlice> gantt = new ArrayList<>();
        Map<String, Integer> completion = new HashMap<>();
        Map<String, Integer> firstCpu = new HashMap<>();

        int time = 0;
        int nextArrival = 0;
        int done = 0;
        while (done < jobs.size()) {
            while (nextArrival < jobs.size() && jobs.get(nextArrival).arrival <= time) {
                queue.addLast(jobs.get(nextArrival));
                nextArrival++;
            }

            if (queue.isEmpty()) {
                gantt.add(new GanttSlice(IDLE_PID, "IDLE", time, time + 1));
                time++;
                continue;
            }

            ProcessRuntime current = queue.removeFirst();
            firstCpu.putIfAbsent(current.pid, time);
            int start = time;
            int used = 0;

            while (used < quantum && current.remaining > 0) {
                current.remaining--;
                time++;
                used++;
                while (nextArrival < jobs.size() && jobs.get(nextArrival).arrival <= time) {
                    queue.addLast(jobs.get(nextArrival));
                    nextArrival++;
                }
            }
            gantt.add(new GanttSlice(current.numericId, current.pid, start, time));

            if (current.remaining > 0) {
                queue.addLast(current);
            } else {
                completion.put(current.pid, time);
                done++;
            }
        }

        return buildResult(input, gantt, completion, firstCpu);
    }

    private ScheduleResult simulatePriorityPreemptive(List<ProcessData> input) {
        List<ProcessRuntime> jobs = copyForRuntime(input);
        jobs.sort(Comparator.comparingInt((ProcessRuntime p) -> p.arrival).thenComparing(p -> p.pid));

        List<ProcessRuntime> ready = new ArrayList<>();
        List<GanttSlice> gantt = new ArrayList<>();
        Map<String, Integer> completion = new HashMap<>();
        Map<String, Integer> firstCpu = new HashMap<>();

        int time = 0;
        int nextArrival = 0;
        int done = 0;
        while (done < jobs.size()) {
            while (nextArrival < jobs.size() && jobs.get(nextArrival).arrival == time) {
                ready.add(jobs.get(nextArrival));
                nextArrival++;
            }

            if (ready.isEmpty()) {
                gantt.add(new GanttSlice(IDLE_PID, "IDLE", time, time + 1));
                time++;
                continue;
            }

            ProcessRuntime selected = pickPriorityProcess(ready);
            firstCpu.putIfAbsent(selected.pid, time);
            selected.remaining--;
            gantt.add(new GanttSlice(selected.numericId, selected.pid, time, time + 1));
            time++;

            if (selected.remaining == 0) {
                completion.put(selected.pid, time);
                ready.remove(selected);
                done++;
            }
        }

        return buildResult(input, gantt, completion, firstCpu);
    }

    private ProcessRuntime pickPriorityProcess(List<ProcessRuntime> ready) {
        ProcessRuntime best = ready.get(0);
        for (int i = 1; i < ready.size(); i++) {
            ProcessRuntime candidate = ready.get(i);
            if (candidate.priority < best.priority) {
                best = candidate;
            } else if (candidate.priority == best.priority) {
                if (candidate.arrival < best.arrival) {
                    best = candidate;
                } else if (candidate.arrival == best.arrival
                        && candidate.pid.compareTo(best.pid) < 0) {
                    best = candidate;
                }
            }
        }
        return best;
    }

    private ScheduleResult buildResult(
            List<ProcessData> original,
            List<GanttSlice> ganttRaw,
            Map<String, Integer> completion,
            Map<String, Integer> firstCpu
    ) {
        ScheduleResult result = new ScheduleResult();
        result.gantt = mergeGantt(ganttRaw);
        result.perProcess = new ArrayList<>();

        double sumWt = 0;
        double sumTat = 0;
        double sumRt = 0;

        for (ProcessData process : sortByArrivalThenPid(original)) {
            int tat = completion.get(process.pid) - process.arrival;
            int wt = tat - process.burst;
            int rt = firstCpu.get(process.pid) - process.arrival;

            ProcessMetrics metrics = new ProcessMetrics(process.pid, process.arrival, process.burst,
                    process.priority, wt, tat, rt);
            result.perProcess.add(metrics);

            sumWt += wt;
            sumTat += tat;
            sumRt += rt;
        }

        int n = original.size();
        result.avgWt = sumWt / n;
        result.avgTat = sumTat / n;
        result.avgRt = sumRt / n;
        return result;
    }

    private List<GanttSlice> mergeGantt(List<GanttSlice> slices) {
        if (slices.isEmpty()) {
            return slices;
        }

        List<GanttSlice> merged = new ArrayList<>();
        GanttSlice current = slices.get(0);
        for (int i = 1; i < slices.size(); i++) {
            GanttSlice next = slices.get(i);
            if (current.label.equals(next.label) && current.end == next.start) {
                current = new GanttSlice(current.pid, current.label, current.start, next.end);
            } else {
                merged.add(current);
                current = next;
            }
        }
        merged.add(current);
        return merged;
    }

    private void printGanttChart(List<GanttSlice> slices) {
        System.out.println("Gantt Chart:");
        StringBuilder bars = new StringBuilder();
        StringBuilder times = new StringBuilder();

        for (int i = 0; i < slices.size(); i++) {
            GanttSlice slice = slices.get(i);
            bars.append("| ").append(slice.label).append(" ");
            if (i == 0) {
                times.append(slice.start);
            }
            times.append(padLeft(String.valueOf(slice.end), 5));
        }
        bars.append("|");

        System.out.println(bars);
        System.out.println(times);
    }

    private String padLeft(String text, int width) {
        if (text.length() >= width) {
            return " " + text;
        }
        StringBuilder builder = new StringBuilder();
        for (int i = text.length(); i < width; i++) {
            builder.append(' ');
        }
        builder.append(text);
        return builder.toString();
    }

    private void printRRTable(ScheduleResult result) {
        System.out.printf("%-6s %-6s %-6s %-6s %-6s %-6s%n", "PID", "AT", "BT", "WT", "TAT", "RT");
        for (ProcessMetrics metric : result.perProcess) {
            System.out.printf("%-6s %-6d %-6d %-6d %-6d %-6d%n",
                    metric.pid, metric.arrival, metric.burst, metric.waiting, metric.turnaround, metric.response);
        }
        System.out.printf(Locale.US, "AVG                         %-6.2f %-6.2f %-6.2f%n",
                result.avgWt, result.avgTat, result.avgRt);
    }

    private void printPriorityTable(ScheduleResult result) {
        System.out.printf("%-6s %-6s %-6s %-9s %-6s %-6s %-6s%n",
                "PID", "AT", "BT", "Priority", "WT", "TAT", "RT");
        for (ProcessMetrics metric : result.perProcess) {
            System.out.printf("%-6s %-6d %-6d %-9d %-6d %-6d %-6d%n",
                    metric.pid, metric.arrival, metric.burst, metric.priority,
                    metric.waiting, metric.turnaround, metric.response);
        }
        System.out.printf(Locale.US, "AVG                                  %-6.2f %-6.2f %-6.2f%n",
                result.avgWt, result.avgTat, result.avgRt);
    }

    private void printComparison(ScheduleResult rr, ScheduleResult pr) {
        System.out.printf("%-12s %-12s %-12s %-12s%n", "Metric", "RR", "Priority", "Winner");
        printComparisonRow("Avg WT", rr.avgWt, pr.avgWt);
        printComparisonRow("Avg TAT", rr.avgTat, pr.avgTat);
        printComparisonRow("Avg RT", rr.avgRt, pr.avgRt);
    }

    private void printComparisonRow(String metric, double rrValue, double prValue) {
        String winner;
        if (Math.abs(rrValue - prValue) < 1e-9) {
            winner = "Tie";
        } else {
            winner = (rrValue < prValue) ? "RR" : "Priority";
        }
        System.out.printf(Locale.US, "%-12s %-12.2f %-12.2f %-12s%n", metric, rrValue, prValue, winner);
    }

    private void printAnalysis(ScheduleResult rr, ScheduleResult pr) {
        System.out.println("- Lower average WT: " + decideLower(rr.avgWt, pr.avgWt));
        System.out.println("- Lower average RT: " + decideLower(rr.avgRt, pr.avgRt));

        boolean rrFairer = (rr.avgRt <= pr.avgRt) || (rr.avgWt <= pr.avgWt);
        System.out.println("- Did RR appear fairer? " + (rrFairer ? "Yes, generally." : "Not clearly in this workload."));

        boolean starvationRiskPriority = isStarvationLikely(pr);
        System.out.println("- Starvation observed/likely in Priority? "
                + (starvationRiskPriority ? "Likely, at least one process waited much longer." : "Not obvious."));

        String recommended = (rr.avgWt + rr.avgRt <= pr.avgWt + pr.avgRt)
                ? "Round Robin"
                : "Preemptive Priority";
        System.out.println("- Recommended algorithm: " + recommended
                + " for this workload based on combined WT and RT behavior.");
    }

    private void printConclusion(ScheduleResult rr, ScheduleResult pr) {
        System.out.println("WT winner: " + decideLower(rr.avgWt, pr.avgWt));
        System.out.println("TAT winner: " + decideLower(rr.avgTat, pr.avgTat));
        System.out.println("RT winner: " + decideLower(rr.avgRt, pr.avgRt));
        System.out.println("Fairness observation: RR tends to distribute CPU more evenly.");
        System.out.println("Starvation risk: higher in Preemptive Priority, lower in RR.");
    }

    private String decideLower(double rr, double pr) {
        if (Math.abs(rr - pr) < 1e-9) {
            return "Tie";
        }
        return (rr < pr) ? "Round Robin" : "Priority";
    }

    private boolean isStarvationLikely(ScheduleResult priorityResult) {
        if (priorityResult.perProcess.isEmpty()) {
            return false;
        }
        int maxWaiting = Integer.MIN_VALUE;
        double avgWaiting = priorityResult.avgWt;
        for (ProcessMetrics metric : priorityResult.perProcess) {
            maxWaiting = Math.max(maxWaiting, metric.waiting);
        }
        return maxWaiting > avgWaiting * 2.0;
    }

    private List<ProcessRuntime> copyForRuntime(List<ProcessData> source) {
        List<ProcessRuntime> copied = new ArrayList<>();
        for (int i = 0; i < source.size(); i++) {
            ProcessData process = source.get(i);
            copied.add(new ProcessRuntime(i + 1, process.pid, process.arrival, process.burst, process.priority));
        }
        return copied;
    }

    private List<ProcessData> sortByArrivalThenPid(List<ProcessData> source) {
        List<ProcessData> sorted = new ArrayList<>(source);
        Collections.sort(sorted, Comparator.comparingInt((ProcessData p) -> p.arrival).thenComparing(p -> p.pid));
        return sorted;
    }

    private static final class InputBundle {
        private final List<ProcessData> processes;
        private final int quantum;
        private final String scenarioName;

        private InputBundle(List<ProcessData> processes, int quantum) {
            this(processes, quantum, null);
        }

        private InputBundle(List<ProcessData> processes, int quantum, String scenarioName) {
            this.processes = processes;
            this.quantum = quantum;
            this.scenarioName = scenarioName;
        }
    }

    private static final class ProcessData {
        private final String pid;
        private final int arrival;
        private final int burst;
        private final int priority;

        private ProcessData(String pid, int arrival, int burst, int priority) {
            this.pid = pid;
            this.arrival = arrival;
            this.burst = burst;
            this.priority = priority;
        }
    }

    private static final class ProcessRuntime {
        private final int numericId;
        private final String pid;
        private final int arrival;
        private final int priority;
        private int remaining;

        private ProcessRuntime(int numericId, String pid, int arrival, int burst, int priority) {
            this.numericId = numericId;
            this.pid = pid;
            this.arrival = arrival;
            this.priority = priority;
            this.remaining = burst;
        }
    }

    private static final class GanttSlice {
        private final int pid;
        private final String label;
        private final int start;
        private final int end;

        private GanttSlice(int pid, String label, int start, int end) {
            this.pid = pid;
            this.label = label;
            this.start = start;
            this.end = end;
        }
    }

    private static final class ProcessMetrics {
        private final String pid;
        private final int arrival;
        private final int burst;
        private final int priority;
        private final int waiting;
        private final int turnaround;
        private final int response;

        private ProcessMetrics(
                String pid,
                int arrival,
                int burst,
                int priority,
                int waiting,
                int turnaround,
                int response
        ) {
            this.pid = pid;
            this.arrival = arrival;
            this.burst = burst;
            this.priority = priority;
            this.waiting = waiting;
            this.turnaround = turnaround;
            this.response = response;
        }
    }

    private static final class ScheduleResult {
        private List<GanttSlice> gantt;
        private List<ProcessMetrics> perProcess;
        private double avgWt;
        private double avgTat;
        private double avgRt;
    }
}
