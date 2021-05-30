package com.gtp.hunter.wms.fragment;

import com.gtp.hunter.wms.model.Document;

public abstract class DocumentFragment extends BaseFragment {

    public abstract void transform(Document doc);

    public abstract void interact(String msg);
}
