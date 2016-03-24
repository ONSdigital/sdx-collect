package com.github.onsdigital.perkin.transform.pck;


import com.github.onsdigital.perkin.json.Survey;
import com.github.onsdigital.perkin.json.SurveyParser;
import com.github.onsdigital.perkin.test.FileHelper;
import com.github.onsdigital.perkin.test.ParameterizedTestHelper;
import com.github.onsdigital.perkin.transform.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
@Slf4j
public class PckTransformerTest {

    private PckTransformer classUnderTest;

    private File survey;
    private File pck;

    public PckTransformerTest(File survey, File pck){
        this.survey = survey;
        this.pck = pck;
    }

    @Before
    public void setUp(){
        classUnderTest = new PckTransformer();
    }

    @Parameterized.Parameters(name = "{index}: {0} should produce {1}")
    public static Collection<Object[]> data() throws IOException {

        return ParameterizedTestHelper.getFiles("to-pck", "json", "pck");
    }

    @Test
    public void shouldTransformSurveyToPck() throws IOException {

        log.debug("TEST|json: " + survey.getName() + " pck: " + pck.getName());

        //Given
        Survey survey = ParameterizedTestHelper.loadSurvey(this.survey);
        log.debug("TEST|survey: {}", survey);
        long batch = 30001L;
        long sequence = 1000;
        long scan = 2;
        TransformContext context = ParameterizedTestHelper.createTransformContext(survey, batch, sequence, scan);

        //When
        List<DataFile> files = classUnderTest.transform(survey, context);
        FileHelper.saveFiles(files);

        //Then
        //TODO: cope with expected Exceptions
        String expected = FileHelper.loadFile(pck);
        String expectedFilename = survey.getId() + "_" + sequence + ".pck";

        assertThat(files, hasSize(1));
        assertThat(files.get(0).toString(), is(expected));
        assertThat(files.get(0).getFilename(), is(expectedFilename));
    }
}
