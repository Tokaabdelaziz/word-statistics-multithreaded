package aggregation;

public class DirectoryStatistics {
    private int totalWords;
    private int totalIs;
    private int totalAre;
    private int totalYou;
    private String longestWord;
    private String shortestWord;

    public DirectoryStatistics() {
        totalWords = 0;
        totalIs = 0;
        totalAre = 0;
        totalYou = 0;
        longestWord = "";
        shortestWord = "";
    }

    // Update statistics with a single file's stats
    public void updateStats(FileStatistics fileStats) {
        totalWords += fileStats.getWordCount();
        totalIs += fileStats.getIsCount();
        totalAre += fileStats.getAreCount();
        totalYou += fileStats.getYouCount();

        // Update longest word
        if (fileStats.getLongestWord().length() > longestWord.length()) {
            longestWord = fileStats.getLongestWord();
        }

        // Update shortest word
        if (shortestWord.isEmpty() || fileStats.getShortestWord().length() < shortestWord.length()) {
            shortestWord = fileStats.getShortestWord();
        }
    }

    // Getters
    public int getTotalWords() { return totalWords; }
    public int getTotalIs() { return totalIs; }
    public int getTotalAre() { return totalAre; }
    public int getTotalYou() { return totalYou; }
    public String getLongestWord() { return longestWord; }
    public String getShortestWord() { return shortestWord; }

    @Override
    public String toString() {
        return "Directory Statistics:\n" +
                "Total Words: " + totalWords + "\n" +
                "Total 'is': " + totalIs + "\n" +
                "Total 'are': " + totalAre + "\n" +
                "Total 'you': " + totalYou + "\n" +
                "Longest Word: " + longestWord + "\n" +
                "Shortest Word: " + shortestWord;
    }
}
