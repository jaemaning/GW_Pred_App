package com.example.gw_pred_app.di

import android.app.Application
import com.example.data.repository.DataSource
import com.naver.maps.map.NaverMapSdk
import dagger.hilt.android.HiltAndroidApp
import com.example.gw_pred_app.BuildConfig

@HiltAndroidApp
class App : Application() {
    companion object {
        private lateinit var application: App
        fun getInstance(): App = application

        lateinit var prefs: DataSource
    }
    override fun onCreate() {
        super.onCreate()
        application = this
        prefs = DataSource(applicationContext)
        NaverMapSdk.getInstance(this).client =
            NaverMapSdk.NaverCloudPlatformClient(BuildConfig.naverMapClient_id)
    }
}