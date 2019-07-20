package io;

import java.io.*;

public class FileManager {

    public static void writeFile(String path, String content) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(path));
        writer.write(content);
        writer.close();
    }

    public static void appendToFile(String path, String content) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(path, true));
        writer.write(content);
        writer.close();
    }

    public static String readFile(String path) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(path));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            return sb.toString();
        } finally {
            br.close();
        }
    }

    public static boolean fileExists(String path) {
        File tmpDir = new File(path);
        return tmpDir.exists();
    }

    public static boolean fileIsDirectory(String path) {
        File tmpDir = new File(path);
        return tmpDir.isDirectory();
    }

    public static boolean deleteFile(String path) {
        File file = new File(path);
        return file.delete();
    }

    public static boolean createDirectory(String directoryName, boolean overwrite) {
        if (overwrite) {
            File dir = new File(directoryName);
            if (dir.exists()) {
                deleteDirectory(directoryName);
            }
        }
        return new File(directoryName).mkdirs();
    }

    public static boolean deleteDirectory(String directoryName) {
        File index = new File(directoryName);
        String[] entries = index.list();
        for (String s : entries){
            File currentFile = new File(index.getPath(), s);
            currentFile.delete();
        }
        return index.delete();
    }

    public static String[] listDirectories(String path) {
        File file = new File(path);
        return file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });
    }

    public static String[] listFiles(String path) {
        File file = new File(path);
        return file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isFile();
            }
        });
    }

}
