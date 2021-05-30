package com.gtp.hunter.structure.viewmodel;

import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class RFIDViewModel extends BaseDocumentViewModel {

    private List<View> diViewList = new ArrayList<>();

    public List<View> getDiViewList() {
        return diViewList;
    }
}