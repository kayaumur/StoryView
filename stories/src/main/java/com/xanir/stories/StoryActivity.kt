package com.xanir.stories

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.xanir.stories.databinding.ActivityStoryBinding
import com.xanir.stories.pagination.StoriesRootFragment


/**
 * Created by Umur Kaya on 20-Sep-20.
 */
class StoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportFragmentManager.beginTransaction().replace(R.id.container, StoriesRootFragment()).commitAllowingStateLoss()
    }
}