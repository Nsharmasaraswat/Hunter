package com.gtp.hunter.process.wf.taskdecorator.solar;

import java.lang.invoke.MethodHandles;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.common.enums.UnitType;
import com.gtp.hunter.common.util.Profiler;
import com.gtp.hunter.core.model.Unit;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.process.model.util.Documents;
import com.gtp.hunter.process.model.util.Things;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.wf.taskdecorator.BaseTaskDecorator;

public class SealDecorator extends BaseTaskDecorator {

	private transient static final Logger	logger	= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private Document						transport;

	public SealDecorator(String params, RegisterService rSvc) {
		super(params, rSvc);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String decorateName(Document d1) {
		StringBuilder sb = new StringBuilder();
		Profiler prof = new Profiler("TaskManager");

		transport = getRegSvc().getDcSvc().findParent(d1);
		String obs = Documents.getStringField(transport, "OBS", "");

		if (transport != null && transport.getPerson() != null)
			sb.append(transport.getPerson().getName());
		else if (obs != null && !obs.isEmpty()) {
			sb.append(obs);
		}
		prof.done("Name Decorated", false, false).forEach(logger::debug);
		return sb.toString().trim();
	}

	@Override
	public String decorateContent(Document d1) {
		Profiler prof = new Profiler("TaskManager");
		StringBuilder sb = new StringBuilder();
		String sTruckId = Documents.getStringField(transport, "TRUCK_ID");

		if (!sTruckId.isEmpty()) {
			Thing t = getRegSvc().getThSvc().findById(UUID.fromString(sTruckId));

			for (UUID u : t.getUnits()) {
				Unit ut = getRegSvc().getUnSvc().findById(u);

				if (ut != null) {
					if (ut.getType().equals(UnitType.EXTERNAL_SYSTEM) && ut.getName() != null && ut.getName().equals("Realpicking")) {
						sb = new StringBuilder("VEÍCULO: ");
						sb.append(ut.getTagId());
						break;
					}
				}
			}
			if (sb.length() == 0)
				sb.append(Things.getStringProperty(t, "CARRIER", ""));
		}

		prof.done("Name Decorated", false, false).forEach(logger::info);
		return sb.toString().trim();
	}
}
