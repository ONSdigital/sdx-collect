package com.github.onsdigital.perkin.json;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Collection {

    @SerializedName("exercise_sid")
    private String exerciseSid;
}