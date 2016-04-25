package com.github.onsdigital.perkin.test;

import com.github.onsdigital.perkin.json.Survey;
import com.github.onsdigital.perkin.json.SurveyParser;
import com.github.onsdigital.perkin.transform.NumberService;
import com.github.onsdigital.perkin.transform.TemplateNotFoundException;
import com.github.onsdigital.perkin.transform.TransformContext;
import com.github.onsdigital.perkin.transform.TransformEngine;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class ParameterizedTestHelper {

    public static Collection<Object[]> getFiles(String path, String sourceExtension, String targetExtension) {

        Collection<File> files = FileHelper.findListOfFiles(path, sourceExtension);
        List<Object[]> objects = new ArrayList<>();

        for (File file: files){
            objects.add(new Object[]{ file, FileHelper.changeFileExtension(file, targetExtension) });
        }

        return objects;
    }

    public static TransformContext createTransformContext(Survey survey, long batch, long sequence, long scan) throws TemplateNotFoundException {
        TransformContext context = TransformEngine.getInstance().createTransformContext(survey);
        //override
        context.setBatch(batch);
        context.setSequence(sequence);
        NumberService numberService = new NumberService("scan", scan, 999999999);
        numberService.reset();
        context.setScanNumberService(numberService);
        return context;
    }

    public static Survey loadSurvey(File survey) throws IOException {
        String json = FileHelper.loadFile(survey);
        SurveyParser parser = new SurveyParser();
        return parser.parse(json);
    }
}
