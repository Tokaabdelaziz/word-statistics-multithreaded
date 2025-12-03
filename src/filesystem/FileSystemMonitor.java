package filesystem;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileSystemMonitor {

    private WatchService watchService;
    private ExecutorService executor;
    private volatile boolean running = false;
    private FileChangeListener listener;
    private Map<WatchKey, Path> watchKeys;
    private boolean includeSubdirectories;
    private Map<String, Long> lastEventTime = new HashMap<>();
    private static final long EVENT_DELAY_MS = 100; // Delay to filter duplicate events

    public FileSystemMonitor() {
        this.watchKeys = new HashMap<>();
    }

    public void startMonitoring(String directoryPath, boolean includeSubdirectories, FileChangeListener listener) {
        this.listener = listener;
        this.includeSubdirectories = includeSubdirectories;

        try {
            Path rootPath = Paths.get(directoryPath);
            this.watchService = FileSystems.getDefault().newWatchService();

            registerDirectory(rootPath);

            if (includeSubdirectories) {
                registerSubdirectories(rootPath);
            }

            running = true;
            executor = Executors.newSingleThreadExecutor();
            executor.submit(this::watchLoop);

        } catch (IOException e) {
            System.err.println("Failed to start monitoring: " + e.getMessage());
        }
    }

    private void registerDirectory(Path dir) throws IOException {
        WatchKey key = dir.register(watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_DELETE);
        watchKeys.put(key, dir);
    }

    private void registerSubdirectories(Path start) throws IOException {
        Files.walk(start, 2)
                .filter(Files::isDirectory)
                .filter(p -> !p.equals(start))
                .forEach(dir -> {
                    try {
                        registerDirectory(dir);
                    } catch (IOException e) {
                        System.err.println("Failed to register: " + dir);
                    }
                });
    }

    private void watchLoop() {
        while (running) {
            try {
                WatchKey key = watchService.take();
                Path dir = watchKeys.get(key);

                if (dir == null) {
                    key.reset();
                    continue;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }

                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path filename = ev.context();
                    Path fullPath = dir.resolve(filename);

                    if (!filename.toString().toLowerCase().endsWith(".txt")) {
                        continue;
                    }

                    // Filter duplicate events
                    String fileKey = fullPath.toString() + "-" + kind.name();
                    long currentTime = System.currentTimeMillis();
                    Long lastTime = lastEventTime.get(fileKey);

                    if (lastTime != null && (currentTime - lastTime) < EVENT_DELAY_MS) {
                        continue; // Skip duplicate event
                    }
                    lastEventTime.put(fileKey, currentTime);

                    if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                        // Check if file actually exists (not just a temp create event)
                        if (fullPath.toFile().exists()) {
                            listener.onFileCreated(fullPath.toFile());
                        }
                    } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                        if (fullPath.toFile().exists()) {
                            listener.onFileModified(fullPath.toFile());
                        }
                    } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                        listener.onFileDeleted(fullPath.toFile());
                    }
                }

                key.reset();

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void stopMonitoring() {
        running = false;
        if (executor != null) {
            executor.shutdownNow();
        }
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException e) {
                System.err.println("Error closing watch service: " + e.getMessage());
            }
        }
        watchKeys.clear();
        lastEventTime.clear();
    }

    public interface FileChangeListener {
        void onFileCreated(java.io.File file);
        void onFileModified(java.io.File file);
        void onFileDeleted(java.io.File file);
    }
}