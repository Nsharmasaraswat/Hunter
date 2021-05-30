package com.gtpautomation.securityguard.pages.search_truck.view_holder

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gtpautomation.securityguard.R

/**
 * Created by Sunil Kumar on 31-03-2021 04:28 PM.
 */
class SearchTruckViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private var truckName: TextView = itemView.findViewById(R.id.truck_card_name)
    private var truckLicense: TextView = itemView.findViewById(R.id.truck_card_license)

    public fun setTruckName(name:String) {
        truckName.text = name
    }

    public fun getRootView():View = itemView

    public fun setTruckLicense(license:String) {
        truckLicense.text = license
    }
}