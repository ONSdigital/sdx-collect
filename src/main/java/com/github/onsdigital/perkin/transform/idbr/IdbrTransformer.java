package com.github.onsdigital.perkin.transform.idbr;

import com.github.onsdigital.perkin.helper.Timer;
import com.github.onsdigital.perkin.json.Survey;
import com.github.onsdigital.perkin.transform.*;
import com.github.onsdigital.perkin.transform.pck.TransformerHelper;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * An IDBR receipt records that a respondent unit (RU) has completed a survey (id)
 * on a particular date.
 *
 * An IDBR batch receipt file can contain multiple receipts.
 */
public class IdbrTransformer implements Transformer {

    private static final String DELIMITER = ":";

    private static final String IDBR_PREFIX = "REC";
    private static final String SEPARATOR = "_";
    private static final String IDBR_FILE_TYPE = ".DAT";

    private static final String NEW_LINE = System.getProperty("line.separator");

    public static final String FILENAME_DATE_FORMAT = "ddMM";
    public static final String RECEIPT_DATE_FORMAT = "yyyyMM";

    @Override
    public List<DataFile> transform(final Survey survey, final TransformContext context) throws TransformException {

        Timer timer = new Timer("transform.idbr.");

        List<DataFile> idbr = Arrays.asList(createIdbrReceipt(survey, survey.getDate(), context.getSequence()));

        timer.stopStatus(200);
        Audit.getInstance().increment(timer);

        return idbr;
    }

    public IdbrReceipt createIdbrReceipt(final Survey survey, final Date date, final long sequence) {

        return IdbrReceipt.builder()
                .receipt(createReceipt(survey))
                .filename(createFilename(date, sequence))
                //TODO: path is hardcoded for now
                .path("\\\\NP3RVWAPXX370\\SDX_preprod\\EDC_QReceipts\\")
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
            .append(survey.getMetadata().getRespondentId())
            .append(DELIMITER)
            .append(survey.getMetadata().getRespondentCheckLetter())
            .append(DELIMITER)
            .append(TransformerHelper.leftPadZeroes(survey.getId(), 3))
            .append(DELIMITER)
            .append(formatIdbrDate(survey.getDate()))
            .append(NEW_LINE);

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

    private String formatIdbrDate(final Date surveyDate) {
        return new SimpleDateFormat(RECEIPT_DATE_FORMAT).format(surveyDate);
    }
}
