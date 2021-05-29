package com.gtp.hunter.process.wf.filter.trigger;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.json.JsonNumber;

import com.gtp.hunter.core.model.BaseModel;
import com.gtp.hunter.ejbcommon.json.IntegrationReturn;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.FilterTrigger;
import com.gtp.hunter.process.model.Product;
import com.gtp.hunter.process.wf.filter.BaseModelEvent;

@SuppressWarnings("rawtypes")
public abstract class WMSBaseUpdate extends BaseTrigger {
	ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);

	public WMSBaseUpdate(FilterTrigger model) {
		super(model);
	}

	@Override
	public boolean execute(BaseModelEvent mdl) {
		BaseModel model = executeImpl(mdl);
		int delay = ((JsonNumber) getParams().get("send-delay")).intValue();

		if (model instanceof Product) {
			exec.schedule(() -> {
				try {
					IntegrationReturn iRet = mdl.getRegSvc().getAglSvc().sendProductToWMS((Product) model, "POST").get();

					if (!iRet.isResult()) {
						mdl.getRegSvc().getAglSvc().sendProductToWMS((Product) model, "PUT");
					}
				} catch (InterruptedException | ExecutionException ie) {
					ie.printStackTrace();
				}
			}, delay, TimeUnit.MILLISECONDS);
		} else if (model instanceof Document) {
			Document d = (Document) model;
			mdl.getRegSvc().getAglSvc().sendDocToWMS(d, (String) getParams().get("http-method"));
		}
		return true;
	}

	protected abstract BaseModel executeImpl(BaseModelEvent mdl);
}