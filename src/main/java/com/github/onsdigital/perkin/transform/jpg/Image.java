package com.github.onsdigital.perkin.transform.jpg;

import com.github.onsdigital.perkin.transform.DataFile;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Image implements DataFile {

    private String filename;
    private byte[] data;

    @Override
    public byte[] getBytes() {
        return data;
    }

    @Override
    public long getSize() {
        return data == null ? 0 : data.length;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Image(filename=").append(filename).append(" size=");

        if (data == null) {
            sb.append(0);
        } else {
            sb.append(data.length);
        }

        return sb.append(")").toString();
    }
}
