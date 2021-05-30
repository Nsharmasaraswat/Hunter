package com.gtp.hunter.structure.viewmodel;

import com.gtp.hunter.HunterMobileWMS;
import com.gtp.hunter.structure.Step;
import com.gtp.hunter.util.DocumentUtil;
import com.gtp.hunter.util.ProductUtil;
import com.gtp.hunter.util.ThingUtil;
import com.gtp.hunter.wms.model.Address;
import com.gtp.hunter.wms.model.DocumentField;
import com.gtp.hunter.wms.model.DocumentItem;
import com.gtp.hunter.wms.model.Product;
import com.gtp.hunter.wms.model.ProductField;
import com.gtp.hunter.wms.model.Thing;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import static com.gtp.hunter.structure.Step.ADDRESS;

public class PickingViewModel extends BaseDocumentViewModel {

    private Thing thing;
    private Address address;
    private DocumentItem item;
    private double productWeight;
    private Step step;
    private int layer;
    private int currentSeq;
    private boolean separator;
    private boolean highlight;
    private String productText;
    private DocumentItem[] stepOrder;

    public Step getStep() {
        if (step != null) return step;
        return null;
    }

    public boolean isActive() {
        return step != null && item != null;
    }

    public void setStep(Step step) {
        this.step = step;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address a) {
        this.address = a;
    }

    public DocumentItem getItem() {
        return item;
    }

    public void setItem(DocumentItem di) {
        this.item = di;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }

    public int getLayer() {
        return layer;
    }

    public void setProductText(String productText) {
        this.productText = productText;
    }

    public String getProductText() {
        return productText;
    }

    public boolean isSeparator() {
        return separator;
    }

    public boolean isHighlight() {
        return highlight;
    }

    public void setSeparator(boolean separator) {
        this.separator = separator;
    }

    public void setHighlight(boolean highlight) {
        this.highlight = highlight;
    }

    public void setProductWeight(double targetWeight) {
        this.productWeight = targetWeight;
    }

    public double getProductWeight() {
        return productWeight;
    }

    public void setThing(Thing thing) {
        this.thing = thing;
    }

    public Thing getThing() {
        return thing;
    }

    public double startPickingStep(int i) {
        if (getDocument().getItems().size() > i) {
            DocumentItem di = stepOrder[i];
            Product prd = HunterMobileWMS.findProduct(di.getProduct().getId());
            ProductField pfWeight = ProductUtil.getGrossWeight(prd);
            ProductField pfUnitBox = ProductUtil.getBoxUnit(prd);
            double unitWeight = Double.parseDouble(pfWeight.getValue());
            double unitCount = Double.parseDouble(pfUnitBox.getValue());

            setProductWeight(unitWeight * unitCount);
            setAddress(HunterMobileWMS.findAddress(UUID.fromString(di.getProps().get("ADDRESS_ID"))));
            setLayer(Integer.parseInt(Objects.requireNonNull(di.getProps().get("LAYER"))));
            setSeparator(Objects.requireNonNull(di.getProps().get("SEPARATOR")).equalsIgnoreCase("TRUE"));
            setHighlight(Objects.requireNonNull(di.getProps().get("HIGHLIGHT")).equalsIgnoreCase("TRUE"));
            setProductText(Objects.requireNonNull(di.getProps().get("PRODUCT_DESCRIPTION_LONG")));
            setStep(ADDRESS);
            setItem(di);
            this.currentSeq = i + 1;
            return di.getQty() * unitWeight * unitCount;
        }
        return 0d;
    }

    public DocumentItem getLastPick() {
        if (currentSeq > 1 && stepOrder.length > currentSeq - 2) {
            return stepOrder[currentSeq - 2];
        }
        return null;
    }

    public int getCurrentSeq() {
        return currentSeq;
    }

    public void init() {
        if (thing == null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
            Product p = HunterMobileWMS.findProduct(UUID.fromString("95b564e9-ea5a-4caa-adbe-06fc7dd0b966"));

            thing = new Thing();
            thing.setName(Objects.requireNonNull(p).getName());
            thing.setProduct(p);
            thing.setAddress(getStage());
            thing.setUpdatedAt(Calendar.getInstance().getTime());
            thing.setCreatedAt(Calendar.getInstance().getTime());
            thing.setStatus("NOVO");
            thing.getProperties().add(ThingUtil.createField("ACTUAL_WEIGHT", "0.0000"));
            thing.getProperties().add(ThingUtil.createField("LOT_EXPIRE", sdf.format(new Date())));
            thing.getProperties().add(ThingUtil.createField("LOT_ID", "VARIADO"));
            thing.getProperties().add(ThingUtil.createField("MANUFACTURING_BATCH", sdf.format(new Date())));
            thing.getProperties().add(ThingUtil.createField("QUANTITY", "1.0000"));
            thing.getProperties().add(ThingUtil.createField("STARTING_WEIGHT", "0.0000"));
        }
        stepOrder = new DocumentItem[document.getItems().size()];
        Comparator<DocumentItem> comp = (o1, o2) -> {
            if (o1 == null && o2 == null) return 0;
            if (o2 == null) return -1;
            if (o1 == null) return 1;
            int seq1 = Integer.parseInt(Objects.requireNonNull(o1.getProps().get("SEQ")));
            int layer1 = Integer.parseInt(Objects.requireNonNull(o1.getProps().get("LAYER")));
            int seq2 = Integer.parseInt(Objects.requireNonNull(o2.getProps().get("SEQ")));
            int layer2 = Integer.parseInt(Objects.requireNonNull(o2.getProps().get("LAYER")));

            if (seq1 == seq2) return layer1 - layer2;
            return seq1 - seq2;
        };
        stepOrder = document.getItems().toArray(stepOrder);
        Arrays.sort(stepOrder, comp);
    }

    public Address getStage() {
        DocumentField bf = DocumentUtil.getField(getDocument(), "STAGE_ID");

        return HunterMobileWMS.findAddress(UUID.fromString(Objects.requireNonNull(bf).getValue()));
    }
}