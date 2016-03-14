package com.github.onsdigital.perkin.test;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Collection;

public class FileHelper extends com.github.onsdigital.perkin.helper.FileHelper {

    public static Collection<File> findListOfFiles(File directory, String extension) {
        return FileUtils.listFiles(directory, new String[] {extension}, false);
    }

    public static File getDirectory(String path){
        return FileUtils.toFile(FileHelper.class.getClassLoader().getResource(path));
    }
}
