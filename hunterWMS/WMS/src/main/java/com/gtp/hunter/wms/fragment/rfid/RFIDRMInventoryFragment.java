package com.gtp.hunter.wms.fragment.rfid;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.gtp.hunter.R;
import com.gtp.hunter.util.ProductUtil;
import com.gtp.hunter.util.ThingUtil;
import com.gtp.hunter.wms.model.BaseField;
import com.gtp.hunter.wms.model.Document;
import com.gtp.hunter.wms.model.DocumentItem;
import com.gtp.hunter.wms.model.Product;
import com.gtp.hunter.wms.model.Thing;

import java.text.DecimalFormat;
import java.util.Objects;

import timber.log.Timber;

public class RFIDRMInventoryFragment extends BaseRFIDFragment {

    private static final DecimalFormat DF = new DecimalFormat("0.0000");

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public RFIDRMInventoryFragment() {
        Timber.d("Constructor RMInventory Called!");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.basePrepareOptionsMenu(menu);
        mSepSendMenuItem.setVisible(false);
        mRmiSendMenuItem.setVisible(true);
        super.onPrepareOptionsMenu(menu);
    }

    public static RFIDRMInventoryFragment newInstance(Document doc) {
        RFIDRMInventoryFragment frag = new RFIDRMInventoryFragment();
        Bundle args = new Bundle();

        args.putSerializable("DOCUMENT", doc);
        frag.setArguments(args);
        return frag;
    }

    @Override
    protected boolean checkThing(Thing t) {
        boolean notFound = true;

        for (View diView : mViewModel.getDiViewList()) {
            TextView txtSku = diView.findViewById(R.id.txtDIProductSku);
            TextView txtQty = diView.findViewById(R.id.txtDIProductQty);

            if (t.getProduct().getSku().equals(txtSku.getText().toString())) {
                BaseField bf = ThingUtil.getQuantityField(t);
                double qty = Double.parseDouble(txtQty.getText().toString().replace(",", ".")) + Double.parseDouble(bf.getValue().replace(",", "."));

                for (Document ds : mViewModel.getDocument().getSiblings()) {
                    if (ds.getModel().getMetaname().equals("APORUAINV")) {
                        for (DocumentItem di : mViewModel.getDocument().getItems()) {
                            if (di.getProduct().getId().equals(t.getProduct().getId())) {
                                di.setQty(qty);
                                break;
                            }
                        }
                    }
                }
                Objects.requireNonNull(getActivity()).runOnUiThread(() -> txtQty.setText(DF.format(qty)));
                notFound = false;
                break;
            }
        }
        if (notFound) {
            LinearLayout diLayout = Objects.requireNonNull(getActivity()).findViewById(R.id.diLayoutInventory);
            View diView = getActivity().getLayoutInflater().inflate(R.layout.item_documentitem_simple, null);
            TextView txtSku = diView.findViewById(R.id.txtDIProductSku);
            TextView txtName = diView.findViewById(R.id.txtDIProductName);
            TextView txtQty = diView.findViewById(R.id.txtDIProductQty);
            TextView txtUM = diView.findViewById(R.id.txtDIProuctUnitMeasure);
            Product p = t.getProduct();
            BaseField bf = ThingUtil.getQuantityField(t);
            BaseField bfUm = ProductUtil.getMeasureUnit(p);
            DocumentItem di = new DocumentItem();

            di.setProduct(p);
            di.setQty(Double.parseDouble(bf.getValue().replace(",", ".")));
            di.setMeasureUnit(bfUm.getValue());
            diView.setLayoutParams(new ConstraintLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            getActivity().runOnUiThread(() -> {
                txtSku.setText(p.getSku());
                txtName.setText(p.getName());
                txtQty.setText(DF.format(di.getQty()));
                txtUM.setText(bfUm.getValue());
            });
            for (Document ds : mViewModel.getDocument().getSiblings()) {
                if (ds.getModel().getMetaname().equals("APORUAINV")) {
                    ds.getItems().add(di);
                    break;
                }
            }
            diLayout.addView(diView);
            mViewModel.getDiViewList().add(diView);
            if (!mViewModel.getDocument().getStatus().equals("SUCESSO"))
                mViewModel.getDocument().setStatus("SUCESSO");
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
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_rfid_rm_inventory;
    }
}
