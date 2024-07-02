package com.example.gw_pred_app.di

import com.example.domain.repository.NaverCloudRepository
import com.example.domain.usecase.NaverCloudUsecase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class UsecaseModule {
    @Provides
    @Singleton
    fun provideNaverCloudUseCase(repository: NaverCloudRepository) = NaverCloudUsecase(repository)
}