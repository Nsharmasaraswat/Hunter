package com.gtp.hunter.util;

import com.gtp.hunter.HunterMobileWMS;
import com.gtp.hunter.wms.model.Address;
import com.gtp.hunter.wms.model.AddressField;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;

public class AddressUtil {

    private static final List<String> storageMetanames =
            Arrays.asList("ROAD", "DRIVE-IN", "BLOCK", "DOCK", "RACK");

    private static final List<UUID> capacityIds =
            Arrays.asList(UUID.fromString("839bf4e3-c872-11e9-90f5-005056a19775"),
                    UUID.fromString("9eab0620-38ab-11ea-89cb-005056a19775"),
                    UUID.fromString("a36c6634-974b-11e9-815b-005056a19775"),
                    UUID.fromString("ea5e19e1-40ed-11ea-b9fa-005056a19775"),
                    UUID.fromString("ea5f12fb-40ed-11ea-b9fa-005056a19775"));

    private static final List<UUID> sequenceIds =
            Collections.singletonList(UUID.fromString("03eee3c1-a249-11e9-97e4-005056a19775"));

    public static AddressField getCapacity(Address a) {
        return getField(a, capacityIds);
    }

    public static AddressField getSequence(Address a) {
        return getField(a, sequenceIds);
    }

    private static AddressField getField(Address a, List<UUID> modelFieldIds) {
        try {
            if (a.getFields().size() == 0)
                Executors.newSingleThreadExecutor().submit(() -> a.getFields().addAll(HunterMobileWMS.getDB().afDao().listByAddressId(a.getId()))).get();
            for (AddressField af : a.getFields()) {
                if (modelFieldIds.contains(af.getModelId())) {

                    if (af.getValue() != null)
                        return af;
                    break;
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
