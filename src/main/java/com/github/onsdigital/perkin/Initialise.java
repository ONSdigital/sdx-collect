package com.github.onsdigital.perkin;

import com.github.davidcarboni.restolino.framework.Startup;

/**
 * Example initialisation task.
 * Take a look at the {@code com.github.davidcarboni.restolino.framework} package for more useful interfaces/classes.
 */
public class Initialise implements Startup {
    @Override
    public void init() {
        System.out.println(" >>>>>>>>>>> This is a startup task.");
    }
}
