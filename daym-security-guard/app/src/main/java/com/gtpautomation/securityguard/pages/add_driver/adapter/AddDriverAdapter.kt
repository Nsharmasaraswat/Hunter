package com.gtpautomation.securityguard.pages.add_driver.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.gtpautomation.securityguard.R
import com.gtpautomation.securityguard.pages.add_driver.AddDriverActivity
import com.gtpautomation.securityguard.pages.add_truck.view_holder.AddTruckFieldViewHolder
import com.gtpautomation.securityguard.pojos.userModel.UserField
import org.json.JSONArray
import org.json.JSONObject
import java.util.ArrayList

/**
 * Created by Sunil Kumar on 05-04-2021 10:06 PM.
 */
class AddDriverAdapter(
    private var activity: AddDriverActivity,
    private var list: ArrayList<UserField>
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

    public fun getFields(): JSONArray?{
        val fieldsArray = JSONArray()
        for (i in 0 until list.size) {
            val field = JSONObject()
            field.put("userField", list[i].id)
            if(fields[i] is EditText){
                val data = (fields[i] as EditText).text.trim()
                if(data.isEmpty() && list[i].isRequired){
                    Toast.makeText(activity, "${list[i].name} is required", Toast.LENGTH_SHORT).show()
                    return null
                }
                field.put("value", data)
            }else if(fields[i] is Spinner){
                val data = (((fields[i] as Spinner).selectedItem) as String).trim()
                if(data.isEmpty() && list[i].isRequired){
                    Toast.makeText(activity, "${list[i].name} is required", Toast.LENGTH_SHORT).show()
                    return null
                }
                field.put("value", data)
            }
            fieldsArray.put(field)
        }
        return fieldsArray
    }
}