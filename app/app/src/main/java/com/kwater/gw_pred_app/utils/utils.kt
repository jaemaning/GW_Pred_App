package com.kwater.gw_pred_app.utils

object utils {
    const val BASE_URL = ""
    const val NAVER_CLOUD_URL = "https://kr.object.ncloudstorage.com"
}


fun splitMarkerName(markerName: String): Pair<String, String> {
    val parts = markerName.split("_")
    val firstPart = parts.getOrNull(0) ?: ""
    val secondPart = parts.getOrNull(1) ?: ""
    return Pair(firstPart, secondPart)
}