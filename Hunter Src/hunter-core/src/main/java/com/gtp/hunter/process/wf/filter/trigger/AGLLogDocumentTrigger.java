package com.gtp.hunter.process.wf.filter.trigger;

import java.lang.invoke.MethodHandles;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.FilterTrigger;
import com.gtp.hunter.process.wf.filter.BaseModelEvent;

public class AGLLogDocumentTrigger extends BaseTrigger {

	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public AGLLogDocumentTrigger(FilterTrigger model) {
		super(model);
	}

	@Override
	public boolean execute(BaseModelEvent mdl) {
		Document d = (Document) mdl.getModel();
		StringBuilder sb = new StringBuilder("LogTrigger:\r\n");

		if (d != null) {
			sb.append(resumeDocument(d, 1));
			logger.info(sb.toString());
		} else
			logger.error("Should not be null - " + mdl.getClass().getCanonicalName());
		return true;
	}

	private StringBuilder resumeDocument(Document d, int spacing) {
		StringBuilder sb = new StringBuilder();
		sb.append(String.join("", Collections.nCopies(spacing, "\t")));
		sb.append("id: ");
		sb.append(d.getId());
		sb.append("\r\n");
		sb.append(String.join("", Collections.nCopies(spacing, "\t")));
		sb.append("Code: ");
		sb.append(d.getCode());
		sb.append("\r\n");
		sb.append(String.join("", Collections.nCopies(spacing, "\t")));
		sb.append("Status: ");
		sb.append(d.getStatus());
		sb.append("\r\n");
		sb.append(String.join("", Collections.nCopies(spacing, "\t")));
		sb.append("Metaname: ");
		sb.append(d.getMetaname());
		sb.append("\r\n");
		sb.append(String.join("", Collections.nCopies(spacing, "\t")));
		sb.append("Siblings: ");
		sb.append(d.getSiblings().size());
		sb.append("\r\n");
		for (Document ds : d.getSiblings()) {
			sb.append(resumeDocument(ds, spacing + 2));
			sb.append("\r\n");
		}
		sb.append(String.join("", Collections.nCopies(spacing, "\t")));
		sb.append("Transports: ");
		sb.append(d.getTransports().size());
		sb.append("\r\n");
		sb.append(String.join("", Collections.nCopies(spacing, "\t")));
		sb.append("Things: ");
		sb.append(d.getThings().size());
		sb.append("\r\n");
		sb.append(String.join("", Collections.nCopies(spacing, "\t")));
		sb.append("Fields: ");
		sb.append(d.getFields().size());
		sb.append("\r\n");
		sb.append(String.join("", Collections.nCopies(spacing, "\t")));
		sb.append("Items: ");
		sb.append(d.getItems().size());
		return sb;
	}

}