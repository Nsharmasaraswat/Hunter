package com.gtp.hunter.rfid.asciiprotocol.responders;

import com.gtp.hunter.rfid.asciiprotocol.enumerations.SwitchState;

public interface ISwitchStateReceivedDelegate {

    /**
     * Delegate method invoked for each switch state notification received
     * <p>
     * Note: This method will be called on a non-UI thread
     *
     * @param state the new switch state
     */
    public void switchStateReceived(SwitchState state);
}
