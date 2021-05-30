package com.gtp.hunter.structure.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.gtp.hunter.wms.model.BaseModelField;
import com.gtp.hunter.wms.model.Product;
import com.gtp.hunter.wms.model.ProductField;
import com.gtp.hunter.wms.model.ProductModel;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

public class ProductDeserializer implements JsonDeserializer<Product> {
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);

    @Override
    public Product deserialize(JsonElement element, Type arg1, JsonDeserializationContext arg2) throws JsonParseException {
        Product ret = new Product();
        JsonObject obj = (JsonObject) element;
        JsonArray fields = obj.getAsJsonArray("fields");

        ret.setId(obj.get("id").isJsonNull() ? null : UUID.fromString(obj.get("id").getAsString()));
        ret.setName(obj.get("name").isJsonNull() ? null : obj.get("name").getAsString());
        ret.setMetaname(obj.get("metaname").isJsonNull() ? null : obj.get("metaname").getAsString());
        ret.setStatus(obj.get("status").isJsonNull() ? null : obj.get("status").getAsString());
        try {
            ret.setCreatedAt(obj.get("createdAt").isJsonNull() ? null : sdf.parse(obj.get("createdAt").getAsString()));
            ret.setUpdatedAt(obj.get("updatedAt").isJsonNull() ? null : sdf.parse(obj.get("updatedAt").getAsString()));
        } catch (ParseException pe) {
            ret.setCreatedAt(Calendar.getInstance().getTime());
            ret.setUpdatedAt(Calendar.getInstance().getTime());
        }
        ret.setSku(obj.get("sku").isJsonNull() ? null : obj.get("sku").getAsString());
        for (JsonElement pfEl : fields) {
            JsonObject pfObj = pfEl.getAsJsonObject();
            JsonObject pmfObj = pfObj.getAsJsonObject("model");
            BaseModelField bmf = new BaseModelField();
            ProductField pf = new ProductField();

            bmf.setId(pmfObj.get("id").isJsonNull() ? null : UUID.fromString(pmfObj.get("id").getAsString()));
            bmf.setMetaname(pmfObj.get("metaname").isJsonNull() ? null : pmfObj.get("metaname").getAsString());
            bmf.setStatus(pmfObj.get("status").isJsonNull() ? null : pmfObj.get("status").getAsString());
            bmf.setType(pmfObj.get("type").isJsonNull() ? null : pmfObj.get("type").getAsString());
            bmf.setOrdem(pmfObj.get("ordem").isJsonNull() ? null : pmfObj.get("ordem").getAsInt());

            pf.setId(pfObj.get("id").isJsonNull() ? null : UUID.fromString(pfObj.get("id").getAsString()));
            pf.setField(bmf);
            pf.setProduct_id(ret.getId());
            pf.setValue(pfObj.get("value").isJsonNull() ? null : pfObj.get("value").getAsString());
            ret.getFields().add(pf);
        }
        if (!obj.get("model").isJsonNull()) {
            ret.setModel(arg2.deserialize(obj.get("model"), ProductModel.class));
        }
        if (!obj.get("parent").isJsonNull()) {
            JsonObject parentObj = obj.get("parent").getAsJsonObject();
            ret.setParent_id(UUID.fromString(parentObj.get("id").getAsString()));
        }
        return ret;
    }
}
