package com.bignerdranch.android.photogallery.api

import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

private const val API_KEY = "057ad3fffd9d8c0dd73d4c4959ebbc85"

class DAPhotoInterceptor : Interceptor {
    override fun intercept(daChain: Interceptor.Chain): Response {
        val daOriginalRequest: Request = daChain.request()
        val daNewUrl: HttpUrl = daOriginalRequest.url().newBuilder()
            .addQueryParameter("api_key", API_KEY)
            .addQueryParameter("format", "json")
            .addQueryParameter("nojsoncallback", "1")
            .addQueryParameter("extras", "url_s")
            .addQueryParameter("safesearch", "1")
            .build()

        val daNewRequest: Request = daOriginalRequest.newBuilder()
            .url(daNewUrl) .build()

        return daChain.proceed(daNewRequest)
    }
}