package com.gtp.hunter.process.wf.process;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;

import com.gtp.hunter.core.model.ComplexData;
import com.gtp.hunter.process.model.Thing;
import com.gtp.hunter.ui.json.process.BaseProcessMessage;

public class StatusChangeProcess extends OriginProcess {

	@Inject
	private static transient Logger	logger;

	private List<Thing>				tList	= new ArrayList<Thing>();

	@Override
	public void onInit() {
		// TODO: remove after parametrizing BaseSingleProcess
		this.autoValidate = false;
	}

	@Override
	protected void processAfter(Thing t) {
		if (t.getStatus().equals(getModel().getEstadoDe())) {
			if (!this.tList.contains(t)) {
				logger.debug("Mudando o status do thing de " + getModel().getEstadoDe() + " para " + getModel().getEstadoPara());
				resend(t);
			} else
				logger.debug("Item já processado");
		} else {
			logger.debug("Item em estado diferente");
			t.getErrors().add(t.getStatus());
			resend(t);
		}
		this.tList.add(t);
	}

	@Override
	protected void success() {
		ArrayList<Thing> param = new ArrayList<>();

		tList.stream().filter(t -> t.getErrors().isEmpty()).forEach(t -> {
			t.setStatus(getModel().getEstadoPara());
			persistThing(t);
			param.add(t);
		});
		getParametros().put("itens", param);
	}

	@Override
	protected void failure() {

	}

	@Override
	protected void connect() {
		logger.debug("Enviando Lista: " + this.tList.size());
		this.tList.stream().forEach(t -> resend(t));
	}

	@Override
	public void message(BaseProcessMessage msg) {
		logger.info("Message Received: " + msg.toString());
	}

	@Override
	protected void processBefore(ComplexData rd) {

	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub
	}
}
