package com.swiftbrowser.fast.secure.data.remote.api

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Url

interface ApiService {
    @GET
    suspend fun fetchRss(@Url url: String): ResponseBody
}
