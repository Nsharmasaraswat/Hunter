package com.gtp.hunter.process.model.util;

import java.util.Date;

import com.gtp.hunter.process.model.Property;
import com.gtp.hunter.process.model.Thing;

import io.reactivex.annotations.NonNull;

public class Things {

	@NonNull
	public static double getDoubleProperty(Thing t, String metaname) {
		return getDoubleProperty(t, metaname, 0d);
	}

	@NonNull
	public static int getIntProperty(Thing t, String metaname) {
		return getIntegerProperty(t, metaname, 1);
	}

	@NonNull
	public static long getLongProperty(Thing t, String metaname) {
		return getLongProperty(t, metaname, 0l);
	}

	@NonNull
	public static String getStringProperty(Thing t, String metaname) {
		return getStringProperty(t, metaname, "");
	}

	@NonNull
	public static double getDoubleProperty(Thing t, String metaname, double defValue) {
		return t == null || t.getProperties() == null ? defValue : t.getProperties().parallelStream()
						.filter(pr -> pr != null && pr.getField() != null && pr.getField().getMetaname() != null && pr.getField().getMetaname().equals(metaname) && !pr.getValue().isEmpty())
						.mapToDouble(pr -> {
							try {
								return Double.parseDouble(pr.getValue().replace(",", "."));
							} catch (Exception e) {
								e.printStackTrace();
								return defValue;
							}
						})
						.findAny()
						.orElse(defValue);
	}

	@NonNull
	public static Date getDateProperty(Thing t, String metaname, Date defValue) {
		return t == null || t.getProperties() == null ? defValue : t.getProperties().parallelStream()
						.filter(pr -> pr != null && pr.getField() != null && pr.getField().getMetaname() != null && pr.getField().getMetaname().equals(metaname) && !pr.getValue().isEmpty())
						.map(pr -> {
							try {
								return CommonUtil.parseDate(pr.getValue());
							} catch (Exception e) {
								e.printStackTrace();
								return defValue;
							}
						})
						.findAny()
						.orElse(defValue);
	}

	@NonNull
	public static String getStringProperty(Thing t, String metaname, String defValue) {
		return t == null || t.getProperties() == null ? defValue : t.getProperties().parallelStream()
						.filter(pr -> pr != null && pr.getField() != null && pr.getField().getMetaname() != null && pr.getField().getMetaname().equals(metaname))
						.map(pr -> {
							return pr.getValue();
						})
						.findAny()
						.orElse(defValue);
	}

	@NonNull
	public static int getIntegerProperty(Thing t, String metaname, int defValue) {
		return t == null || t.getProperties() == null ? defValue : t.getProperties().parallelStream()
						.filter(pr -> pr != null && pr.getField() != null && pr.getField().getMetaname() != null && pr.getField().getMetaname().equals(metaname) && !pr.getValue().isEmpty())
						.mapToInt(pr -> {
							try {
								return Integer.parseInt(pr.getValue());
							} catch (Exception e) {
								e.printStackTrace();
								return defValue;
							}
						})
						.findAny()
						.orElse(defValue);
	}

	@NonNull
	public static long getLongProperty(Thing t, String metaname, long defValue) {
		return t == null || t.getProperties() == null ? defValue : t.getProperties().parallelStream()
						.filter(pr -> pr != null && pr.getField() != null && pr.getField().getMetaname() != null && pr.getField().getMetaname().equals(metaname) && !pr.getValue().isEmpty())
						.mapToLong(pr -> {
							try {
								return Long.parseLong(pr.getValue());
							} catch (Exception e) {
								e.printStackTrace();
								return defValue;
							}
						})
						.findAny()
						.orElse(defValue);
	}

	public static Property findProperty(Thing t, String metaname) {
		return t == null || t.getProperties() == null ? null : t.getProperties().parallelStream()
						.filter(pr -> pr != null && pr.getField() != null && pr.getField().getMetaname() != null && pr.getField().getMetaname().equals(metaname))
						.findAny()
						.orElse(null);
	}
}
