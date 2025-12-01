package core;

import processing.WordStatistics;

public class UpdateDispatcher {

    private final GuiUpdateListener guiListener;
    private final AggregationListener aggregationListener;

    public UpdateDispatcher(GuiUpdateListener guiListener,
                            AggregationListener aggregationListener) {
        this.guiListener = guiListener;
        this.aggregationListener = aggregationListener;
    }

    public void dispatch(WordStatistics stats) {
        if (stats == null) {
            return;
        }

        // Notify GUI (update row for this file)
        if (guiListener != null) {
            guiListener.onFileUpdated(stats);
        }

        // Notify aggregation module
        if (aggregationListener != null) {
            aggregationListener.onFileStats(stats);
        }
    }
}

