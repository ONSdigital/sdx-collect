package com.github.onsdigital.perkin.transform.jpg;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

@Data
@Builder
public class ImageInfo {

    //TODO: csv image index

    @Singular("image")
    private List<Image> images;
}
