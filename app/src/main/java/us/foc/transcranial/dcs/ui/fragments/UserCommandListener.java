package us.foc.transcranial.dcs.ui.fragments;

import us.foc.transcranial.dcs.bluetooth.ConnectionStatus;
import us.foc.transcranial.dcs.model.ProgramEntity;

public interface UserCommandListener {

    void onScanForDevices();

    void onPlayProgram(ProgramEntity programEntity);

    void onStopProgram();

    void onReplayProgram();

    ConnectionStatus getConnectionStatus();
}
