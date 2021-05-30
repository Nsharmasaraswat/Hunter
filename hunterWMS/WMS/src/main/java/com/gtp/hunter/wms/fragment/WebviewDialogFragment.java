package com.gtp.hunter.wms.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.gtp.hunter.R;
import com.gtp.hunter.wms.interfaces.ActionFragmentListener;
import com.gtp.hunter.wms.model.Document;

public class WebviewDialogFragment extends DialogFragment {

    private ActionFragmentListener mActionListener;

    public static WebviewDialogFragment newInstance(Document doc) {
        WebviewDialogFragment dialog = new WebviewDialogFragment();
        Bundle args = new Bundle();

        args.putSerializable("DOCUMENT", doc);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.dialog_webview, container, false);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
    }
}
