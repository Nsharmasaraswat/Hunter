/**
 * 
 */
package com.gtp.hunter.process.model.util;

import java.util.Date;

import com.gtp.hunter.process.model.Document;
import com.gtp.hunter.process.model.DocumentField;

/**
 * @author t_mtormin
 *
 */
public final class Documents {

	public static DocumentField findField(Document d, String metaname) {
		return d.getFields().stream().filter(df -> df.getField().getMetaname().equals(metaname)).findAny().orElse(null);
	}

	public static double getDoubleField(Document d, String metaname) {
		return getDoubleField(d, metaname, 0d);
	}

	public static int getIntField(Document d, String metaname) {
		return getIntegerField(d, metaname, 1);
	}

	public static long getLongField(Document d, String metaname) {
		return getLongField(d, metaname, 0l);
	}

	public static String getStringField(Document d, String metaname) {
		return getStringField(d, metaname, "");
	}

	public static double getDoubleField(Document d, String metaname, double defValue) {
		return d == null || d.getFields() == null ? defValue : d.getFields().parallelStream()
						.filter(pf -> pf != null && pf.getField() != null && pf.getField().getMetaname() != null && pf.getField().getMetaname().equals(metaname) && !pf.getValue().isEmpty())
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

	public static Date getDateField(Document d, String metaname, Date defValue) {
		return d == null || d.getFields() == null ? defValue : d.getFields().parallelStream()
						.filter(pf -> pf != null && pf.getField() != null && pf.getField().getMetaname() != null && pf.getField().getMetaname().equals(metaname) && !pf.getValue().isEmpty())
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

	public static String getStringField(Document d, String metaname, String defValue) {
		return d == null || d.getFields() == null ? defValue : d.getFields().parallelStream()
						.filter(df -> df != null && df.getField() != null && df.getField().getMetaname() != null && df.getField().getMetaname().equals(metaname))
						.map(df -> df.getValue())
						.findAny()
						.orElse(defValue);
	}

	public static int getIntegerField(Document d, String metaname, int defValue) {
		return d == null || d.getFields() == null ? defValue : d.getFields().parallelStream()
						.filter(pf -> pf != null && pf.getField() != null && pf.getField().getMetaname() != null && pf.getField().getMetaname().equals(metaname) && !pf.getValue().isEmpty())
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

	public static long getLongField(Document d, String metaname, long defValue) {
		return d == null || d.getFields() == null ? defValue : d.getFields().parallelStream()
						.filter(pf -> pf != null && pf.getField() != null && pf.getField().getMetaname() != null && pf.getField().getMetaname().equals(metaname) && !pf.getValue().isEmpty())
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
