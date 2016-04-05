package com.github.onsdigital.perkin.services;

import com.github.davidcarboni.httpino.Response;
import org.apache.http.StatusLine;
import org.apache.poi.ss.formula.functions.T;

/**
 * Created by ian on 04/04/2016.
 */
public class MockedResponse<T> extends Response{
    public MockedResponse(StatusLine sl, T body) {
        super(null, null);
        this.statusLine = sl;
        this.body = body;
    }
}
