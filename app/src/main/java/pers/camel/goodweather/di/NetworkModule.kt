package pers.camel.goodweather.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import pers.camel.goodweather.api.QWeatherService
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object NetworkModule {
    @Singleton
    @Provides
    fun provideQWeatherService(@ApplicationContext context: Context): QWeatherService {
        return QWeatherService(context)
    }
}