package com.gtpautomation.securityguard.pages.all_items.adapter


import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.gtpautomation.securityguard.R
import com.gtpautomation.securityguard.pojos.order_items.Products


class AllItemsAdapter(
    private val context: Context?,
    private val listData: ArrayList<Products>
) : RecyclerView.Adapter<TodaysAppointmentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodaysAppointmentViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val listItem: View = layoutInflater.inflate(
            R.layout.all_items_row,
            parent,
            false
        )
        return TodaysAppointmentViewHolder(listItem)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: TodaysAppointmentViewHolder, position: Int) {
        when(listData[position].status){
            1 -> {
                holder.tvStatus.text = context!!.getString(R.string.status_pending)
                holder.tvStatus.setBackgroundColor(context.resources.getColor(R.color.order_pending))
            }
            2 -> {
                holder.tvStatus.text = context!!.getString(R.string.status_pending)
                holder.tvStatus.setBackgroundColor(context.resources.getColor(R.color.order_completed))
            }
        }
        holder.tvProductName.text = listData[position].productName
        holder.tvProductCode.text = listData[position].productCode
        holder.tvWarehouseName.text = listData[position].warehouse
        holder.tvDockName.text = listData[position].dock
    }

    override fun getItemCount(): Int {
        return listData.size
    }

}

class TodaysAppointmentViewHolder(itemView: View) : ViewHolder(itemView) {
    var tvStatus: TextView = itemView.findViewById(R.id.tv_order_status)
    var tvProductName: TextView = itemView.findViewById(R.id.tv_product_name)
    var tvProductCode: TextView = itemView.findViewById(R.id.tv_product_code)
    var tvWarehouseName: TextView = itemView.findViewById(R.id.tv_warehouse_name)
    var tvDockName: TextView = itemView.findViewById(R.id.tv_dock_name)
}