package com.gtp.hunter.wms.fragment;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.gtp.hunter.BuildConfig;
import com.gtp.hunter.HunterMobileWMS;
import com.gtp.hunter.R;
import com.gtp.hunter.structure.CheckingField;
import com.gtp.hunter.structure.CheckingTextWatcher;
import com.gtp.hunter.structure.adapter.AddressAdapter;
import com.gtp.hunter.structure.adapter.ProductAdapter;
import com.gtp.hunter.structure.spinner.SearchableSpinner;
import com.gtp.hunter.structure.viewmodel.AGLCheckingViewModel;
import com.gtp.hunter.structure.viewmodel.BaseDocumentViewModel;
import com.gtp.hunter.util.ProductUtil;
import com.gtp.hunter.wms.model.AGLDocItem;
import com.gtp.hunter.wms.model.AGLDocument;
import com.gtp.hunter.wms.model.AGLThing;
import com.gtp.hunter.wms.model.Address;
import com.gtp.hunter.wms.model.AddressField;
import com.gtp.hunter.wms.model.AddressModel;
import com.gtp.hunter.wms.model.CheckingItem;
import com.gtp.hunter.wms.model.Document;
import com.gtp.hunter.wms.model.DocumentField;
import com.gtp.hunter.wms.model.DocumentItem;
import com.gtp.hunter.wms.model.DocumentThing;
import com.gtp.hunter.wms.model.PrintPayload;
import com.gtp.hunter.wms.model.Product;
import com.gtp.hunter.wms.model.ProductField;
import com.gtp.hunter.wms.model.SugarPrintTemporary;
import com.gtp.hunter.wms.model.Thing;

