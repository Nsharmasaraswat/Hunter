package com.gtp.hunter.structure;

import com.gtp.hunter.R;

public enum Step {
    ORIGIN(R.string.transport_origin),
    LIFT(R.string.transport_lift),
    DESTINATION(R.string.transport_destination),
    DROP(R.string.trasnport_drop),
    ADDRESS(R.string.pick_address),
    PICK(R.string.pick),
    PICK_FINISH(R.string.pick_finish),
    STAGE(R.string.stage),
    PALLET(R.string.pallet_tara);

    private Integer stepAction;

    Step(Integer s) {
        stepAction = s;
    }

    public Integer getResourceId() {
        return stepAction;
    }
}