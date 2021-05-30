package com.gtp.hunter.core.enums;

public enum AddressType {
	LOT(1),
	BLOCK(2),
	RACK(3),
	DRIVEIN(4),
	PICKING(5),
	DOCK(6),
	TRUCK(7);
	
	private final int value;

    AddressType(final int newValue) {
        value = newValue;
    }

    public int getValue() { return value; }
}
