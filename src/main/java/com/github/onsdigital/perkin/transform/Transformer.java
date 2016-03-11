package com.github.onsdigital.perkin.transform;

import com.github.onsdigital.perkin.json.Survey;

import java.util.List;

public interface Transformer {

    List<DataFile> transform(final Survey survey, long batchId) throws TransformException;
}
