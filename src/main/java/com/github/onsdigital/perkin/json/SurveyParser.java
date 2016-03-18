package com.github.onsdigital.perkin.json;

import com.github.onsdigital.perkin.transform.Audit;
import com.google.gson.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

@Slf4j
public class SurveyParser {

    public Survey parse(String json) throws SurveyParserException {
        try {
            Survey survey = deserialize(json);

            //TODO validation - type, origin, respondent should be 12 chars, 11 digits and a check letter
            if (!"0.0.1".equals(survey.getVersion())) {
                logAndThrowError("unsupported version: " + survey.getVersion(), json);
            }

            if (survey.getCollection() == null) {
                logAndThrowError("'collection' section is mandatory", json);
            } else {
                String period = survey.getCollection().getPeriod();
                if (StringUtils.isBlank(period)) {
                    logAndThrowError("collection 'period' is mandatory", json);
                } else {
                    if (period.length() != 4) {
                        //TODO: throw error? or pass to downstream as is
                        Audit.getInstance().increment("survey.period.length.not.4.WARNING");
                        log.warn("SURVEY|PARSE|PERIOD|expected 4 character period, got period: {} length: {}", period, period.length());
                    }
                }
            }

            if (survey.getMetadata() == null) {
                logAndThrowError("'metadata' section is mandatory", json);
            }

            log.debug("SURVEY|PARSE|parsed json as {}", survey);
            return survey;

        } catch(JsonParseException e) {
            String message = "Problem parsing survey from json";
            log.error("SURVEY|PARSE|{} json: {}", message, json, e);
            throw new SurveyParserException(message, json, e);
        }
    }

    private void logAndThrowError(String message, String json) throws SurveyParserException {
        log.error("SURVEY|PARSE|{} json: {}", message, json);
        throw new SurveyParserException(message + " " + json);
    }

    public String prettyPrint(Survey survey) {
        return serialize(survey);
    }

    private String serialize(Survey survey) {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(Date.class, new DateSerializer())
                .create();
        return gson.toJson(survey);
    }

    private Survey deserialize(String json) {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(Date.class, new DateDeserializer())
                .create();

        return gson.fromJson(json, Survey.class);
    }

    public static final String ISO8601 = "yyyy-MM-dd'T'HH:mm:ssX";

    private static final String[] DATE_FORMATS = new String[] {
            ISO8601,
            "yyyy-MM-dd"
    };

    private class DateSerializer implements JsonSerializer<Date> {

        @Override
        public JsonElement serialize(Date date, Type type, JsonSerializationContext jsonSerializationContext) {
            SimpleDateFormat sdf = new SimpleDateFormat(ISO8601);
            return new JsonPrimitive(sdf.format(date));
        }
    }

    private class DateDeserializer implements JsonDeserializer<Date> {

        @Override
        public Date deserialize(JsonElement jsonElement, Type typeOF,  JsonDeserializationContext context) throws JsonParseException {

            for (String format : DATE_FORMATS) {
                try {
                    return new SimpleDateFormat(format).parse(jsonElement.getAsString());
                } catch (ParseException e) {
                    //ignore, will be handled below if none of the formats could be parsed
                }
            }

            throw new JsonParseException("Unparseable date: \"" + jsonElement.getAsString()
                    + "\". Supported formats: " + Arrays.toString(DATE_FORMATS));
        }
    }
}
