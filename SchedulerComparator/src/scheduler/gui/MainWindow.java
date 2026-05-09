package scheduler.gui;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * This is the main application window. It holds a JTabbedPane with two tabs:
 * one for running simulations, one for comparing results.
 */
public class MainWindow extends JFrame {

    private static final int WINDOW_WIDTH = 1280;
    private static final int WINDOW_HEIGHT = 860;
    private static final String WINDOW_TITLE =
            "CPU Scheduling Comparator — Round Robin vs Priority";

    private final JTabbedPane tabbedPane;

    /**
     * Creates and configures the main application window.
     */
    public MainWindow() {
        super(WINDOW_TITLE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setMinimumSize(new java.awt.Dimension(1180, 760));
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();
        ComparisonPanel comparisonPanel = new ComparisonPanel();
        SimulationPanel simulationPanel = new SimulationPanel(
                comparisonPanel, this::switchToComparisonTab);

        tabbedPane.addTab("Simulation", simulationPanel);
        tabbedPane.addTab("Comparison", comparisonPanel);
        add(tabbedPane);
    }

    /**
     * Selects the comparison tab after results are available.
     */
    private void switchToComparisonTab() {
        tabbedPane.setSelectedIndex(1);
    }

    /**
     * Applies preferred look and feel with fallback chain.
     */
    public static void applyBestLookAndFeel() {
        try {
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
            return;
        } catch (Exception ignored) {
        }

        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    return;
                }
            }
        } catch (Exception ignored) {
        }

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
    }

    /**
     * Opens the main window on Swing event dispatch thread.
     */
    public static void showWindow() {
        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();
            window.setVisible(true);
        });
    }
}
