package com.example.selfmedicalrecord.ui.common

import android.content.res.ColorStateList
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.selfmedicalrecord.R
import com.example.selfmedicalrecord.domain.health.StatusTone

@ColorRes
fun StatusTone.textColorRes(): Int = when (this) {
    StatusTone.GOOD -> R.color.badge_green_text
    StatusTone.CAUTION -> R.color.badge_amber_text
    StatusTone.ALERT -> R.color.badge_red_text
}

@ColorRes
fun StatusTone.backgroundColorRes(): Int = when (this) {
    StatusTone.GOOD -> R.color.badge_green_bg
    StatusTone.CAUTION -> R.color.badge_amber_bg
    StatusTone.ALERT -> R.color.badge_red_bg
}

/**
 * Mewarnai badge (latar + titik + teks) sesuai [tone] dan menampilkannya.
 * Latar dan titik memakai drawable hijau yang sama, hanya diberi tint.
 */
fun bindStatusBadge(badge: View, dot: View, label: TextView, tone: StatusTone, text: CharSequence) {
    val context = badge.context
    val textColor = ContextCompat.getColor(context, tone.textColorRes())
    badge.backgroundTintList =
        ColorStateList.valueOf(ContextCompat.getColor(context, tone.backgroundColorRes()))
    dot.backgroundTintList = ColorStateList.valueOf(textColor)
    label.setTextColor(textColor)
    label.text = text
    badge.isVisible = true
}
