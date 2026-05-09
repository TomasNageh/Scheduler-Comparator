package scheduler.algorithm;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import scheduler.metrics.MetricsCalculator;
import scheduler.model.GanttEntry;
import scheduler.model.Process;
import scheduler.model.ScheduleResult;

/**
 * Preemptive Priority scheduling always selects the process with the highest
 * priority among processes that have already arrived.
 * "Preemptive" means a running process can be interrupted immediately when a
 * better-priority process arrives.
 * The priority rule can be configured as lower-number-higher-priority or the
 * opposite, based on user selection.
 * Ties are resolved deterministically: first by earlier arrival, then by
 * smaller process ID.
 * This algorithm can reduce response time for urgent tasks because urgent jobs
 * jump ahead quickly.
 * However, low-priority jobs might wait for a very long time if urgent jobs
 * keep arriving, which is called starvation.
 * For teaching purposes, this implementation runs exactly one tick per loop so
 * preemption behavior is explicit and easy to trace in the Gantt chart.
 */
public class PriorityScheduler implements Scheduler {

    private static final int CPU_IDLE_PROCESS_ID = -1;
    private static final int ONE_TIME_UNIT = 1;

    /**
     * Simulates preemptive priority scheduling tick-by-tick.
     *
     * @param processes input process list
     * @param timeQuantum unused for Priority scheduling
     * @param lowerNumberMeansHigherPriority priority interpretation rule
     * @return result with timeline and metrics
     */
    @Override
    public ScheduleResult simulate(
            List<Process> processes,
            int timeQuantum,
            boolean lowerNumberMeansHigherPriority
    ) {
        ScheduleResult scheduleResult = new ScheduleResult();

        // Step 1: Make a copy of every process so we do not change the originals.
        List<Process> copiedProcesses = copyProcesses(processes);

        // Step 2: Sort by arrival time.
        copiedProcesses.sort(Comparator.comparingInt(Process::getArrivalTime));

        // Step 3: Keep a list of processes that are currently available (ready pool).
        List<Process> readyPool = new ArrayList<>();
        int nextArrivalIndex = 0;
        int completedProcessCount = 0;
        int currentTime = 0;
        Map<Integer, Integer> completionTimeMap = new HashMap<>();
        Map<Integer, Integer> firstExecutionTimeMap = new HashMap<>();

        // Step 4: Loop one time unit at a time until every process has finished.
        while (completedProcessCount < copiedProcesses.size()) {
            // Step 4a: Add any processes that arrive at this exact time unit to the ready pool.
            nextArrivalIndex = addArrivingProcesses(copiedProcesses, nextArrivalIndex, currentTime, readyPool);

            // Step 4b: If the ready pool is empty, the CPU is idle - record an idle block.
            if (readyPool.isEmpty()) {
                scheduleResult.getGanttChart().add(
                        new GanttEntry(CPU_IDLE_PROCESS_ID, currentTime, currentTime + ONE_TIME_UNIT));
                currentTime += ONE_TIME_UNIT;
                continue;
            }

            // Step 4c: Pick the highest-priority process from the ready pool.
            Process selectedProcess = pickHighestPriorityProcess(
                    readyPool, lowerNumberMeansHigherPriority);

            // Step 4d: If this is the first time this process runs, record its firstExecutionTime.
            recordFirstExecutionTime(selectedProcess, currentTime, firstExecutionTimeMap);

            // Step 4e: Run it for exactly 1 time unit. Decrease its remainingTime by 1.
            selectedProcess.setRemainingTime(selectedProcess.getRemainingTime() - ONE_TIME_UNIT);
            int nextTime = currentTime + ONE_TIME_UNIT;

            // Step 4f: Record a GanttEntry for this 1-unit slice.
            scheduleResult.getGanttChart().add(
                    new GanttEntry(selectedProcess.getProcessId(), currentTime, nextTime));

            currentTime = nextTime;

            // Step 4g: If remainingTime is now 0, record its completion time and remove it from the pool.
            if (selectedProcess.getRemainingTime() == 0) {
                completionTimeMap.put(selectedProcess.getProcessId(), currentTime);
                readyPool.remove(selectedProcess);
                completedProcessCount++;
            }

            // Step 4h: On the next iteration, re-evaluate priority - this is what makes it preemptive.
        }

        // Step 5: Calculate metrics and return the result.
        MetricsCalculator metricsCalculator = new MetricsCalculator();
        metricsCalculator.fillMetrics(
                copiedProcesses, completionTimeMap, firstExecutionTimeMap, scheduleResult);
        return scheduleResult;
    }

