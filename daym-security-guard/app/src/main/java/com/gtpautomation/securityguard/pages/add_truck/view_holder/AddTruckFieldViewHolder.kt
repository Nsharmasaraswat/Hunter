package com.gtpautomation.securityguard.pages.add_truck.view_holder

import android.content.Context
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.recyclerview.widget.RecyclerView
import com.gtpautomation.securityguard.R
import com.gtpautomation.securityguard.pages.add_truck.adapter.TruckSpinnerAdapter
import com.gtpautomation.securityguard.pojos.truck.TruckField


/**
 * Created by Sunil Kumar on 31-03-2021 04:28 PM.
 */
class AddTruckFieldViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private var editTextInput: EditText = itemView.findViewById(R.id.add_truck_field_name)
    private var spinnerInput: Spinner = itemView.findViewById(R.id.add_truck_field_spinner)
    public var spinnerDataAdapter: TruckSpinnerAdapter? = null

    public fun getRootView():View = itemView
    public fun getEditText():EditText = editTextInput
    public fun getSpinner():Spinner = spinnerInput

    public fun setHint(hint: String){
        editTextInput.hint = hint
        spinnerInput.prompt = hint
    }

    public fun showTextInput(){
        editTextInput.visibility = View.VISIBLE
        spinnerInput.visibility = View.GONE
        editTextInput.inputType = InputType.TYPE_CLASS_TEXT
    }
    public fun showNumberInput(){
        editTextInput.visibility = View.VISIBLE
        spinnerInput.visibility = View.GONE
        editTextInput.inputType = InputType.TYPE_CLASS_NUMBER
    }
    public fun showSpinnerInput(){
        spinnerInput.visibility = View.VISIBLE
    }

    public fun setSpinnerValue(list: MutableList<TruckField.SelectValue>){
        spinnerDataAdapter = TruckSpinnerAdapter(list, itemView.context)
        spinnerInput.adapter = spinnerDataAdapter
    }
}