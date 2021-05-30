package com.gtp.hunter.ui.json;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.gtp.hunter.core.model.Unit;
import com.gtp.hunter.process.model.Address;
import com.gtp.hunter.process.model.AddressField;
import com.gtp.hunter.process.model.AddressModel;

public class AddressSerializer implements JsonSerializer<Address> {

	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Override
	public JsonElement serialize(Address src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject ret = new JsonObject();
		JsonArray arrFields = new JsonArray();
		JsonArray arrUnits = new JsonArray();
		JsonArray arrSib = new JsonArray();

		for (AddressField af : src.getFields())
			arrFields.add(context.serialize(af, AddressField.class));
		for (Unit u : src.getUnits())
			arrUnits.add(context.serialize(u, Unit.class));
		for (Address a : src.getSiblings())
			arrSib.add(context.serialize(a, Address.class));
		ret.add("id", new JsonPrimitive(src.getId().toString()));
		ret.add("name", new JsonPrimitive(src.getName()));
		ret.add("metaname", new JsonPrimitive(src.getMetaname()));
		ret.add("status", new JsonPrimitive(src.getStatus()));
		ret.add("createdAt", new JsonPrimitive(SDF.format(src.getCreatedAt())));
		ret.add("updatedAt", new JsonPrimitive(SDF.format(src.getUpdatedAt())));
		ret.add("wkt", new JsonPrimitive(src.getWkt()));
		ret.add("model", context.serialize(src.getModel(), AddressModel.class));
		ret.add("fields", arrFields);
		if (src.getParent() != null) {
			//ret.add("parent", context.serialize(src.getParent(), Address.class));
			ret.add("parent_id", new JsonPrimitive(src.getParent_id()));
		}
		ret.add("fields", arrUnits);
		ret.add("siblings", arrSib);
		return ret;
	}
}
