package com.github.onsdigital.perkin.json;

import lombok.Builder;
import lombok.Data;

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
}
