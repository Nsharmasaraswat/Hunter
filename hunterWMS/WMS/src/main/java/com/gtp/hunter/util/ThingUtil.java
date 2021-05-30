package com.gtp.hunter.util;

import com.gtp.hunter.wms.model.BaseField;
import com.gtp.hunter.wms.model.BaseModelField;
import com.gtp.hunter.wms.model.Thing;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ThingUtil {
    private static final Map<String, UUID> modelIdMap = new HashMap<>();

    static {
        modelIdMap.put("QC", UUID.fromString("235926e8-8945-11e9-815b-005056a19775"));
        modelIdMap.put("QUANTITY", UUID.fromString("3901544d-6d1b-11e9-a948-0266c0e70a8c"));
        modelIdMap.put("ACTUAL_WEIGHT", UUID.fromString("3919401a-6d1b-11e9-a948-0266c0e70a8c"));
        modelIdMap.put("LOT_EXPIRE", UUID.fromString("392f72d2-6d1b-11e9-a948-0266c0e70a8c"));
        modelIdMap.put("LOT_ID", UUID.fromString("3943501b-6d1b-11e9-a948-0266c0e70a8c"));
        modelIdMap.put("MANUFACTURING_BATCH", UUID.fromString("395900b1-6d1b-11e9-a948-0266c0e70a8c"));
        modelIdMap.put("STARTING_WEIGHT", UUID.fromString("396eb62f-6d1b-11e9-a948-0266c0e70a8c"));
        modelIdMap.put("INTERNAL_LOT", UUID.fromString("526367e8-79ac-11e9-a9ec-005056a19775"));
        modelIdMap.put("LABEL_OBS", UUID.fromString("52661afe-79ac-11e9-a9ec-005056a19775"));
        modelIdMap.put("REACTIVITY", UUID.fromString("52666847-79ac-11e9-a9ec-005056a19775"));
        modelIdMap.put("LIFETHREAT", UUID.fromString("5266f3ce-79ac-11e9-a9ec-005056a19775"));
        modelIdMap.put("SPECIAL_REC", UUID.fromString("526f27fc-79ac-11e9-a9ec-005056a19775"));
        modelIdMap.put("INFLAMABILITY", UUID.fromString("5276eded-79ac-11e9-a9ec-005056a19775"));
    }

    public static BaseField createField(String metaname, String value) {
        BaseModelField bmf = new BaseModelField();
        BaseField ret = new BaseField();

        bmf.setMetaname(metaname);
        bmf.setId(modelIdMap.get(metaname));
        ret.setField(bmf);
        ret.setValue(value);
        ret.setCreatedAt(new Date());
        ret.setUpdatedAt(new Date());
        return ret;
    }

    public static BaseField getQuantityField(Thing t) {
        return getField(t, modelIdMap.get("QUANTITY"));
    }

    public static BaseField getManufactureField(Thing t) {
        return getField(t, modelIdMap.get("MANUFACTURING_BATCH"));
    }

    public static BaseField getExpiryField(Thing t) {
        return getField(t, modelIdMap.get("LOT_EXPIRE"));
    }

    private static BaseField getField(Thing t, UUID id) {
        for (BaseField bf : t.getProperties()) {
            if (bf.getModelId() != null && bf.getModelId().equals(id))
                return bf;
        }
        return null;
    }

    public static UUID getFieldId(String metaname) {
        return modelIdMap.get(metaname);
    }
}
