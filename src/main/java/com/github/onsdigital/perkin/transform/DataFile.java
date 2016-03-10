package com.github.onsdigital.perkin.transform;

public interface DataFile {

    byte[] getBytes();
    String getFilename();

    //TODO: getMimeType()?
}
