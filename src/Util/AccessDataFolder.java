package Util;

import java.nio.file.Path;
import java.nio.file.Paths;

public class AccessDataFolder {

    // Default to "storage" folder
    public static Path getDataFilePath(String filename) {
        return getDataFilePath(filename, "storage");
    }

    // Overloaded method to allow custom folder
    public static Path getDataFilePath(String filename, String folder) {
        return Paths.get(System.getProperty("user.dir"), folder, filename);
    }
}
