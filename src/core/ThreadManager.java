package core;

import filesystem.ScanResult;
import processing.WordStatistics;
import processing.tasks.FileProcessingTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ThreadManager {

    private final int numberOfThreads;
    private final ExecutorService executorService;
    private final TaskDistributor taskDistributor;

    public ThreadManager() {
        int cores = Runtime.getRuntime().availableProcessors();
        this.numberOfThreads = Math.max(1, cores);
        this.executorService = Executors.newFixedThreadPool(numberOfThreads);
        this.taskDistributor = new TaskDistributor();
    }

    public void startProcessing(ScanResult scanResult,
                                GuiUpdateListener guiListener,
                                AggregationListener aggregationListener) {

        if (scanResult == null) {
            System.out.println("ThreadManager: scanResult is null. Nothing to process.");
            return;
        }

        List<String> filePaths = scanResult.getFilePaths();
        int fileCount = scanResult.getFileCount();
        boolean searchedSubdirs = scanResult.includedSubfolders();

        if (filePaths == null || filePaths.isEmpty()) {
            System.out.println("ThreadManager: No files to process.");
            return;
        }

        System.out.println("ThreadManager: processing " + fileCount +
                " files (searchedSubdirs=" + searchedSubdirs + "), using " +
                numberOfThreads + " threads.");

        // ✅ Create dispatcher once; it knows how to talk to GUI + Aggregation
        UpdateDispatcher dispatcher = new UpdateDispatcher(guiListener, aggregationListener);

        // ✅ Use TaskDistributor to split files across workers
        List<List<String>> chunks = taskDistributor.distribute(filePaths, numberOfThreads);

        // Use CompletionService so we can get results as soon as each task finishes
        CompletionService<WordStatistics> completionService =
                new ExecutorCompletionService<>(executorService);

        List<Future<WordStatistics>> futures = new ArrayList<>();

        // For each chunk (logical worker), submit one FileProcessingTask per file
        for (List<String> chunk : chunks) {
            for (String path : chunk) {
                File file = new File(path);
                if (!file.exists() || !file.isFile()) {
                    System.out.println("ThreadManager: skipping invalid file path: " + path);
                    continue;
                }

                FileProcessingTask task = new FileProcessingTask(file);
                Future<WordStatistics> future = completionService.submit(task);
                futures.add(future);
            }
        }

        int taskCount = futures.size();

        if (taskCount == 0) {
            System.out.println("ThreadManager: no valid files to process after filtering.");
            return;
        }

        // Receive results in the order they complete
        for (int i = 0; i < taskCount; i++) {
            try {
                Future<WordStatistics> finishedFuture = completionService.take();
                WordStatistics stats = finishedFuture.get();

                // ✅ Delegate to UpdateDispatcher (it calls GUI + Aggregation)
                dispatcher.dispatch(stats);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("ThreadManager: interrupted while waiting for tasks.");
            } catch (ExecutionException e) {
                System.err.println("ThreadManager: error while processing a file: " + e.getCause());
            }
        }
    }

    public void shutdown() {
        executorService.shutdown();
    }

    public int getNumberOfThreads() {
        return numberOfThreads;
    }
}
