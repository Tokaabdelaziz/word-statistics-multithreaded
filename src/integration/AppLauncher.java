package integration;

import aggregation.DirectoryStatsAggregator;
import aggregation.FileStatistics;
import aggregation.RealTimeUpdateModel;
import core.*;
import filesystem.DirectoryScanner;
import filesystem.FileSystemMonitor;
import filesystem.ScanResult;
import gui.*;
import processing.FileProcessor;
import processing.WordStatistics;

import javax.swing.*;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class AppLauncher {

    private static FileSystemMonitor fileMonitor;
    private static DirectoryStatsAggregator aggregator;
    private static Map<String, FileStatistics> fileStatsMap = new HashMap<>();
    private static Set<String> existingFiles = new HashSet<>();
    private static ThreadManager threadManager;
    private static String resultsPath = "src/results/";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();

            DirectoryScanner scanner = new DirectoryScanner();
            threadManager = new ThreadManager();

            // Start button - now starts monitoring
            window.getOptionsPanel().addStartListener(() -> {
                String dir = window.getDirectoryChooserPanel().getDirectoryPath().trim();
                if (dir.isEmpty()) {
                    window.getOptionsPanel().setStatus("Please select a directory first");
                    return;
                }

                boolean subdirs = window.getDirectoryChooserPanel().isIncludeSubdirectoriesSelected();

                // Stop existing monitoring
                if (fileMonitor != null) {
                    fileMonitor.stopMonitoring();
                }

                fileStatsMap.clear();
                existingFiles.clear();
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

                    final int totalFiles = scanResult.getFileCount();
                    AtomicInteger processedFiles = new AtomicInteger(0);

                    aggregator = new DirectoryStatsAggregator();

                    // Real-time directory longest/shortest word update
                    RealTimeUpdateModel.DirectoryStatsListener dirListener = stats ->
                            SwingUtilities.invokeLater(() ->
                                    window.getResultsTable().updateDirectorySummary(
                                            stats.getLongestWord(),
                                            stats.getShortestWord()
                                    ));
                    aggregator.getModel().addListener(dirListener);

                    // GUI listener
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

                    // Aggregation listener with progress
                    AggregationListener aggListener = stats -> {
                        FileStatistics fs = new FileStatistics(
                                stats.fileName, stats.wordCount, stats.countIs,
                                stats.countAre, stats.countYou,
                                stats.longestWord, stats.shortestWord
                        );
                        fileStatsMap.put(stats.fileName, fs);
                        existingFiles.add(stats.fileName); // Track existing files
                        aggregator.processFile(fs);

                        int processed = processedFiles.incrementAndGet();
                        int progress = (int) ((processed / (double) totalFiles) * 100);
                        SwingUtilities.invokeLater(() -> {
                            window.getOptionsPanel().setProgress(progress);
                            window.getOptionsPanel().setStatus(
                                    String.format("Processing: %d/%d files", processed, totalFiles)
                            );
                        });
                    };

                    // Initial processing
                    threadManager.startProcessing(scanResult, guiListener, aggListener);

                    // Export initial results
                    File resultsDir = new File(resultsPath);
                    if (!resultsDir.exists()) {
                        resultsDir.mkdirs();
                    }
                    exportResults();

                    SwingUtilities.invokeLater(() -> {
                        window.getOptionsPanel().setBusy(false);
                        window.getOptionsPanel().setProgress(100);
                        window.getOptionsPanel().setStatus("Monitoring: " + totalFiles + " files (live updates enabled)");
                    });

                    // Start file system monitoring for live updates
                    fileMonitor = new FileSystemMonitor();
                    fileMonitor.startMonitoring(dir, subdirs, new FileSystemMonitor.FileChangeListener() {

                        private Set<String> processingFiles = new HashSet<>();

                        @Override
                        public void onFileCreated(File file) {
                            String fileKey = file.getAbsolutePath();
                            String displayName = file.getName();       // for messages only

                            // Prevent duplicate processing
                            synchronized (processingFiles) {
                                if (processingFiles.contains(fileKey)) {
                                    return;
                                }
                                processingFiles.add(fileKey);
                            }

                            // Small delay to avoid race conditions with MODIFY events
                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }

                            // Check if this file already exists (it's actually a modification)
                            if (existingFiles.contains(fileKey)) {
                                // This is actually a modification
                                SwingUtilities.invokeLater(() ->
                                        window.getOptionsPanel().setStatus("File modified: " + displayName)
                                );
                                processFileUpdate(file, window, true);
                            } else {
                                // This is a truly new file
                                SwingUtilities.invokeLater(() ->
                                        window.getOptionsPanel().setStatus("File created: " + displayName)
                                );
                                existingFiles.add(fileKey);
                                processFileUpdate(file, window, false);
                            }

                            // Remove from processing set after a delay
                            new Thread(() -> {
                                try {
                                    Thread.sleep(200);
                                    synchronized (processingFiles) {
                                        processingFiles.remove(fileKey);
                                    }
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                }
                            }).start();
                        }


                        @Override
                        public void onFileModified(File file) {
                            String fileKey = file.getAbsolutePath();
                            String displayName = file.getName();       // for messages only

                            // If file is currently being processed by CREATE, skip this MODIFY event
                            synchronized (processingFiles) {
                                if (processingFiles.contains(fileKey)) {
                                    return;
                                }
                            }

                            SwingUtilities.invokeLater(() ->
                                    window.getOptionsPanel().setStatus("File modified: " + displayName)
                            );
                            processFileUpdate(file, window, true);
                        }


                        @Override
                        public void onFileDeleted(File file) {
                            String fileKey = file.getAbsolutePath();
                            String displayName = file.getName();       // for messages only

                            SwingUtilities.invokeLater(() -> {
                                window.getOptionsPanel().setStatus("File deleted: " + displayName);
                                window.getResultsTable().removeRow(fileKey);   // ✅ use full path
                            });

                            existingFiles.remove(fileKey);
                            fileStatsMap.remove(fileKey);
                            rebuildAggregator(window);
                            exportResults();
                        }

                    });

                }).start();
            });

            // Stop button
            window.getOptionsPanel().addStopListener(() -> {
                if (fileMonitor != null) {
                    fileMonitor.stopMonitoring();
                }
                threadManager.shutdown();
                window.getOptionsPanel().setBusy(false);
                window.getOptionsPanel().setStatus("Stopped");
            });

            // Clear button
            window.getOptionsPanel().addClearListener(() -> {
                window.getResultsTable().clearRows();
                window.getOptionsPanel().reset();
            });

            window.setVisible(true);
        });
    }

    private static void processFileUpdate(File file, MainWindow window, boolean isModification) {
        try {
            FileProcessor processor = new FileProcessor();
            WordStatistics wordStats = processor.process(file.toPath());

            // ✅ Use full path as identity (consistent with FileProcessingTask)
            wordStats.fileName = file.getAbsolutePath();

            FileStatistics fileStats = new FileStatistics(
                    wordStats.fileName,
                    wordStats.wordCount,
                    wordStats.countIs,
                    wordStats.countAre,
                    wordStats.countYou,
                    wordStats.longestWord,
                    wordStats.shortestWord
            );

            // Remove old stats if modification
            if (isModification) {
                fileStatsMap.remove(wordStats.fileName);
            }

            fileStatsMap.put(wordStats.fileName, fileStats);

            // Rebuild aggregator to recalculate totals
            rebuildAggregator(window);

            SwingUtilities.invokeLater(() -> {
                window.getResultsTable().addOrUpdateRow(new ResultsTable.ResultRow(
                        wordStats.fileName,
                        wordStats.wordCount,
                        wordStats.countIs,
                        wordStats.countAre,
                        wordStats.countYou,
                        wordStats.longestWord,
                        wordStats.shortestWord
                ));
            });

            exportResults();
        } catch (Exception e) {
            System.err.println("Error processing file: " + e.getMessage());
        }
    }


    private static void rebuildAggregator(MainWindow window) {
        aggregator = new DirectoryStatsAggregator();

        RealTimeUpdateModel.DirectoryStatsListener dirListener = stats ->
                SwingUtilities.invokeLater(() ->
                        window.getResultsTable().updateDirectorySummary(
                                stats.getLongestWord(),
                                stats.getShortestWord()
                        ));
        aggregator.getModel().addListener(dirListener);

        // Re-process all files in memory
        for (FileStatistics fs : fileStatsMap.values()) {
            aggregator.processFile(fs);
        }
    }

    private static void exportResults() {
        new Thread(() -> {
            try {
                aggregator.exportSummary(resultsPath + "summary.txt");
                aggregator.exportJSON(resultsPath + "directory_statistics.json");
                aggregator.exportCSV(resultsPath + "file_statistics.csv");
            } catch (Exception e) {
                System.err.println("Error exporting: " + e.getMessage());
            }
        }).start();
    }
}