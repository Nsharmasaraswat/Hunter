package com.gtpautomation.driver.pages.profile

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gtpautomation.driver.R

/**
 * Created by Sunil Kumar on 14-12-2020 08:13 AM.
 */
class ProfileFieldsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private var fieldText: TextView = itemView.findViewById(R.id.profile_field)
    private var fieldTitle: TextView = itemView.findViewById(R.id.profile_field_title)
    public fun setValue(value: String) {
        fieldText.text = value
    }

    public fun setTitle(value: String) {
        fieldTitle.text = value
    }
}