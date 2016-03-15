package com.github.onsdigital.perkin.json;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Simple object to contain Survey data.
 */
@Data
@Builder
@Deprecated
public class Survey {

    private String id;
    private String name;
    private String formReference;
    private String date;
    private String respondentId;
    private String respondentCheckLetter;

    @Singular("answer")
    private Map<String, String> answers;

    public String getAnswer(String key) {
        if (answers == null) {
            return null;
        }

        return answers.get(key);
    }

    public Set<String> getKeys() {
        if (answers == null) {
            return Collections.emptySet();
        }

        return answers.keySet();
    }
}
