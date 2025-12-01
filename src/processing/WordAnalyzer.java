
package processing;

public class WordAnalyzer {

    public void updateWordLengths(String word, WordStatistics stats) {

        if (word.length() == 0) return;

        if (stats.longestWord.equals("") || word.length() > stats.longestWord.length()) {
            stats.longestWord = word;
        }

        if (stats.shortestWord.equals("") || word.length() < stats.shortestWord.length()) {
            stats.shortestWord = word;
        }
    }
}
