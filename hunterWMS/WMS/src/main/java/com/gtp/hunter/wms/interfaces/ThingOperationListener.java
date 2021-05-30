package com.gtp.hunter.wms.interfaces;

import com.gtp.hunter.wms.model.Thing;

public interface ThingOperationListener {

    void thingSelected(Thing t);

    void hideFilters();

    void showFilters();
}
