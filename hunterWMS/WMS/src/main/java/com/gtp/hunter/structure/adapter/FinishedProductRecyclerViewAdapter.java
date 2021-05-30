package com.gtp.hunter.structure.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.gtp.hunter.BuildConfig;
import com.gtp.hunter.R;
import com.gtp.hunter.wms.interfaces.ActionFragmentListener;
import com.gtp.hunter.wms.interfaces.FinishedProductListener;
import com.gtp.hunter.wms.model.ViewFinishedProductStub;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import timber.log.Timber;

/**
 * {@link RecyclerView.Adapter} that can display a {@link ViewFinishedProductStub} and makes a call to the
 * specified {@link ActionFragmentListener}.
 */
public class FinishedProductRecyclerViewAdapter extends RecyclerView.Adapter<FinishedProductRecyclerViewAdapter.FinishedProductHolder> {

    private final FinishedProductListener mListener;

    public FinishedProductRecyclerViewAdapter(FinishedProductListener listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public FinishedProductHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fp_inv, parent, false);

        return new FinishedProductHolder(view);
    }

    @Override
    public void onBindViewHolder(FinishedProductHolder holder, int position) {
        holder.fillItem(mListener.getItemAt(position));
    }

    @Override
    public int getItemCount() {
        return mListener.getItemCount();
    }


    public class FinishedProductHolder extends RecyclerView.ViewHolder {
        private final TextView txtAddressName, lblAddressName, txtProduct, lblPalletCount, txtPalletCount, txtBoxCount;
        private final MaterialButton btnAction;

        FinishedProductHolder(View itemView) {
            super(itemView);
            //get the inflater and inflate the XML layout for each item
            lblAddressName = itemView.findViewById(R.id.lblAddressName);
            txtAddressName = itemView.findViewById(R.id.txtAddressName);
            txtProduct = itemView.findViewById(R.id.txtProduct);
            lblPalletCount = itemView.findViewById(R.id.lblPalletCount);
            txtPalletCount = itemView.findViewById(R.id.txtPalletCount);
            txtBoxCount = itemView.findViewById(R.id.txtBoxCount);
            btnAction = itemView.findViewById(R.id.btnAction);
        }

        void fillItem(ViewFinishedProductStub fpItem) {
            DecimalFormat df = new DecimalFormat("0.0000",DecimalFormatSymbols.getInstance(Locale.US));
            Drawable bg = ContextCompat.getDrawable(itemView.getContext(), fpItem.isError() ? R.drawable.background_item_task_nok : R.drawable.background_item_task);

            itemView.setBackground(bg);
            lblPalletCount.setVisibility(View.VISIBLE);
            txtPalletCount.setVisibility(View.VISIBLE);
            lblAddressName.setVisibility(fpItem.getAddress() != null ? View.VISIBLE : View.GONE);
            txtAddressName.setVisibility(fpItem.getAddress() != null ? View.VISIBLE : View.GONE);
            txtAddressName.setText(fpItem.getAddressName());
            txtProduct.setText(fpItem.getProductName());
            txtPalletCount.setText(String.valueOf(fpItem.getPallets()));
            txtBoxCount.setText(df.format(fpItem.getBoxes()));
            btnAction.setOnClickListener(v -> {
                final Context ctx = v.getContext();
                Log.d("Clicked", String.format("Clicked %d Button %d view", btnAction.getId(), v.getId()));
                new MaterialAlertDialogBuilder(ctx)
                        .setMessage(ctx.getString(R.string.question_confirm_remove))
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                            int posRemoved = mListener.removeStub(fpItem);

                            notifyItemRemoved(posRemoved);
                        })
                        .setNegativeButton(android.R.string.no, null).create().show();
            });
            if (BuildConfig.DEBUG) {
                Timber.i("Fill Item product %s - %s with error? %s", fpItem.getProduct().getSku(), fpItem.getProduct().getName(), Boolean.toString(fpItem.isError()));
                Log.d("Fill Item", String.format("Fill Item product %s - %s with error? %s", fpItem.getProduct().getSku(), fpItem.getProduct().getName(), fpItem.isError()));
            }
        }

        @Override
        @NonNull
        public String toString() {
            return super.toString() + " '" + txtAddressName.getText() + " - " + txtProduct.getText() + " = " + txtBoxCount.getText() + "'";
        }
    }
}
