package com.github.onsdigital.perkin.transform.jpg;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Image {

    private String filename;
    private byte[] data;
}