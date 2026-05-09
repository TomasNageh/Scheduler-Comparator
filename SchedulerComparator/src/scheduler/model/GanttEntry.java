package scheduler.model;

/**
 * Each GanttEntry is one execution slice - it records which process ran,
 * and from when to when.
 */
public class GanttEntry {
    private int processId;
    private int startTime;
    private int endTime;

    /**
     * Creates one timeline slice for the Gantt chart.
     *
     * @param processId process ID, or -1 when CPU is idle
     * @param startTime slice start time (inclusive)
     * @param endTime slice end time (exclusive)
     */
    public GanttEntry(int processId, int startTime, int endTime) {
        this.processId = processId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /** @return process ID for this slice */
    public int getProcessId() {
        return processId;
    }

    /** @param processId process ID to set */
    public void setProcessId(int processId) {
        this.processId = processId;
    }

    /** @return start time */
    public int getStartTime() {
        return startTime;
    }

    /** @param startTime start time to set */
    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    /** @return end time */
    public int getEndTime() {
        return endTime;
    }

    /** @param endTime end time to set */
    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    /**
     * Returns the slice duration.
     *
     * @return endTime - startTime
     */
    public int getDuration() {
        return endTime - startTime;
    }
}
