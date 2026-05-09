package scheduler.gui;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import scheduler.model.Process;

/**
 * This class manages the data in the process input table.
 * It connects the List of processes to the JTable display.
 */
public class ProcessTableModel extends AbstractTableModel {

    private static final String[] COLUMN_NAMES = {
        "Process ID", "Arrival Time", "Burst Time", "Priority"
    };

    private final List<Integer[]> tableRows;

    /**
     * Creates an empty process table model.
     */
    public ProcessTableModel() {
        this.tableRows = new ArrayList<>();
    }

    /**
     * Returns number of rows.
     *
     * @return row count
     */
    @Override
    public int getRowCount() {
        return tableRows.size();
    }

    /**
     * Returns number of columns.
     *
     * @return column count
     */
    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    /**
     * Returns display name for a column.
     *
     * @param column column index
     * @return column header text
     */
    @Override
    public String getColumnName(int column) {
        return COLUMN_NAMES[column];
    }

    /**
     * Returns value at one table cell.
     *
     * @param rowIndex row index
     * @param columnIndex column index
     * @return cell value
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return tableRows.get(rowIndex)[columnIndex];
    }

    /**
     * Returns true because all cells are editable.
     *
     * @param rowIndex row index
     * @param columnIndex column index
     * @return always true
     */
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    /**
     * Updates one cell value. If parsing fails, old value is kept.
     *
     * @param newValue value from editor
     * @param rowIndex row index
     * @param columnIndex column index
     */
    @Override
    public void setValueAt(Object newValue, int rowIndex, int columnIndex) {
        Integer[] rowData = tableRows.get(rowIndex);
        int oldValue = rowData[columnIndex];
        int parsedValue = parseIntegerOrFallback(newValue, oldValue);
        rowData[columnIndex] = parsedValue;
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    /**
     * Returns Integer class for all columns.
     *
     * @param columnIndex column index
     * @return Integer.class
     */
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return Integer.class;
    }

    /**
     * Adds one row with default values.
     */
    public void addRow() {
        int nextProcessId = findNextProcessId();
        tableRows.add(new Integer[]{nextProcessId, 0, 1, 1});
        int addedRowIndex = tableRows.size() - 1;
        fireTableRowsInserted(addedRowIndex, addedRowIndex);
    }

    /**
     * Adds one row with given values.
     *
     * @param processId process identifier
     * @param arrivalTime process arrival time
     * @param burstTime process burst time
     * @param priority process priority value
     */
    public void addRow(int processId, int arrivalTime, int burstTime, int priority) {
        tableRows.add(new Integer[]{processId, arrivalTime, burstTime, priority});
        int addedRowIndex = tableRows.size() - 1;
        fireTableRowsInserted(addedRowIndex, addedRowIndex);
    }

    /**
     * Removes one row by index.
     *
     * @param rowIndex selected row index
     */
    public void removeRow(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < tableRows.size()) {
            tableRows.remove(rowIndex);
            fireTableRowsDeleted(rowIndex, rowIndex);
        }
    }

    /**
     * Removes all rows from table model.
     */
    public void clearAll() {
        tableRows.clear();
        fireTableDataChanged();
    }

    /**
     * Converts table rows into Process objects.
     *
     * @return list of processes from table data
     */
    public List<Process> getProcessList() {
        List<Process> processes = new ArrayList<>();
        for (Integer[] rowData : tableRows) {
            processes.add(new Process(rowData[0], rowData[1], rowData[2], rowData[3]));
        }
        return processes;
    }

    /**
     * Parses integer safely, falling back to old value if invalid.
     *
     * @param candidateValue value to parse
     * @param fallbackValue old value to keep when parsing fails
     * @return parsed value or fallback value
     */
    private int parseIntegerOrFallback(Object candidateValue, int fallbackValue) {
        if (candidateValue == null) {
            return fallbackValue;
        }

        try {
            return Integer.parseInt(candidateValue.toString().trim());
        } catch (NumberFormatException exception) {
            return fallbackValue;
        }
    }

    /**
     * Finds next process ID as max existing ID + 1.
     *
     * @return next suggested process ID
     */
    private int findNextProcessId() {
        int maxId = 0;
        for (Integer[] rowData : tableRows) {
            if (rowData[0] != null && rowData[0] > maxId) {
                maxId = rowData[0];
            }
        }
        return maxId + 1;
    }
}
