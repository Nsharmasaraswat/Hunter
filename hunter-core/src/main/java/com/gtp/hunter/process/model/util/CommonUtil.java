package com.gtp.hunter.process.model.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

class CommonUtil {

	static Date parseDate(String sdt) {
		try {
			return new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US).parse(sdt);
		} catch (ParseException pe0) {
			try {
				return new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'", Locale.US).parse(sdt);
			} catch (ParseException pe1) {
				try {
					return new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss", Locale.US).parse(sdt);
				} catch (ParseException pe2) {
					try {
						return new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(sdt);
					} catch (ParseException pe3) {
						try {
							return new SimpleDateFormat("dd/MM/yyyy hh:mm:ss", Locale.US).parse(sdt);
						} catch (ParseException pe4) {
							try {
								return new SimpleDateFormat("dd/MM/yyyy", Locale.US).parse(sdt);
							} catch (ParseException pe5) {
								return new Date();
							}
						}
					}
				}
			}
		}
	}
}
