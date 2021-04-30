package com.bignerdranch.android.photogallery.api

import com.bignerdranch.android.photogallery.DAGalleryItem
import com.google.gson.annotations.SerializedName

class DAPhotoResponse {
    @SerializedName("photo")
    lateinit var daGalleryItems: List<DAGalleryItem>
}