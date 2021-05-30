package com.gtp.hunter.wms.fragment;

import android.os.Bundle;
import android.os.Looper;
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
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.gtp.hunter.BuildConfig;
import com.gtp.hunter.HunterMobileWMS;
import com.gtp.hunter.R;
import com.gtp.hunter.structure.AsyncSendDocument;
import com.gtp.hunter.structure.BoxCalculatorWatcher;
import com.gtp.hunter.structure.adapter.FinishedProductRecyclerViewAdapter;
import com.gtp.hunter.structure.adapter.ItemDiffAdapter;
import com.gtp.hunter.structure.adapter.ProductAdapter;
import com.gtp.hunter.structure.spinner.SearchableSpinner;
import com.gtp.hunter.structure.viewmodel.BaseDocumentViewModel;
import com.gtp.hunter.structure.viewmodel.CheckingViewModel;
import com.gtp.hunter.util.DocumentUtil;
import com.gtp.hunter.util.ProductUtil;
import com.gtp.hunter.wms.interfaces.FinishedProductListener;
import com.gtp.hunter.wms.interfaces.TaskFragmentListener;
import com.gtp.hunter.wms.model.Document;
import com.gtp.hunter.wms.model.DocumentField;
import com.gtp.hunter.wms.model.DocumentItem;
import com.gtp.hunter.wms.model.DocumentModel;
import com.gtp.hunter.wms.model.Product;
import com.gtp.hunter.wms.model.ProductField;
import com.gtp.hunter.wms.model.ViewFinishedProductStub;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
public class FPPalletCheckingFragment extends DocumentFragment implements FinishedProductListener {
    private static final int MAX_ERROR_COUNT = 3;
    private final ConcurrentHashMap<UUID, List<ViewFinishedProductStub>> prdStubs = new ConcurrentHashMap<>();
    private final Product prdPallet = HunterMobileWMS.findProduct(UUID.fromString("95b564e9-ea5a-4caa-adbe-06fc7dd0b966"));//Pallet
    private final Set<Product> checkingProducts = new HashSet<>();
    private final Map<UUID, Double> confMap = new ConcurrentHashMap<>();
    private final Map<String, Double> caskConfMap = new ConcurrentHashMap<>();
    private final Map<String, List<UUID>> caskMap = new ConcurrentHashMap<>();

    private CopyOnWriteArrayList<ViewFinishedProductStub> fpCheckingStubs;
    private FinishedProductRecyclerViewAdapter adapter;
    private CopyOnWriteArrayList<Product> productList;

    private SearchableSpinner cbxProduct;
    private MenuItem mnuDiff;

    private CheckingViewModel mViewModel;
    private boolean allowOtherProducts;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FPPalletCheckingFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fpCheckingStubs = new CopyOnWriteArrayList<>();
        adapter = new FinishedProductRecyclerViewAdapter(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fp_pallet_checking, container, false);
        View itList = view.findViewById(R.id.fpItemList);
        FloatingActionButton btnAddInv = view.findViewById(R.id.btnAddInv);
        TextInputEditText etPalletBox = view.findViewById(R.id.etPalletBoxCount);
        TextInputEditText etPalletCount = view.findViewById(R.id.etPalletCount);
        TextInputEditText etLayerBox = view.findViewById(R.id.etLayerBoxCount);
        TextInputEditText etLayerCount = view.findViewById(R.id.etLayerCount);
        TextInputEditText etBoxUnit = view.findViewById(R.id.etBoxUnitCount);
        TextInputEditText etBoxCount = view.findViewById(R.id.etBoxCount);
        TextInputEditText etUnitCount = view.findViewById(R.id.etUnitCount);
        EditText etBoxes = view.findViewById(R.id.etBoxes);
        productList = new CopyOnWriteArrayList<>();
        productList.add(prdPallet);
        productList.add(HunterMobileWMS.findProduct(UUID.fromString("5548bd5d-b089-4413-b8f4-f5f3ccf43d28")));//Eucatex
        ProductAdapter productAdapter = new ProductAdapter(Objects.requireNonNull(getActivity()), R.layout.item_product_mid, productList).init();
        BoxCalculatorWatcher bcw = new BoxCalculatorWatcher(etPalletBox, etPalletCount, etLayerBox, etBoxUnit, etLayerCount, etBoxCount, etUnitCount, etBoxes);
        RecyclerView recyclerView = (RecyclerView) itList;

