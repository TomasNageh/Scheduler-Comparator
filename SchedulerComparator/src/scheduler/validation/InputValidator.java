package scheduler.validation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import scheduler.model.Process;

/**
 * Before running a simulation, all user input is checked here.
 * Any problems are returned as a list of plain-English error messages.
 */
public class InputValidator {

    private static final int MIN_BURST_TIME = 1;
    private static final int MIN_ARRIVAL_TIME = 0;
    private static final int MIN_PRIORITY = 0;
    private static final int MIN_QUANTUM = 1;

    /**
     * Runs all validation rules and returns all collected errors.
     *
     * @param processes process list entered by user
     * @param quantum time quantum entered by user
     * @return list of errors; empty list means valid input
     */
    public List<String> validate(List<Process> processes, int quantum) {
        List<String> errors = new ArrayList<>();

        errors.addAll(checkAtLeastOneProcess(processes));
        errors.addAll(checkForDuplicateIds(processes));
        errors.addAll(checkBurstTimesAreValid(processes));
        errors.addAll(checkArrivalTimesAreValid(processes));
        errors.addAll(checkPrioritiesAreValid(processes));
        errors.addAll(checkQuantumIsValid(quantum));

        return errors;
    }

    /**
     * Checks whether two processes use the same process ID.
     *
     * @param processes process list to check
     * @return duplicate-ID errors
     */
    public List<String> checkForDuplicateIds(List<Process> processes) {
        List<String> errors = new ArrayList<>();
        Set<Integer> seenIds = new HashSet<>();

        for (Process process : processes) {
            if (seenIds.contains(process.getProcessId())) {
                errors.add("Two processes share the ID " + process.getProcessId()
                        + ". All process IDs must be unique.");
            } else {
                seenIds.add(process.getProcessId());
            }
        }

        return errors;
    }

    /**
     * Checks whether all burst times are valid.
     *
     * @param processes process list to check
     * @return burst-time errors
     */
    public List<String> checkBurstTimesAreValid(List<Process> processes) {
        List<String> errors = new ArrayList<>();

        for (Process process : processes) {
            if (process.getBurstTime() < MIN_BURST_TIME) {
                errors.add("Process P" + process.getProcessId() + " has a burst time of "
                        + process.getBurstTime() + ". Burst time must be at least 1.");
            }
        }

        return errors;
    }

    /**
     * Checks whether all arrival times are valid.
     *
     * @param processes process list to check
     * @return arrival-time errors
     */
    public List<String> checkArrivalTimesAreValid(List<Process> processes) {
        List<String> errors = new ArrayList<>();

        for (Process process : processes) {
            if (process.getArrivalTime() < MIN_ARRIVAL_TIME) {
                errors.add("Process P" + process.getProcessId() + " has an arrival time of "
                        + process.getArrivalTime() + ". Arrival time must be 0 or more.");
            }
        }

        return errors;
    }

    /**
     * Checks whether all priorities are valid.
     *
     * @param processes process list to check
     * @return priority errors
     */
    public List<String> checkPrioritiesAreValid(List<Process> processes) {
        List<String> errors = new ArrayList<>();

        for (Process process : processes) {
            if (process.getPriority() < MIN_PRIORITY) {
                errors.add("Process P" + process.getProcessId() + " has a priority of "
                        + process.getPriority() + ". Priority must be 0 or more.");
            }
        }

        return errors;
    }

    /**
     * Checks whether quantum value is valid.
     *
     * @param quantum time quantum
     * @return quantum errors
     */
    public List<String> checkQuantumIsValid(int quantum) {
        List<String> errors = new ArrayList<>();
        if (quantum < MIN_QUANTUM) {
            errors.add("Time quantum is " + quantum + ". Time quantum must be at least 1.");
        }
        return errors;
    }

    /**
     * Checks whether there is at least one process to simulate.
     *
     * @param processes process list
     * @return empty-list errors
     */
    public List<String> checkAtLeastOneProcess(List<Process> processes) {
        List<String> errors = new ArrayList<>();
        if (processes.isEmpty()) {
            errors.add("There are no processes to simulate. Please add at least one.");
        }
        return errors;
    }
}
