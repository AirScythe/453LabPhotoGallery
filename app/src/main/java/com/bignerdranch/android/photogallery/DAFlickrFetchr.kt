package com.bignerdranch.android.photogallery

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bignerdranch.android.photogallery.DAGalleryItem
import com.bignerdranch.android.photogallery.api.DAFlickrApi
import com.bignerdranch.android.photogallery.api.DAFlickrResponse
import com.bignerdranch.android.photogallery.api.DAPhotoInterceptor
import com.bignerdranch.android.photogallery.api.DAPhotoResponse
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val TAG = "FlickrFetchr"

class DAFlickrFetchr {

    private val daFlickrApi: DAFlickrApi

    init {
        val daClient = OkHttpClient.Builder()
            .addInterceptor(DAPhotoInterceptor())
            .build()

        val daRetrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://api.flickr.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(daClient)
            .build()

        daFlickrApi = daRetrofit.create(DAFlickrApi::class.java)
    }

    fun daFetchPhotosRequest(): Call<DAFlickrResponse> {
        return daFlickrApi.daFetchPhotos()
    }

    fun daFetchPhotos(): LiveData<List<DAGalleryItem>> {
        return daFetchPhotoMetadata(daFetchPhotosRequest())
    }

    fun daSearchPhotosRequest(query: String): Call<DAFlickrResponse> {
        return daFlickrApi.searchPhotos(query)
    }

    fun daSearchPhotos(query: String): LiveData<List<DAGalleryItem>> {
        return daFetchPhotoMetadata(daSearchPhotosRequest(query))
    }

    private fun daFetchPhotoMetadata(daFlickrRequest: Call<DAFlickrResponse>)
        : LiveData<List<DAGalleryItem>> {
        val daResponseLiveData: MutableLiveData<List<DAGalleryItem>> = MutableLiveData()

        daFlickrRequest.enqueue(object : Callback<DAFlickrResponse> {

            override fun onFailure(call: Call<DAFlickrResponse>, t: Throwable) {
                Log.e(TAG, "Failed to fetch photos", t)
            }
            override fun onResponse(
                call: Call<DAFlickrResponse>,
                response: Response<DAFlickrResponse>
            ) {
                Log.d(TAG, "Response received")
                val flickrResponse: DAFlickrResponse? = response.body()
                val daPhotoResponse: DAPhotoResponse? = flickrResponse?.photos
                var galleryItems: List<DAGalleryItem> = daPhotoResponse?.daGalleryItems
                    ?: mutableListOf()
                galleryItems = galleryItems.filterNot {
                    it.url.isBlank()
                }
                daResponseLiveData.value = galleryItems
            }
        })
        return daResponseLiveData
    }

    @WorkerThread
    fun fetchPhoto(url: String): Bitmap? {
        val response: Response<ResponseBody> = daFlickrApi.daFetchUrlBytes(url).execute()
        val bitmap = response.body()?.byteStream()?.use(BitmapFactory::decodeStream)
        Log.i(TAG, "Decoded bitmap=$bitmap from Response=$response")
        return bitmap
    }
}