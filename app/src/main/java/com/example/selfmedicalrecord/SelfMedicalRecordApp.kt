package com.example.selfmedicalrecord

import android.app.Application
import com.example.selfmedicalrecord.data.ServiceLocator

class SelfMedicalRecordApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
    }
}
