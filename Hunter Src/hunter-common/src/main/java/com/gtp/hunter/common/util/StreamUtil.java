package com.gtp.hunter.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

public class StreamUtil {

	public static void copy(InputStream in, OutputStream out) throws IOException {

		byte[] buffer = new byte[1024];
		while (true) {
			int bytesRead = in.read(buffer);
			if (bytesRead == -1)
				break;
			out.write(buffer, 0, bytesRead);
		}
	}

	public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {

		Map<Object, Boolean> seen = new ConcurrentHashMap<>();
		return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
	}
}
