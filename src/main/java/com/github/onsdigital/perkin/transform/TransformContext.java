package com.github.onsdigital.perkin.transform;

import com.github.onsdigital.perkin.json.SurveyTemplate;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class TransformContext {

    private Date date;

    private long batch;
    private long sequence;
    private NumberService scanNumberService;

    private SurveyTemplate surveyTemplate;
    private String pdfTemplate;
}
