package com.example.selfmedicalrecord.ui.admin

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.selfmedicalrecord.R
import com.example.selfmedicalrecord.databinding.DialogAdminResultBinding

/**
 * Popup hasil sebuah pengaturan (berhasil atau gagal) dengan ikon, judul, pesan, satu tombol
 * utama, dan satu tombol kedua opsional. Tombol silang dan tombol di bawah menutup popup.
 */
object ResultDialog {

    enum class Kind { SUCCESS, ERROR }

    fun show(
        context: Context,
        kind: Kind,
        @StringRes title: Int,
        @StringRes message: Int,
        @StringRes primaryText: Int,
        onPrimary: () -> Unit = {},
        @StringRes secondaryText: Int? = null,
        onSecondary: () -> Unit = {}
    ): Dialog {
        val binding = DialogAdminResultBinding.inflate(LayoutInflater.from(context))
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(binding.root)

        val (iconRes, tintRes, backgroundRes) = when (kind) {
            Kind.SUCCESS -> Triple(R.drawable.ic_check_circle, R.color.badge_green_text, R.drawable.bg_result_icon_success)
            Kind.ERROR -> Triple(R.drawable.ic_alert_circle, R.color.badge_red_text, R.drawable.bg_result_icon_error)
        }
        binding.ivIcon.setImageResource(iconRes)
        binding.ivIcon.setColorFilter(ContextCompat.getColor(context, tintRes))
        binding.ivIcon.setBackgroundResource(backgroundRes)

        binding.tvTitle.setText(title)
        binding.tvMessage.setText(message)

        binding.btnPrimary.setText(primaryText)
        binding.btnPrimary.setOnClickListener {
            dialog.dismiss()
            onPrimary()
        }

        binding.btnSecondary.isVisible = secondaryText != null
        if (secondaryText != null) {
            binding.btnSecondary.setText(secondaryText)
            binding.btnSecondary.setOnClickListener {
                dialog.dismiss()
                onSecondary()
            }
        }
        binding.btnClose.setOnClickListener { dialog.dismiss() }

        // Lebar penuh dikurangi margin 24dp di kiri-kanan; latar transparan agar sudut kartu terlihat.
        val margin = (24 * context.resources.displayMetrics.density).toInt()
        dialog.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(InsetDrawable(ColorDrawable(Color.TRANSPARENT), margin))
        }
        dialog.show()
        return dialog
    }
}
