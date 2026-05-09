package scheduler.model;

/**
 * This class stores all the information about one process.
 * It is used as the input to both scheduling algorithms.
 */
public class Process {
    private int processId;
    private int arrivalTime;
    private int burstTime;
    private int priority;
    private int remainingTime;

    /**
     * Creates a process with full input data.
     *
     * @param processId unique numeric process identifier
     * @param arrivalTime time when the process enters the system
     * @param burstTime total CPU time required by this process
     * @param priority scheduling priority value of this process
     */
    public Process(int processId, int arrivalTime, int burstTime, int priority) {
        this.processId = processId;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.priority = priority;
        this.remainingTime = burstTime;
    }

    /**
     * Creates a deep copy of another process.
     *
     * @param sourceProcess process to copy from
     */
    public Process(Process sourceProcess) {
        this.processId = sourceProcess.processId;
        this.arrivalTime = sourceProcess.arrivalTime;
        this.burstTime = sourceProcess.burstTime;
        this.priority = sourceProcess.priority;
        this.remainingTime = sourceProcess.remainingTime;
    }

    /** @return process identifier */
    public int getProcessId() {
        return processId;
    }

    /** @param processId process identifier to set */
    public void setProcessId(int processId) {
        this.processId = processId;
    }

    /** @return arrival time */
    public int getArrivalTime() {
        return arrivalTime;
    }

    /** @param arrivalTime arrival time to set */
    public void setArrivalTime(int arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    /** @return total burst time */
    public int getBurstTime() {
        return burstTime;
    }

    /** @param burstTime burst time to set */
    public void setBurstTime(int burstTime) {
        this.burstTime = burstTime;
    }

    /** @return priority value */
    public int getPriority() {
        return priority;
    }

    /** @param priority priority value to set */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    /** @return remaining execution time */
    public int getRemainingTime() {
        return remainingTime;
    }

    /** @param remainingTime remaining execution time to set */
    public void setRemainingTime(int remainingTime) {
        this.remainingTime = remainingTime;
    }
}
