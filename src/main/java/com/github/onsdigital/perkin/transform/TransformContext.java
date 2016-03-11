package com.github.onsdigital.perkin.transform;

import com.github.onsdigital.perkin.json.SurveyTemplate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransformContext {

    private long batch;
    private SurveyTemplate surveyTemplate;
    private String pdfTemplate;
}
