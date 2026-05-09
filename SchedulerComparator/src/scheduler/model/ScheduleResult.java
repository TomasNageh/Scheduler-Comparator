package scheduler.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * After a simulation finishes, all results are stored here - the Gantt chart
 * timeline, per-process metrics, and averages.
 */
public class ScheduleResult {
    private List<GanttEntry> ganttChart;
    private Map<Integer, Integer> waitingTime;
    private Map<Integer, Integer> turnaroundTime;
    private Map<Integer, Integer> responseTime;
    private double avgWaitingTime;
    private double avgTurnaroundTime;
    private double avgResponseTime;

    /**
     * Creates an empty result object with initialized collections.
     */
    public ScheduleResult() {
        this.ganttChart = new ArrayList<>();
        this.waitingTime = new LinkedHashMap<>();
        this.turnaroundTime = new LinkedHashMap<>();
        this.responseTime = new LinkedHashMap<>();
    }

    /** @return Gantt chart entries */
    public List<GanttEntry> getGanttChart() {
        return ganttChart;
    }

    /** @param ganttChart Gantt chart entries to set */
    public void setGanttChart(List<GanttEntry> ganttChart) {
        this.ganttChart = ganttChart;
    }

    /** @return waiting time map */
    public Map<Integer, Integer> getWaitingTime() {
        return waitingTime;
    }

    /** @param waitingTime waiting time map to set */
    public void setWaitingTime(Map<Integer, Integer> waitingTime) {
        this.waitingTime = waitingTime;
    }

    /** @return turnaround time map */
    public Map<Integer, Integer> getTurnaroundTime() {
        return turnaroundTime;
    }

    /** @param turnaroundTime turnaround time map to set */
    public void setTurnaroundTime(Map<Integer, Integer> turnaroundTime) {
        this.turnaroundTime = turnaroundTime;
    }

    /** @return response time map */
    public Map<Integer, Integer> getResponseTime() {
        return responseTime;
    }

    /** @param responseTime response time map to set */
    public void setResponseTime(Map<Integer, Integer> responseTime) {
        this.responseTime = responseTime;
    }

    /** @return average waiting time */
    public double getAvgWaitingTime() {
        return avgWaitingTime;
    }

    /** @param avgWaitingTime average waiting time to set */
    public void setAvgWaitingTime(double avgWaitingTime) {
        this.avgWaitingTime = avgWaitingTime;
    }

    /** @return average turnaround time */
    public double getAvgTurnaroundTime() {
        return avgTurnaroundTime;
    }

    /** @param avgTurnaroundTime average turnaround time to set */
    public void setAvgTurnaroundTime(double avgTurnaroundTime) {
        this.avgTurnaroundTime = avgTurnaroundTime;
    }

    /** @return average response time */
    public double getAvgResponseTime() {
        return avgResponseTime;
    }

    /** @param avgResponseTime average response time to set */
    public void setAvgResponseTime(double avgResponseTime) {
        this.avgResponseTime = avgResponseTime;
    }
}
