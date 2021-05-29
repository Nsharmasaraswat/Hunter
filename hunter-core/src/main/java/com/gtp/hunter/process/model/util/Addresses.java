/**
 * 
 */
package com.gtp.hunter.process.model.util;

import java.util.Date;

import com.gtp.hunter.process.model.Address;
import com.gtp.hunter.process.model.AddressField;

/**
 * @author t_mtormin
 *
 */
public final class Addresses {

	public static AddressField findField(Address a, String metaname) {
		return a.getFields().stream().filter(af -> af.getModel().getMetaname().equals(metaname)).findAny().orElse(null);
	}

	public static double getDoubleField(Address a, String metaname) {
		return getDoubleField(a, metaname, 0d);
	}

	public static int getIntField(Address a, String metaname) {
		return getIntegerField(a, metaname, 1);
	}

	public static long getLongField(Address a, String metaname) {
		return getLongField(a, metaname, 0l);
	}

	public static String getStringField(Address a, String metaname) {
		return getStringField(a, metaname, "");
	}

	public static double getDoubleField(Address a, String metaname, double defValue) {
		assert a != null;
		return a.getFields().parallelStream()
						.filter(af -> af != null && af.getModel() != null && af.getModel().getMetaname() != null && af.getModel().getMetaname().equals(metaname) && !af.getValue().isEmpty())
						.mapToDouble(af -> {
							try {
								return Double.parseDouble(af.getValue());
							} catch (Exception e) {
								e.printStackTrace();
								return defValue;
							}
						})
						.findAny()
						.orElse(defValue);
	}

	public static Date getDateField(Address a, String metaname, Date defValue) {
		assert a != null;
		return a.getFields().parallelStream()
						.filter(af -> af != null && af.getModel() != null && af.getModel().getMetaname() != null && af.getModel().getMetaname().equals(metaname) && !af.getValue().isEmpty())
						.map(af -> {
							try {
								return CommonUtil.parseDate(af.getValue());
							} catch (Exception e) {
								e.printStackTrace();
								return defValue;
							}
						})
						.findAny()
						.orElse(defValue);
	}

	public static String getStringField(Address a, String metaname, String defValue) {
		assert a != null;
		return a.getFields().parallelStream()
						.filter(af -> af != null && af.getModel() != null && af.getModel().getMetaname() != null && af.getModel().getMetaname().equals(metaname))
						.map(af -> {
							return af.getValue();
						})
						.findAny()
						.orElse(defValue);
	}

	public static int getIntegerField(Address a, String metaname, int defValue) {
		assert a != null;
		return a.getFields().parallelStream()
						.filter(af -> af != null && af.getModel() != null && af.getModel().getMetaname() != null && af.getModel().getMetaname().equals(metaname) && !af.getValue().isEmpty())
						.mapToInt(af -> {
							try {
								return Integer.parseInt(af.getValue());
							} catch (Exception e) {
								e.printStackTrace();
								return defValue;
							}
						})
						.findAny()
						.orElse(defValue);
	}

	public static long getLongField(Address a, String metaname, long defValue) {
		assert a != null;
		return a.getFields().parallelStream()
						.filter(af -> af != null && af.getModel() != null && af.getModel().getMetaname() != null && af.getModel().getMetaname().equals(metaname) && !af.getValue().isEmpty())
						.mapToLong(af -> {
							try {
								return Long.parseLong(af.getValue());
							} catch (Exception e) {
								e.printStackTrace();
								return defValue;
							}
						})
						.findAny()
						.orElse(defValue);
	}
}
