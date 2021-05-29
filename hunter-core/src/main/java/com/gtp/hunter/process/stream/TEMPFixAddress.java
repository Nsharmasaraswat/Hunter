package com.gtp.hunter.process.stream;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.gtp.hunter.ejbcommon.util.ConfigUtil;
import com.gtp.hunter.process.model.Address;
import com.gtp.hunter.process.model.AddressField;
import com.gtp.hunter.process.service.RegisterService;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

@Startup
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
public class TEMPFixAddress {

	private static final double	SPACING	= 0d;

	@Inject
	private Logger				logger;

	@Inject
	private RegisterService		regSvc;

	private enum Direction {
		LEFT_RIGHT, BOTTOM_UP, RIGHT_LEFT, TOP_DOWN;
	}

	@PostConstruct
	public void init() {
		if (ConfigUtil.get("hunter-process", "fix-address", "false").equalsIgnoreCase("TRUE")) {
			logger.info("*****************************************FIX ADDRESS START******************************");
			List<Address> addList = regSvc.getAddSvc().listByModelMetaname("ROAD");

			addList.removeIf(a -> a.getMetaname().startsWith("PATIO") || a.getMetaname().startsWith("EXTE"));
			for (Address r : addList) {
				Supplier<Stream<AddressField>> sup = () -> r.getFields().stream();
				Envelope envelope = r.getRegion().getEnvelopeInternal();
				Coordinate ll = new Coordinate(envelope.getMinX(), envelope.getMinY());
				Coordinate ur = new Coordinate(envelope.getMaxX(), envelope.getMaxY());
				AddressField afStack = sup.get().filter(af -> af.getModel().getMetaname().equals("MAX_STACK")).findAny().orElse(null);
				AddressField afSideQt = sup.get().filter(af -> af.getModel().getMetaname().equals("QUANTITY_WIDTH")).findAny().orElse(null);
				AddressField afDir = sup.get().filter(af -> af.getModel().getMetaname().equals("DIRECTION")).findAny().orElse(null);
				AddressField afOffset = sup.get().filter(af -> af.getModel().getMetaname().equals("OFFSET")).findAny().orElse(null);
				AddressField afPLength = sup.get().filter(af -> af.getModel().getMetaname().equals("PALLET_LENGTH")).findAny().orElse(null);
				int stack = afStack == null ? 0 : Integer.valueOf(afStack.getValue());
				int intDir = afDir == null ? 0 : Integer.valueOf(afDir.getValue());
				int offset = afOffset == null ? 0 : Integer.valueOf(afOffset.getValue());
				int sideQt = afSideQt == null ? 0 : Integer.valueOf(afSideQt.getValue());
				int palletLength = afPLength == null ? 0 : Integer.valueOf(afPLength.getValue());
				Direction dir = Direction.values()[intDir];

				if (afStack != null) {
					//					logger.info("Fixing " + r.getName() + " - " + r.getRegion().toText());
					fix(r.getSiblings(), ll, ur, stack, sideQt, dir, offset, palletLength);
				} else
					logger.info("NOT Fixing " + r.getName() + " - " + r.getRegion().toText());
			}
			addList.clear();
			addList.addAll(regSvc.getAddSvc().listByModelMetaname("BLOCK"));

			for (Address r : addList) {
				Envelope envelope = r.getRegion().getEnvelopeInternal();
				Coordinate ll = new Coordinate(envelope.getMinX(), envelope.getMinY());
				Coordinate ur = new Coordinate(envelope.getMaxX(), envelope.getMaxY());

				//				logger.info("Fixing " + r.getName() + " - " + r.getRegion().toText());
				if (r.getMetaname().startsWith("PK")) {
					int pos = Integer.parseInt(r.getMetaname().replace("PK", ""));

					if ((pos >= 1 && pos <= 18))
						fix(r.getSiblings(), ll, ur, 1, 1, Direction.BOTTOM_UP, 0, 120);
					else if ((pos >= 19 && pos <= 34) || (pos >= 51 && pos <= 66) || (pos >= 83 && pos <= 98) || (pos >= 115 && pos <= 130) || (pos >= 147 && pos <= 162) || (pos >= 179 && pos <= 194))
						fix(r.getSiblings(), ll, ur, 1, 1, Direction.LEFT_RIGHT, 0, 120);
					else if ((pos >= 35 && pos <= 50) || (pos >= 67 && pos <= 82) || (pos >= 99 && pos <= 114) || (pos >= 131 && pos <= 146) || (pos >= 163 && pos <= 178) || (pos >= 195 && pos <= 228))
						fix(r.getSiblings(), ll, ur, 1, 1, Direction.RIGHT_LEFT, 0, 120);
					else
						fix(r.getSiblings(), ll, ur, 1, 1, Direction.TOP_DOWN, 0, 120);
				}
			}

			addList.clear();
			addList.addAll(regSvc.getAddSvc().listByModelMetaname("RACK"));

			for (Address r : addList) {
				Envelope envelope = r.getRegion().getEnvelopeInternal();
				Coordinate ll = new Coordinate(envelope.getMinX(), envelope.getMinY());
				Coordinate ur = new Coordinate(envelope.getMaxX(), envelope.getMaxY());
				AddressField afDir = r.getFields().stream().filter(af -> af.getModel().getMetaname().equals("DIRECTION")).findAny().orElse(null);
				int intDir = afDir == null ? 0 : Integer.valueOf(afDir.getValue());
				Direction dir = Direction.values()[intDir];
				//double length = (dir == Direction.BOTTOM_UP || dir == Direction.TOP_DOWN) ? ur.y - ll.y : ur.x - ll.x;
				double length = 120;
				int qty = r.getSiblings().size();
				//				logger.info("Fixing " + r.getName() + " - " + r.getRegion().toText());

				fix(r.getSiblings(), ll, ur, 1, 1, dir, 0, length / qty);
			}

			logger.info("*****************************************FIX ADDRESS END********************************");
		}
	}

