package com.github.onsdigital.perkin.transform.jpg;

import com.github.onsdigital.perkin.transform.DataFile;
import lombok.Builder;
import lombok.Data;

/**
 * An IDBR receipt records that a respondent unit (RU) has completed a survey (id)
 * on a particular date.
 */
@Data
@Builder
public class ImageIndexCsv implements DataFile {

    private String csv;
    private String path;
    private String filename;

    @Override
    public byte[] getBytes() {
        return csv.getBytes();
    }

    @Override
    public long getSize() {
        return csv.length();
    }
}
