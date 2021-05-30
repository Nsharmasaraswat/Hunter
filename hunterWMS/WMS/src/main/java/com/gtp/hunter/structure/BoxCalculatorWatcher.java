package com.gtp.hunter.structure;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputEditText;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Objects;

public class BoxCalculatorWatcher implements TextWatcher {
    private final TextInputEditText etPalletBox;
    private final TextInputEditText etPalletCount;
    private final TextInputEditText etLayerBox;
    private final TextInputEditText etLayerCount;
    private final TextInputEditText etBoxUnit;
    private final TextInputEditText etBoxCount;
    private final TextInputEditText etUnitCount;
    private final EditText etBoxes;

    public BoxCalculatorWatcher(TextInputEditText etPB, TextInputEditText etPC, TextInputEditText etLB, TextInputEditText etBU, TextInputEditText etLC, TextInputEditText etBC, TextInputEditText etUC, EditText etB) {
        this.etPalletBox = etPB;
        this.etPalletCount = etPC;
        this.etLayerBox = etLB;
        this.etBoxUnit = etBU;
        this.etLayerCount = etLC;
        this.etBoxCount = etBC;
        this.etUnitCount = etUC;
        this.etBoxes = etB;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        DecimalFormat df = new DecimalFormat("0.0000", DecimalFormatSymbols.getInstance(Locale.US));
        String palletBoxStr = Objects.requireNonNull(etPalletBox.getText()).toString();
        String palletCountStr = Objects.requireNonNull(etPalletCount.getText()).toString();
        String layerBoxStr = Objects.requireNonNull(etLayerBox.getText()).toString();
        String boxUnitStr = Objects.requireNonNull(etBoxUnit.getText()).toString();
        String layerCountStr = Objects.requireNonNull(etLayerCount.getText()).toString();
        String boxCountStr = Objects.requireNonNull(etBoxCount.getText()).toString();
        String unitCountStr = Objects.requireNonNull(etUnitCount.getText()).toString();
        double boxes = 0d;

        if (!palletBoxStr.isEmpty() && !palletCountStr.isEmpty()) {
            try {
                int palletBox = Integer.parseInt(palletBoxStr);
                int palletCount = Integer.parseInt(palletCountStr);

                boxes += palletBox * palletCount;
            } catch (Exception ignored) {

            }
        }

        if (!layerBoxStr.isEmpty() && !layerCountStr.isEmpty()) {
            try {
                int layerBox = Integer.parseInt(layerBoxStr);
                int layerCount = Integer.parseInt(layerCountStr);

                boxes += layerBox * layerCount;
            } catch (Exception ignored) {

            }
        }

        if (!boxUnitStr.isEmpty() && !unitCountStr.isEmpty()) {
            try {
                double boxUnit = Double.parseDouble(boxUnitStr);
                double unitCount = Double.parseDouble(unitCountStr);

                boxes += unitCount / (boxUnit == 0 ? 1 : boxUnit);
            } catch (Exception ignored) {

            }
        }

        if (!boxCountStr.isEmpty()) {
            try {
                boxes += Integer.parseInt(boxCountStr);
            } catch (Exception ignored) {

            }
        }

        etBoxes.setText(df.format(boxes));
        fixSize();
    }

    private void fixSize() {
        int pbSize = etPalletBox.getText() == null ? 0 : etPalletBox.getText().length();
        int pcSize = etPalletCount.getText() == null ? 0 : etPalletCount.getText().length();
        int lbSize = etLayerBox.getText() == null ? 0 : etLayerBox.getText().length();
        int buSize = etBoxUnit.getText() == null ? 0 : etBoxUnit.getText().length();
        int lcSize = etLayerCount.getText() == null ? 0 : etLayerCount.getText().length();
        int bcSize = etBoxCount.getText() == null ? 0 : etBoxCount.getText().length();
        int ucSize = etUnitCount.getText() == null ? 0 : etUnitCount.getText().length();

        etPalletBox.setTextSize(pbSize == 0 && !etPalletBox.hasFocus() ? 12 : 26);
        etPalletCount.setTextSize(pcSize == 0 && !etPalletCount.hasFocus() ? 12 : 26);
        etLayerBox.setTextSize(lbSize == 0 && !etLayerBox.hasFocus() ? 12 : 26);
        etBoxUnit.setTextSize(buSize == 0 && !etBoxUnit.hasFocus() ? 12 : 26);
        etLayerCount.setTextSize(lcSize == 0 && !etLayerCount.hasFocus() ? 12 : 26);
        etBoxCount.setTextSize(bcSize == 0 && !etBoxCount.hasFocus() ? 12 : 26);
        etUnitCount.setTextSize(ucSize == 0 && !etUnitCount.hasFocus() ? 12 : 26);
    }
}