package com.gtpautomation.securityguard.pages.add_truck.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.gtpautomation.securityguard.R
import com.gtpautomation.securityguard.pages.add_truck.AddTruckActivity
import com.gtpautomation.securityguard.pages.add_truck.view_holder.AddTruckFieldViewHolder
import com.gtpautomation.securityguard.pojos.truck.TruckField
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception


/**
 * Created by Sunil Kumar on 31-03-2021 04:28 PM.
 */
class AddTruckFieldAdapter(
        private var activity: AddTruckActivity,
        private var list: ArrayList<TruckField>
): RecyclerView.Adapter<AddTruckFieldViewHolder>() {
    private var fields:MutableList<Any> = mutableListOf()
init {
    fields.clear()
}
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddTruckFieldViewHolder {
        return AddTruckFieldViewHolder(
                LayoutInflater.from(activity).inflate(
                        R.layout.add_truck_field_item,
                        parent,
                        false
                )
        )
    }

    override fun onBindViewHolder(holder: AddTruckFieldViewHolder, position: Int) {
        val item = list[position]
        holder.setHint(item.name)
        when (item.type) {
            "String" -> {
                fields.add(holder.getEditText())
                holder.showTextInput()
            }
            "Number" -> {
                fields.add(holder.getEditText())
                holder.showNumberInput()
            }
            "Select" -> {
                fields.add(holder.getSpinner())
                holder.showSpinnerInput()
                holder.setSpinnerValue(item.values)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    public fun getFields():JSONArray?{
        val fieldsArray = JSONArray()
        for (i in 0 until list.size) {
            val field = JSONObject()
            field.put("truckField", list[i].id)
            if(fields[i] is EditText){
                val data = (fields[i] as EditText).text.trim()
                if(data.isEmpty() && list[i].required){
                    Toast.makeText(activity, "${list[i].name} is required", Toast.LENGTH_SHORT).show()
                    return null
                }
                field.put("value", data)
            }else if(fields[i] is Spinner){
                try {
                    val data = (((fields[i] as Spinner).selectedItem) as TruckField.SelectValue)
                    if(data.value == null && list[i].required){
                        Toast.makeText(activity, "${list[i].name} is required", Toast.LENGTH_SHORT).show()
                        return null
                    }
                    field.put("value", data.value)
                }catch(e:Exception){
                    e.printStackTrace()
                }

            }
            fieldsArray.put(field)
        }
        return fieldsArray
    }
}