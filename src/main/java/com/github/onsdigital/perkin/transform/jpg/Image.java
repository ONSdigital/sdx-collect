package com.github.onsdigital.perkin.transform.jpg;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Image {

    private String filename;
    private byte[] data;

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
