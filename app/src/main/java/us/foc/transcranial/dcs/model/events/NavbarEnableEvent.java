package us.foc.transcranial.dcs.model.events;

/**
 * Posted when the navbar requires enabling/disabling.
 */
public class NavbarEnableEvent {

    private final boolean enabled;

    public NavbarEnableEvent(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
