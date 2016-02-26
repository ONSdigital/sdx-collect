package com.github.onsdigital.perkin.storage;

import org.apache.commons.fileupload.FileItem;

import java.io.IOException;

public interface Publisher {
    void publish(final FileItem fileItem, final String path) throws IOException;
}
