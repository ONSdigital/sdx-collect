package com.github.onsdigital.perkin.json;

import lombok.Builder;
import lombok.Data;

/**
 * Simple object to contain encrypted data.
 */
@Data
@Builder
public class EncryptedPayload {

    private String contents;
}
