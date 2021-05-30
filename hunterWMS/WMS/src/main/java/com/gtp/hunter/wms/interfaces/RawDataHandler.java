package com.gtp.hunter.wms.interfaces;

import com.gtp.hunter.wms.model.Rawdata;
import com.gtp.hunter.wms.model.RawdataPayload;

import org.jetbrains.annotations.NotNull;

public interface RawDataHandler<T extends RawdataPayload> {

    void rawdata(@NotNull Rawdata<T> rd);

}
