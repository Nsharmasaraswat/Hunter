package com.gtp.hunter.process.wf.process.location;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gtp.hunter.common.payload.LocatePayload;
import com.gtp.hunter.process.model.Location;
import com.gtp.hunter.process.wf.process.ContinuousProcess;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public abstract class LocationProcess extends ContinuousProcess {

	private transient static final Logger	logger	= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	protected Location						location;

	protected Location getLocation() {
		return this.location;
	}

	protected abstract void checkParamsImpl() throws Exception;

	protected abstract void initImpl() throws Exception;

	@Override
	protected final void checkParams() throws Exception {
		if (!getParametros().containsKey("location"))
			throw new Exception("Parâmetro 'location' não encontrado.");
		checkParamsImpl();
	}

	@Override
	public final void onInit() {
		try {
			String locationMeta = (String) getParametros().get("location");

			this.location = getRegSvc().getLocSvc().findByMetaname(locationMeta);
			initImpl();
		} catch (Exception e) {
			logger.error("Process caused the following error on initialization: " + e.getLocalizedMessage());
			logger.trace(e.getLocalizedMessage(), e);
		}
	}

	protected Geometry getPoint(LocatePayload lcpl) {
		Geometry g = null;
		WKTReader rdr = new WKTReader();

		try {
			String point = "POINT(";
			if (lcpl.getLatitude() != 0 && lcpl.getLongitude() != 0)
				point += lcpl.getLongitude() + " " + lcpl.getLatitude() + ")";
			else
				point += +lcpl.getX() + " " + lcpl.getY() + ")";
			g = rdr.read(point);
		} catch (ParseException pe) {
			logger.error(pe.getLocalizedMessage());
			logger.trace(pe.getLocalizedMessage(), pe);
		}
		return g;
	}
}
