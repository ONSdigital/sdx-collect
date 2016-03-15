package com.github.onsdigital.perkin.transform.pck;


import com.github.onsdigital.perkin.json.Survey2;
import com.github.onsdigital.perkin.json.SurveyParser;
import com.github.onsdigital.perkin.test.FileHelper;
import com.github.onsdigital.perkin.transform.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
@Slf4j
public class TransformerTest {

    private PckTransformer classUnderTest;

    private String surveyFilename;
    private String pckFilename;

    public TransformerTest(String surveyFilename , String pckFilename){
        this.surveyFilename = surveyFilename;
        this.pckFilename = pckFilename;
    }

    @Before
    public void setUp(){
        classUnderTest = new PckTransformer();
    }

    @Parameterized.Parameters(name = "{index}: {0} should produce {1}")
    public static Collection<Object[]> data() throws IOException {

        File directory = FileHelper.getDirectory("to-pck");

        List<File> jsonFiles = new ArrayList<>(FileHelper.findListOfFiles(directory, "json"));
        List<Object[]> objs = new ArrayList<>();

        for (File file: jsonFiles){
            objs.add(new Object[]{ file.getName(), getPckFilename(file) });
        }

        return objs;
    }

    private static String getPckFilename(File file) {
        return FilenameUtils.removeExtension(file.getName()) + ".pck";
    }

    @Test
    public void should() {
        System.out.println("test. survey: " + surveyFilename + " pck: " + pckFilename);
    }

    @Test
    public void shouldBuildPck() throws IOException {

        //Given
        Survey2 survey = loadSurvey();
        log.debug("TEST|survey2: {}", survey);
        long batch = 30001L;
        TransformContext context = createTransformContext(survey, batch);

        //When
        List<DataFile> files = classUnderTest.transform(survey, context);

        //Then
        String expectedPck = loadPck();
        String expectedPckFilename = batch + "_" + survey.getMetadata().getRuRef() + ".pck";

        assertThat(files, hasSize(1));
        assertThat(files.get(0).toString(), is(expectedPck));
        assertThat(files.get(0).getFilename(), is(expectedPckFilename));
    }

    private Survey2 loadSurvey() throws IOException {
        String json = loadFile(surveyFilename);
        SurveyParser parser = new SurveyParser();
        return parser.parse(json);
    }

    private String loadPck() throws IOException {
        return loadFile(pckFilename);
    }

    private String loadFile(String filename) throws IOException {
        log.debug("TEST|loading file: to-pck/" + filename);
        return FileHelper.loadFile("to-pck/" + filename);
    }
    
    private TransformContext createTransformContext(Survey2 survey, long batch) throws TemplateNotFoundException {
        TransformContext context = TransformEngine.getInstance().createTransformContext(survey);
        //override the batch number
        context.setBatch(batch);
        return context;
    }
}
