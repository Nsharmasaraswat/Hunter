package com.gtpautomation.securityguard.pages.search_truck

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gtpautomation.securityguard.R
import com.gtpautomation.securityguard.pages.add_truck.AddTruckActivity
import com.gtpautomation.securityguard.pages.add_truck.adapter.AddTruckFieldAdapter
import com.gtpautomation.securityguard.pages.search_truck.adapter.SearchTruckAdapter
import com.gtpautomation.securityguard.pojos.supplier.Supplier
import com.gtpautomation.securityguard.pojos.truck.Truck
import com.gtpautomation.securityguard.utils.apiCall.TruckApiServices
import java.util.*

class SearchTruckActivity : AppCompatActivity() {
    private lateinit var loaderLin: LinearLayout
    private lateinit var loaderProgressBar: ProgressBar
    private lateinit var loaderText: TextView
    private lateinit var loaderButton: MaterialButton
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private var supplier: String? = null
    private lateinit var searchView: SearchView
    private lateinit var truckListView: RecyclerView
    private lateinit var truckListAdapter  : SearchTruckAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_truck)
        supplier = intent.getStringExtra("supplier")
        initViews()
        fetchData()
    }

    private fun initViews() {
        loaderLin = findViewById(R.id.details_loader_layout)
        loaderProgressBar = findViewById(R.id.loader_progress_bar)
        loaderText = findViewById(R.id.loader_text)
        loaderButton = findViewById(R.id.loader_button)

        searchView = findViewById(R.id.truck_search)

        truckListView = findViewById(R.id.truck_rv)
        swipeRefreshLayout = findViewById(R.id.search_truck_swipe_refresh)
        swipeRefreshLayout.setColorSchemeResources(R.color.primary_500)
        swipeRefreshLayout.setOnRefreshListener {
            swipeRefreshLayout.isRefreshing = true
            fetchData()
        }
    }

    private fun showLoader() {
        loaderProgressBar.visibility = View.VISIBLE
        loaderText.visibility = View.VISIBLE
        loaderButton.visibility = View.GONE
        truckListView.visibility = View.GONE
        swipeRefreshLayout.visibility = View.GONE
        loaderText.text = resources.getString(R.string.please_wait)
        loaderLin.visibility = View.VISIBLE
    }

    private fun hideLoader() {
        swipeRefreshLayout.visibility = View.VISIBLE
        truckListView.visibility = View.VISIBLE
        loaderLin.visibility = View.GONE
    }

    private fun showError(message: String) {
        swipeRefreshLayout.isRefreshing = false
        loaderProgressBar.visibility = View.GONE
        loaderText.visibility = View.VISIBLE
        loaderButton.visibility = View.VISIBLE
        truckListView.visibility = View.GONE
        swipeRefreshLayout.visibility = View.GONE
        loaderText.text = message
        loaderLin.visibility = View.VISIBLE
        loaderButton.setOnClickListener(View.OnClickListener {
            fetchData()
        })
    }

    private fun setupRecyclerView(list: List<Truck>){
        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                truckListAdapter.filter.filter(newText)
                return false
            }
        })
        truckListAdapter = SearchTruckAdapter(this, list as ArrayList<Truck>)
        truckListView.setHasFixedSize(true)
        truckListView.layoutManager = LinearLayoutManager(this)
        truckListView.adapter = truckListAdapter
        truckListAdapter.filter.filter(searchView.query)
    }

    private fun fetchData() {
        showLoader()
        TruckApiServices.getAllTrucks(applicationContext, supplier,
            { response ->
                val list: List<Truck> = Gson().fromJson<List<Truck>>(
                    response,
                    object : TypeToken<List<Truck?>?>() {}.type
                )
                hideLoader()
                swipeRefreshLayout.isRefreshing = false
                setupRecyclerView(list)
            }
        ) {customError ->
            customError.message?.let {
                showError(it)
            } }
    }

    fun addTruck(view: View) {
        val intent:Intent = Intent(this@SearchTruckActivity, AddTruckActivity::class.java)
        intent.putExtra("supplier",supplier)
        startActivityForResult(intent, 10)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==10){
            if(data?.getStringExtra("truck") == null)return
            val truck: Truck = Gson().fromJson(
                    data.getStringExtra("truck"),
                object : TypeToken<Truck>() {}.type
            )
            truckListAdapter.addTruck(truck)
        }
    }
}