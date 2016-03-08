package com.github.onsdigital.perkin.json;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.Map;

/**
 * Simple object to contain Survey data.
 */
@Data
@Builder
public class Survey {

    private String id;
    private String name;
    private String date;
    private String respondentId;
    private String respondentCheckLetter;

    @Singular("answer")
    private Map<String, String> answers;

    public String getAnswer(String key) {
        return answers.get(key);
    }
}
