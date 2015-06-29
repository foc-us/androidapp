package us.foc.transcranial.dcs.model.events;

/**
 * Posted when the navbar requires invalidation
 */
public class NavbarUpdateEvent {

    private final boolean displayPrevious;
    private final boolean displayNext;

    public NavbarUpdateEvent(boolean displayPrevious, boolean displayNext) {
        this.displayPrevious = displayPrevious;
        this.displayNext = displayNext;

    }

    public boolean isDisplayPrevious() {
        return displayPrevious;
    }

    public boolean isDisplayNext() {
        return displayNext;
    }

}
