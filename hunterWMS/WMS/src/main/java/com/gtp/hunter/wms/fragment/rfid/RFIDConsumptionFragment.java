package com.gtp.hunter.wms.fragment.rfid;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.gtp.hunter.R;
import com.gtp.hunter.wms.model.Document;
import com.gtp.hunter.wms.model.DocumentItem;
import com.gtp.hunter.wms.model.Product;
import com.gtp.hunter.wms.model.Thing;

import java.text.DecimalFormat;
import java.util.Objects;

import timber.log.Timber;

public class RFIDConsumptionFragment extends BaseRFIDFragment {

    private static final DecimalFormat DF = new DecimalFormat("0.0000");

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public RFIDConsumptionFragment() {
        Timber.e("Constructor Consumption Called");
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        super.basePrepareOptionsMenu(menu);
        mSepSendMenuItem.setEnabled(false);
        mSepSaveMenuItem.setVisible(false);
    }

    public static RFIDConsumptionFragment newInstance(Document doc) {
        RFIDConsumptionFragment frag = new RFIDConsumptionFragment();
        Bundle args = new Bundle();

        args.putSerializable("DOCUMENT", doc);
        frag.setArguments(args);
        return frag;
    }

    @Override
    protected boolean checkThing(Thing t) {
        boolean productNotFound = true;

        for (DocumentItem di : mViewModel.getDocument().getItems()) {
            if (di.getProduct().getId().equals(t.getProduct().getId())) {
                productNotFound = false;
                if (!mSepSendMenuItem.isEnabled())
                    mSepSendMenuItem.setEnabled(true);
            }
        }

        if (productNotFound) {
            showAlert("Produto Inválido", "Este produto não está na lista de consumo");
            t.setError(true);
        }
        return !t.isError();
    }

    @Override
    public void interact(String msg) {
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> Toast.makeText(getContext(), msg, Toast.LENGTH_LONG));
    }

    @Override
    public void transform(Document d) {
        mViewModel.setDocument(d);
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            LayoutInflater vi = (LayoutInflater) getActivity().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            LinearLayout diLayout = getActivity().findViewById(R.id.diLayoutConsumption);

            diLayout.removeAllViews();
            for (DocumentItem di : d.getItems()) {
                if (di.getProps().containsKey("PRODUCAO") && Objects.equals(di.getProps().get("PRODUCAO"), "PRODUCAO"))
                    continue;
                View diView = vi.inflate(R.layout.item_documentitem_simple, diLayout, false);
                TextView txtSku = diView.findViewById(R.id.txtDIProductSku);
                TextView txtName = diView.findViewById(R.id.txtDIProductName);
                TextView txtQty = diView.findViewById(R.id.txtDIProductQty);
                TextView txtMeasureUnit = diView.findViewById(R.id.txtDIProuctUnitMeasure);
                Product p = di.getProduct();

                if (p != null) {
                    txtSku.setText(p.getSku());
                    txtName.setText(p.getName().length() > 25 ? p.getName().substring(0, 25) : p.getName());
                    txtQty.setText(DF.format(di.getQty()));
                    txtMeasureUnit.setText(di.getMeasureUnit());
                }
                diLayout.addView(diView);
                mViewModel.getDiViewList().add(diView);
            }
            this.txtDoc.setText(d.getCode());
        });
    }

    protected int getLayoutId() {
        return R.layout.fragment_rfid_consumption;
    }
}
