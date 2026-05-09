package scheduler;

import scheduler.gui.MainWindow;

// app entry class

public class App {

    /** Starts application in GUI by default. */
    public static void main(String[] args) {
        if (args.length > 0 && "console".equalsIgnoreCase(args[0])) {
            ConsoleSchedulerSimulator.main(new String[0]);
            return;
        }

        MainWindow.applyBestLookAndFeel();
        MainWindow.showWindow();
    }
}
