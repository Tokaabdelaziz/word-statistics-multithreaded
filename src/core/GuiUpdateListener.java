package core;

import processing.WordStatistics;

public interface GuiUpdateListener {
    void onFileUpdated(WordStatistics stats);
}