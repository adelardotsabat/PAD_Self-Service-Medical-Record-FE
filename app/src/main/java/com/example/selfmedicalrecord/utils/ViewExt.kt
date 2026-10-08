package com.example.selfmedicalrecord.utils

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.example.selfmedicalrecord.R

/**
 * Extension function untuk menerapkan soft shadow ala Figma
 */
fun View.applyFigmaShadow(elevationDp: Float = 6f) {
    this.elevation = elevationDp * resources.displayMetrics.density

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        // #33000000 (20% Alpha) -> Hasil render visual di Android setara soft shadow 6% Figma
        val figmaShadowColor = Color.parseColor("#33000000")
        this.outlineAmbientShadowColor = figmaShadowColor
        this.outlineSpotShadowColor = figmaShadowColor
    }
}

/**
 * Menambahkan tinggi status bar, navigation bar, cutout, dan keyboard ke padding view ini,
 * di atas padding yang sudah ada di XML. Dipakai di root layar karena app edge-to-edge.
 */
fun View.applySystemBarInsetsAsPadding() {
    val startLeft = paddingLeft
    val startTop = paddingTop
    val startRight = paddingRight
    val startBottom = paddingBottom

    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val insets = windowInsets.getInsets(
            WindowInsetsCompat.Type.systemBars() or
                WindowInsetsCompat.Type.displayCutout() or
                WindowInsetsCompat.Type.ime()
        )
        view.updatePadding(
            left = startLeft + insets.left,
            top = startTop + insets.top,
            right = startRight + insets.right,
            bottom = startBottom + insets.bottom
        )
        windowInsets
    }
}

/** Mengisi teks dari string resource lalu menambahkan tanda * merah sebagai penanda wajib diisi. */
fun TextView.setRequiredLabel(@StringRes textRes: Int) {
    val requiredColor = ContextCompat.getColor(context, R.color.required_red)
    text = SpannableStringBuilder(context.getString(textRes)).append(
        "*",
        ForegroundColorSpan(requiredColor),
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
}

/** Menampilkan pesan dari string resource, atau menyembunyikan view jika [textRes] null. */
fun TextView.showMessage(@StringRes textRes: Int?) {
    if (textRes == null) {
        visibility = View.GONE
    } else {
        setText(textRes)
        visibility = View.VISIBLE
    }
}

fun Activity.hideKeyboard() {
    val focused = currentFocus ?: return
    getSystemService(InputMethodManager::class.java)?.hideSoftInputFromWindow(focused.windowToken, 0)
}
