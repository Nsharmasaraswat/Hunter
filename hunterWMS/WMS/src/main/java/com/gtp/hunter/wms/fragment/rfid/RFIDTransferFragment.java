package com.gtp.hunter.wms.fragment.rfid;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.gtp.hunter.R;
import com.gtp.hunter.wms.model.BaseField;
import com.gtp.hunter.wms.model.Document;
import com.gtp.hunter.wms.model.DocumentItem;
import com.gtp.hunter.wms.model.Product;
import com.gtp.hunter.wms.model.Thing;

import java.text.DecimalFormat;
import java.util.Objects;

import timber.log.Timber;

public class RFIDTransferFragment extends BaseRFIDFragment {

    private static final DecimalFormat DF = new DecimalFormat("0.0000");

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public RFIDTransferFragment() {
        Timber.e("Constructor Transfer Called!");
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.basePrepareOptionsMenu(menu);
        mSepSaveMenuItem.setEnabled(false);
        mSepSendMenuItem.setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }

    public static RFIDTransferFragment newInstance(Document doc) {
        RFIDTransferFragment frag = new RFIDTransferFragment();
        Bundle args = new Bundle();

        args.putSerializable("DOCUMENT", doc);
        frag.setArguments(args);
        return frag;
    }

    @Override
    protected boolean checkThing(Thing t) {
        boolean productNotFound = true;

        for (View diView : mViewModel.getDiViewList()) {
            TextView txtSku = diView.findViewById(R.id.txtDIProductSku);
            TextView txtQty = diView.findViewById(R.id.txtDIProductQty);
            TextView txtSelQty = diView.findViewById(R.id.txtDISelProductQty);
            TextView txtMissingQty = diView.findViewById(R.id.txtDIMissingProductQty);

            if (t.getProduct().getSku().equals(txtSku.getText().toString())) {
                double qty = Double.parseDouble(txtQty.getText().toString().replace(",", "."));
                double selQty = Double.parseDouble(txtSelQty.getText().toString().replace(",", "."));
                double missingQty = Double.parseDouble(txtMissingQty.getText().toString().replace(",", "."));

                for (BaseField bf : t.getProperties()) {
                    if (bf.getField().getMetaname().equals("QUANTITY")) {
                        if (missingQty > 0) {
                            selQty += Double.parseDouble(bf.getValue().replace(",", "."));
                            missingQty = qty - selQty;
                        } else {
                            showAlert("Quantidade Ultrapassada", "Este produto já foi totalmente separado");
                            t.setError(true);
                            return false;
                        }
                        break;
                    }
                }
                final double finalSelQty = selQty;
                final double finalMissingQty = missingQty;
                Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
                    txtSelQty.setText(DF.format(finalSelQty));
                    txtMissingQty.setText(DF.format(finalMissingQty));
                    if (finalMissingQty < 1) {
                        mSepSaveMenuItem.setEnabled(true);
                        txtMissingQty.setText("0");
                    }
                });

                productNotFound = false;
                break;
            }
        }
        if (productNotFound) {
            showAlert("Produto Inválido", "Este produto não está na lista de separação");
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

            for (DocumentItem di : d.getItems()) {
                View diView = getActivity().findViewById(R.id.trasnfProductView);
                TextView txtSku = diView.findViewById(R.id.txtDIProductSku);
                TextView txtName = diView.findViewById(R.id.txtDIProductName);
                TextView txtQty = diView.findViewById(R.id.txtDIProductQty);
                TextView txtMeasureUnit = diView.findViewById(R.id.txtDIProuctUnitMeasure);
                TextView txtSelQty = diView.findViewById(R.id.txtDISelProductQty);
                TextView txtSelMeasureUnit = diView.findViewById(R.id.txtDISelProuctUnitMeasure);
                TextView txtMissingQty = diView.findViewById(R.id.txtDIMissingProductQty);
                TextView txtMissingMeasureUnit = diView.findViewById(R.id.txtDIMissingProuctUnitMeasure);
                TextView txtLot = diView.findViewById(R.id.txtPickLots);
                Product p = di.getProduct();

                if (p != null) {
                    txtSku.setText(p.getSku());
                    txtName.setText(p.getName());
                    txtQty.setText(DF.format(di.getQty()));
                    txtMeasureUnit.setText(di.getMeasureUnit());
                    txtSelQty.setText(DF.format(0));
                    txtSelMeasureUnit.setText(di.getMeasureUnit());
                    txtMissingQty.setText(DF.format(di.getQty()));
                    txtMissingMeasureUnit.setText(di.getMeasureUnit());
                    if (di.getProps().containsKey("lot-list")) {
                        String lotList = di.getProps().get("lot-list");

                        if (Objects.requireNonNull(lotList).equals("[]"))
                            txtLot.setText("LIVRE");
                        else
                            txtLot.setText(lotList);
                    }
                }
                mViewModel.getDiViewList().add(diView);
            }
            this.txtDoc.setText(d.getCode());
        });
    }

    @Override
    protected int getLayoutId(){
        return R.layout.fragment_rfid_transfer;
    }
}
