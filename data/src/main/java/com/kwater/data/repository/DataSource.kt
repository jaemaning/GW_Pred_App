package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DataSource @Inject constructor(
    @ApplicationContext context: Context,
) {
    companion object {
        private const val ACCESS_TOKEN = "access_token"
        private const val NCP_AUTH_HEADER = "ncp_auth_header"
        private const val NCP_TIME_STAMP = "ncp_time_stamp"
        private const val NCP_PAYLOAD_HASH = "ncp_payload_hash"
    }
    private fun getTokenPreference(context: Context) : SharedPreferences {
        return context.getSharedPreferences(ACCESS_TOKEN, Context.MODE_PRIVATE)
    }
    private val prefs by lazy { getTokenPreference(context) }
    private val editor by lazy { prefs.edit() }
    private val gson = Gson()

    private fun putString(key: String, data: String?) {
        editor.putString(key, data)
        editor.apply()
    }

    private fun putBoolean(key: String, data: Boolean) {
        editor.putBoolean(key, data)
        editor.apply()
    }

    private fun putInt(key: String, data: Int) {
        editor.putInt(key, data)
        editor.apply()
    }

    private fun putFloat(key: String, data: Float) {
        editor.putFloat(key, data)
        editor.apply()
    }

    private fun putLong(key: String, data: Long) {
        editor.putLong(key, data)
        editor.apply()
    }

    private fun getString(key: String, defValue: String? = null) : String? {
        return prefs.getString(key, defValue)
    }

    private fun getBoolean(key: String, defValue: Boolean = false) : Boolean {
        return prefs.getBoolean(key, defValue)
    }

    private fun getInt(key: String, defValue: Int = 0) : Int {
        return prefs.getInt(key, defValue)
    }

    private fun getFloat(key: String, defValue: Float = 0f) : Float {
        return prefs.getFloat(key, defValue)
    }

    private fun getLong(key: String, defValue: Long = 0) : Long {
        return prefs.getLong(key, defValue)
    }

    private fun getObject(key: String, defValue: Any) : Any {
        val json = getString(key, null)
        return if (json == null) {
            defValue
        } else {
            gson.fromJson(json, defValue::class.java)
        }
    }


    fun putNcpAuthHeader(authorizationHeader: String?) {
        putString(NCP_AUTH_HEADER, authorizationHeader)
    }
    fun putNcpTimeStamp(timeStamp: String?) {
        putString(NCP_TIME_STAMP, timeStamp)
    }
    fun putNcpPayloadHash(payloadHash: String?) {
        putString(NCP_PAYLOAD_HASH, payloadHash)
    }
    fun getNcpAuthHeader() : String? {
        return getString(NCP_AUTH_HEADER)
    }
    fun getNcpTimeStamp() : String? {
        return getString(NCP_TIME_STAMP)
    }
    fun getNcpPayloadHash() : String? {
        return getString(NCP_PAYLOAD_HASH)
    }

    // 토큰 부분
    fun putToken(token: String?) {
        putString(ACCESS_TOKEN, token)
    }
    fun getToken(): String? {
        return getString(ACCESS_TOKEN)
    }
    fun removeToken() {
        putString(ACCESS_TOKEN, null)
    }
}