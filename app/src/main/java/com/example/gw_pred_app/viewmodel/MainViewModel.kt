package com.example.gw_pred_app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.usecase.NaverCloudUsecase
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

}