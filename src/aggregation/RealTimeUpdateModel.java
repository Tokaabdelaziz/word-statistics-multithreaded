package aggregation;

import java.util.ArrayList;
import java.util.List;

public class RealTimeUpdateModel {
    private List<DirectoryStatsListener> listeners = new ArrayList<>();

    public void addListener(DirectoryStatsListener listener) {
        listeners.add(listener);
    }

    public void removeListener(DirectoryStatsListener listener) {
        listeners.remove(listener);
    }

    public void updateStats(DirectoryStatistics stats) {
        for (DirectoryStatsListener listener : listeners) {
            listener.onStatsUpdated(stats);
        }
    }

    public interface DirectoryStatsListener {
        void onStatsUpdated(DirectoryStatistics stats);
    }
}

