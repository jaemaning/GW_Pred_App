package com.kwater.gw_pred_app.di

import android.app.Application
import android.util.Log
import com.kwater.data.repository.DataSource
import com.naver.maps.map.NaverMapSdk
import dagger.hilt.android.HiltAndroidApp
import com.kwater.gw_pred_app.BuildConfig


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
        // Log the client ID being used
        Log.d("App", "NaverMapClient ID: ${BuildConfig.naverMapClient_id}")
        NaverMapSdk.getInstance(this).client =
            NaverMapSdk.NaverCloudPlatformClient(BuildConfig.naverMapClient_id)
        // Check if the client is set correctly
        Log.d("App", "NaverMapSdk client set.")
    }
}