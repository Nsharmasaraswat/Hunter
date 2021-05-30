package com.gtp.hunter.common.util;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class ReflectionUtil {

	public static Class<?> getPropertyType(Class<?> clazz, String fieldName) {
		final String[] fieldNames = fieldName.split("\\.", -1);
		if (fieldNames.length > 1) {
			final String firstProperty = fieldNames[0];
			final String otherProperties = StringUtils.join(fieldNames, '.', 1, fieldNames.length);
			final Class<?> firstPropertyType = getPropertyType(clazz, firstProperty);
			return getPropertyType(firstPropertyType, otherProperties);
		} 
		try {
			Type classe = clazz.getDeclaredField(fieldName).getGenericType();
			if (classe instanceof ParameterizedType) {
		        ParameterizedType pType = (ParameterizedType)classe;
		        return Class.forName(pType.getActualTypeArguments()[0].getTypeName());
		    } else {
				return clazz.getDeclaredField(fieldName).getType();
		    }
		} catch (final Exception e) {
			if (!clazz.equals(Object.class)) {
				return getPropertyType(clazz.getSuperclass(), fieldName);
			}
			throw new IllegalStateException(e);
		}
	}

	public static <T> T getPropertyValue(Object obj, String string) {
		Object ret = obj;
		String[] parts = string.split("\\.");

		for (String field : parts) {
			try {
				Class<?> clazz = ret.getClass();
				Field f = clazz.getDeclaredField(field);
				f.setAccessible(true);
				ret = f.get(ret);
			} catch (NoSuchFieldException | SecurityException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}

		return (T) ret;
	}

	public static boolean isClassCollection(Class c) {
		return Collection.class.isAssignableFrom(c) || Map.class.isAssignableFrom(c);
	}
}
