package com.gtp.hunter.structure;

import android.text.Editable;
import android.text.TextWatcher;

import com.gtp.hunter.structure.viewmodel.AGLCheckingViewModel;
import com.gtp.hunter.wms.model.CheckingItem;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class CheckingTextWatcher implements TextWatcher {
    private int viewId;
    private CheckingField field;
    private AGLCheckingViewModel mViewModel;
    private ScheduledFuture<?> textChanged;

    public CheckingTextWatcher(AGLCheckingViewModel confModel, CheckingField field, int confView) {
        this.mViewModel = confModel;
        this.viewId = confView;
        this.field = field;
    }

    public void afterTextChanged(Editable s) {
        if (textChanged != null && !textChanged.isCancelled() && !textChanged.isDone()) {
            textChanged.cancel(true);
            textChanged = null;
        }
        if (s.length() > 0) {
            textChanged = Executors.newSingleThreadScheduledExecutor().schedule(() ->
            {
                CheckingItem tmpConf = mViewModel.getCheckingByViewId(viewId);

                switch (field) {
                    case LOT_ID:
                        tmpConf.setLot_id(s.toString());
                        break;
                    case LOT_MANUFACTURE:
                        tmpConf.setLot_mfg(s.toString());
                        break;
                    case LOT_EXPIRE:
                        tmpConf.setLot_exp(s.toString());
                        break;
                    case QUANTITY:
                        tmpConf.setQuantity(Double.parseDouble(s.toString()));
                        break;
                }

            }, 400, TimeUnit.MILLISECONDS);
        }
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }
}
