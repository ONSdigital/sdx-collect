package com.github.onsdigital.perkin.transform.idbr;

import com.github.onsdigital.Json;
import com.github.onsdigital.perkin.json.Survey;
import com.github.onsdigital.perkin.test.FileHelper;
import com.github.onsdigital.perkin.test.ParameterizedTestHelper;
import com.github.onsdigital.perkin.transform.DataFile;
import com.github.onsdigital.perkin.transform.TransformContext;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
@Slf4j
public class IdbrTransformerTest {

    private IdbrTransformer classUnderTest;

    private File survey;
    private File idbr;

    @Mock
    private CountDownLatch latch;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    public IdbrTransformerTest(File survey, File idbr){
        this.survey = survey;
        this.idbr = idbr;
    }

    @Parameterized.Parameters(name = "{index}: {0} should produce {1}")
    public static Collection<Object[]> data() throws IOException {

        return ParameterizedTestHelper.getFiles("to-idbr", "json", "idbr");
    }

    @Test
    public void shouldTransformSurveyToIdbr() throws IOException {

        log.debug("TEST|json: " + survey.getName() + " pck: " + idbr.getName());

        //Given
        Survey survey = ParameterizedTestHelper.loadSurvey(this.survey);
        String json = Json.prettyPrint(survey);
        FileHelper.saveFile(json.getBytes(StandardCharsets.UTF_8), "survey.json");
        log.debug("TEST|survey: {}", survey);
        long batch = 30001L;
        long sequence = 2000;
        long scan = 5;
        TransformContext context = ParameterizedTestHelper.createTransformContext(survey, batch, sequence, scan);

        classUnderTest = new IdbrTransformer(survey, context, latch);

        //When
        List<DataFile> files = classUnderTest.transform(survey, context);
        FileHelper.saveFiles(files);

        //Then
        String expected = FileHelper.loadFile(idbr);
        String expectedFilename = "REC1203_" + sequence + ".DAT";

        assertThat(files, hasSize(1));
        assertThat(new String(files.get(0).getBytes()), is(expected));
        assertThat(files.get(0).getFilename(), is(expectedFilename));
    }
}
