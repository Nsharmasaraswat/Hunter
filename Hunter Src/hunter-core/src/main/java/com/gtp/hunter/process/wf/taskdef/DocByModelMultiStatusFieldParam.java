package com.gtp.hunter.process.wf.taskdef;

import java.io.StringReader;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.core.model.User;
import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.TaskDef;
import com.gtp.hunter.process.service.RegisterService;
import com.gtp.hunter.process.stream.RegisterStreamManager;
import com.gtp.hunter.ui.json.ViewTaskStub;

public class DocByModelMultiStatusFieldParam extends BaseTaskDef {

	private transient static final Logger	logger	= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private List<String>					statusList;
	private String							taskDefMeta;
	private String							meta;
	private String							value;

	public DocByModelMultiStatusFieldParam(TaskDef def, RegisterService regSvc, RegisterStreamManager rsm, User usr) {
		super(def, regSvc, rsm, usr);
		this.taskDefMeta = def.getMetaname() == null ? def.getId().toString() : def.getMetaname();
		JsonReader jsonReader = Json.createReader(new StringReader(def.getDecParam()));
		JsonObject object = jsonReader.readObject();
		JsonArray status = object.get("status-list").asJsonArray();

		statusList = new ArrayList<>();
		for (JsonValue jsonValue : status)
			statusList.add(((JsonString) jsonValue).getString());
		meta = object.getString("field-meta");
		value = object.getString("field-value");
		jsonReader.close();
	}

	@Override
	protected List<Document> listQuickDocuments(User usr) {
		List<Document> lst = getRegSvc().getDcSvc().quickListByTypeStatusListFieldValue(getDef().getModel().getMetaname(), statusList, meta, value);
		logger.info(taskDefMeta + ": " + lst.size() + " -> " + lst.stream().map(d -> d.getCode()).collect(Collectors.joining(", ")));
		return lst;
	}

	@Override
	protected List<ViewTaskStub> validateTask(Document d) {
		List<Document> ret = new ArrayList<Document>();

		if (d.getModel().getMetaname().equals(getDef().getModel().getMetaname())) {
			if (statusList.contains(d.getStatus()) && d.getFields().parallelStream().anyMatch(df -> df.getField().getMetaname().equals(meta) && df.getValue().equals(value)))
				ret.add(d);
		}
		return generateTasks(ret);
	}

}
