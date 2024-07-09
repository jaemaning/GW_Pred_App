package com.kwater.gw_pred_app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kwater.domain.usecase.NaverCloudUsecase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    private val naverCloudUseCase: NaverCloudUsecase
) : ViewModel() {
    // naverCloud upload put 요청
    private val _naverCloudPutResponse = MutableLiveData<Response<Unit>>()
    val naverCloudPutResponse : LiveData<Response<Unit>> get() = _naverCloudPutResponse

    fun uploadImageToNaverCloud(requestBody: RequestBody, memberPK: Long) {
        viewModelScope.launch {
            _naverCloudPutResponse.value = naverCloudUseCase.putObject("gw-predict", "images/${memberPK}_profile.png", requestBody)
        }
    }

    // naverCloud get 요청 불러오기
    private val _naverCloudGetResponse = MutableLiveData<Response<ResponseBody>>()
    val naverCloudGetResponse : LiveData<Response<ResponseBody>> get() = _naverCloudGetResponse

    fun getToNaverCloud() {
        viewModelScope.launch {
            try {
                val response = naverCloudUseCase.getObject("gw-predict", "240701/test.png")
                _naverCloudGetResponse.postValue(response)
            } catch (e: Exception) {
                _naverCloudGetResponse.postValue(null)
                e.printStackTrace()
            }
        }
    }

    // HTML 데이터를 LiveData로 관리
    private val _htmlData = MutableLiveData<String>()
    val htmlData: LiveData<String> get() = _htmlData

    fun loadHtmlData(bucketName: String, objectKey: String, fallbackKey: String? = null) {
        viewModelScope.launch {
            val response = naverCloudUseCase.getObject(bucketName, objectKey)
            if (response.isSuccessful && response.body() != null) {
                _htmlData.postValue(response.body()?.string())
            } else if (fallbackKey != null) {
                val fallbackResponse = naverCloudUseCase.getObject(bucketName, fallbackKey)
                if (fallbackResponse.isSuccessful && fallbackResponse.body() != null) {
                    _htmlData.postValue(fallbackResponse.body()?.string())
                } else {
                    _htmlData.postValue("Error loading HTML data.")
                }
            } else {
                _htmlData.postValue("Error loading HTML data.")
            }
        }
    }
}