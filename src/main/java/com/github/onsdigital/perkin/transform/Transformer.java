package com.github.onsdigital.perkin.transform;

import com.github.onsdigital.perkin.json.Survey;
import com.github.onsdigital.perkin.json.SurveyTemplate;

import java.util.List;

public interface Transformer {

    List<DataFile> transform(final Survey survey, final SurveyTemplate template, final long batchId) throws TransformException;
}
