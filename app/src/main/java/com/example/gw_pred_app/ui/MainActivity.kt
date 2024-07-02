package com.example.gw_pred_app.ui

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
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
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.ImageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.gw_pred_app.viewmodel.MainViewModel
import com.example.gw_pred_app.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

data class MarkerInfo(val position: LatLng, val caption: String)

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
                    topBar = { TopAppBar(title = { Greeting(name = "제주도 지하수위 예측 앱") }) },
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

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "$name",
        modifier = modifier
    )
}

@OptIn(ExperimentalNaverMapApi::class)
@Composable
fun NaverMapContent(imageLoader: ImageLoader) {
    val markers = listOf(
        MarkerInfo(LatLng(33.364805, 126.542671), "제주도"),
        MarkerInfo(LatLng(33.5, 126.5), "위치 1"),
        MarkerInfo(LatLng(33.4, 126.6), "위치 2"),
        MarkerInfo(LatLng(33.3, 126.4), "위치 3"),
        // 필요한 만큼 추가...
    )

    val cameraPositionState: CameraPositionState = rememberCameraPositionState {
        position = CameraPosition(markers[0].position, 8.0)
    }

    var mapProperties by remember {
        mutableStateOf(
            MapProperties(
                maxZoom = 15.0,
                minZoom = 7.0,
            )
        )
    }
    var mapUiSettings by remember {
        mutableStateOf(
            MapUiSettings(isLocationButtonEnabled = false)
        )
    }

    val markerClicks = remember { mutableStateOf(Array(markers.size) { false }) }

    val viewModel: MainViewModel = hiltViewModel()
    val context = LocalContext.current
    val naverResult by viewModel.naverCloudGetResponse.observeAsState()


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(16.dp)
    ) {
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
                        onClick = {
                            markerClicks.value[index] = true
                            true
                        }
                    )
                }
                CircleOverlay(
                    center = markers[0].position, radius = 10000.0, // 10km
                    color = Color(0, 0, 255, 0) // 반투명한 파란색
                )
            }
        }
        Spacer(modifier = Modifier.padding(24.dp))
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data("https://kr.object.ncloudstorage.com/gw-predict/240701/test.png")
                .memoryCachePolicy(CachePolicy.DISABLED)
                .addHeader("Host", "kr.object.ncloudstorage.com")
                .crossfade(true)
                .build(),
            contentScale = ContentScale.Crop,
            contentDescription = null,
            imageLoader = imageLoader,
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape),
            error = painterResource(id = R.drawable.error)
        )
    }
}
