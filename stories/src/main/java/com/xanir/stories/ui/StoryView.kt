package com.xanir.stories.ui

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.CountDownTimer
import android.util.AttributeSet
import android.view.*
import android.view.animation.LinearInterpolator
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GestureDetectorCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.xanir.stories.StoryActivity
import com.xanir.stories.databinding.StoryProgressBarBinding
import com.xanir.stories.databinding.StoryViewBinding
import com.xanir.stories.models.StoryGroup
import com.xanir.stories.pagination.StoriesFragment
import com.xanir.stories.pagination.StoriesRootFragment
import kotlin.math.abs


/**
 * Created by Umur Kaya on 19-Sep-20.
 */
class StoryView : ConstraintLayout ,GestureDetector.OnGestureListener{

    private lateinit var storyViewBinding: StoryViewBinding
    private var stories : StoryGroup? = null
    private var remainingTimeBar = arrayListOf<ProgressBar>()
    private var currentStoryNumber = 0
    private lateinit var timer : CountDownTimer
    private var isPaused = false
    private var totalDuration = 5000
    private var currentDuration = 0
    private lateinit var simpleExoPlayer : SimpleExoPlayer
    private lateinit var gestureDetector : GestureDetectorCompat

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    ) {
        init(context)
    }

    private fun init(context: Context) {
        storyViewBinding = StoryViewBinding.inflate(LayoutInflater.from(context), this, true)
        //TODO If first element of view pager, remove it
        //TODO Make it lifecycle aware to pause timers/stop player etc.
        storyViewBinding.storyGoPrevious.setOnClickListener { goToPrevious() }
        gestureDetector = GestureDetectorCompat(context, this)
    }

    @SuppressLint("ClickableViewAccessibility")
    fun loadStories(stories: StoryGroup){
        this.stories = stories
        storyViewBinding.progressBars.removeAllViews()
        remainingTimeBar.clear()
        storyViewBinding.playerView.visibility = GONE
        storyViewBinding.containerImage.visibility = GONE

        storyViewBinding.containerImage.setOnTouchListener{ v, ev ->
            gestureDetector.onTouchEvent(ev)
        }
        storyViewBinding.playerView.setOnTouchListener{ v, ev ->
            gestureDetector.onTouchEvent(ev)
        }
        for(story in stories.story){
            val view = StoryProgressBarBinding.inflate(LayoutInflater.from(context))
            if(story.isSeen){
                view.storyProgressBar.progress = 100
                currentStoryNumber++
            }
            view.storyProgressBar.incrementProgressBy(1)
            view.storyProgressBar.max = 5000
            remainingTimeBar.add(view.root)
            storyViewBinding.progressBars.addView(view.root)

        }

        if(stories.story[currentStoryNumber].isPicture){
            loadImage(stories.story[currentStoryNumber].storyUrl)
            storyViewBinding.containerImage.visibility = View.VISIBLE
        }
        else if(stories.story[currentStoryNumber].isVideo){
            loadVideo(stories.story[currentStoryNumber].storyUrl)
            storyViewBinding.playerView.visibility = View.VISIBLE
        }

        storyViewBinding.progressBars.visibility = VISIBLE
        storyViewBinding.userName.text = stories.userName
        Glide.with(storyViewBinding.userImage.context).load(stories.userProfilePicture).circleCrop().into(
            storyViewBinding.userImage
        )
        invalidate()
    }

    private fun loadImage(url: String){
        Glide.with(context).load(url).apply(
            RequestOptions().override
                (storyViewBinding.root.width, storyViewBinding.root.height)
        ).listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                goToNext()
                return true
            }

            override fun onResourceReady(
                resource: Drawable?, model: Any?, target: Target<Drawable>?,
                dataSource: com.bumptech.glide.load.DataSource?, isFirstResource: Boolean
            ): Boolean {
                (context as AppCompatActivity).runOnUiThread {
                    Glide.with(context).load(resource).apply(
                        RequestOptions().override
                            (storyViewBinding.root.width, storyViewBinding.root.height)
                    ).into(storyViewBinding.containerImage)
                    createTimer(5000)
                    timer.start()
                    isPaused = false
                }
                return true
            }
        }).submit()
    }

    private fun loadVideo(url: String){
        simpleExoPlayer = SimpleExoPlayer.Builder(context).setUseLazyPreparation(true).build()
        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
            context, Util.getUserAgent(
                context,
                context.packageName
            )
        )
        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(
            Uri.parse(
                "https://www.youtube.com/watch?v=FoMlSB6ftQg"
            )
        )
        simpleExoPlayer.prepare(mediaSource)
        simpleExoPlayer.playWhenReady = true
        simpleExoPlayer.repeatMode = Player.REPEAT_MODE_OFF
        storyViewBinding.playerView.hideController()
        storyViewBinding.playerView.apply {
            player = simpleExoPlayer
            simpleExoPlayer.playWhenReady = true
        }
        simpleExoPlayer.addListener(object : Player.EventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                super.onPlayerStateChanged(playWhenReady, playbackState)
                if (playbackState == Player.STATE_READY && playWhenReady) {
                    remainingTimeBar[currentStoryNumber].max = simpleExoPlayer.duration.toInt()
                    remainingTimeBar[currentStoryNumber].incrementProgressBy(1)
                    createTimer(simpleExoPlayer.contentDuration)
                    timer.start()
                }
            }
        })
    }

    private fun createTimer(time: Long) : CountDownTimer {
        totalDuration = time.toInt()
        currentDuration = 0
        timer = object : CountDownTimer(60000, 100) {
            override fun onTick(l: Long) {
                if(currentDuration >= totalDuration){
                    goToNext()
                }
                else if(!isPaused){
                    currentDuration += 100
                    val animation = ObjectAnimator.ofInt(
                        remainingTimeBar[currentStoryNumber],
                        "progress",
                        currentDuration
                    )
                    animation.duration = 100
                    animation.interpolator = LinearInterpolator()
                    animation.start()
                }

            }

            override fun onFinish() {
                timer.start()
            }
        }
        return timer
    }

    private fun goToNext(){
        timer.cancel()
        isPaused = true
        stories?.story!![currentStoryNumber].isSeen = true
        if(currentStoryNumber < stories?.story!!.size){
            remainingTimeBar[currentStoryNumber].progress = 100
            currentStoryNumber++
            if(stories?.story!![currentStoryNumber].isPicture){
                loadImage(stories?.story!![currentStoryNumber].storyUrl)
            }
            else if(stories?.story!![currentStoryNumber].isVideo){
                loadVideo(stories?.story!![currentStoryNumber].storyUrl)
            }
        }
        else if(context is StoryActivity){
            (context as StoryActivity).supportFragmentManager.fragments.firstOrNull { fragment -> fragment is StoriesRootFragment }?.let {it as StoriesRootFragment
                val position = it.fragmentStoriesBinding.storiesPager.currentItem
                if(it.fragmentStoriesBinding.storiesPager.adapter!!.itemCount > position + 1){
                    it.fragmentStoriesBinding.storiesPager.setCurrentItem(position + 1,false)
                }
                else{
                    (context as StoryActivity).onBackPressed()
                }
            }
        }
    }

    private fun goToPrevious(){
        timer.cancel()
        isPaused = true
        stories?.story!![currentStoryNumber].isSeen = false
        if(currentStoryNumber < stories?.story!!.size){
            remainingTimeBar[currentStoryNumber].progress = 0
            currentStoryNumber--
            if(stories?.story!![currentStoryNumber].isPicture){
                loadImage(stories?.story!![currentStoryNumber].storyUrl)
            }
            else if(stories?.story!![currentStoryNumber].isVideo){
                loadVideo(stories?.story!![currentStoryNumber].storyUrl)
            }
            remainingTimeBar[currentStoryNumber].progress = 0
        }
        else if(context is StoryActivity){
            (context as StoryActivity).supportFragmentManager.fragments.firstOrNull { fragment -> fragment is StoriesRootFragment }?.let {it as StoriesRootFragment
                val position = it.fragmentStoriesBinding.storiesPager.currentItem
                if(position > 0){
                    it.fragmentStoriesBinding.storiesPager.setCurrentItem(position - 1,false)
                }
                else{
                    (context as StoryActivity).onBackPressed()
                }
            }
        }
    }

    override fun onDown(e: MotionEvent?): Boolean {
        isPaused = true
        return true
    }

    override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        if (abs(e1.y - e2.y) > 250){
            return false
        }
        // right to left swipe
        if (e1.x - e2.x > 120 && abs(velocityX) > 200) {
            goToNext()
            return true
        }
        // left to right swipe
        else if (e2.x - e1.x > 120 && abs(velocityX) > 200) {
            goToPrevious()
            return true
        }
        return false
    }

    override fun onLongPress(e: MotionEvent?) {

    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        return true
    }

    override fun onShowPress(e: MotionEvent?) {

    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        goToNext()
        return true
    }
}