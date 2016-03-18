package com.github.onsdigital.perkin.transform.idbr;


import com.github.onsdigital.perkin.json.Survey;
import com.github.onsdigital.perkin.json.SurveyParser;
import com.github.onsdigital.perkin.test.FileHelper;
import com.github.onsdigital.perkin.test.ParameterizedTestHelper;
import com.github.onsdigital.perkin.transform.DataFile;
import com.github.onsdigital.perkin.transform.TemplateNotFoundException;
import com.github.onsdigital.perkin.transform.TransformContext;
import com.github.onsdigital.perkin.transform.TransformEngine;
import com.github.onsdigital.perkin.transform.pck.PckTransformer;
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
public class IdbrTransformerTest {

    private IdbrTransformer classUnderTest;

    private File survey;
    private File idbr;

    public IdbrTransformerTest(File survey, File idbr){
        this.survey = survey;
        this.idbr = idbr;
    }

    @Before
    public void setUp(){
        classUnderTest = new IdbrTransformer();
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
        log.debug("TEST|survey: {}", survey);
        long batch = 30005L;
        TransformContext context = ParameterizedTestHelper.createTransformContext(survey, batch);

        //When
        List<DataFile> files = classUnderTest.transform(survey, context);

        //Then
        String expected = FileHelper.loadFile(idbr);
        String expectedFilename = "REC1203_" + batch + ".DAT";

        assertThat(files, hasSize(1));
        assertThat(new String(files.get(0).getBytes()), is(expected));
        assertThat(files.get(0).getFilename(), is(expectedFilename));
    }
}