package com.gtp.hunter.util;

import com.gtp.hunter.HunterMobileWMS;
import com.gtp.hunter.wms.model.Product;
import com.gtp.hunter.wms.model.ProductField;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

public class ProductUtil {

    private static CountDownLatch latch;
    private static final List<UUID> palletBoxIds =
            Arrays.asList(UUID.fromString("080fcd41-39b7-11e9-b38e-641c678336ee"),
                    UUID.fromString("12312e73-39b6-11e9-b38e-641c678336ee"),
                    UUID.fromString("160463ec-39b7-11e9-b38e-641c678336ee"),
                    UUID.fromString("21ab7205-39b7-11e9-b38e-641c678336ee"),
                    UUID.fromString("2d4636e3-39b7-11e9-b38e-641c678336ee"),
                    UUID.fromString("30a39f45-39b6-11e9-b38e-641c678336ee"),
                    UUID.fromString("3642913c-3c49-4708-b337-f0e368b01786"),
                    UUID.fromString("3954412b-39b7-11e9-b38e-641c678336ee"),
                    UUID.fromString("4342915f-39b6-11e9-b38e-641c678336ee"),
                    UUID.fromString("4cf42505-39b7-11e9-b38e-641c678336ee"),
                    UUID.fromString("583923b8-39b6-11e9-b38e-641c678336ee"),
                    UUID.fromString("58c06442-39b7-11e9-b38e-641c678336ee"),
                    UUID.fromString("63d0d04d-39b6-11e9-b38e-641c678336ee"),
                    UUID.fromString("645b8c67-39b7-11e9-b38e-641c678336ee"),
                    UUID.fromString("714d835e-39b6-11e9-b38e-641c678336ee"),
                    UUID.fromString("791a44dc-39b7-11e9-b38e-641c678336ee"),
                    UUID.fromString("8715f604-39b6-11e9-b38e-641c678336ee"),
                    UUID.fromString("998185ff-39b6-11e9-b38e-641c678336ee"),
                    UUID.fromString("a80b531c-39b6-11e9-b38e-641c678336ee"),
                    UUID.fromString("bfc9ffbe-39b6-11e9-b38e-641c678336ee"),
                    UUID.fromString("cbf4c242-39b6-11e9-b38e-641c678336ee"),
                    UUID.fromString("daedfd6f-39b6-11e9-b38e-641c678336ee"),
                    UUID.fromString("e7b050b6-39b6-11e9-b38e-641c678336ee"),
                    UUID.fromString("f6500406-39b6-11e9-b38e-641c678336ee"));

    private static final List<UUID> shelflifeIds =
            Arrays.asList(UUID.fromString("c44e1c1b-cd88-11e9-90f5-005056a19775"),
                    UUID.fromString("c455ceab-cd88-11e9-90f5-005056a19775"),
                    UUID.fromString("c455d0fa-cd88-11e9-90f5-005056a19775"),
                    UUID.fromString("c455d202-cd88-11e9-90f5-005056a19775"),
                    UUID.fromString("c455d300-cd88-11e9-90f5-005056a19775"),
                    UUID.fromString("c455d3b0-cd88-11e9-90f5-005056a19775"),
                    UUID.fromString("c455d465-cd88-11e9-90f5-005056a19775"),
                    UUID.fromString("c455d52d-cd88-11e9-90f5-005056a19775"),
                    UUID.fromString("c455d60e-cd88-11e9-90f5-005056a19775"),
                    UUID.fromString("c455d716-cd88-11e9-90f5-005056a19775"),
                    UUID.fromString("c455d7cb-cd88-11e9-90f5-005056a19775"),
                    UUID.fromString("c455d94d-cd88-11e9-90f5-005056a19775"),
                    UUID.fromString("c455da33-cd88-11e9-90f5-005056a19775"),
                    UUID.fromString("c455db36-cd88-11e9-90f5-005056a19775"),
                    UUID.fromString("c455dc47-cd88-11e9-90f5-005056a19775"),
                    UUID.fromString("c455dcdf-cd88-11e9-90f5-005056a19775"),
                    UUID.fromString("c455dd8a-cd88-11e9-90f5-005056a19775"),
                    UUID.fromString("c455de3f-cd88-11e9-90f5-005056a19775"),
                    UUID.fromString("c455df0c-cd88-11e9-90f5-005056a19775"),
                    UUID.fromString("c455dfed-cd88-11e9-90f5-005056a19775"),
                    UUID.fromString("c455e0e6-cd88-11e9-90f5-005056a19775"),
                    UUID.fromString("c455e1f3-cd88-11e9-90f5-005056a19775"),
                    UUID.fromString("c455e313-cd88-11e9-90f5-005056a19775"));

