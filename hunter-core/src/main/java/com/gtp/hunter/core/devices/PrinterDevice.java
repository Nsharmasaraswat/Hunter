package com.gtp.hunter.core.devices;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.gtp.hunter.common.devicedata.DeviceData;
import com.gtp.hunter.common.devicedata.LabelData;
import com.gtp.hunter.common.model.Command;
import com.gtp.hunter.core.model.Device;
import com.gtp.hunter.core.model.Source;
import com.gtp.hunter.core.websocket.ControlSession;
import com.gtp.hunter.ejbcommon.util.ConfigUtil;

import io.reactivex.subjects.PublishSubject;

public class PrinterDevice extends BaseDevice {

	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public PrinterDevice(Source src, Device model, PublishSubject<Command> commands) {
		super(src, model, commands);
	}

	@Override
	protected Command execute(DeviceData dt) {

		return null;
	}

	public Command print(DeviceData dt) {
		Command cmd = super.getBaseCommand();
		LabelData data = (LabelData) dt;

		logger.info(data.getMaskName());
		try {
			Path p = Paths.get(ConfigUtil.get("hunter-core", "mask-folder", "masks")).resolve(data.getMaskName());
			logger.info(p == null ? "null" : p.toFile().getAbsolutePath());
			String label = new String(Files.readAllBytes(p));
			Map<String, String> fields = data.getLabelFields();

			for (String n : fields.keySet()) {
				if (label == null) {
					logger.info("Mascara Vazia? ");
				} else {
					logger.info("Field: " + n + " Value: " + fields.get(n));
					label = label.replace(String.format("${%s}", n), fields.get(n));
				}
			}
			cmd.setPayload(label);
			cmd.setMethod("print");
			long beforeSend = System.currentTimeMillis();
			cmd = sendDirectCommand(cmd, 300);
			// cmd = sendSyncCommand(cmd, 300);
			logger.info("Sent in " + (System.currentTimeMillis() - beforeSend) + "ms");
		} catch (IOException e) {
			cmd.setReturnValue("Mask not found! " + data.getMaskName());
			logger.error("Mask not found: " + Paths.get(".").getFileName().toAbsolutePath().toString());
			logger.trace("Print Error", e);
		}
		return cmd;
	}

	// CHANGE: direct websocket Command
	private Command sendDirectCommand(Command cmd, long timeout) {
		ControlSession s = this.getModel().getSource().getControlSession();
		Gson gs = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

		if (s != null) {
			synchronized (s) {
				if (s.isOnline()) {
					try {
						s.getRawSession().getBasicRemote().sendText(gs.toJson(cmd));
						s.wait(timeout * 1000);
						if (s.getResponse() != null) {
							cmd = s.getResponse();
						} else {
							cmd.setReturnValue("false");
						}
					} catch (IOException e) {
						cmd.setReturnValue(e.getLocalizedMessage());
					} catch (InterruptedException e) {
						cmd.setReturnValue("timeout");
					}
				} else {
					cmd.setReturnValue("false");
				}
			}
		} else
			cmd.setReturnValue("Impressora não conectada");
		return cmd;
	}
}
