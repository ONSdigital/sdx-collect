package com.github.onsdigital.perkin.tranform.jpg;

import com.github.onsdigital.perkin.helper.FileHelper;
import com.github.onsdigital.perkin.json.Survey;
import com.github.onsdigital.perkin.transform.jpg.Image;
import com.github.onsdigital.perkin.transform.jpg.ImageBuilder;
import com.github.onsdigital.perkin.transform.jpg.ImageInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(MockitoJUnitRunner.class)
public class ImageBuilderTest {

    private ImageBuilder classUnderTest;

    @Before
    public void setUp() throws IOException {
        classUnderTest = new ImageBuilder();
    }

    @Test
    public void shouldCreateImagesFromPdf() throws IOException {
        //given
        byte[] pdf = FileHelper.loadFileAsBytes("to-jpg/2page.pdf");
        Survey survey = Survey.builder().build();
        long batchId = 30000;

        //when
        ImageInfo images = classUnderTest.createImages(pdf, survey, batchId);
        save(images);

        //then
        assertThat(images.getImages().size(), is(2));
    }

    private void save(ImageInfo images) throws IOException {

        for (Image image : images.getImages()) {
            FileHelper.saveFile(image.getData(), image.getFilename());
        }
    }
}
