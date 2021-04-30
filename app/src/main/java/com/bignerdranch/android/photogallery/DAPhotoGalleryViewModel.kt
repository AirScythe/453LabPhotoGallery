package com.bignerdranch.android.photogallery

import android.app.Application
import androidx.lifecycle.*

class DAPhotoGalleryViewModel(private val app: Application) : AndroidViewModel(app) {

    val galleryItemLiveData: LiveData<List<DAGalleryItem>>

    private val daFlickrFetchr = DAFlickrFetchr()
    private val daMutableSearchTerm = MutableLiveData<String>()

    val searchTerm: String
        get() = daMutableSearchTerm.value ?: ""

    init {
        daMutableSearchTerm.value = DAQueryPreferences.daGetStoredQuery(app)

        galleryItemLiveData =
            Transformations.switchMap(daMutableSearchTerm) { searchTerm ->
                if (searchTerm.isBlank()) { daFlickrFetchr.daFetchPhotos()
                } else {
                    daFlickrFetchr.daSearchPhotos(searchTerm)
                }
            }
    }

    fun daFetchPhotos(query: String = "") {
        DAQueryPreferences.daSetStoredQuery(app, query)
        daMutableSearchTerm.value = query
    }
}