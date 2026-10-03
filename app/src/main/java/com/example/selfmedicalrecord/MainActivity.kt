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
import com.example.selfmedicalrecord.utils.applyFigmaShadow

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

        // Kumpulkan 3 elemen penting per menu: (Container, Icon, Text)
        val menuList = listOf(
            Triple(navContainer.navHome, navContainer.iconHome, navContainer.textHome),
            Triple(navContainer.navRecord, navContainer.iconRecord, navContainer.textRecord),
            Triple(navContainer.navScan, navContainer.iconScan, navContainer.textScan),
            Triple(navContainer.navHistory, navContainer.iconHistory, navContainer.textHistory),
            Triple(navContainer.navProfile, navContainer.iconProfile, navContainer.textProfile)
        )

        // Ambil warna dari colors.xml
        val activeColor = ContextCompat.getColor(this, R.color.blue_primary)
        val inactiveColor = ContextCompat.getColor(this, R.color.text_secondary)

        // Fungsi update tampilan tab dengan animasi & pewarnaan ikon
        fun updateActiveTab(selectedContainer: LinearLayout) {
            TransitionManager.beginDelayedTransition(
                navContainer.bottomNavigation,
                AutoTransition().setDuration(150)
            )

            for ((container, icon, text) in menuList) {
                val params = container.layoutParams as LinearLayout.LayoutParams

                if (container == selectedContainer) {
                    // TAB AKTIF
                    container.background = ContextCompat.getDrawable(this, R.drawable.bg_nav_item_active)
                    icon.setColorFilter(activeColor)
                    text.setTextColor(activeColor)
                    text.visibility = View.VISIBLE
                    params.weight = 2.0f // Pelebaran tab aktif
                } else {
                    // TAB INAKTIF
                    container.background = null
                    icon.setColorFilter(inactiveColor)
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

            // Catatan: Jika nanti Catat dijadikan Activity terpisah, tinggal ganti baris di atas dengan:
            // startActivity(Intent(this, InputDataKesehatanActivity::class.java))
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