    /**
     * Creates deep copies of source processes.
     *
     * @param sourceProcesses original process list
     * @return copied process list
     */
    private List<Process> copyProcesses(List<Process> sourceProcesses) {
        List<Process> copiedProcesses = new ArrayList<>();
        for (Process process : sourceProcesses) {
            copiedProcesses.add(new Process(process));
        }
        return copiedProcesses;
    }

    /**
     * Adds arriving processes to the ready pool.
     *
     * @param sortedProcesses processes sorted by arrival time
     * @param nextArrivalIndex index of next candidate process
     * @param currentTime current simulation time
     * @param readyPool list of ready processes
     * @return updated nextArrivalIndex
     */
    private int addArrivingProcesses(
            List<Process> sortedProcesses,
            int nextArrivalIndex,
            int currentTime,
            List<Process> readyPool
    ) {
        while (nextArrivalIndex < sortedProcesses.size()
                && sortedProcesses.get(nextArrivalIndex).getArrivalTime() == currentTime) {
            readyPool.add(sortedProcesses.get(nextArrivalIndex));
            nextArrivalIndex++;
        }
        return nextArrivalIndex;
    }

    /**
     * Selects the process that should run next based on priority rules.
     *
     * @param readyPool currently ready processes
     * @param lowerNumberMeansHigherPriority true when smaller value means higher priority
     * @return chosen process for current tick
     */
    private Process pickHighestPriorityProcess(
            List<Process> readyPool,
            boolean lowerNumberMeansHigherPriority
    ) {
        Process bestProcess = readyPool.get(0);

        for (Process candidateProcess : readyPool) {
            if (isCandidateBetter(candidateProcess, bestProcess, lowerNumberMeansHigherPriority)) {
                bestProcess = candidateProcess;
            }
        }

        return bestProcess;
    }

    /**
     * Applies priority and tie-break rules between two candidate processes.
     *
     * @param candidateProcess process currently being evaluated
     * @param currentBestProcess currently selected best process
     * @param lowerNumberMeansHigherPriority priority rule toggle
     * @return true when candidate should replace current best
     */
    private boolean isCandidateBetter(
            Process candidateProcess,
            Process currentBestProcess,
            boolean lowerNumberMeansHigherPriority
    ) {
        if (candidateProcess.getPriority() != currentBestProcess.getPriority()) {
            if (lowerNumberMeansHigherPriority) {
                return candidateProcess.getPriority() < currentBestProcess.getPriority();
            }
            return candidateProcess.getPriority() > currentBestProcess.getPriority();
        }

        if (candidateProcess.getArrivalTime() != currentBestProcess.getArrivalTime()) {
            return candidateProcess.getArrivalTime() < currentBestProcess.getArrivalTime();
        }

        return candidateProcess.getProcessId() < currentBestProcess.getProcessId();
    }

    /**
     * Saves first execution time for a process if not saved already.
     *
     * @param process process that started executing
     * @param currentTime simulation time
     * @param firstExecutionTimeMap map of first execution times
     */
    private void recordFirstExecutionTime(
            Process process,
            int currentTime,
            Map<Integer, Integer> firstExecutionTimeMap
    ) {
        if (!firstExecutionTimeMap.containsKey(process.getProcessId())) {
            firstExecutionTimeMap.put(process.getProcessId(), currentTime);
        }
    }
}
