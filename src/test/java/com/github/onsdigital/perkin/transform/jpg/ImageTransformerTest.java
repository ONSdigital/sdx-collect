package com.github.onsdigital.perkin.transform.jpg;

import com.github.onsdigital.perkin.helper.FileHelper;
import com.github.onsdigital.perkin.json.*;
import com.github.onsdigital.perkin.transform.DataFile;
import com.github.onsdigital.perkin.transform.TransformContext;
import com.github.onsdigital.perkin.transform.TransformEngine;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;

@RunWith(MockitoJUnitRunner.class)
@Slf4j
public class ImageTransformerTest {

    private ImageTransformer classUnderTest;

    @Before
    public void setUp() throws IOException {
        classUnderTest = new ImageTransformer();
    }

    @Test
    public void shouldCreateImagesAndIndexFromSurvey() throws IOException {
        //given
        Survey survey = new SurveyParser().parse(FileHelper.loadFile("survey.ftp.json"));
        TransformContext context = TransformEngine.getInstance().createTransformContext(survey);
        //TODO: override batch number / sequence number

        //when
        List<DataFile> files = classUnderTest.transform(survey, context);
        save(files);

        //then
        assertThat(files.size(), is(2));
        assertThat(files.get(0).getFilename(), endsWith(".JPG"));
        assertThat(files.get(1).getFilename(), endsWith(".csv"));

        //TODO: change this test to be file based
        ImageIndexCsv index = (ImageIndexCsv) files.get(1);
        log.debug("TEST|image index file: " + index);
        //15/03/2016 10:05:03,\\NP3RVWAPXX370\EDC_PROD\EDC_QImages\Images\S000000001.JPG,20160315,S000000001,023,0203,12345678901A,1234,001,0
        assertThat(index.getCsv(), is("15/03/2016 10:05:03,\\\\NP3RVWAPXX370\\EDC_PROD\\EDC_QImages\\Images\\S000000001.JPG,20160315,S000000001,023,0203,12345678901A,1234,001,0"));

        assertThat(index.getFilename(), is("EDC_023_20160315_30001.csv"));
    }

    private void save(List<DataFile> files) throws IOException {

        for (DataFile file : files) {
            FileHelper.saveFile(file.getBytes(), file.getFilename());
        }
    }
}
