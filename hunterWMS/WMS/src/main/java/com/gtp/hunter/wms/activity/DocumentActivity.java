package com.gtp.hunter.wms.activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NavUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.gtp.hunter.BuildConfig;
import com.gtp.hunter.HunterMobileWMS;
import com.gtp.hunter.R;
import com.gtp.hunter.structure.NoAnimationItemAnimator;
import com.gtp.hunter.structure.WrapContentLinearLayoutManager;
import com.gtp.hunter.structure.adapter.AddressAdapter;
import com.gtp.hunter.structure.adapter.ProductAdapter;
import com.gtp.hunter.structure.adapter.ThingRecyclerViewAdapter;
import com.gtp.hunter.structure.spinner.SearchableSpinner;
import com.gtp.hunter.util.ProductUtil;
import com.gtp.hunter.util.ThingUtil;
import com.gtp.hunter.wms.client.CustomDocumentClient;
import com.gtp.hunter.wms.client.DocumentClient;
import com.gtp.hunter.wms.model.AGLDocItem;
import com.gtp.hunter.wms.model.AGLDocument;
import com.gtp.hunter.wms.model.AGLThing;
import com.gtp.hunter.wms.model.Address;
import com.gtp.hunter.wms.model.AddressField;
import com.gtp.hunter.wms.model.Document;
import com.gtp.hunter.wms.model.DocumentField;
import com.gtp.hunter.wms.model.DocumentItem;
import com.gtp.hunter.wms.model.IntegrationReturn;
import com.gtp.hunter.wms.model.Product;
import com.gtp.hunter.wms.model.ProductField;
import com.gtp.hunter.wms.model.Thing;
import com.gtp.hunter.wms.model.ViewThingStub;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;

