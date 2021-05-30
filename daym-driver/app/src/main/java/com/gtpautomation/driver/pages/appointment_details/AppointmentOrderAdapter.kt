package com.gtpautomation.driver.pages.appointment_details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gtpautomation.driver.R
import com.gtpautomation.driver.data_models.OrdersItem


/**
 * Created by Sunil Kumar on 12-12-2020 08:22 PM.
 */
class AppointmentOrderAdapter() : RecyclerView.Adapter<AppointmentOrderViewHolder>() {
    private lateinit var list: List<OrdersItem>
    private lateinit var activity: AppointmentDetailsActivity

    constructor(activity: AppointmentDetailsActivity, list: List<OrdersItem>) : this() {
        this.activity = activity
        this.list = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentOrderViewHolder {
        return AppointmentOrderViewHolder(LayoutInflater.from(activity).inflate(R.layout.appointment_orders_card, parent, false))
    }

    override fun onBindViewHolder(holder: AppointmentOrderViewHolder, index: Int) {
        holder.setDockName(list[index].dock.name)
        holder.setWareHouseName(list[index].warehouse.name)
        holder.setQuantity(list[index].quantity)
        holder.setStatus(list[index].status)
        holder.hideLoader()
    }

    override fun getItemCount(): Int {
        return list.size
    }

}