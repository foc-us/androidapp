package us.foc.transcranial.dcs.model.events;

/**
 * Posted when the navbar has been clicked to page forwards/backwards
 */
public class NavbarClickEvent {

    private final boolean pagedForwards;

    public NavbarClickEvent(boolean pagedForwards) {
        this.pagedForwards = pagedForwards;
    }

    public boolean isPagedForwards() {
        return pagedForwards;
    }

}
