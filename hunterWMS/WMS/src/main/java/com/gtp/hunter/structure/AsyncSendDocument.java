package com.gtp.hunter.structure;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.gtp.hunter.R;
import com.gtp.hunter.wms.fragment.BaseFragment;
import com.gtp.hunter.wms.model.Document;
import com.gtp.hunter.wms.model.IntegrationReturn;

import java.lang.ref.WeakReference;

public class AsyncSendDocument<T extends BaseFragment> extends AsyncTask<Integer, Void, IntegrationReturn> {
    private final WeakReference<T> fragRef;
    private ProgressDialog progressDialog;

    public AsyncSendDocument(T f) {
        this.fragRef = new WeakReference<>(f);
    }

    @Override
    protected void onPreExecute() {
        progressDialog = new ProgressDialog(fragRef.get().getActivity());
        // Set horizontal animation_progress bar style.
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        // Set animation_progress dialog icon.
        progressDialog.setIcon(R.drawable.image_logo_gtp);
        // Set animation_progress dialog title.
        progressDialog.setTitle(fragRef.get().getString(R.string.info_sending));
        // Whether animation_progress dialog can be canceled or not.
        progressDialog.setCancelable(false);
        // When user touch area outside animation_progress dialog whether the animation_progress dialog will be canceled or not.
        progressDialog.setCanceledOnTouchOutside(false);
        // Set animation_progress dialog message.
        progressDialog.setMessage(fragRef.get().getString(R.string.question_send_to_hunter));
        // Popup the animation_progress dialog.
        progressDialog.show();
    }

    @Override
    protected IntegrationReturn doInBackground(Integer... params) {
        BaseFragment frag = fragRef.get();
        Document ret = frag.getViewModel().getDocument();

        if (ret != null) {
            return frag.getActionListener().sendDocument(ret);
        }
        return new IntegrationReturn(false, "Documento Inválido");
    }

    @Override
    protected void onPostExecute(IntegrationReturn iRet) {
        BaseFragment frag = fragRef.get();

        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        if (!iRet.getResult()) {
            frag.showError(frag.getString(R.string.connection_failed), iRet.getMessage(), true);
        } else {
            frag.clearViewModel();
            frag.getActionListener().returnFromFragment();
        }
    }
}
