package us.foc.transcranial.dcs.common;

import com.squareup.otto.Bus;

/**
 * Provides an instance of the Otto Event Bus
 */
public class BusProvider {

    private static final Bus BUS = new Bus();

    public static Bus instance() {
        return BUS;
    }
}
