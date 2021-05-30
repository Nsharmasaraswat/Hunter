package com.gtpautomation.securityguard.pages.all_items

import android.graphics.PorterDuff
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gtpautomation.securityguard.R
import com.gtpautomation.securityguard.pages.all_items.adapter.AllItemsAdapter
import com.gtpautomation.securityguard.pojos.order_items.Products
import java.util.*

class AllItemsActivity : AppCompatActivity() {

    private lateinit var itemsRecycler: RecyclerView
    private lateinit var ordersList: ArrayList<Products>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_items)
        ordersList = intent.getParcelableArrayListExtra("items")!!
        setToolBar()
        setupRecyclerView()
    }

    private fun setupRecyclerView(){
        itemsRecycler = findViewById(R.id.items_recycler)
        val adapter = AllItemsAdapter(this, ordersList)
        itemsRecycler.setHasFixedSize(true)
        itemsRecycler.layoutManager = LinearLayoutManager(this)
        itemsRecycler.adapter = adapter
    }

    private fun setToolBar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        toolbar.title = getString(R.string.items_in_the_order)
        toolbar.setTitleTextColor(resources.getColor(R.color.white))
        Objects.requireNonNull(toolbar.navigationIcon)!!.setColorFilter(
                resources.getColor(R.color.white),
                PorterDuff.Mode.SRC_ATOP
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}