        cbxProduct = view.findViewById(R.id.cbxProduct);
        etPalletBox.addTextChangedListener(bcw);
        etPalletCount.addTextChangedListener(bcw);
        etLayerCount.addTextChangedListener(bcw);
        etLayerBox.addTextChangedListener(bcw);
        etBoxCount.addTextChangedListener(bcw);
        etPalletBox.setTransitionName("TRANS NAME");
//        etPalletBox.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> Log.d("Changed", v.getTransitionName()));
        btnAddInv.setOnClickListener(v -> {
            String boxStr = etBoxes.getText().toString();

            if (cbxProduct.getSelectedItemPosition() == 0) {
                Toast.makeText(getActivity(), R.string.select_product, Toast.LENGTH_LONG).show();
                return;
            }
            if (boxStr.isEmpty()) {
                new MaterialAlertDialogBuilder(Objects.requireNonNull(getContext()))
                        .setTitle("Quantidades não preenchidas")
                        .setMessage(getString(R.string.filling_required, "CAIXAS"))
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setNeutralButton(android.R.string.ok, null).create().show();
                return;
            }
            Product selPrd = (Product) cbxProduct.getSelectedItem();
            double boxes = Double.parseDouble(boxStr.replace(",", "."));
            ViewFinishedProductStub stub = new ViewFinishedProductStub(null, selPrd, 0, boxes);
            List<ViewFinishedProductStub> stubList = prdStubs.containsKey(stub.getProduct().getId()) ? prdStubs.get(stub.getProduct().getId()) : new ArrayList<>();
            Objects.requireNonNull(stubList).add(stub);
            prdStubs.put(stub.getProduct().getId(), stubList);
            View current = getActivity().getCurrentFocus();

            if (current != null) current.clearFocus();
            fpCheckingStubs.add(stub);
            int pos = fpCheckingStubs.indexOf(stub);
            adapter.notifyItemInserted(pos);
            etBoxCount.requestFocus();
            etLayerCount.setText("");
            etPalletCount.setText("");
            etBoxCount.setText("");
            etUnitCount.setText("");
            etPalletCount.requestFocus();
            LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

            if (layoutManager != null)
                layoutManager.scrollToPositionWithOffset(pos, 0);
            if (BuildConfig.DEBUG) {
                int selPos = cbxProduct.getSelectedItemPosition();
                if (selPos < cbxProduct.getAdapter().getCount() - 1) {
                    cbxProduct.setSelection(selPos + 1);
                }
            }
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
                    etBoxCount.requestFocus();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                etBoxCount.requestFocus();
            }
        });
        // Set the adapter
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(7);
        recyclerView.setAdapter(adapter);
        DefaultItemAnimator animator = new DefaultItemAnimator() {
            @Override
            public boolean canReuseUpdatedViewHolder(@NonNull RecyclerView.ViewHolder viewHolder) {
                return true;
            }
        };
        recyclerView.setItemAnimator(animator);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle b = getArguments();
        setHasOptionsMenu(true);

        mViewModel = new ViewModelProvider(this).get(CheckingViewModel.class);
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
        menu.findItem(R.id.mnuClose).setVisible(BuildConfig.DEBUG);
        menu.findItem(R.id.mnuAddProduct).setVisible(allowOtherProducts);
        mnuDiff = menu.findItem(R.id.mnuDiff);
        mnuDiff.setVisible(BuildConfig.DEBUG);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        if (item.getItemId() == R.id.mnuResume) {
            displayResume();
        } else if (item.getItemId() == R.id.mnuAddProduct) {
            switchProductList();
        } else if (item.getItemId() == R.id.mnuDiff) {
            displayDiff();
        } else if (item.getItemId() == R.id.mnuSend) {
            Map<String, Double> caskTypeMap = new HashMap<>();
            Map<UUID, Double> typeMap = new HashMap<>();

            clearMarkedProducts();
            if (preCheck(caskTypeMap, typeMap)) {
                new MaterialAlertDialogBuilder(Objects.requireNonNull(getActivity()))
                        .setTitle(getString(R.string.task_completed))
                        .setMessage(getString(R.string.question_send_to_hunter))
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                            int bErr = mViewModel.getErrorCount();
                            boolean isValid = validate(caskTypeMap, typeMap);
                            int aErr = mViewModel.getErrorCount();
                            boolean errInc = aErr > bErr;
                            boolean tryOut = aErr > MAX_ERROR_COUNT || (errInc && aErr == MAX_ERROR_COUNT);
                            boolean buildRet = isValid || errInc;
                            String status = isValid ? "SUCESSO" : "FALHA";

                            if (buildRet)
                                buildRetOrdConf(status);
                            if (isValid || tryOut) {
                                mViewModel.getDocument().setUser(HunterMobileWMS.getUser());
                                Objects.requireNonNull(getActivity()).runOnUiThread(() -> new AsyncSendDocument<>(this).execute());
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .create()
                        .show();
            }
        } else if (item.getItemId() == R.id.mnuClose) {
            clearViewModel();
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean preCheck(Map<String, Double> caskTypeMap, Map<UUID, Double> typeMap) {
        for (ViewFinishedProductStub s : this.fpCheckingStubs) {
            Product p = s.getProduct();

            if (p.getModel().getMetaname().equals("VAS") && !p.getName().startsWith("EMB")) {
                ProductField pfSz = ProductUtil.getSize(p);
                String sz = pfSz != null ? pfSz.getValue() : p.getSku();
                Double equiv = caskTypeMap.get(sz);
                double eqQty = equiv == null ? 0 : equiv;

                caskTypeMap.put(sz, eqQty + s.getBoxes());
                if (BuildConfig.DEBUG)
                    Timber.d("Product %s - %s Size: TYPED-%s - %.4f", p.getSku(), p.getName(), sz, s.getBoxes());
            } else {
                UUID pId = p.getId();
                Double q = typeMap.get(pId);
                double qty = q == null ? 0 : q;

                typeMap.put(pId, s.getBoxes() + qty);
            }
        }

        for (UUID pId : confMap.keySet()) {
            Product p = HunterMobileWMS.findProduct(pId);

            if (p == null) {
                showError("Produto não cadastrado!", "Algum produto não foi sincronizado. Troque o dispositivo e acione o suporte.", true);
            } else if (!typeMap.containsKey(pId)) {
                showError("Produto não informado: ", p.getSku() + " - " + p.getName(), false);
                return false;
            }
        }

        for (String sz : caskConfMap.keySet()) {
            if (!caskTypeMap.containsKey(sz)) {
                List<UUID> pIdList = caskMap.get(sz);

                assert pIdList != null;
                for (UUID pId : pIdList) {
                    Product p = HunterMobileWMS.findProduct(pId);

                    if (p == null) {
                        showError("Produto não cadastrado!", "Algum produto não foi sincronizado. Troque o dispositivo e acione o suporte.", true);
                    } else if (!caskTypeMap.containsKey(sz)) {
                        showError("Produto não informado: ", p.getSku() + " - " + p.getName(), false);
                        return false;
                    }
                }
            }
        }

        return true;
    }

    @Override
    public int removeStub(ViewFinishedProductStub stub) {
        int ret = fpCheckingStubs.indexOf(stub);
        List<ViewFinishedProductStub> prdStubList = this.prdStubs.get(stub.getProduct().getId());

        fpCheckingStubs.remove(stub);
        if (prdStubList != null) {
            prdStubList.remove(stub);
            if (prdStubList.isEmpty()) this.prdStubs.remove(stub.getProduct().getId());
        }
        return ret;
    }

    @Override
    public int getItemCount() {
        return this.fpCheckingStubs.size();
    }

    @Override
    public ViewFinishedProductStub getItemAt(int position) {
        return this.fpCheckingStubs.get(position);
    }

    @Override
    public void interact(String msg) {
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            mnuDiff.setVisible(true);
            Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void transform(Document d) {
        mViewModel.setDocument(d);
        DocumentField dfType = DocumentUtil.getField(d, "CONF_TYPE");

        if (dfType != null && dfType.getValue().equals("SPA")) {//AddPallet
            assert prdPallet != null;
            ViewFinishedProductStub stub = new ViewFinishedProductStub(null, prdPallet, 1, 1);
            List<ViewFinishedProductStub> stubList = new ArrayList<>();

            stubList.add(stub);
            prdStubs.put(stub.getProduct().getId(), stubList);

            fpCheckingStubs.add(stub);
        } else if (dfType != null && dfType.getValue().equals("RPAPD"))
            allowOtherProducts = true;
        mViewModel.setErrorCount(0);
        productList.clear();
        for (DocumentItem di : d.getItems()) {
            Product p = HunterMobileWMS.findProduct(di.getProduct().getId());

            if (p != null) {
                if (p.getModel().getMetaname().equals("VAS") && !p.getName().startsWith("EMB")) {
                    ProductField pfSz = ProductUtil.getSize(p);
                    String sz = pfSz != null ? pfSz.getValue() : p.getSku();
                    Double equiv = caskConfMap.get(sz);
                    double eqQty = equiv == null ? 0 : equiv;
                    List<UUID> pIdList = caskMap.get(sz);

                    if (pIdList == null)
                        pIdList = new ArrayList<>();
                    if (pfSz == null)
                        Timber.w("Product %s - %s don't have Size. Quantity (total) %.0f (%.0f)", p.getSku(), p.getName(), eqQty, di.getQty());
                    pIdList.add(p.getId());
                    caskMap.put(sz, pIdList);
                    caskConfMap.put(sz, eqQty + di.getQty());
                } else {
                    UUID pId = p.getId();
                    Double q = confMap.get(pId);
                    double qty = q == null ? 0 : q;

                    confMap.put(pId, di.getQty() + qty);
                }
                checkingProducts.add(p);
            }
        }
        if (BuildConfig.DEBUG)
            for (Map.Entry<String, Double> en : caskConfMap.entrySet())
                Timber.d("Size: CONF-%s - %.0f", en.getKey(), en.getValue());
        Product[] ar = new Product[checkingProducts.size()];

        ar = checkingProducts.toArray(ar);
        if (BuildConfig.DEBUG)
            Arrays.sort(ar, HunterMobileWMS.compareBySku);
        else
            Arrays.sort(ar, HunterMobileWMS.compareByName);
        productList.addAll(Arrays.asList(ar));
        if (Looper.myLooper() != null)
            ((ProductAdapter) cbxProduct.getAdapter()).init().notifyDataSetChanged();
        else
            Objects.requireNonNull(getActivity()).runOnUiThread(() -> ((ProductAdapter) cbxProduct.getAdapter()).init().notifyDataSetChanged());
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
        confMap.clear();
        caskMap.clear();
        caskConfMap.clear();
        checkingProducts.clear();
        fpCheckingStubs.clear();
        ((ProductAdapter) cbxProduct.getAdapter()).clear();
        ((ProductAdapter) cbxProduct.getAdapter()).notifyDataSetChanged();
        adapter.notifyDataSetChanged();
        mnuDiff.setVisible(false);
        getActionListener().returnFromFragment();
    }

    public void displayResume() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(Objects.requireNonNull(getActivity()))
                .setIcon(android.R.drawable.ic_dialog_info)
                .setNeutralButton(android.R.string.ok, null);
        CharSequence[] itemArray = new CharSequence[this.prdStubs.size()];
        int idx = 0;

        for (Map.Entry<UUID, List<ViewFinishedProductStub>> e : this.prdStubs.entrySet()) {
            List<ViewFinishedProductStub> stList = e.getValue();
            Product p = HunterMobileWMS.findProduct(e.getKey());

            if (p != null) {
                double boxCount = 0d;

                for (ViewFinishedProductStub s : stList)
                    boxCount += s.getBoxes();
                itemArray[idx++] = getActivity().getString(R.string.dyn_string_number, p.getSku() + " - " + p.getName(), (int) boxCount);
            }
        }
        builder.setItems(itemArray, null).create().show();
    }

    public void displayDiff() {
        ArrayList<String> diffList = new ArrayList<>();
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(Objects.requireNonNull(getContext()))
                .setIcon(android.R.drawable.ic_dialog_info)
                .setNeutralButton(android.R.string.ok, null);

        for (UUID pId : this.confMap.keySet()) {
            Product p = HunterMobileWMS.findProduct(pId);

            if (p != null) {
                List<ViewFinishedProductStub> stList = this.prdStubs.get(pId);
                Double cCount = confMap.get(p.getId());
                double confCount = cCount == null ? 0d : cCount;
                double boxCount = 0d;

                if (stList == null) {
                    boxCount = 0;
                } else {
                    for (ViewFinishedProductStub s : stList)
                        boxCount += s.getBoxes();
                }

                if ((int) (boxCount - confCount) != 0) {
                    String txt = Objects.requireNonNull(getActivity()).getString(R.string.dyn_string_number, p.getSku() + " - " + p.getName(), (int) (boxCount - confCount));

                    Timber.d("Resume PRD %s", txt);
                    diffList.add(txt);
                }
            }
        }
        builder.setAdapter(new ItemDiffAdapter(getContext(), diffList), null).create().show();
    }

    public void buildRetOrdConf(String status) {
        DocumentModel dm = new DocumentModel();
        Document d = new Document();

        dm.setId(UUID.fromString("6b0982c0-5b9a-11e9-a948-0266c0e70a8c"));
        dm.setMetaname("RETORDCONF");
        d.setModel(dm);
        d.setStatus(status);
        d.setCode(mViewModel.getDocument().getCode().replace("CONF", "ROC") + (status.equals("SUCESSO") ? "" : "-" + mViewModel.getErrorCount()));
        d.setUser(HunterMobileWMS.getUser());

        for (ViewFinishedProductStub s : this.fpCheckingStubs) {
            boolean found = false;
            Product p = s.getProduct();

            for (DocumentItem di : d.getItems()) {
                if (Objects.equals(di.getProduct(), p)) {
                    di.setQty((di.getQty() + s.getBoxes()));
                    found = true;
                }
            }
            if (!found) {
                DocumentItem di = new DocumentItem();
                ProductField pf = ProductUtil.getMeasureUnit(p);

                di.setProduct(p);
                di.setQty(s.getBoxes());
                di.setMeasureUnit(pf == null ? "CX" : pf.getValue());
                d.getItems().add(di);
            }
        }
        mViewModel.getDocument().setStatus(status);
        mViewModel.getDocument().getSiblings().add(d);
    }

    private boolean validate(Map<String, Double> caskTypeMap, Map<UUID, Double> typeMap) {
        Set<Product> wrongProducts = new HashSet<>();
        boolean alerted = false;

        for (Map.Entry<UUID, Double> en : typeMap.entrySet()) {
            Product p = HunterMobileWMS.findProduct(en.getKey());

            if (p != null) {
                Double qty = confMap.get(p.getId());

                if (qty == null) {
                    if (!allowOtherProducts && !wrongProducts.contains(p)) {
                        showError("Produto informado não consta: ", p.getSku() + " - " + p.getName(), false);
                        wrongProducts.add(p);
                    } else if (BuildConfig.DEBUG)
                        Timber.d("Product %s - %s not present in conf. Its Allowed to have other products", p.getSku(), p.getName());
                } else if (qty == 0) {
                    Timber.d("OK Informado mas não confrontado");
                } else if (!en.getValue().equals(qty)) {
                    if (!alerted) {
                        String title = getString(R.string.msg_checking_failed, mViewModel.getErrorCount() + 1);
                        String msg = getString(R.string.msg_failed_count, p.getSku(), p.getName());

                        showError(title, msg, true);
                        alerted = true;
                    }
                    wrongProducts.add(p);
                }
            } else
                showError("Produto informado não cadastrado no dispositivo: ", en.getKey().toString(), false);
        }

        for (Map.Entry<String, Double> en : caskTypeMap.entrySet()) {
            String sz = en.getKey();
            Double qty = caskConfMap.get(sz);

            for (UUID pId : Objects.requireNonNull(caskMap.get(sz))) {
                Product p = HunterMobileWMS.findProduct(pId);

                assert p != null;
                if (qty == null) {
                    if (!allowOtherProducts && !wrongProducts.contains(p)) {
                        showError("Produto informado não consta: ", p.getSku() + " - " + p.getName(), false);
                        wrongProducts.add(p);
                    } else if (BuildConfig.DEBUG)
                        Timber.d("Product %s - %s not present in conf. Its Allowed to have other products", p.getSku(), p.getName());
                } else if (qty == 0) {
                    Timber.d("OK Informado mas não confrontado");
                } else if (!en.getValue().equals(qty)) {
                    if (!alerted) {
                        String title = getString(R.string.msg_checking_failed, mViewModel.getErrorCount() + 1);
                        String msg = getString(R.string.msg_failed_count, p.getSku(), p.getName());

                        showError(title, msg, true);
                        alerted = true;
                    }
                    wrongProducts.add(p);
                }
            }
        }

        if (wrongProducts.size() > 0) {
            mViewModel.incrementErrorCount();

            for (int i = 0; i < this.fpCheckingStubs.size(); i++) {
                ViewFinishedProductStub st = this.fpCheckingStubs.get(i);
                boolean wasError = st.isError();
                boolean isError = wrongProducts.contains(st.getProduct());

                st.setError(isError);
                if (isError != wasError)
                    this.adapter.notifyItemChanged(i);
            }
            return false;
        }

        return true;
    }

    private void clearMarkedProducts() {
        for (ViewFinishedProductStub st : this.fpCheckingStubs) {
            st.setError(false);
        }
        adapter.notifyDataSetChanged();
    }

    private void switchProductList() {
        //if the productlist has the same products than checking, switch to full
        boolean displayChecking = productList.size() != (checkingProducts.size() + 1);//+1 = Choose Product

        productList.clear();
        if (displayChecking) {
            Product[] ar = new Product[checkingProducts.size()];

            ar = checkingProducts.toArray(ar);
            if (BuildConfig.DEBUG)
                Arrays.sort(ar, HunterMobileWMS.compareBySku);
            else
                Arrays.sort(ar, HunterMobileWMS.compareByName);
            productList.addAll(Arrays.asList(ar));
            getActionListener().sendMessageNotification(getString(R.string.msg_showing_checking_products), 1000);
        } else {
            productList.addAll(HunterMobileWMS.getPAProductList());
            productList.addAll(HunterMobileWMS.getRSTProductList());
            getActionListener().sendMessageNotification(getString(R.string.msg_showing_all_products), 1000);
        }

        ((ProductAdapter) cbxProduct.getAdapter()).init().notifyDataSetChanged();
    }
}
