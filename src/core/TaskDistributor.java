package core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class TaskDistributor {

    public List<List<String>> distribute(List<String> filePaths, int numberOfWorkers) {
        if (filePaths == null || filePaths.isEmpty() || numberOfWorkers <= 0) {
            return Collections.emptyList();
        }

        int totalFiles = filePaths.size();
        int workers = Math.min(numberOfWorkers, totalFiles); // no more workers than files

        List<List<String>> result = new ArrayList<>(workers);

        for (int i = 0; i < workers; i++) {
            result.add(new ArrayList<>());
        }

        for (int i = 0; i < totalFiles; i++) {
            String path = filePaths.get(i);
            int workerIndex = i % workers;
            result.get(workerIndex).add(path);
        }

        return result;
    }
}

