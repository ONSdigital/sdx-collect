package com.github.onsdigital.perkin.transform.idbr;

import com.github.onsdigital.perkin.transform.DataFile;
import lombok.Builder;
import lombok.Data;

/**
 * An IDBR receipt records that a respondent unit (RU) has completed a survey (id)
 * on a particular date.
 */
@Data
@Builder
public class IdbrReceipt implements DataFile {

    private String receipt;
    private String path;
    private String filename;

    @Override
    public byte[] getBytes() {
        return receipt.getBytes();
    }

    @Override
    public long getSize() {
        return receipt.length();
    }
}
