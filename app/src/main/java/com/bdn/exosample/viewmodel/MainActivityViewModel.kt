package com.bdn.exosample.viewmodel

import android.app.Application
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.loader.content.CursorLoader
import com.bdn.exosample.database.BookMarkDatabase
import com.bdn.exosample.database.dao.BookMarkDao
import com.bdn.exosample.database.table.BookMarkVideo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivityViewModel(application: Application): ViewModel()  {

    private val mApplication = application
    val cursorLoader: Cursor?
    var count = 0
    val bookMarkDao : BookMarkDao;
    val bookMarkMap = HashMap<String, Boolean>()

    init {
        bookMarkDao = BookMarkDatabase.getDatabase(mApplication).bookMarkDao()
        val proj = arrayOf(MediaStore.Video.Media._ID, MediaStore.Video.Media.DATA,
                    MediaStore.Video.Media.DISPLAY_NAME)
        cursorLoader = CursorLoader(mApplication, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, proj,
        null, null, null).loadInBackground()
        count = cursorLoader?.count?:0
        getBookMarkData()
    }

    private fun getBookMarkData(){
        viewModelScope.launch{
            withContext(Dispatchers.IO){
                val list = bookMarkDao.getAllBookMarkVideo()
                list.forEach{
                    it.path?.let{path->
                        bookMarkMap[path] = it.isBookMark?:false
                    }

                }
            }
        }
    }

    fun getVideos() {
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                val uri: Uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                val projection =
                    arrayOf(MediaStore.Video.VideoColumns.DATA)
                val c: Cursor? = mApplication.contentResolver.query(uri, projection, null, null, null)
                var vidsCount = 0
                if (c != null) {
                    vidsCount = c.count
                    while (c.moveToNext()) {
                        Log.d("VIDEO", c.getString(0))
                    }
                    c.close()
                }
            }
        }

    }

    fun setBookMark(position: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                val index =
                    cursorLoader?.getColumnIndexOrThrow(MediaStore.Video.Media.DATA) ?: -1
                cursorLoader?.moveToPosition(position)

                val mediaUrl = cursorLoader?.getString(index) ?: ""
                val item = bookMarkDao.getBookMarkVideo(mediaUrl)
                if(item == null){
                    val video = BookMarkVideo(path = mediaUrl, isBookMark = true)
                    bookMarkDao.insert(video)
                    bookMarkMap[mediaUrl] = true
                }else {
                    item.let {
                        it.isBookMark = !(it.isBookMark?:false)
                        bookMarkDao.update(it)
                        it.path?.let { itemData ->
                            bookMarkMap[itemData] = it.isBookMark ?: false
                        }

                    }
                }
            }

        }
    }
}