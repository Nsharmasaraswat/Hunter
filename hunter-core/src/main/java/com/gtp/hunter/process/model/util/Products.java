package com.gtp.hunter.process.model.util;

import java.util.Date;

import com.gtp.hunter.process.model.Product;

public class Products {

	public static double getDoubleField(Product p, String metaname) {
		return getDoubleField(p, metaname, 0d);
	}

	public static int getIntField(Product p, String metaname) {
		return getIntegerField(p, metaname, 1);
	}

	public static long getLongField(Product p, String metaname) {
		return getLongField(p, metaname, 0l);
	}

	public static String getStringField(Product p, String metaname) {
		return getStringField(p, metaname, "");
	}

	public static double getDoubleField(Product p, String metaname, double defValue) {
		assert p != null;
		return p.getFields().parallelStream()
						.filter(pf -> pf != null && pf.getModel() != null && pf.getModel().getMetaname() != null && pf.getModel().getMetaname().equals(metaname) && !pf.getValue().isEmpty())
						.mapToDouble(pf -> {
							try {
								return Double.parseDouble(pf.getValue());
							} catch (Exception e) {
								e.printStackTrace();
								return defValue;
							}
						})
						.findAny()
						.orElse(defValue);
	}

	public static Date getDateField(Product p, String metaname, Date defValue) {
		assert p != null;
		return p.getFields().parallelStream()
						.filter(pf -> pf != null && pf.getModel() != null && pf.getModel().getMetaname() != null && pf.getModel().getMetaname().equals(metaname) && !pf.getValue().isEmpty())
						.map(pf -> {
							try {
								return CommonUtil.parseDate(pf.getValue());
							} catch (Exception e) {
								e.printStackTrace();
								return defValue;
							}
						})
						.findAny()
						.orElse(defValue);
	}

	public static String getStringField(Product p, String metaname, String defValue) {
		assert p != null;
		return p.getFields().parallelStream()
						.filter(pf -> pf != null && pf.getModel() != null && pf.getModel().getMetaname() != null && pf.getModel().getMetaname().equals(metaname))
						.map(pf -> {
							return pf.getValue();
						})
						.findAny()
						.orElse(defValue);
	}

	public static int getIntegerField(Product p, String metaname, int defValue) {
		assert p != null;
		return p.getFields().parallelStream()
						.filter(pf -> pf != null && pf.getModel() != null && pf.getModel().getMetaname() != null && pf.getModel().getMetaname().equals(metaname) && !pf.getValue().isEmpty())
						.mapToInt(pf -> {
							try {
								return Integer.parseInt(pf.getValue());
							} catch (Exception e) {
								e.printStackTrace();
								return defValue;
							}
						})
						.findAny()
						.orElse(defValue);
	}

	public static long getLongField(Product p, String metaname, long defValue) {
		assert p != null;
		return p.getFields().parallelStream()
						.filter(pf -> pf != null && pf.getModel() != null && pf.getModel().getMetaname() != null && pf.getModel().getMetaname().equals(metaname) && !pf.getValue().isEmpty())
						.mapToLong(pf -> {
							try {
								return Long.parseLong(pf.getValue());
							} catch (Exception e) {
								e.printStackTrace();
								return defValue;
							}
						})
						.findAny()
						.orElse(defValue);
	}
}
