package aggregation;

// Ensures thread-safe updates
public class StatsSynchronizer {
    private DirectoryStatistics directoryStats;

    public StatsSynchronizer() {
        directoryStats = new DirectoryStatistics();
    }

    public synchronized void update(FileStatistics fileStats) {
        directoryStats.updateStats(fileStats);
    }

    public DirectoryStatistics getDirectoryStatistics() {
        return directoryStats;
    }
}

