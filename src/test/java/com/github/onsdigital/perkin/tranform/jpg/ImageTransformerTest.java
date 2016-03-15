package com.github.onsdigital.perkin.tranform.jpg;

import com.github.onsdigital.perkin.helper.FileHelper;
import com.github.onsdigital.perkin.json.*;
import com.github.onsdigital.perkin.transform.DataFile;
import com.github.onsdigital.perkin.transform.TransformContext;
import com.github.onsdigital.perkin.transform.TransformEngine;
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
public class ImageTransformerTest {

    private ImageTransformer classUnderTest;

    @Before
    public void setUp() throws IOException {
        classUnderTest = new ImageTransformer();
    }

    @Test
    public void shouldCreateImagesFromPdf() throws IOException {
        //given
        Survey survey = new SurveyParser().parse(FileHelper.loadFile("survey.json"));
        TransformContext context = TransformEngine.getInstance().createTransformContext(survey);

        //when
        List<DataFile> files = classUnderTest.transform(survey, context);
        save(files);

        //then
        assertThat(files.size(), is(1));
    }

    private void save(List<DataFile> files) throws IOException {

        for (DataFile file : files) {
            FileHelper.saveFile(file.getBytes(), file.getFilename());
        }
    }
}