	private void fix(Set<Address> addrSet, Coordinate ll, Coordinate ur, int stack, int sideQt, Direction direction, int offset, double deltaLength) {
		AtomicInteger index = new AtomicInteger(0);

		if (sideQt <= 0)
			throw new IllegalArgumentException("Side Quantity should be greater than 0");
		if (stack <= 0)
			throw new IllegalArgumentException("Stack should be greater than 0");
		addrSet.stream().sorted((Address o1, Address o2) -> {
			if (o1 == null && o2 == null) return 0;
			if (o2 == null) return 1;
			if (o1 == null) return -1;
			AddressField ordF1 = o1.getFields().stream()
							.filter(af -> af.getModel().getMetaname().equals("ROAD_SEQ"))
							.findFirst()
							.orElse(null);
			AddressField ordF2 = o2.getFields().stream().filter(af -> af.getModel().getMetaname().equals("ROAD_SEQ"))
							.findFirst()
							.orElse(null);
			if (ordF1 == null && ordF2 == null) return 0;
			if (ordF2 == null) return 1;
			if (ordF1 == null) return -1;
			int ord1 = Integer.parseInt(ordF1.getValue());
			int ord2 = Integer.parseInt(ordF2.getValue());
			return ord1 - ord2;
		}).forEach(a -> {
			int w = (int) Math.floor(index.get() % sideQt);
			int i = (int) Math.floor(index.get() / (stack * sideQt));
			Coordinate aLL = null;
			Coordinate aUR = null;
			double deltaWidth = 0d;

			switch (direction) {
				case BOTTOM_UP:
					deltaWidth = (ur.x - ll.x) / sideQt;
					aLL = new Coordinate(ll.x + w * deltaWidth, ll.y + (i * SPACING) + (i + offset) * deltaLength);
					aUR = new Coordinate(ll.x + (w + 1) * deltaWidth, ll.y + (i * SPACING) + (i + 1 + offset) * deltaLength);
					break;
				case RIGHT_LEFT:
					deltaWidth = (ur.y - ll.y) / sideQt;
					aLL = new Coordinate(ur.x - (i + 1 - offset) * deltaLength - (i * SPACING), ll.y + w * deltaWidth);
					aUR = new Coordinate(ur.x - (i - offset) * deltaLength - (i * SPACING), ll.y + (w + 1) * deltaWidth);
					break;
				case TOP_DOWN:
					deltaWidth = (ur.x - ll.x) / sideQt;
					aLL = new Coordinate(ll.x + w * deltaWidth, ur.y - (i * SPACING) - (i - offset) * deltaLength);
					aUR = new Coordinate(ll.x + (w + 1) * deltaWidth, ur.y - (i * SPACING) - (i + 1 - offset) * deltaLength);
					break;
				case LEFT_RIGHT:
					deltaWidth = (ur.y - ll.y) / sideQt;
					aLL = new Coordinate(ll.x + (i + offset) * deltaLength + (i * SPACING), ll.y + w * deltaWidth);
					aUR = new Coordinate(ll.x + (i + 1 + offset) * deltaLength + (i * SPACING), ll.y + (w + 1) * deltaWidth);
					break;
			}

			Envelope env = new Envelope(aLL, aUR);
			GeometryFactory gf = new GeometryFactory();
			Geometry newArea = gf.toGeometry(env);
			a.setRegion(newArea);
			logger.debug("Index: " + index.get() + " i: " + i + " w: " + w);
			index.getAndIncrement();
		});
	}
}
