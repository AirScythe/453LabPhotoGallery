package com.bignerdranch.android.photogallery.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface DAFlickrApi {
    @GET("services/rest?method=flickr.interestingness.getList")
    fun daFetchPhotos(): Call<DAFlickrResponse>

    @GET
    fun daFetchUrlBytes(@Url url: String): Call<ResponseBody>

    @GET("services/rest?method=flickr.photos.search")
    fun searchPhotos(@Query("text") query: String): Call<DAFlickrResponse>
}