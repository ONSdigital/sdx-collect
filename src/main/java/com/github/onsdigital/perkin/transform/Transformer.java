package com.github.onsdigital.perkin.transform;

import com.github.onsdigital.perkin.json.Survey2;

import java.util.List;

public interface Transformer {

    List<DataFile> transform(final Survey2 survey, final TransformContext context) throws TransformException;
}
