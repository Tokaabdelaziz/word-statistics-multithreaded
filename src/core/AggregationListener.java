package core;

import processing.WordStatistics;

public interface AggregationListener {
    void onFileStats(WordStatistics stats);
}