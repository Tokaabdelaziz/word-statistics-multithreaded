package aggregation;

public class RealTimeUpdateExample {

    public static void main(String[] args) {
       
        RealTimeUpdateModel model = new RealTimeUpdateModel();

        
        RealTimeUpdateModel.DirectoryStatsListener consoleListener = stats -> {
            System.out.println("=== Stats Updated ===");
            System.out.println("Total Words: " + stats.getTotalWords());
            System.out.println("Total 'is': " + stats.getTotalIs());
            System.out.println("Longest Word: " + stats.getLongestWord());
            System.out.println("-------------------");
        };

        
        model.addListener(consoleListener);

      
        DirectoryStatistics stats1 = new DirectoryStatistics();
        stats1.updateStats(new FileStatistics(50, 2, 1, 3, "programming", "a"));

        DirectoryStatistics stats2 = new DirectoryStatistics();
        stats2.updateStats(new FileStatistics(100, 5, 3, 2, "exceptionally", "it"));

        
        model.updateStats(stats1);
        model.updateStats(stats2);

    
        model.removeListener(consoleListener);

      
        DirectoryStatistics stats3 = new DirectoryStatistics();
        stats3.updateStats(new FileStatistics(70, 1, 2, 1, "aggregator", "I"));
        model.updateStats(stats3);

        System.out.println("Example finished.");
    }
}

