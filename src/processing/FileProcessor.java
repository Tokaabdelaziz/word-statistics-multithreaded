package processing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class FileProcessor {

    private final KeywordsCounter keywordsCounter = new KeywordsCounter();
    private final WordAnalyzer analyzer = new WordAnalyzer();

    public WordStatistics process(String filePath) throws IOException {

        WordStatistics stats = new WordStatistics(filePath);

        BufferedReader reader = new BufferedReader(new FileReader(filePath));

        String line;
        while ((line = reader.readLine()) != null) {

            String[] words = line.split("\\W+"); // split on non-letters

            for (String w : words) {
                if (w.isEmpty()) continue;

                stats.addWordCount(1);

                keywordsCounter.count(w, stats);
                analyzer.analyze(w, stats);
            }
        }

        reader.close();
        return stats;
    }
}

