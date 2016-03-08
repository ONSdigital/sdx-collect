package com.github.onsdigital;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class FileHelper {

    public static void saveFile(byte[] bytes, String filename) throws IOException {

        Path path = Paths.get("target/" + filename);

        Files.write(path, bytes);

        System.out.println("FileHelper saved file target/" + filename);
    }

    public static byte[] loadFileAsBytes(String name) throws IOException {
        Path path = null;

        try {
            path = Paths.get(ClassLoader.getSystemResource(name).toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new IOException("problem loading file: " + name, e);
        }

        return Files.readAllBytes(path);
    }
}
