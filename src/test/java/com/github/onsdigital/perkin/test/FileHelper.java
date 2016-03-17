package com.github.onsdigital.perkin.test;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.Collection;

public abstract class FileHelper extends com.github.onsdigital.perkin.helper.FileHelper {

    public static Collection<File> findListOfFiles(String path, String extension) {
        return FileUtils.listFiles(getDirectory(path), new String[] {extension}, false);
    }

    public static File getDirectory(String path){
        return FileUtils.toFile(FileHelper.class.getClassLoader().getResource(path));
    }

    public static File changeFileExtension(File file, String extension) {
        return new File(FilenameUtils.removeExtension(file.getAbsolutePath()) + "." + extension);
    }
}
