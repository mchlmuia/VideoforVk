package akhmedoff.usman.videoforvk.video

import akhmedoff.usman.videoforvk.App.Companion.context
import akhmedoff.usman.videoforvk.R
import akhmedoff.usman.videoforvk.base.BaseActivity
import akhmedoff.usman.videoforvk.data.local.UserSettings
import akhmedoff.usman.videoforvk.data.repository.VideoRepository
import akhmedoff.usman.videoforvk.model.Group
import akhmedoff.usman.videoforvk.model.User
import akhmedoff.usman.videoforvk.model.Video
import akhmedoff.usman.videoforvk.utils.vkApi
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import com.google.android.exoplayer2.DefaultControlDispatcher
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_video.*
import kotlinx.android.synthetic.main.playback_exo_control_view.*
import java.text.SimpleDateFormat
import java.util.*

class VideoActivity : BaseActivity<VideoContract.View, VideoContract.Presenter>(),
    VideoContract.View {
    companion object {

        const val VIDEO_ID = "video_id"
    }

    private var player: SimpleExoPlayer? = null

    override lateinit var videoPresenter: VideoPresenter


    override fun initPresenter(): VideoContract.Presenter = videoPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        videoPresenter = VideoPresenter(
            VideoRepository(
                UserSettings.getUserSettings(applicationContext), vkApi
            )
        )
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)

        fullscreen_toggle.setOnClickListener { videoPresenter.clickFullscreen() }

    }

    override fun showVideo(item: Video) {
        initVideoInfo(item)
        initExoPlayer(item)
    }

    override fun getVideoState() = player?.playWhenReady

    override fun getVideoPosition() = player?.currentPosition

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        videoPresenter.changedConfiguration(newConfig)
    }

    override fun getVideoId() = intent.getStringExtra(VideoActivity.VIDEO_ID)!!

    override fun showLoadError() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun showRecommendatons() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun initVideoInfo(item: Video) {
        video_title?.text = item.title
        video_views?.text = item.views.toString()

        video_date?.text = SimpleDateFormat(
            "HH:mm, dd MMM ",
            Locale.getDefault()
        ).format(Date(item.date))
    }

    private fun initExoPlayer(item: Video) {
        val mp4VideoUri = Uri.parse(
            when {
                item.files.hls != null -> item.files.hls
                item.files.external != null -> item.files.external
                item.files.mp41080 != null -> item.files.mp41080
                item.files.mp4720 != null -> item.files.mp4720
                item.files.mp4480 != null -> item.files.mp4480
                item.files.mp4360 != null -> item.files.mp4360
                else -> item.files.mp4240
            }
        )
        // 1. Create a default TrackSelector
        val bandwidthMeter = DefaultBandwidthMeter()
        val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory(bandwidthMeter)
        val trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)

        // 2. Create the player
        player = ExoPlayerFactory.newSimpleInstance(context, trackSelector)

        simpleExoPlayerView.setControlDispatcher(
            object : DefaultControlDispatcher() {
                override fun dispatchSetPlayWhenReady(
                    player: Player?,
                    playWhenReady: Boolean
                ): Boolean {
                    item.files.external?.let {
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(it)
                            )
                        )
                        return false
                    }
                    return super.dispatchSetPlayWhenReady(player, playWhenReady)
                }
            }
        )

        simpleExoPlayerView.player = player

        // Produces DataSource instances through which media data is loaded.
        val dataSourceFactory = DefaultDataSourceFactory(
            context,
            Util.getUserAgent(context, "yourApplicationName"), bandwidthMeter
        )
        // This is the MediaSource representing the media to be played.
        val videoSource = when {
            item.files.hls != null -> HlsMediaSource.Factory(dataSourceFactory)
            else -> ExtractorMediaSource.Factory(dataSourceFactory)
        }.createMediaSource(mp4VideoUri, null, null)

        // Prepare the player with the source.
        player?.prepare(videoSource)
    }

    override fun showGroupOwnerInfo(group: Group) {
        owner_name?.text = group.name
        owner_photo?.let {
            Picasso.with(context).load(group.photo100).into(it)
        }

        owner_follow?.text = when {
            group.isMember -> getText(R.string.followed)
            else -> getText(R.string.follow)
        }
    }

    override fun showUserOwnerInfo(user: User) {
        owner_name?.text =
                String.format(
                    resources.getText(R.string.user_name).toString(),
                    user.firstName,
                    user.lastName
                )
        Picasso.with(context).load(user.photo100).into(owner_photo)

        owner_follow?.text = when {
            user.isFavorite -> getText(R.string.followed)
            else -> getText(R.string.follow)
        }

    }

    override fun pauseVideo() {
        player?.playWhenReady = false
    }

    override fun resumeVideo(state: Boolean, position: Long) {
        player?.playWhenReady = state
        player?.seekTo(position)
    }

    override fun startVideo() {
        player?.playWhenReady = true
    }

    override fun stopVideo() {
        player?.playWhenReady = false
        player?.release()
    }

    override fun setSaved(saved: Boolean) {
    }

    override fun showFullscreen() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    override fun showSmallScreen() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

}