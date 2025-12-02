
package filesystem;

import java.io.File;

public class FileFilter {

    public static boolean isTxtFile(File f) {
        return f.isFile() && f.getName().toLowerCase().endsWith(".txt");
    }
}
