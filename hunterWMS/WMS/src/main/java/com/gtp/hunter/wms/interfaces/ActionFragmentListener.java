package com.gtp.hunter.wms.interfaces;

import com.gtp.hunter.wms.model.AGLDocument;
import com.gtp.hunter.wms.model.AGLDocumentProps;
import com.gtp.hunter.wms.model.Document;
import com.gtp.hunter.wms.model.IntegrationReturn;
import com.gtp.hunter.wms.model.PrintPayload;
import com.gtp.hunter.wms.model.Thing;

/**
 * Interface that allows parent activity retrieve information from
 * dialog.
 */
public interface ActionFragmentListener {
    boolean sendThing(final Thing t);

    IntegrationReturn sendDocument(final Document doc);

    void lockUI();

    boolean sendAGLDocumentProps(final AGLDocumentProps doc);

    boolean sendAGLDocument(final AGLDocument doc);

    boolean sendPrintFromConference(PrintPayload print);

    void cancelTask();

    void sendMessageNotification(String msg, long interval);

    boolean isRfidDeviceConnected();

    void reconnectActionWS();

    void connectRfidDevice();

    void reconnectRfidDevice();

    void disconnectRfidDevice();

    void resetRfidDevice();

    void returnFromFragment();

    void updateRFID();
}
