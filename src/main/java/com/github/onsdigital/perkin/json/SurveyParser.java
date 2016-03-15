package com.github.onsdigital.perkin.json;

import com.google.gson.*;
import lombok.extern.slf4j.Slf4j;

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
                String message = "Unsupported version (0.0.1 supported), while parsing survey from json: " + json;
                log.error("SURVEY|PARSE|" + message);
                throw new SurveyParserException(message);
            }

            if (log.isDebugEnabled()) {
                log.debug("SURVEY|PARSE|parsed json as {}", survey);
            }
            return survey;

        } catch(JsonParseException e) {
            String message = "Problem parsing survey from json " + json;
            log.error("SURVEY|PARSE|{}", message, e);
            throw new SurveyParserException(message, json, e);
        }
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

    private class CollectionSerializer implements JsonSerializer<Collection> {

        @Override
        public JsonElement serialize(Collection collection, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject object = new JsonObject();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            object.add("exercise_sid", new JsonPrimitive(collection.getExerciseSid()));
            object.add("instrument_id", new JsonPrimitive(collection.getInstrumentId()));
            object.add("period", new JsonPrimitive(sdf.format(collection.getPeriod())));

            return object;
        }
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
