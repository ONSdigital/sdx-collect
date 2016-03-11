package com.github.onsdigital.perkin.tranform.jpg;

import com.github.onsdigital.perkin.helper.FileHelper;
import com.github.onsdigital.perkin.json.Survey;
import com.github.onsdigital.perkin.transform.DataFile;
import com.github.onsdigital.perkin.transform.jpg.ImageTransformer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(MockitoJUnitRunner.class)
public class ImageBuilderTest {

    private ImageTransformer classUnderTest;

    @Before
    public void setUp() throws IOException {
        classUnderTest = new ImageTransformer();
    }

    @Test
    public void shouldCreateImagesFromPdf() throws IOException {
        //given
        byte[] pdf = FileHelper.loadFileAsBytes("to-jpg/2page.pdf");
        Survey survey = Survey.builder().build();
        long batchId = 30000;

        //when
        List<DataFile> files = classUnderTest.createImages(pdf, survey, batchId);
        save(files);

        //then
        assertThat(files.size(), is(2));
    }

    private void save(List<DataFile> files) throws IOException {

        for (DataFile file : files) {
            FileHelper.saveFile(file.getBytes(), file.getFilename());
        }
    }
}
