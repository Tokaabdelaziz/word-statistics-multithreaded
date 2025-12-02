package filesystem;

import java.util.List;

public class ScanResult {

    private List<String> filePaths;
    private boolean includedSubfolders;

    public ScanResult(List<String> filePaths, boolean includedSubfolders) {
        this.filePaths = filePaths;
        this.includedSubfolders = includedSubfolders;
    }

    public List<String> getFilePaths() {
        return filePaths;
    }

    public int getFileCount() {
        return filePaths.size();
    }

    public boolean includedSubfolders() {
        return includedSubfolders;
    }
}

