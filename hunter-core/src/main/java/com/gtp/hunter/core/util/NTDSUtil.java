package com.gtp.hunter.core.util;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NTDSUtil {

	public static String toDC(String domainName) {
		StringBuilder buf = new StringBuilder("DC=");
		String[] tokens = domainName.split("\\.");

		buf.append(Stream.of(tokens).collect(Collectors.joining(",DC=")));
		return buf.toString();
	}

	public static String decodeSID(byte[] sid) {

		final StringBuilder strSid = new StringBuilder("S-");

		// get version
		final int revision = sid[0];
		strSid.append(Integer.toString(revision));

		//next byte is the count of sub-authorities
		final int countSubAuths = sid[1] & 0xFF;

		//get the authority
		long authority = 0;
		//String rid = "";
		for (int i = 2; i <= 7; i++) {
			authority |= ((long) sid[i]) << (8 * (5 - (i - 2)));
		}
		strSid.append("-");
		strSid.append(Long.toHexString(authority));

		//iterate all the sub-auths
		int offset = 8;
		int size = 4; //4 bytes for each sub auth
		for (int j = 0; j < countSubAuths; j++) {
			long subAuthority = 0;
			for (int k = 0; k < size; k++) {
				subAuthority |= (long) (sid[offset + k] & 0xFF) << (8 * k);
			}

			strSid.append("-");
			strSid.append(subAuthority);

			offset += size;
		}

		return strSid.toString();
	}

	public static String convertToDashedString(byte[] objectGUID) {
		StringBuilder displayStr = new StringBuilder();
		displayStr.append(prefixZeros((int) objectGUID[3] & 0xFF));
		displayStr.append(prefixZeros((int) objectGUID[2] & 0xFF));
		displayStr.append(prefixZeros((int) objectGUID[1] & 0xFF));
		displayStr.append(prefixZeros((int) objectGUID[0] & 0xFF));
		displayStr.append("-");
		displayStr.append(prefixZeros((int) objectGUID[5] & 0xFF));
		displayStr.append(prefixZeros((int) objectGUID[4] & 0xFF));
		displayStr.append("-");
		displayStr.append(prefixZeros((int) objectGUID[7] & 0xFF));
		displayStr.append(prefixZeros((int) objectGUID[6] & 0xFF));
		displayStr.append("-");
		displayStr.append(prefixZeros((int) objectGUID[8] & 0xFF));
		displayStr.append(prefixZeros((int) objectGUID[9] & 0xFF));
		displayStr.append("-");
		displayStr.append(prefixZeros((int) objectGUID[10] & 0xFF));
		displayStr.append(prefixZeros((int) objectGUID[11] & 0xFF));
		displayStr.append(prefixZeros((int) objectGUID[12] & 0xFF));
		displayStr.append(prefixZeros((int) objectGUID[13] & 0xFF));
		displayStr.append(prefixZeros((int) objectGUID[14] & 0xFF));
		displayStr.append(prefixZeros((int) objectGUID[15] & 0xFF));
		return displayStr.toString();
	}

	private static String prefixZeros(int value) {
		if (value <= 0xF) {
			StringBuilder sb = new StringBuilder("0");
			sb.append(Integer.toHexString(value));

			return sb.toString();

		} else {
			return Integer.toHexString(value);
		}
	}

}
