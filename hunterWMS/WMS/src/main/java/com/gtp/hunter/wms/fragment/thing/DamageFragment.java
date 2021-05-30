package com.gtp.hunter.wms.fragment.thing;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gtp.hunter.HunterMobileWMS;
import com.gtp.hunter.R;
import com.gtp.hunter.structure.adapter.AddressAdapter;
import com.gtp.hunter.structure.spinner.SearchableSpinner;
import com.gtp.hunter.wms.activity.thing.ThingOperationActivity;
import com.gtp.hunter.wms.client.CustomDocumentClient;
import com.gtp.hunter.wms.interfaces.TaskFragmentListener;
import com.gtp.hunter.wms.interfaces.ThingOperationListener;
import com.gtp.hunter.wms.model.AGLDocument;
import com.gtp.hunter.wms.model.AGLThing;
import com.gtp.hunter.wms.model.AGLTransport;
import com.gtp.hunter.wms.model.Address;
import com.gtp.hunter.wms.model.BaseField;
import com.gtp.hunter.wms.model.IntegrationReturn;
import com.gtp.hunter.wms.model.SpinnerDisplayName;
import com.gtp.hunter.wms.model.Thing;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import retrofit2.Call;
import retrofit2.Response;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link TaskFragmentListener}
 * interface.
 */
public class DamageFragment extends Fragment {

