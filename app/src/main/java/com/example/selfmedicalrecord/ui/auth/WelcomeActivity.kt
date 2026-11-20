package com.example.selfmedicalrecord.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.selfmedicalrecord.R
import com.example.selfmedicalrecord.data.ServiceLocator
import com.example.selfmedicalrecord.databinding.ActivityWelcomeBinding
import com.example.selfmedicalrecord.utils.applySystemBarInsetsAsPadding
import com.google.android.material.snackbar.Snackbar

/**
 * Layar pertama aplikasi: memilih cara masuk. Hanya navigasi, tanpa logika data.
 * Masuk -> LoginActivity, Daftar -> RegisterActivity, Google -> belum tersedia.
 */
class WelcomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Sesi masih ada (token tersimpan): langsung ke aplikasi.
        if (ServiceLocator.tokenStore.hasToken()) {
            startActivity(SessionRouter.homeIntent(this))
            finish()
            return
        }

        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarInsetsAsPadding()

        binding.btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        binding.btnGoogle.setOnClickListener {
            Snackbar.make(binding.root, R.string.feature_coming_soon, Snackbar.LENGTH_SHORT).show()
        }
    }
}
