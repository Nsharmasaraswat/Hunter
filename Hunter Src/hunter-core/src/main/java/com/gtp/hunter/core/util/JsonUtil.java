package com.gtp.hunter.core.util;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonString;

public class JsonUtil {

	public static Map<String, Object> jsonToMap(String str) throws JsonException {
		StringReader reader = new StringReader(str);
		JsonReader jr = Json.createReader(reader);
		JsonObject jo = jr.readObject();
		return jsonToMap(jo);
	}

	public static Map<String, Object> jsonToMap(JsonObject json) throws JsonException {
		Map<String, Object> retMap = new HashMap<String, Object>();

		if (json != JsonObject.NULL) {
			retMap = toMap(json);
		}
		return retMap;
	}

	public static Map<String, Object> toMap(JsonObject object) throws JsonException {
		Map<String, Object> map = new HashMap<String, Object>();

		Iterator<String> keysItr = object.keySet().iterator();
		while (keysItr.hasNext()) {
			String key = keysItr.next();
			Object value = object.get(key);

			if (value instanceof JsonArray) {
				value = toList((JsonArray) value);
			} else if (value instanceof JsonObject) {
				value = toMap((JsonObject) value);
			} else if (value instanceof JsonString) {
				value = ((JsonString) value).getString();
			}

			map.put(key, value);
		}
		return map;
	}

	public static List<Object> toList(JsonArray array) throws JsonException {
		List<Object> list = new ArrayList<Object>();
		for (int i = 0; i < array.size(); i++) {
			Object value = array.get(i);

			if (value instanceof JsonArray) {
				value = toList((JsonArray) value);
			} else if (value instanceof JsonObject) {
				value = toMap((JsonObject) value);
			}
			list.add(value);
		}
		return list;
	}

	public static JsonObject mapToJson(Map<String, Object> params) {
		return params.entrySet().stream().collect(JsonUtil.toJsonBuilder()).build();
	}

	private static Collector<Map.Entry<String, Object>, ?, JsonObjectBuilder> toJsonBuilder() {
		return Collector.of(Json::createObjectBuilder, (t, u) -> {
			t.add(String.valueOf(String.valueOf(u.getKey())), String.valueOf(u.getValue()));
		}, JsonUtil::merge);
	}

	private static JsonObjectBuilder merge(JsonObjectBuilder left, JsonObjectBuilder right) {
		JsonObjectBuilder retVal = Json.createObjectBuilder();
		JsonObject leftObject = left.build();
		JsonObject rightObject = right.build();
		leftObject.keySet().stream().forEach((key) -> retVal.add(key, leftObject.get(key)));
		rightObject.keySet().stream().forEach((key) -> retVal.add(key, rightObject.get(key)));
		return retVal;
	}

}
