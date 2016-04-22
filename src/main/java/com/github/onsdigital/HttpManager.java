package com.github.onsdigital;

import com.github.onsdigital.perkin.helper.Http;

/**
 * Created by ian on 04/04/2016.
 */
public class HttpManager {
    private static Http INSTANCE = new Http();

    private HttpManager() {
        //use getInstance()
    }

    public static Http getInstance() {
        return INSTANCE;
    }
    public static void setInstance(Http manager) {
        INSTANCE = manager;
    }
}
