package com.example.selfmedicalrecord.utils

import android.graphics.Color
import android.os.Build
import android.view.View

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