package com.github.onsdigital.perkin.storage;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Write the FileItem data to the local file system.
 */
public class FilePublisher implements Publisher {

    public void publish(final FileItem data, final String path) throws IOException {
        String filename = path + data.getName();
        System.out.println("FilePublisher saving local file: " + filename);
        FileUtils.copyInputStreamToFile(data.getInputStream(), new File(filename));
    }
}
