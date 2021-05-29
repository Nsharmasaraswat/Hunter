package com.gtp.hunter.common.util;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKBReader;

public class DBUtil {

	private transient static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public static <T> List<T> resultSetToList(ResultSet rs, Class<T> cls) {
		Map<String, Method> setters = new HashMap<String, Method>();
		try {
			Arrays.asList(Introspector.getBeanInfo(cls, Object.class).getPropertyDescriptors()).stream().filter(pd -> Objects.nonNull(pd.getWriteMethod())).forEach(pd -> {
				try {
					setters.put(pd.getName(), pd.getWriteMethod());
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		} catch (IntrospectionException e) {
			e.printStackTrace();
		}

		ArrayList ret = new ArrayList();

		try {
			ResultSetMetaData rsmd = rs.getMetaData();
			int cntColumns = rsmd.getColumnCount();

			while (rs.next()) {
				Object t = Class.forName(cls.getName()).newInstance();
				for (int cnt = 1; cnt < cntColumns; cnt++) {
					String columnTypeName = rsmd.getColumnTypeName(cnt);
					//System.out.println(rsmd.getColumnTypeName(cnt));
					if (!rsmd.getColumnName(cnt).contains("_id")) {
						switch (columnTypeName) {
							case "BIT":
								setters.get(rsmd.getColumnName(cnt)).invoke(t, rs.getInt(cnt));
								break;
							case "BLOB":
								setters.get(rsmd.getColumnName(cnt)).invoke(t, rs.getObject(cnt));
								break;
							case "DATE":
							case "DATETIME":
							case "TIMESTAMP":
								setters.get(rsmd.getColumnName(cnt)).invoke(t, rs.getTimestamp(cnt));
								break;
							case "INT":
								setters.get(rsmd.getColumnName(cnt)).invoke(t, rs.getInt(cnt));
								break;
							case "INTEGER":
								setters.get(rsmd.getColumnName(cnt)).invoke(t, rs.getInt(cnt));
								break;
							case "CHAR":
								if (rs.getString(cnt) != null)
									setters.get(rsmd.getColumnName(cnt)).invoke(t, UUID.fromString(rs.getString(cnt)));
								break;
							case "VARCHAR":
								if (setters.get(rsmd.getColumnName(cnt)).getParameterTypes()[0].isEnum()) {
									Class c = setters.get(rsmd.getColumnName(cnt)).getParameterTypes()[0];
									setters.get(rsmd.getColumnName(cnt)).invoke(t, Enum.valueOf(c, rs.getString(cnt)));
								} else {
									setters.get(rsmd.getColumnName(cnt)).invoke(t, rs.getString(cnt));
								}

								break;
							case "GEOMETRY":
								setters.get(rsmd.getColumnName(cnt)).invoke(t, getGeometryFromInputStream(rs.getBinaryStream(cnt)));
								break;

							default:
								System.out.println("Tipo desconhecido em " + t.getClass().getSimpleName() + ": " + rsmd.getColumnName(cnt) + " - " + rsmd.getColumnTypeName(cnt));
						}
					}
				}
				ret.add(t);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ret;
	}

	private static Geometry getGeometryFromInputStream(InputStream inputStream) throws Exception {

		Geometry dbGeometry = null;

		if (inputStream != null) {

			//convert the stream to a byte[] array
			//so it can be passed to the WKBReader
			byte[] buffer = new byte[255];

			int bytesRead = 0;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				baos.write(buffer, 0, bytesRead);
			}

			byte[] geometryAsBytes = baos.toByteArray();

			if (geometryAsBytes.length < 5) {
				throw new Exception("Invalid geometry inputStream - less than five bytes");
			}

			//first four bytes of the geometry are the SRID,
			//followed by the actual WKB.  Determine the SRID
			//here
			byte[] sridBytes = new byte[4];
			System.arraycopy(geometryAsBytes, 0, sridBytes, 0, 4);
			boolean bigEndian = (geometryAsBytes[4] == 0x00);

			int srid = 0;
			if (bigEndian) {
				for (int i = 0; i < sridBytes.length; i++) {
					srid = (srid << 8) + (sridBytes[i] & 0xff);
				}
			} else {
				for (int i = 0; i < sridBytes.length; i++) {
					srid += (sridBytes[i] & 0xff) << (8 * i);
				}
			}

			//use the JTS WKBReader for WKB parsing
			WKBReader wkbReader = new WKBReader();

			//copy the byte array, removing the first four
			//SRID bytes
			byte[] wkb = new byte[geometryAsBytes.length - 4];
			System.arraycopy(geometryAsBytes, 4, wkb, 0, wkb.length);
			dbGeometry = wkbReader.read(wkb);
			dbGeometry.setSRID(srid);
		}

		return dbGeometry;
	}
}
