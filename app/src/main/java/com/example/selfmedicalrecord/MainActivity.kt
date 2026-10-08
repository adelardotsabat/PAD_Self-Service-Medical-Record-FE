package com.example.selfmedicalrecord

import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import androidx.navigation.ui.findStartDestination
import com.example.selfmedicalrecord.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    /** Satu tab di bottom bar: tujuan navigasi + 3 view yang perlu diubah saat aktif/inaktif. */
    private class Tab(
        val destinationId: Int,
        val container: LinearLayout,
        val icon: ImageView,
        val label: TextView
    )

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var tabs: List<Tab>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWindowInsets()

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_user) as NavHostFragment
        navController = navHostFragment.navController

        setupBottomNav()
    }

    /**
     * Targetnya SDK 35+ memaksa edge-to-edge, jadi konten harus diberi ruang sendiri:
     * - area fragment diberi padding atas/kiri/kanan sebesar status bar & cutout
     * - bottom nav yang melayang diangkat sebesar tinggi navigation bar / gesture bar
     */
    private fun setupWindowInsets() {
        val navBarBaseMargin = resources.getDimensionPixelSize(R.dimen.bottom_nav_margin_bottom)
        val navBarSideMargin = resources.getDimensionPixelSize(R.dimen.bottom_nav_margin_horizontal)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
            val bars = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )

            binding.navHostFragmentUser.updatePadding(
                left = bars.left,
                top = bars.top,
                right = bars.right
            )

            binding.bottomNavigationContainer.root.updateLayoutParams<ConstraintLayout.LayoutParams> {
                leftMargin = navBarSideMargin + bars.left
                rightMargin = navBarSideMargin + bars.right
                bottomMargin = navBarBaseMargin + bars.bottom
            }

            // Tidak dikonsumsi, supaya fragment di dalamnya tetap bisa membaca insets sendiri.
            windowInsets
        }
    }

    private fun setupBottomNav() {
        val nav = binding.bottomNavigationContainer

        tabs = listOf(
            Tab(R.id.navigation_home, nav.navHome, nav.iconHome, nav.textHome),
            Tab(R.id.navigation_catat, nav.navRecord, nav.iconRecord, nav.textRecord),
            Tab(R.id.navigation_scan, nav.navScan, nav.iconScan, nav.textScan),
            Tab(R.id.navigation_riwayat, nav.navHistory, nav.iconHistory, nav.textHistory),
            Tab(R.id.navigation_profil, nav.navProfile, nav.iconProfile, nav.textProfile)
        )

        tabs.forEach { tab ->
            tab.container.setOnClickListener { navigateToTab(tab.destinationId) }
        }

        // Sumber kebenaran tampilan tab = destinasi NavController, bukan klik.
        // Dengan begitu tombol back sistem dan navigasi dari kode ikut menyinkronkan highlight.
        // Listener ini juga langsung dipanggil dengan destinasi saat ini, jadi tab awal otomatis aktif.
        navController.addOnDestinationChangedListener { _, destination, _ ->
            tabs.firstOrNull { it.destinationId == destination.id }?.let(::updateActiveTab)
        }
    }

    /**
     * Pindah tab tanpa menumpuk back stack (pola yang sama dengan NavigationUI):
     * - tap tab yang sedang aktif diabaikan
     * - back stack dipangkas sampai start destination, state tab disimpan dan dipulihkan
     */
    private fun navigateToTab(destinationId: Int) {
        if (navController.currentDestination?.id == destinationId) return

        navController.navigate(
            destinationId,
            null,
            navOptions {
                launchSingleTop = true
                restoreState = true
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
            }
        )
    }

    private fun updateActiveTab(selected: Tab) {
        val activeColor = ContextCompat.getColor(this, R.color.blue_primary)
        val inactiveColor = ContextCompat.getColor(this, R.color.text_secondary)

        TransitionManager.beginDelayedTransition(
            binding.bottomNavigationContainer.bottomNavigation,
            AutoTransition().setDuration(150)
        )

        for (tab in tabs) {
            val params = tab.container.layoutParams as LinearLayout.LayoutParams

            if (tab === selected) {
                tab.container.background =
                    ContextCompat.getDrawable(this, R.drawable.bg_nav_item_active)
                tab.icon.setColorFilter(activeColor)
                tab.label.setTextColor(activeColor)
                tab.label.visibility = View.VISIBLE
                params.weight = 2.0f // tab aktif melebar
            } else {
                tab.container.background = null
                tab.icon.setColorFilter(inactiveColor)
                tab.label.visibility = View.GONE
                params.weight = 1.0f
            }
            tab.container.layoutParams = params
        }
    }
}
