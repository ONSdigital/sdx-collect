package com.github.onsdigital.perkin.transform.jpg;

import com.github.onsdigital.Json;
import com.github.onsdigital.perkin.json.Survey;
import com.github.onsdigital.perkin.test.FileHelper;
import com.github.onsdigital.perkin.test.ParameterizedTestHelper;
import com.github.onsdigital.perkin.transform.DataFile;
import com.github.onsdigital.perkin.transform.TransformContext;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
@Slf4j
public class ImageTransformerTest {

    private ImageTransformer classUnderTest;

    private File survey;
    private File csv;

    @Mock
    private CountDownLatch latch;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    public ImageTransformerTest(File survey, File csv){
        this.survey = survey;
        this.csv = csv;
    }

    @Parameterized.Parameters(name = "{index}: {0} should produce {1}")
    public static Collection<Object[]> data() throws IOException {

        return ParameterizedTestHelper.getFiles("to-jpg", "json", "csv");
    }

    @Test
    public void shouldTransformSurveyToCsv() throws IOException, ParseException {

        log.debug("TEST|json: " + survey.getName() + " csv: " + csv.getName());

        //Given
        Survey survey = ParameterizedTestHelper.loadSurvey(this.survey);
        String json = Json.prettyPrint(survey);
        log.debug("TEST|survey: {}", json);
        long batch = 30001L;
        long sequence = 2000;
        long scan = 7;
        TransformContext context = ParameterizedTestHelper.createTransformContext(survey, batch, sequence, scan);
        context.setDate(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse("15/03/2016 10:05:03"));

        classUnderTest = new ImageTransformer(survey, context, latch);

        //When
        List<DataFile> files = classUnderTest.transform(survey, context);
        FileHelper.saveFiles(files);

        //Then
        String expected = FileHelper.loadFile(csv);
        String expectedFilename = "EDC_023_" + ImageIndexCsvCreator.formatDate(survey.getDate()) + "_" + sequence + ".csv";

        assertThat(files, hasSize(2));
        MatcherAssert.assertThat(files.get(0).getFilename(), Matchers.is("S000000007.JPG"));
        assertThat(new String(files.get(1).getBytes()), is(expected));
        assertThat(files.get(1).getFilename(), is(expectedFilename));
    }
}
