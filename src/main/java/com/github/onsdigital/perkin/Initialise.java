package com.github.onsdigital.perkin;

import com.github.davidcarboni.restolino.framework.Startup;

public class Initialise implements Startup {

    @Override
    public void init() {
        System.out.println(" >>>>>>>>>>> init >>>>>>>>>>>>> start listening for surveys...");

        new SurveyListener().start();
    }
}