    private static final List<UUID> boxLayerIds = Arrays.asList(UUID.fromString("0863ca75-39b7-11e9-b38e-641c678336ee"),
            UUID.fromString("1294aa32-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("164ac84b-39b7-11e9-b38e-641c678336ee"),
            UUID.fromString("21ffe49a-39b7-11e9-b38e-641c678336ee"),
            UUID.fromString("2d888afb-39b7-11e9-b38e-641c678336ee"),
            UUID.fromString("30ffad42-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("39a041a3-39b7-11e9-b38e-641c678336ee"),
            UUID.fromString("439b12bc-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("4d3b2410-39b7-11e9-b38e-641c678336ee"),
            UUID.fromString("5882632f-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("5908a0ee-39b7-11e9-b38e-641c678336ee"),
            UUID.fromString("642b30ad-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("649d113a-39b7-11e9-b38e-641c678336ee"),
            UUID.fromString("71a2aaa8-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("878226d6-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("99d95ca6-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("a85a9254-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("c00d881f-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("cc40cdce-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("db35052f-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("e812e4ce-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("ea4028df-70af-4828-a50c-0b8ae2b4057d"),
            UUID.fromString("f6932532-39b6-11e9-b38e-641c678336ee"));

    private static final List<UUID> measureUnitIds = Arrays.asList(UUID.fromString("0819fc52-39b7-11e9-b38e-641c678336ee"),
            UUID.fromString("1246406f-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("1611e10e-39b7-11e9-b38e-641c678336ee"),
            UUID.fromString("21b58f34-39b7-11e9-b38e-641c678336ee"),
            UUID.fromString("2d504191-39b7-11e9-b38e-641c678336ee"),
            UUID.fromString("30b5592c-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("395e6ee9-39b7-11e9-b38e-641c678336ee"),
            UUID.fromString("39a0b90e-8bac-4b57-9bc9-c0a4e90ecbd4"),
            UUID.fromString("434cb531-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("4cfe4c96-39b7-11e9-b38e-641c678336ee"),
            UUID.fromString("58483b88-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("58cdf4ce-39b7-11e9-b38e-641c678336ee"),
            UUID.fromString("63dafc58-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("6465b4c5-39b7-11e9-b38e-641c678336ee"),
            UUID.fromString("7159e985-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("87202bdf-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("998b9f2f-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("a8156c37-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("bfd41fdf-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("cbfeec14-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("dafca569-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("e7ba5f4f-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("f65a2b70-39b6-11e9-b38e-641c678336ee"));

    private static final List<UUID> grossWeightIds = Arrays.asList(UUID.fromString("08456e44-39b7-11e9-b38e-641c678336ee"),
            UUID.fromString("1277f859-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("1631d8a7-39b7-11e9-b38e-641c678336ee"),
            UUID.fromString("21e2c51c-39b7-11e9-b38e-641c678336ee"),
            UUID.fromString("2d6ebee4-39b7-11e9-b38e-641c678336ee"),
            UUID.fromString("30e59ee3-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("39875f4c-39b7-11e9-b38e-641c678336ee"),
            UUID.fromString("4381130a-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("4d1cb294-39b7-11e9-b38e-641c678336ee"),
            UUID.fromString("5866ac7e-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("58ec7033-39b7-11e9-b38e-641c678336ee"),
            UUID.fromString("63f96b61-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("6484473d-39b7-11e9-b38e-641c678336ee"),
            UUID.fromString("71854723-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("87607054-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("8a51de88-0014-491c-a77f-b2987a58c9bd"),
            UUID.fromString("99c0884a-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("a83debb3-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("bff28c4c-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("cc249b3e-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("db1b1d69-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("e7d8e02f-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("f67a2ccd-39b6-11e9-b38e-641c678336ee"));

    private static final List<UUID> unitBoxIds = Arrays.asList(UUID.fromString("142e7671-c8d7-11e9-90f5-005056a19775"),
            UUID.fromString("142e9520-c8d7-11e9-90f5-005056a19775"),
            UUID.fromString("142e976f-c8d7-11e9-90f5-005056a19775"),
            UUID.fromString("142e9899-c8d7-11e9-90f5-005056a19775"),
            UUID.fromString("142e99a1-c8d7-11e9-90f5-005056a19775"),
            UUID.fromString("142e9ab8-c8d7-11e9-90f5-005056a19775"),
            UUID.fromString("142e9b4f-c8d7-11e9-90f5-005056a19775"),
            UUID.fromString("142e9bff-c8d7-11e9-90f5-005056a19775"),
            UUID.fromString("142e9ded-c8d7-11e9-90f5-005056a19775"),
            UUID.fromString("142e9ee1-c8d7-11e9-90f5-005056a19775"),
            UUID.fromString("142e9f83-c8d7-11e9-90f5-005056a19775"),
            UUID.fromString("142ea041-c8d7-11e9-90f5-005056a19775"),
            UUID.fromString("142ea118-c8d7-11e9-90f5-005056a19775"),
            UUID.fromString("142ea1fe-c8d7-11e9-90f5-005056a19775"),
            UUID.fromString("142ea2f7-c8d7-11e9-90f5-005056a19775"),
            UUID.fromString("142ea404-c8d7-11e9-90f5-005056a19775"),
            UUID.fromString("142ea4a1-c8d7-11e9-90f5-005056a19775"),
            UUID.fromString("142ea551-c8d7-11e9-90f5-005056a19775"),
            UUID.fromString("142ea619-c8d7-11e9-90f5-005056a19775"),
            UUID.fromString("142ea6ff-c8d7-11e9-90f5-005056a19775"),
            UUID.fromString("142ea7f3-c8d7-11e9-90f5-005056a19775"),
            UUID.fromString("142ea8fb-c8d7-11e9-90f5-005056a19775"),
            UUID.fromString("142eaa1b-c8d7-11e9-90f5-005056a19775"));

    private static final List<UUID> materialGroupIds = Arrays.asList(UUID.fromString("07f61e58-39b7-11e9-b38e-641c678336ee"),
            UUID.fromString("12102fc9-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("15ea6927-39b7-11e9-b38e-641c678336ee"),
            UUID.fromString("2191937a-39b7-11e9-b38e-641c678336ee"),
            UUID.fromString("26e3dc02-5226-4e73-bf5a-6edd03a391db"),
            UUID.fromString("2d2d661c-39b7-11e9-b38e-641c678336ee"),
            UUID.fromString("30858420-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("393b5620-39b7-11e9-b38e-641c678336ee"),
            UUID.fromString("4325cf32-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("4cda645d-39b7-11e9-b38e-641c678336ee"),
            UUID.fromString("5817d8ba-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("58a6a16a-39b7-11e9-b38e-641c678336ee"),
            UUID.fromString("63b24deb-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("6441dac7-39b7-11e9-b38e-641c678336ee"),
            UUID.fromString("7134bbdd-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("86fc0f95-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("9963b2dc-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("a7f05495-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("bfaf99fe-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("cbd66fd6-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("dac3bf9f-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("e7978267-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("f6363f50-39b6-11e9-b38e-641c678336ee"));

    private static final List<UUID> sizeIds = Arrays.asList(UUID.fromString("084aec21-39b7-11e9-b38e-641c678336ee"),
            UUID.fromString("12805fe4-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("1636860b-39b7-11e9-b38e-641c678336ee"),
            UUID.fromString("21eba2c1-39b7-11e9-b38e-641c678336ee"),
            UUID.fromString("2d7445fc-39b7-11e9-b38e-641c678336ee"),
            UUID.fromString("30eb4e8d-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("398c07bc-39b7-11e9-b38e-641c678336ee"),
            UUID.fromString("4386d299-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("4d27015f-39b7-11e9-b38e-641c678336ee"),
            UUID.fromString("586c4b56-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("58f10103-39b7-11e9-b38e-641c678336ee"),
            UUID.fromString("63ff2819-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("6488c9da-39b7-11e9-b38e-641c678336ee"),
            UUID.fromString("718e3e00-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("8764c5aa-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("8e050602-a2fe-4af0-acad-1256ce9fabb0"),
            UUID.fromString("99c50464-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("a84279dc-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("bff93972-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("cc2c80f0-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("db20cc1a-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("e7de6bf3-39b6-11e9-b38e-641c678336ee"),
            UUID.fromString("f67edc4b-39b6-11e9-b38e-641c678336ee"));

    public static ProductField getPalletBox(Product p) {
        return getField(p, palletBoxIds);
    }

    public static ProductField getLayerBox(Product p) {
        return getField(p, boxLayerIds);
    }

    public static ProductField getShelfLife(Product p) {
        return getField(p, shelflifeIds);
    }

    public static ProductField getMeasureUnit(Product p) {
        return getField(p, measureUnitIds);
    }

    public static ProductField getGrossWeight(Product p) {
        return getField(p, grossWeightIds);
    }

    public static ProductField getBoxUnit(Product p) {
        return getField(p, unitBoxIds);
    }

    public static ProductField getMaterialGroup(Product p) {
        return getField(p, materialGroupIds);
    }

    public static ProductField getSize(Product p) {
        return getField(p, sizeIds);
    }

    private static ProductField getField(Product p, List<UUID> modelFieldIds) {
        try {
            if (p.getFields().size() == 0) {
                latch = new CountDownLatch(1);
                Executors.newSingleThreadExecutor().submit(() -> {
                    p.getFields().addAll(HunterMobileWMS.getDB().pfDao().listByProductId(p.getId()));
                    latch.countDown();
                }).get();
                latch.await();
            }
            for (ProductField pf : p.getFields()) {
                if (modelFieldIds.contains(pf.getModelId())) {

                    if (pf.getValue() != null)
                        return pf;
                    break;
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
