package com.github.onsdigital.perkin.transform.pck;


import com.github.davidcarboni.httpino.Serialiser;
import com.github.onsdigital.perkin.test.FileHelper;
import com.github.onsdigital.perkin.json.Survey;
import com.github.onsdigital.perkin.transform.*;
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
public class TransformerTest {

    private static final long BATCH_ID = 30001L;

    private String name;
    private Transformer classUnderTest;

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
        Survey survey = loadSurvey();
        TransformContext context = createTransformContext(survey, BATCH_ID);

        //When
        List<DataFile> files = classUnderTest.transform(survey, context);

        //Then
        String expectedPck = loadPck();
        assertThat(files, hasSize(1));
        assertThat(files.get(0).toString(), is(expectedPck));
        assertThat(files.get(0).getFilename(), is(BATCH_ID+"_respondentId.pck"));
    }

    private Survey loadSurvey() throws IOException {
        System.out.println("loading survey file: " + surveyFilename);
        String json = new String(com.github.onsdigital.perkin.helper.FileHelper.loadFile("to-pck/" + surveyFilename));
        return (Survey) Serialiser.deserialise(json, Survey.class);

    }

    private String loadPck() throws IOException {
        System.out.println("loading survey file: to-pck/valid.pck");
        return  com.github.onsdigital.perkin.helper.FileHelper.loadFile("to-pck/" + pckFilename);
    }

    private TransformContext createTransformContext(Survey survey, long batch) throws TemplateNotFoundException {
        TransformContext context = TransformEngine.getInstance().createTransformContext(survey);
        context.setBatch(batch);
        return context;
    }
}
