package com.gtpautomation.securityguard.ui.todays_appointments.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.gtpautomation.securityguard.R
import com.gtpautomation.securityguard.pages.AppointmentDetailsActivity
import com.gtpautomation.securityguard.pojos.appointment.AppointmentResponse


class TodaysAppointmentsAdapter(
    private val context: Context?,
    private val listData: Array<AppointmentResponse>
) : RecyclerView.Adapter<TodaysAppointmentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodaysAppointmentViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val listItem: View = layoutInflater.inflate(
            R.layout.todays_appointment_list_item,
            parent,
            false
        )
        return TodaysAppointmentViewHolder(listItem)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: TodaysAppointmentViewHolder, position: Int) {
        when(listData[position].status){
            4 -> {
                holder.tvStatus.text = context!!.getString(R.string.scheduled)
                holder.bgAppointmentStatus.setBackgroundColor(context.resources.getColor(R.color.order_pending))
                holder.bgGate.setBackgroundColor(context.resources.getColor(R.color.order_pending))
            }
            5 -> {
                holder.tvStatus.text = context!!.getString(R.string.accepted)
                holder.bgAppointmentStatus.setBackgroundColor(context.resources.getColor(R.color.order_pending))
                holder.bgGate.setBackgroundColor(context.resources.getColor(R.color.order_pending))
            }
            6 -> {
                holder.tvStatus.text = context!!.getString(R.string.rejected)
                holder.bgAppointmentStatus.setBackgroundColor(context.resources.getColor(R.color.order_rejected))
                holder.bgGate.setBackgroundColor(context.resources.getColor(R.color.order_pending))
            }
            7 -> {
                holder.tvStatus.text = context!!.getString(R.string.truck_entered)
                holder.bgAppointmentStatus.setBackgroundColor(context.resources.getColor(R.color.order_pending))
                holder.bgGate.setBackgroundColor(context.resources.getColor(R.color.order_pending))}
            8 -> {
                holder.tvStatus.text = context!!.getString(R.string.completed)
                holder.bgAppointmentStatus.setBackgroundColor(context.resources.getColor(R.color.order_completed))
                holder.bgGate.setBackgroundColor(context.resources.getColor(R.color.order_pending))
            }
        }

        if(listData[position].driver == null){
            holder.tvDriverName.text = context!!.getString(R.string.driver_not_assigned)
        }else{
            holder.tvDriverName.text = listData[position].driver.name
        }

        holder.tvSupplierName.text = listData[position].supplier.name
        holder.tvGate.text = listData[position].entranceGate.name
        if(listData[position].user !=null)
            holder.tvOwnerName.text = listData[position].user.name
        else
            holder.tvOwnerName.text = "User not added"
        holder.itemView.setOnClickListener {
            val intent = Intent(context, AppointmentDetailsActivity::class.java).putExtra("appointment", listData[position].id)
            context?.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return listData.size
    }

}

class TodaysAppointmentViewHolder(itemView: View) : ViewHolder(itemView) {
    var tvStatus: TextView = itemView.findViewById(R.id.tv_appointment_status)
    var tvGate: TextView = itemView.findViewById(R.id.tv_gate)
    var tvSupplierName: TextView = itemView.findViewById(R.id.tv_supplier_name)
    var tvDriverName: TextView = itemView.findViewById(R.id.tv_driver_name)
    var tvOwnerName: TextView = itemView.findViewById(R.id.tv_owner_name)
    var bgAppointmentStatus: LinearLayout = itemView.findViewById(R.id.bg_appointment_status)
    var bgGate: LinearLayout = itemView.findViewById(R.id.bg_gate)
}