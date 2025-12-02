package filesystem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DirectoryScanner {

    private List<String> foundFiles;
    private final int MAX_DEPTH = 2; 

    public ScanResult scanDirectory(String path, boolean includeSubfolders) {

        foundFiles = new ArrayList<>();
        File root = new File(path);

        if (!root.exists() || !root.isDirectory()) {
            System.out.println("Invalid directory path!");
            return new ScanResult(foundFiles, includeSubfolders);
        }

        scanRecursive(root, includeSubfolders, 0);

        return new ScanResult(foundFiles, includeSubfolders);
    }

    private void scanRecursive(File folder, boolean recursive, int currentLevel) {

        File[] files = folder.listFiles();
        if (files == null) return;

        for (File f : files) {

            if (FileFilter.isTxtFile(f)) {
                foundFiles.add(f.getAbsolutePath());
            }

            if (recursive && f.isDirectory() && currentLevel < MAX_DEPTH) {
                scanRecursive(f, true, currentLevel + 1);
            }
        }
    }
}

