package com.bdn.exosample

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearSnapHelper
import com.bdn.exosample.adapter.VideoListAdapter
import com.bdn.exosample.common.viewModelFactory
import com.bdn.exosample.databinding.ActivityMainBinding
import com.bdn.exosample.viewmodel.MainActivityViewModel

class MainActivity : AppCompatActivity() {
    private var firstTime: Boolean = true
    private lateinit var mMainBinding: ActivityMainBinding
    private lateinit var mMainViewModel: MainActivityViewModel
    private lateinit var mVideoAdapter: VideoListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mMainViewModel = ViewModelProvider(
            this,
            viewModelFactory {
                MainActivityViewModel(application)
            }).get(MainActivityViewModel::class.java)

        mVideoAdapter = VideoListAdapter(mMainViewModel)

        mMainBinding.rvVideoList.viewModel = mMainViewModel
        mMainBinding.rvVideoList.adapter = mVideoAdapter
        LinearSnapHelper().attachToRecyclerView(mMainBinding.rvVideoList)

        if (firstTime) {
            Handler(Looper.getMainLooper()).post { mMainBinding.rvVideoList.playVideo(false) }
            firstTime = false
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onDestroy() {
        mMainBinding.rvVideoList.releasePlayer()
        super.onDestroy()
    }
}