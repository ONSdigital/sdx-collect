package com.github.onsdigital.perkin.json;

import lombok.Builder;
import lombok.Data;

/**
 * An IDBR receipt records that a respondent unit (RU) has completed a survey (id)
 * on a particular date.
 */
@Data
@Builder
public class IdbrReceipt {

    private String receipt;
    private String filename;
}
