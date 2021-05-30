package com.gtp.hunter.ui.json.process.drone;

import com.vividsolutions.jts.geom.Point;

public class Measurement {

	private Point	position;
	private double	height;
	private double	distance;
	private double	variation;
	private short	type;//0=below, 1=front

	public Measurement(Point position, double height, double distance, double variation, short type) {
		this.position = position;
		this.height = height;
		this.distance = distance;
		this.variation = variation;
		this.type = type;
	}

	/**
	 * @return the position
	 */
	public Point getPosition() {
		return position;
	}

	/**
	 * @param position the position to set
	 */
	public void setPosition(Point position) {
		this.position = position;
	}

	/**
	 * @return the height
	 */
	public double getHeight() {
		return height;
	}

	/**
	 * @param height the height to set
	 */
	public void setHeight(double height) {
		this.height = height;
	}

	/**
	 * @return the distance
	 */
	public double getDistance() {
		return distance;
	}

	/**
	 * @param distance the distance to set
	 */
	public void setDistance(double distance) {
		this.distance = distance;
	}

	/**
	 * @return the variation
	 */
	public double getVariation() {
		return variation;
	}

	/**
	 * @param variation the variation to set
	 */
	public void setVariation(double variation) {
		this.variation = variation;
	}

	/**
	 * @return the type
	 */
	public short getType() {
		return type;
	}

	/**
	 * @param type the typeto set
	 */
	public void setType(short type) {
		this.type = type;
	}
}
