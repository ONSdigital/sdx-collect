package com.github.onsdigital.perkin.transform.jpg;

import com.github.onsdigital.perkin.json.Survey;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
public class ImageIndexCsvCreator {

    //TODO: this may need to be a windows new line? - check with Martyn Colmer. FTP may modify this though - he's happy with unix format
    private static final String NEW_LINE = System.lineSeparator();
    private static final char COMMA = ',';

    private String filename = "UNKNOWN.csv";
    private String path = "UNKNOWN";
    private StringBuilder csv;

    public ImageIndexCsvCreator() {
        csv = new StringBuilder();
    }

    /*
    date+time, full path to image, batch number - a date, scan number (image name without suffix), survey_id, form_type
    e.g. 0203, idbr ru_ref without check letter, period, page number, optional front page marker 03/03/2016 10:05:03,\\NP3RVWAPXX370\EDC_PROD\EDC_QImages\Images\E100080458.JPG,20160303,E100080458,244,0005,49902138794,201602,001,0
    */
    public void addImage(Date now, long sequence, Survey survey, String path, String filename, String scanId, int pageNumber) {

        if (pageNumber > 1) {
            //start a new line if it's not the first entry
            csv.append(NEW_LINE);
        }

        String period = createPeriod(survey);

        csv.append(formatDateTime(now)).append(COMMA)
                .append(path + filename).append(COMMA)
                .append(formatDate(now)).append(COMMA) // this is their 'batch number' but it's a different batch number to what we know - we set to a date
                .append(scanId).append(COMMA)
                .append(survey.getId()).append(COMMA)
                .append(survey.getCollection().getInstrumentId()).append(COMMA)
                .append(survey.getMetadata().getStatisticalUnitId()).append(COMMA)
                .append(period).append(COMMA)
                .append(format(pageNumber));

        if (pageNumber == 1) {
            //indicate this is the first page
            csv.append(",0");
            //set the filename
            this.filename = "EDC_" + survey.getId() + "_" + formatDate(survey.getDate()) + "_" + sequence + ".csv";
        }
    }

    protected static String createPeriod(Survey survey) {
        String period = survey.getCollection().getPeriod();

        if (period == null) {
            period = "";
        }

        if (period.length() == 4) {
            period = "20" + period;
            log.debug("CSV|prepending period with 20. period now: {}", period);
        } else {
            if (period.length() > 6) {
                period = period.substring(0, 6);
            } else {
                if (period.length() < 6) {
                    period = StringUtils.leftPad(period, 6, '0');
                    log.debug("CSV|left padded period with 0. period now: {}", period);
                }
            }
        }

        return period;
    }

    /**
     * @param pageNumber the page number
     * @return left padded with zeros e.g. 001
     */
    private String format(int pageNumber) {
       return StringUtils.leftPad("" + pageNumber, 3, '0');
    }

    protected static String formatDateTime(Date date) {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(date);
    }

    protected static String formatDate(Date date) {
        return new SimpleDateFormat("yyyyMMdd").format(date);
    }

    public ImageIndexCsv getFile() {
        return ImageIndexCsv.builder()
                .csv(csv.toString())
                .filename(filename)
                .path(path)
                .build();
    }
}
