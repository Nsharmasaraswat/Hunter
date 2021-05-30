//----------------------------------------------------------------------------------------------
// Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved.
//----------------------------------------------------------------------------------------------

package com.gtp.hunter.wms.inventory;

import android.util.Log;

import com.gtp.hunter.HunterMobileWMS;
import com.gtp.hunter.rfid.RFIDModelBase;
import com.gtp.hunter.rfid.asciiprotocol.commands.BarcodeCommand;
import com.gtp.hunter.rfid.asciiprotocol.commands.FactoryDefaultsCommand;
import com.gtp.hunter.rfid.asciiprotocol.commands.InventoryCommand;
import com.gtp.hunter.rfid.asciiprotocol.enumerations.TriState;
import com.gtp.hunter.rfid.asciiprotocol.responders.ICommandResponseLifecycleDelegate;
import com.gtp.hunter.rfid.asciiprotocol.responders.ITransponderReceivedDelegate;
import com.gtp.hunter.rfid.asciiprotocol.responders.TransponderData;
import com.gtp.hunter.rfid.utils.HexEncoding;

import java.util.Locale;

public class InventoryRFIDModel extends RFIDModelBase {

    // Control
    private boolean mAnyTagSeen;
    private boolean mEnabled;

    public boolean enabled() {
        return mEnabled;
    }

    public void setEnabled(boolean state) {
        if (HunterMobileWMS.isRFIDAvailable()) {
            boolean oldState = mEnabled;
            mEnabled = state;

            // Update the commander for state changes
            if (oldState != state) {
                if (mEnabled) {
                    // Listen for transponders
                    getCommander().addResponder(mInventoryResponder);
                    // Listen for barcodes
                    getCommander().addResponder(mBarcodeResponder);
                } else {
                    // Stop listening for transponders
                    getCommander().removeResponder(mInventoryResponder);
                    // Stop listening for barcodes
                    getCommander().removeResponder(mBarcodeResponder);
                }

            }
        } else
            mEnabled = false;
    }

    // The command to use as a responder to capture incoming inventory responses
    private final InventoryCommand mInventoryResponder;
    // The command used to issue commands
    private final InventoryCommand mInventoryCommand;

    // The command to use as a responder to capture incoming barcode responses
    private final BarcodeCommand mBarcodeResponder;

    // The inventory command configuration
    public InventoryCommand getCommand() {
        return mInventoryCommand;
    }

    public InventoryRFIDModel() {
        // This is the command that will be used to perform configuration changes and inventories
        mInventoryCommand = new InventoryCommand();

        // Configure the type of inventory
        mInventoryCommand.setIncludeTransponderRssi(TriState.YES);
        mInventoryCommand.setIncludeChecksum(TriState.YES);
        mInventoryCommand.setIncludePC(TriState.YES);

        // Use an InventoryCommand as a responder to capture all incoming inventory responses
        mInventoryResponder = new InventoryCommand();

        // Also capture the responses that were not from App commands
        mInventoryResponder.setCaptureNonLibraryResponses(true);

        // Notify when each transponder is seen
        mInventoryResponder.setTransponderReceivedDelegate(new ITransponderReceivedDelegate() {

            int mTagsSeen = 0;

            @Override
            public void transponderReceived(TransponderData transponder, boolean moreAvailable) {
                mAnyTagSeen = true;

                String tidMessage = transponder.getTidData() == null ? "" : HexEncoding.bytesToString(transponder.getTidData());
                String infoMsg = String.format(Locale.US, "\nRSSI: %d  PC: %04X  CRC: %04X", transponder.getRssi(), transponder.getPc(), transponder.getCrc());
                sendMessageNotification("EPC: " + transponder.getEpc() + infoMsg + "\nTID: " + tidMessage);

                mTagsSeen++;
                if (!moreAvailable) {
                    sendMessageNotification("");
                    Log.d("TagCount", String.format("Tags seen: %s", mTagsSeen));
                }
            }
        });

        mInventoryResponder.setResponseLifecycleDelegate(new ICommandResponseLifecycleDelegate() {

            @Override
            public void responseEnded() {
                if (!mAnyTagSeen) {
                    sendMessageNotification("No transponders seen");
                }
            }

            @Override
            public void responseBegan() {
                mAnyTagSeen = false;
            }
        });

        // This command is used to capture barcode responses
        mBarcodeResponder = new BarcodeCommand();
        mBarcodeResponder.setCaptureNonLibraryResponses(true);
        mBarcodeResponder.setUseEscapeCharacter(TriState.YES);
        mBarcodeResponder.setBarcodeReceivedDelegate(barcode -> sendMessageNotification("BC: " + barcode));


    }

    //
    // Reset the reader configuration for the switch actions, inventory and barcode commands
    //
    public void resetDevice() {
        if (HunterMobileWMS.isRFIDAvailable() && getCommander().isConnected()) {
            getCommander().executeCommand(new FactoryDefaultsCommand());
        }
    }

    //
    // Update the reader configuration from the command
    // Call this after each change to the model's command
    //
    public void updateConfiguration() {
        if (HunterMobileWMS.isRFIDAvailable() && getCommander().isConnected()) {
            mInventoryCommand.setTakeNoAction(TriState.YES);
            getCommander().executeCommand(mInventoryCommand);
        }
    }

    //
    // Perform an inventory scan with the current command parameters
    //
    public void scan() {
        testForAntenna();
        if (HunterMobileWMS.isRFIDAvailable() && getCommander().isConnected()) {
            mInventoryCommand.setTakeNoAction(TriState.NO);
            getCommander().executeCommand(mInventoryCommand);
        }
    }


    //
    // Test for the presence of the antenna
    //
    private void testForAntenna() {
        if (HunterMobileWMS.isRFIDAvailable() && getCommander().isConnected()) {
            InventoryCommand testCommand = InventoryCommand.synchronousCommand();
            testCommand.setTakeNoAction(TriState.YES);
            getCommander().executeCommand(testCommand);
            if (!testCommand.isSuccessful()) {
                sendMessageNotification("ER:Error! Code: " + testCommand.getErrorCode() + " " + testCommand.getMessages().toString());
            }
        }
    }
}
