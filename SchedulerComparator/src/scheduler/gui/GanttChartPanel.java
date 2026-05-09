package scheduler.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import scheduler.model.GanttEntry;

/**
 * This panel draws the Gantt chart for one algorithm.
 * Each execution block is a colored rectangle. Time markers appear below.
 */
public class GanttChartPanel extends JPanel {

    private static final int BLOCK_HEIGHT = 50;
    private static final int TIME_MARKER_AREA_HEIGHT = 20;
    private static final int TOP_PADDING = 10;
    private static final int LEFT_PADDING = 10;
    private static final int MIN_BLOCK_WIDTH = 50;
    private static final int OUTLINE_THICKNESS = 1;

    private static final Color[] PROCESS_COLORS = {
        new Color(66, 133, 244),
        new Color(219, 68, 55),
        new Color(244, 180, 0),
        new Color(15, 157, 88),
        new Color(171, 71, 188),
        new Color(0, 172, 193),
        new Color(255, 112, 67),
        new Color(124, 179, 66),
        new Color(57, 73, 171),
        new Color(141, 110, 99)
    };

    private List<GanttEntry> ganttEntries;

    /**
     * Creates an empty Gantt chart panel.
     */
    public GanttChartPanel() {
        this.ganttEntries = new ArrayList<>();
        setBackground(Color.WHITE);
    }

    /**
     * Updates chart data and triggers redraw.
     *
     * @param ganttEntries new timeline entries
     */
    public void setGanttEntries(List<GanttEntry> ganttEntries) {
        this.ganttEntries = ganttEntries == null ? new ArrayList<>() : ganttEntries;
        updatePreferredSize();
        revalidate();
        repaint();
    }

    /**
     * Paints all chart elements.
     *
     * @param graphics drawing context
     */
    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (ganttEntries == null || ganttEntries.isEmpty()) {
            return;
        }
        drawAllBlocks(graphics);
    }

    /**
     * Loops through chart entries and draws each block.
     *
     * @param graphics drawing context
     */
    private void drawAllBlocks(Graphics graphics) {
        int blockWidth = calculateBlockWidth();
        int xPosition = LEFT_PADDING;

        for (int entryIndex = 0; entryIndex < ganttEntries.size(); entryIndex++) {
            GanttEntry entry = ganttEntries.get(entryIndex);
            drawSingleBlock(graphics, entry, xPosition, blockWidth);
            drawTimeMarkers(graphics, entry, xPosition, blockWidth, entryIndex == ganttEntries.size() - 1);
            xPosition += blockWidth;
        }
    }

    /**
     * Draws one process or idle block.
     *
     * @param graphics drawing context
     * @param entry one gantt entry
     * @param xPosition left pixel position of block
     * @param blockWidth width in pixels
     */
    private void drawSingleBlock(Graphics graphics, GanttEntry entry, int xPosition, int blockWidth) {
        Color fillColor = getBlockColor(entry.getProcessId());
        String labelText = entry.getProcessId() == -1 ? "idle" : "P" + entry.getProcessId();

        graphics.setColor(fillColor);
        graphics.fillRect(xPosition, TOP_PADDING, blockWidth, BLOCK_HEIGHT);

        graphics.setColor(Color.BLACK);
        graphics.drawRect(xPosition, TOP_PADDING, blockWidth - OUTLINE_THICKNESS, BLOCK_HEIGHT);

        drawCenteredLabel(graphics, labelText, xPosition, blockWidth);
    }

    /**
     * Draws time labels below each block.
     *
     * @param graphics drawing context
     * @param entry gantt entry
     * @param xPosition left position of block
     * @param blockWidth width of block
     * @param isLastBlock true when this is the rightmost block
     */
    private void drawTimeMarkers(
            Graphics graphics,
            GanttEntry entry,
            int xPosition,
            int blockWidth,
            boolean isLastBlock
    ) {
        int markerBaseline = TOP_PADDING + BLOCK_HEIGHT + TIME_MARKER_AREA_HEIGHT - 3;
        graphics.setColor(Color.DARK_GRAY);
        graphics.drawString(String.valueOf(entry.getStartTime()), xPosition, markerBaseline);

        if (isLastBlock) {
            int endMarkerX = xPosition + blockWidth - 10;
            graphics.drawString(String.valueOf(entry.getEndTime()), endMarkerX, markerBaseline);
        }
    }

    /**
     * Chooses color for one process ID or idle state.
     *
     * @param processId process identifier
     * @return fill color for block
     */
    private Color getBlockColor(int processId) {
        if (processId == -1) {
            return Color.LIGHT_GRAY;
        }
        int colorIndex = Math.floorMod(processId, PROCESS_COLORS.length);
        return PROCESS_COLORS[colorIndex];
    }

    /**
     * Draws centered text label inside one block.
     *
     * @param graphics drawing context
     * @param text text to draw
     * @param xPosition left position of block
     * @param blockWidth width of block
     */
    private void drawCenteredLabel(Graphics graphics, String text, int xPosition, int blockWidth) {
        FontMetrics fontMetrics = graphics.getFontMetrics();
        int textWidth = fontMetrics.stringWidth(text);
        int textX = xPosition + (blockWidth - textWidth) / 2;
        int textY = TOP_PADDING + (BLOCK_HEIGHT + fontMetrics.getAscent()) / 2 - 3;
        graphics.setColor("idle".equals(text) ? Color.DARK_GRAY : Color.BLACK);
        graphics.drawString(text, textX, textY);
    }

    /**
     * Calculates width of each block based on panel width.
     *
     * @return block width in pixels
     */
    private int calculateBlockWidth() {
        int availableWidth = Math.max(getWidth() - (LEFT_PADDING * 2), MIN_BLOCK_WIDTH);
        int fitBlockWidth = availableWidth / Math.max(ganttEntries.size(), 1);
        return Math.max(fitBlockWidth, MIN_BLOCK_WIDTH);
    }

    /**
     * Updates preferred size so parent scroll pane can scroll when needed.
     */
    private void updatePreferredSize() {
        int blockWidth = calculateBlockWidth();
        int preferredWidth = LEFT_PADDING * 2 + (blockWidth * Math.max(ganttEntries.size(), 1));
        int preferredHeight = TOP_PADDING + BLOCK_HEIGHT + TIME_MARKER_AREA_HEIGHT + 10;
        setPreferredSize(new Dimension(preferredWidth, preferredHeight));
    }
}
