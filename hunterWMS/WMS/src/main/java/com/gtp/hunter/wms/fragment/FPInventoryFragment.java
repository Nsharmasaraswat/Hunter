package com.gtp.hunter.wms.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.gtp.hunter.BuildConfig;
import com.gtp.hunter.HunterMobileWMS;
import com.gtp.hunter.R;
import com.gtp.hunter.structure.BoxCalculatorWatcher;
import com.gtp.hunter.structure.TriStateCheckBox;
import com.gtp.hunter.structure.adapter.AddressAdapter;
import com.gtp.hunter.structure.adapter.FinishedProductRecyclerViewAdapter;
import com.gtp.hunter.structure.adapter.ProductAdapter;
import com.gtp.hunter.structure.spinner.SearchableSpinner;
import com.gtp.hunter.structure.viewmodel.BaseDocumentViewModel;
import com.gtp.hunter.structure.viewmodel.InventoryViewModel;
import com.gtp.hunter.util.ProductUtil;
import com.gtp.hunter.wms.interfaces.FinishedProductListener;
import com.gtp.hunter.wms.interfaces.TaskFragmentListener;
import com.gtp.hunter.wms.model.Address;
import com.gtp.hunter.wms.model.Document;
import com.gtp.hunter.wms.model.DocumentField;
import com.gtp.hunter.wms.model.DocumentItem;
import com.gtp.hunter.wms.model.DocumentModel;
import com.gtp.hunter.wms.model.DocumentModelField;
import com.gtp.hunter.wms.model.Product;
import com.gtp.hunter.wms.model.ProductField;
import com.gtp.hunter.wms.model.ViewFinishedProductStub;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import timber.log.Timber;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link TaskFragmentListener}
 * interface.
 */
public class FPInventoryFragment extends DocumentFragment implements FinishedProductListener {

    private CopyOnWriteArrayList<ViewFinishedProductStub> fpInventoryStubs;
    private final ConcurrentHashMap<Product, List<ViewFinishedProductStub>> prdStubs = new ConcurrentHashMap<>();
    private FinishedProductRecyclerViewAdapter adapter;

    private SearchableSpinner cbxAddress;
    private SearchableSpinner cbxProduct;

