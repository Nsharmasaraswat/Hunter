package com.gtp.hunter.custom.solar.sap;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.conn.jco.ext.DataProviderException;
import com.sap.conn.jco.ext.DestinationDataEventListener;
import com.sap.conn.jco.ext.DestinationDataProvider;

/**
 * Each application using Java Connector 3 deals with destinations. A destination represents a logical address of an ABAP system and thus separates the destination configuration from application logic. JCo retrieves the destination parameters required at runtime from DestinationDataProvider and ServerDataProvider registered in the runtime environment. If no provider is registered, JCo uses the default implementation that reads the configuration from a properties file. It is expected that each environment provides a suitable implementation that meets security and other requirements. Furthermore, only one DestinationDataProvider and only one ServerDataProvider can be registered per process. The reason behind this design decision is the following: the provider implementations are part of the environment infrastructure. The implementation should not be application specific, and in particular must be separated from application logic.
 * 
 * This example demonstrates a simple implementation of the DestinationDataProvider interface and shows how to register it. A real world implementation should save the configuration data in a secure way.
 */
public class SAPDestinationProvider {

	private transient static final Logger	logger		= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private SAPDestinationDataProvider		myProvider	= new SAPDestinationDataProvider();

	public SAPDestinationProvider(String destName, Properties properties) {
		// register the provider with the JCo environment;
		// catch IllegalStateException if an instance is already registered
		try {
			logger.info("Registering Destination Data Provider");
			com.sap.conn.jco.ext.Environment.registerDestinationDataProvider(myProvider);
			logger.info("Changing Provider Properties");
			myProvider.changeProperties(destName, properties);
		} catch (IllegalStateException providerAlreadyRegisteredException) {
			// somebody else registered its implementation,
			// stop the execution
			logger.warn(destName + " already implemented!");
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage());
			//logger.trace(e.getLocalizedMessage(), e);
		}
	}

	// The custom destination data provider implements DestinationDataProvider and
	// provides an implementation for at least getDestinationProperties(String).
	// Whenever possible the implementation should support events and notify the JCo runtime
	// if a destination is being created, changed, or deleted. Otherwise JCo runtime
	// will check regularly if a cached destination configuration is still valid which incurs
	// a performance penalty.
	static class SAPDestinationDataProvider implements DestinationDataProvider {

		private DestinationDataEventListener	eL;
		private HashMap<String, Properties>		secureDBStorage	= new HashMap<String, Properties>();

		public Properties getDestinationProperties(String destinationName) {
			try {
				logger.info("Getting Destination Properties");
				// read the destination from DB
				Properties p = secureDBStorage.get(destinationName);

				if (p != null) {
					// check if all is correct, for example
					if (p.isEmpty()) {
						logger.info("Destination Properties is empty");
						throw new DataProviderException(DataProviderException.Reason.INVALID_CONFIGURATION, "destination configuration is incorrect", null);
					}
					logger.info("Destination Properties: " + p);
					return p;
				}
				logger.info("Destination Properties is null");
				return null;
			} catch (RuntimeException re) {
				throw new DataProviderException(DataProviderException.Reason.INTERNAL_ERROR, re);
			}
		}

		// An implementation supporting events has to retain the eventListener instance provided
		// by the JCo runtime. This listener instance shall be used to notify the JCo runtime
		// about all changes in destination configurations.
		public void setDestinationDataEventListener(DestinationDataEventListener eventListener) {
			this.eL = eventListener;
		}

		public boolean supportsEvents() {
			return true;
		}

		// implementation that saves the properties in a very secure way
		void changeProperties(String destName, Properties properties) {
			synchronized (secureDBStorage) {
				if (properties == null) {
					if (secureDBStorage.remove(destName) != null)
						eL.deleted(destName);
				} else {
					secureDBStorage.put(destName, properties);
					eL.updated(destName); // create or updated
				}
			}
		}
	}
}
