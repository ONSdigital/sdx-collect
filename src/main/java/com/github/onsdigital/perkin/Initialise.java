package com.github.onsdigital.perkin;

import com.github.davidcarboni.restolino.framework.Startup;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Initialise implements Startup {

    @Override
    public void init() {
        log.info(">>>>>>>>>>> init >>>>>>>>>>>>>");

        new SurveyListener().start();
    }
}
