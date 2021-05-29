package com.gtp.hunter.process.wf.process.activity;

import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import javax.json.JsonNumber;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.common.enums.UnitType;
import com.gtp.hunter.core.devices.location.UbeeLocationDevice;
import com.gtp.hunter.core.model.ComplexData;
import com.gtp.hunter.core.model.Unit;
import com.gtp.hunter.core.util.JsonUtil;
import com.gtp.hunter.process.model.ProcessActivity;
import com.gtp.hunter.process.model.Product;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.process.wf.origin.BaseOrigin;
import com.gtp.hunter.process.wf.origin.DeviceOrigin;
import com.gtp.hunter.process.wf.process.BaseProcess;

public class UbeeCommandProcessActivity extends BaseProcessActivity<ComplexData, Thing> {

	private final Logger		logger	= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private Map<String, Object>	params;

	public UbeeCommandProcessActivity(ProcessActivity model, BaseOrigin origin) {
		super(model, origin);
		this.params = JsonUtil.jsonToMap(model.getParam());
	}

	@Override
	public ProcessActivityExecuteReturn executePostConstruct() {
		return ProcessActivityExecuteReturn.OK;
	}

	@Override
	public ProcessActivityExecuteReturn executePreTransform(ComplexData arg) {
		return ProcessActivityExecuteReturn.OK;
	}

	@Override
	public ProcessActivityExecuteReturn execute(Thing t) {
		if (t != null) {
			try {
				String addressMeta = (String) this.params.get("address-metaname");
				String firstCallStatus = (String) this.params.get("first-call-status");
				int callAttempts = ((JsonNumber) this.params.get("call-attempts")).intValue();
				Unit tag = t.getUnitModel().stream().filter(u -> u.getType() == UnitType.valueOf((String) this.params.get("unit-type"))).findFirst().get();
				int recallTime = ((JsonNumber) this.params.get("recall-timeout")).intValue();
				UbeeLocationDevice dev = (UbeeLocationDevice) ((DeviceOrigin) getOrigin()).getDevices().get((String) params.get("device-meta"));

				if (t.getAddress() != null && t.getAddress().getMetaname().equalsIgnoreCase(addressMeta)) {
					if (t.getStatus().toUpperCase().startsWith(firstCallStatus.toUpperCase())) {
						int attempt = t.getStatus().equalsIgnoreCase(firstCallStatus) ? 0 : Integer.parseInt(t.getStatus().toUpperCase().replace(firstCallStatus.toUpperCase().trim(), ""));
						boolean performActions = attempt == 0 || System.currentTimeMillis() - t.getUpdatedAt().getTime() > recallTime;

						if (performActions) {
							if (attempt <= callAttempts) {
								if (this.params.containsKey("send-text")) {
									String line1 = (String) this.params.get("text-line1");
									String line2 = (String) this.params.get("text-line2");
									int textTime = ((JsonNumber) this.params.get("send-text")).intValue();

									dev.sendTextMessage(Integer.parseInt(tag.getTagId()), line1, line2, textTime);
									t.setStatus((String) this.params.get("status-tag-called"));
								}
								if (this.params.containsKey("light")) {
									int lightTime = ((JsonNumber) this.params.get("light")).intValue();

									dev.led(Integer.parseInt(tag.getTagId()), true, lightTime);
									t.setStatus((String) this.params.get("status-tag-called"));
								}
								if (this.params.containsKey("buzz")) {
									int buzzTime = ((JsonNumber) this.params.get("buzz")).intValue();

									dev.buzz(Integer.parseInt(tag.getTagId()), true, buzzTime);
									t.setStatus((String) this.params.get("status-tag-called"));
								}
							} else
								logger.info("NO CALL - ATTEMPTS");
						} else
							logger.info("NO CALL - NO PERFORM");
					} else
						logger.info("NO CALL - STATUS");
				} else
					logger.info("NO CALL - ADDRESS");
			} catch (NoSuchElementException nsee) {
				return ProcessActivityExecuteReturn.NOK;
			}
		}
		return ProcessActivityExecuteReturn.OK;
	}

	@Override
	public ProcessActivityExecuteReturn execute(BaseProcess p) {
		return ProcessActivityExecuteReturn.OK;
	}

	@Override
	public Thing executeUnknown(ComplexData arg) {
		Thing t = new Thing("", new Product("ITEM DESCONHECIDO", null, "", "CANCELADO"), null, (String) this.params.get("sku"));
		try {
			StringBuilder sb = new StringBuilder(arg.getTagId());
			sb.insert(8, "-");
			sb.insert(12, "-");
			sb.insert(16, "-");
			sb.insert(20, "-");
			t.setId(UUID.fromString(sb.toString()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		t.getUnitModel().add(new Unit(arg.getTagId(), null));
		return t;
	}

	@Override
	public ProcessActivityExecuteReturn execute() {
		return ProcessActivityExecuteReturn.OK;
	}

}
