package scheduler.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import scheduler.model.Process;
import scheduler.model.ScheduleResult;

/**
 * Shows a table of per-process scheduling metrics after a simulation run.
 */
public class MetricsTablePanel extends JPanel {

    private static final Color HIGHEST_WAITING_TIME_COLOR = new Color(255, 243, 205);
    private static final Color LOWEST_WAITING_TIME_COLOR = new Color(212, 237, 218);

    private final DefaultTableModel tableModel;
    private final JTable metricsTable;
    private final JLabel averagesLabel;
    private int highestWaitingTimeRow = -1;
    private int lowestWaitingTimeRow = -1;

    /**
     * Creates table panel with table and footer area.
     */
    public MetricsTablePanel() {
        setLayout(new BorderLayout());

        tableModel = new DefaultTableModel(
                new String[]{"Process", "Arrival", "Burst", "Priority", "WT", "TAT", "RT"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        metricsTable = new JTable(tableModel);
        metricsTable.setFillsViewportHeight(true);
        metricsTable.setDefaultRenderer(Object.class, new HighlightRenderer());

        JScrollPane tableScrollPane = new JScrollPane(metricsTable);
        add(tableScrollPane, BorderLayout.CENTER);

        averagesLabel = new JLabel("Averages: WT = 0.00   TAT = 0.00   RT = 0.00");
        averagesLabel.setHorizontalAlignment(SwingConstants.CENTER);
        averagesLabel.setFont(averagesLabel.getFont().deriveFont(Font.BOLD));

        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.add(averagesLabel, BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);
    }

    /**
     * Populates metrics table and footer from a simulation result.
     *
     * @param processes original process list
     * @param result algorithm result to display
     */
    public void displayResults(List<Process> processes, ScheduleResult result) {
        clearTable();
        highestWaitingTimeRow = -1;
        lowestWaitingTimeRow = -1;

        List<Integer> waitingTimes = new ArrayList<>();
        for (int rowIndex = 0; rowIndex < processes.size(); rowIndex++) {
            Process process = processes.get(rowIndex);
            int processId = process.getProcessId();
            int waitingTime = result.getWaitingTime().get(processId);
            int turnaroundTime = result.getTurnaroundTime().get(processId);
            int responseTime = result.getResponseTime().get(processId);

            waitingTimes.add(waitingTime);
            tableModel.addRow(new Object[]{
                "P" + processId,
                process.getArrivalTime(),
                process.getBurstTime(),
                process.getPriority(),
                waitingTime,
                turnaroundTime,
                responseTime
            });
        }

        findHighlightRows(waitingTimes);
        updateAveragesFooter(result);
        metricsTable.repaint();
    }

    /**
     * Clears table data.
     */
    private void clearTable() {
        tableModel.setRowCount(0);
    }

    /**
     * Finds row indexes with highest and lowest waiting time.
     *
     * @param waitingTimes waiting times per displayed row
     */
    private void findHighlightRows(List<Integer> waitingTimes) {
        if (waitingTimes.isEmpty()) {
            return;
        }

        int highestValue = waitingTimes.get(0);
        int lowestValue = waitingTimes.get(0);
        highestWaitingTimeRow = 0;
        lowestWaitingTimeRow = 0;

        for (int rowIndex = 1; rowIndex < waitingTimes.size(); rowIndex++) {
            int currentValue = waitingTimes.get(rowIndex);
            if (currentValue > highestValue) {
                highestValue = currentValue;
                highestWaitingTimeRow = rowIndex;
            }
            if (currentValue < lowestValue) {
                lowestValue = currentValue;
                lowestWaitingTimeRow = rowIndex;
            }
        }
    }

    /**
     * Updates averages text in footer.
     *
     * @param result schedule result with average values
     */
    private void updateAveragesFooter(ScheduleResult result) {
        String footerText = "Averages:   WT = " + String.format("%.2f", result.getAvgWaitingTime())
                + "   TAT = " + String.format("%.2f", result.getAvgTurnaroundTime())
                + "   RT = " + String.format("%.2f", result.getAvgResponseTime());
        averagesLabel.setText(footerText);
    }

    /**
     * Highlights rows based on waiting-time extremes.
     */
    private class HighlightRenderer extends DefaultTableCellRenderer {

        /**
         * Returns rendered table cell component with row highlight.
         *
         * @param table source table
         * @param value value to display
         * @param isSelected selection state
         * @param hasFocus focus state
         * @param row row index
         * @param column column index
         * @return rendered component
         */
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            Component renderedComponent = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);

            if (isSelected) {
                return renderedComponent;
            }

            if (row == highestWaitingTimeRow) {
                renderedComponent.setBackground(HIGHEST_WAITING_TIME_COLOR);
            } else if (row == lowestWaitingTimeRow) {
                renderedComponent.setBackground(LOWEST_WAITING_TIME_COLOR);
            } else {
                renderedComponent.setBackground(Color.WHITE);
            }

            return renderedComponent;
        }
    }
}
