package processing;

public class KeywordsCounter {

    public void updateCounts(String word, WordStatistics stats) {
        String w = word.toLowerCase();

        if (w.equals("is")) stats.countIs++;
        if (w.equals("are")) stats.countAre++;
        if (w.equals("you")) stats.countYou++;
    }
}

