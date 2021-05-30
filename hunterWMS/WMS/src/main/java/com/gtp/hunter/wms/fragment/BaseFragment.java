package com.gtp.hunter.wms.fragment;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.gtp.hunter.R;
import com.gtp.hunter.structure.AsyncSendDocument;
import com.gtp.hunter.structure.viewmodel.BaseDocumentViewModel;
import com.gtp.hunter.wms.interfaces.ActionFragmentListener;

import java.util.Objects;


public abstract class BaseFragment extends Fragment {

    private ActionFragmentListener mActionListener;
    private AlertDialog alertDialog;

    @Override
    public void onAttach(Context ctx) {
        super.onAttach(ctx);
        if (ctx instanceof ActionFragmentListener)
            mActionListener = (ActionFragmentListener) ctx;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mActionListener != null)
            mActionListener = null;
    }

    @Override
    public void onStop() {
        super.onStop();

        if (alertDialog != null)
            alertDialog.dismiss();
        alertDialog = null;
    }

    public abstract BaseDocumentViewModel getViewModel();

    public ActionFragmentListener getActionListener() {
        return mActionListener;
    }

    public abstract void clearViewModel();

    public void completeTask() {
        new MaterialAlertDialogBuilder(Objects.requireNonNull(getActivity()))
                .setTitle(getString(R.string.task_completed))
                .setMessage(getString(R.string.question_send_to_hunter))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> Objects.requireNonNull(getActivity()).runOnUiThread(() -> new AsyncSendDocument<>(this).execute()))
                .setNegativeButton(android.R.string.no, null)
                .create()
                .show();
    }

    public void showError(String title, String message, boolean critical) {
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            if (alertDialog != null && alertDialog.isShowing()) alertDialog.dismiss();
            MaterialAlertDialogBuilder alertBuilder = new MaterialAlertDialogBuilder(Objects.requireNonNull(getContext()));

            if (critical)
                alertBuilder.setIcon(android.R.drawable.ic_dialog_alert);
            else
                alertBuilder.setIcon(android.R.drawable.ic_dialog_info);
            alertBuilder.setCancelable(true)
                    .setTitle(title)
                    .setMessage(message);
            alertDialog = alertBuilder.create();
            alertDialog.show();
        });
    }
}
