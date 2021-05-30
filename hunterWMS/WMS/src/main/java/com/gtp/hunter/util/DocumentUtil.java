package com.gtp.hunter.util;

import com.gtp.hunter.wms.model.Document;
import com.gtp.hunter.wms.model.DocumentField;

public class DocumentUtil {
    public static DocumentField getField(Document d, String metaname) {
        try {
            for (DocumentField bf : d.getFields()) {
                if (bf.getField() != null && bf.getField().getMetaname().equals(metaname)) {
                    if (bf.getValue() != null)
                        return bf;
                    break;
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
