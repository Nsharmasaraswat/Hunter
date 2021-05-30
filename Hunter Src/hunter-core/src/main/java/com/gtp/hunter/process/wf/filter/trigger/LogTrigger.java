package com.gtp.hunter.process.wf.filter.trigger;

import java.lang.annotation.Annotation;

import com.google.gson.GsonBuilder;
import com.gtp.hunter.process.model.FilterTrigger;
import com.gtp.hunter.process.wf.filter.BaseModelEvent;

public class LogTrigger extends BaseTrigger {

	public LogTrigger(FilterTrigger model) {
		super(model);
	}

	@Override
	public boolean execute(BaseModelEvent mdl) {
//		System.out.println("Injection: " + mdl.getMetadata().getInjectionPoint().toString());
//		System.out.println("Type:      " + mdl.getMetadata().getType().toString());
//		System.out.println("Qualifiers:");
		for(Annotation a : mdl.getMetadata().getQualifiers()) {
			System.out.println("           " + a.toString());
		}
		System.out.println("**************************************************************************************************************");
		System.out.println(new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create().toJson(mdl.getModel()));
		System.out.println("**************************************************************************************************************");
		return true;
	}

}
