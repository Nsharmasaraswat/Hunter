package com.gtp.hunter.structure.viewmodel;

import android.view.View;

import com.gtp.hunter.HunterMobileWMS;
import com.gtp.hunter.wms.model.AGLDocItem;
import com.gtp.hunter.wms.model.AGLDocument;
import com.gtp.hunter.wms.model.CheckingItem;
import com.gtp.hunter.wms.model.DocumentItem;
import com.gtp.hunter.wms.model.Thing;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;

public class AGLCheckingViewModel extends BaseDocumentViewModel {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    private int volumeCount = 0;

    private AGLDocument ret;
    private final List<CheckingItem> checkingItemList = new CopyOnWriteArrayList<>();
    private final Map<Integer, UUID> sugarThings = new HashMap<>();
    private int errorCount = 0;

    public int getErrorCount() {
        return errorCount;
    }

    public void increaseErrorCount() {
        this.errorCount++;
    }

    public void clearErrorCount() {
        this.errorCount = 0;
    }

    public void addSugarThingId(Integer vId, UUID tid) {
        this.sugarThings.put(vId, tid);
    }

    public void createReturn() {
        if (ret == null) {
            ret = new AGLDocument();
            ret.setId(document.getId().toString());
            ret.setName(document.getName());
            ret.setMetaname("ORDCONF");
            ret.setCreatedAtSQL(SDF.format(document.getCreatedAt()));
            ret.setUpdatedAtSQL(SDF.format(document.getUpdatedAt()));
            ret.setStatus(document.getStatus());
            ret.setParent_id(document.getParent_id());
            ret.setUser_id(HunterMobileWMS.getUser().getId().toString());
            ret.setCode(document.getCode());
            for (DocumentItem di : document.getItems()) {
//                if(di.getProduct() == null)
                AGLDocItem admi = new AGLDocItem();
                admi.setProduct_id(di.getProduct().getId().toString());
                admi.setQty(di.getQty());
                admi.setLayer(di.getLayer());
                admi.setMeasureUnit(di.getMeasureUnit());
                ret.getItems().add(admi);
            }
        }
    }

    public AGLDocument getReturn() {
        return ret;
    }

    public void setReturn(AGLDocument d) {
        this.ret = d;
    }

    public UUID getSugarThingId(Integer vId) {
        return this.sugarThings.get(vId);
    }

    public boolean isSugarPrinted(Integer vId) {
        return this.sugarThings.containsKey(vId);
    }

    public List<UUID> getSugarThings() {
        return new ArrayList<>(this.sugarThings.values());
    }

    public int updateVolumeCount(int value) {
        return volumeCount += value;
    }

    public View getView(int viewId) {
        for (CheckingItem tmp : checkingItemList)
            if (tmp.getView().getId() == viewId)
                return tmp.getView();
        return null;
    }

    public Thing getThing(Integer viewId) {
        if (viewId != null) {
            for (CheckingItem tmp : checkingItemList)
                if (tmp.getView().getId() == viewId && tmp.getThing() != null)
                    return tmp.getThing();
        }
        Thing ret = new Thing();
        ret.setId(UUID.randomUUID());
        return ret;
    }

    public void clearWrongViews() {
        for (CheckingItem tmp : checkingItemList)
            tmp.setWrong(false);
    }

    public void setWrong(UUID productId) {
        for (CheckingItem tmp : checkingItemList)
            if (tmp.getProduct_id().equals(productId))
                tmp.setWrong(true);
    }

    public void clearTemp() {
        final UUID docId = this.document.getId();
        clearWrongViews();
        clearErrorCount();
        Executors.newSingleThreadExecutor().execute(() -> HunterMobileWMS.getDB().checkItemDao().clear(docId));
        this.checkingItemList.clear();
    }

    public int newViewId() {
        int newId = checkingItemList.size();

        for (CheckingItem tmp : checkingItemList) {
            int vId = tmp.getView().getId();

            if (vId >= newId)
                newId = vId + 1;
        }

        return newId;
    }

    public int getViewId(View v) {
        for (CheckingItem tmp : checkingItemList)
            if (tmp.getView().getId() == v.getId())
                return tmp.getView().getId();
        return -1;
    }

    public CheckingItem getCheckingByViewId(int viewId) {
        for (CheckingItem tmp : checkingItemList)
            if (tmp.getView().getId() == viewId)
                return tmp;
        return null;
    }

    public void removeConf(CheckingItem item) {
        final int viewId = item.getView().getId();

        for (CheckingItem tmp : checkingItemList) {
            int vId = tmp.getView().getId();

            if (vId == viewId) {
                checkingItemList.remove(tmp);
                Executors.newSingleThreadExecutor().execute(() -> HunterMobileWMS.getDB().checkItemDao().delete(tmp));
            } else if (vId > viewId)
                tmp.getView().setId(vId - 1);
        }
    }

    public List<CheckingItem> getConferenceItems() {
        return checkingItemList;
    }
}