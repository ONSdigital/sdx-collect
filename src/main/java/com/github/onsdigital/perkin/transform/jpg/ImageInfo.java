package com.github.onsdigital.perkin.transform.jpg;

import com.github.onsdigital.perkin.transform.DataFile;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

@Data
@Builder
public class ImageInfo implements DataFile {

    //TODO: csv image index

    @Singular("image")
    private List<Image> images;

    @Override
    public byte[] getBytes() {
        //TODO need to support all the images
        return images.get(0).getData();
    }

    @Override
    public String getFilename() {
        //TODO need to support all the images
        return images.get(0).getFilename();
    }
}
