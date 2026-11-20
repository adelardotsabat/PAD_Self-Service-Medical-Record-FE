package com.example.selfmedicalrecord.ui.auth

import android.content.Context
import android.content.Intent
import com.example.selfmedicalrecord.MainActivity
import com.example.selfmedicalrecord.data.ServiceLocator
import com.example.selfmedicalrecord.ui.admin.AdminDashboardActivity

/** Menentukan layar tujuan setelah login / saat aplikasi dibuka, berdasarkan peran akun. */
object SessionRouter {

    private const val NEW_ROOT = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

    /** ADMIN -> dashboard admin, selain itu -> aplikasi pasien. */
    fun homeIntent(context: Context): Intent {
        val target = if (ServiceLocator.tokenStore.isAdmin()) {
            AdminDashboardActivity::class.java
        } else {
            MainActivity::class.java
        }
        return Intent(context, target).addFlags(NEW_ROOT)
    }

    fun loggedOutIntent(context: Context): Intent =
        Intent(context, WelcomeActivity::class.java).addFlags(NEW_ROOT)
}
