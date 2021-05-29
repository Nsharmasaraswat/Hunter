package com.gtp.hunter.process.wf.taskdef;

import java.io.StringReader;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.core.model.User;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.TaskDef;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.stream.RegisterStreamManager;
import com.gtp.hunter.ui.json.ViewTaskStub;

public class DocByModelAndStatusFieldParam extends BaseTaskDef {

	private transient static final Logger	logger	= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private String							taskDefMeta;

	public DocByModelAndStatusFieldParam(TaskDef def, RegisterService regSvc, RegisterStreamManager rsm, User usr) {
		super(def, regSvc, rsm, usr);
		this.taskDefMeta = def.getMetaname() == null ? def.getId().toString() : def.getMetaname();
	}

	@Override
	protected List<Document> listQuickDocuments(User usr) {
		JsonReader jsonReader = Json.createReader(new StringReader(getDef().getDecParam()));
		JsonObject object = jsonReader.readObject();
		String meta = object.getString("field-meta");
		String value = object.getString("field-value");
		List<Document> lst = getRegSvc().getDcSvc().quickListByTypeStatusFieldValue(getDef().getModel().getMetaname(), getDef().getState(), meta, value);

		jsonReader.close();
		logger.info(taskDefMeta + ": " + lst.size() + " -> " + lst.stream().map(d -> d.getCode()).collect(Collectors.joining(", ")));
		return lst;
	}

	@Override
	protected List<ViewTaskStub> validateTask(Document d) {
		List<Document> ret = new ArrayList<Document>();

		if (d.getModel().getMetaname().equals(getDef().getModel().getMetaname())) {
			if (d.getStatus().equals(getDef().getState())) {
				JsonReader jsonReader = Json.createReader(new StringReader(getDef().getDecParam()));
				JsonObject object = jsonReader.readObject();
				String meta = object.getString("field-meta");
				String value = object.getString("field-value");

				jsonReader.close();
				if (d.getFields().stream().anyMatch(df -> df.getField().getMetaname().equals(meta) && df.getValue().equals(value)))
					ret.add(d);
			}
		}
		return generateTasks(ret);
	}

}
