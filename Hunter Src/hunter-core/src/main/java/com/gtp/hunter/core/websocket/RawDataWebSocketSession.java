package com.gtp.hunter.core.websocket;

import com.gtp.hunter.core.model.ComplexData;

public interface RawDataWebSocketSession {

	public void onMessage(ComplexData msg);
	public void onError(Exception e);
	public void onClose();
	
}
