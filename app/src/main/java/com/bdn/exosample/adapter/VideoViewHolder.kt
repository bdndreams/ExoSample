package com.bdn.exosample.adapter

import android.net.Uri
import android.provider.MediaStore
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bdn.exosample.R
import com.bdn.exosample.databinding.LayoutMediaListItemBinding
import com.bdn.exosample.viewmodel.MainActivityViewModel
import com.bumptech.glide.Glide
import java.io.File

class VideoViewHolder(private val parent: View, val viewModel:MainActivityViewModel) : RecyclerView.ViewHolder(parent) {
    val videoImage: ImageView
    val bookMark: ImageView
    private val title: TextView
    var playerContainer: FrameLayout? = null
    var isBookMark:Boolean = false
    private val binding = LayoutMediaListItemBinding.bind(parent)

    fun onBind(position: Int) {
        parent.tag = this
        title.text = "position $position"
        bookMark.setOnClickListener {
            viewModel.setBookMark(position)
            setBookMarkItem(!isBookMark)
        }
        var index =
            viewModel.cursorLoader?.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME) ?: -1
        viewModel.cursorLoader?.moveToPosition(position)

        val name = viewModel?.cursorLoader?.getString(index) ?: ""
        name?.let {
            title.text = "$it"

        }


        index =
            viewModel.cursorLoader?.getColumnIndexOrThrow(MediaStore.Video.Media.DATA) ?: -1
        viewModel.cursorLoader?.moveToPosition(position)

        val mediaUrl = viewModel?.cursorLoader?.getString(index) ?: ""
        val isBookMark = viewModel.bookMarkMap[mediaUrl]

        setBookMarkItem(isBookMark)
        Glide.with(parent.context)
            .load(Uri.fromFile(File(mediaUrl)))
            .placeholder(R.drawable.exo_edit_mode_logo)
            .into(videoImage);


    }

    private fun setBookMarkItem(it:Boolean?){
        if(it == true){
            isBookMark = true
            bookMark.setImageResource(R.drawable.bookmark)
        }else{
            isBookMark = false
            bookMark.setImageResource(R.drawable.bookmark_border)
        }
    }
    init {
        videoImage = binding.imageView
        title = binding.tvTitle
        playerContainer = binding.playerContainer
        bookMark = binding.bookmark
    }
}