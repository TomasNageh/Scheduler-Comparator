package scheduler.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import scheduler.algorithm.PriorityScheduler;
import scheduler.algorithm.RoundRobinScheduler;
import scheduler.model.GanttEntry;
import scheduler.model.Process;
import scheduler.model.ScheduleResult;
import scheduler.validation.InputValidator;

/**
 * This panel contains user input controls, process table, and both algorithm
 * result views. It also triggers simulation and updates the comparison panel.
 */
public class SimulationPanel extends JPanel {

    private static final int DEFAULT_QUANTUM = 3;
    private static final int MIN_QUANTUM = 1;
    private static final int MAX_QUANTUM = 100;
    private static final Color RUN_BUTTON_COLOR = new Color(74, 144, 217);
    private static final String SCENARIO_CUSTOM = "Custom (Manual Input)";
    private static final String SCENARIO_A = "Scenario A - Basic mixed workload";
    private static final String SCENARIO_B = "Scenario B - Urgency case";
    private static final String SCENARIO_C = "Scenario C - Fairness case";
    private static final String SCENARIO_D = "Scenario D - Starvation case";
    private static final String SCENARIO_E = "Scenario E - Validation demo";

    private final ProcessTableModel processTableModel;
    private final JTable processTable;
    private final JSpinner quantumSpinner;
    private final JRadioButton lowerNumberHigherPriorityOption;
    private final JRadioButton higherNumberHigherPriorityOption;
    private final JComboBox<String> scenarioComboBox;
    private final JButton addProcessButton;
    private final JButton quickPasteButton;
    private final JButton deleteItemButton;
    private final JButton clearAllButton;
    private final JButton resetButton;
    private final JButton loadScenarioButton;
    private final JButton runSimulationButton;
    private final JLabel errorLabel;
    private final JPanel resultsPanel;
    private final JLabel roundRobinTitleLabel;
    private final GanttChartPanel roundRobinGanttPanel;
    private final MetricsTablePanel roundRobinMetricsPanel;
    private final GanttChartPanel priorityGanttPanel;
    private final MetricsTablePanel priorityMetricsPanel;
    private final JLabel roundRobinQueueLabel;
    private final JLabel priorityQueueLabel;
    private final JLabel roundRobinFooterLabel;
    private final JLabel priorityFooterLabel;
    private final ComparisonPanel comparisonPanel;
    private final Runnable onResultsReadyAction;

    /**
     * Creates the full simulation panel.
     *
     * @param comparisonPanel panel that displays comparison chart and text
     * @param onResultsReadyAction callback to switch tabs after successful run
     */
    public SimulationPanel(ComparisonPanel comparisonPanel, Runnable onResultsReadyAction) {
        this.comparisonPanel = comparisonPanel;
        this.onResultsReadyAction = onResultsReadyAction;
        setLayout(new BorderLayout(10, 10));

        processTableModel = new ProcessTableModel();
        processTable = new JTable(processTableModel);
        quantumSpinner = new JSpinner(new SpinnerNumberModel(DEFAULT_QUANTUM, MIN_QUANTUM, MAX_QUANTUM, 1));
        lowerNumberHigherPriorityOption = new JRadioButton("Lower number = higher priority", true);
        higherNumberHigherPriorityOption = new JRadioButton("Higher number = higher priority");
        scenarioComboBox = new JComboBox<>(new String[]{
            SCENARIO_CUSTOM, SCENARIO_A, SCENARIO_B, SCENARIO_C, SCENARIO_D, SCENARIO_E
        });
        addProcessButton = new JButton("Add Process");
        quickPasteButton = new JButton("Quick Paste");
        deleteItemButton = new JButton("Delete Item");
        clearAllButton = new JButton("Clear All");
        resetButton = new JButton("Reset");
        loadScenarioButton = new JButton("Load Scenario");
        runSimulationButton = new JButton("Run Simulation");
        errorLabel = new JLabel("");
        roundRobinTitleLabel = new JLabel();
        roundRobinGanttPanel = new GanttChartPanel();
        roundRobinMetricsPanel = new MetricsTablePanel();
        priorityGanttPanel = new GanttChartPanel();
        priorityMetricsPanel = new MetricsTablePanel();
        roundRobinQueueLabel = new JLabel("Queue: -");
        priorityQueueLabel = new JLabel("Queue: -");
        roundRobinFooterLabel = new JLabel("Avg WT: 0.00 | Avg TAT: 0.00 | Avg RT: 0.00");
        priorityFooterLabel = new JLabel("Avg WT: 0.00 | Avg TAT: 0.00 | Avg RT: 0.00");

        add(createTopControlsPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);

        resultsPanel = createResultsPanel();
        resultsPanel.setVisible(false);
        add(resultsPanel, BorderLayout.SOUTH);

        preloadSampleRows();
    }

