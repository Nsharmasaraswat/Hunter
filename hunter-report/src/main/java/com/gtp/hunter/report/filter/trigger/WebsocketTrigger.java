package com.gtp.hunter.report.filter.trigger;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.FilterTrigger;
import com.gtp.hunter.process.wf.filter.BaseModelEvent;
import com.gtp.hunter.process.wf.filter.trigger.BaseTrigger;
import com.gtp.hunter.report.websocket.session.DashboardSession;

public class WebsocketTrigger extends BaseTrigger {

	private transient static final Logger	logger	= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private DashboardSession				ds;
	private static final Gson				gs		= new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

	public WebsocketTrigger(FilterTrigger model) {
		super(model);
	}

	public WebsocketTrigger(DashboardSession ds) {
		super(new FilterTrigger());
		this.ds = ds;
	}

	@Override
	public boolean execute(BaseModelEvent mdl) {
		Document doc = (Document) mdl.getModel();

		logger.info("New Document On Report: " + doc.getCode());
		try {
			ds.getBaseSession().getBasicRemote().sendText(gs.toJson(doc));
		} catch (IOException e) {
			ds.close(new CloseReason(CloseCodes.CLOSED_ABNORMALLY, e.getLocalizedMessage()));
		}
		return true;
	}
}