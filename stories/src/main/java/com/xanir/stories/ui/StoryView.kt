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
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GestureDetectorCompat
import androidx.lifecycle.*
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
import com.xanir.stories.pagination.StoriesRootFragment
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs


/**
 * Created by Umur Kaya on 19-Sep-20.
 */
class StoryView : ConstraintLayout ,GestureDetector.OnGestureListener,LifecycleObserver{

    private lateinit var storyViewBinding: StoryViewBinding
    var stories : StoryGroup? = null
    private var remainingTimeBar = arrayListOf<ProgressBar>()
    var currentStoryNumber = 0
    private var timer : CountDownTimer? = null
    private var isPaused = AtomicBoolean(false)
    private var totalDuration = 5000
    private var currentDuration = 0
    private var simpleExoPlayer : SimpleExoPlayer? = null
    private lateinit var gestureDetector : GestureDetectorCompat
    private var isRecentlyReleased = false

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
        (context as LifecycleOwner).lifecycle.addObserver(this)
        storyViewBinding = StoryViewBinding.inflate(LayoutInflater.from(context), this, true)
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
        currentStoryNumber = 0
        storyViewBinding.containerImage.setOnTouchListener{ v, ev ->
            val detectedUp = ev.action == MotionEvent.ACTION_UP
            val gesture = gestureDetector.onTouchEvent(ev)
            if(!gesture && detectedUp){
                resumeStories()
                return@setOnTouchListener true
            }
            else{
                return@setOnTouchListener gesture
            }
        }
        storyViewBinding.playerView.setOnTouchListener{ v, ev ->
            val detectedUp = ev.action == MotionEvent.ACTION_UP
            val gesture = gestureDetector.onTouchEvent(ev)
            if(!gesture && detectedUp){
                isRecentlyReleased = true
                resumeStories()
                return@setOnTouchListener true
            }
            else{
                return@setOnTouchListener gesture
            }
        }
        storyViewBinding.progressBars.weightSum = stories.story.size.toFloat()
        for(story in stories.story){
            val view = StoryProgressBarBinding.inflate(LayoutInflater.from(context))
            view.storyProgressBar.layoutParams = LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.MATCH_PARENT,1.0F)
            view.storyProgressBar.incrementProgressBy(1)
            view.storyProgressBar.max = 5000
            if(story.isSeen){
                view.storyProgressBar.progress = 100
                if(currentStoryNumber < stories.story.size - 1){
                    currentStoryNumber++
                }
                view.storyProgressBar.progress = 5000
            }
            remainingTimeBar.add(view.root)
            storyViewBinding.progressBars.addView(view.root)
        }
        storyViewBinding.progressBars.postInvalidate()
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
        Glide.with(storyViewBinding.userImage.context).
        load(stories.userProfilePicture).circleCrop().into(storyViewBinding.userImage)
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
                (context as StoryActivity).runOnUiThread {
                    Glide.with(context).load(resource).apply(
                        RequestOptions().override
                            (storyViewBinding.root.width, storyViewBinding.root.height)
                    ).into(storyViewBinding.containerImage)
                    isPaused.set(false)
                    createTimer(5000).start()
                }
                return true
            }
        }).submit()
    }

    private fun loadVideo(url: String) {
        simpleExoPlayer = SimpleExoPlayer.Builder(context).setUseLazyPreparation(true).build().apply {
            val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
                context, Util.getUserAgent(context, context.packageName)
            )
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse(url))
            this.prepare(mediaSource)
            this.playWhenReady = true
            this.repeatMode = Player.REPEAT_MODE_OFF
            storyViewBinding.playerView.hideController()
            storyViewBinding.playerView.also { playerView ->
                playerView.player = this
                this.playWhenReady = true
            }
            this.addListener(object : Player.EventListener {
                override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                    if (playbackState == Player.STATE_READY && playWhenReady && !isRecentlyReleased) {
                        simpleExoPlayer?.let {
                            remainingTimeBar[currentStoryNumber].max = it.duration.toInt()
                            isPaused.set(false)
                            createTimer(it.duration).start()
                        }
                    } else if (playbackState == Player.STATE_ENDED) {
                        goToNext()
                    }
                }
            })
            return@apply
        }
    }

    private fun createTimer(time: Long) : CountDownTimer {
        totalDuration = time.toInt()
        currentDuration = 0
        timer = object : CountDownTimer(60000, 100) {
            override fun onTick(l: Long) {
                if(currentDuration >= totalDuration){
                    goToNext()
                }
                else if(!isPaused.get()){
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
                start()
            }
        }
        return timer!!
    }

    private fun goToNext(){
        commonForChanges()
        if(currentStoryNumber < stories?.story!!.size - 1){
            stories?.story!![currentStoryNumber].isSeen = true
            if(stories?.story!![currentStoryNumber + 1].isPicture){
                goToNextPicture()
            }
            else if(stories?.story!![currentStoryNumber + 1].isVideo){
                goToNextVideo()
            }
        }
        else if(context is StoryActivity){
            goToNextViewPagerElement()
        }
    }

    private fun goToNextViewPagerElement(){
        (context as StoryActivity).supportFragmentManager.fragments.firstOrNull { fragment -> fragment is StoriesRootFragment }?.let {it as StoriesRootFragment
            val position = it.fragmentStoriesBinding.storiesPager.currentItem
            if(it.fragmentStoriesBinding.storiesPager.adapter!!.itemCount > position + 1){
                currentStoryNumber = 0
                it.fragmentStoriesBinding.storiesPager.setCurrentItem(position + 1, true)
            }
            else{
                (context as StoryActivity).finish()
            }
        }
    }

    private fun goToNextPicture(){
        remainingTimeBar[currentStoryNumber].progress = 5000
        currentStoryNumber++
        storyViewBinding.containerImage.visibility = VISIBLE
        storyViewBinding.playerView.visibility = GONE
        loadImage(stories?.story!![currentStoryNumber].storyUrl)
    }

    private fun goToNextVideo(){
        remainingTimeBar[currentStoryNumber].progress = totalDuration
        currentStoryNumber++
        storyViewBinding.playerView.visibility = VISIBLE
        storyViewBinding.containerImage.visibility = GONE
        loadVideo(stories?.story!![currentStoryNumber].storyUrl)
    }

    private fun goToPreviousViewPagerElement(){
        (context as StoryActivity).supportFragmentManager.fragments.firstOrNull { fragment -> fragment is StoriesRootFragment }?.let {it as StoriesRootFragment
            val position = it.fragmentStoriesBinding.storiesPager.currentItem
            if(position > 0){
                currentStoryNumber = 0
                it.fragmentStoriesBinding.storiesPager.setCurrentItem(position - 1, true)
            }
            else{
                (context as StoryActivity).onBackPressed()
            }
        }
    }

    private fun commonForChanges(){
        isRecentlyReleased = false
        timer?.cancel()
        isPaused.set(true)
        simpleExoPlayer?.let {
            it.stop()
            it.release()
        }
    }

    private fun goToPreviousVideo(){
        loadVideo(stories?.story!![currentStoryNumber].storyUrl)
        storyViewBinding.containerImage.visibility = GONE
        storyViewBinding.playerView.visibility = VISIBLE
    }

    private fun goToPreviousImage(){
        loadImage(stories?.story!![currentStoryNumber].storyUrl)
        storyViewBinding.containerImage.visibility = VISIBLE
        storyViewBinding.playerView.visibility = GONE
    }

    private fun goToPrevious(){
        commonForChanges()
        stories?.story!![currentStoryNumber].isSeen = false
        if(currentStoryNumber > 0){
            remainingTimeBar[currentStoryNumber].progress = 0
            currentStoryNumber--
            remainingTimeBar[currentStoryNumber].progress = 0
            if(stories?.story!![currentStoryNumber].isPicture){
                goToPreviousImage()
            }
            else if(stories?.story!![currentStoryNumber].isVideo){
                goToPreviousVideo()
            }
        }
        else if(context is StoryActivity){
            goToPreviousViewPagerElement()
        }
    }

    override fun onDown(e: MotionEvent?): Boolean {
        pauseStories()
        return true
    }

    override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        if (abs(e1.y - e2.y) > 250){
            return false
        }
        // right to left swipe
        if (e1.x - e2.x > 120 && abs(velocityX) > 200) {
            commonForChanges()
            if(currentStoryNumber < stories?.story!!.size - 1){
                stories?.story!![currentStoryNumber].isSeen = true
            }
            goToNextViewPagerElement()
            return true
        }
        // left to right swipe
        else if (e2.x - e1.x > 120 && abs(velocityX) > 200) {
            commonForChanges()
            stories?.story!![currentStoryNumber].isSeen = false
            goToPreviousViewPagerElement()
            return true
        }
        return false
    }

    override fun onLongPress(e: MotionEvent?) {

    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        return true
    }

    override fun onShowPress(e: MotionEvent?) {

    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        goToNext()
        return true
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun pauseStories(){
        simpleExoPlayer?.playWhenReady = false
        isPaused.set(true)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun resumeStories(){
        simpleExoPlayer?.playWhenReady = true
        isPaused.set(false)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun garbageCollector(){
        simpleExoPlayer?.release()
        simpleExoPlayer = null
    }
}