    /**
     * Creates top section with controls and action buttons.
     *
     * @return controls panel
     */
    private JPanel createTopControlsPanel() {
        JPanel controlsPanel = new JPanel(new BorderLayout());
        JPanel inputRowPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        JPanel configRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel actionsRow = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JLabel quantumLabel = new JLabel("Time Quantum:");
        configRow.add(quantumLabel);
        configRow.add(quantumSpinner);

        ButtonGroup priorityRuleGroup = new ButtonGroup();
        priorityRuleGroup.add(lowerNumberHigherPriorityOption);
        priorityRuleGroup.add(higherNumberHigherPriorityOption);
        configRow.add(new JLabel("Priority Rule:"));
        configRow.add(lowerNumberHigherPriorityOption);
        configRow.add(higherNumberHigherPriorityOption);

        configRow.add(new JLabel("Preset:"));
        scenarioComboBox.setPreferredSize(new Dimension(240, 26));
        configRow.add(scenarioComboBox);

        scenarioComboBox.addActionListener(event -> updateInputControlsState());
        loadScenarioButton.addActionListener(event -> loadSelectedScenario());
        configRow.add(loadScenarioButton);

        addProcessButton.addActionListener(event -> processTableModel.addRow());
        actionsRow.add(addProcessButton);

        quickPasteButton.setToolTipText("Paste multiple rows: ID,Arrival,Burst,Priority or Arrival,Burst,Priority");
        quickPasteButton.addActionListener(event -> openQuickPasteDialog());
        actionsRow.add(quickPasteButton);

        deleteItemButton.addActionListener(event -> removeSelectedTableRow());
        actionsRow.add(deleteItemButton);

        clearAllButton.addActionListener(event -> processTableModel.clearAll());
        actionsRow.add(clearAllButton);

        runSimulationButton.setFont(runSimulationButton.getFont().deriveFont(Font.BOLD, 14f));
        runSimulationButton.setBackground(RUN_BUTTON_COLOR);
        runSimulationButton.setForeground(Color.WHITE);
        runSimulationButton.addActionListener(event -> runSimulation());
        actionsRow.add(runSimulationButton);

        resetButton.addActionListener(event -> resetToDefaultState());
        actionsRow.add(resetButton);

        inputRowPanel.add(configRow);
        inputRowPanel.add(actionsRow);

        errorLabel.setForeground(Color.RED);
        errorLabel.setVisible(false);

        controlsPanel.add(inputRowPanel, BorderLayout.NORTH);
        controlsPanel.add(errorLabel, BorderLayout.SOUTH);
        controlsPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        return controlsPanel;
    }

