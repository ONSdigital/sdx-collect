package com.github.onsdigital.perkin.json;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Metadata {

    @SerializedName("user_id")
    private String userId;
    @SerializedName("ru_ref")
    private String ruRef;
}