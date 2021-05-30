package com.gtpautomation.driver.pages.appointment_details

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.gtpautomation.driver.R

/**
 * Created by Sunil Kumar on 12-12-2020 07:01 PM.
 */
class AppointmentOrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private var loaderLayout: LinearLayout = itemView.findViewById(R.id.order_card_loader_layout)
    private var orderCardDataLayout: ConstraintLayout = itemView.findViewById(R.id.order_card_data_layout)
    private var orderQuantity: TextView = itemView.findViewById(R.id.order_card_quantity)
    private var dockName: TextView = itemView.findViewById(R.id.order_card_dock_name)
    private var warehouseName: TextView = itemView.findViewById(R.id.order_card_warehouse_name)
    private var completedText: TextView = itemView.findViewById(R.id.order_card_completed)

    public fun showLoader() {
        loaderLayout.visibility = View.VISIBLE
        orderCardDataLayout.visibility = View.INVISIBLE
    }

    public fun hideLoader() {
        loaderLayout.visibility = View.INVISIBLE
        orderCardDataLayout.visibility = View.VISIBLE
    }

    public fun setDockName(name: String) {
        dockName.text = name
    }

    public fun setWareHouseName(name: String) {
        warehouseName.text = name
    }

    public fun setQuantity(quantity: Int) {
        orderQuantity.text = String.format(itemView.context.getString(R.string.quantity), quantity)
    }


    public fun setStatus(status: Int) {
        if (status == 1) {
            completedText.visibility = View.GONE
        } else {
            completedText.visibility = View.VISIBLE
        }
    }
}