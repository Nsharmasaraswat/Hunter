package com.gtp.hunter.ui.json.process.drone;

import java.lang.invoke.MethodHandles;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.gtp.hunter.ejbcommon.util.ConfigUtil;
import com.gtp.hunter.process.model.Address;
import com.gtp.hunter.process.model.Product;
import com.gtp.hunter.process.model.ProductField;

public class CalculatedStock {
	private transient static final Logger	logger			= LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public static final Double				DRONE_HEIGHT	= Double.parseDouble(ConfigUtil.get("hunter-process", "inventory_drone_height", "600"));

	private static final short				PALLET_HEIGHT	= Short.parseShort(ConfigUtil.get("hunter-process", "inventory_pallet_height", "10"));
	private static final short				FRONT_DISTANCE	= Short.parseShort(ConfigUtil.get("hunter-process", "inventory_drone_front_distance", "100"));
	private static final Double				MAX_DISTANCE	= 1.3d * DRONE_HEIGHT;
	private static final Double				MIN_DISTANCE	= 0.06d * DRONE_HEIGHT;

	private static final DecimalFormat		DF				= new DecimalFormat("##0.##", DecimalFormatSymbols.getInstance(Locale.US));

	@Expose
	@SerializedName("address")
	private Address							address;

	@Expose
	@SerializedName("product")
	private Product							product;

	@Expose
	@SerializedName("count")
	private int								count;

	@Expose
	@SerializedName("count-average")
	private int								countAverageHeight;

	private List<Measurement>				measurementList;
	private int[]							countArr;
	private int								maxStack;
	private double							averageHeight;
	private double							averageDistance;
	private double							productHeight;
	private final double					_MARGIN;

	public CalculatedStock(Address a, Product p) {
		this.address = a;
		this.product = p;
		this.measurementList = new ArrayList<>();
		this.maxStack = a.getParent().getFields().stream()
						.filter(af -> af.getModel().getMetaname().equals("MAX_STACK"))
						.mapToInt(af -> Integer.parseInt(af.getValue()))
						.sum();
		this.countArr = new int[maxStack + 1];
		calcProductHeight();
		_MARGIN = this.productHeight * Integer.parseInt(ConfigUtil.get("hunter-process", "inventory_height_margin", "10")) / 100;
	}

	public CalculatedStock calculate() {
		try {
			int heightCount = 0, distCount = 0;
			int maxCount = 0;
			Arrays.fill(countArr, 0);

			for (Measurement m : measurementList) {
				if (m.getType() == 0 && (m.getHeight() > MAX_DISTANCE || m.getHeight() < MIN_DISTANCE)) continue;//filter lidar errors/Takeoff/Landing

				double dist = m.getType() == 0 ? m.getHeight() : m.getDistance();
				int count = count(dist, m.getType());

				switch (m.getType()) {
					case 0:
						this.countArr[count]++;
						this.averageHeight += dist;
						heightCount++;
						break;
					case 1:
						this.averageDistance += dist;
						distCount++;
						break;
				}
			}
			this.averageHeight = heightCount == 0 ? 0 : this.averageHeight / heightCount;
			this.averageDistance = distCount == 0 ? 0 : this.averageDistance / distCount;
			this.countAverageHeight = count(this.averageHeight, (short) 0);
			if (this.address.getParent().getModel().getMetaname().equals("RACK")) {
				this.count = count(this.averageDistance, (short) 1);
			} else {
				for (int i = 0; i < countArr.length; i++) {
					if (maxCount <= countArr[i]) {
						maxCount = countArr[i];
						this.count = i;
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return this;
	}

	private int count(double dist, short type) {
		boolean verbose = ConfigUtil.get("hunter-process", "verbose-process", "FALSE").equalsIgnoreCase("TRUE");
		switch (type) {
			case 0:
				double stackHeight = DRONE_HEIGHT - dist;
				if (verbose) logger.info("Vertical Distance: " + dist);
				if (productHeight > 0 && dist > 0) {
					for (int i = 1; i <= maxStack; i++) {
						if (stackHeight >= (i * productHeight - _MARGIN) && stackHeight <= (i * productHeight + _MARGIN)) {
							if (verbose) logger.info(address.getMetaname() + "Found " + i + " Pallets for product " + product.getName() + " Drone Height: " + DF.format(DRONE_HEIGHT) + " Measure: " + DF.format(dist) + " Stack Height: " + DF.format(stackHeight) + " ProductHeight: " + DF.format(productHeight) + " Margin: " + DF.format(_MARGIN));
							return i;
						}
					}
				}
				break;
			case 1://RACK
				if (dist < FRONT_DISTANCE) {
					if (verbose) logger.info("Horizontal Distance: " + dist);
					return 1;
				}
				break;
		}
		return 0;
	}

	private void calcProductHeight() {
		ProductField pfHeight = product == null ? null : product.getFields().stream()
						.filter(pf -> pf.getModel().getMetaname().equals("PALLET_HEIGHT"))
						.findAny()
						.orElse(null);

		this.productHeight = pfHeight == null ? 0 : Double.parseDouble(pfHeight.getValue()) * 100 + PALLET_HEIGHT;//cm
	}

	/**
	 * @return the address
	 */
	public Address getAddress() {
		return address;
	}

	/**
	 * @return the pallet
	 */
	public Product getProduct() {
		return product;
	}

	/**
	 * @return the productHeight
	 */
	public double getProductHeight() {
		return productHeight;
	}

	/**
	 * @return the count
	 */
	public int getCount() {
		return count;
	}

	/**
	 * @return the measurementList
	 */
	public List<Measurement> getMeasurementList() {
		return measurementList;
	}

	/**
	 * @return the averageDistance
	 */
	public double getAverageDistance() {
		return averageDistance;
	}

	/**
	 * @return the averageHeight
	 */
	public double getAverageHeight() {
		return averageHeight;
	}

	/**
	 * @return the countArr
	 */
	public int[] getCountArr() {
		return countArr;
	}

	/**
	 * @return the countAverageHeight
	 */
	public int getCountAverageHeight() {
		return countAverageHeight;
	}

	/**
	 * @return the MARGIN
	 */
	public double getMargin() {
		return _MARGIN;
	}
}