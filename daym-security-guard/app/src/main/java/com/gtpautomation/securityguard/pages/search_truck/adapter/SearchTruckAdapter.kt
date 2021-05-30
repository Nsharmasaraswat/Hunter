package com.gtpautomation.securityguard.pages.search_truck.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.gtpautomation.securityguard.R
import com.gtpautomation.securityguard.pages.search_truck.SearchTruckActivity
import com.gtpautomation.securityguard.pages.add_truck.view_holder.AddTruckFieldViewHolder
import com.gtpautomation.securityguard.pages.search_truck.view_holder.SearchTruckViewHolder
import com.gtpautomation.securityguard.pojos.truck.Truck
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Sunil Kumar on 31-03-2021 04:28 PM.
 */
class SearchTruckAdapter(
    private var activity: SearchTruckActivity,
    private var list: ArrayList<Truck>
): RecyclerView.Adapter<SearchTruckViewHolder>(), Filterable {
    private var truckFilterList = ArrayList<Truck>()

    init {
        truckFilterList = list
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchTruckViewHolder {
        return SearchTruckViewHolder(
            LayoutInflater.from(activity).inflate(
                R.layout.search_truck_card,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SearchTruckViewHolder, position: Int) {
        holder.setTruckName(truckFilterList[position].name)
        holder.setTruckLicense(truckFilterList[position].licensePlate)
        holder.getRootView().setOnClickListener(View.OnClickListener {
            val intent = Intent()
            intent.putExtra("truck", Gson().toJson(truckFilterList[position]))
            activity.setResult(12, intent)
            activity.finish()
        })
    }

    override fun getItemCount(): Int {
        return truckFilterList.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                truckFilterList = if (charSearch.isEmpty()) {
                    list
                } else {
                    val resultList = ArrayList<Truck>()
                    for (row in list) {
                        if (row.name.toLowerCase(Locale.ROOT).contains(charSearch.toLowerCase(Locale.ROOT)) ||
                            row.licensePlate.toLowerCase(Locale.ROOT).contains(
                                charSearch.toLowerCase(
                                    Locale.ROOT
                                )
                            )) {
                            resultList.add(row)
                        }
                    }
                    resultList
                }
                val filterResults = FilterResults()
                filterResults.values = truckFilterList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                truckFilterList = results?.values as ArrayList<Truck>
                notifyDataSetChanged()
            }

        }
    }

    fun addTruck(truck:Truck){
        list.add(truck)
        notifyDataSetChanged()
    }

}