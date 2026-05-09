import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import scheduler.algorithm.PriorityScheduler;
import scheduler.algorithm.RoundRobinScheduler;
import scheduler.model.GanttEntry;
import scheduler.model.Process;
import scheduler.model.ScheduleResult;
import scheduler.validation.InputValidator;

/**
 * Plain Java test runner that prints PASS or FAIL messages without JUnit.
 */
public class TestRunner {

    /**
     * Runs all tests.
     *
     * @param args command line arguments (unused)
     */
    public static void main(String[] args) {
        runTestCase1();
        runTestCase2();
        runTestCase3();
    }

    /**
     * Test Case 1: Normal workload sanity checks.
     */
    private static void runTestCase1() {
        List<Process> processes = createScenarioAProcesses();

        RoundRobinScheduler roundRobinScheduler = new RoundRobinScheduler();
        PriorityScheduler priorityScheduler = new PriorityScheduler();

        ScheduleResult rrResult = roundRobinScheduler.simulate(processes, 3, true);
        ScheduleResult priorityResult = priorityScheduler.simulate(processes, 3, true);

        printAssertion("Test 1.1 - All processes completed (RR)",
                rrResult.getWaitingTime().size() == processes.size());
        printAssertion("Test 1.2 - All processes completed (Priority)",
                priorityResult.getWaitingTime().size() == processes.size());
        printAssertion("Test 1.3 - All waiting times are non-negative (RR)",
                allValuesNonNegative(rrResult.getWaitingTime()));
        printAssertion("Test 1.4 - All waiting times are non-negative (Priority)",
                allValuesNonNegative(priorityResult.getWaitingTime()));
        printAssertion("Test 1.5 - All turnaround times are >= burst times (RR)",
                allTurnaroundTimesAtLeastBurst(processes, rrResult));
        printAssertion("Test 1.6 - All turnaround times are >= burst times (Priority)",
                allTurnaroundTimesAtLeastBurst(processes, priorityResult));
    }

    /**
     * Test Case 2: Urgency versus fairness behavior checks.
     */
    private static void runTestCase2() {
        List<Process> processes = createScenarioBProcesses();

        RoundRobinScheduler roundRobinScheduler = new RoundRobinScheduler();
        PriorityScheduler priorityScheduler = new PriorityScheduler();

        ScheduleResult rrResult = roundRobinScheduler.simulate(processes, 2, true);
        ScheduleResult priorityResult = priorityScheduler.simulate(processes, 2, true);

        int p1PriorityCompletion = findCompletionTime(priorityResult.getGanttChart(), 1);
        int p2PriorityCompletion = findCompletionTime(priorityResult.getGanttChart(), 2);
        printAssertion("Test 2.1 - Priority preemption lets P2 finish before P1",
                p2PriorityCompletion < p1PriorityCompletion);

        int firstProcessInRoundRobin = findFirstRunningProcess(rrResult.getGanttChart());
        boolean rrNotPurePriority = firstProcessInRoundRobin == 1;
        printAssertion("Test 2.2 - RR behavior is not purely priority-based",
                rrNotPurePriority);
    }

    /**
     * Test Case 3: Input validation checks.
     */
    private static void runTestCase3() {
        InputValidator inputValidator = new InputValidator();

        List<Process> scenarioEValidationProcesses = createScenarioEValidationProcesses();
        boolean duplicateCaught = !inputValidator.checkForDuplicateIds(scenarioEValidationProcesses).isEmpty();
        boolean invalidArrivalCaught = !inputValidator.checkArrivalTimesAreValid(scenarioEValidationProcesses).isEmpty();
        boolean invalidBurstCaught = !inputValidator.checkBurstTimesAreValid(scenarioEValidationProcesses).isEmpty();
        boolean invalidPriorityCaught = !inputValidator.checkPrioritiesAreValid(scenarioEValidationProcesses).isEmpty();
        boolean invalidQuantumCaught = !inputValidator.checkQuantumIsValid(0).isEmpty();

        printAssertion("Test 3.1 - Scenario E duplicate IDs are caught", duplicateCaught);
        printAssertion("Test 3.2 - Scenario E invalid arrival is caught", invalidArrivalCaught);
        printAssertion("Test 3.3 - Scenario E invalid burst is caught", invalidBurstCaught);
        printAssertion("Test 3.4 - Scenario E invalid priority is caught", invalidPriorityCaught);
        printAssertion("Test 3.5 - Scenario E invalid quantum is caught", invalidQuantumCaught);
    }

