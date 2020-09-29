package com.bdn.exosample.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bdn.exosample.R
import com.bdn.exosample.model.Video
import com.bdn.exosample.viewmodel.MainActivityViewModel


class VideoListAdapter(private val vieModel: MainActivityViewModel): RecyclerView.Adapter<VideoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return VideoViewHolder(
            inflater.inflate(R.layout.layout_media_list_item, parent, false), vieModel)
    }

    override fun getItemCount() = vieModel.count

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.onBind(position)
    }

}