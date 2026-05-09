package scheduler.algorithm;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import scheduler.metrics.MetricsCalculator;
import scheduler.model.GanttEntry;
import scheduler.model.Process;
import scheduler.model.ScheduleResult;

/**
 * Round Robin is a preemptive scheduling algorithm that shares CPU time fairly.
 * All ready processes stand in a queue and take turns using the CPU.
 * The time quantum is the maximum time one process can run in one turn.
 * If a process finishes before using the full quantum, it leaves the system.
 * If it does not finish, it is paused and moved to the back of the queue.
 * This queue rotation gives each process regular CPU opportunities.
 * Because everyone receives equal-sized slices, Round Robin is considered fair.
 * It is often good for interactive systems where quick first response matters.
 * Very small quantum increases context switches, while very large quantum starts
 * behaving similarly to FCFS.
 */
public class RoundRobinScheduler implements Scheduler {

    private static final int CPU_IDLE_PROCESS_ID = -1;
    private static final int ONE_TIME_UNIT = 1;

    /**
     * Simulates Round Robin scheduling over the provided process list.
     *
     * @param processes input processes from the UI
     * @param timeQuantum maximum time units per CPU turn
     * @param lowerNumberMeansHigherPriority unused in Round Robin
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

        // Step 2: Sort the copied processes by arrival time (earliest first).
        copiedProcesses.sort(Comparator.comparingInt(Process::getArrivalTime));

        // Step 3: Set up the ready queue - this holds processes waiting for CPU time.
        Queue<Process> readyQueue = new ArrayDeque<>();

        // Step 4: Keep track of which processes have arrived so we do not add them twice.
        int nextArrivalIndex = 0;
        int completedProcessCount = 0;
        int currentTime = 0;
        Map<Integer, Integer> completionTimeMap = new HashMap<>();
        Map<Integer, Integer> firstExecutionTimeMap = new HashMap<>();

        // Step 5: Loop one time unit at a time until every process has finished.
        while (completedProcessCount < copiedProcesses.size()) {
            // Step 5a: Check if any new processes have arrived at this time unit and add them.
            nextArrivalIndex = addArrivingProcesses(
                    copiedProcesses, nextArrivalIndex, currentTime, readyQueue);

            // Step 5b: If the ready queue is empty, the CPU is idle - record an idle block and move on.
            if (readyQueue.isEmpty()) {
                scheduleResult.getGanttChart().add(
                        new GanttEntry(CPU_IDLE_PROCESS_ID, currentTime, currentTime + ONE_TIME_UNIT));
                currentTime += ONE_TIME_UNIT;
                continue;
            }

            // Step 5c: Take the process at the front of the queue.
            Process currentProcess = readyQueue.poll();

            // Step 5d: If this is the first time this process runs, record its firstExecutionTime.
            recordFirstExecutionTime(currentProcess, currentTime, firstExecutionTimeMap);

            int sliceStartTime = currentTime;

            // Step 5e: Run it for up to one full time quantum (inner loop, one tick at a time).
            int usedQuantum = 0;
            while (usedQuantum < timeQuantum && currentProcess.getRemainingTime() > 0) {
                currentProcess.setRemainingTime(currentProcess.getRemainingTime() - ONE_TIME_UNIT);
                currentTime += ONE_TIME_UNIT;
                usedQuantum += ONE_TIME_UNIT;

                // Inside the inner loop, check again for newly arrived processes each tick.
                nextArrivalIndex = addArrivingProcesses(
                        copiedProcesses, nextArrivalIndex, currentTime, readyQueue);
            }

            // Step 5f: Record a GanttEntry for this execution slice.
            scheduleResult.getGanttChart().add(
                    new GanttEntry(currentProcess.getProcessId(), sliceStartTime, currentTime));

            // Step 5g: If the process still has remaining time, put it back at the end of the queue.
            if (currentProcess.getRemainingTime() > 0) {
                readyQueue.offer(currentProcess);
                continue;
            }

            // Step 5h: If it is done, record its completion time.
            completionTimeMap.put(currentProcess.getProcessId(), currentTime);
            completedProcessCount++;
        }

        // Step 6: Once all processes are done, calculate metrics and return the result.
        MetricsCalculator metricsCalculator = new MetricsCalculator();
        metricsCalculator.fillMetrics(
                copiedProcesses, completionTimeMap, firstExecutionTimeMap, scheduleResult);
        return scheduleResult;
    }

    /**
     * Creates fresh process objects for simulation use.
     *
     * @param sourceProcesses input process list
     * @return deep-copied process list
     */
    private List<Process> copyProcesses(List<Process> sourceProcesses) {
        List<Process> copiedProcesses = new ArrayList<>();
        for (Process process : sourceProcesses) {
            copiedProcesses.add(new Process(process));
        }
        return copiedProcesses;
    }

    /**
     * Adds all processes that have arrived by current time to the queue.
     *
     * @param sortedProcesses processes sorted by arrival time
     * @param nextArrivalIndex pointer to next process candidate
     * @param currentTime simulation clock
     * @param readyQueue queue to receive arrived processes
     * @return updated nextArrivalIndex
     */
    private int addArrivingProcesses(
            List<Process> sortedProcesses,
            int nextArrivalIndex,
            int currentTime,
            Queue<Process> readyQueue
    ) {
        while (nextArrivalIndex < sortedProcesses.size()
                && sortedProcesses.get(nextArrivalIndex).getArrivalTime() <= currentTime) {
            readyQueue.offer(sortedProcesses.get(nextArrivalIndex));
            nextArrivalIndex++;
        }
        return nextArrivalIndex;
    }

    /**
     * Saves first execution time only once for each process.
     *
     * @param currentProcess currently running process
     * @param currentTime simulation time
     * @param firstExecutionTimeMap map where first execution times are stored
     */
    private void recordFirstExecutionTime(
            Process currentProcess,
            int currentTime,
            Map<Integer, Integer> firstExecutionTimeMap
    ) {
        if (!firstExecutionTimeMap.containsKey(currentProcess.getProcessId())) {
            firstExecutionTimeMap.put(currentProcess.getProcessId(), currentTime);
        }
    }
}
