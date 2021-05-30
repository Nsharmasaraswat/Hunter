package com.gtpautomation.securityguard.pages

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.gtpautomation.securityguard.R
import com.gtpautomation.securityguard.utils.CustomTypefaceSpan
import com.gtpautomation.securityguard.utils.sharedPreferenceHelper.SharedPreferenceHelper


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener {
            startActivity(Intent(this@MainActivity, ScanActivity::class.java))
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show()
        }

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)

        val headerView: View = navView.getHeaderView(0)

        headerView.findViewById<TextView>(R.id.user_name).text = SharedPreferenceHelper.getUser(this@MainActivity)!!.user.name
        headerView.findViewById<TextView>(R.id.assigned_gate_no).text =
            getString(R.string.gate_keeper_at) +" "+SharedPreferenceHelper.getUser(this@MainActivity)!!.user.gate.name

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_todays_appointments, R.id.nav_completed_appointments
            ), drawerLayout
        )

        val m = navView.menu
        for (i in 0 until m.size()){
            val mi = m.getItem(i)

            val subMenu = mi.subMenu
            if (subMenu != null && subMenu.size() > 0) {
                for (j in 0 until subMenu.size()) {
                    val subMenuItem = subMenu.getItem(j)
                    applyFontToMenuItem(subMenuItem)
                }
            }

            applyFontToMenuItem(mi)
        }
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun applyFontToMenuItem(mi: MenuItem) {
        val font = Typeface.createFromAsset(assets, "poppins.ttf")
        val mNewTitle = SpannableString(mi.title)
        mNewTitle.setSpan(
            CustomTypefaceSpan("", font),
            0,
            mNewTitle.length,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        mi.title = mNewTitle
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_logout -> {
                val dialog: AlertDialog = AlertDialog.Builder(this)
                        .setTitle(getString(R.string.logout_alert_title))
                        .setMessage(getString(R.string.logout_alert_msg))
                        .setPositiveButton(getString(R.string.yes)) { _, _ ->
                            SharedPreferenceHelper.clearPref(applicationContext)
                            val intent = Intent(this, LoginActivity::class.java)
                            startActivity(intent)
                            finishAffinity()
                        }
                        .setNegativeButton(getString(R.string.no), null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show()
                val textView = dialog!!.findViewById<View>(android.R.id.message) as TextView
                val face = Typeface.createFromAsset(assets, "poppins.ttf")
                textView.typeface = face
            }
        }
        return super.onOptionsItemSelected(item)
    }
}