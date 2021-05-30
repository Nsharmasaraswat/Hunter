package com.gtp.hunter.rfid.asciiprotocol.responders;

public interface IBarcodeReceivedDelegate {
    /// This method will be called on a non-UI thread

    /**
     * Delegate method invoked for each barcode received
     * <p>
     * Note: This method will be called on a non-UI thread
     *
     * @param barcode The barcode scanned as a String
     */
    public void barcodeReceived(String barcode);

}
