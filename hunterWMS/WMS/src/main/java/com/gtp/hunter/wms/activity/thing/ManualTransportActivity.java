package com.gtp.hunter.wms.activity.thing;

import android.os.Bundle;

import androidx.fragment.app.FragmentTransaction;

import com.gtp.hunter.R;
import com.gtp.hunter.wms.fragment.thing.ManualTransportFragment;
import com.gtp.hunter.wms.model.Thing;

public class ManualTransportActivity extends ThingOperationActivity {

    private ManualTransportFragment frag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        frag = (ManualTransportFragment) manager.findFragmentById(R.id.manualTransportFragment);
    }

    @Override
    protected void operation(Thing t) {
        FragmentTransaction fragmentTransaction = manager.beginTransaction();

        displayingFragment = true;
        frag.setThing(t);
        fragmentTransaction.show(frag);
        fragmentTransaction.addToBackStack(null);
        // Commit the transaction
        fragmentTransaction.commit();
        hideFilters();
    }

    @Override
    protected void onShowFilters() {
        FragmentTransaction fragmentTransaction = manager.beginTransaction();

        displayingFragment = false;
        fragmentTransaction.hide(frag);
        fragmentTransaction.commit();
    }

    @Override
    protected void onHideFilters() {

    }
}