    private final SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss", Locale.US);
    private final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.US);

    private SearchableSpinner cbxAddress;
    private Thing thing;
    private ThingOperationListener mListener;
    private View fragmentView;

    private CustomDocumentClient cdClient;

    private final AdapterView.OnItemSelectedListener addressChangedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (cbxAddress.getTag() != null && (int) cbxAddress.getTag() != position) {
                if (cbxAddress.getSelectedItemPosition() > 0) {
                    new AlertDialog.Builder(getActivity())
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle(Objects.requireNonNull(getActivity()).getString(R.string.send))
                            .setMessage(Objects.requireNonNull(getActivity()).getString(R.string.question_send_to_hunter))
                            .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                                Address destination = (Address) cbxAddress.getSelectedItem();
                                List<AGLThing> tList = new ArrayList<>();
                                List<AGLTransport> trList = new ArrayList<>();
                                AGLTransport tr = new AGLTransport();
                                AGLDocument doc = new AGLDocument();

                                tr.setSeq(1);
                                tr.setThing_id(thing.getId().toString());
                                tr.setOrigin_id(thing.getAddress().getId().toString());
                                tr.setAddress_id(destination.getId().toString());
                                tr.setParent_id(destination.getParent_id().toString());
                                tList.add(thing.getAGLThing());
                                trList.add(tr);
                                doc.setId(UUID.randomUUID().toString());
                                doc.setMetaname("ORDMOV");
                                doc.setStatus("WMS_ATIVO");
                                doc.setThings(tList);
                                doc.setUser_id(HunterMobileWMS.getUser().getId().toString());
                                doc.setTransports(trList);
                                new AsyncCreateTransport(DamageFragment.this, doc).execute();
                            })
                            .setNegativeButton(android.R.string.no, (dialog, whichButton) -> dialog.dismiss())
                            .create()
                            .show();
                } else {
                    showError(getString(R.string.invalid_destination), getString(R.string.select_address), true);
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DamageFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manual_transport, container, false);
        SearchableSpinner warehouseTypeSpinner = view.findViewById(R.id.cbxAddressTypes);
        String[] stringArray = getResources().getStringArray(R.array.warehouse_types);
        List<SpinnerDisplayName> warehouseTypes = new ArrayList<>();

        warehouseTypes.add(new SpinnerDisplayName("", getString(R.string.select_type)));
        for (String entry : stringArray) {
            String[] splitResult = entry.split("\\|", 2);
            int resourceId = getResources().getIdentifier(splitResult[1], "string", Objects.requireNonNull(getActivity()).getPackageName());

            warehouseTypes.add(new SpinnerDisplayName(splitResult[0], getString(resourceId != 0 ? resourceId : R.string.not_registered)));
        }
        ArrayAdapter<SpinnerDisplayName> adapter = new ArrayAdapter<>(Objects.requireNonNull(getActivity()), android.R.layout.simple_spinner_dropdown_item, warehouseTypes);

        cbxAddress = view.findViewById(R.id.cbxMtrAddress);
        warehouseTypeSpinner.setAdapter(adapter);
        warehouseTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

                if (warehouseTypeSpinner.getSelectedItemPosition() > 0) {
                    SpinnerDisplayName type = (SpinnerDisplayName) warehouseTypeSpinner.getSelectedItem();
                    CopyOnWriteArrayList<Address> addressList = new CopyOnWriteArrayList<>(HunterMobileWMS.getBottomAddressListTopType(type.getName()));
                    AddressAdapter addrAdapter = new AddressAdapter(Objects.requireNonNull(getActivity()), R.layout.item_address, addressList);

                    cbxAddress.setTitle(getString(R.string.select_address));
                    cbxAddress.setAdapter(addrAdapter.init());
                    cbxAddress.setVisibility(View.VISIBLE);
                    cbxAddress.setOnItemSelectedListener(addressChangedListener);
                    cbxAddress.setTag(0);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        cdClient = new CustomDocumentClient(getActivity().getBaseContext());
        fragmentView = view;
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ThingOperationActivity) {
            mListener = (ThingOperationListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement ThingOperationListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setThing(Thing t) {
        final Address a = t.getAddress();
        this.thing = t;
        Thing productThing = null;
        TextView txtMtrProduct = fragmentView.findViewById(R.id.txtThingProduct);
        TextView txtMtrManufacture = fragmentView.findViewById(R.id.txtThingManufacture);
        TextView txtMtrExpiry = fragmentView.findViewById(R.id.txtThingExpiry);
        TextView txtMtrQuantity = fragmentView.findViewById(R.id.txtThingQuantity);
        TextView txtMtrAddress = fragmentView.findViewById(R.id.txtThingAddress);
        for (Thing ts : t.getSiblings()) {
            if (ts != null) {
                productThing = ts;
                break;
            }
        }
        Set<BaseField> prSet = productThing.getProperties();
        Date manufacture = null;
        Date expire = null;
        double quantity = 0;

        for (BaseField f : prSet) {
            switch (f.getField().getMetaname()) {
                case "QUANTITY":
                    quantity = Double.parseDouble(f.getValue());
                    break;
                case "MANUFACTURING_BATCH":
                    try {
                        manufacture = parser.parse(f.getValue());
                    } catch (ParseException pe) {
                        Log.e("Manufacture", " Error on date: " + f.getValue());
                    }
                    break;
                case "LOT_EXPIRE":
                    try {
                        expire = parser.parse(f.getValue());
                    } catch (ParseException pe) {
                        Log.e("Expiry", " Error on date: " + f.getValue());
                    }
                    break;
            }
        }

        txtMtrProduct.setText(Objects.requireNonNull(getActivity()).getString(R.string.dyn_string_pair, productThing.getProduct().getSku(), productThing.getProduct().getName()));
        txtMtrManufacture.setText(Objects.requireNonNull(getActivity()).getString(R.string.dyn_manufacture, formatter.format(manufacture)));
        txtMtrExpiry.setText(Objects.requireNonNull(getActivity()).getString(R.string.dyn_expiry, formatter.format(expire)));
        txtMtrQuantity.setText(Objects.requireNonNull(getActivity()).getString(R.string.dyn_qty, quantity));
        txtMtrAddress.setText(Objects.requireNonNull(getActivity()).getString(R.string.dyn_address, a.getName()));
        txtMtrProduct.setVisibility(View.VISIBLE);
    }

    private static class AsyncCreateTransport extends AsyncTask<Integer, Void, IntegrationReturn> {
        private ProgressDialog progressDialog;
        private final WeakReference<DamageFragment> activityReference;
        private final AGLDocument doc;

        private AsyncCreateTransport(DamageFragment ctx, AGLDocument doc) {
            activityReference = new WeakReference<>(ctx);
            this.doc = doc;
        }

        @Override
        protected void onPreExecute() {
            DamageFragment act = activityReference.get();

            progressDialog = new ProgressDialog(act.getActivity());
            // Set horizontal animation_progress bar style.
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            // Set animation_progress dialog icon.
            progressDialog.setIcon(R.drawable.image_logo_gtp);
            // Set animation_progress dialog title.
            progressDialog.setTitle(act.getString(R.string.synchronizing));
            // Whether animation_progress dialog can be canceled or not.
            progressDialog.setCancelable(true);
            // When user touch area outside animation_progress dialog whether the animation_progress dialog will be canceled or not.
            progressDialog.setCanceledOnTouchOutside(false);
            // Set animation_progress dialog message.
            progressDialog.setMessage(act.getString(R.string.wait_sync));
            // Popup the animation_progress dialog.
            progressDialog.show();
        }

        @Override
        protected IntegrationReturn doInBackground(Integer... params) {
            DamageFragment act = activityReference.get();
            Call<IntegrationReturn> call = act.cdClient.postDocument(doc);

            try {
                Response<IntegrationReturn> resp = call.execute();

                if (resp.isSuccessful())
                    return resp.body();
                else
                    return new IntegrationReturn(false, act.getString(R.string.internal_server_error));
            } catch (IOException ioe) {
                return new IntegrationReturn(false, ioe.getLocalizedMessage());
            }
        }

        @Override
        protected void onPostExecute(IntegrationReturn ret) {
            DamageFragment act = activityReference.get();
            progressDialog.dismiss();
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(act.getActivity())
                    .setIcon(android.R.drawable.ic_dialog_alert);

            if (ret.getResult()) {
                alertBuilder.setTitle(act.getString(R.string.success)).setMessage(Objects.requireNonNull(act.getActivity()).getString(R.string.msg_document_sent));
                act.mListener.showFilters();
            } else {
                alertBuilder.setTitle(act.getString(R.string.fail)).setMessage(ret.getMessage());
            }
            alertBuilder.create().show();
            super.onPostExecute(ret);
        }
    }

    protected void showError(String title, String message, boolean critical) {
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getContext());

            if (critical)
                alertBuilder.setIcon(android.R.drawable.ic_dialog_alert);
            else
                alertBuilder.setIcon(android.R.drawable.ic_dialog_info);
            alertBuilder.setCancelable(true);
            alertBuilder.setTitle(title);
            alertBuilder.setMessage(message);
            alertBuilder.create().show();
        });
    }
}
