package com.github.onsdigital.perkin.json;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Status {

    private String status;
    private String message;
}
