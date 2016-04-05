package com.github.onsdigital.perkin.json;

import com.github.onsdigital.perkin.helper.Timer;
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
        Timer timer = new Timer("survey.parse.");
        try {
            Survey survey = deserialize(json);

            //TODO validation - type, origin, respondent should be 12 chars, 11 digits and a check letter - can be longer - log a warning?

            if (survey == null) {
                logAndThrowError("Problem parsing survey from json", json);
            }

            if (StringUtils.isBlank(survey.getVersion())) {
                logAndThrowError("'version' is mandatory", json);
            } else {
                if (! survey.getVersion().equals("0.0.1")) {
                    logAndThrowError("unsupported version: " + survey.getVersion(), json);
                }
            }

            if (StringUtils.isBlank(survey.getId())) {
                logAndThrowError("'survey_id' is mandatory", json);
            }

            if (survey.getDate() == null) {
                logAndThrowError("'submitted_at' is mandatory", json);
            }

            if (survey.getCollection() == null) {
                logAndThrowError("'collection' section is mandatory", json);
            } else {
                Date period = survey.getCollection().getPeriod();
                if (period == null) {
                    logAndThrowError("collection 'period' is mandatory", json);
                } else {
//                    if (period.length() != 4) {
//                        Audit.getInstance().increment("survey.period.length.not.4.WARNING");
//                        log.warn("SURVEY|PARSE|PERIOD|expected 4 character period, got period: {} length: {}", period, period.length());
//                    }
                }
            }

            if (survey.getMetadata() == null) {
                logAndThrowError("'metadata' section is mandatory", json);
            }

            timer.stopStatus(200);
            log.debug("SURVEY|PARSE|parsed json as {}", survey);
            return survey;

        } catch(JsonParseException e) {
            timer.stopStatus(500);
            String message = "Problem parsing survey from json";
            log.error("SURVEY|PARSE|{} json: {}", message, json, e);
            throw new SurveyParserException(message, json, e);
        } finally {
            Audit.getInstance().increment(timer);
        }
    }

    private void logAndThrowError(String message, String json) throws SurveyParserException {
        log.error("SURVEY|PARSE|{} json: {}", message, json);
        throw new SurveyParserException(message, json);
    }

    public String prettyPrint(Survey survey) {
        return serialize(survey);
    }

    private String serialize(Survey survey) {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(Collection.class, new CollectionSerializer())
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
            "MMyyyy"
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

    private class CollectionSerializer implements JsonSerializer<Collection> {

        @Override
        public JsonElement serialize(Collection collection, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject object = new JsonObject();
            SimpleDateFormat sdf = new SimpleDateFormat("MMyyyy");
            object.add("exercise_sid", new JsonPrimitive(collection.getExerciseSid()));
            object.add("instrument_id", new JsonPrimitive(collection.getInstrumentId()));
            object.add("period", new JsonPrimitive(sdf.format(collection.getPeriod())));

            return object;
        }
    }
}
