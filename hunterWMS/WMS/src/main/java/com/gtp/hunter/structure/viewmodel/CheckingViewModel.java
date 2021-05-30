package com.gtp.hunter.structure.viewmodel;

import com.gtp.hunter.wms.model.Document;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class CheckingViewModel extends BaseDocumentViewModel {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    private int errorCount;

    private Document doc;

    public void setDocument(Document d) {
        this.doc = d;
    }

    public Document getDocument() {
        return this.doc;
    }

    public void incrementErrorCount() {
        ++errorCount;
    }

    public void setErrorCount(int ec) {
        this.errorCount = ec;
    }

    public int getErrorCount() {
        return this.errorCount;
    }
}
