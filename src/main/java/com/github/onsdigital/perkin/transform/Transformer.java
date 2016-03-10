package com.github.onsdigital.perkin.transform;

import com.github.onsdigital.perkin.json.Survey;

public interface Transformer {

    //TODO need > 1 datafile - list or array?
    DataFile transform(final Survey survey, long batchId) throws TransformException;
}
