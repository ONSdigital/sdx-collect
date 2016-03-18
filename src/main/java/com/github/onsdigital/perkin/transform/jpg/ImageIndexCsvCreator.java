package com.github.onsdigital.perkin.transform.jpg;

public class ImageIndexCsvCreator {

    //TODO: this may need to be a windows new line? - check with Martyn Colmer. FTP may modify this though
    private static final String NEW_LINE = System.lineSeparator();

    private StringBuilder csv;

    public ImageIndexCsvCreator() {
        csv = new StringBuilder();
    }

    //TODO: add more image data
    public void add(String filename) {
        csv.append(filename).append(NEW_LINE);
    }

    public ImageIndexCsv getFile(String filename) {
        return ImageIndexCsv.builder()
                .csv(csv.toString())
                .filename(filename)
                .build();
    }
}
