package scheduler.algorithm;

import java.util.List;
import scheduler.model.Process;
import scheduler.model.ScheduleResult;

/**
 * Both scheduling algorithms implement this interface so they can be called in
 * the same way from the GUI.
 */
public interface Scheduler {

    /**
     * Runs a scheduling simulation and returns all computed outputs.
     *
     * @param processes input processes from the UI
     * @param timeQuantum quantum value used by Round Robin
     * @param lowerNumberMeansHigherPriority priority rule selection from UI
     * @return full schedule result including Gantt chart and metrics
     */
    ScheduleResult simulate(
            List<Process> processes,
            int timeQuantum,
            boolean lowerNumberMeansHigherPriority
    );
}
