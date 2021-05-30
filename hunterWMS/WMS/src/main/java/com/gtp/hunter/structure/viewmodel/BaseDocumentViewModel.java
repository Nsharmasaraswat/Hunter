package com.gtp.hunter.structure.viewmodel;

import androidx.lifecycle.ViewModel;

import com.gtp.hunter.wms.model.Document;

public abstract class BaseDocumentViewModel extends ViewModel {

    protected Document document;

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }
}
