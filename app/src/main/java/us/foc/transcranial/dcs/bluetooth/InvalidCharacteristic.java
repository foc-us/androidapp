package us.foc.transcranial.dcs.bluetooth;

import java.util.UUID;

public class InvalidCharacteristic extends Exception {

    private final UUID serviceUUID;
    private final UUID characteristicUUID;

    InvalidCharacteristic(UUID serviceUUID, UUID characteristicUUID) {

        this.serviceUUID = serviceUUID;
        this.characteristicUUID = characteristicUUID;
    }

    @Override
    public String toString() {
        return serviceUUID.toString() +
                (characteristicUUID != null ? " - " + characteristicUUID.toString() : "");
    }
}