    /**
     * Prints one assertion result in required format.
     *
     * @param testName assertion label
     * @param passed assertion outcome
     */
    private static void printAssertion(String testName, boolean passed) {
        String status = passed ? "PASS" : "FAIL";
        System.out.println("[" + status + "] " + testName);
    }

    /**
     * Checks all values in map are zero or positive.
     *
     * @param metricMap metric map
     * @return true when all values are non-negative
     */
    private static boolean allValuesNonNegative(Map<Integer, Integer> metricMap) {
        for (Integer value : metricMap.values()) {
            if (value < 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks TAT >= burst for every process.
     *
     * @param processes process list
     * @param result schedule result
     * @return true when all entries satisfy rule
     */
    private static boolean allTurnaroundTimesAtLeastBurst(List<Process> processes, ScheduleResult result) {
        for (Process process : processes) {
            int processId = process.getProcessId();
            int turnaroundTime = result.getTurnaroundTime().get(processId);
            if (turnaroundTime < process.getBurstTime()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Finds completion time of one process from Gantt chart.
     *
     * @param ganttEntries timeline entries
     * @param processId target process ID
     * @return completion time or -1 if missing
     */
    private static int findCompletionTime(List<GanttEntry> ganttEntries, int processId) {
        int completionTime = -1;
        for (GanttEntry entry : ganttEntries) {
            if (entry.getProcessId() == processId) {
                completionTime = entry.getEndTime();
            }
        }
        return completionTime;
    }

    /**
     * Finds first non-idle process that gets CPU time.
     *
     * @param ganttEntries timeline entries
     * @return first running process ID, or -1 if missing
     */
    private static int findFirstRunningProcess(List<GanttEntry> ganttEntries) {
        for (GanttEntry entry : ganttEntries) {
            if (entry.getProcessId() == -1) {
                continue;
            }
            return entry.getProcessId();
        }
        return -1;
    }

    /**
     * Scenario A values from GUI preset.
     *
     * @return process list for Scenario A
     */
    private static List<Process> createScenarioAProcesses() {
        List<Process> processes = new ArrayList<>();
        processes.add(new Process(1, 0, 7, 3));
        processes.add(new Process(2, 2, 4, 1));
        processes.add(new Process(3, 4, 1, 4));
        processes.add(new Process(4, 5, 4, 2));
        processes.add(new Process(5, 6, 3, 5));
        return processes;
    }

    /**
     * Scenario B values from GUI preset.
     *
     * @return process list for Scenario B
     */
    private static List<Process> createScenarioBProcesses() {
        List<Process> processes = new ArrayList<>();
        processes.add(new Process(1, 0, 9, 4));
        processes.add(new Process(2, 1, 3, 1));
        processes.add(new Process(3, 2, 5, 3));
        processes.add(new Process(4, 3, 2, 2));
        processes.add(new Process(5, 4, 4, 5));
        return processes;
    }

    /**
     * Scenario E invalid values used by validation demo.
     *
     * @return intentionally invalid process list
     */
    private static List<Process> createScenarioEValidationProcesses() {
        List<Process> processes = new ArrayList<>();
        processes.add(new Process(1, 0, 4, 1));
        processes.add(new Process(1, -1, 0, -1));
        return processes;
    }
}
