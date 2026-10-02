package com.example.selfmedicalrecord

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import com.example.selfmedicalrecord.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ambil NavController
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_user) as NavHostFragment

        val navController = navHostFragment.navController

        // Bottom Navigation
        val navHome = findViewById<LinearLayout>(R.id.navHome)
        val navRecord = findViewById<LinearLayout>(R.id.navRecord)
        val navScan = findViewById<LinearLayout>(R.id.navScan)
        val navHistory = findViewById<LinearLayout>(R.id.navHistory)
        val navProfile = findViewById<LinearLayout>(R.id.navProfile)

        val textHome = findViewById<TextView>(R.id.textHome)
        val textRecord = findViewById<TextView>(R.id.textRecord)
        val textScan = findViewById<TextView>(R.id.textScan)
        val textHistory = findViewById<TextView>(R.id.textHistory)
        val textProfile = findViewById<TextView>(R.id.textProfile)

        // Beranda aktif saat pertama dibuka
        selectNavigation(
            navHome,
            textHome,
            navRecord,
            textRecord,
            navScan,
            textScan,
            navHistory,
            textHistory,
            navProfile,
            textProfile
        )

        // Beranda
        navHome.setOnClickListener {
            selectNavigation(
                navHome,
                textHome,
                navRecord,
                textRecord,
                navScan,
                textScan,
                navHistory,
                textHistory,
                navProfile,
                textProfile
            )

            navController.navigate(R.id.navigation_home)
        }

        // Catat
        navRecord.setOnClickListener {
            selectNavigation(
                navRecord,
                textRecord,
                navHome,
                textHome,
                navScan,
                textScan,
                navHistory,
                textHistory,
                navProfile,
                textProfile
            )

            navController.navigate(R.id.navigation_catat)
        }

        // Scan
        navScan.setOnClickListener {
            selectNavigation(
                navScan,
                textScan,
                navHome,
                textHome,
                navRecord,
                textRecord,
                navHistory,
                textHistory,
                navProfile,
                textProfile
            )

            navController.navigate(R.id.navigation_scan)
        }

        // Riwayat
        navHistory.setOnClickListener {
            selectNavigation(
                navHistory,
                textHistory,
                navHome,
                textHome,
                navRecord,
                textRecord,
                navScan,
                textScan,
                navProfile,
                textProfile
            )

            navController.navigate(R.id.navigation_riwayat)
        }

        // Profil
        navProfile.setOnClickListener {
            selectNavigation(
                navProfile,
                textProfile,
                navHome,
                textHome,
                navRecord,
                textRecord,
                navScan,
                textScan,
                navHistory,
                textHistory
            )

            navController.navigate(R.id.navigation_profil)
        }
    }

    private fun selectNavigation(
        selectedItem: LinearLayout,
        selectedText: TextView,
        vararg otherItems: Any
    ) {

        // Background item yang dipilih
        selectedItem.background =
            ContextCompat.getDrawable(
                this,
                R.drawable.bg_nav_selected
            )

        // Tampilkan text item yang dipilih
        selectedText.visibility = View.VISIBLE

        // Sembunyikan text item lainnya
        var i = 0

        while (i < otherItems.size) {

            val item = otherItems[i] as LinearLayout
            val text = otherItems[i + 1] as TextView

            item.background = null
            text.visibility = View.GONE

            i += 2
        }
    }
}