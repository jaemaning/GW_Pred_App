package com.kwater.gw_pred_app.di

import android.content.Context
import android.util.Log
import coil.ImageLoader
import coil.request.CachePolicy
import coil.util.DebugLogger
import com.kwater.data.remote.api.NaverCloudApi
import com.kwater.gw_pred_app.BuildConfig
import com.kwater.gw_pred_app.utils.ObjectStorageInterceptor
import com.kwater.gw_pred_app.utils.utils.NAVER_CLOUD_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    // Naver cloud 활용 network module 부분
    @Provides
    @Singleton
    fun provideObjectStorageInterceptor(): ObjectStorageInterceptor {
        val accessKey = BuildConfig.naverAccess_key
        val secretKey = BuildConfig.naverSecret_key
        val region = "kr-standard"
        return ObjectStorageInterceptor(accessKey, secretKey, region)
    }

    @Provides
    @Singleton
    fun okHttpClientWithObjectStorage(objectStorageInterceptor: ObjectStorageInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .readTimeout(10, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(objectStorageInterceptor) // ObjectStorageInterceptor 추가
            .build()
    }

    // 기존 Retrofit 제공 함수를 ObjectStorageInterceptor를 포함하도록 수정
    @Provides
    @Singleton
    @Named("objectStorageRetrofit")
    fun retrofitWithObjectStorage(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(NAVER_CLOUD_URL)
            .client(okHttpClientWithObjectStorage(provideObjectStorageInterceptor()))
            .addConverterFactory(ScalarsConverterFactory.create()) // 문자열 처리
            .addConverterFactory(GsonConverterFactory.create()) // 객체 Json
            .build()
    }

    @Provides
    @Singleton
    fun provideNaverStorageApi(@Named("objectStorageRetrofit") retrofitWithObjectStorage: Retrofit): NaverCloudApi {
        return retrofitWithObjectStorage.create(NaverCloudApi::class.java)
    }

    @Provides
    @Singleton
    fun provideImageLoader(@ApplicationContext context: Context): ImageLoader {
        val objectStorageInterceptor = provideObjectStorageInterceptor()

        // OkHttp 클라이언트를 구성합니다. 이 때, 로깅 인터셉터와 ObjectStorageInterceptor를 추가합니다.
        val naverOkHttpClient = OkHttpClient.Builder()
            .addInterceptor(objectStorageInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

        val imageLoader = ImageLoader.Builder(context)
            .logger(DebugLogger())
            .diskCachePolicy(CachePolicy.DISABLED)
            .okHttpClient(naverOkHttpClient)
            .build()

        return imageLoader
    }

    // HTML 인터셉터
    @Provides
    @Singleton
    fun provideHtmlClient(objectStorageInterceptor: ObjectStorageInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(objectStorageInterceptor)
            .build()
    }

    @Provides
    @Singleton
    @Named("htmlRetrofit")
    fun provideHtmlRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(NAVER_CLOUD_URL)
            .client(client)
            .addConverterFactory(ScalarsConverterFactory.create()) // HTML 데이터를 문자열로 변환
            .build()
    }

}