    /**
     * Creates middle section that holds editable process table.
     *
     * @return table wrapper panel
     */
    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        processTable.setPreferredScrollableViewportSize(new Dimension(800, 180));
        processTable.setRowHeight(24);
        processTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        JScrollPane tableScrollPane = new JScrollPane(processTable);
        tablePanel.add(tableScrollPane, BorderLayout.CENTER);
        tablePanel.setBorder(BorderFactory.createTitledBorder("Process Input Table"));
        return tablePanel;
    }

    /**
     * Creates bottom results area with RR and Priority side by side.
     *
     * @return results panel
     */
    private JPanel createResultsPanel() {
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        JPanel leftPanel = createAlgorithmResultPanel(
                roundRobinTitleLabel, roundRobinGanttPanel, roundRobinQueueLabel, roundRobinMetricsPanel, roundRobinFooterLabel);
        JPanel rightPanel = createAlgorithmResultPanel(
                new JLabel("Preemptive Priority Scheduling"),
                priorityGanttPanel, priorityQueueLabel, priorityMetricsPanel, priorityFooterLabel);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerLocation(0.5);
        wrapperPanel.add(splitPane, BorderLayout.CENTER);
        wrapperPanel.setPreferredSize(new Dimension(1000, 360));
        return wrapperPanel;
    }

    /**
     * Creates one side of the result split view.
     *
     * @param titleLabel section title
     * @param ganttChartPanel chart panel for this algorithm
     * @param metricsTablePanel metrics table panel for this algorithm
     * @param footerLabel averages footer label
     * @return assembled algorithm result panel
     */
    private JPanel createAlgorithmResultPanel(
            JLabel titleLabel,
            GanttChartPanel ganttChartPanel,
            JLabel queueLabel,
            MetricsTablePanel metricsTablePanel,
            JLabel footerLabel
    ) {
        JPanel algorithmPanel = new JPanel(new BorderLayout(6, 6));
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));

        JScrollPane ganttScrollPane = new JScrollPane(ganttChartPanel);
        ganttScrollPane.setPreferredSize(new Dimension(420, 110));
        queueLabel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

        JPanel contentPanel = new JPanel(new BorderLayout(6, 6));
        JPanel queueAndMetricsPanel = new JPanel(new BorderLayout(4, 4));
        queueAndMetricsPanel.add(queueLabel, BorderLayout.NORTH);
        queueAndMetricsPanel.add(metricsTablePanel, BorderLayout.CENTER);
        contentPanel.add(ganttScrollPane, BorderLayout.NORTH);
        contentPanel.add(queueAndMetricsPanel, BorderLayout.CENTER);

        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footerPanel.add(footerLabel);

        algorithmPanel.add(titleLabel, BorderLayout.NORTH);
        algorithmPanel.add(contentPanel, BorderLayout.CENTER);
        algorithmPanel.add(footerPanel, BorderLayout.SOUTH);
        algorithmPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        return algorithmPanel;
    }

    /**
     * Removes selected row from process table.
     */
    private void removeSelectedTableRow() {
        int selectedRow = findSelectedRowFromTable();
        if (selectedRow >= 0) {
            processTableModel.removeRow(selectedRow);
        } else {
            JOptionPane.showMessageDialog(this, "Please select a row to remove.");
        }
    }

    /**
     * Finds selected row from visible process table.
     *
     * @return selected row index or -1
     */
    private int findSelectedRowFromTable() {
        return processTable.getSelectedRow();
    }

    /**
     * Runs both schedulers and updates all result views.
     */
    private void runSimulation() {
        List<Process> inputProcesses = processTableModel.getProcessList();
        int timeQuantum = (Integer) quantumSpinner.getValue();
        boolean lowerNumberMeansHigherPriority = lowerNumberHigherPriorityOption.isSelected();

        InputValidator inputValidator = new InputValidator();
        List<String> errors = inputValidator.validate(inputProcesses, timeQuantum);
        if (!errors.isEmpty()) {
            showValidationErrors(errors);
            return;
        }

        clearValidationError();

        RoundRobinScheduler roundRobinScheduler = new RoundRobinScheduler();
        PriorityScheduler priorityScheduler = new PriorityScheduler();

        ScheduleResult roundRobinResult = roundRobinScheduler.simulate(
                inputProcesses, timeQuantum, lowerNumberMeansHigherPriority);
        ScheduleResult priorityResult = priorityScheduler.simulate(
                inputProcesses, timeQuantum, lowerNumberMeansHigherPriority);

        updateResultsViews(inputProcesses, timeQuantum, roundRobinResult, priorityResult);
    }

    /**
     * Displays input validation errors in top red label.
     *
     * @param errors list of human-readable error messages
     */
    private void showValidationErrors(List<String> errors) {
        StringBuilder messageBuilder = new StringBuilder("<html>");
        for (String error : errors) {
            messageBuilder.append("• ").append(error).append("<br>");
        }
        messageBuilder.append("</html>");
        errorLabel.setText(messageBuilder.toString());
        errorLabel.setVisible(true);
    }

    /**
     * Hides and clears validation error label.
     */
    private void clearValidationError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
    }

    /**
     * Updates all result components after successful simulation.
     *
     * @param processes source process list
     * @param quantum quantum used for Round Robin
     * @param roundRobinResult RR result
     * @param priorityResult priority result
     */
    private void updateResultsViews(
            List<Process> processes,
            int quantum,
            ScheduleResult roundRobinResult,
            ScheduleResult priorityResult
    ) {
        roundRobinTitleLabel.setText("Round Robin | Quantum = " + quantum);

        roundRobinGanttPanel.setGanttEntries(roundRobinResult.getGanttChart());
        priorityGanttPanel.setGanttEntries(priorityResult.getGanttChart());
        roundRobinQueueLabel.setText(buildExecutionQueueText(roundRobinResult.getGanttChart()));
        priorityQueueLabel.setText(buildExecutionQueueText(priorityResult.getGanttChart()));

        roundRobinMetricsPanel.displayResults(processes, roundRobinResult);
        priorityMetricsPanel.displayResults(processes, priorityResult);

        roundRobinFooterLabel.setText(buildFooterText(roundRobinResult));
        priorityFooterLabel.setText(buildFooterText(priorityResult));

        comparisonPanel.updateResults(roundRobinResult, priorityResult, processes);
        String selectedScenario = (String) scenarioComboBox.getSelectedItem();
        comparisonPanel.updateContext(
                selectedScenario == null ? SCENARIO_CUSTOM : selectedScenario,
                quantum,
                lowerNumberHigherPriorityOption.isSelected()
        );

        resultsPanel.setVisible(true);
        revalidate();
        repaint();

        if (onResultsReadyAction != null) {
            onResultsReadyAction.run();
        }
    }

    /**
     * Builds footer text for one result set.
     *
     * @param result result to summarize
     * @return formatted averages string
     */
    private String buildFooterText(ScheduleResult result) {
        return "Avg WT: " + String.format("%.2f", result.getAvgWaitingTime())
                + " | Avg TAT: " + String.format("%.2f", result.getAvgTurnaroundTime())
                + " | Avg RT: " + String.format("%.2f", result.getAvgResponseTime());
    }

    /**
     * Builds a compact execution queue text from Gantt entries.
     *
     * @param ganttEntries timeline entries
     * @return queue text in Pn arrow format
     */
    private String buildExecutionQueueText(List<GanttEntry> ganttEntries) {
        List<String> tokens = new ArrayList<>();
        for (GanttEntry entry : ganttEntries) {
            if (entry.getProcessId() == -1) {
                continue;
            }
            String token = "P" + entry.getProcessId();
            if (tokens.isEmpty() || !tokens.get(tokens.size() - 1).equals(token)) {
                tokens.add(token);
            }
        }
        if (tokens.isEmpty()) {
            return "Queue: -";
        }
        return "Queue: " + String.join(" -> ", tokens);
    }

    /**
     * Adds default sample processes on startup.
     */
    private void preloadSampleRows() {
        processTableModel.addRow(1, 0, 5, 3);
        processTableModel.addRow(2, 1, 3, 1);
        processTableModel.addRow(3, 2, 8, 2);
        processTableModel.addRow(4, 3, 2, 4);
        updateInputControlsState();
    }

    /**
     * Loads selected preset scenario into process table and quantum input.
     */
    private void loadSelectedScenario() {
        String selectedScenario = (String) scenarioComboBox.getSelectedItem();
        if (SCENARIO_CUSTOM.equals(selectedScenario)) {
            return;
        }

        ScenarioPreset scenarioPreset = buildScenarioPreset(selectedScenario);
        if (scenarioPreset == null) {
            return;
        }

        processTableModel.clearAll();
        for (Integer[] row : scenarioPreset.rows) {
            processTableModel.addRow(row[0], row[1], row[2], row[3]);
        }
        quantumSpinner.setValue(scenarioPreset.quantum);
        clearValidationError();
    }

    /**
     * Enables manual input controls only for custom mode.
     */
    private void updateInputControlsState() {
        boolean isCustomMode = SCENARIO_CUSTOM.equals(scenarioComboBox.getSelectedItem());
        processTable.setEnabled(isCustomMode);
        addProcessButton.setEnabled(isCustomMode);
        quickPasteButton.setEnabled(isCustomMode);
        deleteItemButton.setEnabled(isCustomMode);
        clearAllButton.setEnabled(isCustomMode);
        lowerNumberHigherPriorityOption.setEnabled(isCustomMode);
        higherNumberHigherPriorityOption.setEnabled(isCustomMode);
        quantumSpinner.setEnabled(isCustomMode);
    }

    /**
     * Resets full simulation UI state to default custom setup.
     */
    private void resetToDefaultState() {
        scenarioComboBox.setSelectedItem(SCENARIO_CUSTOM);
        processTableModel.clearAll();
        preloadSampleRows();
        quantumSpinner.setValue(DEFAULT_QUANTUM);
        lowerNumberHigherPriorityOption.setSelected(true);
        clearValidationError();
        resultsPanel.setVisible(false);
        revalidate();
        repaint();
    }

    /**
     * Creates preset scenario data from selected scenario label.
     *
     * @param selectedScenario selected scenario name
     * @return scenario preset object or null
     */
    private ScenarioPreset buildScenarioPreset(String selectedScenario) {
        ScenarioPreset scenarioPreset = new ScenarioPreset();

        if (SCENARIO_A.equals(selectedScenario)) {
            scenarioPreset.quantum = 3;
            scenarioPreset.rows.add(new Integer[]{1, 0, 7, 3});
            scenarioPreset.rows.add(new Integer[]{2, 2, 4, 1});
            scenarioPreset.rows.add(new Integer[]{3, 4, 1, 4});
            scenarioPreset.rows.add(new Integer[]{4, 5, 4, 2});
            scenarioPreset.rows.add(new Integer[]{5, 6, 3, 5});
            return scenarioPreset;
        }

        if (SCENARIO_B.equals(selectedScenario)) {
            scenarioPreset.quantum = 2;
            scenarioPreset.rows.add(new Integer[]{1, 0, 9, 4});
            scenarioPreset.rows.add(new Integer[]{2, 1, 3, 1});
            scenarioPreset.rows.add(new Integer[]{3, 2, 5, 3});
            scenarioPreset.rows.add(new Integer[]{4, 3, 2, 2});
            scenarioPreset.rows.add(new Integer[]{5, 4, 4, 5});
            return scenarioPreset;
        }

        if (SCENARIO_C.equals(selectedScenario)) {
            scenarioPreset.quantum = 2;
            scenarioPreset.rows.add(new Integer[]{1, 0, 4, 5});
            scenarioPreset.rows.add(new Integer[]{2, 0, 4, 1});
            scenarioPreset.rows.add(new Integer[]{3, 0, 4, 3});
            scenarioPreset.rows.add(new Integer[]{4, 0, 4, 2});
            scenarioPreset.rows.add(new Integer[]{5, 0, 4, 4});
            return scenarioPreset;
        }

        if (SCENARIO_D.equals(selectedScenario)) {
            scenarioPreset.quantum = 2;
            scenarioPreset.rows.add(new Integer[]{1, 0, 25, 10});
            scenarioPreset.rows.add(new Integer[]{2, 1, 2, 1});
            scenarioPreset.rows.add(new Integer[]{3, 2, 2, 1});
            scenarioPreset.rows.add(new Integer[]{4, 3, 2, 1});
            scenarioPreset.rows.add(new Integer[]{5, 4, 2, 1});
            scenarioPreset.rows.add(new Integer[]{6, 5, 2, 1});
            scenarioPreset.rows.add(new Integer[]{7, 6, 2, 1});
            scenarioPreset.rows.add(new Integer[]{8, 7, 2, 1});
            return scenarioPreset;
        }

        if (SCENARIO_E.equals(selectedScenario)) {
            // Intentionally invalid rows to demonstrate validation behavior.
            scenarioPreset.quantum = 2;
            scenarioPreset.rows.add(new Integer[]{1, 0, 4, 1});
            scenarioPreset.rows.add(new Integer[]{1, -1, 0, 0});
            return scenarioPreset;
        }

        return null;
    }

    /**
     * Container for preset scenario values.
     */
    private static class ScenarioPreset {
        private final java.util.List<Integer[]> rows = new java.util.ArrayList<>();
        private int quantum;
    }

    /**
     * Opens a quick paste dialog for bulk process input.
     */
    private void openQuickPasteDialog() {
        JTextArea textArea = new JTextArea(10, 42);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setText(
                "# One process per line\n"
                + "# Formats:\n"
                + "#   ID,Arrival,Burst,Priority\n"
                + "#   Arrival,Burst,Priority (ID will be auto-generated)\n");

        JScrollPane scrollPane = new JScrollPane(textArea);
        int result = JOptionPane.showConfirmDialog(
                this,
                scrollPane,
                "Quick Paste Processes",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String[] lines = textArea.getText().split("\\r?\\n");
        int addedCount = 0;
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }

            String[] parts = trimmed.split("[,\\s]+");
            try {
                if (parts.length == 4) {
                    int processId = Integer.parseInt(parts[0]);
                    int arrival = Integer.parseInt(parts[1]);
                    int burst = Integer.parseInt(parts[2]);
                    int priority = Integer.parseInt(parts[3]);
                    processTableModel.addRow(processId, arrival, burst, priority);
                    addedCount++;
                } else if (parts.length == 3) {
                    int arrival = Integer.parseInt(parts[0]);
                    int burst = Integer.parseInt(parts[1]);
                    int priority = Integer.parseInt(parts[2]);
                    processTableModel.addRow();
                    int lastRow = processTableModel.getRowCount() - 1;
                    processTableModel.setValueAt(arrival, lastRow, 1);
                    processTableModel.setValueAt(burst, lastRow, 2);
                    processTableModel.setValueAt(priority, lastRow, 3);
                    addedCount++;
                } else {
                    JOptionPane.showMessageDialog(
                            this,
                            "Invalid line format:\n" + line
                            + "\n\nUse either 4 values (ID,Arrival,Burst,Priority)"
                            + " or 3 values (Arrival,Burst,Priority).",
                            "Quick Paste Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }
            } catch (NumberFormatException exception) {
                JOptionPane.showMessageDialog(
                        this,
                        "Only integers are allowed.\nInvalid line:\n" + line,
                        "Quick Paste Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }
        }

        if (addedCount > 0) {
            clearValidationError();
            JOptionPane.showMessageDialog(
                    this,
                    "Added " + addedCount + " process row(s) successfully.",
                    "Quick Paste",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }
}
