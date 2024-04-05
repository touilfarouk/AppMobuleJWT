package com.plcoding.jwtauthktorandroid.di

import android.app.Application
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.plcoding.jwtauthktorandroid.auth.AuthApi
import com.plcoding.jwtauthktorandroid.auth.AuthRepository
import com.plcoding.jwtauthktorandroid.auth.AuthRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import java.net.URL
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val IP_URL = ""

    @Provides
    @Singleton
    fun provideAuthApi(): AuthApi {
        val baseUrl = runBlocking { fetchDynamicIP() }
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create()
    }

    @Provides
    @Singleton
    fun provideSharedPref(app: Application): SharedPreferences {
        return app.getSharedPreferences("prefs", MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(api: AuthApi, prefs: SharedPreferences): AuthRepository {
        return AuthRepositoryImpl(api, prefs)
    }

    private suspend fun fetchDynamicIP(): String {
        return try {
            var ip = "192.168.0.2" // Default IP if fetching fails
            GlobalScope.launch(Dispatchers.IO) {
                val jsonString = URL(IP_URL).readText()
                val regex = """(?<="ip":")[\d.]+""".toRegex()
                ip = regex.find(jsonString)?.value ?: throw Exception("IP not found")
            }.join() // Wait for the coroutine to finish
            "http://$ip:3000/"
        } catch (e: Exception) {
            "http://192.168.0.2:3000/" // Default IP if fetching fails
        }
    }

}
