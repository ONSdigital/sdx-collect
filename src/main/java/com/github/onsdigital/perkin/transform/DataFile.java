package com.github.onsdigital.perkin.transform;

public interface DataFile {

    byte[] getBytes();
    String getPath();
    String getFilename();
    long getSize();

    //TODO: getMimeType()?
}