import io.reactivex.annotations.NonNull;
import retrofit2.Call;
import retrofit2.Response;
import timber.log.Timber;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class DocumentActivity extends AppCompatActivity {
    private static final Product PALLET_PRD = HunterMobileWMS.findProduct(UUID.fromString("95b564e9-ea5a-4caa-adbe-06fc7dd0b966"));
    private final Gson gson = new GsonBuilder().serializeNulls().excludeFieldsWithoutExposeAnnotation().enableComplexMapKeySerialization().create();
    private static final SimpleDateFormat SQL_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    private static final SimpleDateFormat LOT_FORMAT = new SimpleDateFormat("'CNAT'ddMMyy", Locale.US);
    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
    private static final DecimalFormat DF = new DecimalFormat("#0.0000", DecimalFormatSymbols.getInstance(Locale.US));
    private static final long RESEND_DELAY = 5000;

    private DatePickerDialog dPicker;
    private TimePickerDialog tPicker;

    private String type;

    private AGLDocument ret;
    private Product activeProduct;
    private ThingRecyclerViewAdapter adapter;
    private CopyOnWriteArrayList<ViewThingStub> things;

    private CustomDocumentClient cdClient;
    private DocumentClient dcClient;

    private View apoBox;
    private View activeBox;
    private String selectedLine;
    private TextView txtPalletCount;
    private MenuItem mniBlock;
    private Handler hndlr;

    private boolean production;
    private int palletCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JsonObject params = gson.fromJson(Objects.requireNonNull(getIntent().getExtras()).getString("params"), JsonObject.class);
        String model = params.get("model").getAsString();

        type = params.get("type").getAsString();
        production = type.equalsIgnoreCase("APONTAMENTO");
        if (model.equalsIgnoreCase("APONTAMENTO")) model = "ORDCRIACAO";
        setContentView(R.layout.activity_document);
        hndlr = new Handler();
        switch (type) {
            case "APONTAMENTO":
                apoChooser();
                break;
            case "DAMAGE":
                prepareDMG();
                break;
            case "RNC":
                prepareRNC();
                break;
            case "CREATE":
                prepareCRE();
                break;
            case "RESTOCK":
                prepareRST();
                break;
            case "REPACK":
                prepareRPK();
                break;
            case "RETURN":
                prepareRET();
                break;
        }
        createBaseDocument(model, null);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (HunterMobileWMS.getUser() != null) {
            showFullScreen();
        } else {
            Intent intent = new Intent(this, LoginActivity.class);

            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        cdClient = new CustomDocumentClient(getBaseContext());
        dcClient = new DocumentClient(getBaseContext());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.document, menu);
        return true;
    }

    /**
     * Prepare the menu options
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mniBlock = menu.findItem(R.id.mnuSend);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            // This ID represents the Home or Up button.
            NavUtils.navigateUpFromSameTask(this);
            return true;
        } else if (id == R.id.mnuSend) {
            if (type.equals("REPACK")) {
                if (things.size() > 0) {
                    item.setEnabled(false);
                    Thing pallet = new Thing();
                    Address address = ret.getStatus().equals("REEMBALAGEM") ? HunterMobileWMS.findAddress(UUID.fromString("a5b8c839-408a-11ea-b9fa-005056a19775")) : HunterMobileWMS.findAddress(UUID.fromString("2814d99b-34a2-11ea-8a83-005056a19775"));
                    double totalWeight = Double.parseDouble(ProductUtil.getGrossWeight(PALLET_PRD).getValue());

                    pallet.setId(UUID.randomUUID());
                    pallet.setStatus("REPACK");
                    pallet.setAddress(address);
                    pallet.setProduct(PALLET_PRD);
                    pallet.setName(PALLET_PRD.getName());
                    pallet.setCreatedAt(new Date());
                    pallet.setUpdatedAt(new Date());
                    pallet.getProperties().add(ThingUtil.createField("QUANTITY", "1.0000"));
                    pallet.getProperties().add(ThingUtil.createField("LOT_ID", "VARIADO"));
                    pallet.getProperties().add(ThingUtil.createField("MANUFACTURING_BATCH", SQL_FORMAT.format(new Date())));
                    pallet.getProperties().add(ThingUtil.createField("LOT_EXPIRE", SQL_FORMAT.format(new Date())));
                    for (ViewThingStub stub : things) {
                        AGLDocItem aitm = new AGLDocItem();
                            Thing thPrd = stub.getThing();

                            aitm.setProduct_id(thPrd.getProduct().getId().toString());
                            aitm.setMeasureUnit(ProductUtil.getMeasureUnit(thPrd.getProduct()).getValue());
                            aitm.setQty(Double.parseDouble(ThingUtil.getQuantityField(thPrd).getValue()));
                            aitm.setStatus("REPACK");
                            aitm.setCreatedAtSQL(SQL_FORMAT.format(new Date()));
                            aitm.setUpdatedAtSQL(SQL_FORMAT.format(new Date()));
                            ret.getItems().add(aitm);
                            totalWeight += Double.parseDouble(ThingUtil.getQuantityField(thPrd).getValue());
                            thPrd.setId(UUID.randomUUID());
                            thPrd.setAddress(address);
                            thPrd.setCreatedAt(new Date());
                            thPrd.setUpdatedAt(new Date());
                            pallet.getSiblings().add(thPrd);
                        }
                        pallet.getProperties().add(ThingUtil.createField("STARTING_WEIGHT", DF.format(totalWeight)));
                        pallet.getProperties().add(ThingUtil.createField("ACTUAL_WEIGHT", DF.format(totalWeight)));
                        ret.getThings().add(pallet.getAGLThing());
                        new AsyncSendDocument(this).execute();
                    } else
                        showError(getString(R.string.fail), getString(R.string.select_product));
                } else {
                    boolean nullAddress = false;
                    boolean nullProduct = false;

                    ret.setCreatedAtSQL(SQL_FORMAT.format(Calendar.getInstance().getTime()));
                    ret.setUpdatedAtSQL(SQL_FORMAT.format(Calendar.getInstance().getTime()));
                    for (AGLThing at : ret.getThings()) {
                        at.setCreated_at(SQL_FORMAT.format(Calendar.getInstance().getTime()));
                        at.setUpdated_at(SQL_FORMAT.format(Calendar.getInstance().getTime()));

                        if (at.getAddress_id() == null || at.getAddress_id().isEmpty())
                            nullAddress = true;
                        for (AGLThing ats : at.getSiblings()) {
                            ats.setCreated_at(SQL_FORMAT.format(Calendar.getInstance().getTime()));
                            ats.setUpdated_at(SQL_FORMAT.format(Calendar.getInstance().getTime()));
                            if (ats.getAddress_id() == null || ats.getAddress_id().isEmpty())
                                nullAddress = true;
                            if (ats.getProduct_id() == null || ats.getProduct_id().isEmpty())
                                nullProduct = true;
                        }
                    }
                if (nullAddress) {
                    showError(getString(R.string.fail), getString(R.string.select_address));
                } else if (nullProduct) {
                    showError(getString(R.string.fail), getString(R.string.select_product));
                } else {
                    item.setEnabled(false);
                    new AsyncSendDocument(this).execute();
                }
            }
            return true;
        } else if (id == R.id.mnuClear) {
            clearForms();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void clearForms() {
        if (things != null)
            things.clear();
        clearFields(findViewById(R.id.creBox));
        clearFields(findViewById(R.id.rstBox));
        clearFields(findViewById(R.id.damageBox));
        clearFields(findViewById(R.id.repackBox));
        clearFields(findViewById(R.id.rncBox));
        clearFields(findViewById(R.id.returnBox));
        if (!production)
            clearFields(findViewById(R.id.apoBox));
    }

    private void clearFields(ViewGroup group) {
        for (int i = 0, count = group.getChildCount(); i < count; ++i) {
            View view = group.getChildAt(i);

            if (view instanceof EditText) {
                ((EditText) view).setText("");
            } else if (view instanceof SearchableSpinner) {
                ((SearchableSpinner) view).setSelection(0, false);
            } else if (view instanceof CheckBox) {
                ((CheckBox) view).setChecked(false);
            } else if (view instanceof RecyclerView) {
                if (((RecyclerView) view).getAdapter() != null)
                    runOnUiThread(() -> Objects.requireNonNull(((RecyclerView) view).getAdapter()).notifyDataSetChanged());
            }

            if (view instanceof ViewGroup && (((ViewGroup) view).getChildCount() > 0))
                clearFields((ViewGroup) view);
        }
    }

    private void showFullScreen() {
        // Hide UI first
//        ActionBar actionBar = getSupportActionBar();
//        if (actionBar != null) {
//            actionBar.setDisplayHomeAsUpEnabled(false);
//            actionBar.hide();
//        }
//        baseLayout.setVisibility(View.VISIBLE);
//        baseLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
//                | View.SYSTEM_UI_FLAG_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    private void createBaseDocument(@NonNull String model, String parent_id) {
        boolean rst = model.equalsIgnoreCase("RESTOCK");
        boolean cre = model.equalsIgnoreCase("ORDCRIACAO");
        boolean rot = model.equalsIgnoreCase("APORETORNO");
        ret = new AGLDocument();

        ret.setId(UUID.randomUUID().toString());
        ret.setMetaname(model);
        ret.setStatus(type.equals("REPACK") ? "REEMBALAGEM" : "NOVO");
        ret.setUser_id(HunterMobileWMS.getUser().getId().toString());
        if (parent_id != null)
            ret.setParent_id(parent_id);

        if (cre || rst || rot) {
            SearchableSpinner cbxProduct = activeBox.findViewById(R.id.cbxProduct);
            SearchableSpinner cbxAddress = activeBox.findViewById(R.id.cbxAddress);
            EditText edtMan = activeBox.findViewById(R.id.edtManufacture);
            EditText edtExp = activeBox.findViewById(R.id.edtExpiry);
            EditText edtLot = activeBox.findViewById(R.id.edtLot);
            EditText edtQty = activeBox.findViewById(R.id.edtQuantity);
            Product prd = cbxProduct.getSelectedItemPosition() > 0 ? (Product) cbxProduct.getSelectedItem() : null;
            Address add = cbxAddress.getSelectedItemPosition() > 0 ? (Address) cbxAddress.getSelectedItem() : null;
            AGLThing itemContainer = new AGLThing();
            AGLThing item = new AGLThing();
            Date man = null;
            Date exp = null;

            if (edtMan != null && edtMan.getText() != null)
                try {
                    man = SDF.parse(edtMan.getText().toString());
                } catch (ParseException ignored) {
                }
            if (edtExp != null && edtExp.getText() != null)
                try {
                    exp = SDF.parse(edtExp.getText().toString());
                } catch (ParseException ignored) {
                }
            if (prd != null)
                Timber.d("Product %s", prd.toString());
            if (add != null)
                Timber.d("Address %s", add.toString());

            itemContainer.setId(UUID.randomUUID().toString());
            itemContainer.setStatus("NOVO");
            itemContainer.setCreated_at(SQL_FORMAT.format(Calendar.getInstance().getTime()));
            itemContainer.setUpdated_at(SQL_FORMAT.format(Calendar.getInstance().getTime()));
            itemContainer.setName(PALLET_PRD.getName());
            itemContainer.setProduct_id(PALLET_PRD.getId().toString());
            itemContainer.setAddress_id(add == null ? null : add.getId().toString());
            itemContainer.setUser_id(HunterMobileWMS.getUser().getId().toString());
            itemContainer.getProps().put("lot_id", edtLot == null ? "" : edtLot.getText().toString());
            itemContainer.getProps().put("actual_weight", "");
            itemContainer.getProps().put("starting_weight", "");
            itemContainer.getProps().put("quantity", edtQty == null || edtQty.getText().length() == 0 ? "1" : edtQty.getText().toString());
            item.setId(UUID.randomUUID().toString());
            item.setStatus("NOVO");
            item.setCreated_at(SQL_FORMAT.format(Calendar.getInstance().getTime()));
            item.setUpdated_at(SQL_FORMAT.format(Calendar.getInstance().getTime()));
            item.setName(prd == null ? null : prd.getName());
            item.setProduct_id(prd == null ? null : prd.getId().toString());
            item.setParent_id(itemContainer.getId());
            item.setAddress_id(add == null ? null : add.getId().toString());
            item.setUser_id(HunterMobileWMS.getUser().getId().toString());
            if (man != null) {
                itemContainer.getProps().put("manufacturing_batch", SQL_FORMAT.format(man));
                item.getProps().put("manufacturing_batch", SQL_FORMAT.format(man));
            } else {
                itemContainer.getProps().put("manufacturing_batch", "");
                item.getProps().put("manufacturing_batch", "");
            }
            if (exp != null) {
                itemContainer.getProps().put("lot_expire", SQL_FORMAT.format(exp));
                item.getProps().put("lot_expire", SQL_FORMAT.format(exp));
            } else {
                itemContainer.getProps().put("lot_expire", "");
                item.getProps().put("lot_expire", "");
            }
            item.getProps().put("lot_id", edtLot == null ? "" : edtLot.getText().toString());
            item.getProps().put("actual_weight", "");
            item.getProps().put("starting_weight", "");
            item.getProps().put("quantity", edtQty == null ? "1" : edtQty.getText().toString());
            itemContainer.getSiblings().add(item);
            ret.getThings().add(itemContainer);
            if (activeProduct != null)
                updateProduct();
        }
    }

    private void prepareRET() {
        View retBox = findViewById(R.id.returnBox);
        SearchableSpinner cbxProduct = retBox.findViewById(R.id.cbxProduct);
        SearchableSpinner cbxAddress = retBox.findViewById(R.id.cbxAddress);
        EditText edtQty = retBox.findViewById(R.id.edtQuantity);
        CopyOnWriteArrayList<Product> productList = new CopyOnWriteArrayList<>(HunterMobileWMS.getRSTProductList());
        CopyOnWriteArrayList<Address> addressList = new CopyOnWriteArrayList<>(HunterMobileWMS.getBottomAddressListTopType("BATE_CAIXA"));

        edtQty.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                for (AGLThing at : ret.getThings()) {
                    for (AGLThing ats : at.getSiblings())
                        ats.getProps().put("quantity", edtQty.getText().length() == 0 ? "1" : edtQty.getText().toString());
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
            }
        });
        cbxProduct.setAdapter(new ProductAdapter(this, R.layout.item_product, productList).init());
        cbxProduct.setTitle(getString(R.string.select_product));
        cbxProduct.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                Product prd = null;
                if (cbxProduct.getSelectedItemPosition() > 0) {
                    prd = (Product) cbxProduct.getSelectedItem();
                    prdSelected(prd);
                }

                if (prd != null) {
                    for (AGLThing at : ret.getThings()) {
                        at.getProps().put("lot_id", LOT_FORMAT.format(Calendar.getInstance().getTime()) + prd.getSku());
                        at.getProps().put("manufacturing_batch", SDF.format(Calendar.getInstance().getTime()));
                        at.getProps().put("lot_expire", SDF.format(Calendar.getInstance().getTime()));
                        at.getProps().put("quantity", "1");
                        for (AGLThing ats : at.getSiblings()) {
                            ats.setName(prd.getName());
                            ats.getProps().put("lot_id", LOT_FORMAT.format(Calendar.getInstance().getTime()) + prd.getSku());
                            ats.getProps().put("manufacturing_batch", SDF.format(Calendar.getInstance().getTime()));
                            ats.getProps().put("lot_expire", SDF.format(Calendar.getInstance().getTime()));
                            ats.getProps().put("quantity", edtQty.getText().length() == 0 ? "1" : edtQty.getText().toString());
                            ats.setProduct_id(prd.getId().toString());
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        cbxAddress.setTitle(getString(R.string.select_address));
        cbxAddress.setAdapter(new AddressAdapter(this, R.layout.item_address, addressList).init());
        cbxAddress.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String addId = null;

                if (cbxAddress.getSelectedItemPosition() > 0) {
                    addId = ((Address) cbxAddress.getSelectedItem()).getId().toString();
                }
                for (AGLThing at : ret.getThings()) {
                    at.setAddress_id(addId);
                    for (AGLThing ats : at.getSiblings()) {
                        ats.setAddress_id(addId);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        setTitle(getString(R.string.menu_MOBILERET, ""));
        retBox.setVisibility(View.VISIBLE);
        activeBox = retBox;
    }

    private void prepareRPK() {
        View rpkBox = findViewById(R.id.repackBox);
        RadioGroup rbgLocation = rpkBox.findViewById(R.id.rbgLocation);
        SearchableSpinner cbxProduct = rpkBox.findViewById(R.id.cbxProduct);
        TextInputEditText edtMfg = rpkBox.findViewById(R.id.edtManufacture);
        TextInputEditText edtExp = rpkBox.findViewById(R.id.edtExpiry);
        TextInputEditText edtQty = rpkBox.findViewById(R.id.edtQty);
        Button btnAdd = rpkBox.findViewById(R.id.btnAdd);
        CopyOnWriteArrayList<Product> productList = new CopyOnWriteArrayList<>(HunterMobileWMS.getPAProductList());
        RecyclerView thingListView = rpkBox.findViewById(R.id.palletList);

        things = new CopyOnWriteArrayList<>();
        adapter = new ThingRecyclerViewAdapter(things, null);
        thingListView.setLayoutManager(new WrapContentLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        thingListView.setItemAnimator(new NoAnimationItemAnimator());
        thingListView.setAdapter(adapter);
        edtMfg.setOnClickListener(v1 -> {
            final Calendar cal = Calendar.getInstance();
            EditText inst = (EditText) v1;
            String currVal = inst.getText().toString();

            if (!currVal.isEmpty()) {
                Date curr;

                try {
                    curr = SDF.parse(currVal);
                    if (curr != null) cal.setTime(curr);
                } catch (ParseException ignored) {
                }
            }
            int day = cal.get(Calendar.DAY_OF_MONTH);
            int month = cal.get(Calendar.MONTH);
            int year = cal.get(Calendar.YEAR);
            // date picker dialog
            dPicker = new DatePickerDialog(this,
                    (view, year1, monthOfYear, dayOfMonth) -> edtMfg.setText(getString(R.string.normal_date, dayOfMonth, (monthOfYear + 1), year1)), year, month, day);
            dPicker.getDatePicker().setMaxDate(Calendar.getInstance().getTimeInMillis());
            dPicker.show();
        });

        edtExp.setOnClickListener(v1 -> {
            final Calendar cal = Calendar.getInstance();
            EditText inst = (EditText) v1;
            String currVal = inst.getText().toString();

            if (!currVal.isEmpty()) {
                Date curr;

                try {
                    curr = SDF.parse(currVal);
                    if (curr != null) cal.setTime(curr);
                } catch (ParseException ignored) {
                }
            }
            int day = cal.get(Calendar.DAY_OF_MONTH);
            int month = cal.get(Calendar.MONTH);
            int year = cal.get(Calendar.YEAR);
            // date picker dialog
            dPicker = new DatePickerDialog(this,
                    (view, year1, monthOfYear, dayOfMonth) -> {
                        edtExp.setText(getString(R.string.normal_date, dayOfMonth, (monthOfYear + 1), year1));

                        if (cbxProduct.getSelectedItemPosition() > 0) {
                            prdSelected((Product) cbxProduct.getSelectedItem());
                        }
                    },
                    year, month, day);
            dPicker.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis());
            dPicker.show();
        });
        cbxProduct.setAdapter(new ProductAdapter(this, R.layout.item_product, productList).init());
        cbxProduct.setTitle(getString(R.string.select_product));
        cbxProduct.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (cbxProduct.getSelectedItemPosition() > 0) {
                    prdSelected((Product) cbxProduct.getSelectedItem());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        btnAdd.setOnClickListener((v) -> {
            if (edtQty.getText() == null || edtQty.getText().length() == 0) {
                Toast.makeText(this, getString(R.string.filling_required, getString(R.string.label_quantity)), Toast.LENGTH_LONG).show();
                return;
            }
            if (edtExp.getText() == null || edtExp.getText().length() == 0) {
                Toast.makeText(this, getString(R.string.filling_required, getString(R.string.label_expiry)), Toast.LENGTH_LONG).show();
                return;
            }
            if (edtMfg.getText() == null || edtMfg.getText().length() == 0) {
                Toast.makeText(this, getString(R.string.filling_required, getString(R.string.label_manufacture)), Toast.LENGTH_LONG).show();
                return;
            }
            if (cbxProduct.getSelectedItemPosition() <= 0) {
                Toast.makeText(this, getString(R.string.filling_required, getString(R.string.label_product)), Toast.LENGTH_LONG).show();
                return;
            }
            Thing t = new Thing();
            Product prd = (Product) cbxProduct.getSelectedItem();
            String sMan = edtMfg.getText().toString();
            String sExp = edtExp.getText().toString();
            Date man = Calendar.getInstance().getTime();
            Date exp = Calendar.getInstance().getTime();
            String lotId = prd.getSku();
            int qty = Integer.parseInt(edtQty.getText().toString());
            double weight = Double.parseDouble(ProductUtil.getGrossWeight(prd).getValue());
            try {
                man = SDF.parse(sMan);
                SimpleDateFormat rpkLot = new SimpleDateFormat("'RPCK'ddMMyy", Locale.US);
                lotId = String.format("%s%s", rpkLot.format(Objects.requireNonNull(man)), lotId);
            } catch (ParseException ignored) {

            }
            try {
                exp = SDF.parse(sExp);
            } catch (ParseException ignored) {

            }
            t.setId(UUID.randomUUID());
            t.setProduct(prd);
            t.setStatus("REPACK");
            t.setName(prd.getName());
            t.getProperties().add(ThingUtil.createField("QUANTITY", DF.format(qty)));
            t.getProperties().add(ThingUtil.createField("LOT_ID", lotId));
            t.getProperties().add(ThingUtil.createField("MANUFACTURING_BATCH", SQL_FORMAT.format(Objects.requireNonNull(man))));
            t.getProperties().add(ThingUtil.createField("LOT_EXPIRE", SQL_FORMAT.format(Objects.requireNonNull(exp))));
            t.getProperties().add(ThingUtil.createField("STARTING_WEIGHT", DF.format(qty * weight)));
            t.getProperties().add(ThingUtil.createField("ACTUAL_WEIGHT", DF.format(qty * weight)));
            ViewThingStub tStub = new ViewThingStub(t);

            things.add(tStub);
            //runOnUiThread(() -> adapter.notifyDataSetChanged());
            adapter.notifyDataSetChanged();
        });
        rbgLocation.setOnCheckedChangeListener((group, checkedId) -> updateDocStatus(checkedId));
        updateDocStatus(rbgLocation.getCheckedRadioButtonId());
        setTitle(getString(R.string.menu_MOBILERPK, ""));
        rpkBox.setVisibility(View.VISIBLE);
        activeBox = rpkBox;
    }

    void updateDocStatus(int id) {
        if (ret != null) {
            if (id == R.id.rbRepack) {
                ret.setStatus("REEMBALAGEM");
            } else if (id == R.id.rbRReturn) {
                ret.setStatus("RETORNO DE ROTA");
            }
        }
    }

    private void prdSelected(Product p) {
        EditText edtMfg = activeBox.findViewById(R.id.edtManufacture);
        EditText edtExp = activeBox.findViewById(R.id.edtExpiry);
        EditText edtQty = activeBox.findViewById(R.id.edtQuantity);
        try {
            if (p.getFields().size() == 0) {
                Executors.newSingleThreadExecutor().submit(() -> p.getFields().addAll(HunterMobileWMS.getDB().pfDao().listByProductId(p.getId()))).get();
            }

            if (edtMfg != null && edtExp != null) {
                ProductField shelf = ProductUtil.getShelfLife(p);
                Calendar man = Calendar.getInstance();
                Date dtExp = SDF.parse(edtExp.getText().toString());
                int sl = shelf.getValue().isEmpty() ? 0 : Integer.parseInt(shelf.getValue());

                man.setTime(Objects.requireNonNull(dtExp));
                man.add(Calendar.DAY_OF_YEAR, sl * -1);
                edtMfg.setText(getString(R.string.normal_date, man.get(Calendar.DAY_OF_MONTH), (man.get(Calendar.MONTH) + 1), man.get(Calendar.YEAR)));
            }
            if (edtQty != null) {
                ProductField palletBox = ProductUtil.getPalletBox(p);
                edtQty.setText(palletBox.getValue());
            }
        } catch (ParseException e) {
            if (BuildConfig.DEBUG)
                e.printStackTrace();
        } catch (Exception ignored) {
        }
    }

    private void prepareDMG() {
        View dmgBox = findViewById(R.id.damageBox);
        SearchableSpinner cbxAddress = dmgBox.findViewById(R.id.cbxAddress);
        EditText edtSeq = dmgBox.findViewById(R.id.edtSeq);

        cbxAddress.setTitle(getString(R.string.select_address));
        cbxAddress.setAdapter(new AddressAdapter(this, R.layout.item_address, new CopyOnWriteArrayList<>(HunterMobileWMS.getAddressList("ADDRESS"))).init());
        cbxAddress.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (cbxAddress.getSelectedItemPosition() > 0) {
                    Address a = (Address) cbxAddress.getSelectedItem();
                    String text = a.getName().contains(".") ? a.getName().substring(a.getName().lastIndexOf(".") + 1) : a.getName();

                    ret.getProps().put("address_id", a.getId().toString());
                    runOnUiThread(() -> Toast.makeText(DocumentActivity.this, text, Toast.LENGTH_LONG).show());
                } else
                    ret.getProps().put("address_id", "");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        edtSeq.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ret.getProps().put("seq", s.toString());
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
            }
        });

        setTitle(getString(R.string.menu_MOBILEDMG, ""));
        dmgBox.setVisibility(View.VISIBLE);
        activeBox = dmgBox;
    }

    private void prepareRNC() {
        View rncBox = findViewById(R.id.rncBox);
        ConstraintLayout layoutLot = rncBox.findViewById(R.id.layoutLot);
        ConstraintLayout layoutProduct = rncBox.findViewById(R.id.layoutProduct);
        SearchableSpinner cbxProduct = layoutProduct.findViewById(R.id.cbxProduct);
        EditText edtStart = layoutProduct.findViewById(R.id.edtStart);
        EditText edtEnd = layoutProduct.findViewById(R.id.edtEnd);
        EditText edtLot = layoutLot.findViewById(R.id.edtLot);
        RadioGroup rGroup = rncBox.findViewById(R.id.rdgRncType);

        Product empty = new Product();

        empty.setId(UUID.randomUUID());
        empty.setSku("");
        empty.setName(getString(R.string.select_product));
        cbxProduct.setTitle(getString(R.string.select_product));
        cbxProduct.setAdapter(new ProductAdapter(this, R.layout.item_product, empty));
        cbxProduct.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                ret.getProps().put("product_id", cbxProduct.getSelectedItemPosition() > 0 ? ((Product) cbxProduct.getSelectedItem()).getId().toString() : "");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                ret.getProps().put("product_id", "");
            }
        });

        edtStart.setOnClickListener(v1 -> {
            final Calendar cal = Calendar.getInstance();
            int day = cal.get(Calendar.DAY_OF_MONTH);
            int month = cal.get(Calendar.MONTH);
            int year = cal.get(Calendar.YEAR);
            int hr = cal.get(Calendar.HOUR_OF_DAY);
            int min = cal.get(Calendar.MINUTE);
            // date picker dialog
            dPicker = new DatePickerDialog(this,
                    (view, year1, monthOfYear, dayOfMonth) -> {
                        final String base = getString(R.string.normal_date, dayOfMonth, (monthOfYear + 1), year1);

                        edtStart.setText(base);
                        tPicker = new TimePickerDialog(this, (vw, hour, minute) -> {
                            String dtTime = base + " " + getString(R.string.normal_time, hour, minute, 0);

                            edtStart.setText(dtTime);
                            ret.getProps().put("hour_start", dtTime);
                        }, hr, min, true);
                        tPicker.setOnCancelListener((v) -> {
                            String dtTime = base + " " + getString(R.string.normal_time, 0, 0, 0);

                            edtStart.setText(dtTime);
                            ret.getProps().put("hour_start", dtTime);
                        });
                        tPicker.show();
                    },
                    year, month, day);
            dPicker.getDatePicker().setMaxDate(Calendar.getInstance().getTimeInMillis());
            dPicker.show();
        });

        edtEnd.setOnClickListener(v1 -> {
            final Calendar cal = Calendar.getInstance();
            int day = cal.get(Calendar.DAY_OF_MONTH);
            int month = cal.get(Calendar.MONTH);
            int year = cal.get(Calendar.YEAR);
            int hr = cal.get(Calendar.HOUR_OF_DAY);
            int min = cal.get(Calendar.MINUTE);
            // date picker dialog
            dPicker = new DatePickerDialog(this,
                    (view, year1, monthOfYear, dayOfMonth) -> {
                        final String base = getString(R.string.normal_date, dayOfMonth, (monthOfYear + 1), year1);

                        edtEnd.setText(base);
                        tPicker = new TimePickerDialog(this, (vw, hour, minute) -> {
                            String dtTime = base + " " + getString(R.string.normal_time, hour, minute, 0);

                            edtEnd.setText(dtTime);
                            ret.getProps().put("hour_end", dtTime);
                        }, hr, min, true);
                        tPicker.setOnCancelListener((v) -> {
                            String dtTime = base + " " + getString(R.string.normal_time, 0, 0, 0);

                            edtEnd.setText(dtTime);
                            ret.getProps().put("hour_end", dtTime);
                        });
                        tPicker.show();
                    },
                    year, month, day);
            dPicker.getDatePicker().setMaxDate(Calendar.getInstance().getTimeInMillis());
            dPicker.show();
        });

        edtLot.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ret.getProps().put("lot_id", s.toString());
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
            }
        });

        rGroup.setOnCheckedChangeListener((group, checkedId) -> {
            // This will get the radiobutton that has changed in its check state
            RadioButton checkedRadioButton = group.findViewById(checkedId);
            // This puts the value (true/false) into the variable
            boolean isChecked = checkedRadioButton.isChecked();
            // If the radiobutton that has changed in check state is now checked...
            if (isChecked)
                if (checkedRadioButton.getText().equals(getString(R.string.label_lot))) {
                    layoutProduct.setVisibility(View.GONE);
                    layoutLot.setVisibility(View.VISIBLE);
                    ret.getProps().put("product_id", "");
                    ret.getProps().put("hour_start", "");
                    ret.getProps().put("hour_end", "");
                    ret.getProps().put("lot_id", edtLot.getText().toString());
                } else {
                    layoutLot.setVisibility(View.GONE);
                    layoutProduct.setVisibility(View.VISIBLE);
                    ret.getProps().put("product_id", cbxProduct.getSelectedItemPosition() > 0 ? ((Product) cbxProduct.getSelectedItem()).getId().toString() : "");
                    ret.getProps().put("hour_start", edtStart.getText().toString());
                    ret.getProps().put("hour_end", edtEnd.getText().toString());
                    ret.getProps().put("lot_id", "");
                }
        });
        setTitle(getString(R.string.menu_MOBILERNC, ""));
        rncBox.setVisibility(View.VISIBLE);
        activeBox = rncBox;
    }

    private void prepareCRE() {
        View creBox = findViewById(R.id.creBox);
        SearchableSpinner cbxProduct = creBox.findViewById(R.id.cbxProduct);
        SearchableSpinner cbxAddress = creBox.findViewById(R.id.cbxAddress);
        EditText edtManufacture = creBox.findViewById(R.id.edtManufacture);
        EditText edtExpiry = creBox.findViewById(R.id.edtExpiry);
        EditText edtLot = creBox.findViewById(R.id.edtLot);
        EditText edtQuantity = creBox.findViewById(R.id.edtQuantity);
        Product empty = new Product();
        Address emptyAddress = new Address();
        Calendar c = Calendar.getInstance();
        Date dateNow = c.getTime();
        String lotId = LOT_FORMAT.format(c.getTime());
        c.add(Calendar.MONTH, 6);
        Date dateExp = c.getTime();

        edtManufacture.setText(SDF.format(dateNow));
        edtExpiry.setText(SDF.format(dateExp));
        edtLot.setText(lotId);
        AGLThing container = ret != null && ret.getThings() != null && !ret.getThings().isEmpty() ? ret.getThings().get(0) : null;

        if (container != null) {
            container.getProps().put("manufacturing_batch", SQL_FORMAT.format(dateNow));
            container.getProps().put("lot_expire", SQL_FORMAT.format(dateExp));
            for (AGLThing ats : container.getSiblings()) {
                ats.getProps().put("manufacturing_batch", SQL_FORMAT.format(dateNow));
                ats.getProps().put("lot_expire", SQL_FORMAT.format(dateExp));
            }
        }
        empty.setId(UUID.randomUUID());
        empty.setSku("");
        empty.setName(getString(R.string.select_product));
        CopyOnWriteArrayList<Product> productList = new CopyOnWriteArrayList<>(HunterMobileWMS.getPAProductList());
        productList.add(0, empty);
        cbxProduct.setTitle(getString(R.string.select_product));
        cbxProduct.setAdapter(new ProductAdapter(this, R.layout.item_product, productList));
        cbxProduct.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                Product p = cbxProduct.getSelectedItemPosition() > 0 ? ((Product) cbxProduct.getSelectedItem()) : null;
                ProductField palletBox = ProductUtil.getPalletBox(p);

                if (p != null) {
                    try {
                        if (p.getFields().size() == 0)
                            Executors.newSingleThreadExecutor().submit(() -> p.getFields().addAll(HunterMobileWMS.getDB().pfDao().listByProductId(p.getId()))).get();
                    } catch (Exception ignored) {
                    }
                    try {
                        edtLot.setText(String.format("%s%s", LOT_FORMAT.format(Objects.requireNonNull(SDF.parse(edtManufacture.getText().toString()))), p.getSku()));
                    } catch (Exception e) {
                        edtLot.setText(String.format("%s%s", lotId, p.getSku()));
                    }
                    AGLThing container = ret != null && ret.getThings() != null && !ret.getThings().isEmpty() ? ret.getThings().get(0) : null;

                    if (container != null) {
                        container.getProps().put("lot_id", edtLot.getText().toString());
                        for (AGLThing ats : ret.getThings().get(0).getSiblings()) {
                            ats.setProduct_id(p.getId().toString());
                            ats.setName(p.getName());
                            ats.getProps().put("lot_id", edtLot.getText().toString());
                        }
                    }

                    if (palletBox != null)
                        edtQuantity.setText(palletBox.getValue());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                for (AGLThing ats : ret.getThings().get(0).getSiblings()) {
                    ats.setProduct_id(null);
                    ats.setName(null);
                }
            }
        });

        emptyAddress.setId(UUID.randomUUID());
        emptyAddress.setName(getString(R.string.select_address));
        CopyOnWriteArrayList<Address> addressList = new CopyOnWriteArrayList<>(HunterMobileWMS.getBottomAddressListTopType("FACTORY"));
        addressList.add(0, emptyAddress);
        addressList.addAll(HunterMobileWMS.getBottomAddressListTopType("DOCK"));
        cbxAddress.setTitle(getString(R.string.select_address));
        cbxAddress.setAdapter(new AddressAdapter(this, R.layout.item_address, addressList));
        cbxAddress.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                ret.getThings().get(0).setAddress_id(cbxAddress.getSelectedItemPosition() > 0 ? ((Address) cbxAddress.getSelectedItem()).getId().toString() : "");
                for (AGLThing ats : ret.getThings().get(0).getSiblings())
                    ats.setAddress_id(cbxAddress.getSelectedItemPosition() > 0 ? ((Address) cbxAddress.getSelectedItem()).getId().toString() : "");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        edtManufacture.setOnClickListener(v1 -> {
            final Calendar cal = Calendar.getInstance();
            int day = cal.get(Calendar.DAY_OF_MONTH);
            int month = cal.get(Calendar.MONTH);
            int year = cal.get(Calendar.YEAR);

            // date picker dialog
            dPicker = new DatePickerDialog(this,
                    (view, year1, monthOfYear, dayOfMonth) -> {
                        final String base = getString(R.string.normal_date, dayOfMonth, (monthOfYear + 1), year1);

                        edtManufacture.setText(base);
                        try {
                            Date chosen = SDF.parse(base);
                            Product p = cbxProduct.getSelectedItemPosition() > 0 ? ((Product) cbxProduct.getSelectedItem()) : null;

                            assert chosen != null;
                            ret.getThings().get(0).getProps().put("manufacturing_batch", SQL_FORMAT.format(chosen));
                            for (AGLThing ats : ret.getThings().get(0).getSiblings())
                                ats.getProps().put("manufacturing_batch", SQL_FORMAT.format(Objects.requireNonNull(SDF.parse(base))));
                            if (p != null)
                                edtLot.setText(String.format("%s%s", LOT_FORMAT.format(chosen), p.getSku()));
                        } catch (ParseException ignored) {
                        }
                    },
                    year, month, day);
            dPicker.getDatePicker().setMaxDate(Calendar.getInstance().getTimeInMillis());
            dPicker.show();
        });

        edtExpiry.setOnClickListener(v1 -> {
            final Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MONTH, 6);
            int day = cal.get(Calendar.DAY_OF_MONTH);
            int month = cal.get(Calendar.MONTH);
            int year = cal.get(Calendar.YEAR);

            // date picker dialog
            dPicker = new DatePickerDialog(this,
                    (view, year1, monthOfYear, dayOfMonth) -> {
                        final String base = getString(R.string.normal_date, dayOfMonth, (monthOfYear + 1), year1);

                        edtExpiry.setText(base);
                        try {
                            ret.getThings().get(0).getProps().put("lot_expire", SQL_FORMAT.format(Objects.requireNonNull(SDF.parse(base))));
                            for (AGLThing ats : ret.getThings().get(0).getSiblings())
                                ats.getProps().put("lot_expire", SQL_FORMAT.format(Objects.requireNonNull(SDF.parse(base))));
                        } catch (ParseException ignored) {
                        }
                    },
                    year, month, day);
            dPicker.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis());
            dPicker.show();
        });

        edtLot.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ret.getThings().get(0).getProps().put("lot_id", s.toString());
                for (AGLThing ats : ret.getThings().get(0).getSiblings())
                    ats.getProps().put("lot_id", s.toString());
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
            }
        });

        edtQuantity.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ret.getThings().get(0).getProps().put("quantity", s.toString());
                for (AGLThing ats : ret.getThings().get(0).getSiblings())
                    ats.getProps().put("quantity", s.toString());
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
            }
        });

        setTitle(getString(R.string.menu_MOBILECRE, ""));
        creBox.setVisibility(View.VISIBLE);
        activeBox = creBox;
    }

    private void prepareRST() {
        View rstBox = findViewById(R.id.rstBox);
        CopyOnWriteArrayList<Product> productList = new CopyOnWriteArrayList<>(HunterMobileWMS.getRSTProductList());
        CopyOnWriteArrayList<Address> addressList = new CopyOnWriteArrayList<>(HunterMobileWMS.getBottomAddressListTopType("FACTORY"));
        SearchableSpinner cbxProduct = rstBox.findViewById(R.id.cbxProduct);
        SearchableSpinner cbxAddress = rstBox.findViewById(R.id.cbxAddress);
        Product empty = new Product();
        Address emptyAddress = new Address();

        empty.setId(UUID.randomUUID());
        empty.setSku("");
        empty.setName(getString(R.string.select_product));
        productList.add(0, empty);
        cbxProduct.setTitle(getString(R.string.select_product));
        cbxProduct.setAdapter(new ProductAdapter(this, R.layout.item_product, productList));
        cbxProduct.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (cbxProduct.getTag() != null && (int) cbxProduct.getTag() != pos) {
                    Product p = cbxProduct.getSelectedItemPosition() > 0 ? ((Product) cbxProduct.getSelectedItem()) : null;

                    if (p != null) {
                        for (AGLThing ats : ret.getThings().get(0).getSiblings()) {
                            ats.setProduct_id(p.getId().toString());
                            ats.getProps().put("quantity", "1");
                        }
                    }
                    cbxProduct.setTag(pos);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        cbxProduct.setTag(0);

        emptyAddress.setId(UUID.randomUUID());
        emptyAddress.setName(getString(R.string.select_address));
        addressList.add(0, emptyAddress);
        cbxAddress.setTitle(getString(R.string.select_address));
        cbxAddress.setAdapter(new AddressAdapter(this, R.layout.item_address, addressList));
        cbxAddress.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (cbxAddress.getTag() != null && (int) cbxAddress.getTag() != pos) {
                    Address a = cbxAddress.getSelectedItemPosition() > 0 ? ((Address) cbxAddress.getSelectedItem()) : null;

                    if (a != null) {
                        ret.getThings().get(0).setAddress_id(a.getId().toString());
                        for (AGLThing ats : ret.getThings().get(0).getSiblings()) {
                            ats.setAddress_id(a.getId().toString());
                        }
                        cbxAddress.setTag(pos);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        cbxAddress.setTag(0);
        setTitle(getString(R.string.menu_MOBILERST, ""));
        rstBox.setVisibility(View.VISIBLE);
        activeBox = rstBox;
    }

    private void apoChooser() {
        apoBox = findViewById(R.id.apoBox);
        Spinner cbxLine = apoBox.findViewById(R.id.cbxLine);

        apoBox.setVisibility(View.VISIBLE);
        cbxLine.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    palletCount = 0;
                    selectedLine = (String) cbxLine.getItemAtPosition(position);

                    new AsyncGetDocument(DocumentActivity.this, selectedLine).execute();
                } else onNothingSelected(parent);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                disableUI();
            }
        });
        setTitle(getString(R.string.menu_MOBILECRE, ""));
        activeBox = apoBox;
    }

    private void disableUI() {
        if (apoBox != null) {
            apoBox.findViewById(R.id.lblApoManufacture).setVisibility(View.GONE);
            apoBox.findViewById(R.id.lblApoExpiry).setVisibility(View.GONE);
            apoBox.findViewById(R.id.lblApoLot).setVisibility(View.GONE);
            apoBox.findViewById(R.id.lblApoQuantity).setVisibility(View.GONE);
            apoBox.findViewById(R.id.cbxProduct).setVisibility(View.GONE);
            apoBox.findViewById(R.id.cbxAddress).setVisibility(View.GONE);
            apoBox.findViewById(R.id.edtManufacture).setVisibility(View.GONE);
            apoBox.findViewById(R.id.edtExpiry).setVisibility(View.GONE);
            apoBox.findViewById(R.id.edtLot).setVisibility(View.GONE);
            apoBox.findViewById(R.id.edtQuantity).setVisibility(View.GONE);
            apoBox.findViewById(R.id.chbIncomplete).setVisibility(View.GONE);
        }
    }


    private void refreshCounter() {
        txtPalletCount.setText(getString(R.string.label_produced_quantity, this.palletCount));
    }

    private void prepareAPO(String lineStr, String docName) {
        Product empty = new Product();
        Calendar c = Calendar.getInstance();
        Date dateNow = c.getTime();
        Date dateExp = c.getTime();
        Address emptyAddress = new Address();
        SearchableSpinner cbxProduct = apoBox.findViewById(R.id.cbxProduct);
        SearchableSpinner cbxAddress = apoBox.findViewById(R.id.cbxAddress);
        EditText edtManufacture = apoBox.findViewById(R.id.edtManufacture);
        EditText edtExpiry = apoBox.findViewById(R.id.edtExpiry);
        EditText edtLot = apoBox.findViewById(R.id.edtLot);
        EditText edtQuantity = apoBox.findViewById(R.id.edtQuantity);
        TextView txtApoPO = apoBox.findViewById(R.id.txtPO);
        CheckBox chbIncomplete = apoBox.findViewById(R.id.chbIncomplete);
        int line = Integer.parseInt(lineStr.replace("LINHA 0", ""));
        String lotId = LOT_FORMAT.format(c.getTime());
        CopyOnWriteArrayList<Product> productList = new CopyOnWriteArrayList<>(HunterMobileWMS.getPAProductList());
        CopyOnWriteArrayList<Address> addressList = new CopyOnWriteArrayList<>(HunterMobileWMS.getBottomAddressListTopType("FACTORY"));

        if (docName != null) {
            txtApoPO.setText(docName);
            txtApoPO.setVisibility(View.VISIBLE);
        }
        txtPalletCount = apoBox.findViewById(R.id.txtPalletCount);
        refreshCounter();
        apoBox.findViewById(R.id.lblApoManufacture).setVisibility(View.VISIBLE);
        apoBox.findViewById(R.id.lblApoExpiry).setVisibility(View.VISIBLE);
        apoBox.findViewById(R.id.lblApoLot).setVisibility(View.VISIBLE);
        apoBox.findViewById(R.id.lblApoQuantity).setVisibility(View.VISIBLE);
        edtManufacture.setVisibility(View.VISIBLE);
        edtManufacture.setText(SDF.format(dateNow));
        edtExpiry.setVisibility(View.VISIBLE);
        edtExpiry.setText(SDF.format(dateExp));
        edtLot.setVisibility(View.VISIBLE);
        edtLot.setText(lotId);
        ret.getThings().get(0).getProps().put("manufacturing_batch", SQL_FORMAT.format(dateNow));
        ret.getThings().get(0).getProps().put("lot_expire", SQL_FORMAT.format(dateExp));
        for (AGLThing ats : ret.getThings().get(0).getSiblings()) {
            ats.getProps().put("manufacturing_batch", SQL_FORMAT.format(dateNow));
            ats.getProps().put("lot_expire", SQL_FORMAT.format(dateExp));
        }
        empty.setId(UUID.randomUUID());
        empty.setSku("");
        empty.setName(getString(R.string.select_product));
        productList.add(0, empty);
        cbxProduct.setVisibility(View.VISIBLE);
        cbxProduct.setTitle(getString(R.string.select_product));
        cbxProduct.setEnabled(false);
        cbxProduct.setAdapter(new ProductAdapter(this, R.layout.item_product, productList));
        cbxProduct.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                Product p = cbxProduct.getSelectedItemPosition() > 0 ? ((Product) cbxProduct.getSelectedItem()) : null;

                if (p != null) {
                    try {
                        if (p.getFields().size() == 0)
                            Executors.newSingleThreadExecutor().submit(() -> p.getFields().addAll(HunterMobileWMS.getDB().pfDao().listByProductId(p.getId()))).get();
                    } catch (Exception ignored) {
                    }
                    if (cbxProduct.getSelectedView() != null)
                        cbxProduct.getSelectedView().setEnabled(false);
                    edtLot.setText(String.format("%s%s", lotId, p.getSku()));
                    ret.getThings().get(0).getProps().put("lot_id", edtLot.getText().toString());
                    for (AGLThing ats : ret.getThings().get(0).getSiblings()) {
                        ats.setProduct_id(p.getId().toString());
                        ats.setName(p.getName());
                        ats.getProps().put("lot_id", edtLot.getText().toString());
                    }
                    ProductField palletBox = ProductUtil.getPalletBox(p);
                    ProductField shelf = ProductUtil.getShelfLife(p);
                    if (palletBox != null) {
                        edtQuantity.setText(palletBox.getValue());
                    }
                    if (shelf != null && !shelf.getValue().isEmpty()) {
                        Calendar cal = Calendar.getInstance();

                        cal.add(Calendar.DAY_OF_YEAR, Integer.parseInt(shelf.getValue()));
                        edtExpiry.setText(SDF.format(cal.getTime()));
                        ret.getThings().get(0).getProps().put("lot_expire", SQL_FORMAT.format(cal.getTime()));
                        for (AGLThing ats : ret.getThings().get(0).getSiblings()) {
                            ats.getProps().put("lot_expire", SQL_FORMAT.format(cal.getTime()));
                        }
                    } else {
                        runOnUiThread(() -> Toast.makeText(DocumentActivity.this, "SHELFLIFE DO PRODUTO INVÁLIDO", Toast.LENGTH_LONG));
                        disableUI();
                        apoChooser();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                for (AGLThing ats : ret.getThings().get(0).getSiblings()) {
                    ats.setProduct_id(null);
                    ats.setName(null);
                }
            }
        });
        emptyAddress.setId(UUID.randomUUID());
        emptyAddress.setName(getString(R.string.select_address));

        for (Address a : addressList) {
            switch (line) {
                case 3:
                    if (!a.getName().startsWith("LATA"))
                        addressList.remove(a);
                    break;
                case 4:
                    if (!a.getName().startsWith("RGB"))
                        addressList.remove(a);
                    break;
                case 5:
                    if (!a.getName().startsWith("PET"))
                        addressList.remove(a);
                    break;
            }
        }
        for (Address a : addressList) {
            for (AddressField f : a.getFields()) {
                if (f != null && f.getValue() != null) {
                    Timber.d("Field %s = %s", f.getField().getMetaname(), f.getValue());
                }
            }
        }
        addressList.add(0, emptyAddress);
        cbxAddress.setVisibility(View.VISIBLE);
        cbxAddress.setTitle(getString(R.string.select_address));
        cbxAddress.setAdapter(new AddressAdapter(this, R.layout.item_address, addressList));
        cbxAddress.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                ret.getThings().get(0).setAddress_id(cbxAddress.getSelectedItemPosition() > 0 ? ((Address) cbxAddress.getSelectedItem()).getId().toString() : "");
                for (AGLThing ats : ret.getThings().get(0).getSiblings())
                    ats.setAddress_id(cbxAddress.getSelectedItemPosition() > 0 ? ((Address) cbxAddress.getSelectedItem()).getId().toString() : "");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        if (addressList.size() == 2) {//Only 1 choice
            cbxAddress.setSelection(1);
        }

        edtManufacture.setOnClickListener(v1 -> {
            final Calendar cal = Calendar.getInstance();
            int day = cal.get(Calendar.DAY_OF_MONTH);
            int month = cal.get(Calendar.MONTH);
            int year = cal.get(Calendar.YEAR);

            // date picker dialog
            dPicker = new DatePickerDialog(this, (view, year1, monthOfYear, dayOfMonth) -> {
                final String base = getString(R.string.normal_date, dayOfMonth, (monthOfYear + 1), year1);

                edtManufacture.setText(base);
                try {
                    ret.getThings().get(0).getProps().put("manufacturing_batch", SQL_FORMAT.format(Objects.requireNonNull(SDF.parse(base))));
                    for (AGLThing ats : ret.getThings().get(0).getSiblings())
                        ats.getProps().put("manufacturing_batch", SQL_FORMAT.format(Objects.requireNonNull(SDF.parse(base))));
                } catch (ParseException ignored) {
                }
            },
                    year, month, day);
            dPicker.getDatePicker().setMaxDate(Calendar.getInstance().getTimeInMillis());
            dPicker.show();
        });

        edtExpiry.setOnClickListener(v1 -> {
            final Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MONTH, 6);
            int day = cal.get(Calendar.DAY_OF_MONTH);
            int month = cal.get(Calendar.MONTH);
            int year = cal.get(Calendar.YEAR);

            // date picker dialog
            dPicker = new DatePickerDialog(this, (view, year1, monthOfYear, dayOfMonth) -> {
                final String base = getString(R.string.normal_date, dayOfMonth, (monthOfYear + 1), year1);

                edtExpiry.setText(base);
                try {
                    ret.getThings().get(0).getProps().put("lot_expire", SQL_FORMAT.format(Objects.requireNonNull(SDF.parse(base))));
                    for (AGLThing ats : ret.getThings().get(0).getSiblings())
                        ats.getProps().put("lot_expire", SQL_FORMAT.format(Objects.requireNonNull(SDF.parse(base))));
                } catch (ParseException ignored) {
                }
            },
                    year, month, day);
            dPicker.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis());
            dPicker.show();
        });

        edtLot.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ret.getThings().get(0).getProps().put("lot_id", s.toString());
                for (AGLThing ats : ret.getThings().get(0).getSiblings())
                    ats.getProps().put("lot_id", s.toString());
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
            }
        });

        edtQuantity.setVisibility(View.VISIBLE);
        edtQuantity.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ret.getThings().get(0).getProps().put("quantity", s.toString());
                for (AGLThing ats : ret.getThings().get(0).getSiblings())
                    ats.getProps().put("quantity", s.toString());
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
            }
        });
        for (AGLThing at : ret.getThings()) {
            at.setStatus("BLOQUEADO");
            for (AGLThing ats : at.getSiblings()) {
                ats.setStatus("BLOQUEADO");
            }
        }

        chbIncomplete.setVisibility(View.VISIBLE);
        chbIncomplete.setOnCheckedChangeListener((buttonView, isChecked) -> {
            edtQuantity.setEnabled(isChecked);
            if (!isChecked) {
                ProductField palletBox = ProductUtil.getPalletBox(activeProduct);

                if (palletBox != null)
                    edtQuantity.setText(palletBox.getValue());
            }
        });
        setTitle(getString(R.string.menu_MOBILEAPO, ""));
        apoBox.setVisibility(View.VISIBLE);
    }

    private void updateProduct() {
        SearchableSpinner cbxProduct = apoBox.findViewById(R.id.cbxProduct);
        SearchableSpinner cbxAddress = apoBox.findViewById(R.id.cbxAddress);
        int sel = cbxAddress.getSelectedItemPosition();

        cbxProduct.setSelection(0);
        cbxAddress.setSelection(0);
        prepareAPO(selectedLine, null);
        for (int i = 0; i < cbxProduct.getCount(); i++) {
            Product it = (Product) cbxProduct.getAdapter().getItem(i);

            if (it.getId().equals(activeProduct.getId())) {
                cbxProduct.setSelection(i);
                cbxAddress.setSelection(sel);
                break;
            }
        }
    }

    private static class AsyncSendDocument extends AsyncTask<Integer, Void, IntegrationReturn> {
        private boolean problem = false;
        private ProgressDialog progressDialog;
        private final WeakReference<DocumentActivity> activityReference;

        private AsyncSendDocument(DocumentActivity ctx) {
            activityReference = new WeakReference<>(ctx);
        }

        @Override
        protected void onPreExecute() {
            DocumentActivity act = activityReference.get();

            progressDialog = new ProgressDialog(act);
            // Set horizontal animation_progress bar style.
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            // Set animation_progress dialog icon.
            progressDialog.setIcon(R.drawable.image_logo_gtp);
            // Set animation_progress dialog title.
            progressDialog.setTitle(act.getString(R.string.synchronizing));
            // Whether animation_progress dialog can be canceled or not.
            progressDialog.setCancelable(false);
            // When user touch area outside animation_progress dialog whether the animation_progress dialog will be canceled or not.
            progressDialog.setCanceledOnTouchOutside(false);
            // Set animation_progress dialog message.
            progressDialog.setMessage(act.getString(R.string.wait_sync));
            // Popup the animation_progress dialog.
            progressDialog.show();
        }

        @Override
        protected IntegrationReturn doInBackground(Integer... params) {
            DocumentActivity act = activityReference.get();
            Call<IntegrationReturn> call = act.cdClient.postDocument(act.ret);

            act.hndlr.postDelayed(() -> act.mniBlock.setEnabled(true), RESEND_DELAY);
            try {
                Response<IntegrationReturn> resp = call.execute();

                if (resp.isSuccessful())
                    return resp.body();
                else
                    return new IntegrationReturn(false, act.getString(R.string.internal_server_error));
            } catch (IOException ioe) {
                problem = true;
                return new IntegrationReturn(false, ioe.getLocalizedMessage());
            }
        }

        @Override
        protected void onPostExecute(IntegrationReturn ret) {
            super.onPostExecute(ret);
            DocumentActivity act = activityReference.get();

            progressDialog.dismiss();
            if (ret.getResult()) {
                if (act.txtPalletCount != null)
                    ++act.palletCount;

                new AlertDialog.Builder(act)
                        .setTitle(act.getString(R.string.success))
                        .setMessage(act.getString(R.string.msg_document_sent))
                        .setOnDismissListener((dialog) -> {
                            act.createBaseDocument(act.ret.getMetaname(), act.ret.getParent_id());
                            act.clearForms();
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setNeutralButton(android.R.string.ok, (dialog, whichButton) -> dialog.dismiss())
                        .create()
                        .show();
            } else {
                new AlertDialog.Builder(act)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setNeutralButton(android.R.string.ok, (dialog, whichButton) -> {
                            dialog.dismiss();
                            if (problem) {
                                Intent intent = new Intent(act, LoginActivity.class);

                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                act.startActivity(intent);
                            }
                        }).setTitle(act.getString(R.string.fail)).setMessage(ret.getMessage())
                        .create()
                        .show();
            }
        }
    }

    private static class AsyncGetDocument extends AsyncTask<Integer, Void, Document> {
        private final String line;
        private ProgressDialog progressDialog;
        private final WeakReference<DocumentActivity> activityReference;

        private AsyncGetDocument(DocumentActivity ctx, String line) {
            activityReference = new WeakReference<>(ctx);
            this.line = line;
        }

        @Override
        protected void onPreExecute() {
            DocumentActivity act = activityReference.get();

            progressDialog = new ProgressDialog(act);
            // Set horizontal animation_progress bar style.
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            // Set animation_progress dialog icon.
            progressDialog.setIcon(R.drawable.image_logo_gtp);
            // Set animation_progress dialog title.
            progressDialog.setTitle(act.getString(R.string.synchronizing));
            // Whether animation_progress dialog can be canceled or not.
            progressDialog.setCancelable(false);
            // When user touch area outside animation_progress dialog whether the animation_progress dialog will be canceled or not.
            progressDialog.setCanceledOnTouchOutside(false);
            // Set animation_progress dialog message.
            progressDialog.setMessage(act.getString(R.string.wait_sync));
            // Popup the animation_progress dialog.
            progressDialog.show();
        }

        @Override
        protected Document doInBackground(Integer... params) {
            DocumentActivity act = activityReference.get();
            Document d = new Document();
            Call<Document> call = act.dcClient.getRunningPO(this.line);

            try {
                d = call.execute().body();
            } catch (IOException e) {
                d.setStatus(e.getLocalizedMessage());
            }
            return d;
        }

        @Override
        protected void onPostExecute(Document ret) {
            DocumentActivity act = activityReference.get();

            if (ret == null) {
                act.showError(act.getString(R.string.fail), act.getString(R.string.production_line_stopped, line));
            } else if (ret.getId() == null) {
                act.showError(act.getString(R.string.fail), ret.getStatus());
            } else {
                TextView txtPalletCount = act.apoBox.findViewById(R.id.txtPalletCount);

                act.palletCount = 0;
                act.prepareAPO(line, ret.getCode());
                for (DocumentField df : ret.getFields())
                    if (df.getField().getMetaname().equals("PALLET_COUNT") && !df.getValue().isEmpty()) {
                        act.palletCount += Integer.parseInt(df.getValue());
                        break;
                    }

                act.refreshCounter();
                txtPalletCount.setVisibility(View.VISIBLE);
                act.ret.setParent_id(ret.getId().toString());
                for (DocumentItem di : ret.getItems()) {
                    if (di.getProps().containsKey("PRODUCAO") && "PRODUCAO".equals(di.getProps().get("PRODUCAO"))) {
                        act.activeProduct = di.getProduct();
                        act.updateProduct();
                        break;
                    }
                }
            }
            progressDialog.dismiss();
            super.onPostExecute(ret);
        }
    }

    private void showError(String title, String message) {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(title).setMessage(message)
                .setNeutralButton(android.R.string.ok, (dialog, whichButton) -> dialog.dismiss())
                .create()
                .show();
    }
}
