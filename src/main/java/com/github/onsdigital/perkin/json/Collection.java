package com.github.onsdigital.perkin.json;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * Period - will be passed in, can be 2, 4, 6 chars, we will insert into downstream system
 * what we are given.
 */
@Data
@Builder
public class Collection {

    @SerializedName("exercise_sid")
    private String exerciseSid;
    @SerializedName("instrument_id")
    private String instrumentId;
    private String period;
}