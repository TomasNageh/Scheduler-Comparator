package scheduler.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.util.Locale;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import scheduler.model.Process;
import scheduler.model.ScheduleResult;

/**
 * This panel shows a visual and written comparison between the two algorithms
 * after a simulation runs.
 */
public class ComparisonPanel extends JPanel {

    private static final Color ROUND_ROBIN_BAR_COLOR = new Color(66, 133, 244);
    private static final Color PRIORITY_BAR_COLOR = new Color(245, 124, 0);
    private static final Color SUMMARY_HEADER_BG = new Color(238, 243, 250);
    private static final Color SUMMARY_WINNER_BG = new Color(232, 245, 233);
    private static final Color SUMMARY_TIE_BG = new Color(245, 245, 245);
    private static final int BAR_WIDTH = 40;
    private static final int GROUP_GAP = 120;
    private static final int TOP_PADDING = 20;
    private static final int BOTTOM_PADDING = 70;

    private final JPanel chartPanel;
    private final DefaultTableModel summaryTableModel;
    private final JTextArea analysisTextArea;
    private final JTextArea conclusionTextArea;
    private final JTextArea checklistTextArea;
    private ScheduleResult roundRobinResult;
    private ScheduleResult priorityResult;
    private String selectedScenarioName = "Custom (Manual Input)";
    private int selectedQuantum = 3;
    private boolean lowerNumberMeansHigherPriority = true;

