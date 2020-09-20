package com.xanir.instastories

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.xanir.instastories.databinding.ActivityMainBinding
import com.xanir.stories.StoryActivity
import com.xanir.stories.models.Story
import com.xanir.stories.models.StoryGroup
import com.xanir.stories.pagination.StoriesRootFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.startStories.setOnClickListener {
            startActivity(Intent(this,StoryActivity::class.java).apply { this.flags = FLAG_ACTIVITY_NEW_TASK })
        }
    }
}