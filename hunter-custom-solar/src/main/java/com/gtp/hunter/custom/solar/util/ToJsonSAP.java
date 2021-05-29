package com.gtp.hunter.custom.solar.util;

import java.lang.invoke.MethodHandles;
import java.text.ParseException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sap.conn.jco.AbapException;
import com.sap.conn.jco.ConversionException;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoField;
import com.sap.conn.jco.JCoFieldIterator;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoStructure;
import com.sap.conn.jco.JCoTable;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class ToJsonSAP {
	private transient static final Logger	logger	= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private JCoFunction						function;
	private Gson							gson;
	private LinkedHashMap<String, Object>	resultLinked;

	public ToJsonSAP(JCoFunction function) {
		this.function = function;
		createJsonParser();
	}

	private void createJsonParser() {
		this.gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").setPrettyPrinting().create();
		this.resultLinked = new LinkedHashMap<String, Object>();
	}

	public String execute(JCoDestination destination) throws JCoException {
		if (function != null) {
			try {
				function.execute(destination);
			} catch (AbapException e) {
				resultLinked.put(e.getKey(), e);
			} catch (JCoException ex) {
				resultLinked.put(ex.getKey(), ex);
			}
			// Export Parameters
			if (function.getExportParameterList() != null) {
				logger.info("Export Parameter List");
				getFields(function.getExportParameterList().getFieldIterator());
			}
			// Changing parameters
			if (function.getChangingParameterList() != null) {
				logger.info("Changing Parameter List");
				getFields(function.getChangingParameterList().getFieldIterator());
			}
			// Table Parameters
			if (function.getTableParameterList() != null) {
				logger.info("Table Parameter List");
				getFields(function.getTableParameterList().getFieldIterator());
			}
		} else {
			logger.error("Function is null");
		}
		String ret = this.gson.toJson(resultLinked);
		logger.info(ret);
		return ret;
	}

	private void getFields(JCoFieldIterator iter) {
		while (iter.hasNextField()) {
			JCoField f = iter.nextField();

			if (f.isTable()) {
				logger.info("Table Field: " + f.getName());
				resultLinked.put(f.getName(), getTableParameter(f));
			} else if (f.isStructure()) {
				logger.info("Structure Field: " + f.getName());
				resultLinked.put(f.getName(), getStructureParameter(f));
			} else {
				logger.info("Field: " + f.getName());
				resultLinked.put(f.getName(), f.getValue());
			}
		}
	}

	private LinkedList<LinkedHashMap<String, Object>> getTableParameter(JCoField table) {
		LinkedList<LinkedHashMap<String, Object>> l = new LinkedList<LinkedHashMap<String, Object>>();
		JCoTable t = table.getTable();
		for (int i = 0; i < t.getNumRows(); i++) {
			t.setRow(i);
			JCoFieldIterator iter = t.getFieldIterator();
			LinkedHashMap<String, Object> m = new LinkedHashMap<String, Object>();
			while (iter.hasNextField()) {
				JCoField f = iter.nextField();

				try {
					m.put(f.getName(), t.getValue(f.getName()));
					logger.debug("Column " + f.getName());
				} catch (ConversionException ce) {
					logger.error("Error converting " + f.getName());
				}
			}
			l.add(m);
		}
		return l;
	}

	private LinkedHashMap<String, Object> getStructureParameter(JCoField structure) {
		JCoFieldIterator iter = structure.getStructure().getFieldIterator();
		LinkedHashMap<String, Object> m = new LinkedHashMap<String, Object>();

		while (iter.hasNextField()) {
			JCoField f = iter.nextField();
			m.put(f.getName(), structure.getStructure().getValue(f.getName()));
		}
		return m;
	}

	public void setParameters(String jsonParameters) throws ParseException {
		Map<String, Object> params = gson.fromJson(jsonParameters, Map.class);
		setParameters(params);
	}

	public void setParameters(Map<String, Object> Parameters) {
		Iterator iter = Parameters.entrySet().iterator();

		while (iter.hasNext()) {
			Entry parameter = (Map.Entry) iter.next();
			setParameter(parameter.getKey().toString(), parameter.getValue());
		}
	}

	public void setParameter(String name, Object value) {
		if (value instanceof LinkedList) {
			setTableParameter(name, (LinkedList) value);
		} else if (value instanceof LinkedHashMap) {
			setStructureParameter(name, (LinkedHashMap) value);
		} else {
			setSimpleParameter(name, value);
		}
	}

	public void setSimpleParameter(String name, Object value) {
		// Find Simple, non structure or table parameter with this name and set the
		// appropriate value
		// Importing Parameters
		if (function.getImportParameterList() != null) {
			setSimpleParameterValue(function.getImportParameterList().getFieldIterator(), name, value);
		}
		// Changing Parameters
		if (function.getChangingParameterList() != null) {
			setSimpleParameterValue(function.getChangingParameterList().getFieldIterator(), name, value);
		}

	}

	private void setSimpleParameterValue(JCoFieldIterator iter, String name, Object value) {

		while (iter.hasNextField()) {
			JCoField f = iter.nextField();
			if (f.getName().equals(name) && !f.isStructure() && !f.isTable()) {
				f.setValue(value);
			}
		}
	}

	public void setStructureParameter(String name, LinkedHashMap map) {
		// Find structure parameter with this name and set the appropriate values
		JCoFieldIterator iter = function.getImportParameterList().getFieldIterator();
		while (iter.hasNextField()) {
			JCoField f = iter.nextField();
			if (f.getName().equals(name) && f.isStructure()) {
				Iterator fieldIter = map.entrySet().iterator();
				JCoStructure structure = f.getStructure();
				while (fieldIter.hasNext()) {
					Entry field = (Map.Entry) fieldIter.next();
					structure.setValue(field.getKey().toString(), field.getValue().toString());
				}
			}
		}
	}

	public void setTableParameter(String name, LinkedList<LinkedHashMap<String, Object>> list) {
		// Find table parameter with this name and set the appropriate valies
		JCoFieldIterator iter = function.getTableParameterList().getFieldIterator();

		while (iter.hasNextField()) {
			JCoField f = iter.nextField();

			if (f.getName().equals(name) && f.isTable()) {
				Iterator recordIter = list.listIterator();
				JCoTable table = f.getTable();

				while (recordIter.hasNext()) {
					table.appendRow();
					LinkedHashMap<String, String> fields = (LinkedHashMap<String, String>) recordIter.next();
					Iterator fieldIter = fields.entrySet().iterator();

					while (fieldIter.hasNext()) {
						Entry field = (Map.Entry) fieldIter.next();

						table.setValue(field.getKey().toString(), field.getValue());
					}
				}
			}
		}
	}

	public LinkedHashMap<String, Object> getResultLinked() {
		return resultLinked;
	}

}
