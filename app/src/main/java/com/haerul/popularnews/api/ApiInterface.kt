package com.haerul.popularnews.api

import retrofit2.Retrofit
import com.haerul.popularnews.api.ApiClient
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import kotlin.Throws
import retrofit2.http.GET
import com.haerul.popularnews.models.News
import retrofit2.Call
import retrofit2.http.Query

interface ApiInterface {
    @GET("top-headlines")
    fun getNews(
        @Query("country") country: String?,
        @Query("apiKey") apiKey: String?
    ): Call<News?>?

    @GET("everything")
    fun getNewsSearch(
        @Query("q") keyword: String?,
        @Query("language") language: String?,
        @Query("sortBy") sortBy: String?,
        @Query("apiKey") apiKey: String?
    ): Call<News?>?
}