package com.github.onsdigital.perkin.transform.idbr;

//import org.springframework.util.Assert;

import com.github.onsdigital.perkin.json.Survey2;
import com.github.onsdigital.perkin.transform.DataFile;
import com.github.onsdigital.perkin.transform.TransformContext;
import com.github.onsdigital.perkin.transform.TransformException;
import com.github.onsdigital.perkin.transform.Transformer;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

    private DateTimeFormatter surveyFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private DateTimeFormatter idbrReceiptFormatter = DateTimeFormatter.ofPattern("yyyyMM");
    private DateTimeFormatter idbrFilenameFormatter = DateTimeFormatter.ofPattern("ddMM");

    @Override
    public List<DataFile> transform(final Survey2 survey, final TransformContext context) throws TransformException {
        //TODO: use a date from the survey - submitted date?
        return Arrays.asList(createIdbrReceipt(survey, survey.getDate(), context.getBatch()));
    }

    public IdbrReceipt createIdbrReceipt(final Survey2 survey, final LocalDate date, final long batchId) {
        //TODO: import an assert library? this is spring below
//        Assert.notNull(survey, "survey should not be null");
//        Assert.notNull(survey.getDate(), "survey date should not be null");
//        Assert.notNull(date, "date should not be null");

        return IdbrReceipt.builder()
                .receipt(createReceipt(survey))
                .filename(createFilename(date, batchId))
                .build();
    }

    public IdbrReceipt createIdbrReceipt(final Survey2 survey, final Date date, final long batchId) {

        return IdbrReceipt.builder()
                .receipt(createReceipt(survey))
                .filename(createFilename(date, batchId))
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
    private String createReceipt(final Survey2 survey) {

        final StringBuilder receipt = new StringBuilder()
            .append(survey.getRespondentId())
            .append(DELIMITER)
            .append(survey.getRespondentCheckLetter())
            .append(DELIMITER)
            .append(survey.getId())
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
    private String createFilename(final LocalDate date, final long batch) {
        final StringBuilder filename = new StringBuilder()
                .append(IDBR_PREFIX)
                .append(idbrFilenameFormatter.format(date))
                .append(SEPARATOR)
                .append(batch)
                .append(IDBR_FILE_TYPE);

        return filename.toString();
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
                .append(new SimpleDateFormat("ddMM").format(date))
                .append(SEPARATOR)
                .append(batch)
                .append(IDBR_FILE_TYPE);

        return filename.toString();
    }

    private String formatIdbrDate(final String surveyDate) {
        LocalDate parsedSurveyDate = LocalDate.parse(surveyDate, surveyFormatter);
        return idbrReceiptFormatter.format(parsedSurveyDate);
    }

    private String formatIdbrDate(final Date surveyDate) {
        //TODO tidy up
        return new SimpleDateFormat("yyyyMM").format(surveyDate);
    }
}
