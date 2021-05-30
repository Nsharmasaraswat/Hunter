package com.gtp.hunter.structure.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.gtp.hunter.R;
import com.gtp.hunter.wms.interfaces.ThingOperationListener;
import com.gtp.hunter.wms.model.Address;
import com.gtp.hunter.wms.model.BaseField;
import com.gtp.hunter.wms.model.Product;
import com.gtp.hunter.wms.model.Thing;
import com.gtp.hunter.wms.model.ViewPalletStub;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * {@link RecyclerView.Adapter} that can display a {@link ViewPalletStub}
 */
public class ThingOperationRecyclerViewAdapter extends RecyclerView.Adapter<ThingOperationRecyclerViewAdapter.PalletHolder> {

    private final List<UUID> sentList = new ArrayList<>();
    private final ThingOperationListener mListener;
    private final List<ViewPalletStub> things;
    private final SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss", Locale.US);
    private final SimpleDateFormat parser2 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US);
    private final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.US);

    public ThingOperationRecyclerViewAdapter(List<ViewPalletStub> items, ThingOperationListener listener) {
        things = items;
        mListener = listener;
    }

    @Override
    @NotNull
    public PalletHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_thing_operation, parent, false);
        return new PalletHolder(view);
    }

    @Override
    public void onBindViewHolder(PalletHolder holder, int position) {
        holder.fillItem(this.things.get(position));
    }

    @Override
    public int getItemCount() {
        return things.size();
    }


    class PalletHolder extends RecyclerView.ViewHolder {
        private ConstraintLayout baseLayout;
        private TextView txtMtrManufacture;
        private TextView txtMtrExpiry;
        private TextView txtMtrQuantity;
        private TextView txtMtrAddress;
        private TextView txtMtrProduct;
        private TextView txtMtrStatus;

        private PalletHolder(View itemView) {
            super(itemView);
            baseLayout = itemView.findViewById(R.id.layoutItemThing);
            txtMtrProduct = itemView.findViewById(R.id.txtThingProduct);
            txtMtrManufacture = itemView.findViewById(R.id.txtThingManufacture);
            txtMtrExpiry = itemView.findViewById(R.id.txtThingExpiry);
            txtMtrQuantity = itemView.findViewById(R.id.txtThingQuantity);
            txtMtrAddress = itemView.findViewById(R.id.txtThingAddress);
            txtMtrStatus = itemView.findViewById(R.id.txtThingStatus);
        }

        void fillItem(ViewPalletStub tStub) {
            final Context ctx = baseLayout.getContext();
            final Drawable sel = ContextCompat.getDrawable(ctx, R.drawable.background_conf_print);
            final Drawable drw = ContextCompat.getDrawable(ctx, R.drawable.background_item_thing);
            final Thing t = tStub.getThing();
            final Address a = t.getAddress();
            Product p = t.getProduct();

            Set<BaseField> prSet = tStub.getThing().getProperties();
            String manufacture = "-";
            String expire = "-";
            double quantity = 0;

            for (Thing ts : t.getSiblings()) {
                p = ts.getProduct();
            }

            for (BaseField f : prSet) {
                switch (f.getField().getMetaname()) {
                    case "QUANTITY":
                        quantity = Double.parseDouble(f.getValue());
                        break;
                    case "MANUFACTURING_BATCH":
                        try {
                            Date man = f.getValue().contains("T") ? parser.parse(f.getValue()) : parser2.parse(f.getValue());

                            manufacture = formatter.format(man);
                        } catch (ParseException pe) {
                            manufacture = f.getValue();
                        }
                        break;
                    case "LOT_EXPIRE":
                        try {
                            Date exp = f.getValue().contains("T") ? parser.parse(f.getValue()) : parser2.parse(f.getValue());

                            expire = formatter.format(exp);
                        } catch (ParseException pe) {
                            expire = f.getValue();
                        }
                        break;
                }
            }
            txtMtrProduct.setText(ctx.getString(R.string.dyn_string_pair, p.getSku(), p.getName()));
            txtMtrManufacture.setText(ctx.getString(R.string.dyn_manufacture, manufacture));
            txtMtrExpiry.setText(ctx.getString(R.string.dyn_expiry, expire));
            txtMtrQuantity.setText(ctx.getString(R.string.dyn_qty, quantity));
            txtMtrAddress.setText(ctx.getString(R.string.dyn_address, a.getName()));
            txtMtrStatus.setText(t.getStatus());
            baseLayout.setOnClickListener(v -> {
                baseLayout.setBackground(sel);
                sentList.add(t.getId());
                mListener.thingSelected(t);
            });
            if (!sentList.contains(t.getId()))
                baseLayout.setBackground(drw);
        }

        @Override
        @NonNull
        public String toString() {
            return super.toString() + " '" + txtMtrAddress.getText() + "(" + txtMtrQuantity.getText() + ")'";
        }
    }
}