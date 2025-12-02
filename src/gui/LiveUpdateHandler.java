package gui;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Cosmetic helper that animates the UI with placeholder updates.
 */
public class LiveUpdateHandler {

    private static final List<ResultsTable.ResultRow> SAMPLE_ROWS = List.of(
            new ResultsTable.ResultRow(
                    "chapter1.txt", 1180, 42, 13, 9, "synchronization", "a"),
            new ResultsTable.ResultRow(
                    "chapter2.txt", 1324, 48, 15, 12, "multithreading", "I"),
            new ResultsTable.ResultRow(
                    "notes.txt", 305, 5, 2, 1, "responsiveness", "be"),
            new ResultsTable.ResultRow(
                    "summary.txt", 210, 9, 4, 3, "observability", "OS")
    );

    private final ResultsTable resultsTable;
    private final OptionsPanel optionsPanel;

    private Timer timer;

    public LiveUpdateHandler(ResultsTable resultsTable, OptionsPanel optionsPanel) {
        this.resultsTable = resultsTable;
        this.optionsPanel = optionsPanel;
    }

    public void startDemo(String directory, boolean includeSubdirectories) {
        stopDemo("Preparing preview…");
        resultsTable.clearRows();

        String directoryLabel = directory.isBlank() ? "(no directory selected)" : directory;
        String statusPrefix = includeSubdirectories
                ? "Scanning with subdirectories: "
                : "Scanning single level: ";

        optionsPanel.setBusy(true);
        optionsPanel.setStatus(statusPrefix + directoryLabel);
        optionsPanel.setProgress(0);

        Iterator<ResultsTable.ResultRow> iterator = SAMPLE_ROWS.iterator();
        AtomicInteger progress = new AtomicInteger();

        timer = new Timer(900, e -> {
            if (!iterator.hasNext()) {
                stopDemo("Preview complete");
                return;
            }

            ResultsTable.ResultRow row = iterator.next();
            resultsTable.addResultRow(row);
            resultsTable.updateDirectorySummary(row.longestWord(), row.shortestWord());

            int pct = (int) ((progress.incrementAndGet() / (double) SAMPLE_ROWS.size()) * 100);
            optionsPanel.setProgress(pct);
            optionsPanel.setStatus("Processing " + row.fileName());
        });

        timer.start();
    }

    public void stopDemo(String message) {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
        SwingUtilities.invokeLater(() -> {
            optionsPanel.setBusy(false);
            if (message != null) {
                optionsPanel.setStatus(message);
            }
        });
    }
}
