package com.gtp.hunter.wms.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.gtp.hunter.R;
import com.gtp.hunter.util.DocumentUtil;
import com.gtp.hunter.wms.interfaces.ActionFragmentListener;
import com.gtp.hunter.wms.model.AGLDocModel;
import com.gtp.hunter.wms.model.AGLDocumentProps;
import com.gtp.hunter.wms.model.ActionFormOption;
import com.gtp.hunter.wms.model.Document;
import com.gtp.hunter.wms.model.DocumentField;
import com.gtp.hunter.wms.model.DocumentModelField;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class FormDialogFragment extends DialogFragment {

    private static final int LABEL_SIZE = 18;

    private AGLDocumentProps aglDocument;
    private Document document;
    private HashMap<String, View> uiProps;
    private ActionFragmentListener mActionListener;

    public static FormDialogFragment newInstance(Document doc) {
        FormDialogFragment dialog = new FormDialogFragment();
        Bundle args = new Bundle();

        args.putSerializable("DOCUMENT", doc);
        dialog.setArguments(args);
        return dialog;
    }

    public static FormDialogFragment newInstance(AGLDocumentProps doc) {
        FormDialogFragment dialog = new FormDialogFragment();
        Bundle args = new Bundle();

        args.putSerializable("AGLDOCUMENT", doc);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.uiProps = new HashMap<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.form_dynamic, container, false);
        LinearLayout layout = v.findViewById(R.id.dynamicFieldsLayout);
        Bundle b = getArguments();
        ViewGroup.MarginLayoutParams margins = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        TextView title = v.findViewById(R.id.txtTitle);

        margins.setMargins(0, 6, 0, 6);
        if (b != null && b.containsKey("AGLDOCUMENT")) {
            this.aglDocument = (AGLDocumentProps) b.getSerializable("AGLDOCUMENT");
            title.setText(this.aglDocument == null ? "" : this.aglDocument.getCode());
            createAGLForm(layout, v, margins);
        } else if (b != null && b.containsKey("DOCUMENT")) {
            this.document = (Document) b.getSerializable("DOCUMENT");
            title.setText(this.document == null ? "" : this.document.getCode());
            createDocForm(layout, v, margins);
        }

        for (View vw : uiProps.values()) {
            if (!vw.isInLayout()) {
                vw.requestLayout();
            }
        }
        if (!layout.isInLayout()) {
            layout.requestLayout();
        }
        if (!v.isInLayout()) {
            v.requestLayout();
        }
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getDialog() != null) {
            Window window = getDialog().getWindow();
            int width = getResources().getDisplayMetrics().widthPixels;

            assert window != null;
            getDialog().setCanceledOnTouchOutside(false);
            getDialog().setTitle(this.aglDocument == null ? this.document.getCode() : this.aglDocument.getCode());
            window.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);
            WindowManager.LayoutParams params = window.getAttributes();
            params.y = 8;
            window.setAttributes(params);
        }
    }

    private void createAGLForm(LinearLayout layout, View baseLayout, ViewGroup.MarginLayoutParams margins) {
        View lastView = null;

        for (AGLDocModel prop : Objects.requireNonNull(FormDialogFragment.this.aglDocument).getModel()) {
            String type = prop.getType();
            TextView tv = new TextView(baseLayout.getContext());

            tv.setText(prop.getName());
            switch (type) {
                case "BOOLEAN":
                    CheckBox cb = new CheckBox(baseLayout.getContext());

                    cb.setText(prop.getName());
                    cb.setLayoutParams(margins);
                    uiProps.put(prop.getAttrib(), cb);
                    layout.addView(cb);
                    break;
                case "COMBO":
                    Spinner sp = new Spinner(baseLayout.getContext());
                    List<String> options = new ArrayList<>();

                    for (int i = 0; i < prop.getOptions().size(); i++) {
                        ActionFormOption option = prop.getOptions().get(i);

                        options.add(option.getLabel());
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(baseLayout.getContext(), android.R.layout.simple_spinner_item, new ArrayList<>(options));

                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    sp.setAdapter(adapter);
                    layout.addView(tv);
                    uiProps.put(prop.getAttrib(), sp);
                    sp.setLayoutParams(margins);
                    layout.addView(sp);
                    break;
                case "CHAR":
                case "NUMBER":
                default:
                    EditText et = new EditText(baseLayout.getContext());

                    if (type.equals("NUMBER"))
                        et.setInputType(InputType.TYPE_CLASS_NUMBER);
                    tv.setTextSize(LABEL_SIZE);
                    tv.setLabelFor(et.getId());
                    layout.addView(tv);
                    et.setMinimumWidth(200);
                    uiProps.put(prop.getAttrib(), et);
                    et.setLayoutParams(margins);
                    layout.addView(et);
                    break;
            }
            tv.forceLayout();
            if (lastView != null)
                lastView.setNextFocusDownId(Objects.requireNonNull(uiProps.get(prop.getAttrib())).getId());
            lastView = uiProps.get(prop.getAttrib());
        }

        baseLayout.findViewById(R.id.btnCancel).setOnClickListener((View vc) -> dismiss());
        baseLayout.findViewById(R.id.btnSave).setOnClickListener((View vc) -> {
            for (AGLDocModel prop : FormDialogFragment.this.aglDocument.getModel()) {
                String type = prop.getType();

                switch (type) {
                    case "BOOLEAN":
                        CheckBox cb = (CheckBox) uiProps.get(prop.getAttrib());

                        prop.setValue(String.valueOf(Objects.requireNonNull(cb).isChecked()));
                        break;
                    case "COMBO":
                        Spinner sp = (Spinner) uiProps.get(prop.getAttrib());

                        for (ActionFormOption opt : prop.getOptions()) {
                            if (opt.getLabel().equals(Objects.requireNonNull(sp).getSelectedItem().toString()))
                                prop.setValue(opt.getValue());
                        }
                        break;
                    case "CHAR":
                    case "NUMBER":
                    default:
                        EditText et = (EditText) uiProps.get(prop.getAttrib());

                        prop.setValue(Objects.requireNonNull(et).getText().toString());
                        break;
                }
            }
            if (mActionListener.sendAGLDocumentProps(aglDocument))
                dismiss();
        });
    }

    private void createDocForm(LinearLayout layout, View v, ViewGroup.MarginLayoutParams margins) {
        View lastView = null;

        for (DocumentModelField dmf : Objects.requireNonNull(FormDialogFragment.this.document).getModel().getFields()) {
            String type = dmf.getType();
            TextView tv = new TextView(v.getContext());
            Gson g = new Gson();
            Type tp = new TypeToken<List<ActionFormOption>>() {
            }.getType();
            List<ActionFormOption> docOptions = g.fromJson(dmf.getParams(), tp);

            tv.setText(dmf.getName());
            switch (type) {
                case "BOOLEAN":
                    CheckBox cb = new CheckBox(v.getContext());

                    cb.setText(dmf.getName());
                    cb.setLayoutParams(margins);
                    uiProps.put(dmf.getMetaname(), cb);
                    layout.addView(cb);
                    break;
                case "COMBO":
                    Spinner sp = new Spinner(v.getContext());
                    List<String> options = new ArrayList<>();

                    for (int i = 0; i < docOptions.size(); i++) {
                        ActionFormOption option = docOptions.get(i);

                        options.add(option.getLabel());
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(v.getContext(), android.R.layout.simple_spinner_item, new ArrayList<>(options));

                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    sp.setAdapter(adapter);
                    layout.addView(tv);
                    uiProps.put(dmf.getMetaname(), sp);
                    sp.setLayoutParams(margins);
                    layout.addView(sp);
                    break;
                case "CHAR":
                case "NUMBER":
                default:
                    EditText et = new EditText(v.getContext());

                    if (type.equals("NUMBER")) {
                        et.setInputType(InputType.TYPE_CLASS_NUMBER);
                        et.setSingleLine(true);
                    }
                    tv.setTextSize(LABEL_SIZE);
                    tv.setLabelFor(et.getId());
                    layout.addView(tv);
                    et.setMinimumWidth(220);
                    uiProps.put(dmf.getMetaname(), et);
                    et.setLayoutParams(margins);
                    layout.addView(et);
                    break;
            }
            tv.forceLayout();
            if (lastView != null)
                lastView.setNextFocusDownId(Objects.requireNonNull(uiProps.get(dmf.getMetaname())).getId());
            lastView = uiProps.get(dmf.getMetaname());
        }

        v.findViewById(R.id.btnCancel).setOnClickListener((View vc) -> dismiss());
        v.findViewById(R.id.btnSave).setOnClickListener((View vc) -> {
            for (DocumentModelField dmf : FormDialogFragment.this.document.getModel().getFields()) {
                String type = dmf.getType();
                Gson g = new Gson();
                Type tp = new TypeToken<List<ActionFormOption>>() {
                }.getType();
                List<ActionFormOption> docOptions = g.fromJson(dmf.getParams(), tp);
                DocumentField df = DocumentUtil.getField(this.document, dmf.getMetaname());

                if (df == null) {
                    df = new DocumentField();
                    df.setField(dmf);
                    df.setStatus("NOVO");
                    df.setModelId(dmf.getId());
                    df.setCreatedAt(Calendar.getInstance().getTime());
                    df.setUpdatedAt(Calendar.getInstance().getTime());
                    this.document.getFields().add(df);
                }
                switch (type) {
                    case "BOOLEAN":
                        CheckBox cb = (CheckBox) uiProps.get(dmf.getMetaname());

                        df.setValue(String.valueOf(Objects.requireNonNull(cb).isChecked()));
                        break;
                    case "COMBO":
                        Spinner sp = (Spinner) uiProps.get(dmf.getMetaname());

                        for (ActionFormOption opt : docOptions) {
                            if (opt.getLabel().equals(Objects.requireNonNull(sp).getSelectedItem().toString()))
                                df.setValue(opt.getValue());
                        }
                        break;
                    case "CHAR":
                    case "NUMBER":
                    default:
                        EditText et = (EditText) uiProps.get(dmf.getMetaname());

                        df.setValue(Objects.requireNonNull(et).getText().toString());
                        break;
                }
            }
            if (mActionListener.sendDocument(document).getResult())
                dismiss();
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mActionListener = (ActionFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement NewItemDialogFragment");
        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        mActionListener.cancelTask();
        mActionListener.returnFromFragment();
    }
}
