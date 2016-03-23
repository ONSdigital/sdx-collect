package com.github.onsdigital.perkin.transform.jpg;

import com.github.onsdigital.perkin.json.Survey;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

public class ImageIndexCsvCreator {

    //TODO: this may need to be a windows new line? - check with Martyn Colmer. FTP may modify this though - he's happy with unix format
    private static final String NEW_LINE = System.lineSeparator();
    private static final char COMMA = ',';

    private String filename = "UNKNOWN.csv";
    private StringBuilder csv;

    public ImageIndexCsvCreator() {
        csv = new StringBuilder();
    }

    /*
    date+time, full path to image, batch number - a date, scan number (image name without suffix), survey_id, form_type
    e.g. 0203, idbr ru_ref without check letter, period, page number, optional front page marker 03/03/2016 10:05:03,\\NP3RVWAPXX370\EDC_PROD\EDC_QImages\Images\E100080458.JPG,20160303,E100080458,244,0005,49902138794,201602,001,0
    */
    public void addImage(long sequence, Survey survey, String filename, String scanId, int pageNumber) {

        //TODO: hardcoded path for now
        //TODO: note that the EDC_PROD could be other environments
        String fullPath = "\\\\NP3RVWAPXX370\\EDC_PROD\\EDC_QImages\\Images\\" + filename;
        //TODO: hardcoded survey date for now
        String surveyDate = "20160315";

        if (pageNumber > 1) {
            //start a new line if it's not the first entry
            csv.append(NEW_LINE);
        }

        //TODO: is this date now or the date the survey was completed?
        csv.append(formatDate(survey.getDate())).append(COMMA)
                .append(fullPath).append(COMMA)
                .append(surveyDate).append(COMMA) // this is their 'batch number' but it's a different batch number to what we know
                .append(scanId).append(COMMA)
                .append(survey.getId()).append(COMMA)
                .append(survey.getCollection().getInstrumentId()).append(COMMA)
                //TODO: need to drop the check letter
                .append(survey.getMetadata().getRuRef()).append(COMMA)
                .append(survey.getCollection().getPeriod()).append(COMMA)
                .append(StringUtils.leftPad("" + pageNumber, 3, '0'));

        if (pageNumber == 1) {
            //indicate this is the first page
            csv.append(",0");
            //set the filename
            this.filename = "EDC_" + survey.getId() + "_" + surveyDate + "_" + sequence + ".csv";
        }
    }

    private String formatDate(Date date) {
        //TODO hardcoded date for now
        return "15/03/2016 10:05:03";
    }

    public ImageIndexCsv getFile() {
        return ImageIndexCsv.builder()
                .csv(csv.toString())
                .filename(filename)
                .build();
    }
}
