package com.example.selfmedicalrecord

import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.View
import android.widget.LinearLayout
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

        // Setup NavController
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_user) as NavHostFragment
        val navController = navHostFragment.navController

        // Akses custom bottom nav container
        val navContainer = binding.bottomNavigationContainer

        // Kumpulkan pasangan menu (Container & Text)
        val menuList = listOf(
            Pair(navContainer.navHome, navContainer.textHome),
            Pair(navContainer.navRecord, navContainer.textRecord),
            Pair(navContainer.navScan, navContainer.textScan),
            Pair(navContainer.navHistory, navContainer.textHistory),
            Pair(navContainer.navProfile, navContainer.textProfile)
        )

        // Fungsi update tampilan tab dengan animasi geser & pelebaran background
        fun updateActiveTab(selectedContainer: LinearLayout) {
            TransitionManager.beginDelayedTransition(
                navContainer.bottomNavigation,
                AutoTransition().setDuration(200)
            )

            for ((container, text) in menuList) {
                val params = container.layoutParams as LinearLayout.LayoutParams

                if (container == selectedContainer) {
                    container.background = ContextCompat.getDrawable(this, R.drawable.bg_nav_selected)
                    text.visibility = View.VISIBLE
                    params.weight = 1.6f
                } else {
                    container.background = null
                    text.visibility = View.GONE
                    params.weight = 1.0f
                }
                container.layoutParams = params
            }
        }

        // Default tab aktif pertama kali (Beranda)
        updateActiveTab(navContainer.navHome)

        // Event Listener tiap tombol
        navContainer.navHome.setOnClickListener {
            updateActiveTab(navContainer.navHome)
            navController.navigate(R.id.navigation_home)
        }

        navContainer.navRecord.setOnClickListener {
            updateActiveTab(navContainer.navRecord)
            navController.navigate(R.id.navigation_catat)
        }

        navContainer.navScan.setOnClickListener {
            updateActiveTab(navContainer.navScan)
            navController.navigate(R.id.navigation_scan)
        }

        navContainer.navHistory.setOnClickListener {
            updateActiveTab(navContainer.navHistory)
            navController.navigate(R.id.navigation_riwayat)
        }

        navContainer.navProfile.setOnClickListener {
            updateActiveTab(navContainer.navProfile)
            navController.navigate(R.id.navigation_profil)
        }
    }
}