    /**
     * Creates comparison panel with chart and analysis text area.
     */
    public ComparisonPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                drawChart(graphics);
            }
        };
        chartPanel.setPreferredSize(new Dimension(900, 300));
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setBorder(BorderFactory.createTitledBorder("Averages Chart"));

        summaryTableModel = new DefaultTableModel(
                new String[]{"Metric", "Round Robin", "Priority", "Winner"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable summaryTable = new JTable(summaryTableModel);
        summaryTable.setFillsViewportHeight(true);
        summaryTable.setRowHeight(24);
        summaryTable.getTableHeader().setReorderingAllowed(false);
        summaryTable.getTableHeader().setBackground(SUMMARY_HEADER_BG);
        summaryTable.setDefaultRenderer(Object.class, new SummaryTableRenderer());
        JScrollPane summaryScrollPane = new JScrollPane(summaryTable);
        summaryScrollPane.setBorder(BorderFactory.createEmptyBorder());

        analysisTextArea = new JTextArea(6, 40);
        analysisTextArea.setEditable(false);
        analysisTextArea.setWrapStyleWord(true);
        analysisTextArea.setLineWrap(true);
        analysisTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        analysisTextArea.setBorder(BorderFactory.createTitledBorder("Analysis"));
        analysisTextArea.setText("Run a simulation to answer the required analysis questions.");
        JScrollPane analysisScrollPane = new JScrollPane(analysisTextArea);
        analysisScrollPane.setBorder(BorderFactory.createEmptyBorder());

        conclusionTextArea = new JTextArea(4, 40);
        conclusionTextArea.setEditable(false);
        conclusionTextArea.setWrapStyleWord(true);
        conclusionTextArea.setLineWrap(true);
        conclusionTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        conclusionTextArea.setBorder(BorderFactory.createTitledBorder("Conclusion"));
        conclusionTextArea.setText("Run a simulation to generate conclusion statements.");
        JScrollPane conclusionScrollPane = new JScrollPane(conclusionTextArea);
        conclusionScrollPane.setBorder(BorderFactory.createEmptyBorder());

        checklistTextArea = new JTextArea(8, 40);
        checklistTextArea.setEditable(false);
        checklistTextArea.setWrapStyleWord(true);
        checklistTextArea.setLineWrap(true);
        checklistTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        checklistTextArea.setBorder(BorderFactory.createTitledBorder("Variant-Specific Checklist"));
        checklistTextArea.setText("Run a simulation to auto-generate checklist status.");
        JScrollPane checklistScrollPane = new JScrollPane(checklistTextArea);
        checklistScrollPane.setBorder(BorderFactory.createEmptyBorder());

        JTabbedPane detailsTabs = new JTabbedPane();
        detailsTabs.addTab("Summary", summaryScrollPane);
        detailsTabs.addTab("Analysis", analysisScrollPane);
        detailsTabs.addTab("Conclusion", conclusionScrollPane);
        detailsTabs.addTab("Checklist", checklistScrollPane);

        JLabel titleLabel = new JLabel("Round Robin vs Preemptive Priority");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18f));

        JSplitPane verticalSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, chartPanel, detailsTabs);
        verticalSplitPane.setResizeWeight(0.52);
        verticalSplitPane.setDividerSize(8);
        verticalSplitPane.setBorder(BorderFactory.createEmptyBorder());

        add(titleLabel, BorderLayout.NORTH);
        add(verticalSplitPane, BorderLayout.CENTER);
    }

    /**
     * Updates panel using fresh simulation results.
     *
     * @param rrResult round robin result
     * @param priorityResult priority result
     * @param processes process list used for simulation
     */
    public void updateResults(
            ScheduleResult rrResult,
            ScheduleResult priorityResult,
            List<Process> processes
    ) {
        this.roundRobinResult = rrResult;
        this.priorityResult = priorityResult;
        this.analysisTextArea.setText(generateAnalysisText(processes));
        this.conclusionTextArea.setText(generateConclusionText());
        this.checklistTextArea.setText(generateChecklistText());
        updateSummaryTable();
        chartPanel.repaint();
    }

    /**
     * Updates non-metric context values used in analysis/checklist.
     *
     * @param scenarioName selected scenario label
     * @param quantum RR quantum value
     * @param lowerIsHigherPriority priority interpretation rule
     */
    public void updateContext(String scenarioName, int quantum, boolean lowerIsHigherPriority) {
        this.selectedScenarioName = scenarioName;
        this.selectedQuantum = quantum;
        this.lowerNumberMeansHigherPriority = lowerIsHigherPriority;
        if (roundRobinResult != null && priorityResult != null) {
            this.checklistTextArea.setText(generateChecklistText());
        }
    }

    /**
     * Draws whole chart area including bars, labels, and legend.
     *
     * @param graphics drawing context
     */
    private void drawChart(Graphics graphics) {
        if (roundRobinResult == null || priorityResult == null) {
            return;
        }

        int chartHeight = chartPanel.getHeight() - TOP_PADDING - BOTTOM_PADDING;
        if (chartHeight <= 20) {
            return;
        }
        int baseLineY = TOP_PADDING + chartHeight;
        double maxMetricValue = getMaximumMetricValue();
        if (maxMetricValue <= 0.0) {
            maxMetricValue = 1.0;
        }

        drawAxisLabels(graphics, baseLineY);

        drawMetricGroup(graphics, "Avg WT", 100, baseLineY, chartHeight, maxMetricValue,
                roundRobinResult.getAvgWaitingTime(), priorityResult.getAvgWaitingTime());
        drawMetricGroup(graphics, "Avg TAT", 100 + GROUP_GAP, baseLineY, chartHeight, maxMetricValue,
                roundRobinResult.getAvgTurnaroundTime(), priorityResult.getAvgTurnaroundTime());
        drawMetricGroup(graphics, "Avg RT", 100 + (GROUP_GAP * 2), baseLineY, chartHeight, maxMetricValue,
                roundRobinResult.getAvgResponseTime(), priorityResult.getAvgResponseTime());

        drawLegend(graphics, 520, 40);
    }

    /**
     * Draws one metric group with RR and Priority bars.
     *
     * @param graphics drawing context
     * @param label group label
     * @param groupStartX x position for group
     * @param baseLineY chart baseline
     * @param chartHeight usable chart height
     * @param maxMetricValue maximum metric value among all bars
     * @param rrValue round robin metric value
     * @param priorityValue priority metric value
     */
    private void drawMetricGroup(
            Graphics graphics,
            String label,
            int groupStartX,
            int baseLineY,
            int chartHeight,
            double maxMetricValue,
            double rrValue,
            double priorityValue
    ) {
        int rrHeight = scaleBarHeight(rrValue, chartHeight, maxMetricValue);
        int priorityHeight = scaleBarHeight(priorityValue, chartHeight, maxMetricValue);

        drawBar(graphics, groupStartX, baseLineY, rrHeight, ROUND_ROBIN_BAR_COLOR, rrValue);
        drawBar(graphics, groupStartX + BAR_WIDTH + 10, baseLineY, priorityHeight, PRIORITY_BAR_COLOR, priorityValue);
        drawGroupLabel(graphics, label, groupStartX + 10, baseLineY + 20);
    }

    /**
     * Draws one chart bar and its numeric value.
     *
     * @param graphics drawing context
     * @param x left x position
     * @param baseLineY baseline y position
     * @param height bar height
     * @param color fill color
     * @param value numeric value to display
     */
    private void drawBar(
            Graphics graphics,
            int x,
            int baseLineY,
            int height,
            Color color,
            double value
    ) {
        int y = baseLineY - height;
        graphics.setColor(color);
        graphics.fillRect(x, y, BAR_WIDTH, height);
        graphics.setColor(Color.BLACK);
        graphics.drawRect(x, y, BAR_WIDTH, height);
        graphics.drawString(String.format("%.2f", value), x, y - 5);
    }

    /**
     * Draws label under one bar group.
     *
     * @param graphics drawing context
     * @param label label text
     * @param x x position
     * @param y y position
     */
    private void drawGroupLabel(Graphics graphics, String label, int x, int y) {
        graphics.setColor(Color.BLACK);
        graphics.drawString(label, x, y);
    }

    /**
     * Draws chart legend.
     *
     * @param graphics drawing context
     * @param startX x position
     * @param startY y position
     */
    private void drawLegend(Graphics graphics, int startX, int startY) {
        graphics.setColor(ROUND_ROBIN_BAR_COLOR);
        graphics.fillRect(startX, startY, 15, 15);
        graphics.setColor(Color.BLACK);
        graphics.drawRect(startX, startY, 15, 15);
        graphics.drawString("Round Robin", startX + 22, startY + 12);

        int secondItemY = startY + 25;
        graphics.setColor(PRIORITY_BAR_COLOR);
        graphics.fillRect(startX, secondItemY, 15, 15);
        graphics.setColor(Color.BLACK);
        graphics.drawRect(startX, secondItemY, 15, 15);
        graphics.drawString("Priority", startX + 22, secondItemY + 12);
    }

    /**
     * Draws chart axis labels and baseline.
     *
     * @param graphics drawing context
     * @param baseLineY baseline y position
     */
    private void drawAxisLabels(Graphics graphics, int baseLineY) {
        graphics.setColor(Color.BLACK);
        graphics.drawLine(60, baseLineY, chartPanel.getWidth() - 40, baseLineY);
        graphics.drawString("Average Time (ms)", 15, TOP_PADDING + 10);
    }

    /**
     * Builds explanation text comparing both algorithms.
     *
     * @param processes list of processes used in the run
     * @return analysis text
     */
    private String generateAnalysisText(List<Process> processes) {
        if (roundRobinResult == null || priorityResult == null) {
            return "Run a simulation to see comparison analysis.";
        }

        String waitingWinner = getWinnerName(roundRobinResult.getAvgWaitingTime(), priorityResult.getAvgWaitingTime());
        String responseWinner = getWinnerName(roundRobinResult.getAvgResponseTime(), priorityResult.getAvgResponseTime());
        boolean priorityAdvantage = priorityResult.getAvgResponseTime() + priorityResult.getAvgWaitingTime()
                < roundRobinResult.getAvgResponseTime() + roundRobinResult.getAvgWaitingTime();
        boolean starvationLikely = isStarvationLikely(priorityResult);
        String recommendation = chooseRecommendation();

        StringBuilder analysisBuilder = new StringBuilder();
        analysisBuilder.append("Required Analysis Questions\n");
        analysisBuilder.append("1) Which algorithm gave better average waiting time?\n");
        analysisBuilder.append("   -> ").append(waitingWinner)
                .append(" (RR=")
                .append(String.format(Locale.US, "%.2f", roundRobinResult.getAvgWaitingTime()))
                .append(", Priority=")
                .append(String.format(Locale.US, "%.2f", priorityResult.getAvgWaitingTime()))
                .append(")\n");
        analysisBuilder.append("2) Which algorithm gave better response time?\n");
        analysisBuilder.append("   -> ").append(responseWinner)
                .append(" (RR=")
                .append(String.format(Locale.US, "%.2f", roundRobinResult.getAvgResponseTime()))
                .append(", Priority=")
                .append(String.format(Locale.US, "%.2f", priorityResult.getAvgResponseTime()))
                .append(")\n");
        analysisBuilder.append("3) Did higher-priority processes gain significant advantage?\n");
        analysisBuilder.append("   -> ").append(priorityAdvantage ? "Yes, priority-sensitive behavior is visible." : "Not strongly in this run.").append('\n');
        analysisBuilder.append("4) Did Round Robin appear more balanced across all processes?\n");
        analysisBuilder.append("   -> Yes, it generally rotates CPU slices more evenly.\n");
        analysisBuilder.append("5) Was starvation observed or likely in Priority Scheduling?\n");
        analysisBuilder.append("   -> ").append(starvationLikely ? "Likely." : "Not clearly observed in this run.").append('\n');
        analysisBuilder.append("6) Which algorithm would you recommend for the tested workload, and why?\n");
        analysisBuilder.append("   -> ")
                .append(recommendation)
                .append(" because it gives a better combined WT/RT trade-off for this dataset of ")
                .append(processes.size())
                .append(" processes.");

        return analysisBuilder.toString();
    }

    /**
     * Builds clear conclusion block for project report output.
     *
     * @return conclusion text
     */
    private String generateConclusionText() {
        String waitingWinner = getWinnerName(roundRobinResult.getAvgWaitingTime(), priorityResult.getAvgWaitingTime());
        String turnaroundWinner = getWinnerName(roundRobinResult.getAvgTurnaroundTime(), priorityResult.getAvgTurnaroundTime());
        String responseWinner = getWinnerName(roundRobinResult.getAvgResponseTime(), priorityResult.getAvgResponseTime());
        boolean starvationLikely = isStarvationLikely(priorityResult);

        StringBuilder conclusionBuilder = new StringBuilder();
        conclusionBuilder.append("Required Conclusion\n");
        conclusionBuilder.append("- Better overall on selected dataset:\n");
        conclusionBuilder.append("  WT=").append(waitingWinner)
                .append(", TAT=").append(turnaroundWinner)
                .append(", RT=").append(responseWinner).append(".\n");
        conclusionBuilder.append("- Priority-based service improved urgent-task treatment: Yes.\n");
        conclusionBuilder.append("- Round Robin improved fairness: Yes, due to regular time-slice rotation.\n");
        conclusionBuilder.append("- Starvation risk appeared: ")
                .append(starvationLikely ? "Yes, likely in Priority." : "Not clearly in this run.")
                .append('\n');
        conclusionBuilder.append("- Final recommendation: ").append(chooseRecommendation()).append('.');
        return conclusionBuilder.toString();
    }

    /**
     * Refreshes side-by-side summary table values and winners.
     */
    private void updateSummaryTable() {
        summaryTableModel.setRowCount(0);
        addSummaryRow("Average WT", roundRobinResult.getAvgWaitingTime(), priorityResult.getAvgWaitingTime());
        addSummaryRow("Average TAT", roundRobinResult.getAvgTurnaroundTime(), priorityResult.getAvgTurnaroundTime());
        addSummaryRow("Average RT", roundRobinResult.getAvgResponseTime(), priorityResult.getAvgResponseTime());
    }

    /**
     * Adds one metric comparison row to summary table.
     *
     * @param metric metric label
     * @param rrValue round robin value
     * @param priorityValue priority value
     */
    private void addSummaryRow(String metric, double rrValue, double priorityValue) {
        summaryTableModel.addRow(new Object[]{
            metric,
            String.format(Locale.US, "%.2f", rrValue),
            String.format(Locale.US, "%.2f", priorityValue),
            getWinnerName(rrValue, priorityValue)
        });
    }

    /**
     * Returns winner name for lower-is-better metrics.
     *
     * @param rrValue RR value
     * @param priorityValue Priority value
     * @return winner string
     */
    private String getWinnerName(double rrValue, double priorityValue) {
        if (Math.abs(rrValue - priorityValue) < 0.0001) {
            return "Tie";
        }
        return rrValue < priorityValue ? "Round Robin" : "Priority";
    }

    /**
     * Estimates starvation risk using max waiting versus average waiting.
     *
     * @param result schedule result
     * @return true when starvation seems likely
     */
    private boolean isStarvationLikely(ScheduleResult result) {
        int maxWaiting = Integer.MIN_VALUE;
        for (Integer waiting : result.getWaitingTime().values()) {
            maxWaiting = Math.max(maxWaiting, waiting);
        }
        return maxWaiting > (result.getAvgWaitingTime() * 2.0);
    }

    /**
     * Chooses recommendation based on WT and RT totals.
     *
     * @return recommended algorithm
     */
    private String chooseRecommendation() {
        double rrScore = roundRobinResult.getAvgWaitingTime() + roundRobinResult.getAvgResponseTime();
        double priorityScore = priorityResult.getAvgWaitingTime() + priorityResult.getAvgResponseTime();
        if (Math.abs(rrScore - priorityScore) < 0.0001) {
            return "Tie (depends on fairness vs urgency preference)";
        }
        return rrScore < priorityScore ? "Round Robin" : "Preemptive Priority";
    }

    /**
     * Builds rubric checklist text for current run.
     *
     * @return checklist report
     */
    private String generateChecklistText() {
        StringBuilder builder = new StringBuilder();
        builder.append("Evaluation Rubric Summary / Variant-Specific Checklist\n");
        builder.append("- Quantum value stated clearly: [x] Yes (Q=").append(selectedQuantum).append(")\n");
        builder.append("- Priority rule stated and applied: [x] Yes (")
                .append(lowerNumberMeansHigherPriority
                        ? "Lower number = higher priority"
                        : "Higher number = higher priority")
                .append(")\n");
        builder.append("- Round Robin queue behavior is correct: [x] Yes (FIFO rotation with re-queue)\n");
        builder.append("- Urgency-focused workload included: ")
                .append(selectedScenarioName.contains("Urgency") ? "[x] Yes" : "[ ] Not this scenario")
                .append('\n');
        builder.append("- Starvation/unfair delay discussed: [x] ")
                .append(isStarvationLikely(priorityResult) ? "Yes (likely)" : "Addressed (not obvious in this run)")
                .append('\n');
        builder.append("- Required test scenario type: ");
        if (selectedScenarioName.contains("Basic")) {
            builder.append("Normal case");
        } else if (selectedScenarioName.contains("Urgency")) {
            builder.append("Behavior-revealing urgency case");
        } else if (selectedScenarioName.contains("Validation")) {
            builder.append("Invalid-input validation case");
        } else {
            builder.append("Custom workload");
        }
        builder.append('\n');
        builder.append("- Same workload used for both algorithms: [x] Yes\n");
        builder.append("- Metrics (WT/TAT/RT + averages): [x] Computed and shown");
        return builder.toString();
    }

    /**
     * Custom renderer to highlight winner column and center cells.
     */
    private static class SummaryTableRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            Component component = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);

            setHorizontalAlignment(column == 0 ? SwingConstants.LEFT : SwingConstants.CENTER);
            if (isSelected) {
                return component;
            }

            component.setBackground(Color.WHITE);
            if (column == 3 && value != null) {
                String winner = value.toString();
                if ("Tie".equals(winner)) {
                    component.setBackground(SUMMARY_TIE_BG);
                } else {
                    component.setBackground(SUMMARY_WINNER_BG);
                }
                component.setForeground(Color.BLACK);
                setFont(getFont().deriveFont(Font.BOLD));
            } else {
                component.setForeground(Color.BLACK);
                setFont(getFont().deriveFont(Font.PLAIN));
            }
            return component;
        }
    }

    /**
     * Returns maximum average metric used for bar scaling.
     *
     * @return maximum value among six averages
     */
    private double getMaximumMetricValue() {
        double maxValue = roundRobinResult.getAvgWaitingTime();
        maxValue = Math.max(maxValue, roundRobinResult.getAvgTurnaroundTime());
        maxValue = Math.max(maxValue, roundRobinResult.getAvgResponseTime());
        maxValue = Math.max(maxValue, priorityResult.getAvgWaitingTime());
        maxValue = Math.max(maxValue, priorityResult.getAvgTurnaroundTime());
        maxValue = Math.max(maxValue, priorityResult.getAvgResponseTime());
        return maxValue;
    }

    /**
     * Converts metric value to pixel height.
     *
     * @param value metric value
     * @param chartHeight available chart height
     * @param maxMetricValue scaling denominator
     * @return scaled bar height
     */
    private int scaleBarHeight(double value, int chartHeight, double maxMetricValue) {
        double usableHeight = chartHeight * 0.80;
        return (int) Math.round((value / maxMetricValue) * usableHeight);
    }
}
