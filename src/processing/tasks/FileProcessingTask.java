package processing.tasks;

import processing.FileProcessor;
import processing.WordStatistics;

import java.io.File;
import java.util.concurrent.Callable;

public class FileProcessingTask implements Callable<WordStatistics> {
    private final File file;

    public FileProcessingTask(File file) {
        this.file = file;
    }

    @Override
    public WordStatistics call() {
        FileProcessor processor = new FileProcessor();
        WordStatistics stats = processor.process(file.toPath());

        // Make sure we always use full path as identity
        stats.fileName = file.getAbsolutePath();

        return stats;
    }

}



