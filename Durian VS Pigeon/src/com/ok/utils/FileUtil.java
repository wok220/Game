package com.ok.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtil {
    public static String readFile(String path) throws IOException {
        Path filePath = Paths.get(path);
        return Files.readString(filePath);
    }
    
    public static void writeFile(String path, String content) throws IOException {
        Path filePath = Paths.get(path);
        Files.writeString(filePath, content);
    }
    
    public static boolean fileExists(String path) {
        File file = new File(path);
        return file.exists();
    }
    
    public static boolean createDirectory(String path) {
        File directory = new File(path);
        return directory.mkdirs();
    }
}