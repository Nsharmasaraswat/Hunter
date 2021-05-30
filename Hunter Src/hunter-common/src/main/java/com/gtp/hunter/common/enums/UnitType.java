package com.gtp.hunter.common.enums;

public enum UnitType {
	QRCODE(false, 0), 
	EPC96(true, 24),
	EPC128(true, 36),
	UPCA(true, 12),
	UPCE(true, 6),
	EAN8(true, 8),
	EAN13(true, 13),
	CODE39(true, 43),
	CODE128(true, 43),
	ITF(true, 14),
	CODE93(true, 43),
	CODABAR(true, 16),
	LICENSEPLATES(false, 0),
	NFC(false, 0), 
	BLE(false, 0), 
	RTLS(false, 0),
	EXTERNAL_SYSTEM(false, 0);
	
	private boolean creatable;
	private int dataSize;
	
	private UnitType(boolean creatable, int dataSize) {
		this.creatable = creatable;
		this.dataSize = dataSize;
	}
	
	public boolean isCreatable() {
		return creatable;
	}
	public int getDataSize() {
		return dataSize;
	}
	
}