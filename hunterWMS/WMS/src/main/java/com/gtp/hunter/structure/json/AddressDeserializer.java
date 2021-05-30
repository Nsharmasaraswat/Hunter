package com.gtp.hunter.structure.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.gtp.hunter.wms.model.Address;
import com.gtp.hunter.wms.model.AddressField;
import com.gtp.hunter.wms.model.AddressModel;
import com.gtp.hunter.wms.model.BaseModelField;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

public class AddressDeserializer implements JsonDeserializer<Address> {
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);

    @Override
    public Address deserialize(JsonElement element, Type arg1, JsonDeserializationContext arg2) throws JsonParseException {
        Address ret = new Address();
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
        ret.setWkt(!obj.has("wkt") || obj.get("wkt").isJsonNull() ? null : obj.get("wkt").getAsString());
        for (JsonElement pfEl : fields) {
            JsonObject pfObj = pfEl.getAsJsonObject();
            JsonObject pmfObj = pfObj.getAsJsonObject("model");
            BaseModelField bmf = new BaseModelField();
            AddressField af = new AddressField();

            bmf.setId(pmfObj.get("id").isJsonNull() ? null : UUID.fromString(pmfObj.get("id").getAsString()));
            bmf.setMetaname(pmfObj.get("metaname").isJsonNull() ? null : pmfObj.get("metaname").getAsString());
            bmf.setStatus(pmfObj.get("status").isJsonNull() ? null : pmfObj.get("status").getAsString());
            bmf.setType(pmfObj.get("type").isJsonNull() ? null : pmfObj.get("type").getAsString());
            bmf.setOrdem(pmfObj.get("ordem").isJsonNull() ? null : pmfObj.get("ordem").getAsInt());

            af.setId(pfObj.get("id").isJsonNull() ? null : UUID.fromString(pfObj.get("id").getAsString()));
            af.setField(bmf);
            af.setAddress_id(ret.getId());
            af.setValue(pfObj.get("value").isJsonNull() ? null : pfObj.get("value").getAsString());
            ret.getFields().add(af);
        }
        if (!obj.get("model").isJsonNull()) {
            ret.setModel(arg2.deserialize(obj.get("model"), AddressModel.class));
        }
        if (obj.has("parent_id") && !obj.get("parent_id").isJsonNull()) {
            ret.setParent_id(UUID.fromString(obj.get("parent_id").getAsString()));
        }
        if (obj.has("parent") && !obj.get("parent").isJsonNull()) {
            Address p = arg2.deserialize(obj.get("parent_id"), Address.class);

            ret.setParent_id(p.getId());
        }
        return ret;
    }
}