import java.lang.ref.WeakReference;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CheckingFragment extends DocumentFragment {

    private static final Product PALLET_PRD = HunterMobileWMS.findProduct(UUID.fromString("95b564e9-ea5a-4caa-adbe-06fc7dd0b966"));
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    private static final SimpleDateFormat SDFHR = new SimpleDateFormat("dd/MM/yyyy", Locale.US);

    private static final String MP_PF_KIT_ID = "f69ffb77-8bd7-11e9-815b-005056a19775";
    private static final String MP_PF_KIT_QTY_ID = "bdb68aa7-8bee-11e9-815b-005056a19775";
    private static final String MP_PF_UM_ID = "39a0b90e-8bac-4b57-9bc9-c0a4e90ecbd4";

    private static final String AF_SEQ = "03eee3c1-a249-11e9-97e4-005056a19775";

    private static final int MAX_RETRY = 3;
    private static final int SHOW_RESULT_RETRY = 1;

    private boolean isPaConf;
    private boolean copyModeEnabled;

    private int selectedView = -1;
    private AGLCheckingViewModel mViewModel;

    private TextView txtVolCount;
    private EditText edtCTE;
    private MenuItem menuItemPrint;

    private GridLayout baseLayout;
    private DatePickerDialog picker;
    private ProgressBar progress;
    private boolean init;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_conference, container, false);

        baseLayout = v.findViewById(R.id.conferenceLayout);
        progress = v.findViewById(R.id.confProgress);
        txtVolCount = v.findViewById(R.id.txtVolCount);
        edtCTE = v.findViewById(R.id.edtCTE);
        edtCTE.setText("");
        edtCTE.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (mViewModel.getReturn() != null)
                    mViewModel.getReturn().getProps().put("CTE", editable.toString());
            }
        });
        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
        for (CheckingItem tmpConf : mViewModel.getConferenceItems())
            runOnThread(() -> HunterMobileWMS.getDB().checkItemDao().save(tmpConf));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle b = getArguments();

        setHasOptionsMenu(true);
        mViewModel = new ViewModelProvider(this).get(AGLCheckingViewModel.class);

        // Use the ViewModel
        if (b != null)
            mViewModel.setDocument((Document) b.getSerializable("DOCUMENT"));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.conference, menu);
        menu.findItem(R.id.confCopyProduct).setVisible(isPaConf);
        menuItemPrint = menu.findItem(R.id.confPrint);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.confAddProduct:
                View v = addConferenceItem().getView();

                v.setSelected(!isPaConf);
                selectedView = v.getId();
                break;
            case R.id.confCopyProduct:
                if (!copyModeEnabled)
                    if (selectedView > -1) {
                        Drawable drw = ContextCompat.getDrawable(Objects.requireNonNull(getActivity()), R.drawable.background_conf_copy);

                        mViewModel.getView(selectedView).setBackground(drw);
                        getActionListener().sendMessageNotification(getString(R.string.copy_mode_enabled), 600);
                    } else {
                        getActionListener().sendMessageNotification(getString(R.string.select_box), 600);
                        copyModeEnabled = true;
                    }
                else {
                    Drawable drw = ContextCompat.getDrawable(Objects.requireNonNull(getActivity()), R.drawable.background_white_border);

                    mViewModel.getView(selectedView).setBackground(drw);
                    getActionListener().sendMessageNotification(getString(R.string.copy_mode_disabled), 600);
                }
                copyModeEnabled = !copyModeEnabled;
                break;
            case R.id.confRemoveProduct:
                new AlertDialog.Builder(getActivity())
                        .setTitle(getString(android.R.string.dialog_alert_title))
                        .setMessage(CheckingFragment.this.getString(R.string.question_confirm_remove))
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                            removeProductView();
                        })
                        .setNegativeButton(android.R.string.no, null).show();
                break;
            case R.id.confApply:
                new AlertDialog.Builder(getActivity())
                        .setTitle(getString(R.string.conference_complete))
                        .setMessage(CheckingFragment.this.getString(R.string.question_send_to_hunter))
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                            copyModeEnabled = false;
                            new AsyncSendAction(this).execute();
                        })
                        .setNegativeButton(android.R.string.no, null).show();
                break;
            case R.id.confCancel:
                if (mViewModel.getSugarThings().size() > 0) {//Tem impressão, não pode cancelar
                    showError("ERRO", "EXISTEM PRODUTOS JÁ IMPRESSOS", true);
                } else {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(getString(android.R.string.dialog_alert_title))
                            .setMessage(CheckingFragment.this.getString(R.string.question_confirm_cancel))
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                                clearViewModel();
                            })
                            .setNegativeButton(android.R.string.no, null).show();
                }
                break;
            case R.id.confPrint:
                sendPrint();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void sendPrint() {
        if (selectedView > -1) {
            PrintPayload payload = new PrintPayload();
            Document doc = mViewModel.getDocument();
            AGLDocument ret = mViewModel.getReturn();
            View vw = mViewModel.getView(selectedView);
            SearchableSpinner cbxProduct = vw.findViewById(R.id.cbxConfProduct);
            SearchableSpinner cbxAddress = vw.findViewById(R.id.cbxConfAddress);
            EditText edtMfg = vw.findViewById(R.id.edtConfMfg);
            EditText edtExp = vw.findViewById(R.id.edtConfExp);
            EditText edtLot = vw.findViewById(R.id.edtConfLot);
            EditText edtQty = vw.findViewById(R.id.edtConfQuantity);
            EditText edtVol = vw.findViewById(R.id.edtConfVolume);
            CheckBox cbxIndeterminate = vw.findViewById(R.id.cbxConfIndeterminate);
            Product prd = (Product) cbxProduct.getSelectedItem();
            Address add = (Address) cbxAddress.getSelectedItem();
            String lot = edtLot.getText().toString();
            String qty = edtQty.getText().toString();
            String mfg = edtMfg.getText().toString();
            String exp = edtExp.getText().toString();
            boolean indExp = cbxIndeterminate.isChecked();
            UUID tId = UUID.randomUUID();

            edtVol.setText("1");
            if (checkProperties(prd, add, mfg, exp, indExp, lot, qty, "1")) {
                Double dblQty = Double.parseDouble(qty);
                if (dblQty > 1600) {
                    showError("Valor muito alto", "Açúcar não impresso", false);
                    return;
                } else if (dblQty < 500) {
                    showError("Valor muito baixo", "Açúcar não impresso", false);
                    return;
                }
                SugarPrintTemporary print = new SugarPrintTemporary();

                print.setProduct_id(prd.getId());
                print.setThing_id(tId);
                print.setDocument_id(doc.getId());
                print.setLot_id(lot);
                print.setQuantity(dblQty);
                print.setLot_exp(exp);
                print.setLot_mfg(mfg);
                payload.setProduct(prd.getId());
                payload.setThing(tId);
                payload.setDocument(doc.getId());
                payload.setSku(prd.getSku());
                payload.getProperties().put("loteFornecedor", lot);
                payload.getProperties().put("qtdPorVolume", qty);
                payload.getProperties().put("qtdVolume", "1");
                payload.getProperties().put("dtFabricacao", mfg);
                payload.getProperties().put("dtValidade", exp);
                payload.getProperties().put("SKU", prd.getSku());
                payload.getProperties().put("unidademedida", ret.getItems().get(0).getMeasureUnit());
                payload.getProperties().put("produto", prd.getName());
                payload.getProperties().put("qtdRecebimento", qty);
                //New Things
                payload.getProperties().put("QUANTITY", qty);
                payload.getProperties().put("ACTUAL_WEIGHT", "0");
                payload.getProperties().put("LOT_ID", lot);
                payload.getProperties().put("LOT_EXPIRE", exp);
                payload.getProperties().put("MANUFACTURING_BATCH", mfg);
                payload.getProperties().put("STARTING_WEIGHT", "0");
                payload.getProperties().put("LABEL_OBS", "");
                payload.getProperties().put("REACTIVITY", "");
                payload.getProperties().put("LIFETHREAT", "");
                payload.getProperties().put("SPECIAL_REC", "");
                payload.getProperties().put("INFLAMABILITY", "");

                //Fill LoteInterno
                // get dialog_text.xml view
                LayoutInflater li = Objects.requireNonNull(getActivity()).getLayoutInflater();
                View promptsView = li.inflate(R.layout.dialog_text, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(promptsView);

                final EditText userInput = promptsView.findViewById(R.id.editTextDialogUserInput);

                // set dialog message
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                (dialog, id) -> {
                                    String loteInterno = "";
                                    Editable e = userInput.getText();

                                    if (e != null)
                                        loteInterno = e.toString();
                                    payload.getProperties().put("loteInterno", loteInterno);
                                    payload.getProperties().put("INTERNAL_LOT", loteInterno);
                                    new AsyncPrintAction(this, vw, payload, print).execute();
                                })
                        .setNegativeButton("Cancel", (dialog, id) -> dialog.cancel());

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
            }
        } else
            showError("Item não selecionado", "Selecione item para impressão", false);
    }

    private AGLDocument createReturnDocument(List<AGLThing> containerList, String status) {
        AGLDocument retOrdConf = new AGLDocument();
        String oldCode = mViewModel.getReturn().getCode().replaceAll("CONF", "");
        String newCode = "ROC" + oldCode + (mViewModel.getErrorCount() > 0 ? "-" + mViewModel.getErrorCount() : "");

        retOrdConf.setId(UUID.randomUUID().toString());
        retOrdConf.setParent_id(mViewModel.getReturn().getId());
        retOrdConf.setCreatedAtSQL(SDF.format(Calendar.getInstance().getTime()));
        retOrdConf.setUpdatedAtSQL(SDF.format(Calendar.getInstance().getTime()));
        retOrdConf.setCode(newCode);
        retOrdConf.setName("Retorno da Conferência " + oldCode);
        retOrdConf.setMetaname("RETORDCONF");
        retOrdConf.setStatus(status);
        retOrdConf.setProps(mViewModel.getReturn().getProps());
        retOrdConf.getThings().addAll(containerList);
        return retOrdConf;
    }

    private Thing createContainer(Integer id) {
        Thing ret = mViewModel.getThing(id);

        ret.setStatus("TEMPORARIO");
        ret.setCreatedAt(Calendar.getInstance().getTime());
        ret.setUpdatedAt(Calendar.getInstance().getTime());
        ret.setName(isPaConf ? PALLET_PRD.getName() : "CONTAINER");
        ret.setMetaname("THINGS");
        ret.setStatus("CONFERIDO");
        ret.setProduct(PALLET_PRD);
        return ret;
    }

    private AGLDocument checkCount() {
        List<AGLThing> containerList = new ArrayList<>();
        Thing t = createContainer(null);
        AGLThing itemContainer = t.getAGLThing();
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        DecimalFormat DF = new DecimalFormat("0.0000", symbols);
        DF.setRoundingMode(RoundingMode.FLOOR);
        Map<String, Double> countMap = new HashMap<>();
        boolean success = true;
        boolean isSugar = false;

        mViewModel.clearWrongViews();
        for (CheckingItem tmp : mViewModel.getConferenceItems()) {
            View v = tmp.getView();

            if (isPaConf) {
                t = createContainer(v.getId());
                itemContainer = t.getAGLThing();
            }
            tmp.setThing(t);
            SearchableSpinner cbxProduct = v.findViewById(R.id.cbxConfProduct);
            SearchableSpinner cbxAddress = v.findViewById(R.id.cbxConfAddress);
            TextView txtPrdId = v.findViewById(R.id.txtProductId);
            EditText txtMfg = v.findViewById(R.id.edtConfMfg);
            EditText txtExp = v.findViewById(R.id.edtConfExp);
            CheckBox cbxIndeterminate = v.findViewById(R.id.cbxConfIndeterminate);
            EditText txtLot = v.findViewById(R.id.edtConfLot);
            EditText txtQty = v.findViewById(R.id.edtConfQuantity);
            EditText txtVol = v.findViewById(R.id.edtConfVolume);
            Product prd = (Product) cbxProduct.getSelectedItem();
            Address add = (Address) cbxAddress.getSelectedItem();
            String mfg = txtMfg.getText().toString();
            String exp = txtExp.getText().toString();
            String lot = txtLot.getText().toString();
            String qty = txtQty.getText().toString().replace(",", ".");
            String vol = txtVol.getText().toString();
            String key = prd.getId().toString();
            boolean indExp = cbxIndeterminate.isChecked();
            double lotQuantity = 0d;
            if (txtPrdId.getText().equals("67831e8a-bbc5-4bba-be49-06233db6d416")) {//ACUCAR CRISTAL
                Objects.requireNonNull(getActivity()).runOnUiThread(() -> txtVol.setText("1"));
                isSugar = true;
            }
            if (!checkProperties(prd, add, mfg, exp, indExp, lot, qty, vol)) {
                Drawable confNok = ContextCompat.getDrawable(Objects.requireNonNull(getActivity()), R.drawable.background_conf_nok);
                AGLDocument doc = new AGLDocument();

                doc.setStatus("ERROR");
                Objects.requireNonNull(getActivity()).runOnUiThread(() -> v.setBackground(confNok));
                return doc;
            }
            if (countMap.containsKey(key)) lotQuantity = countMap.get(key);
            lotQuantity += Double.parseDouble(qty) * Integer.parseInt(vol);
            countMap.put(key, lotQuantity);

            for (int i = 0; i < Integer.parseInt(vol); i++) {
                AGLThing thing = new AGLThing();

                if (isSugar) {
                    if (mViewModel.isSugarPrinted(v.getId())) {
                        thing.setId(mViewModel.getSugarThingId(v.getId()).toString());
                    } else {
                        showError("Impressão Obrigatória", "Produto não Impresso", false);
                        Drawable confNok = ContextCompat.getDrawable(Objects.requireNonNull(getActivity()), R.drawable.background_conf_nok);
                        Objects.requireNonNull(getActivity()).runOnUiThread(() -> v.setBackground(confNok));
                        AGLDocument doc = new AGLDocument();

                        doc.setStatus("ERROR");
                        return doc;
                    }
                }
                thing.setCreated_at(SDF.format(Calendar.getInstance().getTime()));
                thing.setUpdated_at(SDF.format(Calendar.getInstance().getTime()));
                thing.setStatus("NOVO");
                thing.setName(prd.getName());
                thing.setMetaname("THINGS");
                thing.setParent_id(itemContainer.getId());
                thing.setProduct_id(prd.getId().toString());
                thing.setAddress_id(add.getId().toString());
                itemContainer.setAddress_id(add.getId().toString());
                thing.setUser_id(HunterMobileWMS.getUser().getId().toString());
                thing.getProps().put("actual_weight", DF.format(0d));
                thing.getProps().put("lot_expire", indExp ? "Indeterminado" : exp);
                thing.getProps().put("lot_id", lot);
                thing.getProps().put("manufacturing_batch", mfg);
                thing.getProps().put("quantity", DF.format(Double.parseDouble(qty)));
                thing.getProps().put("starting_weight", DF.format(0d));
                if (isPaConf) {
                    itemContainer.getProps().put("actual_weight", DF.format(0d));
                    itemContainer.getProps().put("lot_expire", indExp ? "Indeterminado" : exp);
                    itemContainer.getProps().put("lot_id", lot);
                    itemContainer.getProps().put("manufacturing_batch", mfg);
                    itemContainer.getProps().put("quantity", "1.0000");
                    itemContainer.getProps().put("starting_weight", DF.format(0d));
                }
                itemContainer.getSiblings().add(thing);

                boolean addContainer = true;
                for (AGLThing container : containerList) {
                    if (container.getId().equals(itemContainer.getId())) {
                        addContainer = false;
                        break;
                    }
                }

                if (addContainer)
                    containerList.add(itemContainer);
            }
        }

        if (!isSugar) {
            for (AGLDocItem di : mViewModel.getReturn().getItems()) {
                if (countMap.containsKey(di.getProduct_id())) {
                    DecimalFormat compareDF = new DecimalFormat("0.000");//Compara com 3 grava com 4
                    double docCount = Double.parseDouble(compareDF.format(di.getQty()).replace(",", "."));
                    double blindCount = Double.parseDouble(compareDF.format(countMap.remove(di.getProduct_id())).replace(",", "."));

                    if (docCount != blindCount) {
                        success = false;
                        if (BuildConfig.DEBUG)
                            getActionListener().sendMessageNotification("NF: " + docCount + " <> " + blindCount + " :CNT", 6000);
                        if (mViewModel.getErrorCount() < (MAX_RETRY - 1))
                            showError("Quantidade Divergente", "Contagem diferente da Nota Fiscal", false);
                        mViewModel.setWrong(UUID.fromString(di.getProduct_id()));
                    }
                } else {
                    success = false;
                    if (mViewModel.getErrorCount() < (MAX_RETRY - 1)) {
                        Product prd = HunterMobileWMS.findProduct(UUID.fromString(di.getProduct_id()));

                        assert prd != null;
                        showError("Produto não informado: ", prd.getSku() + " - " + prd.getName(), false);
                    }
                    mViewModel.setWrong(UUID.fromString(di.getProduct_id()));
                }
            }
            if (!countMap.keySet().isEmpty()) {
                String[] productIds = new String[countMap.keySet().size()];
                productIds = countMap.keySet().toArray(productIds);
                final String prdId = productIds[0];
                if (BuildConfig.DEBUG)
                    getActionListener().sendMessageNotification("Produto informado não consta: " + prdId, 8000);
                if (mViewModel.getErrorCount() < MAX_RETRY) {
                    Product prd = HunterMobileWMS.findProduct(UUID.fromString(prdId));

                    assert prd != null;
                    showError("Produto informado não consta: ", prdId, false);
                }
                success = false;
                mViewModel.setWrong(UUID.fromString(productIds[0]));
            }
        }
        if (!success) {
            mViewModel.increaseErrorCount();
            if (mViewModel.getErrorCount() >= SHOW_RESULT_RETRY)
                Objects.requireNonNull(getActivity()).runOnUiThread(this::highlightWrongViews);
        }
        mViewModel.getReturn().getSiblings().add(createReturnDocument(containerList, success ? "SUCESSO" : "FALHA"));
        mViewModel.getReturn().setStatus(success ? "SUCESSO" : "FALHA");
        return mViewModel.getReturn();
    }

    private boolean checkProperties(Product prd, Address add, String mfg, String exp, boolean indExp, String lot, String qty, String vol) {
        if (prd.getName().equals(getString(R.string.select_product))) {
            showError(getString(R.string.filling_required, getString(R.string.label_product)), getString(R.string.select_product), false);
            return false;
        }
        if (add.getName().equals(getString(R.string.select_address))) {
            showError(getString(R.string.filling_required, getString(R.string.label_address)), getString(R.string.select_address), false);
            return false;
        }
        if (mfg.isEmpty()) {
            showError(getString(R.string.filling_required, getString(R.string.label_manufacture)), "Data de Fabricação Inválida", false);
            return false;
        }
        if (!indExp && exp.isEmpty()) {
            showError(getString(R.string.filling_required, getString(R.string.label_expiry)), "Data de Validade Inválida", false);
            return false;
        }
        if (lot.isEmpty()) {
            showError(getString(R.string.filling_required, getString(R.string.label_lot)), "Lote Inválido", false);
            return false;
        }
        if (qty.isEmpty()) {
            showError(getString(R.string.filling_required, getString(R.string.label_quantity)), "Quantidade Inválida", false);
            return false;
        }
        if (vol.isEmpty()) {
            showError(getString(R.string.filling_required, getString(R.string.label_volume)), "Volumes Inválido", false);
            return false;
        }
        return true;
    }

    private void highlightWrongViews() {
        for (CheckingItem tmp : mViewModel.getConferenceItems()) {
            if (tmp.isWrong()) {
                Drawable confNok = ContextCompat.getDrawable(Objects.requireNonNull(getActivity()), R.drawable.background_conf_nok);

                tmp.getView().setBackground(confNok);
            } else {
                Drawable confOk = ContextCompat.getDrawable(Objects.requireNonNull(getActivity()), R.drawable.background_conf_ok);

                tmp.getView().setBackground(confOk);
            }
        }
    }

    @Override
    public void clearViewModel() {
        init = false;
        mViewModel.clearTemp();
        mViewModel.setReturn(null);
        mViewModel.setDocument(null);
        selectedView = -1;
        baseLayout.removeAllViewsInLayout();
        progress.setVisibility(View.GONE);
        getActionListener().returnFromFragment();
    }

    @Override
    public BaseDocumentViewModel getViewModel() {
        return mViewModel;
    }

    private CheckingItem addConferenceItem() {
        final CheckingItem tmpConf = new CheckingItem();
        final View v = getLayoutInflater().inflate(R.layout.item_conference, baseLayout, false);
        Drawable deselected = ContextCompat.getDrawable(Objects.requireNonNull(getActivity()), R.drawable.background_white_border);
        Drawable selected = ContextCompat.getDrawable(getActivity(), R.drawable.background_primary_border);
        SearchableSpinner cbxProduct = v.findViewById(R.id.cbxConfProduct);
        SearchableSpinner cbxAddress = v.findViewById(R.id.cbxConfAddress);
        EditText edtMfg = v.findViewById(R.id.edtConfMfg);
        EditText edtExp = v.findViewById(R.id.edtConfExp);
        TextView txtLot = v.findViewById(R.id.txtConfLot);
        EditText edtLot = v.findViewById(R.id.edtConfLot);
        EditText edtQty = v.findViewById(R.id.edtConfQuantity);
        TextView txtVol = v.findViewById(R.id.txtConfVolume);
        EditText edtVol = v.findViewById(R.id.edtConfVolume);
        TextView txtQty = v.findViewById(R.id.txtConfQuantity);
        CheckBox chbIndExp = v.findViewById(R.id.cbxConfIndeterminate);
        Product empty = new Product();
        Address emptyAddress = new Address();
        TextView txtVwId = v.findViewById(R.id.txtVwId);
        int newId = mViewModel.newViewId();

        tmpConf.setDocument_id(mViewModel.getDocument().getId());
        empty.setId(UUID.randomUUID());
        empty.setName(getString(R.string.select_product));
        empty.setSku("");

        emptyAddress.setId(UUID.randomUUID());
        emptyAddress.setName(getString(R.string.select_address));

        cbxAddress.setTitle(Objects.requireNonNull(getActivity()).getString(R.string.select_address));
        if (!isPaConf) {
            Address a = new Address();
            AddressModel am = new AddressModel();

            am.setId(UUID.fromString("10f3ca19-9c45-11e9-94d8-005056a19775"));
            am.setMetaname("DOCK");
            am.setName("Docas");
            a.setId(UUID.fromString("27998254-563a-11e9-b375-005056a19775"));
            a.setMetaname("ALMOXARIFADO");
            a.setName("ALMOXARIFADO");
            a.setModel(am);
            cbxAddress.setAdapter(new AddressAdapter(Objects.requireNonNull(getContext()), R.layout.item_address, a, ""));
            cbxProduct.setAdapter(new ProductAdapter(Objects.requireNonNull(getContext()), R.layout.item_product, empty));
            cbxAddress.setVisibility(View.GONE);

            txtVol.setVisibility(View.VISIBLE);
            edtVol.setVisibility(View.VISIBLE);
            txtLot.setVisibility(View.VISIBLE);
            edtLot.setVisibility(View.VISIBLE);
        } else {
            List<Address> addList = HunterMobileWMS.getAddressList("ADDRESS");
            CopyOnWriteArrayList<Address> addressList = new CopyOnWriteArrayList<>();
            CopyOnWriteArrayList<Product> productList = new CopyOnWriteArrayList<>();

            addressList.add(0, emptyAddress);
            for (Address a : addList) {
                if (a != null && a.getMetaname() != null && a.getMetaname().startsWith("S") && a.getMetaname().length() == 7)
                    addressList.add(a);
            }

            productList.add(0, empty);
            for (DocumentItem di : mViewModel.getDocument().getItems()) {
                productList.add(HunterMobileWMS.findProduct(di.getProduct().getId()));
            }
            txtVol.setVisibility(View.GONE);
            edtVol.setText("1");
            txtLot.setVisibility(View.GONE);
            edtLot.setText(Objects.requireNonNull(getActivity()).getString(R.string.status_temporary));
            edtLot.setVisibility(View.GONE);
            edtVol.setVisibility(View.GONE);
            chbIndExp.setVisibility(View.GONE);
            cbxProduct.setAdapter(new ProductAdapter(Objects.requireNonNull(getContext()), R.layout.item_product, productList));
            cbxAddress.setAdapter(new AddressAdapter(Objects.requireNonNull(getContext()), R.layout.item_address, addressList));
            cbxAddress.setVisibility(View.VISIBLE);
            if (productList.size() == 2) {
                cbxProduct.setSelection(1);
            }
        }
        cbxProduct.setTitle(Objects.requireNonNull(getActivity()).getString(R.string.select_product));
        cbxProduct.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                prdSelected(v);
                doTheSugarTrick(view);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        cbxAddress.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (cbxAddress.getSelectedItemPosition() > 0) {
                    final CheckingItem tmpConf = mViewModel.getCheckingByViewId(v.getId());
                    Address addr = (Address) cbxAddress.getSelectedItem();

                    if (addr != null)
                        tmpConf.setAddress_id(addr.getId());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        edtLot.addTextChangedListener(new CheckingTextWatcher(mViewModel, CheckingField.LOT_ID, v.getId()));
        edtMfg.addTextChangedListener(new CheckingTextWatcher(mViewModel, CheckingField.LOT_MANUFACTURE, v.getId()));
        edtExp.addTextChangedListener(new CheckingTextWatcher(mViewModel, CheckingField.LOT_EXPIRE, v.getId()));
        edtQty.addTextChangedListener(new CheckingTextWatcher(mViewModel, CheckingField.QUANTITY, v.getId()));
        chbIndExp.setOnClickListener((View chb) -> edtExp.setEnabled(!((CheckBox) chb).isChecked()));
        if (selectedView > -1) {
            View selVw = mViewModel.getView(selectedView);
            SearchableSpinner selCbxProduct = selVw.findViewById(R.id.cbxConfProduct);
            EditText selEdtMfg = selVw.findViewById(R.id.edtConfMfg);
            EditText selEdtExp = selVw.findViewById(R.id.edtConfExp);
            EditText selEdtLot = selVw.findViewById(R.id.edtConfLot);
            EditText selEdtQty = selVw.findViewById(R.id.edtConfQuantity);
            TextView selTxtQty = selVw.findViewById(R.id.txtConfQuantity);

            cbxProduct.setSelection(selCbxProduct.getSelectedItemPosition());
            edtMfg.setText(selEdtMfg.getText());
            edtExp.setText(selEdtExp.getText());
            edtLot.setText(selEdtLot.getText());
            txtQty.setText(selTxtQty.getText());
            if (isPaConf)
                edtQty.setText(selEdtQty.getText());
            if (mViewModel.isSugarPrinted(selVw.getId())) {
                Drawable printed = ContextCompat.getDrawable(getActivity(), R.drawable.background_conf_print);

                selVw.setBackground(printed);
            } else
                selVw.setBackground(deselected);
            selVw.setSelected(false);
            selectedView = -1;
            tmpConf.setLot_mfg(edtMfg.getText().toString());
            tmpConf.setLot_exp(edtExp.getText().toString());
            tmpConf.setLot_id(edtLot.getText().toString());
            tmpConf.setQuantity(edtQty.getText().length() > 0 ? Double.parseDouble(edtQty.getText().toString()) : 0d);
            tmpConf.setProduct_id(((Product) selCbxProduct.getSelectedItem()).getId());
        } else {
            txtQty.setText(txtQty.getText().toString().replace("(%s)", ""));
            edtQty.setHint(edtQty.getHint().toString().replace("(%s)", ""));
        }

        edtMfg.setOnClickListener(v1 -> {
            final Calendar cal = Calendar.getInstance();
            EditText inst = (EditText) v1;
            String currVal = inst.getText().toString();

            if (!currVal.isEmpty()) {
                Date curr = null;
                try {
                    curr = SDFHR.parse(currVal);
                    cal.setTime(Objects.requireNonNull(curr));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            int day = cal.get(Calendar.DAY_OF_MONTH);
            int month = cal.get(Calendar.MONTH);
            int year = cal.get(Calendar.YEAR);
            // date picker dialog
            picker = new DatePickerDialog(getContext(),
                    (view, year1, monthOfYear, dayOfMonth) -> {
                        edtMfg.setText(getActivity().getString(R.string.normal_date, dayOfMonth, (monthOfYear + 1), year1));
                        tmpConf.setLot_mfg(edtMfg.getText().toString());
                    }, year, month, day);
            picker.getDatePicker().setMaxDate(Calendar.getInstance().getTimeInMillis());
            picker.show();
        });

        edtExp.setOnClickListener(v1 -> {
            final Calendar cal = Calendar.getInstance();
            EditText inst = (EditText) v1;
            String currVal = inst.getText().toString();

            if (!currVal.isEmpty()) {
                Date curr;

                try {
                    curr = SDFHR.parse(currVal);
                    cal.setTime(Objects.requireNonNull(curr));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            int day = cal.get(Calendar.DAY_OF_MONTH);
            int month = cal.get(Calendar.MONTH);
            int year = cal.get(Calendar.YEAR);
            // date picker dialog
            picker = new DatePickerDialog(getContext(),
                    (view, year1, monthOfYear, dayOfMonth) -> {
                        edtExp.setText(getActivity().getString(R.string.normal_date, dayOfMonth, (monthOfYear + 1), year1));
                        prdSelected(v);
                    },
                    year, month, day);
            picker.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis());
            picker.show();
        });

        edtVol.addTextChangedListener(new TextWatcher() {
            private int valueBefore;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    valueBefore = Integer.parseInt(charSequence.toString());
                } catch (Exception e) {
                    valueBefore = 1;
                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                int valueAfter;

                try {
                    valueAfter = Integer.parseInt(editable.toString());
                } catch (Exception e) {
                    valueAfter = 1;
                }
                txtVolCount.setText(getString(R.string.label_label_count, mViewModel.updateVolumeCount(valueAfter - valueBefore)));
            }
        });

        v.setId(newId);
        txtVwId.setText(getString(R.string.number_mask, newId + 1));
        v.setOnTouchListener(new View.OnTouchListener() {
            private final static int CLICK_ACTION_THRESHOLD = 50;
            private float startX;
            private float startY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        startY = event.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        float endX = event.getX();
                        float endY = event.getY();
                        if (isAClick(startX, endX, startY, endY) && !copyModeEnabled) {
                            v.performClick();
                        }
                        break;
                }
                return false;
            }

            private boolean isAClick(float startX, float endX, float startY, float endY) {
                float differenceX = Math.abs(startX - endX);
                float differenceY = Math.abs(startY - endY);
                return !(differenceX > CLICK_ACTION_THRESHOLD/* =5 */ || differenceY > CLICK_ACTION_THRESHOLD);
            }
        });
        v.setOnClickListener((clicked) -> {
            if (copyModeEnabled && selectedView > -1) {
                final View sel = mViewModel.getView(selectedView);

                if (sel.getId() == clicked.getId()) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Copiar Todos?")
                            .setMessage("Todas as caixas vazias serão substituídas")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                                for (CheckingItem tmp : mViewModel.getConferenceItems()) {
                                    View dst = tmp.getView();

                                    if (dst.getId() != clicked.getId()) {
                                        EditText dstEdtMfg = dst.findViewById(R.id.edtConfMfg);
                                        EditText dstEdtExp = dst.findViewById(R.id.edtConfExp);
                                        EditText dstEdtQty = dst.findViewById(R.id.edtConfQuantity);

                                        if (dstEdtMfg.getText().length() == 0 && dstEdtExp.getText().length() == 0 && dstEdtQty.getText().length() == 0) {
                                            copyProps(sel, dst);
                                        }
                                    }
                                }
                            })
                            .setNegativeButton(android.R.string.no, null).show();
                } else {
                    copyProps(sel, clicked);
                }
            } else {
                Drawable printSel = ContextCompat.getDrawable(getActivity(), R.drawable.background_conf_print_sel);
                if (selectedView > -1) {
                    View prev = mViewModel.getView(selectedView);

                    if (mViewModel.isSugarPrinted(prev.getId()))
                        prev.setBackground(printSel);
                    else
                        prev.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getActivity()), R.drawable.background_white_border));
                }
                doTheSugarTrick(clicked);
                selectedView = mViewModel.getViewId(clicked);
                if (mViewModel.isSugarPrinted(clicked.getId()))
                    clicked.setBackground(printSel);
                else
                    clicked.setBackground(selected);
            }
        });
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            baseLayout.addView(v);
            if (menuItemPrint != null) {
                menuItemPrint.setVisible(false);
            }
            txtVolCount.setText(getString(R.string.label_label_count, mViewModel.updateVolumeCount(1)));
            v.setSelected(true);
        });
        tmpConf.setView(v);
        mViewModel.getConferenceItems().add(tmpConf);
        return tmpConf;
    }

    private void copyProps(View source, View target) {
        init = false;
        CheckingItem tmpConf = mViewModel.getCheckingByViewId(target.getId());
        SearchableSpinner srcCbxProduct = source.findViewById(R.id.cbxConfProduct);
        EditText srcEdtMfg = source.findViewById(R.id.edtConfMfg);
        EditText srcEdtExp = source.findViewById(R.id.edtConfExp);
        MaterialTextView srcTxtMfg = source.findViewById(R.id.txtConfMfg);
        MaterialTextView srcTxtExp = source.findViewById(R.id.txtConfExp);
        EditText srcEdtQty = source.findViewById(R.id.edtConfQuantity);
        EditText srcEdtLot = source.findViewById(R.id.edtConfLot);
        CheckBox srcChkInd = source.findViewById(R.id.cbxConfIndeterminate);
        SearchableSpinner tgtCbxProduct = target.findViewById(R.id.cbxConfProduct);
        SearchableSpinner tgtCbxAddress = target.findViewById(R.id.cbxConfAddress);
        EditText tgtEdtMfg = target.findViewById(R.id.edtConfMfg);
        EditText tgtEdtExp = target.findViewById(R.id.edtConfExp);
        EditText tgtEdtQty = target.findViewById(R.id.edtConfQuantity);
        MaterialTextView tgtTxtMfg = target.findViewById(R.id.txtConfMfg);
        MaterialTextView tgtTxtExp = target.findViewById(R.id.txtConfExp);
        CheckBox tgtChkInd = target.findViewById(R.id.cbxConfIndeterminate);

        tgtCbxProduct.setSelection(srcCbxProduct.getSelectedItemPosition());
        tgtEdtExp.setText(srcEdtExp.getText());
        tgtEdtExp.setVisibility(srcEdtExp.getVisibility());
        tgtEdtMfg.setText(srcEdtMfg.getText());
        tgtEdtMfg.setVisibility(srcEdtMfg.getVisibility());
        tgtEdtQty.setText(srcEdtQty.getText());
        tgtTxtMfg.setVisibility(srcTxtMfg.getVisibility());
        tgtTxtExp.setVisibility(srcTxtExp.getVisibility());
        tgtChkInd.setVisibility(srcChkInd.getVisibility());
        tmpConf.setLot_mfg(srcEdtMfg.getText().toString());
        tmpConf.setLot_exp(srcEdtExp.getText().toString());
        tmpConf.setLot_id(srcEdtLot.getText().toString());
        tmpConf.setQuantity(srcEdtQty.getText().length() == 0 ? 0d : Double.parseDouble(srcEdtQty.getText().toString()));
        tmpConf.setProduct_id(((Product) srcCbxProduct.getSelectedItem()).getId());
        tmpConf.setAddress_id(((Address) tgtCbxAddress.getSelectedItem()).getId());

        Executors.newSingleThreadScheduledExecutor().schedule(() -> {
            HunterMobileWMS.getDB().checkItemDao().save(tmpConf);
            init = true;
        }, 400, TimeUnit.MILLISECONDS);
    }

    private void fillSaved(CheckingItem srcConf, CheckingItem targetConf) {
        View target = targetConf.getView();
        SearchableSpinner tgtCbxProduct = target.findViewById(R.id.cbxConfProduct);
        SearchableSpinner tgtCbxAddress = target.findViewById(R.id.cbxConfAddress);
        EditText tgtEdtMfg = target.findViewById(R.id.edtConfMfg);
        EditText tgtEdtExp = target.findViewById(R.id.edtConfExp);
        EditText tgtEdtQty = target.findViewById(R.id.edtConfQuantity);
        EditText tgtEdtLot = target.findViewById(R.id.edtConfLot);

        targetConf.setId(srcConf.getId());//set same id for listeners

        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            for (int i = 0; i < tgtCbxProduct.getAdapter().getCount(); i++) {
                Product prd = (Product) tgtCbxProduct.getItemAtPosition(i);
                if (prd.getId().equals(srcConf.getProduct_id())) {
                    tgtCbxProduct.setSelection(i);
                    break;
                }
            }
            for (int i = 0; i < tgtCbxAddress.getAdapter().getCount(); i++) {
                Address prd = (Address) tgtCbxAddress.getItemAtPosition(i);
                if (prd.getId().equals(srcConf.getAddress_id())) {
                    tgtCbxAddress.setSelection(i);
                    break;
                }
            }
            tgtEdtMfg.setText(srcConf.getLot_mfg() == null ? "" : srcConf.getLot_mfg());
            tgtEdtExp.setText(srcConf.getLot_exp() == null ? "" : srcConf.getLot_exp());
            tgtEdtQty.setText(srcConf.getQuantity() == null ? "" : String.valueOf(srcConf.getQuantity().intValue()));
            tgtEdtLot.setText(srcConf.getLot_id() == null ? "TEMPORARIO" : srcConf.getLot_id());
        });
        targetConf.setLot_mfg(srcConf.getLot_mfg());
        targetConf.setLot_exp(srcConf.getLot_exp());
        targetConf.setLot_id(srcConf.getLot_id());
        targetConf.setQuantity(srcConf.getQuantity());
        targetConf.setProduct_id(srcConf.getProduct_id());
        targetConf.setAddress_id(srcConf.getAddress_id());
        targetConf.setDocument_id(srcConf.getDocument_id());
        if (srcConf.getThing() != null)
            targetConf.setThing(srcConf.getThing());
    }

    private void prdSelected(View v) {
        final CheckingItem tmpConf = mViewModel.getCheckingByViewId(v.getId());

        if (init) {
            SearchableSpinner cbxProduct = v.findViewById(R.id.cbxConfProduct);

            if (cbxProduct.getSelectedItemPosition() > 0) {
                TextView lblMfg = v.findViewById(R.id.txtConfMfg);
                TextView lblExp = v.findViewById(R.id.txtConfExp);
                CheckBox cbxInd = v.findViewById(R.id.cbxConfIndeterminate);
                EditText edtMfg = v.findViewById(R.id.edtConfMfg);
                EditText edtExp = v.findViewById(R.id.edtConfExp);
                EditText edtQty = v.findViewById(R.id.edtConfQuantity);
                Product p = (Product) cbxProduct.getSelectedItem();
                boolean updateQuantity = edtMfg.getText().length() == 0 && edtExp.getText().length() == 0;

                try {
                    if (p.getFields().size() == 0)
                        Executors.newSingleThreadExecutor().submit(() -> p.getFields().addAll(HunterMobileWMS.getDB().pfDao().listByProductId(p.getId()))).get();
                } catch (Exception ignored) {
                }
                ProductField palletBox = ProductUtil.getPalletBox(p);
                ProductField shelf = ProductUtil.getShelfLife(p);

                tmpConf.setProduct_id(p.getId());
                if (updateQuantity && palletBox != null) {
                    edtQty.setText(palletBox.getValue());
                    tmpConf.setQuantity(Double.parseDouble(palletBox.getValue()));
                }
                if (p.getModel().getMetaname().equals("VAS") || p.getModel().getMetaname().equals("OUT")) {
                    Calendar cal = Calendar.getInstance(TimeZone.getDefault(), Locale.US);
                    String dt = Objects.requireNonNull(getActivity()).getString(R.string.normal_date, cal.get(Calendar.DAY_OF_MONTH), (cal.get(Calendar.MONTH) + 1), cal.get(Calendar.YEAR));

                    edtMfg.setVisibility(View.GONE);
                    edtExp.setVisibility(View.GONE);
                    lblMfg.setVisibility(View.GONE);
                    lblExp.setVisibility(View.GONE);
                    edtMfg.setText(dt);
                    edtExp.setText(dt);
                } else if (shelf != null && edtExp.getText().length() > 0) {
                    try {
                        Calendar man = Calendar.getInstance();
                        Date dtExp = SDFHR.parse(edtExp.getText().toString());
                        int sl = shelf.getValue().isEmpty() ? 0 : Integer.parseInt(shelf.getValue());

                        assert dtExp != null;
                        man.setTime(dtExp);
                        man.add(Calendar.DAY_OF_YEAR, sl * -1);
                        edtMfg.setText(Objects.requireNonNull(getActivity()).getString(R.string.normal_date, man.get(Calendar.DAY_OF_MONTH), (man.get(Calendar.MONTH) + 1), man.get(Calendar.YEAR)));
                        edtMfg.setVisibility(View.VISIBLE);
                        edtExp.setVisibility(View.VISIBLE);
                        lblMfg.setVisibility(View.VISIBLE);
                        lblExp.setVisibility(View.VISIBLE);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                cbxInd.setVisibility(p.getModel().getMetaname().equals("MP") ? View.VISIBLE : View.GONE);
                tmpConf.setLot_mfg(edtMfg.getText().toString());
                tmpConf.setLot_exp(edtExp.getText().toString());
            }
            runOnThread(() -> HunterMobileWMS.getDB().checkItemDao().save(tmpConf));
        }
    }

    private void removeProductView() {
        if (selectedView > -1) {
            CheckingItem tmpConf = mViewModel.getCheckingByViewId(selectedView);
            View toRemove = tmpConf.getView();

            if (!mViewModel.isSugarPrinted(toRemove.getId())) {
                EditText edtVol = toRemove.findViewById(R.id.edtConfVolume);
                int remCount = 1;

                if (edtVol.getText() != null && edtVol.getText().length() > 0) {
                    remCount = Integer.parseInt(edtVol.getText().toString());
                }
                Objects.requireNonNull(getActivity()).runOnUiThread(() -> baseLayout.removeView(toRemove));
                toRemove.setSelected(false);
                mViewModel.removeConf(tmpConf);
                txtVolCount.setText(getString(R.string.label_label_count, mViewModel.updateVolumeCount(-remCount)));
            } else {
                getActionListener().sendMessageNotification("Etiqueta já Impressa. Não é possível excluir", 2000);
            }
        }
        selectedView = -1;
    }

    private void doTheSugarTrick(View view) {
        TextView txtPrdId = view.findViewById(R.id.txtProductId);

        if (menuItemPrint != null)
            if (txtPrdId.getText().equals("67831e8a-bbc5-4bba-be49-06233db6d416")) {//ACUCAR CRISTAL
                menuItemPrint.setVisible(!mViewModel.isSugarPrinted(view.getId()));
            } else
                menuItemPrint.setVisible(false);
    }

    @Override
    public void interact(String msg) {
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> Toast.makeText(getContext(), msg, Toast.LENGTH_LONG));
    }

    @Override
    public void transform(Document d) {
        mViewModel.updateVolumeCount(-mViewModel.updateVolumeCount(0));
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> new AsyncCreateViews(this, d).execute());
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
//        baseLayout.removeAllViewsInLayout();
//        baseLayout.setColumnCount(getResources().getInteger(R.integer.confColumnCount));
//        for (View v : mViewModel.getProductViews()) {
//            baseLayout.addView(v);
//        }
    }

    public final void runOnThread(Runnable action) {
        new Thread(action).start();
    }


    private static class AsyncCreateViews extends AsyncTask<Integer, Integer, String> {
        private final Document d;
        private final WeakReference<CheckingFragment> fragRef;

        public AsyncCreateViews(CheckingFragment f, Document d) {
            this.d = d;
            fragRef = new WeakReference<>(f);
        }

        @Override
        protected void onPreExecute() {
            CheckingFragment frag = fragRef.get();

            frag.txtVolCount.setVisibility(View.GONE);
            frag.progress.setVisibility(View.VISIBLE);
            frag.progress.setMax(Math.max(d.getItems().size(), d.getThings().size()));
            frag.progress.setProgress(0);
        }

        @Override
        protected String doInBackground(Integer... integers) {
            CheckingFragment frag = fragRef.get();

            for (DocumentField bf : d.getFields()) {
                if (bf == null || bf.getValue() == null)
                    continue;
                if (bf.getValue().equals("EPAPD")) {
                    frag.isPaConf = true;
                    break;
                }
            }
            final List<CheckingItem> confList = HunterMobileWMS.getDB().checkItemDao().list(d.getId());
            final List<SugarPrintTemporary> tmpSugar = HunterMobileWMS.getDB().sugarDao().listAll();

            if (!tmpSugar.isEmpty()) {
                DecimalFormat DF = new DecimalFormat("0.0000");
                DF.setRoundingMode(RoundingMode.FLOOR);

                if (!tmpSugar.get(0).getDocument_id().equals(d.getId()))
                    return "Existe uma Conferência de Açúcar em andamento nesse dispositivo";
                frag.mViewModel.setDocument(d);
                frag.mViewModel.createReturn();

                Objects.requireNonNull(frag.getActivity()).runOnUiThread(() -> {
                    int vId = 0;

                    for (SugarPrintTemporary sugar : tmpSugar) {
                        Drawable printed = ContextCompat.getDrawable(Objects.requireNonNull(frag.getActivity()), R.drawable.background_conf_print);
                        Product p = HunterMobileWMS.findProduct(sugar.getProduct_id());
                        View v = frag.addConferenceItem().getView();
                        SearchableSpinner cbxProduct = v.findViewById(R.id.cbxConfProduct);
                        TextView txtQty = v.findViewById(R.id.txtConfQuantity);
                        EditText edtQty = v.findViewById(R.id.edtConfQuantity);
                        EditText edtVol = v.findViewById(R.id.edtConfVolume);
                        EditText edtMfg = v.findViewById(R.id.edtConfMfg);
                        EditText edtExp = v.findViewById(R.id.edtConfExp);
                        EditText edtLot = v.findViewById(R.id.edtConfLot);

                        cbxProduct.setSelection(HunterMobileWMS.getProductList().indexOf(p) + 1);
                        cbxProduct.setEnabled(false);

                        txtQty.setText(frag.getString(R.string.label_quantityUM, "KG"));
                        edtQty.setHint(frag.getString(R.string.label_quantityUM, "KG"));
                        edtQty.setText(DF.format(sugar.getQuantity()));
                        edtQty.setEnabled(false);

                        edtVol.setText("1");
                        edtVol.setEnabled(false);
                        edtMfg.setText(sugar.getLot_mfg());
                        edtMfg.setEnabled(false);
                        edtExp.setText(sugar.getLot_exp());
                        edtExp.setEnabled(false);
                        edtLot.setText(sugar.getLot_id());
                        edtLot.setEnabled(false);
                        v.findViewById(R.id.cbxConfIndeterminate).setEnabled(false);
                        v.setId(vId++);
                        frag.mViewModel.addSugarThingId(v.getId(), sugar.getThing_id());
                        v.setBackground(printed);
                    }
                });
            } else if (frag.mViewModel.getDocument() == null) {
                frag.mViewModel.setDocument(d);
                frag.mViewModel.createReturn();
                if (!frag.isPaConf) {
                    final List<Product> toAdd = new ArrayList<>();
                    boolean clearedItems = false;

                    for (DocumentItem di : d.getItems()) {
                        boolean kit = false;
                        Product p = di.getProduct();
                        Set<ProductField> fields = p.getFields();

                        if (fields == null || fields.isEmpty()) {
                            fields = new HashSet<>(HunterMobileWMS.getDB().pfDao().listByProductId(p.getId()));
                        }

                        for (ProductField pf : fields) {
                            if (pf.getModelId().equals(UUID.fromString(MP_PF_KIT_ID))) {
                                kit = pf.getValue().equalsIgnoreCase("true") || pf.getValue().equalsIgnoreCase("X");
                                break;
                            }
                        }
                        if (kit) {
                            //Document has kit, need to change the product views for the children
                            List<Product> pList = HunterMobileWMS.getDB().prdDao().listByParent(p.getId());
                            if (!clearedItems) {
                                frag.mViewModel.getReturn().getItems().clear(); //remove the currentitems
                                clearedItems = true;
                            }

                            for (Product pChild : pList) {
                                double kitQty = 0d;
                                AGLDocItem admi = new AGLDocItem();
                                Set<ProductField> childFields = pChild.getFields();

                                if (childFields == null || childFields.isEmpty()) {
                                    childFields = new HashSet<>(HunterMobileWMS.getDB().pfDao().listByProductId(pChild.getId()));
                                }

                                for (ProductField pf : childFields) {
                                    if (pf.getModelId().equals(UUID.fromString(MP_PF_KIT_QTY_ID))) {
                                        kitQty = Double.parseDouble(pf.getValue().replace(",", "."));
                                        break;
                                    }
                                }
                                admi.setProduct_id(pChild.getId().toString());
                                admi.setQty(di.getQty() * kitQty);
                                admi.setLayer(di.getLayer());
                                admi.setMeasureUnit(di.getMeasureUnit());
                                frag.mViewModel.getReturn().getItems().add(admi);
                                toAdd.add(pChild);
                            }
                        } else {
                            toAdd.add(p);
                        }
                        publishProgress(1);
                    }
                    Objects.requireNonNull(frag.getActivity()).runOnUiThread(() -> {
                        for (Product add : toAdd) {
                            CheckingItem tmpConf = frag.addConferenceItem();
                            View v = tmpConf.getView();
                            SearchableSpinner cbxProduct = v.findViewById(R.id.cbxConfProduct);
                            TextView txtQty = v.findViewById(R.id.txtConfQuantity);
                            EditText edtQty = v.findViewById(R.id.edtConfQuantity);
                            int index = 0;

                            for (int i = 0; i < HunterMobileWMS.getProductList().size(); i++) {
                                Product p = HunterMobileWMS.getProductList().get(i);
                                if (p.getId().equals(add.getId())) {
                                    index = i + 1;
                                    break;
                                }
                            }

                            for (ProductField pf : add.getFields()) {
                                if (pf.getModelId().equals(UUID.fromString(MP_PF_UM_ID))) {
                                    txtQty.setText(frag.getString(R.string.label_quantityUM, pf.getValue()));
                                    edtQty.setHint(frag.getString(R.string.label_quantityUM, pf.getValue()));
                                    break;
                                }
                            }
                            cbxProduct.setSelection(index, false);
                        }
                    });
                } else {
                    if (d.getThings().size() == 0)
                        return "Aguardando Descarga dos Paletes";
                    final DocumentThing[] ar = new DocumentThing[d.getThings().size()];
                    Comparator<DocumentThing> compareByMetaname = (DocumentThing d1, DocumentThing d2) -> {
                        Address o1 = d1.getThing().getAddress();
                        Address o2 = d2.getThing().getAddress();
                        int seq1 = 0;
                        int seq2 = 0;
                        if (o1 == null && o2 == null) return 0;
                        if (o1 == null) return 1;
                        if (o1.getFields().isEmpty()) {
                            o1.getFields().addAll(HunterMobileWMS.getDB().afDao().listByAddressId(o1.getId()));
                            HunterMobileWMS.putFields(o1.getId(), o1.getFields());
                        }
                        if (o2 == null) return -1;
                        if (o2.getFields().isEmpty()) {
                            o2.getFields().addAll(HunterMobileWMS.getDB().afDao().listByAddressId(o2.getId()));
                            HunterMobileWMS.putFields(o2.getId(), o2.getFields());
                        }

                        for (AddressField af : o1.getFields()) {
                            if (af.getModelId().equals(UUID.fromString(AF_SEQ))) {
                                seq1 = Integer.parseInt(af.getValue());
                                break;
                            }
                        }
                        for (AddressField af : o2.getFields()) {
                            if (af.getModelId().equals(UUID.fromString(AF_SEQ))) {
                                seq2 = Integer.parseInt(af.getValue());
                                break;
                            }
                        }
                        if (o1.getMetaname() == null && o2.getMetaname() == null) return 0;
                        if (o2.getMetaname() == null) return -1;
                        if (o1.getMetaname() == null) return 1;
                        if (seq1 == seq2) return o1.getMetaname().compareTo(o2.getMetaname());
                        return seq1 - seq2;
                    };
                    d.getThings().toArray(ar);
                    Arrays.sort(ar, compareByMetaname);

                    Objects.requireNonNull(frag.getActivity()).runOnUiThread(() -> {//ESSA PORRA DE SAMSUNG SO FUNCIONA COM ESSE PEDACO NA MAIN THREAD! LIXO!
                        for (int i = 0; i < ar.length; i++) {
                            DocumentThing dt = ar[i];
                            Thing itemContainer = dt.getThing();
                            CheckingItem tmpConf = frag.addConferenceItem();
                            SearchableSpinner cbxAddress = tmpConf.getView().findViewById(R.id.cbxConfAddress);
                            AddressAdapter adapter = (AddressAdapter) cbxAddress.getAdapter();

                            cbxAddress.setSelection(adapter.getItemPosition(itemContainer.getAddress()), false);
                            tmpConf.setThing(itemContainer);
                            publishProgress(i);
                        }
                        frag.runOnThread(() -> {
                            for (CheckingItem savedConf : confList) {
                                boolean found = false;
                                for (CheckingItem tmpConf : frag.mViewModel.getConferenceItems()) {
                                    if (savedConf.getThing_id() != null && tmpConf.getThing_id() != null && savedConf.getThing_id().equals(tmpConf.getThing_id()) && savedConf.getQuantity() != null) {
                                        frag.fillSaved(savedConf, tmpConf);
                                        HunterMobileWMS.getDB().checkItemDao().save(tmpConf);
                                        found = true;
                                        break;
                                    }
                                }

                                if (!found && savedConf.getQuantity() != null && savedConf.getLot_id() != null && savedConf.getLot_mfg() != null && savedConf.getLot_exp() != null && savedConf.getAddress_id() != null) {
                                    Objects.requireNonNull(frag.getActivity()).runOnUiThread(() -> {
                                        CheckingItem tmpConf = frag.addConferenceItem();
                                        SearchableSpinner cbxAddress = tmpConf.getView().findViewById(R.id.cbxConfAddress);
                                        AddressAdapter adapter = (AddressAdapter) cbxAddress.getAdapter();
                                        Address addr = HunterMobileWMS.findAddress(savedConf.getAddress_id());

                                        frag.fillSaved(savedConf, tmpConf);
                                        cbxAddress.setSelection(adapter.getItemPosition(addr), false);
                                    });
                                }
                            }
                        });
                    });
                }
            }

            if (frag.mViewModel.getReturn().getProps().containsKey("CTE"))
                frag.edtCTE.setText(frag.mViewModel.getReturn().getProps().get("CTE"));
            return "OK";
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            CheckingFragment frag = fragRef.get();
            if (BuildConfig.DEBUG)
                Log.d("Progress", "" + values[0]);
            frag.progress.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            CheckingFragment frag = fragRef.get();

            frag.progress.setVisibility(View.GONE);
            if (result.equals("OK")) {
                frag.txtVolCount.setVisibility(View.VISIBLE);
            } else {
                frag.showError("Documento Incorreto", result, true);
                frag.clearViewModel();
            }
            Executors.newSingleThreadScheduledExecutor().schedule(() -> {
                frag.init = true;
                if (BuildConfig.DEBUG)
                    Log.d("Views", "Initialized");
            }, 1000, TimeUnit.MILLISECONDS);
        }
    }

    private static class AsyncSendAction extends AsyncTask<Integer, Void, Boolean> {
        private boolean finish = false;
        private final WeakReference<CheckingFragment> fragRef;
        private ProgressDialog progressDialog;

        public AsyncSendAction(CheckingFragment f) {
            this.fragRef = new WeakReference<>(f);
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(fragRef.get().getActivity());
            // Set horizontal animation_progress bar style.
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            // Set animation_progress dialog icon.
            progressDialog.setIcon(R.drawable.image_logo_gtp);
            // Set animation_progress dialog title.
            progressDialog.setTitle(fragRef.get().getString(R.string.info_sending));
            // Whether animation_progress dialog can be canceled or not.
            progressDialog.setCancelable(true);
            // When user touch area outside animation_progress dialog whether the animation_progress dialog will be canceled or not.
            progressDialog.setCanceledOnTouchOutside(false);
            // Set animation_progress dialog message.
            progressDialog.setMessage(fragRef.get().getString(R.string.question_send_to_hunter));
            // Popup the animation_progress dialog.
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            CheckingFragment frag = fragRef.get();
            AGLDocument ret = frag.checkCount();

            if (ret != null) {
                if (ret.getStatus().equals("ERROR"))
                    return true;
                if (frag.mViewModel.getReturn().getStatus().equals("SUCESSO")) {
                    if (frag.getActionListener().sendAGLDocument(frag.mViewModel.getReturn())) {
                        finish = true;
                        return true;
                    } else
                        return false;
                } else if (frag.mViewModel.getErrorCount() >= MAX_RETRY) {
                    if (frag.getActionListener().sendAGLDocument(frag.mViewModel.getReturn())) {
                        finish = true;
                        return true;
                    } else
                        return false;
                } else
                    return true;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            CheckingFragment frag = fragRef.get();

            if (!result) {
                frag.showError(frag.getString(R.string.connection_failed), frag.getString(R.string.try_again), true);
            } else if (finish) {
                if (frag.mViewModel.getErrorCount() >= MAX_RETRY) {
                    MaterialAlertDialogBuilder alertBuilder = new MaterialAlertDialogBuilder(Objects.requireNonNull(frag.getActivity()));

                    alertBuilder.setIcon(android.R.drawable.ic_dialog_info);
                    alertBuilder.setCancelable(true)
                            .setTitle(frag.getString(R.string.info_max_retry_reached))
                            .setMessage(frag.getString(R.string.msg_alert_supervisor, frag.getViewModel().getDocument().getCode()))
                            .setNeutralButton(android.R.string.yes, (dialog, whichButton) -> {
                                dialog.dismiss();
                                frag.runOnThread(() -> HunterMobileWMS.getDB().sugarDao().clearSugar());
                                frag.clearViewModel();
                            })
                            .create().show();
                } else {
                    frag.runOnThread(() -> HunterMobileWMS.getDB().sugarDao().clearSugar());
                    frag.clearViewModel();
                }
            }
            if (progressDialog != null)
                progressDialog.dismiss();
        }
    }

    private static class AsyncPrintAction extends AsyncTask<Integer, Void, Boolean> {

        private final WeakReference<View> vwRef;
        private final PrintPayload payload;
        private final SugarPrintTemporary print;
        private final WeakReference<CheckingFragment> fragRef;
        private ProgressDialog progressDialog;

        public AsyncPrintAction(CheckingFragment f, View vw, PrintPayload payload, SugarPrintTemporary print) {
            this.fragRef = new WeakReference<>(f);
            this.vwRef = new WeakReference<>(vw);
            this.payload = payload;
            this.print = print;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(fragRef.get().getActivity());
            // Set horizontal animation_progress bar style.
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            // Set animation_progress dialog icon.
            progressDialog.setIcon(R.drawable.image_logo_gtp);
            // Set animation_progress dialog title.
            progressDialog.setTitle("Enviando...");
            // Whether animation_progress dialog can be canceled or not.
            progressDialog.setCancelable(true);
            // When user touch area outside animation_progress dialog whether the animation_progress dialog will be canceled or not.
            progressDialog.setCanceledOnTouchOutside(false);
            // Set animation_progress dialog message.
            progressDialog.setMessage("Aguarde um instante");
            // Popup the animation_progress dialog.
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            return fragRef.get().getActionListener().sendPrintFromConference(payload);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            CheckingFragment frag = fragRef.get();
            View vw = vwRef.get();

            if (result) {
                Drawable printed = ContextCompat.getDrawable(Objects.requireNonNull(frag.getActivity()), R.drawable.background_conf_print);
                SearchableSpinner cbxProduct = vw.findViewById(R.id.cbxConfProduct);
                EditText edtMfg = vw.findViewById(R.id.edtConfMfg);
                EditText edtExp = vw.findViewById(R.id.edtConfExp);
                EditText edtLot = vw.findViewById(R.id.edtConfLot);
                EditText edtQty = vw.findViewById(R.id.edtConfQuantity);
                EditText edtVol = vw.findViewById(R.id.edtConfVolume);

                frag.mViewModel.addSugarThingId(vw.getId(), payload.getThing());
                edtMfg.setEnabled(false);
                edtExp.setEnabled(false);
                edtLot.setEnabled(false);
                edtQty.setEnabled(false);
                edtVol.setEnabled(false);
                cbxProduct.setEnabled(false);
                vw.findViewById(R.id.cbxConfIndeterminate).setEnabled(false);

                frag.selectedView = -1;
                vw.setSelected(false);
                vw.setBackground(printed);
                frag.runOnThread(() -> HunterMobileWMS.getDB().sugarDao().save(print));
            } else {
                frag.showError(frag.getString(R.string.connection_failed), frag.getString(R.string.try_again), true);
            }
            if (progressDialog != null)
                progressDialog.dismiss();
        }
    }
}
