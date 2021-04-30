package com.bignerdranch.android.photogallery

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.*
import java.util.concurrent.TimeUnit

private const val TAG = "PhotoGalleryFragment"
private const val POLL_WORK = "POLL_WORK"

class DAPhotoGalleryFragment : DAVisibleFragment() {
    private lateinit var daPhotoRecyclerView: RecyclerView
    private lateinit var daPhotoGalleryViewModel: DAPhotoGalleryViewModel
    private lateinit var daThumbnailDownloader: DAThumbnailDownloader<DAPhotoHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        retainInstance = true
        setHasOptionsMenu(true)

        daPhotoGalleryViewModel =
            ViewModelProviders.of(this).get(DAPhotoGalleryViewModel::class.java)

        val responseHandler = Handler()
        daThumbnailDownloader =
            DAThumbnailDownloader(responseHandler) { daPhotoHolder, bitmap ->
                val daDrawable = BitmapDrawable(resources, bitmap)
                daPhotoHolder.bindDrawable(daDrawable)
            }
        lifecycle.addObserver(daThumbnailDownloader.fragmentLifecycleObserver)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewLifecycleOwner.lifecycle.removeObserver(
            daThumbnailDownloader.viewLifecycleObserver
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycle.removeObserver(
            daThumbnailDownloader.fragmentLifecycleObserver
        )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.da_fragment_photo_gallery, menu)

        val daSearchItem: MenuItem = menu.findItem(R.id.menu_item_search)
        val daSearchView = daSearchItem.actionView as SearchView

        daSearchView.apply {
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(queryText: String): Boolean {
                    Log.d(TAG, "QueryTextSubmit: $queryText")
                    daPhotoGalleryViewModel.daFetchPhotos(queryText)
                    return true
                }

                override fun onQueryTextChange(queryText: String): Boolean {
                    Log.d(TAG, "QueryTextChange: $queryText")
                    return false
                }
            })

            setOnSearchClickListener {
                daSearchView.setQuery(daPhotoGalleryViewModel.searchTerm, false)
            }
        }

        val daToggleItem = menu.findItem(R.id.menu_item_toggle_polling)
        val daIsPolling = DAQueryPreferences.daIsPolling(requireContext())
        val daToggleItemTitle = if (daIsPolling) {
            R.string.stop_polling
        } else {
            R.string.start_polling
        }
        daToggleItem.setTitle(daToggleItemTitle)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_clear -> {
                daPhotoGalleryViewModel.daFetchPhotos("")
                true
            }

            R.id.menu_item_toggle_polling -> {
                val daIsPolling = DAQueryPreferences.daIsPolling(requireContext())
                if (daIsPolling) {
                    WorkManager.getInstance().cancelUniqueWork(POLL_WORK)
                    DAQueryPreferences.daSetPolling(requireContext(), false)
                } else {
                    val daConstraints = Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.UNMETERED)
                        .build()
                    val daPeriodicRequest = PeriodicWorkRequest
                        .Builder(DAPollWorker::class.java, 15, TimeUnit.MINUTES)
                        .setConstraints(daConstraints)
                        .build()
                    WorkManager.getInstance().enqueueUniquePeriodicWork(POLL_WORK,
                        ExistingPeriodicWorkPolicy.KEEP,
                        daPeriodicRequest)
                    DAQueryPreferences.daSetPolling(requireContext(), true)
                }
                activity?.invalidateOptionsMenu()
                return true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewLifecycleOwner.lifecycle.addObserver(
            daThumbnailDownloader.viewLifecycleObserver
        )

        val daView = inflater.inflate(R.layout.da_fragment_photo_gallery, container, false)

        daPhotoRecyclerView = daView.findViewById(R.id.photo_recycler_view)
        daPhotoRecyclerView.layoutManager = GridLayoutManager(context, 3)

        return daView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        daPhotoGalleryViewModel.galleryItemLiveData.observe(
            viewLifecycleOwner,
            Observer { galleryItems ->
                daPhotoRecyclerView.adapter = DAPhotoAdapter(galleryItems)
            })
    }

    private class DAPhotoHolder(itemImageView: ImageView)
        : RecyclerView.ViewHolder(itemImageView) {
        val bindDrawable: (Drawable) -> Unit = itemImageView::setImageDrawable
    }

    private inner class DAPhotoAdapter(private val galleryItems: List<DAGalleryItem>)
        : RecyclerView.Adapter<DAPhotoHolder>() {

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): DAPhotoHolder {
            val view = layoutInflater.inflate(
                R.layout.da_list_item_gallery,
                parent,
                false
            ) as ImageView
            return DAPhotoHolder(view)
        }
        override fun getItemCount(): Int = galleryItems.size
        override fun onBindViewHolder(holder: DAPhotoHolder, position: Int) {
            val galleryItem = galleryItems[position]
            val placeholder: Drawable = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.bill_up_close
            ) ?: ColorDrawable()
            holder.bindDrawable(placeholder)

            daThumbnailDownloader.daQueueThumbnail(holder, galleryItem.url)
        }
    }

    companion object {
        fun newInstance() = DAPhotoGalleryFragment()
    }
}