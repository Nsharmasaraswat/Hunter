package com.gtp.hunter.structure.viewmodel;

import androidx.lifecycle.ViewModel;

import com.gtp.hunter.wms.model.AGLDocument;

public abstract class AGLDocumentViewModel extends ViewModel {

    protected AGLDocument document;

    public AGLDocument getDocument() {
        return document;
    }

    public void setDocument(AGLDocument document) {
        this.document = document;
    }
}
