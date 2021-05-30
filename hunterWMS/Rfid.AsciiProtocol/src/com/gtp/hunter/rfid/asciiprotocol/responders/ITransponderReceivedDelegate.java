package com.gtp.hunter.rfid.asciiprotocol.responders;

public interface ITransponderReceivedDelegate {
    /**
     * Delegate method invoked for each transponder received
     * <p>
     * Note: This method will be called on a non-UI thread
     *
     * @param transponder   a transponder response from an Inventory, read or write command
     * @param moreAvailable true if there are more transponders
     */
    public void transponderReceived(TransponderData transponder, boolean moreAvailable);

}
