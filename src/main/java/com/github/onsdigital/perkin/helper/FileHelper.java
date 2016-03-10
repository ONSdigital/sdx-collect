package com.github.onsdigital.perkin.helper;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class FileHelper {

    public static void saveFile(byte[] bytes, String filename) throws IOException {

        Path path = Paths.get("target/" + filename);

        Files.write(path, bytes);

        System.out.println("FileHelper saved file target/" + filename);
    }

    public static byte[] loadFileAsBytes(String filename) throws IOException {

        InputStream in = FileHelper.class.getClassLoader().getResourceAsStream(filename);
        System.out.println("loaded file:  " + filename + " as: " + in);
        return IOUtils.toByteArray(in);
    }

    public static String loadFile(String filename) throws IOException {
        return new String(loadFileAsBytes(filename), StandardCharsets.UTF_8);
    }
}
