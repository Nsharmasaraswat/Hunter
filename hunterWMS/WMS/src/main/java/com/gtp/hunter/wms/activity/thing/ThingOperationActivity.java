package com.gtp.hunter.wms.activity.thing;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.gtp.hunter.HunterMobileWMS;
import com.gtp.hunter.R;
import com.gtp.hunter.structure.adapter.AddressAdapter;
import com.gtp.hunter.structure.adapter.ProductAdapter;
import com.gtp.hunter.structure.adapter.ThingOperationRecyclerViewAdapter;
import com.gtp.hunter.structure.spinner.SearchableSpinner;
import com.gtp.hunter.wms.activity.LoginActivity;
import com.gtp.hunter.wms.api.HunterURL;
import com.gtp.hunter.wms.client.ThingClient;
import com.gtp.hunter.wms.interfaces.ThingOperationListener;
import com.gtp.hunter.wms.model.Address;
import com.gtp.hunter.wms.model.Product;
import com.gtp.hunter.wms.model.Thing;
import com.gtp.hunter.wms.model.ViewPalletStub;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class ThingOperationActivity extends AppCompatActivity implements Observer, ThingOperationListener {

    private ThingClient tClient;

    private ThingOperationRecyclerViewAdapter adapter;
    private CopyOnWriteArrayList<ViewPalletStub> things;
    private CopyOnWriteArrayList<ViewPalletStub> fullThingsList;

    private ConstraintLayout baseLayout;
    private ProgressBar pgbLoad;
    private RecyclerView thingListView;
    private SearchableSpinner cbxAddress;
    private SearchableSpinner cbxProduct;
    private AddressAdapter addrAdapter;
    private Handler mainHandler;
    private long start = 0;

    protected boolean displayingFragment;

    protected FragmentManager manager = getSupportFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentTransaction fragmentTransaction = manager.beginTransaction();
        setContentView(R.layout.activity_thing_operation);
        //String warehouseType = savedInstanceState.getBundle("PARAMS");
        cbxProduct = findViewById(R.id.cbxMtrProduct);
        cbxAddress = findViewById(R.id.cbxMtrAddress);
        CopyOnWriteArrayList<Product> productList = new CopyOnWriteArrayList<>(HunterMobileWMS.getPAProductList());

        mainHandler = new Handler();
        baseLayout = findViewById(R.id.mtrLayout);
        pgbLoad = findViewById(R.id.pgbLoad);
        cbxProduct.setVisibility(View.VISIBLE);
        cbxProduct.setTitle(getString(R.string.select_product));
        cbxProduct.setAdapter(new ProductAdapter(this, R.layout.item_product, productList).init());
        cbxProduct.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                Product p = cbxProduct.getSelectedItemPosition() > 0 ? ((Product) cbxProduct.getSelectedItem()) : null;

                if (p != null) {
                    pgbLoad.setVisibility(View.VISIBLE);
                    cbxAddress.setSelection(0);
                    start = SystemClock.elapsedRealtime();
                    tClient.asyncListByProductNotStatus(p, "EXPEDIDO");
                    showFullScreen();
                } else {
                    onNothingSelected(parent);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.e("No Product", "Selected");
                things.clear();
                mainHandler.postDelayed(() -> showFullScreen(), 100);
                showFullScreen();
            }
        });
        addrAdapter = new AddressAdapter(this, R.layout.item_address, filterAddressParent());
        cbxAddress.setTitle(getString(R.string.select_address));
        cbxAddress.setAdapter(addrAdapter.init());
        cbxAddress.setTag(0);
        cbxAddress.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (cbxAddress.getTag() != null && (int) cbxAddress.getTag() != pos) {
                    if (cbxProduct.getSelectedItemPosition() > 0) {
                        if (cbxAddress.getSelectedItemPosition() > 0) {
                            filterThings();
                            showFullScreen();
                        } else {
                            onNothingSelected(parent);
                        }
                    } else {
                        Address a = (Address) cbxAddress.getSelectedItem();

                        if (a != null) {
                            pgbLoad.setVisibility(View.VISIBLE);
                            cbxProduct.setSelection(0);
                            start = SystemClock.elapsedRealtime();
                            tClient.asyncListByAddressChildren(a);
                            showFullScreen();
                        } else {
                            onNothingSelected(parent);
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.e("No Address", "Selected");
                if (cbxAddress.getSelectedItemPosition() == 0) {
                    things.clear();
                    things.addAll(fullThingsList);
                }
                showFullScreen();
            }
        });
        if (HunterURL.BASE == null) {
            onNavigateUp();
        }
        things = new CopyOnWriteArrayList<>();
        adapter = new ThingOperationRecyclerViewAdapter(things, this);

        thingListView = findViewById(R.id.lstMtrThing);

        //thingListView.setLayoutManager(new WrapContentLinearLayoutManager(getBaseContext(), LinearLayoutManager.VERTICAL, false));
        //thingListView.setItemAnimator(new NoAnimationItemAnimator());
        thingListView.setAdapter(adapter);

        fragmentTransaction.hide(Objects.requireNonNull(manager.findFragmentById(R.id.manualTransportFragment)));
        fragmentTransaction.hide(Objects.requireNonNull(manager.findFragmentById(R.id.damageFragment)));
        fragmentTransaction.commit();
    }

    private void filterThings() {
        if (fullThingsList != null) {
            List<ViewPalletStub> orig = new ArrayList<>(fullThingsList);
            things.clear();

            if (cbxAddress.getSelectedItemPosition() > 0) {
                Address a = (Address) cbxAddress.getSelectedItem();

                for (ViewPalletStub tStub : orig) {
                    if (tStub.getThing().getAddress().getParent_id().equals(a.getId()))
                        things.add(tStub);
                }
            }
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        tClient = new ThingClient(this);
        tClient.addObserver(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        things.clear();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (HunterMobileWMS.getUser() != null) {
            showFullScreen();
        } else {
            backToLogin();
        }
    }

    private CopyOnWriteArrayList<Address> filterAddressParent() {
        CopyOnWriteArrayList<Address> addressList = new CopyOnWriteArrayList<>(HunterMobileWMS.getAddressList("ROAD"));
        addressList.addAll(HunterMobileWMS.getAddressList("BLOCK"));
        addressList.addAll(HunterMobileWMS.getAddressList("RACK"));
        addressList.addAll(HunterMobileWMS.getAddressList("DRIVE-IN"));
        addressList.addAll(HunterMobileWMS.getAddressList("DOCK"));
        Address[] ar = new Address[addressList.size()];
        Comparator<Address> compareByMetaname = (Address o1, Address o2) -> {
            if (o1 == null && o2 == null) return 0;
            if (o2 == null) return -1;
            if (o1 == null) return 1;
            if (o1.getMetaname() == null && o2.getMetaname() == null) return 0;
            if (o2.getMetaname() == null) return -1;
            if (o1.getMetaname() == null) return 1;
            return o1.getMetaname().compareTo(o2.getMetaname());
        };

        ar = addressList.toArray(ar);
        Arrays.sort(ar, compareByMetaname);
        addressList.clear();
        addressList.addAll(Arrays.asList(ar));
        return addressList;
    }

    @Override
    public void update(Observable observable, @NonNull Object o) {
        Log.e("Download Things", "" + (SystemClock.elapsedRealtime() - start));
        pgbLoad.setVisibility(View.GONE);
        if (o instanceof Thing) {
            Thing t = (Thing) o;

            if (t.getSiblings().size() > 0)
                operation(t);
        } else if (o instanceof List) {
            List<ViewPalletStub> recLst = new ArrayList<>();
            List<Address> addrList = new ArrayList<>();
            List<Thing> tList = (List<Thing>) o;

            for (Thing t : tList) {
                if (t.getAddress() != null) {
                    Address parent = HunterMobileWMS.findAddress(t.getAddress().getParent_id());

                    if (!addrList.contains(parent))
                        addrList.add(parent);
                    recLst.add(new ViewPalletStub(t));
                }
            }
            things.clear();
            things.addAll(sortPalletList(recLst));
            fullThingsList = new CopyOnWriteArrayList<>(things);
            runOnUiThread(() -> {
                if (addrList.size() > 0) {
                    addrAdapter.setAddresses(addrList);
                    addrAdapter.sortList(false);
                    addrAdapter.init();
                }
                adapter.notifyDataSetChanged();
            });
            Log.e("Load Things", "" + (SystemClock.elapsedRealtime() - start));
        }
    }

    protected List<ViewPalletStub> sortPalletList(List<ViewPalletStub> recLst) {
        ViewPalletStub[] ar = new ViewPalletStub[recLst.size()];

        ar = recLst.toArray(ar);
        Arrays.sort(ar, Collections.reverseOrder());
        return Arrays.asList(ar);
    }


    private void showFullScreen() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        baseLayout.setVisibility(View.VISIBLE);
        baseLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    private void backToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public void thingSelected(Thing t) {
        //fetch pallet
        tClient.findParent(t);
        pgbLoad.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideFilters() {
        cbxProduct.setVisibility(View.INVISIBLE);
        cbxAddress.setVisibility(View.INVISIBLE);
        thingListView.setVisibility(View.INVISIBLE);
        onHideFilters();
    }

    @Override
    public void showFilters() {
        cbxProduct.setVisibility(View.VISIBLE);
        cbxAddress.setVisibility(View.VISIBLE);
        thingListView.setVisibility(View.VISIBLE);
        if (cbxProduct.getSelectedItemPosition() == 0) {
            addrAdapter.setAddresses(filterAddressParent());
            addrAdapter.init();
        }
        showFullScreen();
        onShowFilters();
    }

    @Override
    public void onBackPressed() {
        if (displayingFragment)
            showFilters();
        else
            super.onBackPressed();
    }

    protected abstract void operation(Thing t);

    protected abstract void onShowFilters();

    protected abstract void onHideFilters();
}
