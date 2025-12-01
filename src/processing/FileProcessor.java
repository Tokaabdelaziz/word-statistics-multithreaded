package processing;

import java.io.*;
import java.nio.file.*;

public class FileProcessor {

    private KeywordsCounter keywordCounter = new KeywordsCounter();
    private WordAnalyzer wordAnalyzer = new WordAnalyzer();

    public WordStatistics process(Path filePath) {

        WordStatistics stats = new WordStatistics(filePath.getFileName().toString());

        try (BufferedReader reader = Files.newBufferedReader(filePath)) {

            String line;

            while ((line = reader.readLine()) != null) {

                String[] words = line.split("\\W+"); // split by non-letters

                for (String w : words) {
                    if (w.isEmpty()) continue;

                    stats.wordCount++;
                    keywordCounter.updateCounts(w, stats);
                    wordAnalyzer.updateWordLengths(w, stats);
                }
            }

        } catch (IOException e) {
            System.out.println("Error reading file: " + filePath);
        }

        return stats;
    }
}
