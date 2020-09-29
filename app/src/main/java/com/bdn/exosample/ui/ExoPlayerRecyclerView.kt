package com.bdn.exosample.ui

import android.content.Context
import android.graphics.Point
import android.net.Uri
import android.provider.MediaStore
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bdn.exosample.R
import com.bdn.exosample.adapter.VideoViewHolder
import com.bdn.exosample.viewmodel.MainActivityViewModel
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.*
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.BandwidthMeter
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import java.util.*

class ExoPlayerRecyclerView : RecyclerView {

    private var videoImage: ImageView? = null

    private var viewHolderParent: View? = null
    private var playerContainer: FrameLayout? = null
    private var videoSurfaceView: PlayerView? = null
    private var videoPlayer: SimpleExoPlayer? = null

    private var videoSurfaceDefaultHeight = 0
    private var screenDefaultHeight = 0

    private var playPosition = -1
    private var isVideoViewAdded = false
    var viewModel: MainActivityViewModel?=  null


    constructor(@NonNull context: Context) : super(context) {
        init(context)
    }

    constructor(
        @NonNull context: Context,
        @Nullable attrs: AttributeSet?
    ) : super(context, attrs) {
        init(context)
    }

    constructor(
        @NonNull context: Context,
        @Nullable attrs: AttributeSet?, flag:Int
    ) : super(context, attrs) {
        init(context)
    }


    private fun init(context: Context) {

        val display = (Objects.requireNonNull<Any>(
            getContext().getSystemService(Context.WINDOW_SERVICE)
        ) as WindowManager).defaultDisplay
        val point = Point()
        display.getSize(point)
        videoSurfaceDefaultHeight = point.x
        screenDefaultHeight = point.y
        videoSurfaceView = PlayerView(getContext())
        videoSurfaceView?.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
        val bandwidthMeter: BandwidthMeter = DefaultBandwidthMeter()
        val videoTrackSelectionFactory: TrackSelection.Factory =
            AdaptiveTrackSelection.Factory(bandwidthMeter)
        val trackSelector: TrackSelector = DefaultTrackSelector(videoTrackSelectionFactory)


        videoPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector)

        videoSurfaceView?.useController = false

        videoSurfaceView?.player = videoPlayer

        addOnScrollListener(object : OnScrollListener() {
            override fun onScrollStateChanged(
                @NonNull recyclerView: RecyclerView,
                newState: Int
            ) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (videoImage != null) {
                        videoImage?.visibility = VISIBLE
                    }

                    if (!recyclerView.canScrollVertically(1)) {
                        playVideo(true)
                    } else {
                        playVideo(false)
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
            }
        })
        addOnChildAttachStateChangeListener(object : OnChildAttachStateChangeListener {
            override fun onChildViewDetachedFromWindow(@NonNull view: View) {
                if (viewHolderParent != null && viewHolderParent == view) {
                    resetVideoView()
                }
            }

            override fun onChildViewAttachedToWindow(view: View) {
            }
        })

