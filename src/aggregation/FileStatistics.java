package aggregation;

public class FileStatistics {
    private String fileName;
    private int wordCount;
    private int isCount;
    private int areCount;
    private int youCount;
    private String longestWord;
    private String shortestWord;

    public FileStatistics(String fileName, int wordCount, int isCount, int areCount, int youCount,
                          String longestWord, String shortestWord) {
        this.fileName = fileName;
        this.wordCount = wordCount;
        this.isCount = isCount;
        this.areCount = areCount;
        this.youCount = youCount;
        this.longestWord = longestWord;
        this.shortestWord = shortestWord;
    }

    public String getFileName() { return fileName; }
    public int getWordCount() { return wordCount; }
    public int getIsCount() { return isCount; }
    public int getAreCount() { return areCount; }
    public int getYouCount() { return youCount; }
    public String getLongestWord() { return longestWord; }
    public String getShortestWord() { return shortestWord; }
}