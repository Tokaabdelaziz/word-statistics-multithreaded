package integration;

import aggregation.DirectoryStatsAggregator;
import aggregation.FileStatistics;
import aggregation.RealTimeUpdateModel;
import core.*;
import filesystem.DirectoryScanner;
import filesystem.ScanResult;
import gui.*;
import processing.WordStatistics;

import javax.swing.*;
import java.io.File;

public class AppLauncher {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();

            // GUI listener – updates the table row by row
            GuiUpdateListener guiListener = stats -> SwingUtilities.invokeLater(() -> {
                window.getResultsTable().addResultRow(new ResultsTable.ResultRow(
                        stats.fileName,
                        stats.wordCount,
                        stats.countIs,
                        stats.countAre,
                        stats.countYou,
                        stats.longestWord,
                        stats.shortestWord
                ));
            });

            DirectoryScanner scanner = new DirectoryScanner();
            ThreadManager threadManager = new ThreadManager();

            // Start button
            window.getOptionsPanel().addStartListener(() -> {
                String dir = window.getDirectoryChooserPanel().getDirectoryPath().trim();
                if (dir.isEmpty()) {
                    window.getOptionsPanel().setStatus("Please select a directory first");
                    return;
                }

                boolean subdirs = window.getDirectoryChooserPanel().isIncludeSubdirectoriesSelected();

                window.getOptionsPanel().setBusy(true);
                window.getOptionsPanel().setStatus("Scanning directory...");
                window.getResultsTable().clearRows();

                new Thread(() -> {
                    ScanResult scanResult = scanner.scanDirectory(dir, subdirs);

                    if (scanResult.getFileCount() == 0) {
                        SwingUtilities.invokeLater(() -> {
                            window.getOptionsPanel().setStatus("No .txt files found");
                            window.getOptionsPanel().setBusy(false);
                        });
                        return;
                    }

                    DirectoryStatsAggregator aggregator = new DirectoryStatsAggregator();

                    // Real-time directory longest/shortest word update
                    RealTimeUpdateModel.DirectoryStatsListener dirListener = stats ->
                            SwingUtilities.invokeLater(() ->
                                    window.getResultsTable().updateDirectorySummary(
                                            stats.getLongestWord(),
                                            stats.getShortestWord()
                                    ));
                    aggregator.getModel().addListener(dirListener);

                    // Convert WordStatistics → FileStatistics for the aggregator
                    AggregationListener aggListener = stats -> aggregator.processFile(new FileStatistics(
                            stats.fileName, stats.wordCount, stats.countIs,
                            stats.countAre, stats.countYou,
                            stats.longestWord, stats.shortestWord
                    ));

                    UpdateDispatcher dispatcher = new UpdateDispatcher(guiListener, aggListener);

                    // Start processing (this returns when everything is done)
                    threadManager.startProcessing(scanResult, guiListener, aggListener);

                    // Final export
                    String out = dir.endsWith(File.separator) ? dir : dir + File.separator;
                    aggregator.exportSummary(out + "summary.txt");
                    aggregator.exportJSON(out + "directory_statistics.json");
                    aggregator.exportCSV(out + "file_statistics.csv");

                    SwingUtilities.invokeLater(() -> {
                        window.getOptionsPanel().setBusy(false);
                        window.getOptionsPanel().setStatus("Finished – " + scanResult.getFileCount() + " files processed");
                    });
                }).start();
            });

            // Stop button
            window.getOptionsPanel().addStopListener(() -> {
                threadManager.shutdown();
                window.getOptionsPanel().setBusy(false);
                window.getOptionsPanel().setStatus("Stopped by user");
            });

            // Clear button
            window.getOptionsPanel().addClearListener(() -> {
                window.getResultsTable().clearRows();
                window.getOptionsPanel().reset();
            });

            window.setVisible(true);
        });
    }
}
