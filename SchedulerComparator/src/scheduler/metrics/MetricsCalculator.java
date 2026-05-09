package scheduler.metrics;

import java.util.List;
import java.util.Map;
import scheduler.model.Process;
import scheduler.model.ScheduleResult;

/**
 * After a simulation runs, this class computes the three standard scheduling
 * metrics for each process.
 *
 * Turnaround Time (TAT) = Completion Time - Arrival Time
 * Waiting Time (WT) = Turnaround Time - Burst Time
 * Response Time (RT) = First Execution - Arrival Time
 */
public class MetricsCalculator {

    /**
     * Calculates all metrics for all processes and writes them into result.
     *
     * @param processes process definitions used in simulation
     * @param completionTimeMap completion times per process ID
     * @param firstExecutionTimeMap first execution times per process ID
     * @param result result object to fill
     */
    public void fillMetrics(
            List<Process> processes,
            Map<Integer, Integer> completionTimeMap,
            Map<Integer, Integer> firstExecutionTimeMap,
            ScheduleResult result
    ) {
        for (Process process : processes) {
            int processId = process.getProcessId();
            int completionTime = completionTimeMap.get(processId);
            int firstExecutionTime = firstExecutionTimeMap.get(processId);

            int turnaroundTime = calculateTurnaroundTime(completionTime, process.getArrivalTime());
            int waitingTime = calculateWaitingTime(turnaroundTime, process.getBurstTime());
            int responseTime = calculateResponseTime(firstExecutionTime, process.getArrivalTime());

            result.getTurnaroundTime().put(processId, turnaroundTime);
            result.getWaitingTime().put(processId, waitingTime);
            result.getResponseTime().put(processId, responseTime);
        }

        calculateAverages(result);
    }

    /**
     * Calculates turnaround time for one process.
     *
     * @param completionTime process completion time
     * @param arrivalTime process arrival time
     * @return turnaround time
     */
    public int calculateTurnaroundTime(int completionTime, int arrivalTime) {
        return completionTime - arrivalTime;
    }

    /**
     * Calculates waiting time for one process.
     *
     * @param turnaroundTime process turnaround time
     * @param burstTime process burst time
     * @return waiting time
     */
    public int calculateWaitingTime(int turnaroundTime, int burstTime) {
        return turnaroundTime - burstTime;
    }

    /**
     * Calculates response time for one process.
     *
     * @param firstExecutionTime first time process got CPU
     * @param arrivalTime process arrival time
     * @return response time
     */
    public int calculateResponseTime(int firstExecutionTime, int arrivalTime) {
        return firstExecutionTime - arrivalTime;
    }

    /**
     * Calculates average WT, TAT, and RT across all processes.
     *
     * @param result result object containing per-process metrics
     */
    public void calculateAverages(ScheduleResult result) {
        double totalWaitingTime = 0.0;
        double totalTurnaroundTime = 0.0;
        double totalResponseTime = 0.0;

        int processCount = result.getWaitingTime().size();
        if (processCount == 0) {
            return;
        }

        for (Integer value : result.getWaitingTime().values()) {
            totalWaitingTime += value;
        }

        for (Integer value : result.getTurnaroundTime().values()) {
            totalTurnaroundTime += value;
        }

        for (Integer value : result.getResponseTime().values()) {
            totalResponseTime += value;
        }

        result.setAvgWaitingTime(totalWaitingTime / processCount);
        result.setAvgTurnaroundTime(totalTurnaroundTime / processCount);
        result.setAvgResponseTime(totalResponseTime / processCount);
    }
}
