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

    /**
     * @return empty String if null, the first 11 characters if the length is 12 and the last character is a letter, otherwise the full String
     */
    public String getRespondentId() {
        if (ruRef == null) {
            return "";
        }

        int length = ruRef.length();
        if (length < 12) {
            return ruRef;
        }

        if (length == 12 && Character.isLetter(ruRef.charAt(11)) ) {
            return ruRef.substring(0, 11);
        }

        return ruRef;
    }

    public char getRespondentCheckLetter() {
        int length = ruRef.length();

        if (ruRef == null || length < 12) {
            return ' ';
        }


        if (length == 12 && Character.isLetter(ruRef.charAt(11)) ) {
            return ruRef.charAt(11);
        }

        return ' ';
    }
}