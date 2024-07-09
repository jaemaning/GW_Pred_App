package com.kwater.gw_pred_app.di

import com.kwater.data.remote.api.NaverCloudApi
import com.kwater.data.repository.NaverCloudRepositoryImpl
import com.kwater.domain.repository.NaverCloudRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {
    @Provides
    @Singleton
    fun provideNaverCloudRepository(api: NaverCloudApi) : NaverCloudRepository {
        return NaverCloudRepositoryImpl(api)
    }
}