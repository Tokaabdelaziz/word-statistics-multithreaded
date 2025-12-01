package processing;

public class WordStatistics {
    public String fileName;
    public int wordCount;
    public int countIs;
    public int countAre;
    public int countYou;
    public String longestWord = "";
    public String shortestWord = "";

    public WordStatistics(String fileName) {
        this.fileName = fileName;
    }
}
