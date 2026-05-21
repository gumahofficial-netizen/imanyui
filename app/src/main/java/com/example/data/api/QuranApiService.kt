package com.example.data.api

import com.example.data.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface QuranApiService {

    @GET("surahs")
    suspend fun getSurahs(): List<SurahDto>

    // Fetches ayahs by surah index (1 to 114)
    @GET("ayah")
    suspend fun getAyahsBySurah(@Query("number") surahNumber: Int): List<AyahDto>

    @GET("reciters")
    suspend fun getReciters(): RecitersDtoResponse

    @GET("reciterAudio")
    suspend fun getReciterAudio(@Query("reciter_id") reciterId: Int): ReciterAudioDtoResponse

    @GET("azkar")
    suspend fun getAzkar(): AzkarDtoResponse

    @GET("duas")
    suspend fun getDuas(): DuasDtoResponse

    @GET("laylatAlQadr")
    suspend fun getLaylatAlQadr(): LaylatAlQadrDtoResponse

    companion object {
        private const val BASE_URL = "https://quran.yousefheiba.com/api/"

        private val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        private val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        val instance: QuranApiService by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(QuranApiService::class.java)
        }
    }
}
