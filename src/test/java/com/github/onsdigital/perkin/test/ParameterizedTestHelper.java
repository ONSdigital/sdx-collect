package com.github.onsdigital.perkin.test;


import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class ParameterizedTestHelper {

    public static Collection<Object[]> getFiles(String path, String sourceExtension, String targetExtension) {

        Collection<File> files = FileHelper.findListOfFiles(path, sourceExtension);
        List<Object[]> objects = new ArrayList<>();

        for (File file: files){
            objects.add(new Object[]{ file, FileHelper.changeFileExtension(file, targetExtension) });
        }

        return objects;
    }

    public static Collection<Object[]> getFiles(String path, String sourceExtension, String targetExtension, String noBatchExtension) {

        Collection<File> files = FileHelper.findListOfFiles(path, sourceExtension);
        List<Object[]> objects = new ArrayList<>();

        for (File file: files){
            objects.add(new Object[]{ file, FileHelper.changeFileExtension(file, targetExtension), FileHelper.changeFileExtension(file, noBatchExtension) });
        }

        return objects;
    }
}