        videoPlayer?.addListener(object : Player.EventListener {
            override fun onTimelineChanged(
                timeline: Timeline,
                @Nullable manifest: Any?,
                reason: Int
            ) {
            }

            override fun onTracksChanged(
                trackGroups: TrackGroupArray,
                trackSelections: TrackSelectionArray
            ) {
            }

            override fun onLoadingChanged(isLoading: Boolean) {}
            override fun onPlayerStateChanged(
                playWhenReady: Boolean,
                playbackState: Int
            ) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                    }
                    Player.STATE_ENDED -> {
                        Log.d(
                            TAG,
                            "onPlayerStateChanged: Video ended."
                        )
                        videoPlayer?.seekTo(0)
                    }
                    Player.STATE_IDLE -> {
                    }
                    Player.STATE_READY -> {
                        Log.e(
                            TAG,
                            "onPlayerStateChanged: Ready to play."
                        )

                        if (!isVideoViewAdded) {
                            addVideoView()
                        }
                    }
                    else -> {
                    }
                }
            }

            override fun onRepeatModeChanged(repeatMode: Int) {}
            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {}
            override fun onPlayerError(error: ExoPlaybackException) {}
            override fun onPositionDiscontinuity(reason: Int) {}
            override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {}
            override fun onSeekProcessed() {}
        })
    }

    fun playVideo(isEndOfList: Boolean) {
        try {
            val targetPosition: Int
            if (!isEndOfList) {
                val startPosition: Int = (Objects.requireNonNull<Any>(
                    layoutManager
                ) as LinearLayoutManager).findFirstVisibleItemPosition()
                var endPosition: Int =
                    (layoutManager as LinearLayoutManager).findLastVisibleItemPosition()

                if (endPosition - startPosition > 1) {
                    endPosition = startPosition + 1
                }

                if (startPosition < 0 || endPosition < 0) {
                    return
                }

                // if there is more than 1 list-item on the screen
                targetPosition = if (startPosition != endPosition) {
                    val startPositionVideoHeight = getVisibleVideoSurfaceHeight(startPosition)
                    val endPositionVideoHeight = getVisibleVideoSurfaceHeight(endPosition)
                    if (startPositionVideoHeight > endPositionVideoHeight) startPosition else endPosition
                } else {
                    startPosition
                }
            } else {
                targetPosition = viewModel?.count ?: 0 - 1
            }
            Log.d(
                TAG,
                "playVideo: target position: $targetPosition"
            )

            // video is already playing so return
            if (targetPosition == playPosition) {
                return
            }

            playPosition = targetPosition
            if (videoSurfaceView == null) {
                return
            }

            videoSurfaceView?.visibility = INVISIBLE
            removeVideoView(videoSurfaceView)
            val currentPosition: Int = targetPosition - (Objects.requireNonNull<Any>(
                layoutManager
            ) as LinearLayoutManager).findFirstVisibleItemPosition()
            val child: View = getChildAt(currentPosition) ?: return
            val holder: VideoViewHolder = child.tag as VideoViewHolder
            if (holder == null) {
                playPosition = -1
                return
            }

            viewHolderParent = holder.itemView
            playerContainer = holder.playerContainer
            videoImage = holder.videoImage
            videoSurfaceView?.player = videoPlayer

            val index =
                viewModel?.cursorLoader?.getColumnIndexOrThrow(MediaStore.Video.Media.DATA) ?: -1
            viewModel?.cursorLoader?.moveToPosition(targetPosition)

            val mediaUrl = viewModel?.cursorLoader?.getString(index) ?: ""
            val videoSource: MediaSource = buildMediaSource(Uri.parse(mediaUrl), context)

            videoPlayer?.prepare(videoSource)
            videoPlayer?.playWhenReady = true
        }catch (e: Exception){
            Log.d(TAG, "$e")
        }
    }

    private fun buildMediaSource(uri: Uri, context: Context): MediaSource {
        val dataSourceFactory: DataSource.Factory =
            DefaultDataSourceFactory(
                context,
                context.getString(R.string.app_name)
            )
        return ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(uri)
    }

    private fun getVisibleVideoSurfaceHeight(playPosition: Int): Int {
        val at: Int = playPosition - (Objects.requireNonNull<Any>(
            layoutManager
        ) as LinearLayoutManager).findFirstVisibleItemPosition()
        Log.d(
            TAG,
            "getVisibleVideoSurfaceHeight: at: $at"
        )
        val child: View = getChildAt(at) ?: return 0
        val location = IntArray(2)
        child.getLocationInWindow(location)
        return if (location[1] < 0) {
            location[1] + videoSurfaceDefaultHeight
        } else {
            screenDefaultHeight - location[1]
        }
    }

    // Remove the old player
    private fun removeVideoView(videoView: PlayerView?) {
        try {
            val parent = videoView?.parent as ViewGroup ?: return
            val index = parent.indexOfChild(videoView)
            if (index >= 0) {
                parent.removeViewAt(index)
                isVideoViewAdded = false
                viewHolderParent?.setOnClickListener(null)
            }
        }catch (e: Exception){

        }
    }

    private fun addVideoView() {
        playerContainer?.addView(videoSurfaceView)
        isVideoViewAdded = true
        videoSurfaceView?.requestFocus()
        videoSurfaceView?.visibility = VISIBLE
        videoSurfaceView?.alpha = 1f
        videoImage?.visibility = GONE
    }

    private fun resetVideoView() {
        if (isVideoViewAdded) {
            removeVideoView(videoSurfaceView)
            playPosition = -1
            videoSurfaceView?.visibility = INVISIBLE
            videoImage?.visibility = VISIBLE
        }
    }

    fun releasePlayer() {
        if (videoPlayer != null) {
            videoPlayer?.release()
            videoPlayer = null
        }
        viewHolderParent = null
    }

    fun onPausePlayer() {
        if (videoPlayer != null) {
            videoPlayer?.stop(true)
        }
    }

    companion object {
        private const val TAG = "ExoPlayerRecyclerView"
    }
}