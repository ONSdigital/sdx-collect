package com.github.onsdigital.perkin.transform.idbr;

import com.github.onsdigital.perkin.helper.Timer;
import com.github.onsdigital.perkin.json.Survey;
import com.github.onsdigital.perkin.transform.*;
import com.github.onsdigital.perkin.transform.pck.TransformerHelper;
import com.github.onsdigital.perkin.transform.pck.derivator.DerivatorFactory;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

/**
 * An IDBR receipt records that a respondent unit (RU) has completed a survey (id)
 * on a particular date.
 *
 * An IDBR batch receipt file can contain multiple receipts.
 */
@Slf4j
public class IdbrTransformer implements Transformer, Callable<List<DataFile>> {

    private static final String DELIMITER = ":";

    private static final String IDBR_PREFIX = "REC";
    private static final String SEPARATOR = "_";
    private static final String IDBR_FILE_TYPE = ".DAT";

    public static final String FILENAME_DATE_FORMAT = "ddMM";

    private Survey survey;
    private TransformContext context;
    private CountDownLatch latch;

    public IdbrTransformer(final Survey survey, final TransformContext context, final CountDownLatch latch) {
        this.survey = survey;
        this.context = context;
        this.latch = latch;
    }

    @Override
    public List<DataFile> call() throws TransformException {
        return transform(survey, context);
    }

    @Override
    public List<DataFile> transform(final Survey survey, final TransformContext context) throws TransformException {

        List<DataFile> idbr = new ArrayList<>();

        try {
            Timer timer = new Timer("transform.idbr.");

            idbr = Arrays.asList(createIdbrReceipt(survey, survey.getDate(), context));

            timer.stopStatus(200);
            Audit.getInstance().increment(timer);
        } finally {
            latch.countDown();
        }

        return idbr;
    }

    public IdbrReceipt createIdbrReceipt(final Survey survey, final Date date, final TransformContext context) {

        return IdbrReceipt.builder()
                .receipt(createReceipt(survey))
                .filename(createFilename(date, context.getSequence()))
                .path(context.getIdbrPath())
                .build();
    }

    /**
     * Create IDBR receipt data for a survey.
     *
     * Format is respondent:checkletter:surveyId:date
     *
     * e.g. 99999994188:F:244:201410
     *
     * @param survey
     * @return the IDBR receipt data
     * @throws java.text.ParseException if problem parsing the survey date e.g. 01 Oct 2014
     */
    private String createReceipt(final Survey survey) {

        final StringBuilder receipt = new StringBuilder()
            .append(survey.getMetadata().getStatisticalUnitId())
            .append(DELIMITER)
            .append(survey.getMetadata().getRuRefCheckLetter())
            .append(DELIMITER)
            .append(TransformerHelper.leftPadZeroes(survey.getId(), 3))
            .append(DELIMITER)
            .append(formatIdbrDate(survey.getCollection().getPeriod()));

        return receipt.toString();
    }

    /**
     * Create IDBR filename for a batch.
     *
     * Format is RECddMM_batchId.DAT
     *
     * e.g. REC1001_30000.DAT
     * for 10th January, batch 30000
     *
     * @param date the date to use in the filename
     * @param batch the batch to use in the filename
     * @return the IDBR filename
     */
    private String createFilename(final Date date, final long batch) {
        final StringBuilder filename = new StringBuilder()
                .append(IDBR_PREFIX)
                .append(new SimpleDateFormat(FILENAME_DATE_FORMAT).format(date))
                .append(SEPARATOR)
                .append(batch)
                .append(IDBR_FILE_TYPE);

        return filename.toString();
    }

    private String formatIdbrDate(final String period) {
        if (period == null || period.length() != 4) {
            log.warn("IDBR|period does not have length 4. IDBR receipt will have a problem downstream! period: {}", period);
        }
        return "20" + period;
    }
}
