package com.gtp.hunter.process.wf.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;

import com.gtp.hunter.core.model.User;
import com.gtp.hunter.core.util.JsonUtil;
import com.gtp.hunter.process.model.Action;
import com.gtp.hunter.process.model.DocumentField;
import com.gtp.hunter.process.model.DocumentModelField;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.stream.RegisterStreamManager;

public class AttachUnitProcessAction extends BaseAction {

	@Inject
	private static transient Logger	logger;

	private Map<String, Object>		params	= new HashMap<String, Object>();

	public AttachUnitProcessAction(User usr, Action action, RegisterStreamManager rsm, RegisterService regSvc) {
		super(usr, action, rsm, regSvc);
		this.params = JsonUtil.jsonToMap(action.getParams());
	}

	@Override
	public String execute(Action t) throws Exception {
		DocumentModelField dmf = getRegSvc().getDmfSvc().findByMetaname(this.params.get("docproperty").toString().replaceAll("\"", ""));
		logger.debug("UNIT PARA ACTION: " + getUser().getProperties().get(this.params.get("usrproperty").toString().replaceAll("\"", "")));
		List<DocumentField> itns = this.getRegSvc().getDfSvc().listByDocumentId(UUID.fromString(this.params.get("docid").toString().replaceAll("\"", "")));
		for (DocumentField df : itns) {
			logger.debug("FIELD: " + df.getField().getMetaname());
			logger.debug("VALUE: " + df.getValue());
			if ((df.getField().getMetaname().equals("UNIT")) && (!df.getValue().equals(getUser().getProperties().get(this.params.get("usrproperty").toString().replaceAll("\"", ""))))) {
				//				throw new Exception("Documento bloqueado"); 
				return null;
			}
		}
		this.getRegSvc().getDfSvc().quickRemoveDocumentField(dmf.getId(), getUser().getProperties().get(this.params.get("usrproperty").toString().replaceAll("\"", "")));
		this.getRegSvc().getDfSvc().quickInsertDocumentField(UUID.fromString(this.params.get("docid").toString().replaceAll("\"", "")), dmf.getId(), getUser().getProperties().get(this.params.get("usrproperty").toString().replaceAll("\"", "")));
		return t.getRoute();
	}
}
