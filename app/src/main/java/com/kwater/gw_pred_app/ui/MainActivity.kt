package com..gw_pred_app.ui

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.gw_pred_app.ui.theme.GW_Pred_AppTheme
import com.naver.maps.map.compose.ExperimentalNaverMapApi
import com.naver.maps.map.compose.MapProperties
import com.naver.maps.map.compose.MapUiSettings
import com.naver.maps.map.compose.NaverMap
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import com.naver.maps.map.compose.rememberCameraPositionState
import com.naver.maps.map.compose.CameraPositionState
import com.naver.maps.map.CameraPosition
import com.naver.maps.geometry.LatLng
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.naver.maps.map.compose.CircleOverlay
import com.naver.maps.map.compose.Marker
import com.naver.maps.map.compose.MarkerState
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import coil.compose.AsyncImage
import coil.ImageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.request.ImageResult
import com.example.data.remote.api.NaverCloudApi
import com.example.gw_pred_app.viewmodel.MainViewModel
import com.example.gw_pred_app.R
import com.example.gw_pred_app.utils.splitMarkerName
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class MarkerInfo(val position: LatLng, val caption: String, val subcaption: String, val fileName: String)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var imageLoader: ImageLoader

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GW_Pred_AppTheme {
                Scaffold(
                    topBar = { TopAppBar(title = { Text("지하수위 예측 앱 In Jeju") }) },
                    content = { innerPadding ->
                        Box(
                            modifier = Modifier
                                .padding(innerPadding)
                                .fillMaxWidth()
                                .fillMaxHeight()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                NaverMapContent(imageLoader)
                            }
                        }
                        Spacer(modifier = Modifier.padding(24.dp))
                    }
                )
            }
        }
    }
}


@OptIn(ExperimentalNaverMapApi::class)
@Composable
fun NaverMapContent(imageLoader: ImageLoader) {
    val markers = listOf(
        MarkerInfo(LatLng(33.44635833, 126.4825), "제주노형", "제주", "graph_제주노형"),
        MarkerInfo(LatLng(33.27546389, 126.5668194), "제주동홍", "서귀포", "graph_제주동홍"),
        MarkerInfo(LatLng(33.322375, 126.2706417), "제주한경", "고산", "graph_제주한경"),
        MarkerInfo(LatLng(33.42769722, 126.7248361), "제주조천", "성산", "graph_제주조천"),
    )

    val cameraPositionState: CameraPositionState = rememberCameraPositionState {
        position = CameraPosition(markers[0].position, 8.0)
    }

    var mapProperties by remember {
        mutableStateOf(
            MapProperties(
                maxZoom = 15.0,
                minZoom = 5.0,
            )
        )
    }
    var mapUiSettings by remember {
        mutableStateOf(
            MapUiSettings(isLocationButtonEnabled = false)
        )
    }

    // 오늘 날짜를 "yyyyMMdd" 형식으로 가져옴
    var currentDate = remember {
        SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
    }
    // 어제 날짜를 "yyyyMMdd" 형식으로 가져옴
    val previousDate = remember {
        Calendar.getInstance().apply {
            add(Calendar.DATE, -1)
        }.time.let {
            SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(it)
        }
    }

    var markerName by remember { mutableStateOf("graph_제주노형") }
    val (_, secondPart) = splitMarkerName(markerName)
    var imageUrl by remember { mutableStateOf("https://kr.object.ncloudstorage.com/gw-predict/$currentDate/$markerName") }

    // imageUrl이 변경될 때마다 갱신하도록 LaunchedEffect 사용
    // 에러처리가 필요함
    // 만약 당일 자료가 생성되기 전이라면 전날 자료를 사용하도록 에러 처리를 진행
    LaunchedEffect(markerName) {
        val encodedFileName = URLEncoder.encode(markerName, StandardCharsets.UTF_8.toString())
        imageUrl = "https://kr.object.ncloudstorage.com/gw-predict/$currentDate/${encodedFileName}.jpg"
    }

    val viewModel: MainViewModel = hiltViewModel()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(16.dp)
    ) {
        Text(text = "지역선택")
        Spacer(modifier = Modifier.padding(3.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.4f)
        ) {
            NaverMap(
                cameraPositionState = cameraPositionState,
                properties = mapProperties,
                uiSettings = mapUiSettings,
            ) {
                markers.forEachIndexed { index, marker ->
                    Marker(
                        state = MarkerState(position = marker.position),
                        captionText = marker.caption,
                        captionTextSize = 16.sp,
                        subCaptionText = marker.subcaption,
                        subCaptionColor = Color.Blue,
                        subCaptionTextSize = 12.sp,
                        onClick = {
                            // 마커 클릭 시 처리할 작업
                            markerName = marker.fileName
                            true // 이벤트를 소비하고 전파되지 않도록 합니다.
                        }
                    )
                }
                CircleOverlay(
                    center = markers[0].position, radius = 10000.0, // 10km
                    color = Color(0, 0, 255, 0) // 반투명한 파란색
                )
            }
        }
        Spacer(modifier = Modifier.padding(5.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(3.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = secondPart)
            Text(text = currentDate)
        }
        Spacer(modifier = Modifier.padding(3.dp))
        WebViewScreen(viewModel, markerName, currentDate, previousDate)
//        AsyncImage(
//            model = ImageRequest.Builder(context)
//                .data(imageUrl)
//                .memoryCachePolicy(CachePolicy.ENABLED)
//                .addHeader("Host", "kr.object.ncloudstorage.com")
//                .crossfade(true)
//                .build(),
//            contentDescription = null,
//            imageLoader = imageLoader,
//            modifier = Modifier
//                .padding(5.dp)
//                .fillMaxWidth(),
//            error = painterResource(id = R.drawable.error),
//            onError = {
//                // 이미지 로드 실패 시 이전 날짜의 이미지를 로드
//                imageUrl = "https://kr.object.ncloudstorage.com/gw-predict/$previousDate/$markerName.jpg"
//                currentDate = previousDate
//                Log.d("errCheck", "이전날짜:${previousDate}, 오늘날짜: ${currentDate}")
//            },
//        )
    }
}


@Composable
fun WebViewScreen(viewModel: MainViewModel, markerName: String, currentDate : String, previousDate : String) {
    val htmlData by viewModel.htmlData.observeAsState()
    val context = LocalContext.current

    LaunchedEffect(markerName, currentDate) {
        viewModel.loadHtmlData("gw-predict", "${currentDate}/${markerName}.html", "${previousDate}/${markerName}.html")
    }

    AndroidView(
        factory = {
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                webViewClient = WebViewClient()
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            }
        },
        update = { webView ->
            htmlData?.let {
                Log.d("HTML_DATA", it)
                webView.loadDataWithBaseURL(null, it, "text/html", "UTF-8", null)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