    private InventoryViewModel mViewModel;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FPInventoryFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fpInventoryStubs = new CopyOnWriteArrayList<>();
        adapter = new FinishedProductRecyclerViewAdapter(this);
    }

    private void changeAddress(TriStateCheckBox tscbAddress) {
        int state = tscbAddress.getState();//-1 = tras, 0 = nada, 1 = frente
        if (state == -1 && cbxAddress.getSelectedItemPosition() > 0)
            cbxAddress.setSelection(cbxAddress.getSelectedItemPosition() - 1, true);
        else if (state == 1 && cbxAddress.getSelectedItemPosition() < cbxAddress.getAdapter().getCount() - 1)
            cbxAddress.setSelection(cbxAddress.getSelectedItemPosition() + 1, true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fp_inventory, container, false);
        View itList = view.findViewById(R.id.fpItemList);
        FloatingActionButton btnAddInv = view.findViewById(R.id.btnAddInv);
        TriStateCheckBox tscbAddress = view.findViewById(R.id.cbAutoNextAddr);
        TextInputEditText etPalletBox = view.findViewById(R.id.etPalletBoxCount);
        TextInputEditText etPalletCount = view.findViewById(R.id.etPalletCount);
        TextInputEditText etLayerBox = view.findViewById(R.id.etLayerBoxCount);
        TextInputEditText etLayerCount = view.findViewById(R.id.etLayerCount);
        TextInputEditText etBoxUnit = view.findViewById(R.id.etBoxUnitCount);
        TextInputEditText etBoxCount = view.findViewById(R.id.etBoxCount);
        TextInputEditText etUnitCount = view.findViewById(R.id.etUnitCount);
        EditText etBoxes = view.findViewById(R.id.etBoxes);
        CopyOnWriteArrayList<Product> productList = new CopyOnWriteArrayList<>(HunterMobileWMS.getPAProductList());
        AddressAdapter addressAdapter = new AddressAdapter(Objects.requireNonNull(getActivity()), R.layout.item_address_mid, HunterMobileWMS.filterAddressParent());
        ProductAdapter productAdapter = new ProductAdapter(Objects.requireNonNull(getActivity()), R.layout.item_product_mid, productList).init();
        BoxCalculatorWatcher bcw = new BoxCalculatorWatcher(etPalletBox, etPalletCount, etLayerBox, etBoxUnit, etLayerCount, etBoxCount, etUnitCount, etBoxes);
        RecyclerView recyclerView = (RecyclerView) itList;

        cbxAddress = view.findViewById(R.id.cbxAddress);
        cbxProduct = view.findViewById(R.id.cbxProduct);
        tscbAddress.setBackwardText(getString(R.string.auto_addr_backward));
        tscbAddress.setForwardText(getString(R.string.auto_addr_forward));
        tscbAddress.setNoneText(getString(R.string.auto_addr_none));
        tscbAddress.setState(1);
        etPalletCount.addTextChangedListener(bcw);
        etPalletBox.addTextChangedListener(bcw);
        etLayerCount.addTextChangedListener(bcw);
        etLayerBox.addTextChangedListener(bcw);
        etBoxUnit.addTextChangedListener(bcw);
        etBoxCount.addTextChangedListener(bcw);
        etUnitCount.addTextChangedListener(bcw);
        etPalletBox.setTransitionName("TRANS NAME");
        etPalletBox.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> Timber.d("Changed %s", v.getTransitionName()));
        btnAddInv.setOnClickListener(v -> {
            String boxStr = etBoxes.getText().toString();

            if (cbxAddress.getSelectedItemPosition() < 0) {
                Toast.makeText(getActivity(), R.string.select_address, Toast.LENGTH_LONG).show();
                return;
            }
            if (cbxProduct.getSelectedItemPosition() == 0) {
                Toast.makeText(getActivity(), R.string.select_product, Toast.LENGTH_LONG).show();
                return;
            }
            if (boxStr.isEmpty() || boxStr.equals("0")) {
                new MaterialAlertDialogBuilder(Objects.requireNonNull(getContext()))
                        .setTitle("Quantidades não preenchidas")
                        .setMessage("Deseja apenas alterar o endereço?")
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> changeAddress(tscbAddress))
                        .setNegativeButton(android.R.string.cancel, null).create().show();
                return;
            }
            Address selAddr = (Address) cbxAddress.getSelectedItem();
            Product selPrd = (Product) cbxProduct.getSelectedItem();
            String palCntStr = Objects.requireNonNull(etPalletCount.getText()).toString();
            int palCnt = palCntStr.isEmpty() ? 0 : Integer.parseInt(palCntStr);
            double boxes = Double.parseDouble(boxStr.replace(",","."));
            ViewFinishedProductStub stub = new ViewFinishedProductStub(selAddr, selPrd, palCnt, boxes);
            List<ViewFinishedProductStub> stubList = prdStubs.containsKey(stub.getProduct()) ? prdStubs.get(stub.getProduct()) : new ArrayList<>();
            Objects.requireNonNull(stubList).add(stub);
            prdStubs.put(stub.getProduct(), stubList);
            View current = Objects.requireNonNull(getActivity()).getCurrentFocus();

            if (current != null) current.clearFocus();
            fpInventoryStubs.add(stub);
            int pos = fpInventoryStubs.indexOf(stub);
            adapter.notifyItemInserted(pos);
            changeAddress(tscbAddress);
            etPalletCount.setText("");
            etLayerCount.setText("");
            etBoxCount.setText("");
            etUnitCount.setText("");
            etPalletCount.requestFocus();
            LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

            if (layoutManager != null)
                layoutManager.scrollToPositionWithOffset(pos, 0);
        });
        cbxProduct.setTitle(getString(R.string.select_product));
        cbxProduct.setAdapter(productAdapter);
        cbxProduct.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Product p = cbxProduct.getSelectedItemPosition() > 0 ? ((Product) cbxProduct.getSelectedItem()) : null;

                if (p != null) {
                    ProductField palletBox = ProductUtil.getPalletBox(p);
                    ProductField layerBox = ProductUtil.getLayerBox(p);
                    ProductField boxUnit = ProductUtil.getBoxUnit(p);

                    if (palletBox != null) etPalletBox.setText(palletBox.getValue());
                    if (layerBox != null) etLayerBox.setText(layerBox.getValue());
                    if (boxUnit != null) etBoxUnit.setText(boxUnit.getValue());
                    etPalletCount.requestFocus();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        cbxAddress.setTitle(getString(R.string.select_address));
        cbxAddress.setAdapter(addressAdapter);
        // Set the adapter
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(7);
        recyclerView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle b = getArguments();
        setHasOptionsMenu(true);

        mViewModel = new ViewModelProvider(this).get(InventoryViewModel.class);
        // Use the ViewModel
        if (b != null) {
            mViewModel.setDocument((Document) b.getSerializable("DOCUMENT"));
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.inventory, menu);
        menu.findItem(R.id.mnuResume).setVisible(BuildConfig.DEBUG);
        menu.findItem(R.id.mnuClose).setVisible(BuildConfig.DEBUG);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.mnuResume:
                displayResume();
                break;
            case R.id.mnuSend:
                buildDocumentItems();
                buildAddressDocuments();
                completeTask();
                break;
            case R.id.mnuClose:
                clearViewModel();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public int removeStub(ViewFinishedProductStub stub) {
        int ret = fpInventoryStubs.indexOf(stub);
        List<ViewFinishedProductStub> prdStubList = this.prdStubs.get(stub.getProduct());

        fpInventoryStubs.remove(stub);
        if (prdStubList != null) {
            prdStubList.remove(stub);
            if (prdStubList.isEmpty()) this.prdStubs.remove(stub.getProduct());
        }
        return ret;
    }


    @Override
    public int getItemCount() {
        return this.fpInventoryStubs.size();
    }

    @Override
    public ViewFinishedProductStub getItemAt(int position) {
        return this.fpInventoryStubs.get(position);
    }

    @Override
    public void interact(String msg) {
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> Toast.makeText(getContext(), msg, Toast.LENGTH_LONG));
    }

    @Override
    public void transform(Document d) {
        List<Address> addrList = new ArrayList<>();
        AddressAdapter adapter = (AddressAdapter) cbxAddress.getAdapter();

        mViewModel.setDocument(d);
        for (Document sib : d.getSiblings()) {
            if (sib.getCode().startsWith("ARI")) {
                boolean sibok = sib.getItems().size() > 0;

                for (DocumentItem sibDi : sib.getItems())
                    if (sibDi.getQty() != 0) {
                        sibok = true;
                        break;
                    }
                if (!sibok)
                    for (DocumentField df : sib.getFields())
                        addrList.add(HunterMobileWMS.findAddress(UUID.fromString(df.getValue())));
            }
        }
        adapter.setAddresses(HunterMobileWMS.sortAddresses(addrList));
        adapter.notifyDataSetChanged();
    }

    @Override
    public BaseDocumentViewModel getViewModel() {
        return mViewModel;
    }

    @Override
    public void clearViewModel() {
        mViewModel.setDocument(null);
        prdStubs.clear();
        fpInventoryStubs.clear();
        cbxProduct.setSelection(0);
        cbxAddress.setSelection(0);
        adapter.notifyDataSetChanged();
        getActionListener().returnFromFragment();
    }

    public void displayResume() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(Objects.requireNonNull(getActivity()))
                .setIcon(android.R.drawable.ic_dialog_info)
                .setNeutralButton(android.R.string.ok, null);
        CharSequence[] itemArray = new CharSequence[this.prdStubs.size()];
        int idx = 0;

        for (Map.Entry<Product, List<ViewFinishedProductStub>> e : this.prdStubs.entrySet()) {
            List<ViewFinishedProductStub> stList = e.getValue();
            double boxCount = 0d;

            for (ViewFinishedProductStub s : stList)
                boxCount += s.getBoxes();
            String txt = getActivity().getString(R.string.dyn_string_number, e.getKey().getSku() + " - " + e.getKey().getName(), (int) boxCount);
            Timber.d("Resume %s", txt);
            itemArray[idx++] = txt;
        }
        builder.setItems(itemArray, null).create().show();
    }

    public void buildDocumentItems() {
        mViewModel.getDocument().getItems().clear();

        for (Map.Entry<Product, List<ViewFinishedProductStub>> e : this.prdStubs.entrySet()) {
            Product p = e.getKey();
            List<ViewFinishedProductStub> stList = e.getValue();
            DocumentItem di = new DocumentItem();
            double palletCount = 0d;
            double boxCount = 0d;

            for (ViewFinishedProductStub s : stList) {
                palletCount += s.getPallets();
                boxCount += s.getBoxes();
            }
            di.setProduct(p);
            di.setQty(boxCount);
            di.getProps().put("volumes", String.valueOf((int) palletCount));
            di.setMeasureUnit(ProductUtil.getMeasureUnit(p).getValue());
            mViewModel.getDocument().getItems().add(di);
        }
    }

    public void buildAddressDocuments() {
        DocumentModel dm = new DocumentModel();

        dm.setId(UUID.fromString("f4c71ad0-8581-11ea-9d3c-005056a19775"));
        dm.setMetaname("APORUAINV");
        mViewModel.getDocument().getSiblings().clear();
        for (ViewFinishedProductStub s : this.fpInventoryStubs) {
            boolean found = false;
            Address a = s.getAddress();
            Product p = s.getProduct();
            Document d = findByAddrId(a.getId().toString());

            if (d.getStatus() == null || d.getStatus().isEmpty()) {
                DocumentModelField dmfAddr = new DocumentModelField();
                DocumentModelField dmfPrd = new DocumentModelField();
                DocumentField dfAddr = new DocumentField();
                DocumentField dfPrd = new DocumentField();

                dmfAddr.setId(UUID.fromString("2a56b614-8582-11ea-9d3c-005056a19775"));
                dmfAddr.setMetaname("INVADDRESS");

                dfAddr.setValue(a.getId().toString());
                dfAddr.setField(dmfAddr);
                dfAddr.setModelId(dmfAddr.getId());

                dmfPrd.setId(UUID.fromString("78521247-8582-11ea-9d3c-005056a19775"));
                dmfPrd.setMetaname("INVADDRESSPRD");

                dfPrd.setField(dmfPrd);
                dfPrd.setValue(p.getId().toString());
                dfPrd.setModelId(dmfPrd.getId());

                d.getFields().add(dfAddr);
                d.getFields().add(dfPrd);
                d.setModel(dm);
                d.setStatus("NOVO");
            }

            for (DocumentItem di : d.getItems()) {
                if (Objects.equals(di.getProduct(), p)) {
                    di.setQty((di.getQty() + s.getBoxes()));
                    di.getProps().put("VOLUMES", String.valueOf(Integer.parseInt(Objects.requireNonNull(di.getProps().get("VOLUMES"))) + s.getPallets()));
                    found = true;
                }
            }
            if (!found) {
                DocumentItem di = new DocumentItem();

                di.setProduct(p);
                di.setQty(s.getBoxes());
                di.getProps().put("VOLUMES", String.valueOf(s.getPallets()));
                di.setMeasureUnit(ProductUtil.getMeasureUnit(p).getValue());
                d.getItems().add(di);
            }
            mViewModel.getDocument().getSiblings().add(d);
        }
    }

    private Document findByAddrId(String addrId) {
        for (int i = 0; i < mViewModel.getDocument().getSiblings().size(); i++) {
            Document ds = mViewModel.getDocument().getSiblings().get(i);

            for (DocumentField f : ds.getFields()) {
                if (f.getValue().equals(addrId))
                    return mViewModel.getDocument().getSiblings().remove(i);
            }
        }
        return new Document();
    }
}
