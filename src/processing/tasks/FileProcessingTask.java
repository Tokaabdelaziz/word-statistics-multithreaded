package processing.tasks;

import processing.*;

import java.nio.file.Path;
import java.util.List;

public class FileProcessingTask implements Runnable {

    private List<Path> files;
    private UpdateDispatcher dispatcher; // Person 5 implements this

    public FileProcessingTask(List<Path> files, UpdateDispatcher dispatcher) {
        this.files = files;
        this.dispatcher = dispatcher;
    }

    @Override
    public void run() {

        FileProcessor processor = new FileProcessor();

        for (Path file : files) {

            WordStatistics stats = processor.process(file);

            dispatcher.sendUpdate(stats);
        }
    }
}


