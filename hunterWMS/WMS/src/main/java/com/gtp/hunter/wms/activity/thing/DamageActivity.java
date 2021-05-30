package com.gtp.hunter.wms.activity.thing;

import android.os.Bundle;

import androidx.fragment.app.FragmentTransaction;

import com.gtp.hunter.R;
import com.gtp.hunter.wms.fragment.thing.DamageFragment;
import com.gtp.hunter.wms.model.Thing;

import java.util.Objects;

public class DamageActivity extends ThingOperationActivity {

    private DamageFragment frag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentTransaction fragmentTransaction = manager.beginTransaction();

        frag = (DamageFragment) manager.findFragmentById(R.id.damageFragment);
        fragmentTransaction.hide(Objects.requireNonNull(frag));
        fragmentTransaction.commit();
    }

    @Override
    protected void operation(Thing t) {
        FragmentTransaction fragmentTransaction = manager.beginTransaction();

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

        fragmentTransaction.hide(frag);
        fragmentTransaction.commit();
    }

    @Override
    protected void onHideFilters() {

    }


}
