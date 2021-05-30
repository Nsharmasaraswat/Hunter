package com.gtp.hunter.structure;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.gtp.hunter.BuildConfig;
import com.gtp.hunter.R;

public class TriStateCheckBox extends MaterialCheckBox {
    static private final int BACKWARD = -1;
    static private final int NONE = 0;
    static private final int FORWARD = 1;
    private int state;
    private String backwardText = "";
    private String forwardText = "";
    private String noneText = "";

    public TriStateCheckBox(Context context) {
        super(context);
        init();
    }

    public TriStateCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TriStateCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        state = NONE;
        updateBtn();

        // checkbox status is changed from uncheck to checked.
        setOnCheckedChangeListener((buttonView, isChecked) -> {
            switch (state) {
                case BACKWARD:
                    state = NONE;
                    break;
                default:
                case NONE:
                    state = FORWARD;
                    break;
                case FORWARD:
                    state = BACKWARD;
                    break;
            }
            updateBtn();
        });
    }

    @Override
    public boolean isChecked() {
        return state != NONE;
    }

    private void updateBtn() {
        int btnDrawable;

        switch (state) {
            case BACKWARD:
                btnDrawable = R.drawable.ic_checkbox_backward;
                setText(backwardText);
                break;
            default:
            case NONE:
                btnDrawable = R.drawable.ic_checkbox_none;
                setText(noneText);
                break;
            case FORWARD:
                btnDrawable = R.drawable.ic_checkbox_forward;
                setText(forwardText);
                break;
        }
        setButtonDrawable(btnDrawable);
        if (BuildConfig.DEBUG)
            Log.d("TriStateCheckBox", "Width: " + getWidth() + " Checked: " + isChecked());
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
        updateBtn();
    }

    public void setBackwardText(String text) {
        this.backwardText = text;
    }

    public void setNoneText(String text) {
        this.noneText = text;
    }

    public void setForwardText(String text) {
        this.forwardText = text;
    }
}
