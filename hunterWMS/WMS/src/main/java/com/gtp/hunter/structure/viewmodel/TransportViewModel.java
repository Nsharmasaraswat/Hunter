package com.gtp.hunter.structure.viewmodel;

import com.gtp.hunter.HunterMobileWMS;
import com.gtp.hunter.structure.Step;
import com.gtp.hunter.wms.model.AGLDocItem;
import com.gtp.hunter.wms.model.AGLDocument;
import com.gtp.hunter.wms.model.AGLTransport;
import com.gtp.hunter.wms.model.DocumentItem;
import com.gtp.hunter.wms.model.DocumentThing;
import com.gtp.hunter.wms.model.DocumentTransport;
import com.gtp.hunter.wms.model.Thing;

import java.text.SimpleDateFormat;
import java.util.Locale;

import static com.gtp.hunter.structure.Step.ORIGIN;

public class TransportViewModel extends BaseDocumentViewModel {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    private AGLDocument ret;
    private DocumentTransport transport;
    private Step step;
    private int currentSeq;

    public DocumentTransport getTransport() {
        return transport;
    }

    public void setTransport(DocumentTransport transport) {
        this.transport = transport;
    }

    public Step getStep() {
        return step;
    }

    public boolean isActive() {
        return step != null && transport != null;
    }

    public void setStep(Step step) {
        this.step = step;
    }

    public void startTransportStep(int i) {
        if (getDocument().getTransports().size() > i) {
            setTransport(getDocument().getTransports().get(i));
            setStep(ORIGIN);
            this.currentSeq = i + 1;
        }
    }

    public int getCurrentSeq() {
        return currentSeq;
    }

    public void createReturn() {
        if (ret == null) {
            ret = new AGLDocument();
            ret.setId(document.getId().toString());
            ret.setName(document.getName());
            ret.setMetaname("ORDMOV");
            ret.setCreatedAtSQL(SDF.format(document.getCreatedAt()));
            ret.setUpdatedAtSQL(SDF.format(document.getUpdatedAt()));
            ret.setStatus(document.getStatus());
            ret.setParent_id(document.getParent_id());
            ret.setCode(document.getCode());
            ret.setUser_id(HunterMobileWMS.getUser().getId().toString());
            for (DocumentItem di : document.getItems()) {
                AGLDocItem admi = new AGLDocItem();

                admi.setProduct_id(di.getProduct() == null ? di.getProduct_id() : di.getProduct().getId().toString());
                admi.setQty(di.getQty());
                admi.setLayer(di.getLayer());
                admi.setMeasureUnit(di.getMeasureUnit());
                ret.getItems().add(admi);
            }
            for (DocumentTransport dtr : document.getTransports()) {
                AGLTransport atr = new AGLTransport();

                atr.setAddress_id(dtr.getAddress().getId().toString());
                atr.setThing_id(dtr.getThing().getId().toString());
                atr.setSeq(dtr.getSeq());
                atr.setParent_id(dtr.getAddress().getParent_id().toString());
                ret.getTransports().add(atr);
            }
            for (DocumentThing dt : document.getThings()) {
                Thing t = dt.getThing();

                ret.getThings().add(t.getAGLThing());
            }
        }
    }

    public AGLDocument getReturn() {
        return ret;
    }

    public void setReturn(AGLDocument doc) {
        this.ret = doc;
    }

    public boolean isAlert() {
        return transport != null && transport.getThing() != null && transport.getThing().getPayload() != null && transport.getThing().getPayload().contains("ALERT");
    }
}
