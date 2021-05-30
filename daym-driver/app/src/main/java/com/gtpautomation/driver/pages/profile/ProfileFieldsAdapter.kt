package com.gtpautomation.driver.pages.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.gtpautomation.driver.R
import com.gtpautomation.driver.data_models.FieldsItem

/**
 * Created by Sunil Kumar on 14-12-2020 08:17 AM.
 */
class ProfileFieldsAdapter() : RecyclerView.Adapter<ProfileFieldsViewHolder>() {
    private lateinit var list: List<FieldsItem>
    private lateinit var activity: AppCompatActivity

    constructor(activity: AppCompatActivity, list: List<FieldsItem>) : this() {
        this.activity = activity
        this.list = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileFieldsViewHolder {
        return ProfileFieldsViewHolder(LayoutInflater.from(activity).inflate(R.layout.profile_field_card, parent, false))
    }

    override fun onBindViewHolder(holder: ProfileFieldsViewHolder, index: Int) {
        holder.setTitle(list[index].userField.name)
        holder.setValue(list[index].value)
    }

    override fun getItemCount(): Int {
        return list.size
    }
}