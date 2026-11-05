package com.example.selfmedicalrecord.ui.admin

import android.content.res.ColorStateList
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.selfmedicalrecord.data.ServiceLocator

/**
 * Dasar semua layar admin. Panggil [ensureAdmin] di awal onCreate; bila akun bukan admin,
 * layar langsung ditutup. Ini hanya lapis kenyamanan di UI: yang benar-benar melindungi
 * data admin adalah pemeriksaan peran di backend (middleware EnsureUserHasRole).
 */
abstract class AdminActivity : AppCompatActivity() {

    protected fun ensureAdmin(): Boolean {
        if (ServiceLocator.tokenStore.isAdmin()) return true
        finish()
        return false
    }
}

/** Mengisi lencana (pill) dengan teks dan warna latar/teks dari resource warna. */
fun TextView.bindBadge(text: CharSequence, @ColorRes background: Int, @ColorRes foreground: Int) {
    this.text = text
    setTextColor(ContextCompat.getColor(context, foreground))
    backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, background))
}
