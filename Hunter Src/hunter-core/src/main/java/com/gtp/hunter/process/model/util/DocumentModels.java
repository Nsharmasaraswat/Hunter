package com.gtp.hunter.process.model.util;

import com.gtp.hunter.process.model.DocumentModel;
import com.gtp.hunter.process.model.DocumentModelField;

public class DocumentModels {
	public static DocumentModelField findField(DocumentModel dm, String metaname) {
		assert dm != null;
		return dm.getFields().parallelStream()
						.filter(df -> df != null && df.getMetaname() != null && df.getMetaname().equals(metaname))
						.findAny()
						.orElse(null);
	}
}
