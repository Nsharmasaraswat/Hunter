package com.gtp.hunter.structure.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.gtp.hunter.R;
import com.gtp.hunter.wms.interfaces.ActionFragmentListener;
import com.gtp.hunter.wms.model.BaseField;
import com.gtp.hunter.wms.model.Product;
import com.gtp.hunter.wms.model.Thing;
import com.gtp.hunter.wms.model.ViewThingStub;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import timber.log.Timber;

/**
 * {@link RecyclerView.Adapter} that can display a {@link ViewThingStub} and makes a call to the
 * specified {@link ActionFragmentListener}.
 */
public class ThingRecyclerViewAdapter extends RecyclerView.Adapter<ThingRecyclerViewAdapter.ThingHolder> {

    private final List<ViewThingStub> things;
    private final ActionFragmentListener mListener;
    private Drawable okBackground;
    private Drawable syncedBackground;
    private Drawable nokBackground;

    public ThingRecyclerViewAdapter(List<ViewThingStub> items, ActionFragmentListener listener) {
        things = items;
        mListener = listener;
    }

    @Override
    @NotNull
    public ThingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rfid_read, parent, false);
        nokBackground = ContextCompat.getDrawable(parent.getContext(), R.drawable.background_conf_nok);
        okBackground = ContextCompat.getDrawable(parent.getContext(), R.drawable.background_conf_ok);
        syncedBackground = ContextCompat.getDrawable(parent.getContext(), R.drawable.background_conf_print);
        return new ThingHolder(view);
    }

    @Override
    public void onBindViewHolder(ThingHolder holder, int position) {
        holder.fillItem(this.things.get(position));
    }

    @Override
    public int getItemCount() {
        return things.size();
    }


    class ThingHolder extends RecyclerView.ViewHolder implements View.OnTouchListener, View.OnClickListener {
        private final SimpleDateFormat SQL_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        private final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        private final ConstraintLayout baseLayout;
        private final TextView txtThingProductName;
        private final TextView txtRfidReadLot;
        private final TextView txtRfidReadManufacture;
        private final TextView txtRfidReadExpire;
        private final TextView txtRfidReadSerial;
        private final TextView txtRfidReadQuantity;

        private ViewThingStub stub;

        private final static int CLICK_ACTION_THRESHOLD = 50;
        private final static long LENGTH_ACTION_THRESHOLD = (long) (2 * 10e7);
        private Drawable lastDraw;
        private float startX;
        private float startY;
        private long startTime;

        ThingHolder(View itemView) {
            super(itemView);
            baseLayout = itemView.findViewById(R.id.productReadView);
            txtThingProductName = itemView.findViewById(R.id.txtThingProductName);
            txtRfidReadLot = itemView.findViewById(R.id.txtRfidReadLot);
            txtRfidReadManufacture = itemView.findViewById(R.id.txtRfidReadManufacture);
            txtRfidReadExpire = itemView.findViewById(R.id.txtRfidReadExpire);
            txtRfidReadSerial = itemView.findViewById(R.id.txtRfidReadSerial);
            txtRfidReadQuantity = itemView.findViewById(R.id.txtRfidReadQuantity);
            itemView.setOnTouchListener(this);
            itemView.setOnClickListener(this);
        }

        void fillItem(ViewThingStub tStub) {
            Product p = tStub.getThing().getProduct();
            Set<BaseField> prSet = tStub.getThing().getProperties();
            String lot = "";
            String manufacture = "";
            String expire = "";
            String quantity = "";
            String tag = tStub.getThing().getUnits().size() > 0 ? tStub.getThing().getUnits().iterator().next().getTagId() : "";

            for (BaseField f : prSet) {
                switch (f.getField().getMetaname()) {
                    case "QUANTITY":
                        quantity = f.getValue();
                        break;
                    case "MANUFACTURING_BATCH":
                        try {
                            Date man = SQL_FORMAT.parse(f.getValue());

                            assert man != null;
                            manufacture = txtRfidReadManufacture.getContext().getString(R.string.dyn_manufacture, SDF.format(man));
                        } catch (ParseException e) {
                            manufacture = f.getValue();
                        }
                        break;
                    case "LOT_ID":
                        lot = txtRfidReadLot.getContext().getString(R.string.dyn_lot, f.getValue());
                        break;
                    case "LOT_EXPIRE":
                        try {
                            Date exp = SQL_FORMAT.parse(f.getValue());

                            assert exp != null;
                            expire = txtRfidReadExpire.getContext().getString(R.string.dyn_expiry, SDF.format(exp));
                        } catch (ParseException e) {
                            expire = f.getValue();
                        }
                        break;
                }
            }

            txtThingProductName.setText(p.getName());
            txtRfidReadLot.setText(lot);
            txtRfidReadManufacture.setText(manufacture);
            txtRfidReadExpire.setText(expire);
            txtRfidReadQuantity.setText(quantity);
            txtRfidReadSerial.setText(tag);
            Thing thing = tStub.getThing();
            if (tStub.isSent())
                baseLayout.setBackground(syncedBackground);
            else {
                if (thing.isError())
                    baseLayout.setBackground(nokBackground);
                else
                    baseLayout.setBackground(okBackground);
            }
            this.stub = tStub;
        }

        @NotNull
        @Override
        public String toString() {
            return super.toString() + " '" + txtThingProductName.getText() + "'";
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastDraw = baseLayout.getBackground();
                    baseLayout.setBackground(okBackground);
                    startX = event.getX();
                    startY = event.getY();
                    startTime = SystemClock.elapsedRealtimeNanos();
                    Timber.d("Time down %d", startTime);
                    v.setSelected(true);
                    break;
                case MotionEvent.ACTION_UP:
                    float endX = event.getX();
                    float endY = event.getY();
                    long touchLength = (SystemClock.elapsedRealtimeNanos() - startTime);
                    Timber.d("Downtime %s", event.getDownTime());
                    Timber.d("Time up %s", SystemClock.elapsedRealtimeNanos());
                    Timber.d("Press Time %s", (SystemClock.elapsedRealtimeNanos() - startTime) + " (" + LENGTH_ACTION_THRESHOLD + ")");
                    baseLayout.setBackground(lastDraw);
                    v.setSelected(false);
                    if (isAClick(startX, endX, startY, endY) && touchLength > LENGTH_ACTION_THRESHOLD) {
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

        @Override
        public void onClick(View v) {
            Context ctx = v.getContext();
            MaterialAlertDialogBuilder alert = new MaterialAlertDialogBuilder(ctx)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                        int pos = things.indexOf(stub);
                        things.remove(stub);
                        notifyItemRemoved(pos);
                        dialog.dismiss();
                    })
                    .setNegativeButton(android.R.string.no, (dialog, whichButton) -> dialog.dismiss())
                    .setTitle(ctx.getString(R.string.question_confirm_remove))
                    .setMessage(txtThingProductName.getText());
            alert.show();
        }
    }
}