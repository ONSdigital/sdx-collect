package com.github.onsdigital.perkin.api;

import com.github.davidcarboni.restolino.framework.Api;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import java.io.IOException;

@Api
public class Survey {

    @GET
    public com.github.onsdigital.perkin.json.Survey get(HttpServletRequest request, HttpServletResponse response) throws IOException {

        return com.github.onsdigital.perkin.json.Survey.builder()
                .id("id")
                .name("name")
                .respondentId("respondentId")
                .date("01 Oct 2014")
                .respondentCheckLetter("A")

                .answer("1", "y")
                .answer("11", "y")
                .answer("20", "n")
                .answer("30", "y")
                .answer("40", "700")
                .answer("50", "311008")
                .answer("70", "74")
                .answer("90", "74")
                .answer("100", "some comment")

                .build();
    }
}
