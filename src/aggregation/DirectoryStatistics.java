package aggregation;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DirectoryStatsAggregator {
    private StatsSynchronizer synchronizer;
    private List<FileStatistics> allFileStats; 

    public DirectoryStatsAggregator() {
        synchronizer = new StatsSynchronizer();
        allFileStats = new ArrayList<>();
    }

    // Process a single file statistics
    public void processFile(FileStatistics fileStats) {
        synchronizer.update(fileStats);
        allFileStats.add(fileStats);
    }

    // Get final directory statistics
    public DirectoryStatistics getFinalStats() {
        return synchronizer.getDirectoryStatistics();
    }

    // Export to summary.txt
    public void exportSummary(String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            DirectoryStatistics stats = getFinalStats();
            writer.write("Directory Summary\n");
            writer.write("-----------------\n");
            writer.write("Total Words: " + stats.getTotalWords() + "\n");
            writer.write("Total 'is': " + stats.getTotalIs() + "\n");
            writer.write("Total 'are': " + stats.getTotalAre() + "\n");
            writer.write("Total 'you': " + stats.getTotalYou() + "\n");
            writer.write("Longest Word: " + stats.getLongestWord() + "\n");
            writer.write("Shortest Word: " + stats.getShortestWord() + "\n");
            System.out.println("summary.txt created successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Export to directory_statistics.json
    public void exportJSON(String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            DirectoryStatistics stats = getFinalStats();
            writer.write("{\n");
            writer.write("  \"totalWords\": " + stats.getTotalWords() + ",\n");
            writer.write("  \"totalIs\": " + stats.getTotalIs() + ",\n");
            writer.write("  \"totalAre\": " + stats.getTotalAre() + ",\n");
            writer.write("  \"totalYou\": " + stats.getTotalYou() + ",\n");
            writer.write("  \"longestWord\": \"" + stats.getLongestWord() + "\",\n");
            writer.write("  \"shortestWord\": \"" + stats.getShortestWord() + "\"\n");
            writer.write("}");
            System.out.println("directory_statistics.json created successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Export to file_statistics.csv
    public void exportCSV(String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            // Header
            writer.write("File Name,Total Words,is,are,you,Longest Word,Shortest Word\n");
            int index = 1;
            for (FileStatistics fs : allFileStats) {
                writer.write("File" + index + "," + fs.getWordCount() + "," + fs.getIsCount() + ","
                        + fs.getAreCount() + "," + fs.getYouCount() + "," + fs.getLongestWord() + ","
                        + fs.getShortestWord() + "\n");
                index++;
            }
            System.out.println("file_statistics.csv created successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

