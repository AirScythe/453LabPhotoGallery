package com.bignerdranch.android.photogallery

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class DAPhotoGalleryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daphoto_gallery)

        val daIsFragmentContainerEmpty = savedInstanceState == null
        if (daIsFragmentContainerEmpty) {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragmentContainer, DAPhotoGalleryFragment.newInstance())
                .commit()
        }
    }

    companion object {
        fun daNewIntent(context: Context): Intent {
            return Intent(context, DAPhotoGalleryActivity::class.java)
        }
    }
}